package edu.jach.qt.app ;

/* JSKY imports */

/* ORAC imports */
/* QT imports */
/* Standard imports */
import edu.jach.qt.gui.QtFrame ;
import edu.jach.qt.gui.WidgetDataBag ;
import edu.jach.qt.utils.QtTools ;

/* Standard imports */

import java.awt.Dimension ;
import java.awt.Toolkit ;
import java.awt.AWTError ;
import jsky.app.ot.OtCfg ;
import edu.jach.qt.utils.JACLogger ;
/**
 * This is the top most OMP-QT class.  Upon init it instantiates 
 * the Querytool and QtFrame classes, in that order.  These two classes
 * define the structure of the OMP-QT design.  There has been defined
 * a partition between the Graphical User Interface (GUI) and the logic
 * behind the application.  Hence, the directory structure shows a 'qt/gui'
 * and a 'qt/app'.  There also is an 'qt/utils' directory which is a 
 * repository of utility classes needed for both 'app' and 'gui' specific 
 * classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 *
 * $Id$
 */
final public class QT
{
	static JACLogger logger = JACLogger.getLogger( QT.class ) ;

	/**
	 * Creates a new <code>QT</code> instance which starts a Querytool, the app itself, and a QtFrame, the user interface. The frame is also set be centered on the screen.
	 */
	public QT()
	{
		logger.info( "-------WELCOME TO THE QT----------" ) ;

		try
		{
			OtCfg.init() ;
		}
		catch( Exception e )
		{
			logger.fatal( "PreTranslator error starting the QT" , e ) ;
			System.exit( 1 ) ;
		}
		catch( ClassCircularityError cce )
		{
			logger.fatal( "Talk to SHAUN!!!: PreTranslator ClassCircularityError starting the QT" , cce ) ;
			System.exit( 1 ) ;
		}

		QtTools.loadConfig( System.getProperty( "qtConfig" ) ) ;

		WidgetDataBag wdb = new WidgetDataBag() ;
		Querytool qt = new Querytool( wdb ) ;
		QtFrame qtf = new QtFrame( wdb , qt ) ;

		qtf.setSize( 1150 , 620 ) ;
		qtf.setTitle( "OMP Query Tool Observation Manager" ) ;

		Dimension screenSize ;
		try
		{
			Toolkit tk = Toolkit.getDefaultToolkit() ;
			screenSize = tk.getScreenSize() ;
		}
		catch( AWTError awe )
		{
			screenSize = new Dimension( 640 , 480 ) ;
		}
		Dimension frameSize = qtf.getSize() ;

		/* Fill screen if the screen is smaller that qtfSize. */
		if( frameSize.height > screenSize.height )
			frameSize.height = screenSize.height ;
		if( frameSize.width > screenSize.width )
			frameSize.width = screenSize.width ;

		/* Center the screen */
		int x = screenSize.width / 2 - frameSize.width / 2 ;
		int y = screenSize.height / 2 - frameSize.height / 2 ;
		qtf.setLocation( x , y ) ;
		qtf.validate() ;
		qtf.setVisible( true ) ;

		logger.info( "QT should now be visible" ) ;

		String bigTelescope = System.getProperty( "TELESCOPE" ) ;
		if( bigTelescope == null || bigTelescope.equals( "" ) )
			System.setProperty( "TELESCOPE" , System.getProperty( "telescope" ) ) ;
	}

	/**
	 * Currently we take no args at startup. Just get the LookAndFeel from the UIManager and start the Main QT class.
	 * 
	 * @param args
	 *            a <code>String[]</code> value
	 */
	public static void main( String[] args )
	{
		try
		{
			new QT() ;
		}
		catch( RuntimeException rte )
		{
			logger.fatal( "Caught a run-time exception from main" , rte ) ;
		}
		catch( Exception e )
		{
			logger.fatal( "Caught an unexpected exception in main" , e ) ;
		}
	}
} // Omp

