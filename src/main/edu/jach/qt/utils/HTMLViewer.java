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
    static URL _baseURL;
    static CalHTMLPane _calPane = new CalHTMLPane();
    private JOptionPane optionPane;
    private static boolean _isVisible = false;

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
        _baseURL = url;

        Object[] inputArray = {_calPane};

        final String backString = "Return to Translation";
        final String exitString = "Dismiss";
        Object[] options = {backString, exitString};

        optionPane = new JOptionPane(inputArray, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options);
        setContentPane(optionPane);
        pack();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window, we're going to change
                 * the JOptionPane's value property.
                 */
                optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (isVisible()
                        && (e.getSource() == optionPane)
                        && (prop.equals(JOptionPane.VALUE_PROPERTY)
                                || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                    Object value = optionPane.getValue();

                    if (value == JOptionPane.UNINITIALIZED_VALUE) {
                        return;
                    }

                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

                    if (value.equals(backString)) {
                        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        _calPane.showHTMLDocument(_baseURL);

                        return;

                    } else if (!value.equals(exitString)) {
                        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

                        return;
                    }

                    setVisible(false);
                    _isVisible = false;
                }
            }
        });

        pack();
        setVisible(true);
        _isVisible = true;
        this.getRootPane().requestFocus();
    }

    synchronized public static void showViewer(String name) {
        new HTMLViewer(null, name);
    }

    synchronized static public boolean visible() {
        return _isVisible;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Base");
        frame.setVisible(true);
        frame.setSize(new Dimension(100, 100));
        new HTMLViewer(null, "/observe/ompodf/hettrans.html");
        System.out.println("Control returned to main");
        System.exit(0);
    }
}
