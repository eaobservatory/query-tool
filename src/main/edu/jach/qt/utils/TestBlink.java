package edu.jach.qt.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.lang.Thread;

public class TestBlink extends JPanel implements Runnable {

   private Dimension d;
   private int x,y;

   private boolean _set;
   private boolean _done = false;
   //private Thread externalThread;

   public TestBlink() {
      d = super.getSize();
      x = (int)d.getWidth() - 1;
      y = (int)d.getHeight() - 1;

      MatteBorder matte = new MatteBorder(5,5,5,5,Color.white);
      setBorder(matte);

      this.setOpaque(true);

      //setBackground(Color.green);
      //repaint();
   }

   public void setDone() {
      _done = true;
   }

   public void run() {
      while (!_done) {
	 try {
	    Thread.sleep(200);
	    System.out.println(Thread.currentThread().getName());
	    _set = !_set;
	    repaint();
	 }catch (InterruptedException ie) {ie.printStackTrace();}
      }
      System.out.println("I'm done");
      _done = false;
      _set=false;
      repaint();
   }

   public void paint(Graphics g) {
      //Paint a filled rectangle at user's chosen point.
      //g.drawRect(x/2-100/2, y/2-100/2, 100, 100);
      if(_set)
	 g.setColor(Color.green);
      else
	 g.setColor(Color.black);
      g.fillRect(0,0, 98, 98);
   }

   public static void main(String[] args) {
      JFrame f = new JFrame();
      f.setSize(new Dimension(400,400));
      TestBlink tb = new TestBlink();

      Thread thread = new Thread(tb);
      f.getContentPane().add(tb);
      f.addWindowListener(new BasicWindowMonitor());

      //f.pack();
      f.show();

      thread.start();

   }

}
