/*
 * Copyright (C) 2007 Science and Technology Facilities Council.
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

package edu.jach.qt.utils ;

import java.util.Timer ;
import java.util.TimerTask ;

public class OMPTimer
{
	private static final OMPTimer omptimer = new OMPTimer() ;
	Timer timer ;
	
	private OMPTimer()
	{
		timer = new Timer( "OMPTimer" ) ;
	}
	
	public static OMPTimer getOMPTimer()
	{
		return omptimer ;
	}
	
	public TimerTask setTimer( long interval , final OMPTimerListener listener )
	{
		return setTimer( interval , listener , true ) ;
	}
	
	public TimerTask setTimer( long interval , final OMPTimerListener listener , boolean repeat )
	{
		TimerTask task = new TimerTask()
		{ 
			public void run()
			{
				listener.timeElapsed() ;
				timer.purge() ;
			} 
		} ;
		if( repeat )
			timer.scheduleAtFixedRate( task , interval , interval ) ;
		else
			timer.schedule( task , interval , interval ) ;
		
		return task ;
	}
}
