package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

/**
 * LabeldRangeTextField.java
 *
 *
 * Created: Tue Sep 25 15:55:43 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

public class LabeledRangeTextField extends LabeledTextField {

   protected JTextField upperBound;

   public LabeledRangeTextField(Hashtable ht, WidgetDataBag wdb, String text) {
      super(ht, wdb, text);

      upperBound = new JTextField();

      setup();
   }

   private void setup() {
      //setBackground(java.awt.Color.gray);
      setForeground(Color.white);
      this.setLayout(new GridLayout (1,5));
      add(label);
      add(new JLabel("Min: ",JLabel.TRAILING));
      add(textField);
      textField.getDocument().addDocumentListener(this);
      add(new JLabel("Max: ",JLabel.TRAILING));
      add(upperBound);
      upperBound.getDocument().addDocumentListener(this);
   }
}
