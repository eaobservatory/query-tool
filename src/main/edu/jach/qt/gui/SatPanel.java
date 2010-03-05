package edu.jach.qt.gui ;

import java.awt.Color ;
import java.awt.Image ;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.lang.Exception ;
import java.net.URL ;
import java.util.StringTokenizer ;
import javax.swing.JLabel ;
import javax.swing.SwingConstants ;
import javax.swing.BorderFactory ;
import javax.swing.ImageIcon ;
import javax.swing.border.TitledBorder ;

import edu.jach.qt.utils.OMPTimer ;
import edu.jach.qt.utils.OMPTimerListener ;

/**
 * Associates an image with a label.
 *In this case, the image is a satelliet image derived from a configurable URL.
 *
 * Created: Mon Apr  8 10:18:45 2002
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @version $Id$
 */

@SuppressWarnings( "serial" )
public class SatPanel extends JLabel implements OMPTimerListener
{
	private TitledBorder satBorder ;
	private static String currentWebPage ;

	/**
	 * Constructor.
	 * Sets the look and feel of the <code>JLabel</code> and associates a timer
	 * with it to update the display.
	 */
	public SatPanel()
	{
		setHorizontalAlignment( SwingConstants.CENTER ) ;
		satBorder = BorderFactory.createTitledBorder( BorderFactory.createLineBorder( new Color( 51 , 134 , 206 ) ) , "Loading Sat..." , TitledBorder.CENTER , TitledBorder.DEFAULT_POSITION ) ;
		satBorder.setTitleColor( new Color( 51 , 134 , 206 ) ) ;
		setBorder( satBorder ) ;

		if( System.getProperty( "telescope" ).equalsIgnoreCase( "jcmt" ) )
			currentWebPage = System.getProperty( "satelliteWVPage" ) ;
		else
			currentWebPage = System.getProperty( "satelliteIRPage" ) ;

		refreshIcon() ;

		OMPTimer.getOMPTimer().setTimer( 600000 , this ) ; //refresh every 10 minutes
	}

	// implementation of edu.jach.qt.gui.TimerListener interface

	/**
	 * Implementation of edu.jach.qt.utils.OMPTimerListener interface
	 */
	public void timeElapsed()
	{
		refreshIcon() ;
	}

	/**
	 * Redraws the associated image.
	 */
	public void refreshIcon()
	{
		URL url ;
		try
		{
			url = new URL( currentWebPage ) ;
		}
		catch( Exception mue )
		{
			System.out.println( "Unable to convert to URL" ) ;
			url = null ;
		}
		final URL thisURL = url ;
		SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				try
				{
					String imageSuffix = URLReader.getImageString( thisURL ) ;
					String timeString = imageSuffix.substring( imageSuffix.lastIndexOf( "/" ) + 1 , imageSuffix.lastIndexOf( "/" ) + 13 ) ;

					// Make sure we scale the image
					ImageIcon icon = new ImageIcon( new URL( InfoPanel.IMG_PREFIX + imageSuffix ) ) ;
					icon.setImage( icon.getImage().getScaledInstance( 112 , 90 , Image.SCALE_DEFAULT ) ) ;
					setIcon( icon ) ;

					satBorder.setTitle( timeString + " UTC" ) ;
				}
				catch( Exception e ){}
				return null ;
			}
		} ;
		if( url != null )
			worker.start() ;
	}

	/**
	 * Method to set the currently displayed image.  If the input string
	 * is "Water Vapour", a water vapour image is displayed.  Otherwise
	 * and infra red image is displayed.
	 * @param image  The type of satellite image to display.
	 */
	public void setDisplay( String image )
	{
		if( image.equalsIgnoreCase( "Water Vapour" ) )
			currentWebPage = System.getProperty( "satelliteWVPage" ) ;
		else
			currentWebPage = System.getProperty( "satelliteIRPage" ) ;

		refreshIcon() ;
	}
}// SatPanel

/**
 * Reads a URL.
 */
class URLReader
{

	/**
	 * Get the String associated with a URL.
	 * @param url  The URL associated with the satellite image.
	 * @return The name of the Image.
	 * @exception Exception if unable to open the URL.
	 */
	public static String getImageString( URL url ) throws Exception
	{
		String imgString = "" ;
		String inputLine , html = "" ;
		BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) ) ;
		while( ( inputLine = in.readLine() ) != null )
			html = html + inputLine ;

		in.close() ;

		StringTokenizer st = new StringTokenizer( html ) ;

		while( st.hasMoreTokens() )
		{
			String temp = st.nextToken() ;
			if( temp.startsWith( "SRC" ) )
			{
				imgString = temp.substring( 4 , temp.indexOf( '>' ) ) ;
				break ;
			}
		}

		return imgString ;
	}
}
