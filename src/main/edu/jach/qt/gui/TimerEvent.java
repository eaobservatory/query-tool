package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

/**
 * Define a new event, <code>TimerEvent</code>.
 */
public class TimerEvent extends AWTEvent {

    /**
     * Constructor.
     * @param  t    A timer object to drive the event.
     */
   public TimerEvent(Timer t) {
      super(t, TIMER_EVENT); 
   }

   public static final int TIMER_EVENT 
      = AWTEvent.RESERVED_ID_MAX  + 5555;
}
