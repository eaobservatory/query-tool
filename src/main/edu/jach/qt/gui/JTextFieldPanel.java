package edu.jach.qt.gui;

import edu.jach.qt.gui.WidgetDataBag;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * JTextFieldPanel.java
 *
 * This class is a generic radioPanel with it's group of JTextFieldButtons
 * enclosed with a titled border.
 *
 * Created: Tue Aug  7 09:35:40 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class JTextFieldPanel extends WidgetPanel 
  implements DocumentListener{

  private JTextField tf;
  private String next, myTitle;
  private LinkedList myElems;
  private ListIterator iterator;
  private int viewPosition = BoxLayout.Y_AXIS;

  /**
   * The variable <code>radioElems</code> is a LinkedList of the buttons.
   *
   */
  public LinkedList fieldElems = new LinkedList();

  /**
   * Creates a new <code>JTextFieldPanel</code> instance.
   *
   * @param ht a <code>Hashtable</code> value
   * @param wdb a <code>WidgetDataBag</code> value
   * @param title a <code>String</code> value
   * @param elems a <code>LinkedList</code> value
   */
  public JTextFieldPanel (Hashtable ht, WidgetDataBag wdb, CompInfo info){
    super(ht, wdb);
    myTitle = info.getTitle();
    viewPosition = info.getView();
    myElems = info.getList();
    config();
  }

  private void config() {
    iterator = myElems.listIterator(0);
    //setOpaque(false);
    setBorder(BorderFactory.createTitledBorder
	      (BorderFactory.createEtchedBorder(), myTitle,
	       TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
    setLayout(new BoxLayout(this, viewPosition));
    LabeledTextField tf;
    for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
      next = (String)iterator.next();
      tf = new LabeledTextField(super.abbrevTable, super.widgetBag, next);
	 
      add(tf);
      fieldElems.add(tf);
      tf.setAlignmentX(tf.CENTER_ALIGNMENT);
    }
  }

  public void setTau(String val) {
    LabeledTextField temp = (LabeledTextField) (fieldElems.getFirst());
    temp.setText(val);
  }

  /**
   * The <code>insertUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void insertUpdate(DocumentEvent e) {
    ListIterator iter = fieldElems.listIterator(0);
    LabeledTextField tf;
    String name;
    
    for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
      tf = (LabeledTextField)iterator.next();

      name = tf.getName();
      setAttribute(name.substring(0,name.length()-1), tf.getText());
    } // end of for (int  = 0;  < ; ++)
  }

  /**
   * The <code>removeUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void removeUpdate(DocumentEvent e) {
    ListIterator iter = fieldElems.listIterator(0);
    LabeledTextField tf;
    String name;

    for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
      tf = (LabeledTextField)iterator.next();

      name = tf.getName();
      setAttribute(name.substring(0,name.length()-1), tf.getText());
    } // end of for (int  = 0;  < ; ++)
  }

  /**
   * The <code>changedUpdate</code> method is not implemented.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void changedUpdate(DocumentEvent e) {
    ListIterator iter = fieldElems.listIterator(0);
    String name;
    LabeledTextField tf;

    for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
      tf = (LabeledTextField)iterator.next();
      name = tf.getName();

      setAttribute(name.substring(0,name.length()-1), tf.getText());
    } // end of for (int  = 0;  < ; ++)
    
  }

}// JTextFieldPanel
