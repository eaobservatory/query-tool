package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;


/**
 * LabeledMinMaxTextField.java
 * 
 * This composite widget contains a JLabel, a JComboBox, and a
 * JTextField.  The JLabel and JTextField are inherited from
 * LabeledTextField.  The JComboBox is extending LabeledTextField.
 * Created: Thu Sep 20 14:49:45 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

public class LabeledMinMaxTextField extends LabeledTextField {
   
   protected LinkedList valueList;
   protected JComboBox rangeList;

   public LabeledMinMaxTextField(Hashtable ht, WidgetDataBag wdb, String text) {
      super(ht, wdb, text);

      valueList = new LinkedList();
      rangeList = new JComboBox();

      setup();
   }

   private void setup() {
      this.setLayout(new GridLayout (1,3));
      add(label);

      rangeList.addItem("Max");
      rangeList.addItem("Min");
      rangeList.setSelectedIndex(0);
      rangeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	       JComboBox cb = (JComboBox)e.getSource();
	       String selection = (String)cb.getSelectedItem();
	       //System.out.println("Selection: "+ selection);

	       setAttribute(getLabel(), valueList);
	       //cb.setSelectedItem(selection);
	    }
	 });
      add(rangeList);
      valueList.add(0,rangeList);
      add(textField);
      textField.getDocument().addDocumentListener(this);
   }

   /**
    * The <code>insertUpdate</code> method adds the current text in the
    * text field to slot 1 of the valueList.  The WidgetDataBag is updated.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void insertUpdate(DocumentEvent e) {
      setAttribute(getLabel(), valueList);
      
      //setAttribute(getLabel(), textField.getText());
   }

   /**
    * The <code>removeUpdate</code> method adds the current text in the
    * text field to slot 1 of the valueList.  The WidgetDataBag is updated.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void removeUpdate(DocumentEvent e) {
      valueList.add(1,textField.getText());
      setAttribute(getLabel(), valueList);
   }

   /**
    * The <code>changedUpdate</code> method is not implemented.  We
    * don't require an action here.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void changedUpdate(DocumentEvent e) {
      
   }

   /**
    * The <code>getLabel</code> method adds the current text in the
    * text field to slot 1 of the valueList.  It returns the label of
    * this object.
    *
    * @return a <code>String</code> value
    */
   public String getLabel() {
      valueList.add(1,textField.getText());
      String name = label.getText().trim();
      return name.substring(0,name.length()-1);
   }

   
}

