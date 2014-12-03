/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
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

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;

import java.util.LinkedList;
import java.util.Hashtable;

/**
 * This composite widget contains a JLabel, a JComboBox, and a JTextField.
 *
 * The JLabel and JTextField are inherited from LabeledTextField. The JComboBox
 * is extending LabeledTextField.
 *
 * Created: Thu Sep 20 14:49:45 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class LabeledMinMaxTextField extends LabeledTextField {
    protected LinkedList<Serializable> valueList;
    protected JComboBox rangeList;

    /**
     * Constructor.
     *
     * @param ht <code>Hashtable</code> of widget names and abbreviated names.
     * @param wdb <code>WidgetDataBag</code> of widget information.
     * @param text The label for this object.
     */
    public LabeledMinMaxTextField(Hashtable<String, String> ht,
            WidgetDataBag wdb, String text) {
        super(ht, wdb, text);

        valueList = new LinkedList<Serializable>();
        rangeList = new JComboBox();

        setup();
    }

    public LabeledMinMaxTextField(Hashtable<String, String> ht,
            WidgetDataBag wdb, String text, String toolTip) {
        super(ht, wdb, text, toolTip);

        valueList = new LinkedList<Serializable>();
        rangeList = new JComboBox();

        setup();
    }

    private void setup() {
        this.setLayout(new GridLayout(1, 3));
        add(label);

        rangeList.addItem("Max");
        rangeList.addItem("Min");
        rangeList.setSelectedIndex(0);
        rangeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAttribute(getLabel(), valueList);
            }
        });
        add(rangeList);
        valueList.add(0, rangeList);
        add(textField);
        textField.getDocument().addDocumentListener(this);
    }

    /**
     * Add the current text in the text field to slot 1 of the valueList.
     *
     * The WidgetDataBag is updated.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void insertUpdate(DocumentEvent e) {
        setAttribute(getLabel(), valueList);
    }

    /**
     * Add the current text in the text field to slot 1 of the valueList.
     *
     * The WidgetDataBag is updated.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void removeUpdate(DocumentEvent e) {
        valueList.add(1, textField.getText());
        setAttribute(getLabel(), valueList);
    }

    /**
     * Not implemented.
     *
     * We don't require an action here.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void changedUpdate(DocumentEvent e) {
    }

    /**
     * Add the current text in the text field to slot 1 of the valueList.
     *
     * It returns the label of this object.
     *
     * @return a <code>String</code> value
     */
    public String getLabel() {
        valueList.add(1, textField.getText());
        String name = label.getText().trim();
        return name.substring(0, name.length() - 1);
    }
}
