package edu.jach.qt.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.TitledBorder;




/**
 * Associates an image with a label.
 *In this case, the image is a satelliet image derived from a configurable URL.
 *
 * Created: Mon Apr  8 10:18:45 2002
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @version $Id$
 */

public class SatPanel extends JLabel implements TimerListener {

  private TitledBorder satBorder;
  private static final String SAT_WEBPAGE = System.getProperty("satellitePage");
  private static final String IMG_PREFIX = System.getProperty("imagePrefix");
    /**
     * Constructor.
     * Sets the look and feel of the <code>JLabel</code> and associates a timer
     * with it to update the display.
     */
  public SatPanel (){
    setHorizontalAlignment(SwingConstants.CENTER);

    satBorder = BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder(new Color(51, 134, 206)), "Loading Sat...",
       TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);

    setBorder(satBorder);
    refreshIcon();

    Timer timer = new Timer(600000); //refresh every 10 minutes
    timer.addTimerListener(this);

  }

  // implementation of edu.jach.qt.gui.TimerListener interface

  /**
   * Implementation of edu.jach.qt.gui.TimerListener interface
   * @param param1 A <code>TimerEvent</code>
   */
  public void timeElapsed(TimerEvent param1) {
	      refreshIcon();
  }

    /**
     * Redraws the associated image.
     */
  public void refreshIcon() {
      SwingWorker worker = new SwingWorker() {
	      public Object construct () {
		  try {
		      String imageSuffix = URLReader.getImageString(new URL(SAT_WEBPAGE));
		      String timeString = imageSuffix.substring(8, imageSuffix.indexOf('.'));
		      
		      //System.out.println("IMG=>>>"+InfoPanel.IMG_PREFIX + imageSuffix+"<<<");
		      setIcon(new ImageIcon(new URL(InfoPanel.IMG_PREFIX + imageSuffix)));
		      
		      satBorder.setTitle(timeString+" UTC");
		  } catch ( Exception e) { } 
		  return null;
	      }
	  };
      worker.start();
  }
}// SatPanel

/**
 * Reads a URL.
 */
class URLReader {

    /**
     * Get the String associated with a URL.
     * @param url  The URL associated with the satellite image.
     * @return The name of the Image.
     * @exception Exception if unable to open the URL.
     */
    public static String getImageString(URL url) throws Exception {

    String imgString = "";
    String inputLine, html="";
    BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream()));
    while ((inputLine = in.readLine()) != null) {
      html = html+inputLine;
    }
    in.close();

    StringTokenizer st = new StringTokenizer(html);

    while (st.hasMoreTokens()) {
      String temp = st.nextToken();
      if (temp.startsWith("SRC=ni4")) {
	imgString = temp.substring(4, temp.indexOf('>'));
      }
    }
    
    return imgString;
  }
}
