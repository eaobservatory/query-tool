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

package edu.jach.qt.utils;

import java.text.NumberFormat;
import gemini.sp.SpItem;
import gemini.sp.SpObs;
import gemini.sp.SpMSB;
import java.awt.Component;
import java.awt.Color;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import gemini.util.JACLogger;

/**
 * ObsListCellRenderer
 *
 * Created: Mon Mar 4 15:05:01 2002
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class ObsListCellRenderer extends DefaultListCellRenderer {
    final static ImageIcon obsIcon = new ImageIcon(
            System.getProperty("IMAG_PATH") + "observation.gif");
    static final JACLogger logger =
            JACLogger.getLogger(ObsListCellRenderer.class);
    public static Color OracBlue = new Color(0x80, 0x80, 0xFF);

    /**
     * This is the only method defined by ListCellRenderer.
     *
     * We just reconfigure the JLabel each time we're called.
     *
     * @param value value to display
     * @param index cell index
     * @param isSelected is the cell selected
     * @param cellHasFocus the list and the cell have the focus
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            logger.error("ObsListCellRenderer got a null value"
                    + " - this should not happen");
            return this;
        }

        if (!(value instanceof SpObs)) {
            logger.error("ObsListCellRenderer got a value of type "
                    + value.getClass().getName() + "- this should not happen");
            return this;
        }

        String s = ((SpObs) value).getTitle();

        // See if this observation is from the program list and has been done
        // This is indicated by a * at the end of the title attribute
        boolean hasBeenObserved = false;
        String titleAttr = ((SpObs) value).getTitleAttr();
        if (titleAttr != null && !(titleAttr.equals(""))) {
            if (titleAttr.endsWith("*")) {
                hasBeenObserved = true;
            }
        }

        String duration = ((SpObs) value).getTable().get("estimatedDuration");
        Double d = new Double(duration);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);
        try {
            duration = nf.format(d);
        } catch (Exception e) {
        }

        s += "( " + duration + " seconds )";
        setText(s);
        boolean isDone = false;

        String[] split = s.split("_");
        int i = 0;
        while (i < split.length) {
            if (split[i++].equals("done")) {
                isDone = true;
                break;
            }
        }

        if (((SpObs) value).isOptional() == false) {
            setForeground(list.getForeground());
        } else {
            setForeground(Color.green);
        }

        if (((SpObs) value).isMSB() && ((SpObs) value).isSuspended()) {
            setForeground(Color.red);
        } else if (!((SpObs) value).isMSB()) {
            // Find the parent MSB and see if it is suspended
            SpItem parent = ((SpObs) value).parent();
            while (parent != null && !(parent instanceof SpMSB)) {
                parent = parent.parent();
            }

            if (parent != null) {
                if (((SpMSB) parent).isSuspended()) {
                    setForeground(Color.red);
                }
            }
        }

        // Override the defaults
        if (isDone) {
            setForeground(Color.blue); // Done calibrations appear red
        }

        if (hasBeenObserved) {
            setForeground(Color.gray); // Done Observations appear gray
        }

        setText(s);
        setIcon(obsIcon);

        if (isSelected) {
            setBackground(OracBlue);
        } else {
            setBackground(list.getBackground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        repaint();

        return this;
    }
}
