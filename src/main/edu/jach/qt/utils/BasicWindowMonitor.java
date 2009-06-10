package edu.jach.qt.utils ;

import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import java.awt.Window ;
import edu.jach.qt.utils.JACLogger ;

/**
 * Class implementing a window listener.
 */
public class BasicWindowMonitor extends WindowAdapter
{
	static final JACLogger logger = JACLogger.getLogger( BasicWindowMonitor.class ) ;

	/**
	 * Implementation of the WindowClosing method.
	 *
	 * @see     java.awt.Window
	 *
	 * @param   e    A window event
	 */
	public void windowClosing( WindowEvent e )
	{
		logger.info( "Shutting down due to WindowClosing event" ) ;
		if( e != null )
		{
			Window w = e.getWindow() ;
			if( w != null )
			{
				w.setVisible( false ) ;
				w.dispose() ;
			}
		}
		System.exit( 0 ) ;
	}
	
} // BasicWindowMonito
