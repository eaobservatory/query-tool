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
 * This offers a JTextField with a JLabel packaged up in a JPanel.
 *
 * Created: Thu Mar 22 11:04:49 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class LabeledTextField extends WidgetPanel
   implements DocumentListener {

   protected JTextField textField;
   protected JLabel label;

   private String name;
   
   /**
    * Creates a new <code>LabeledTextField</code> instance.
    *
    * @param parent a <code>WidgetPanel</code> value
    * @param text a <code>String</code> value
    */
   public LabeledTextField (Hashtable ht, WidgetDataBag wdb, String text) {
      super(ht, wdb);
      textField = new JTextField("");
      label = new JLabel(text + ": ", JLabel.LEADING);

      setup();
   }
	
   private void setup() {
      name = label.getText().trim();

      this.setLayout(new GridLayout(1,2));
      add(label);

      add(textField);
      textField.getDocument().addDocumentListener(this);
   }
 
   /**
    * The <code>insertUpdate</code> adds the current text to the
    * WidgetDataBag.  All observers are notified.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void insertUpdate(DocumentEvent e) {
      //setAttribute(getLabel(), valueList);
      setAttribute(name.substring(0,name.length()-1), textField.getText());
   }

   /**
    * The <code>removeUpdate</code> adds the current text to the
    * WidgetDataBag.  All observers are notified.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void removeUpdate(DocumentEvent e) {
      setAttribute(name.substring(0,name.length()-1), textField.getText());
   }

   /**
    * The <code>changedUpdate</code> method is not implemented.
    *
    * @param e a <code>DocumentEvent</code> value
    */
   public void changedUpdate(DocumentEvent e) {
      
   }

}// LabeledTextField
