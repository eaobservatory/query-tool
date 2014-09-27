/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.gui ;

import java.awt.Color ;
import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.net.URL ;
import javax.swing.JPanel ;
import javax.swing.JButton ;
import javax.swing.AbstractAction ;
import javax.swing.ImageIcon ;
import javax.swing.Timer ;

/**
 * Led.java
 *
 *
 * Created: Wed Nov 21 13:28:52 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @deprecated  Not repalced.
 */

@SuppressWarnings( "serial" )
public class Led extends JPanel
{
	/** Handle for the "LED" button */
	protected JButton led ;

	/** Action linked to the stop button and led */
	protected AbstractAction stopAction = new AbstractAction( "Stop" )
	{
		public void actionPerformed( ActionEvent evt )
		{
			this.setEnabled( false ) ;
		}
	} ;

	/** Timer used for blinking the led */
	protected Timer timer ;

	/** The current state of the led (for blinking) */
	protected boolean ledState ;

	public Led()
	{
		setBackground( Color.black ) ;
		add( createLed() ) ;
		led.setEnabled( false ) ;
	}

	/**
	 * Create the red LED button
	 */
	protected JButton createLed()
	{
		URL url = ClassLoader.getSystemResource( "green_led.gif" ) ;
		led = new JButton( new ImageIcon( url ) ) ;
		led.setBackground( Color.black ) ;
		led.setFocusPainted( false ) ;
		led.setBorderPainted( false ) ;

		url = ClassLoader.getSystemResource( "green_led_disabled.gif" ) ;
		led.setDisabledIcon( new ImageIcon( url ) ) ;
		led.addActionListener( stopAction ) ;

		return led ;
	}

	protected void blinkLed( boolean enabled )
	{
		led.setEnabled( ledState = enabled ) ;
		if( enabled )
		{
			if( timer == null )
			{
				timer = new Timer( 200 , new ActionListener()
				{
					public void actionPerformed( ActionEvent ev )
					{
						ledState = !ledState ;
						led.setEnabled( ledState ) ;
					}
				} ) ;
				timer.start() ;
			}
			else
			{
				timer.restart() ;
			}
		}
		else
		{
			if( timer != null )
				timer.stop() ;
		}
	}
}// Led
