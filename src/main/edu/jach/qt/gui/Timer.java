package edu.jach.qt.gui;

import java.awt.Component ;
import java.awt.Toolkit ;
import java.awt.EventQueue ;
import java.awt.AWTEvent ;

/**
 * Class to allow generation of a <code>TimerEvent</code> at a
 * specified rate.
 */
public class Timer extends Component implements Runnable
{
	/**
	 * Constructor.
	 * Starts a new thread and starts the timer in that thread.
	 * @param i     the interval between successive events in milliseconds.
	 */
	public Timer( int i )
	{
		interval = i;
		Thread t = new Thread( this );
		t.start();
		evtq = Toolkit.getDefaultToolkit().getSystemEventQueue();
		enableEvents( 0 );
	}

	/**
	 * Adds a TimerListener object to the current class.
	 * @param l  The TimeListener to add.
	 */
	public void addTimerListener( TimerListener l )
	{
		listener = l;
	}

	/**
	 * Implementation of the <code>Runnable</code> interface.
	 * Posts TimerEvents on the event queue.
	 */
	public void run()
	{
		while( true )
		{
			try
			{
				Thread.sleep( interval );
			}
			catch( InterruptedException e ){}
			TimerEvent te = new TimerEvent( this );
			evtq.postEvent( te );
		}
	}

	/**
	 * Prcess TimerEvents on the queue.  Anything that is a TimerEvent
	 * is dealt with here, otherwise we let it propogate up.
	 * @param evt An event on the queue being monitored.
	 */
	public void processEvent( AWTEvent evt )
	{
		if( evt instanceof TimerEvent )
		{
			if( listener != null )
				listener.timeElapsed( ( TimerEvent )evt );
		}
		else
		{
			super.processEvent( evt );
		}
	}

	private int interval;
	private TimerListener listener;
	private static EventQueue evtq;
}
