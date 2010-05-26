package edu.jach.qt.gui ;

/* QT imports */
import edu.jach.qt.app.Querytool ;

/* Standard imports */

import java.awt.Cursor ;
import java.awt.GridBagLayout ;
import java.awt.Color ;
import java.awt.Dimension ;
import java.awt.GridBagConstraints ;
import java.awt.Insets ;
import java.awt.Component ;
import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import edu.jach.qt.utils.OMPTimer ;
import edu.jach.qt.utils.OMPTimerListener ;

import java.net.URL ;
import java.util.TimerTask ;

import javax.swing.JPanel ;
import javax.swing.JButton ;
import javax.swing.ImageIcon ;
import javax.swing.SwingConstants ;
import javax.swing.border.MatteBorder ;

import gemini.util.JACLogger ;
import gemini.util.ObservingToolUtilities ;

import edu.jach.qt.utils.SpQueuedMap ;

/**
 * InfoPanel.java
 *
 *
 * Created: Tue Apr 24 16:28:12 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
@SuppressWarnings( "serial" )
public class InfoPanel extends JPanel implements ActionListener , OMPTimerListener
{
	private static final JACLogger logger = JACLogger.getLogger( InfoPanel.class ) ;

	/**
	 * The constant <code>LOGO_IMAGE</code> specifies the String
	 * location for the QT logo image.
	 * 
	 */
	public static final String LOGO_IMAGE = System.getProperty( "qtLogo" ) ;

	/**
	 * The constant <code>SAT_WEBPAGE</code> specifies the webpage
	 * containing the String of the latest image to show.
	 * 
	 */
	public static final String SAT_WEBPAGE = System.getProperty( "satellitePage" ) ;

	/**
	 * The constant <code>IMG_PREFIX</code> is the static portion of the
	 * image source URL.
	 * 
	 */
	public static final String IMG_PREFIX = System.getProperty( "imagePrefix" ) ;

	/**
	 * The variable <code>searchButton</code> is the button clicked to
	 * start a query.
	 * 
	 */
	public static JButton searchButton = new JButton() ;

	/**
	 * The variable <code>logoPanel</code> is a reference to the easily get the LogoPanel.
	 *
	 */
	public static LogoPanel logoPanel = new LogoPanel() ;
	private TelescopeDataPanel telescopeInfoPanel ;
	private MSBQueryTableModel msb_qtm ;
	private TimePanel timePanel ;
	private SatPanel satPanel ;
	private Querytool localQuerytool ;
	private QtFrame qtf ;
	private JButton exitButton ;
	private JButton fetchMSB ;
	private TimerTask queryTask ;
	final private InfoPanel infoPanel ;
	final private Cursor busyCursor = new Cursor( Cursor.WAIT_CURSOR ) ;
	final private Cursor normalCursor = new Cursor( Cursor.DEFAULT_CURSOR ) ;

	/**
	 * Creates a new <code>InfoPanel</code> instance.
	 *
	 * @param msbQTM a <code>MSBQueryTableModel</code> value
	 * @param qt a <code>Querytool</code> value
	 * @param qtFrame a <code>QtFrame</code> value
	 */
	public InfoPanel( MSBQueryTableModel msbQTM , Querytool qt , QtFrame qtFrame )
	{
		super() ;
		localQuerytool = qt ;
		msb_qtm = msbQTM ;
		qtf = qtFrame ;
		
		infoPanel = this ;

		MatteBorder matte = new MatteBorder( 3 , 3 , 3 , 3 , Color.green ) ;
		GridBagLayout gbl = new GridBagLayout() ;

		setBackground( Color.black ) ;
		setBorder( matte ) ;
		setLayout( gbl ) ;
		setMinimumSize( new Dimension( 174 , 450 ) ) ;
		setPreferredSize( new Dimension( 174 , 450 ) ) ;
		setMaximumSize( new Dimension( 174 , 450 ) ) ;

		compInit() ;
	}

	private void compInit()
	{
		final GridBagConstraints gbc = new GridBagConstraints() ;

		exitButton = new JButton() ;
		fetchMSB = new JButton() ;
		timePanel = new TimePanel() ;
		satPanel = new SatPanel() ;
		telescopeInfoPanel = new TelescopeDataPanel( this ) ;

		/* Setup the SEARCH button */
		InfoPanel.searchButton.setText( "Search" ) ;
		InfoPanel.searchButton.setName( "Search" ) ;
		final URL url = ObservingToolUtilities.resourceURL( "green_light1.gif" ) ;
		final ImageIcon icon = new ImageIcon( url ) ;
		InfoPanel.searchButton.setIcon( icon ) ;
		blinkIcon() ;
		InfoPanel.searchButton.setHorizontalTextPosition( SwingConstants.LEADING ) ;
		InfoPanel.searchButton.setToolTipText( "Red icon - all constraints disabled" ) ;
		InfoPanel.searchButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				searchButton.setEnabled( false ) ;
				qtf.setCursor( busyCursor ) ;
				qtf.getWidgets().setButtons() ;
				qtf.updateColumnSizes() ;
				qtf.repaint( 0 ) ;

				ChecksumCacheThread checksumCacheThread = new ChecksumCacheThread() ;
				checksumCacheThread.start() ;

				final SwingWorker worker = new SwingWorker()
				{
					Boolean isStatusOK ;

					public Object construct()
					{
						isStatusOK = new Boolean( localQuerytool.queryMSB() ) ;
						return isStatusOK ; // not used yet
					}

					// Runs on the event-dispatching thread.
					public void finished()
					{
						logoPanel.stop() ;
						qtf.setCursor( normalCursor ) ;
						searchButton.setEnabled( true ) ;
						if( isStatusOK )
						{
							Thread tableFill = new Thread( msb_qtm ) ;
							tableFill.start() ;
							try
							{
								tableFill.join() ;
							}
							catch( InterruptedException iex )
							{
								logger.warn( "Problem joining tablefill thread" ) ;
							}

							synchronized( this )
							{
								Thread projectFill = new Thread( qtf.getProjectModel() ) ;
								projectFill.start() ;
								try
								{
									projectFill.join() ;
								}
								catch( InterruptedException iex )
								{
									logger.warn( "Problem joining projectfill thread" ) ;
								}

								qtf.initProjectTable() ;
							}
							msb_qtm.setProjectId( "All" ) ;
							qtf.setColumnSizes() ;
							qtf.resetScrollBars() ;
							logoPanel.stop() ;
							qtf.setCursor( normalCursor ) ;
							if( queryTask != null )
								queryTask.cancel() ;
							qtf.setQueryExpired( false ) ;
							String queryTimeout = System.getProperty( "queryTimeout" ) ;
							System.out.println( "Query expiration: " + queryTimeout ) ;
							Integer timeout = new Integer( queryTimeout ) ;
							if( timeout != 0 )
							{
								int delay = timeout * 60 * 1000 ; // Conversion from minutes of milliseconds
								queryTask = OMPTimer.getOMPTimer().setTimer( delay , infoPanel , false ) ;
							}
						}
					}
				} ;
				logger.info( "Query Sent" ) ;

				localQuerytool.printXML() ;
				logoPanel.start() ;
				worker.start() ; // required for SwingWorker 3
			}
		} ) ;

		InfoPanel.searchButton.setBackground( java.awt.Color.gray ) ;

		fetchMSB.setText( "Fetch MSB" ) ;
		fetchMSB.setName( "Fetch MSB" ) ;
		fetchMSB.setBackground( java.awt.Color.gray ) ;
		fetchMSB.addActionListener( this ) ;

		/* Setup the EXIT button */
		exitButton.setText( "Exit" ) ;
		exitButton.setName( "Exit" ) ;
		exitButton.setBackground( java.awt.Color.gray ) ;
		exitButton.addActionListener( this ) ;

		gbc.fill = GridBagConstraints.BOTH ;
		gbc.anchor = GridBagConstraints.NORTH ;
		gbc.weighty = 0.0 ;
		add( logoPanel , gbc , 0 , 0 , 1 , 1 ) ;

		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.anchor = GridBagConstraints.CENTER ;
		gbc.insets = new Insets( 2 , 15 , 2 , 15 ) ;
		gbc.weighty = 0.0 ;

		/* Add all the buttons */
		add( InfoPanel.searchButton , gbc , 0 , 5 , 1 , 1 ) ;
		add( fetchMSB , gbc , 0 , 10 , 1 , 1 ) ;
		add( exitButton , gbc , 0 , 15 , 1 , 1 ) ;

		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.anchor = GridBagConstraints.CENTER ;
		gbc.weightx = 100 ;
		gbc.insets = new Insets( 0 , 2 , 0 , 2 ) ;
		gbc.weighty = 0 ;
		add( telescopeInfoPanel , gbc , 0 , 20 , 1 , 1 ) ;

		add( satPanel , gbc , 0 , 30 , 1 , 1 ) ;

		gbc.fill = GridBagConstraints.HORIZONTAL ;
		gbc.weightx = 100 ;
		gbc.weighty = 0 ;
		gbc.insets.left = 0 ;
		gbc.insets.right = 0 ;
		add( timePanel , gbc , 0 , 40 , 1 , 1 ) ;
	}

	/**
	 * Get the parent frame.
	 * @return The parent QT Frame object.
	 */
	public QtFrame getFrame()
	{
		return qtf ;
	}

	/**
	 * Get the current query.
	 * @return The current <code>QueryTool</code> object.
	 */
	public Querytool getQuery()
	{
		return localQuerytool ;
	}

	private void add( Component c , GridBagConstraints gbc , int x , int y , int w , int h )
	{
		gbc.gridx = x ;
		gbc.gridy = y ;
		gbc.gridwidth = w ;
		gbc.gridheight = h ;
		add( c , gbc ) ;
	}

	/**
	 * <code>getXMLquery</code> will get the String that contains the
	 * current XML defining the query.
	 *
	 * @return a <code>String</code> representing the query. 
	 */
	public String getXMLquery()
	{
		return localQuerytool.getXML() ;
	}

	/**
	 * Get the <code>TelescopeDataPanel</code>
	 * @return  the current telescope panel.
	 */
	public TelescopeDataPanel getTelescopeDataPanel()
	{
		return telescopeInfoPanel ;
	}

	/**
	 * Get the <code>SatPanel</code>
	 * @return  the current satellite panel.
	 */
	public SatPanel getSatPanel()
	{
		return satPanel ;
	}

	/**
	 * <code>actionPerformed</code> satisfies the ActionListener
	 * interface.  This is called when any ActionEvents are triggered by
	 * registered ActionListeners. In this case it's either the exit
	 * button or the fetchMSB button.
	 *
	 * @param e an <code>ActionEvent</code> value 
	 */
	public void actionPerformed( ActionEvent e )
	{
		Object source = e.getSource() ;
		if( source == exitButton )
		{
			if( TelescopeDataPanel.DRAMA_ENABLED )
				telescopeInfoPanel.closeHub() ;

			qtf.exitQT() ;
		}
		else if( source == fetchMSB )
		{
			qtf.sendToStagingArea() ;
		}
	}

	public void timeElapsed()
	{
			System.out.println( "Query has expired" ) ;
			qtf.setQueryExpired( true ) ;
			if( queryTask != null )
				queryTask.cancel() ;
	}

	private void blinkIcon()
	{
		javax.swing.Timer t = new javax.swing.Timer( 500 , new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				ImageIcon imageIcon = ( ImageIcon )InfoPanel.searchButton.getIcon() ;
				if( imageIcon == null )
					return ;
				String iconName = imageIcon.toString() ;
				if( iconName == null || iconName.indexOf( "green_light" ) != -1 )
				{
					return ;
				}
				else
				{
					if( iconName.indexOf( "_light1" ) != -1 )
					{
						// Set light to light 2
						try
						{
							java.net.URL url = new java.net.URL( iconName.replaceAll( "light1" , "light2" ) ) ;
							ImageIcon icon = new ImageIcon( url ) ;
							InfoPanel.searchButton.setIcon( icon ) ;
						}
						catch( Exception x )
						{
							//Ignore
						}
					}
					else if( iconName.indexOf( "_light2" ) != -1 )
					{
						// Set light to light 1
						try
						{
							java.net.URL url = new java.net.URL( iconName.replaceAll( "light2" , "light1" ) ) ;
							ImageIcon icon = new ImageIcon( url ) ;
							InfoPanel.searchButton.setIcon( icon ) ;
						}
						catch( Exception x )
						{
							//Ignore
						}
					}
					else
					{
						// Do nothing
					}
				}
			}
		} ) ;
		t.start() ;
	}

	public class ChecksumCacheThread extends Thread
	{
		public void run()
		{
			SpQueuedMap map = SpQueuedMap.getSpQueuedMap() ;
			map.fillCache() ;
		}
	}
}// InfoPanel

