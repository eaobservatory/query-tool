package edu.jach.qt.gui;

import junit.extensions.jfcunit.*;
import java.util.*;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import edu.jach.qt.app.Querytool;

/**
 * GuiTest.java
 *
 *
 * Created: Tue Aug 21 11:21:25 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class GuiTest extends JFCTestCase{
   private JFCTestHelper helper;

   public GuiTest (String test){
      super(test);
   }

   public void setUp() {
      //Start listening for window open events
      helper = new JFCTestHelper();
   }

   public void testMain() {
      Set windows;
      Window mainWindow;
      Component button;

      //Run the code we are testing
      edu.jach.qt.app.QT.main(new String[0]);

      //Let the AWT Thread show the window
      awtSleep();

      windows = helper.getWindows();

      try {
	 mainWindow = helper.getWindow("OMP Query Tool Observation Manager");
	 assertNotNull("Main window NULL!", mainWindow);

	 InfoPanel infoPanel = (InfoPanel)JFCTestHelper.findComponent(InfoPanel.class, mainWindow, 0);
	 //assertNotNull("Must not find 'does-not-exist'", infoPanel);
	 //assertEquals("/home/mrippa/netroot/src/QT/icons/omp_logo1.gif", infoPanel.getImageFile() );

	 //String search = "-Search".substring(1); //String with same text, different object.
	 //assertTrue("Must have different ref to string containing 'Search'", !(search == "Search"));
	 
	 button = helper.findNamedComponent("Search", infoPanel, 0);
	 assertNotNull("Must find 'Search' Button", button);

	 button = helper.findNamedComponent("Execute", infoPanel, 0);
	 assertNotNull("Must find 'Execute' Button", button);
	 helper.enterClickAndLeave(this, button);
	 assertTrue("Must not find null XML string.", !(infoPanel.getXMLquery() == null));

	 button = helper.findNamedComponent("Exit", infoPanel, 0);
	 assertNotNull("Must find 'Exit' Button", button);

	 JLabel label = JFCTestHelper.findJLabel("HST", infoPanel ,0);
	 assertNotNull("Must find 'HST' Label!", label);
      }
      catch(JFCTestException e) {
	 System.out.println("JFCUNIT: "+e.getMessage());
      }

      //We should have created exactly one window
      assertEquals(1, windows.size());
      
   }


}// GuiTest
