package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Hashtable;
import edu.jach.qt.gui.WidgetDataBag;
import java.util.LinkedList;


/**
 * LabeledTextField.java
 *
 *
 * Created: Thu Mar 22 11:04:49 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class LabeledTextField extends WidgetPanel
   implements DocumentListener {

   private JTextField textField;
   private String attribute;
   
   public LinkedList valueList;
   /**
    * Creates a new <code>LabeledTextField</code> instance.
    *
    * @param parent a <code>WidgetPanel</code> value
    * @param text a <code>String</code> value
    */
   public LabeledTextField (Hashtable ht, WidgetDataBag wdb, String text) {
      super(ht, wdb);
      attribute = text;
      valueList = new LinkedList();
      setup();
   }
	
   private void setup() {
      this.setLayout(new GridLayout(1,3));
      add(new JLabel(attribute + ": ", JLabel.LEADING));

      String[] list = {"max","min"};

      JComboBox rangeList = new JComboBox(list);
      rangeList.setSelectedIndex(0);
      rangeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	       JComboBox cb = (JComboBox)e.getSource();
	       String selection = (String)cb.getSelectedItem();
	    }
	 });
      add(rangeList);
      valueList.add(0,rangeList);

      add(textField = new JTextField(""));
      textField.getDocument().addDocumentListener(this);
      
   }
 

   /**
    * Describe <code>getLabel</code> method here.
    *
    * @return a <code>String</code> value
    */
   public String getLabel() {
      return attribute;
   }

   /**
    * Describe <code>insertUpdate</code> method here.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void insertUpdate(DocumentEvent e) {
      valueList.add(1,textField.getText());
      setAttribute(getLabel(), valueList);
   }

   /**
    * Describe <code>removeUpdate</code> method here.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void removeUpdate(DocumentEvent e) {
      valueList.add(1,textField.getText());
      setAttribute(getLabel(), valueList);
   }

   /**
    * Describe <code>changedUpdate</code> method here.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void changedUpdate(DocumentEvent e) {
      
   }

}// LabeledTextField
