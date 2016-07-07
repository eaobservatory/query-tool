/*
 * Copyright (C) 2003-2010 Science and Technology Facilities Council.
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

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import calpa.html.CalHTMLPane;

@SuppressWarnings("serial")
public class HTMLViewer extends JDialog {
    static CalHTMLPane _calPane = new CalHTMLPane();
    private JOptionPane optionPane;

    public HTMLViewer(JFrame parent, String fName) {
        super((JFrame) null, false);

        final CalHTMLPane _calPane = new CalHTMLPane();
        _calPane.setLoadSynchronously(false);
        _calPane.setPreferredSize(new Dimension(950, 700));
        _calPane.setVisible(true);
        URL url = null;

        try {
            url = new URL("file://" + fName);
        } catch (Exception e) {
            System.out.println("Error loasdinf doc");
        }

        _calPane.showHTMLDocument(url);

        setContentPane(_calPane);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        this.getRootPane().requestFocus();
    }
}
