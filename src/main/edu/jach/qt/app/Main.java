package edu.jach.qt.app;

import edu.jach.qt.gui.QtFrame;
import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.QtTools;
import java.awt.*;
import javax.swing.UIManager;

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
final public class Main {

   /**
    * Creates a new <code>Main</code> instance which starts a 
    * Querytool, the app itself, and a QtFrame, the user interface.  The
    * frame is also set be centered on the screen.
    */
   public Main () {
      QtTools.loadConfig("config/qt.conf");

      WidgetDataBag wdb = new WidgetDataBag ();
      Querytool qt = new Querytool(wdb);
      QtFrame qtf = new QtFrame(wdb, qt);
      
      qtf.setSize(new Dimension(950, 550));
      qtf.setTitle("OMP Query Tool Observation Manager");

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = qtf.getSize();

      // Validate frames that have preset sizes
      qtf.validate();
      
      // Pack frmaes otherwise
      //qtf.pack();

      //Fill screen if the screen is smaller that qtfSize.
      if (frameSize.height > screenSize.height) {
	 frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
	 frameSize.width = screenSize.width;
      }

      //Center the screen
      int x = screenSize.width/2 - frameSize.width/2;
      int y = screenSize.height/2 - frameSize.height/2;
      qtf.setLocation(x,y);
      qtf.setVisible(true);

   }
   
   /**
    * Currently we take no args at startup.  Just get the 
    * LookAndFeel from the UIManager and start the Main QT class.
    *
    * @param args a <code>String[]</code> value
    */
   public static void main(String[] args) {
      try {
	 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e) {
	 e.printStackTrace();
      }
      new Main();
   }
} // Omp

//$Log$
//Revision 1.5  2001/09/29 05:34:50  mrippa
//Frame centers on screen.
//
//Revision 1.4  2001/09/20 01:58:07  mrippa
//Added QtTools.loadConfig("config.qt.conf"); which loads
//the main config file for the QT.
//
//Revision 1.3  2001/09/18 21:53:39  mrippa
//All classes and methods documented.
//
//Revision 1.2  2001/09/07 01:18:10  mrippa
//The QT now supports a query of the MSB server retrieving a MSB summaries.
//The summaries are displayed in a JTable which listens for double clicks
//on the rows, corresponding to the MSB ID for that summary.  The MSB is
//then translated and given to the OM for lower level processing.
//
//Revision 1.1.1.1  2001/08/28 02:53:45  mrippa
//Import of QT
//
//Revision 1.3  2001/07/27 19:49:05  mrippa
//More comments
//
