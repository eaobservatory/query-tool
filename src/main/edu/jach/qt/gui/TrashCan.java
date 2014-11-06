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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Ultra simplistic Trashcan class.
 *
 * All it does is associate a icon with a label!
 */
@SuppressWarnings("serial")
public class TrashCan extends JLabel {
    public static final String BIN_IMAGE = System.getProperty("binImage");
    public static final String BIN_SEL_IMAGE = System.getProperty("binImage");

    /**
     * Contructor.
     */
    public TrashCan() {
        try {
            URL url = new URL("file://" + BIN_IMAGE);
            setIcon(new ImageIcon(url));
        } catch (MalformedURLException mue) {
            setIcon(new ImageIcon(ProgramTree.class.getResource("file://"
                    + BIN_IMAGE)));
        }
    }
}
