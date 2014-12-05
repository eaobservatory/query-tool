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
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import java.util.ListIterator;
import java.util.LinkedList;
import javax.swing.border.TitledBorder;
import java.util.Hashtable;
import edu.jach.qt.gui.WidgetDataBag;

/**
 * This is composite object designed for choosing instruments.
 *
 * JCheckboxes are used for selection and are grouped together with a titled
 * border.
 *
 * Created: Tue Mar 6 11:52:13 2001
 *
 * @author <a href="mailto:">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class ButtonPanel extends WidgetPanel {
    LinkedList<JCheckBox> buttonList;
    ListIterator<String> iterator;
    private String next, myTitle;

    /**
     * Creates a new <code>ButtonPanel</code> instance.
     *
     * @param parent a <code>WidgetPanel</code> value
     * @param title a <code>String</code> value
     * @param options a <code>LinkedList</code> value
     */
    public ButtonPanel(Hashtable<String, String> ht, WidgetDataBag wdb,
            CompInfo info) {
        super(ht, wdb);
        myTitle = info.getTitle();
        iterator = info.getList().listIterator(0);

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), myTitle,
                TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
        buttonList = new LinkedList<JCheckBox>();

        setLayout(new GridLayout(3, info.getSize() / 2));
        for (iterator.nextIndex(); iterator.hasNext(); iterator.nextIndex()) {
            next = iterator.next();

            JCheckBox cb = new JCheckBox(next);
            cb.addActionListener(this);

            add(cb);
            buttonList.add(cb);
        }
    }

    /**
     * Enables or diables each JCheckBox depending on the value of booleanFlag.
     *
     * @param booleanFlag a <code>boolean</code> value
     */
    public void setEnabled(boolean booleanFlag) {
        JCheckBox next;
        for (ListIterator<JCheckBox> iter = buttonList.listIterator();
                iter.hasNext(); iter.nextIndex()) {
            next = iter.next();

            if (!next.getText().equals("Any Instrument")
                    && !next.getText().equals("Any Heterodyne")
                    && !next.getText().equals("Any Country")
                    && !next.getText().equals("current")) {
                next.setEnabled(booleanFlag);
            }
        }
    }

    /**
     * Selects or unselects each JCheckBox method depending on the value
     * of flag.
     *
     * @param flag a <code>boolean</code> value
     */
    public void setSelected(boolean flag) {
        JCheckBox next;
        for (ListIterator<JCheckBox> iter = buttonList.listIterator();
                iter.hasNext(); iter.nextIndex()) {
            next = iter.next();

            if (!next.getText().equals("Any Instrument")
                    && !next.getText().equals("Any Heterodyne")
                    && !next.getText().equals("Any Country")
                    && !next.getText().equals("current")) {
                next.setSelected(flag);
                setAttribute(myTitle, buttonList);
            }
        }
    }

    private void wfcamSelected(boolean selected) {
        JCheckBox next;
        for (ListIterator<JCheckBox> iter = buttonList.listIterator();
                iter.hasNext(); iter.nextIndex()) {
            next = iter.next();

            // If flag is true, we need to deselect everything else and disable
            // the
            if (selected) {
                boolean wfcam = (next.getText().equals("WFCAM"));
                next.setSelected(wfcam);
                next.setEnabled(wfcam);
            } else {
                // WFCAM is deselected. Just enable all other buttons
                next.setEnabled(!selected);
            }
        }

        setAttribute(myTitle, buttonList);
    }

    /**
     * Notifies the WidgetDataBag of the state of all JCheckBoxes.
     *
     * @param evt an <code>ActionEvent</code> value
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        JCheckBox temp = (JCheckBox) source;

        if (temp.getText().equals("WFCAM")) {
            wfcamSelected(temp.isSelected());
            return;
        }

        if (temp.getText().equals("Any Instrument")
                || temp.getText().equals("Any Heterodyne")
                || temp.getText().equals("Any Country")
                || temp.getText().equals("current")) {
            if (temp.isSelected()) {
                setSelected(false);
                setEnabled(false);
            } else {
                setEnabled(true);
                setSelected(false);
            }
        } else {
            setAttribute(myTitle, buttonList);
        }
    }
}
