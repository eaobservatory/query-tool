package edu.jach.qt.gui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class Timer extends Component implements Runnable {
   public Timer(int i) {
      interval = i;
      Thread t = new Thread(this);
      t.start();
      evtq = Toolkit.getDefaultToolkit()
	 .getSystemEventQueue();
      enableEvents(0);
   }
   
   public void addTimerListener(TimerListener l) {
      listener = l;
   }
   
   public void run() {
      while (true)
	 {  try { Thread.sleep(interval); } 
	 catch(InterruptedException e) {}
	 TimerEvent te = new TimerEvent(this); 
	 evtq.postEvent(te);   
	 }
   }

   public void processEvent(AWTEvent evt) {
      if (evt instanceof TimerEvent)
	 {  if (listener != null)
	    listener.timeElapsed((TimerEvent)evt);
	 }
      else super.processEvent(evt);
   }

   private int interval;
   private TimerListener listener;
   private static EventQueue evtq;
}
