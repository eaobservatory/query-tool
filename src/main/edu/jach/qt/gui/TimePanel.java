package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.DateFormat;
/**
 * Class associateing local time with a label for display on an interface.
 *
 *
 * Created: Fri Apr 20 14:55:26 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class TimePanel extends JLabel
   implements TimerListener {
   
    /**
     * Constructor.
     * Sets a timer running and adds a listener.
     */
   public TimePanel () {
      setHorizontalAlignment(SwingConstants.CENTER);

      //MatteBorder matte = new MatteBorder(1,1,1,1,Color.white);

      TitledBorder border = BorderFactory.createTitledBorder
	(BorderFactory.createLineBorder(new Color(51, 134, 206)), 
	"Local Time",
	 TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
      setBorder(border);
      
      this.setOpaque(true);
      Timer t = new Timer(1000);
      t.addTimerListener(this);
   }
   
    /**
     * Implenetation of the <code>timeElapsed</code> interface.
     * Updates the associated label.
     * @param evt   the <code>TimerEvent</code> to consume.
     */
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
