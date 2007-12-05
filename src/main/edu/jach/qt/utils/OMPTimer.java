package edu.jach.qt.utils ;

import java.util.Timer ;
import java.util.TimerTask ;

public class OMPTimer
{
	private static final OMPTimer omptimer = new OMPTimer() ;
	Timer timer ;
	
	private OMPTimer()
	{
		timer = new Timer() ;
	}
	
	public static OMPTimer getOMPTimer()
	{
		return omptimer ;
	}
	
	public void setTimer( long interval , final OMPTimerListener listener )
	{
		setTimer( interval , listener , true ) ;
	}
	
	public void setTimer( long interval , final OMPTimerListener listener , boolean repeat )
	{
		TimerTask task = new TimerTask()
		{ 
			public void run()
			{
				listener.timeElapsed() ; 
			} 
		} ;
		if( repeat )
			timer.scheduleAtFixedRate( task , interval , interval ) ;
		else
			timer.schedule( task , interval , interval ) ;
	}
}