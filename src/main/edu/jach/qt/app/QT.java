package edu.jach.qt.app;

/* JSKY imports */
import jsky.app.ot.OtFileIO;

/* ORAC imports */
import orac.ukirt.util.UkirtPreTranslator;
import orac.util.SpItemDOM;

/* QT imports */
import edu.jach.qt.gui.QtFrame;
import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.BasicWindowMonitor;
import edu.jach.qt.utils.QtTools;

/* Standard imports */
import java.awt.*;
import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

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
final public class QT {

  static Logger logger = Logger.getLogger(QT.class);

   /**
    * Creates a new <code>QT</code> instance which starts a 
    * Querytool, the app itself, and a QtFrame, the user interface.  The
    * frame is also set be centered on the screen.
    */
   public QT () {
       if (System.getProperty("QT_LOG_DIR").equals(""))
       {
	   PropertyConfigurator.configure("/home/dewitt/omp/QT/config/nolog4j.properties");
       }
       else
       {
	 PropertyConfigurator.configure("/jac_sw/omp/QT/config/log4j.properties");
       }

     logger.info("-------WELCOME TO THE QT----------");
     OtFileIO.setXML(System.getProperty("OMP") != null);

     try {
       SpItemDOM.setPreTranslator(new UkirtPreTranslator("Base", "GUIDE"));
        
     } catch ( Exception e) {
       logger.fatal("PreTranslator error starting the QT");
       System.exit(1);
     }

      QtTools.loadConfig(System.getProperty("qtConfig"));

      WidgetDataBag wdb = new WidgetDataBag ();
      Querytool qt = new Querytool(wdb);
      QtFrame qtf = new QtFrame(wdb, qt);
      
      qtf.addWindowListener(new BasicWindowMonitor());
      qtf.setSize(1150,550);
      //qtf.setSize(new Dimension(1150, 600));
      qtf.setTitle("OMP Query Tool Observation Manager");

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = qtf.getSize();

      /* Fill screen if the screen is smaller that qtfSize. */
      if (frameSize.height > screenSize.height) {
	 frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
	 frameSize.width = screenSize.width;
      }

      /* Center the screen */
      int x = screenSize.width/2 - frameSize.width/2;
      int y = screenSize.height/2 - frameSize.height/2;
      qtf.setLocation(x,y);
      qtf.validate();
      qtf.setVisible(true);
   }
   
   /**
    * Currently we take no args at startup.  Just get the 
    * LookAndFeel from the UIManager and start the Main QT class.
    *
    * @param args a <code>String[]</code> value
    */
   public static void main(String[] args) {
      new QT();
   }
} // Omp

/*
 * $Log$
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
 * Added QtTools.loadConfig("config.qt.conf"); which loads
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
