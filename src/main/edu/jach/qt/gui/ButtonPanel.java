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
 * @version $Id$
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
      
      setLayout(new GridLayout(3, info.getSize()/2));
      JCheckBox wfcamTickBox = null;
      for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
	    next = (String)iterator.next();
	    JCheckBox cb = new JCheckBox(next);
	    cb.addActionListener(this);
            if ( "WFCAM".equals(next) ) wfcamTickBox = cb;
	    //cb.setBackground(java.awt.Color.gray);

	    add(cb);
            buttonList.add(cb);
      }
//       if ( wfcamTickBox != null ) {
//           wfcamTickBox.doClick();
//           wfcamTickBox.setSelected(true);
//       }
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
	 if (!next.getText().equals("Any Instrument") &&
	     !next.getText().equals("Any Heterodyne") &&
             !next.getText().equals("current")) {
	   next.setEnabled(booleanFlag);
	   
	 }
	 
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
	 if (!next.getText().equals("Any Instrument") &&
	     !next.getText().equals("Any Heterodyne") &&
             !next.getText().equals("current")) {
	   next.setSelected(flag);
	   setAttribute(myTitle, buttonList);
	 }
      }
   }

   private void wfcamSelected(boolean selected) {
       JCheckBox next;
       for ( ListIterator iter = buttonList.listIterator(); iter.hasNext(); iter.nextIndex() ) {
           next = (JCheckBox)iter.next();
           // If flag is true, we need to deselect everything
           // else and disable the
           if ( selected ) {
               if ( next.getText().equals("WFCAM") ) {
                   next.setSelected(selected);
                   next.setEnabled(selected);
               }
               else {
                   next.setSelected(!selected);
                   next.setEnabled(!selected);
               }
           }
           else {
               // WFCAM is deselected.  Just enable all other buttons
               next.setEnabled(!selected);
           }
       }
       setAttribute(myTitle, buttonList);
   }

   /**
    * The <code>actionPerformed</code> method notifies the
    * WidgetDataBag of the state of all JCheckBoxes.
    *
    * @param evt an <code>ActionEvent</code> value
    */
   public void actionPerformed (ActionEvent evt) {

     Object source = evt.getSource();
     JCheckBox temp = (JCheckBox)source;
      
     if ( temp.getText().equals("WFCAM") ) {
         if ( temp.isSelected() ) {
             wfcamSelected(true);
         }
         else {
             wfcamSelected(false);
         }
         return;
     }
     if (temp.getText().equals("Any Instrument") ||
	 temp.getText().equals("Any Heterodyne") ||
         temp.getText().equals("current")) {
       if (temp.isSelected()) {
	 setSelected(false);
	 setEnabled(false);
       }
       else {
	 setEnabled(true);
	 setSelected(false);
       }
     }

     else {
       setAttribute(myTitle, buttonList);
     }
   }
}// ButtonPanel
