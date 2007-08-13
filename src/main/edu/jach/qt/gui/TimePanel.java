package edu.jach.qt.gui;

import java.awt.GridLayout;
import java.awt.Color;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.text.SimpleDateFormat;

import edu.jach.qt.utils.LocalSiderealTime;

/**
 * Class associateing local time with a label for display on an interface.
 *
 *
 * Created: Fri Apr 20 14:55:26 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version $Id$
 */

public class TimePanel extends JPanel implements TimerListener
{
	JLabel local;
	JLabel universal;
	SimpleDateFormat localDateFormatter;
	SimpleDateFormat universalDateFormatter;
	LocalSiderealTime localSiderealTime;
	JLabel lst;

	/**
	 * Constructor.
	 * Sets a timer running and adds a listener.
	 */
	public TimePanel()
	{
		setBackground( Color.black );

		local = new JLabel();
		universal = new JLabel();
		lst = new JLabel();

		local.setHorizontalAlignment( SwingConstants.CENTER );
		universal.setHorizontalAlignment( SwingConstants.CENTER );
		lst.setHorizontalAlignment( SwingConstants.CENTER );

		local.setBackground( Color.black );
		universal.setBackground( Color.black );
		lst.setBackground( Color.black );
		local.setForeground( Color.green );
		universal.setForeground( Color.green );
		lst.setForeground( Color.green );

		local.setOpaque( true );
		universal.setOpaque( true );
		lst.setOpaque( true );

		setLayout( new GridLayout( 3 , 1 ) );

		add( local );
		add( universal );
		add( lst );

		localDateFormatter = new SimpleDateFormat( "kk.mm.ss z" );
		universalDateFormatter = new SimpleDateFormat( "kk.mm.ss z" );
		universalDateFormatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

		localSiderealTime = new LocalSiderealTime( System.getProperty( "telescope" ).trim() );

		Timer t = new Timer( 1000 );
		t.addTimerListener( this );
	}

	/**
	 * Implenetation of the <code>timeElapsed</code> interface.
	 * Updates the associated label.
	 * @param evt   the <code>TimerEvent</code> to consume.
	 */
	public void timeElapsed( TimerEvent evt )
	{
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		localSiderealTime.setDate();
		String localTime = localDateFormatter.format( date );
		String universalTime = universalDateFormatter.format( date );
		local.setText( localTime );
		universal.setText( universalTime );
		lst.setText( localSiderealTime.stime() );
	}

}// TimePanel
