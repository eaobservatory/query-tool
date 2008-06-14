package edu.jach.qt.gui ;

import java.awt.Color ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.TimeZone ;
import javax.swing.JLabel ;
import javax.swing.SwingConstants ;
import java.text.SimpleDateFormat ;

import edu.jach.qt.utils.OMPTimerListener ;
import edu.jach.qt.utils.OMPTimer ;

/**
 * Display the UTC time in a panel as a <code>JLabel</code>..
 *
 * @author $Author$
 * @version $Id$
 */

public class UTPanel extends JLabel implements OMPTimerListener
{
	/**
	 * Constructor.
	 * Starts a timer so that the display is updated once a second.
	 */
	public UTPanel()
	{
		setHorizontalAlignment( SwingConstants.CENTER ) ;

		this.setOpaque( true ) ;
		OMPTimer.getOMPTimer().setTimer( 1000 , this ) ;
	}

	/**
	 * Implementation of TimeListener.
	 * Sets the date and time on the associated object once a second.
	 * @param evt  A timer event.
	 */
	public void timeElapsed()
	{
		setBackground( Color.black ) ;
		setForeground( Color.green ) ;
		Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ) ;
		Date date = cal.getTime() ;
		SimpleDateFormat df = new SimpleDateFormat( "kk:mm:ss z" ) ;
		df.setTimeZone( TimeZone.getTimeZone( "UTC" ) ) ;
		String time = df.format( date ) ;
		setText( time ) ;
	}

}// TimePanel
