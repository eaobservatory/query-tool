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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

// OT imports
import gemini.sp.SpItem;
import gemini.sp.SpObs;
import gemini.sp.SpTelescopePos;
import gemini.sp.SpTreeMan;
import gemini.sp.obsComp.SpInstObsComp;
import gemini.sp.obsComp.SpTelescopeObsComp;
import orac.jcmt.inst.SpInstHeterodyne;

// QT imports
import edu.jach.qt.utils.CalibrationList;

@SuppressWarnings("serial")
public class CalibrationsPanel extends JPanel {
    private final static Color lightBlue = new Color(0xCC, 0xCC, 0xFF);

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

            if (items.size() != 0) {
                listModel.addElement(key);
            }
        }

        // Creating the GUI needs to be done in the Swing thread,
        // so use invokeLater.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JList firstList = new JList(listModel);
                final JList<SpItem> secondList = new JList<SpItem>();

                firstList.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                firstList.setLayoutOrientation(JList.VERTICAL);
                firstList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        Object value = firstList.getSelectedValue();
                        if (value != null && value instanceof String) {
                            DefaultListModel<SpItem> listModel = new DefaultListModel<SpItem>();
                            List<SpItem> currentList = calibrationList.get(value);

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
                        SpItem item = secondList.getSelectedValue();
                        if (item != null && e.getValueIsAdjusting()) {
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

                JPanel sortPanel = new JPanel();
                right.add(sortPanel, BorderLayout.NORTH);

                sortPanel.add(new JLabel("Sort by:"));

                JButton button = new JButton("Title");
                button.setMargin(new Insets(1, 1, 1, 1));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sortSpItemList(secondList, new Comparator<SpItem>() {
                             public int compare(SpItem o1, SpItem o2) {
                                String t1 = o1.getTitleAttr();
                                String t2 = o2.getTitleAttr();
                                return t1.compareTo(t2);
                            }
                        });
                    }
                });
                sortPanel.add(button);

                button = new JButton("Target name");
                button.setMargin(new Insets(1, 1, 1, 1));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sortSpItemList(secondList, new SpItemTargetComparator(true));
                    }
                });
                sortPanel.add(button);

                button = new JButton("Target pos.");
                button.setMargin(new Insets(1, 1, 1, 1));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sortSpItemList(secondList, new SpItemTargetComparator(false));
                    }
                });
                sortPanel.add(button);

                button = new JButton("Freq.");
                button.setMargin(new Insets(1, 1, 1, 1));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sortSpItemList(secondList, new Comparator<SpItem>() {
                            public int compare(SpItem o1, SpItem o2) {
                                SpItem obs1 = SpTreeMan.findAllItems(o1, SpObs.class.getName()).firstElement();
                                SpItem obs2 = SpTreeMan.findAllItems(o2, SpObs.class.getName()).firstElement();

                                SpInstObsComp inst1 = SpTreeMan.findInstrument(obs1);
                                SpInstObsComp inst2 = SpTreeMan.findInstrument(obs2);

                                if (((inst1 == null) || ! (inst1 instanceof SpInstHeterodyne)) &&
                                        ((inst2 == null) || ! (inst2 instanceof SpInstHeterodyne))) {
                                    return 0;
                                }
                                else if ((inst1 == null) || ! (inst1 instanceof SpInstHeterodyne)) {
                                    return 1;
                                }
                                else if ((inst2 == null) || ! (inst2 instanceof SpInstHeterodyne)) {
                                    return -1;
                                }
                                else {
                                    return Double.compare(
                                        ((SpInstHeterodyne) inst1).getRestFrequency(0),
                                        ((SpInstHeterodyne) inst2).getRestFrequency(0));
                                }
                            }
                        });
                    }
                });
                sortPanel.add(button);

                ready = true;
            }
        });
    }

    private class SpItemTargetComparator implements Comparator<SpItem> {
        boolean name;

        public SpItemTargetComparator(boolean name) {
            this.name = name;
        }

        public int compare(SpItem o1, SpItem o2) {
            SpItem obs1 = SpTreeMan.findAllItems(o1, SpObs.class.getName()).firstElement();
            SpItem obs2 = SpTreeMan.findAllItems(o2, SpObs.class.getName()).firstElement();

            SpTelescopeObsComp obsComp1 = SpTreeMan.findTargetList(obs1);
            SpTelescopeObsComp obsComp2 = SpTreeMan.findTargetList(obs2);

            SpTelescopePos pos1 = (obsComp1 == null) ? null : obsComp1.getPosList().getBasePosition();
            SpTelescopePos pos2 = (obsComp2 == null) ? null : obsComp2.getPosList().getBasePosition();

            if ((pos1 == null) && (pos2 == null)) {
                return 0;
            }
            else if (pos1 == null) {
                return 1;
            }
            else if (pos2 == null) {
                return -1;
            }
            else if (name) {
                return pos1.getName().compareTo(pos2.getName());
            }
            else {
                return Double.compare(pos1.getXaxis(), pos2.getXaxis());
            }
        }
    }

    private void sortSpItemList(JList<SpItem> list, Comparator<SpItem> comparator) {
        list.clearSelection();

        DefaultListModel<SpItem> model = (DefaultListModel<SpItem>) list.getModel();
        if ((model == null) || (model.size() == 0)) {
            return;
        }

        for (int i = 1; i < model.size(); i ++) {
            SpItem item = model.get(i);

            for (int j = 0; j < i; j ++ ) {
                if (comparator.compare(item, model.get(j)) < 0) {
                    model.add(j, model.remove(i));
                    break;
                }
            }
        }

        list.ensureIndexIsVisible(0);
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
