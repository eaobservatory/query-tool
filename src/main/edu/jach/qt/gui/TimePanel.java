package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.SimpleDateFormat;

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

	JLabel local ;
	JLabel universal ;
	SimpleDateFormat localDateFormatter ;
	SimpleDateFormat universalDateFormatter ;
	/**
	 * Constructor.
	 * Sets a timer running and adds a listener.
	 */
	public TimePanel()
	{
		setBackground( Color.black ) ;
		
		local = new JLabel() ;
		universal = new JLabel() ;
		
		local.setHorizontalAlignment( SwingConstants.CENTER );
		universal.setHorizontalAlignment( SwingConstants.CENTER );
		
		local.setBackground( Color.black );
		universal.setBackground( Color.black );
		local.setForeground( Color.green );
		universal.setForeground( Color.green );
		
		local.setOpaque( true );
		universal.setOpaque( true );
		
		setLayout( new GridLayout( 2 , 1 ) ) ;
		
		add( local ) ;
		add( universal ) ;
		
		localDateFormatter = new SimpleDateFormat( "kk.mm.ss z" );
		universalDateFormatter = new SimpleDateFormat( "kk.mm.ss z" );
		universalDateFormatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) ) ;

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
		String localTime = localDateFormatter.format( date ) ;
		String universalTime = universalDateFormatter.format( date ) ;
		local.setText( localTime ) ;
		universal.setText( universalTime );
	}

}// TimePanel
