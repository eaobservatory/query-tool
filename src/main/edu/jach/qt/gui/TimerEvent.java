package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class TimerEvent extends AWTEvent {

   public TimerEvent(Timer t) {
      super(t, TIMER_EVENT); 
   }

   public static final int TIMER_EVENT 
      = AWTEvent.RESERVED_ID_MAX  + 5555;
}