/*
 * $Log$
 * Revision 1.32  2007/02/15 00:13:50  shart
 * For debugging puposes -> SH
 *
 * Revision 1.31  2006/10/24 02:00:29  shart
 * Hack to help the fact that the same parameter in OT and QT use a different case, will fix, just not today  -> SH
 *
 * Revision 1.30  2006/07/10 21:28:59  shart
 * Some changes recommmended by PMD -> SH
 *
 * Revision 1.29  2006/01/23 20:37:04  shart
 * Remove all references to OtFileIO.setXML as it has been removed in the OT -> SH
 *
 * Revision 1.28  2005/02/24 20:30:23  dewitt
 * Merge fro JAC_WFCAM branch and fix for fault [20050224.002]
 *
 * Revision 1.27.2.1  2004/10/13 18:41:53  dewitt
 * WFCAM changes
 *
 * Revision 1.27  2003/03/28 18:59:49  dewitt
 * Added checking MSB status on exit.  Also added a handler for SIGTERMs for future use with ocs_down script.
 *
 * Revision 1.26  2003/03/19 19:31:30  dewitt
 * New UKIRT instrument handling.
 *
 * Revision 1.25  2003/02/20 22:44:44  dewitt
 * Removed shutdownhook which is implicated in the QT failing to exit cleanly.
 *
 * Revision 1.24  2003/02/20 20:30:38  dewitt
 * Added logging to try to track down shutdown problems.
 *
 * Revision 1.23  2003/01/30 21:05:17  dewitt
 * Merged wide_mode_qt branch
 *
 * Revision 1.22  2003/01/30 01:14:38  dewitt
 * Undone last merge - some left over problem.  Will get back to it
 *
 * Revision 1.20  2002/12/02 20:50:33  dewitt
 * Added extra error handling within the main to try to trap most exceptions and dump the info to the log file.  Any uncaught error should propogate up to here and will be caught and handled.
 *
 * Revision 1.19  2002/11/04 20:06:32  mrippa
 * Catches ClassCircularityError
 *
 * Revision 1.18  2002/10/22 00:47:03  dewitt
 * Added a shutdown hook so that the QT lock file is cleaned up even if the system exits.  May still not work in the event of a kernel panic, but we will wait and see.
 *
 * Revision 1.17  2002/07/31 23:52:32  dewitt
 * Changed the starting of the PreTranslators to use OtCgf.init().  This is more generic (and lets us read JCMT project files!).
 *
 * Revision 1.16  2002/07/26 01:08:36  dewitt
 * Added some additional error handling
 *
 * Revision 1.15  2002/06/13 00:47:14  dewitt
 * Modified logging so that if a user can not write to the default log dir, it
 * only does console logging.
 *
 * Revision 1.14  2002/05/30 22:26:26  mrippa
 * colapsed imports > 4
 * Implemented logging with log4j
 *
 * Revision 1.13  2002/04/17 03:35:24  mrippa
 * Removed needless comments
 *
 * Revision 1.12  2002/04/17 03:29:32  mrippa
 * Replaced Main.java with QT.java
 *
 * Revision 1.11  2002/04/17 03:15:47  mrippa
 * The new Main.java is now called QT.java
 *
 * Revision 1.10  2002/03/08 10:08:14  mrippa
 * The PRE-translator thingy was added here. Everything was completely
 * broken without it!
 *
 * Revision 1.9  2002/02/24 06:50:56  mrippa
 * Reset Window size of the QtFrame.
 *
 * Revision 1.8  2001/11/24 04:56:20  mrippa
 * Added a BasicWindowMonitor so as to cleanly exit the QT.
 *
 * Revision 1.7  2001/11/05 18:58:16  mrippa
 * New system wide config file is read in.
 *
 * Revision 1.6  2001/10/20 04:12:11  mrippa
 * The main config file is now in $install/omp/QT/config/qt.conf
 *
 * Revision 1.5  2001/09/29 05:34:50  mrippa
 * Frame centers on screen.
 *
 * Revision 1.4  2001/09/20 01:58:07  mrippa
 * Added QtTools.loadConfig("config.qt.conf") ; which loads
 * the main config file for the QT.
 *
 * Revision 1.3  2001/09/18 21:53:39  mrippa
 * All classes and methods documented.
 *
 * Revision 1.2  2001/09/07 01:18:10  mrippa
 * The QT now supports a query of the MSB server retrieving a MSB summaries.
 * The summaries are displayed in a JTable which listens for double clicks
 * on the rows, corresponding to the MSB ID for that summary.  The MSB is
 * then translated and given to the OM for lower level processing.
 *
 * Revision 1.1.1.1  2001/08/28 02:53:45  mrippa
 * Import of QT
 */
