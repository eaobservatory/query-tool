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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.List;
import java.util.Map;

// OT imports
import gemini.sp.SpItem;

// QT imports
import edu.jach.qt.utils.CalibrationList;

@SuppressWarnings("serial")
public class CalibrationsPanel extends JPanel {
    private final static Color lightBlue = new Color(0xCC, 0xCC, 0xFF);
    private final static String AND_STRING = "AND Folder: ";

    private JPanel left = new JPanel();
    private JPanel right = new JPanel();

    private Map<String, List<SpItem>> calibrationList;

    private JLabel waiting = new JLabel("Waiting for database ...");

    private boolean ready = false;

    public CalibrationsPanel() {
        setLayout(new GridLayout(1, 2, 10, 10));

        add(left);
        add(right);

        left.setLayout(new BorderLayout());
        right.setLayout(new BorderLayout());

        left.add(waiting, BorderLayout.NORTH);
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
        left.add(waiting, BorderLayout.NORTH);
        repaint();

        init();
    }

    private void fetchCalibrations() {
        final DefaultListModel listModel = new DefaultListModel();
        calibrationList = CalibrationList.getCalibrations();

        for (final Map.Entry<String, List<SpItem>> entry : calibrationList.entrySet()) {
            String key = entry.getKey();
            List<SpItem> items = entry.getValue();

            if (key.startsWith(AND_STRING) && (items.size() != 0)) {
                listModel.addElement(key.substring(AND_STRING.length()));
            }
        }

        // Creating the GUI needs to be done in the Swing thread,
        // so use invokeLater.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JList firstList = new JList(listModel);
                final JList secondList = new JList();

                firstList.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                firstList.setLayoutOrientation(JList.VERTICAL);
                firstList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object value = firstList.getSelectedValue();
                        if (value != null && value instanceof String) {
                            DefaultListModel listModel = new DefaultListModel();
                            List<SpItem> currentList = calibrationList.get(AND_STRING + value);

                            if (currentList != null) {
                                for (SpItem item : currentList) {
                                    listModel.addElement(item);
                                }
                            }

                            secondList.setModel(listModel);
                        }
                    }
                });

                left.removeAll();
                JScrollPane firstScrollPane = new JScrollPane(firstList);
                left.add(firstScrollPane, BorderLayout.CENTER);

                secondList.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                secondList.setLayoutOrientation(JList.VERTICAL);
                secondList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object value = secondList.getSelectedValue();
                        if (value != null && (value instanceof SpItem) && e.getValueIsAdjusting()) {
                            SpItem item = (SpItem) value;
                            DeferredProgramList.addCalibration(item);
                            JOptionPane.showMessageDialog(CalibrationsPanel.this,
                                    "'" + item.getTitleAttr()
                                    + "' added to deferred observations.");
                        }
                    }
                });
                secondList.setCellRenderer(new CalListCellRenderer());

                right.removeAll();
                JScrollPane secondScrollPane = new JScrollPane(secondList);
                right.add(secondScrollPane, BorderLayout.CENTER);

                ready = true;
            }
        });
    }

    private class CalListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if ((value == null) || ! (value instanceof SpItem)) {
                return this;
            }

            SpItem item = (SpItem) value;
            String title = item.getTitleAttr();

            setText(title);

            if (isSelected) {
                setBackground(lightBlue);
            } else {
                setBackground(list.getBackground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            repaint();

            return this;
        }
    }
}
