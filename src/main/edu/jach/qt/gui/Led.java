package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import jsky.util.gui.BasicWindowMonitor;

/**
 * Led.java
 *
 *
 * Created: Wed Nov 21 13:28:52 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @deprecated  Not repalced.
 */

public class Led extends JPanel {

  /** Handle for the "LED" button */
  protected JButton led;

  /** Action linked to the stop button and led */
  protected AbstractAction stopAction = new AbstractAction("Stop") {
      public void actionPerformed(ActionEvent evt) {
	this.setEnabled(false);
      }
    };
  
  /** Timer used for blinking the led */
  protected Timer timer;

  /** The current state of the led (for blinking) */
  protected boolean ledState;

  public Led () {
    setBackground(Color.black);
    add(createLed());
    led.setEnabled(false);
  }


  /**
     * Create the red LED button
     */
  protected JButton createLed() {
    //ClassLoader cl = ClassLoader.getSystemClassLoader();
    URL url = ClassLoader.getSystemResource("green_led.gif");
    led = new JButton(new ImageIcon(url));
    led.setBackground(Color.black);
    led.setFocusPainted(false);
    led.setBorderPainted(false);

    url = ClassLoader.getSystemResource("green_led_disabled.gif");
    led.setDisabledIcon(new ImageIcon(url));
    led.addActionListener(stopAction);

    return led;
  }

  protected void blinkLed(boolean enabled) {
    led.setEnabled(ledState = enabled);
    if (enabled) {
      if (timer == null) {
	timer = new Timer(200, new ActionListener() {
	    public void actionPerformed(ActionEvent ev) {
	      ledState = !ledState;
	      led.setEnabled(ledState);
	    }
	  });
	timer.start();
      }
      else {
	timer.restart();
      }
    } 
    else {
      if (timer != null) {
	timer.stop();
      }
    }
  }
  
}// Led
