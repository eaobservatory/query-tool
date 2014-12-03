/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.gui;

import edu.jach.qt.gui.WidgetDataBag;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;

/**
 * This class is a generic radioPanel with its group of JTextFieldButtons
 * enclosed with a titled border.
 *
 * Created: Tue Aug 7 09:35:40 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class JTextFieldPanel extends WidgetPanel implements DocumentListener {
    private String next, myTitle;
    private LinkedList<String> myElems;
    private ListIterator<String> iterator;
    private int viewPosition = BoxLayout.Y_AXIS;

    /**
     * A LinkedList of the buttons.
     *
     */
    public LinkedList<LabeledTextField> fieldElems =
            new LinkedList<LabeledTextField>();

    /**
     * Creates a new <code>JTextFieldPanel</code> instance.
     *
     * @param ht a <code>Hashtable</code> value
     * @param wdb a <code>WidgetDataBag</code> value
     * @param title a <code>String</code> value
     * @param elems a <code>LinkedList</code> value
     */
    public JTextFieldPanel(Hashtable<String, String> ht, WidgetDataBag wdb,
            CompInfo info) {
        super(ht, wdb);
        myTitle = info.getTitle();
        viewPosition = info.getView();
        myElems = info.getList();
        config();
    }

    /**
     * Configures the panel.
     */
    private void config() {
        iterator = myElems.listIterator(0);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), myTitle,
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
        setLayout(new BoxLayout(this, viewPosition));
        LabeledTextField tf;

        for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
            next = (String) iterator.next();
            String toolTip = null;

            if (next.matches(".*-.*")) {
                String[] split = next.split("-");
                next = split[0].trim();
                toolTip = split[1].trim();
            }

            tf = new LabeledTextField(super.abbrevTable, super.widgetBag, next,
                    toolTip);

            add(tf);
            fieldElems.add(tf);
            tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    /**
     * Set a given textfield with a value.
     *
     * @param field The Name of the associated <code>JTextField</code> object
     * @param value The value to set in the <code>JTextField</code>.
     */
    public void setTextField(String field, String val) {
        ListIterator<LabeledTextField> iterator = fieldElems.listIterator(0);

        while (iterator.hasNext()) {
            LabeledTextField thisTextField = iterator.next();

            if (thisTextField.getName().equals(field)) {
                thisTextField.setText(val);
            }
        }
    }

    /**
     * Get the text currently in a text field on the panel.
     *
     * @param field The label associated with the text field.
     * @return The value in the textfield as a <code>String</code>.
     */
    public String getText(String field) {
        String rtn = null;
        ListIterator<LabeledTextField> iterator = fieldElems.listIterator(0);

        while (iterator.hasNext()) {
            LabeledTextField thisTextField = iterator.next();

            if (thisTextField.getName().equals(field)) {
                rtn = thisTextField.getText();
            }
        }

        return rtn;
    }

    /**
     * Set the Tau in the specific text field dealing with it..
     *
     * @param value The value to set in the <code>JTextField</code>.
     */
    public void setTau(String val) {
        LabeledTextField temp = (fieldElems.getFirst());
        temp.setText(val);
    }

    /**
     * Add the current text to the WidgetDataBag.
     *
     * All observers are notified.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void insertUpdate(DocumentEvent e) {
        ListIterator<LabeledTextField> iter = fieldElems.listIterator(0);
        LabeledTextField tf;
        String name;

        for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
            tf = iter.next();

            name = tf.getName();
            setAttribute(name.substring(0, name.length() - 1), tf.getText());
        }
    }

    /**
     * Add the current text to the WidgetDataBag.
     *
     * All observers are notified.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void removeUpdate(DocumentEvent e) {
        ListIterator<LabeledTextField> iter = fieldElems.listIterator(0);
        LabeledTextField tf;
        String name;

        for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
            tf = iter.next();

            name = tf.getName();
            setAttribute(name.substring(0, name.length() - 1), tf.getText());
        }
    }

    /**
     * @param e a <code>DocumentEvent</code> value
     */
    public void changedUpdate(DocumentEvent e) {
        ListIterator<LabeledTextField> iter = fieldElems.listIterator(0);
        String name;
        LabeledTextField tf;

        for (iter.nextIndex(); iter.hasNext(); iter.nextIndex()) {
            tf = iter.next();
            name = tf.getName();

            setAttribute(name.substring(0, name.length() - 1), tf.getText());
        }
    }
}
