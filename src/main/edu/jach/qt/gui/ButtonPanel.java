package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.LinkedList;
import javax.swing.border.*;
import java.util.Hashtable;
import edu.jach.qt.gui.WidgetDataBag;

/**
 * ButtonPanel.java
 *
 * This is composite object designed for choosing instruments.
 * JCheckboxes are used for selection and are grouped together with a
 * titled border.  
 * Created: Tue Mar  6 11:52:13 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class ButtonPanel extends WidgetPanel  {

   LinkedList buttonList;
   ListIterator iterator;
   private String next, myTitle;
   
   /**
    * Creates a new <code>ButtonPanel</code> instance.
    *
    * @param parent a <code>WidgetPanel</code> value
    * @param title a <code>String</code> value
    * @param options a <code>LinkedList</code> value
    */
   public ButtonPanel(Hashtable ht, WidgetDataBag wdb, CompInfo info) {
      super(ht, wdb);
      myTitle = info.getTitle();
      iterator = info.getList().listIterator(0);
      //setBackground(java.awt.Color.gray);

      setBorder(BorderFactory.createTitledBorder
		(BorderFactory.createEtchedBorder(), myTitle,
		 TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
      buttonList = new LinkedList();
      
      setLayout(new GridLayout(2, info.getSize()/2));
      for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
	    next = (String)iterator.next();
	    JCheckBox cb = new JCheckBox(next);
	    cb.addActionListener(this);
	    //cb.setBackground(java.awt.Color.gray);

	    add(cb);
	    buttonList.add(cb);
      }
   }
   
   /**
    * The <code>setEnabled</code> method enables or diables each
    * JCheckBox depending on the value of <code>boolean</code>.
    *
    * @param booleanFlag a <code>boolean</code> value
    */
   public void setEnabled(boolean booleanFlag) {
      JCheckBox next;      
      for (ListIterator iter = buttonList.listIterator(); iter.hasNext(); iter.nextIndex()) {
	 next = (JCheckBox)iter.next();
	 next.setEnabled(booleanFlag);
      }
   }

   /**
    * The <code>setSelected</code> method selects or unselects each
    * JCheckBox method depending on the value of <code>boolean</code>.
    *
    * @param flag a <code>boolean</code> value
    */
   public void setSelected(boolean flag) {
      JCheckBox next;      
      for (ListIterator iter = buttonList.listIterator(); iter.hasNext(); iter.nextIndex()) {
	 next = (JCheckBox)iter.next();
	 next.setSelected(flag);
	 setAttribute(myTitle, buttonList);
      }
   }

   /**
    * The <code>actionPerformed</code> method notifies the
    * WidgetDataBag of the state of all JCheckBoxes.
    *
    * @param evt an <code>ActionEvent</code> value
    */
   public void actionPerformed (ActionEvent evt) {
      setAttribute(myTitle, buttonList);
   }
}// ButtonPanel
