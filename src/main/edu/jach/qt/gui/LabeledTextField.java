package edu.jach.qt.gui;

import edu.jach.qt.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * LabeledTextField.java
 * This offers a JTextField with a JLabel packaged up in a JPanel.
 *
 * Created: Thu Mar 22 11:04:49 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version $Id$
 */

public class LabeledTextField extends WidgetPanel
  implements KeyListener, DocumentListener{

  protected JTextField textField;
  protected JLabel label;

  protected String name;
   
  /**
   * Creates a new <code>LabeledTextField</code> instance.
   *
   * @param parent a <code>WidgetPanel</code> value
   * @param text a <code>String</code> value for the name of the LabeledTextField
   */
  public LabeledTextField (Hashtable ht, WidgetDataBag wdb, String text) {
    super(ht, wdb);
    textField = new JTextField("");
    label = new JLabel(text + ": ", JLabel.LEADING);
    setup();
  }

  private  void setup() {
    name = label.getText().trim();
    textField.setHorizontalAlignment(JTextField.LEFT);
    label.setForeground(Color.black);
     
    this.setLayout(new GridLayout());
    add(label);
     
    add(textField);
    textField.getDocument().addDocumentListener(this);
    textField.addKeyListener(this);
  }

    /**
     * Get the abbreviated name of the current <code>LabeledTextField</code>.
     * The abbreviated name is the name on the interface up to, but not including
     * any white space characters.
     * @return The abbreviated name of the text field.
     */
  public String getName() {
    return abbreviate(name);
  }

    /**
     * Get the text currently contained in the <code>LabeledTextField</code>.
     * @return The information in the text field.
     */
  public String getText() {
    return textField.getText();
  }

    /**
     * Set the text currently contained in the <code>LabeledTextField</code>.
     * @param val  The value to be set.
     */
  public void setText(String val) {
    textField.setText(val);
  }

    /*
     * Get the current text as a <code>Vector</code> list.
     * A list may be given by typing comma separated values.
     * @return A <code>Vector</code> containing each item in the list.
     */
  public Vector getList() {
    String tmpStr = getText();
    Vector result = new Vector();
    StringTokenizer st = new StringTokenizer(tmpStr, ",");

    while (st.hasMoreTokens()) {
      String temp1 = st.nextToken();
      result.add(temp1);
    }
    return result;
  }


  /**
   * The <code>insertUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void insertUpdate(DocumentEvent e) {
    
    setAttribute(name.substring(0,name.length()-1), this);
  }

  /**
   * The <code>removeUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void removeUpdate(DocumentEvent e) {
    setAttribute(name.substring(0,name.length()-1), this);
  }

  /**
   * The <code>changedUpdate</code> method is not implemented.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void changedUpdate(DocumentEvent e) {
      
  }

  // implementation of java.awt.event.KeyListener interface

  /**
   * Implementation of <code>java.awt.event.KeyListener</code> interface.
   * If a CR character is pressed a Search is performed.
   * @param param1 A <code>KeyEvent</code> object.
   */
  public void keyTyped(KeyEvent param1) {
    // TODO: implement this java.awt.event.KeyListener method

    
    if ( param1.getKeyChar() == 10) {
      InfoPanel.searchButton.doClick();
    } // end of if ()
    
  }

  /**
   * Implementation of <code>java.awt.event.KeyListener</code> interface.
   * @param param1 A <code>KeyEvent</code> object.
   *
   */
  public void keyPressed(KeyEvent param1) {
    // TODO: implement this java.awt.event.KeyListener method
  }

  /**
   * Implementation of <code>java.awt.event.KeyListener</code> interface.
   * @param param1 A <code>KeyEvent</code> object.
   *
   */
  public void keyReleased(KeyEvent param1) {
    // TODO: implement this java.awt.event.KeyListener method
  }


}// LabeledTextField
