package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Hashtable;
import edu.jach.qt.gui.WidgetDataBag;

/**
 * RadioPanel.java
 *
 *
 * Created: Tue Aug  7 09:35:40 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class RadioPanel extends WidgetPanel 
   implements ActionListener{

   private JRadioButton rb;
   private String next, myTitle;
   private LinkedList myElems;
   private ListIterator iterator;

   public ButtonGroup group = new ButtonGroup();
   public LinkedList radioElems = new LinkedList();

   public RadioPanel (Hashtable ht, WidgetDataBag wdb, String title, LinkedList elems){
      super(ht, wdb);
      myTitle = title;
      myElems = elems;
      config();
   }

   private void config() {
      iterator = myElems.listIterator(0);

      setBorder(BorderFactory.createTitledBorder
		(BorderFactory.createEtchedBorder(), myTitle,
		 TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
	 next = (String)iterator.next();
	 rb = new JRadioButton(next);
	 rb.addActionListener(this);
	 add(rb);
	 rb.setAlignmentX(rb.CENTER_ALIGNMENT);
	 group.add(rb);
	 radioElems.add(rb);
	 if ((iterator.nextIndex() - 1) == 0)
	    rb.doClick();
      }
   }

   public void actionPerformed (ActionEvent evt) {
      printTable();
      setAttribute(myTitle, radioElems);
   }
}// RadioPanel
