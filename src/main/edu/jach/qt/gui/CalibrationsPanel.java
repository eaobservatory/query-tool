/*
 * Copyright (C) 2010-2013 Science and Technology Facilities Council.
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

// Standard imports
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// OT imports
import orac.util.OrderedMap;
import gemini.sp.SpItem;

// QT imports
import edu.jach.qt.utils.CalibrationList;

@SuppressWarnings("serial")
public class CalibrationsPanel extends JPanel {
    private final static String AND_STRING = "AND Folder: ";

    private JPanel left = new JPanel();
    private JPanel right = new JPanel();

    private JList firstList;
    private JList secondList;

    private JScrollPane firstScrollPane;
    private JScrollPane secondScrollPane;

    private GridLayout gridlayout = new GridLayout(1, 2);

    private OrderedMap<String, OrderedMap<String, SpItem>> calibrationList;
    private OrderedMap<String, SpItem> currentList;

    private JLabel waiting = new JLabel("Waiting for database ...");

    private boolean ready = false;

    public CalibrationsPanel() {
        this.setLayout(gridlayout);
        this.add(left);
        this.add(right);

        left.add(waiting);
    }

    public void init() {
        (new Thread() {
            public void run() {
                fetchCalibrations();
            }
        }).start();
    }

    // Reload calibrations panel.  Must be called from the Swing thread.
    public void reload() {
        if (! ready) {
            System.err.println("Calibrations not yet loaded: can not reload");
            return;
        }

        ready = false;

        left.removeAll();
        right.removeAll();
        left.add(waiting);
        repaint();

        init();
    }

    private DefaultListModel setup() {
        DefaultListModel listModel = new DefaultListModel();
        calibrationList = CalibrationList.getCalibrations();
        int trimLength = AND_STRING.length();

        for (int index = 0; index < calibrationList.size(); index++) {
            OrderedMap<String, SpItem> folder = calibrationList.find(index);

            if (folder.size() != 0) {
                String key = calibrationList.getNameForIndex(index);

                if (key.startsWith("AND")) {
                    listModel.addElement(key.substring(trimLength));
                }
            }
        }

        return listModel;
    }

    private void fetchCalibrations() {
        // Call the setup() method to actually fetch the calibrations.
        final DefaultListModel listModel = setup();

        // Creating the GUI needs to be done in the Swing thread,
        // so use invokeLater.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                firstList = new JList(listModel);
                firstList.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                firstList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                firstList.setVisibleRowCount(-1);
                firstList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object value = firstList.getSelectedValue();
                        if (value != null && value instanceof String) {
                            secondList.setModel(second((String) value));
                        }
                    }
                });
                firstScrollPane = new JScrollPane(firstList);
                firstScrollPane.setPreferredSize(new Dimension(350, 400));
                left.remove(waiting);
                left.add(firstScrollPane);

                secondList = new JList();
                secondList.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                secondList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                secondList.setVisibleRowCount(-1);
                secondList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object value = secondList.getSelectedValue();
                        if (value != null && value instanceof String && e.getValueIsAdjusting() && currentList != null) {
                            SpItem item = currentList.find((String) value);
                            DeferredProgramList.addCalibration(item);
                            JOptionPane.showMessageDialog(CalibrationsPanel.this, "'" + value
                                    + "' added to deferred observations.");
                        }
                    }
                });
                secondScrollPane = new JScrollPane(secondList);
                secondScrollPane.setPreferredSize(new Dimension(350, 400));
                right.add(secondScrollPane);

                ready = true;
            }
        });
    }

    private DefaultListModel second(String selection) {
        DefaultListModel listModel = new DefaultListModel();
        currentList = calibrationList.find(AND_STRING + selection);

        for (int index = 0; index < currentList.size(); index++) {
            listModel.addElement(currentList.find(index).getTitleAttr());
        }

        return listModel;
    }
}
