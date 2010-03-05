package edu.jach.qt.gui ;

// Standard imports
import java.awt.Dimension ;
import java.awt.GridLayout ;

import javax.swing.DefaultListModel ;
import javax.swing.JLabel ;
import javax.swing.JList ;
import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.JScrollPane ;
import javax.swing.ListSelectionModel ;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener ;

// OT imports
import orac.util.OrderedMap ;
import gemini.sp.SpItem ;

// QT imports
import edu.jach.qt.utils.CalibrationList ;

@SuppressWarnings( "serial" )
public class CalibrationsPanel extends JPanel implements ListSelectionListener , Runnable
{
	private final static String AND_STRING = "AND Folder: " ;

	private JPanel left = new JPanel() ;
	private JPanel right = new JPanel() ;

	private JList firstList ;
	private JList secondList ;

	private JScrollPane firstScrollPane ;
	private JScrollPane secondScrollPane ;

	private GridLayout gridlayout = new GridLayout( 1 , 2 ) ;

	private OrderedMap<String,OrderedMap<String,SpItem>> calibrationList ;
	private OrderedMap<String,SpItem> currentList ;
	
	private JLabel waiting = new JLabel( "Waiting for database ..." ) ;

	public CalibrationsPanel()
	{
		this.setLayout( gridlayout ) ;
		this.add( left ) ;
		this.add( right ) ;

		left.add( waiting ) ;
	}
	
	public void init()
	{
		Thread thread = new Thread( this ) ;
		thread.start() ;
	}

	private DefaultListModel setup()
	{
		DefaultListModel listModel = new DefaultListModel() ;
		calibrationList = CalibrationList.getCalibrations() ;
		int trimLength = AND_STRING.length() ;
		for( int index = 0 ; index < calibrationList.size() ; index++ )
		{
			OrderedMap<String,SpItem> folder = calibrationList.find( index ) ;
			if( folder.size() != 0 )
			{
				String key = calibrationList.getNameForIndex( index ) ;
				if( key.startsWith( "AND" ) )
					listModel.addElement( key.substring( trimLength ) ) ;
			}
		}
		return listModel ;
	}
	
	public void run()
	{
		firstList = new JList( setup() ) ;
		firstList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		firstList.setLayoutOrientation( JList.HORIZONTAL_WRAP );
		firstList.setVisibleRowCount( -1 ) ;
		firstList.addListSelectionListener( this ) ;
		firstScrollPane = new JScrollPane( firstList ) ;
		firstScrollPane.setPreferredSize( new Dimension( 350 , 400 ) ) ;
		left.remove( waiting ) ;
		left.add( firstScrollPane ) ;

		secondList = new JList() ;
		secondList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		secondList.setLayoutOrientation( JList.HORIZONTAL_WRAP );
		secondList.setVisibleRowCount( -1 ) ;
		secondList.addListSelectionListener( this ) ;
		secondScrollPane = new JScrollPane( secondList ) ;
		secondScrollPane.setPreferredSize( new Dimension( 350 , 400 ) ) ;
		right.add( secondScrollPane ) ;
	}

	private DefaultListModel second( String selection )
	{
		DefaultListModel listModel = new DefaultListModel() ;
		currentList = calibrationList.find( AND_STRING + selection ) ;
		for( int index = 0 ; index < currentList.size() ; index++ )
			listModel.addElement( currentList.find( index ).getTitleAttr() ) ;

		return listModel ;
	}

	public void valueChanged( ListSelectionEvent e )
	{
		Object source = e.getSource() ;
		if( source instanceof JList )
		{
			JList list = ( JList )source ;
			Object value = list.getSelectedValue() ;
			if( value != null && value instanceof String )
			{
				if( list.equals( firstList ) )
				{
					secondList.setModel( second( ( String )value ) ) ;
				}
				else if( list.equals( secondList ) && e.getValueIsAdjusting() )
				{
					if( currentList != null )
					{
						SpItem item = currentList.find( ( String )value ) ;
						DeferredProgramList.addCalibration( item ) ;
						JOptionPane.showMessageDialog( this , "'" + value + "' added to deferred observations." ) ;
					}
				}
			}
		}
	}
}
