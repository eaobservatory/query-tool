package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.DateFormat;
/**
 * TimePanel.java
 *
 *
 * Created: Fri Apr 20 14:55:26 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class TimePanel extends JLabel
   implements TimerListener {
   
   public TimePanel () {
      setHorizontalAlignment(SwingConstants.CENTER);

      //TitledBorder titled = new TitledBorder("HST");
      //titled.setTitlePosition(TitledBorder.ABOVE_TOP);
      //titled.setTitleJustification(TitledBorder.CENTER);

      MatteBorder matte = new MatteBorder(1,1,1,1,Color.white);
      setBorder(matte);
      
      this.setOpaque(true);
      Timer t = new Timer(1000);
      t.addTimerListener(this);
   }
   
   public void timeElapsed(TimerEvent evt) {
      setBackground(Color.black);
      setForeground(Color.green);
      //Graphics g = getGraphics();
      //g.setFont( g.getFont().deriveFont((float)16.0));
      Calendar cal = Calendar.getInstance();
      Date date = cal.getTime();
      DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM);
      //      g.clearRect(0,0,200,200);
      //      g.drawString(dateFormatter.format(date), 10, 20);
      setText("" + dateFormatter.format(date));
   }
   
}// TimePanel
