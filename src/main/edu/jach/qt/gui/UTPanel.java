package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.DateFormat;

/**
 * Display the UTC time in a panel as a <code>JLabel</code>..
 *
 * @author $Author$
 * @version $Id$
 */

public class UTPanel extends JLabel
   implements TimerListener {
   
    /**
     * Constructor.
     * Starts a timer so that the display is updated once a second.
     */
   public UTPanel () {
      setHorizontalAlignment(SwingConstants.CENTER);

      //MatteBorder matte = new MatteBorder(1,1,1,1,Color.white);

      TitledBorder border = BorderFactory.createTitledBorder
	(BorderFactory.createLineBorder(new Color(51, 134, 206)), "UTC",
	 TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
      setBorder(border);
      
      this.setOpaque(true);
      Timer t = new Timer(1000);
      t.addTimerListener(this);
   }
   
    /**
     * Implementation of TimeListener.
     * Sets the date and time on the associated object once a second.
     * @param evt  A timer event.
     */
   public void timeElapsed(TimerEvent evt) {
      setBackground(Color.black);
      setForeground(Color.green);
      //Graphics g = getGraphics();
      //g.setFont( g.getFont().deriveFont((float)16.0));
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      Date date = cal.getTime();
      DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
      df.setTimeZone(TimeZone.getTimeZone("UTC"));
      String time = df.format(date);
      setText(time);
   }
   
}// TimePanel
