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
 * This class is a generic radioPanel with it's group of JRadioButtons
 * enclosed with a titled border.
 *
 * Created: Tue Aug  7 09:35:40 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class RadioPanel extends WidgetPanel 
  implements ActionListener{

  private JRadioButton rb;
  private String next, myTitle;
  private LinkedList myElems;
  private ListIterator iterator;
  private int viewPosition = BoxLayout.Y_AXIS;

  /**
   * The variable <code>group</code> is the list of JRadioButtons.
   *
   */
  public ButtonGroup group = new ButtonGroup();

  /**
   * The variable <code>radioElems</code> is a LinkedList of the buttons.
   *
   */
  public LinkedList radioElems = new LinkedList();

  /**
   * Creates a new <code>RadioPanel</code> instance.
   *
   * @param ht a <code>Hashtable</code> value
   * @param wdb a <code>WidgetDataBag</code> value
   * @param title a <code>String</code> value
   * @param elems a <code>LinkedList</code> value
   */
  public RadioPanel (Hashtable ht, WidgetDataBag wdb, CompInfo info){
    super(ht, wdb);
    myTitle = info.getTitle();
    viewPosition = info.getView();
    myElems = info.getList();
    config();
  }

  private void config() {
    iterator = myElems.listIterator(0);
    setOpaque(false);
    setBorder(BorderFactory.createTitledBorder
	      (BorderFactory.createEtchedBorder(), myTitle,
	       TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
    setLayout(new BoxLayout(this, viewPosition));
    for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
      next = (String)iterator.next();
      rb = new JRadioButton(next);
      rb.addActionListener(this);
      //rb.setBackground(java.awt.Color.gray);
      //rb.setForeground(java.awt.Color.green);
	 
      add(rb);
      rb.setAlignmentX(rb.CENTER_ALIGNMENT);
      group.add(rb);
      radioElems.add(rb);
      if ((iterator.nextIndex() - 1) == 0)
	rb.doClick();
    }
  }

  /**
   * The <code>actionPerformed</code> method will notify the
   * WidgetDataBag of changes.  This updates the XML string contained
   * in the Querytool.
   *
   * @param evt an <code>ActionEvent</code> value
   */
  public void actionPerformed (ActionEvent evt) {
    setAttribute(myTitle, radioElems);
  }
}// RadioPanel
