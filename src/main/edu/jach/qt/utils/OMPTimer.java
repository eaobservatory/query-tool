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