/*
 * Copyright 1999 United Kingdom Astronomy Technology Centre, an
 * establishment of the Science and Technology Facilities Council.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright (C) 2001-2013 Science and Technology Facilities Council.
 * Copyright (C) 2016 East Asian Observatory.
 */

package edu.jach.qt.gui;

/* Gemini imports */
import gemini.sp.SpItem;
import gemini.sp.SpObs;
import gemini.sp.SpTreeMan;
import gemini.sp.SpType;
import gemini.sp.obsComp.SpInstObsComp;
import gemini.util.JACLogger;

/* ORAC imports */
import orac.jcmt.inst.SpInstHeterodyne;

/* QT imports */
import edu.jach.qt.utils.ObsListCellRenderer;
import edu.jach.qt.utils.QtTools;

/* Standard imports */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Component;

import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragGestureEvent;
import java.awt.datatransfer.StringSelection;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Vector;
import java.util.TooManyListenersException;

import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.BorderFactory;
import javax.swing.ToolTipManager;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * A panel to select an observation from a List object.
 *
 * @version 1.0 1st June 1999
 * @author M.Tan@roe.ac.uk, modified by Mathew Rippa
 */
@SuppressWarnings("serial")
final public class ProgramTree extends JPanel implements
        DragSourceListener, DragGestureListener,
        DropTargetListener {
    static final JACLogger logger = JACLogger.getLogger(ProgramTree.class);
    private GridBagConstraints gbc;
    private static JButton engButton;
    private static JList obsList;
    private DefaultListModel model;
    private JScrollPane scrollPane = new JScrollPane();;
    private static SpItem _spItem;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private TreeViewer tv = null;
    private TreePath path;
    private DropTarget dropTarget = null;
    private DragSource dragSource = null;
    private TrashCan trash = null;
    private static SpItem selectedItem = null;
    private static SpItem obsToDefer = null;
    private Vector<SpObs> haveScaled = new Vector<SpObs>();
    private Vector<Double> scaleFactors = new Vector<Double>();
    private JMenuItem scaleAgain;
    private JPopupMenu scalePopup;

    private static final String sendToQueue = "Send to Queue";
    private static final String disabled =
            "Time constraint edited, click \"Set Default\"";

    private SelectionListener selectionlistener = null;
    private MouseListener popupListener = null;

    /**
     * The constructor.
     *
     * The class has only one constructor so far. A few thing are done during
     * the construction. They are mainly about adding a run button and setting
     * up listeners.
     */
    public ProgramTree() {
        // Ensure nothing is selected
        setSelectedItem(null);

        Border border =
                BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
        setBorder(new TitledBorder(border, "Retrieved MSBs", 0, 0,
                new Font("Roman", Font.BOLD, 12), Color.black));
        setLayout(new BorderLayout());

        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        gbc = new GridBagConstraints();

        engButton = new JButton();
        engButton.setText(sendToQueue);
        engButton.setMargin(new Insets(5, 10, 5, 10));
        engButton.setEnabled(true);
        engButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doExecute(true);
            }
        });

        JButton xpand = new JButton("Expand Observation");
        xpand.setMargin(new Insets(5, 10, 5, 10));
        xpand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                SpItem itemToXpand;

                if (getSelectedItem() == null
                        && DeferredProgramList.getCurrentItem() == null) {
                    return;
                } else if (getSelectedItem() == null) {
                    itemToXpand = DeferredProgramList.getCurrentItem();
                } else {
                    itemToXpand = getSelectedItem();
                }

                SpItem temp = itemToXpand.deepCopy();

                SpInstObsComp inst = SpTreeMan.findInstrumentInContext(itemToXpand);

                if (inst == null) {
                    inst = SpTreeMan.findInstrument(itemToXpand);

                    if (inst != null) {
                        QtTools.insert(inst, temp);
                    }
                }

                if (tv == null) {
                    tv = new TreeViewer(temp);
                } else {
                    tv.update(temp);
                }
            }
        });

        dropTarget = new DropTarget();
        try {
            dropTarget.addDropTargetListener(this);
        } catch (TooManyListenersException tmle) {
            logger.error("Too many drop target listeners", tmle);
        }

        trash = new TrashCan();
        trash.setDropTarget(dropTarget);

        dragSource = new DragSource();

        // Create a popup menu
        scalePopup = new JPopupMenu();

        JMenuItem edit = new JMenuItem("Edit Attribute...");
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editAttributes();
            }
        });
        scalePopup.add(edit);

        JMenuItem scale = new JMenuItem("Scale Exposure Times...");
        scale.setEnabled(false);
        scale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scaleAttributes();
            }
        });
        scalePopup.add(scale);

        scaleAgain = new JMenuItem("Re-do Scale Exposure Times");
        scaleAgain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rescaleAttributes();
            }
        });
        scaleAgain.setEnabled(false);
        scalePopup.add(scaleAgain);

        JMenuItem sendToEng = new JMenuItem("Send for Engineering");
        sendToEng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doExecute(false);
            }
        });
        scalePopup.add(sendToEng);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets.left = 10;
        gbc.insets.right = 0;
        add(trash, gbc, 1, 1, 1, 1);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.insets.bottom = 20;
        add(engButton, gbc, 0, 1, 1, 1);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.insets.bottom = 20;
        add(xpand, gbc, 0, 2, 1, 1);
    }

    public static void setSelectedItem(SpItem item) {
        selectedItem = item;
    }

    public static SpItem getSelectedItem() {
        return selectedItem;
    }

    public static void setCurrentItem(SpItem item) {
        _spItem = item;
    }

    public static SpItem getCurrentItem() {
        return _spItem;
    }

    public static void setObservationToDefer(SpItem item) {
        obsToDefer = item;
    }

    public static SpItem getObservationToDefer() {
        return obsToDefer;
    }

    /**
     * Set the "Send for Execution" to (dis)abled.
     *
     * @param flag <code>true</code> to enable execution.
     */
    public static void setExecutable(boolean flag) {
        logger.debug("In ProgramTree.setExecutable(); setting run.enabled to "
                + flag);
        engButton.setEnabled(flag);

        if (flag == false) {
            engButton.setText(disabled);
            engButton.setToolTipText("Disabled due to edited time constraint");
            ToolTipManager.sharedInstance().setInitialDelay(250);
        } else {
            engButton.setText(sendToQueue);
            engButton.setToolTipText(null);
        }
    }

    /**
     * Add a compnent to the <code>GridBagConstraints</code>
     *
     * @param c a <code>Component</code> value
     * @param gbc a <code>GridBagConstraints</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     */
    public void add(Component c, GridBagConstraints gbc, int x, int y, int w,
            int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        add(c, gbc);
    }

    public void doExecute(boolean useQueue) {
        SpItem item = null;
        boolean isDeferred = false;

        if (obsList.getSelectedValue() == null
                && DeferredProgramList.getCurrentItem() == null) {
            JOptionPane.showMessageDialog(null,
                    "You have not selected an observation!",
                    "Please select an observation.", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (obsList.getSelectedValue() == null) {
            isDeferred = true;
            item = DeferredProgramList.getCurrentItem();
        } else if (useQueue || System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
            item = ProgramTree.getCurrentItem();
        } else {
            item = ProgramTree.getSelectedItem();
        }

        setExecutable(false);
        engButton.setToolTipText("Run button disabled during execution");

        if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
            (new ExecuteJCMT(item, isDeferred) {
                @Override
                protected void doAfterExecute(boolean success) {
                    if (success) {
                        if (! isDeferred) {
                            model.clear();
                            setCurrentItem(null);
                            setSelectedItem(null);

                        } else {
                            DeferredProgramList.markThisObservationAsDone(
                                    itemToExecute);
                        }
                    }

                    setExecutable(true);

                    if (! isDeferred && success) {
                        obsList.setListData(new Vector());
                        obsList.clearSelection();
                    }

                    logger.debug("Enabling run button since the ExecuteJCMT task has"
                            + " completed");
                }
            }).execute();
        }
    }

    private void markAsDone(int index) {
        SpObs obs = (SpObs) model.getElementAt(index);
        String title = obs.getTitle();

        // Search through and see if we have a remaining of the form "(nX)"
        if (title.endsWith("X)")) {
            // Find the index of the last space character
            int lastSpace = title.lastIndexOf(" ");
            title = title.substring(0, lastSpace);
        }

        if (!(title.endsWith("*"))) {
            title = title + "*";
            obs.setTitleAttr(title);
            model.setElementAt(obs, index);
        }
    }

    /**
     * Set up the List GUI and populate it with the results of a query.
     *
     * @param sp The list of obervations in the MSB.
     */
    public void addList(SpItem sp) {
        // Check if there is already an existing model and whether it still has
        // observations to perform
        SpInstObsComp instrumentContext = getContext(sp);

        // If we get here we should reinitialise with the new argument
        setCurrentItem(sp);

        model = new DefaultListModel();

        if (getCurrentItem() == null) {
            model.clear();
        } else {
            Vector<SpItem> obsVector = SpTreeMan.findAllItems(sp,
                    SpObs.class.getName());

            for (SpItem item : obsVector) {
                model.addElement(item);
            }
        }

        if (obsList == null) {
            obsList = new JList(model);
        } else {
            obsList.setModel(model);
        }

        ToolTipManager.sharedInstance().registerComponent(obsList);
        ToolTipManager.sharedInstance().setDismissDelay(3000);
        obsList.setCellRenderer(new ObsListCellRenderer());
        obsList.setToolTipText(
                "<html>Optional observations are shown in GREEN"
                + "<br>Calibrations which have been done are shown in BLUE"
                + "<br>Suspended MSBs are shown in RED</html>");

        if (selectionlistener == null) {
            selectionlistener = new SelectionListener();
            obsList.addMouseListener(selectionlistener);
        }

        if (popupListener == null) {
            popupListener = new PopupListener();
            obsList.addMouseListener(popupListener);
        }

        if (model.size() > 0) {
            obsList.setSelectedIndex(0);
            setSelectedItem((SpItem) obsList.getSelectedValue());
        }

        dragSource.createDefaultDragGestureRecognizer(obsList,
                DnDConstants.ACTION_MOVE, this);

        // Add the listbox to a scrolling pane
        scrollPane.getViewport().removeAll();
        scrollPane.getViewport().add(obsList);
        scrollPane.getViewport().setOpaque(false);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.bottom = 5;
        gbc.insets.left = 10;
        gbc.insets.right = 5;
        gbc.weightx = 100;
        gbc.weighty = 100;
        add(scrollPane, gbc, 0, 0, 2, 1);
    }

    /**
     * Clear the selection from the Program Tree List.
     */
    public static void clearSelection() {
        obsList.clearSelection();
        setSelectedItem(null);
    }

    /**
     * Remove the currently selected node.
     */
    public void removeCurrentNode() {
        SpObs item = (SpObs) obsList.getSelectedValue();

        Vector<SpItem> obsV = SpTreeMan.findAllItems(getCurrentItem(),
                SpObs.class.getName());

        int index = obsList.getSelectedIndex();
        ((DefaultListModel) obsList.getModel()).removeElementAt(index);

        SpObs[] obsToDelete = null;
        if (obsV.size() > index) {
            obsToDelete = new SpObs[]{(SpObs) obsV.elementAt(index)};
        }

        try {
            if (obsToDelete != null && SpTreeMan.evalExtract(obsToDelete)) {
                SpTreeMan.extract(obsToDelete);
            } else if (item == null) {
                JOptionPane.showMessageDialog(this, "No Observation to remove",
                        "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Exception encountered while deleting observation", e);
        }
    }

    /**
     * Invoke the attribute editor on the current item, as long as that item is
     * an observation.
     */
    private void editAttributes() {
        // Recheck that this is an observation
        if (getSelectedItem().type() == SpType.OBSERVATION) {
            setExecutable(false);

            SpObs observation = (SpObs) getSelectedItem();

            try {
                if (!observation.equals(null)) {
                    new AttributeEditor(
                            observation, new javax.swing.JFrame(), true
                    ).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Current selection is not an observation.",
                            "Not an Obs!", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                logger.error("Error instantiating AttributeEditor", e);
            } finally {
                setExecutable(true);
            }
        }
    }

    /**
     * Invoke the attribute scaler on the current item, as long as that item is
     * an observation.
     */
    private void scaleAttributes() {
        if (getSelectedItem() != null
                && getSelectedItem().type() == SpType.OBSERVATION) {
            setExecutable(false);

            SpObs observation = (SpObs) getSelectedItem();

            if (!observation.equals(null)) {
                new AttributeEditor(
                        observation, new javax.swing.JFrame(),
                        true, "EXPTIME", haveScaled.contains(observation),
                        lastScaleFactor(), false
                ).setVisible(true);

                double sf = AttributeEditor.scaleFactorUsed();

                if (sf > 0) {
                    haveScaled.addElement(observation);
                    scaleFactors.addElement(new Double(sf));
                    scaleAgain.setEnabled(true);
                    scaleAgain.setText(
                        "Re-do Scale Exposure Times (x" + sf + ")");
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Current selection is not an observation.",
                        "Not an Obs!", JOptionPane.INFORMATION_MESSAGE);
            }

            setExecutable(true);
        }
    }

    /**
     * Reinvoke the attribute scaler on the current item, as long as that item
     * is an observation.
     */
    private void rescaleAttributes() {
        if (getSelectedItem() == null
                || getSelectedItem().type() != SpType.OBSERVATION) {
            setExecutable(false);

            SpObs observation = (SpObs) getSelectedItem();

            if (!observation.equals(null)) {
                new AttributeEditor(
                        observation, new javax.swing.JFrame(),
                        true, "EXPTIME", haveScaled.contains(observation),
                        lastScaleFactor(), true
                ).setVisible(true);

                double sf = AttributeEditor.scaleFactorUsed();

                if (sf > 0) {
                    haveScaled.addElement(observation);
                    scaleFactors.addElement(new Double(sf));
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Current selection is not an observation.",
                        "Not an Obs!", JOptionPane.INFORMATION_MESSAGE);
            }

            setExecutable(true);
        }
    }

    private Double lastScaleFactor() {
        if (scaleFactors.size() == 0) {
            if (AttributeEditor.scaleFactorUsed() > 0) {
                return new Double(AttributeEditor.scaleFactorUsed());
            } else {
                return new Double(1.0);
            }
        } else {
            return scaleFactors.elementAt(scaleFactors.size() - 1);
        }
    }

    private SpInstObsComp getContext(SpItem item) {
        SpInstObsComp instrumentContext = null;

        if (item != null) {
            Vector<SpItem> obs =
                    SpTreeMan.findAllItems(item, SpObs.class.getName());
            instrumentContext = SpTreeMan.findInstrument(obs.firstElement());
        }

        return instrumentContext;
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            // If this was not the right button just return immediately.
            if (!e.isPopupTrigger()) {
                return;
            }

            // If this is an observation then show the popup
            if (getSelectedItem() != null
                    && getSelectedItem().type() == SpType.OBSERVATION) {
                scalePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class SelectionListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                setSelectedItem((SpItem) obsList.getSelectedValue());
                doExecute(true);

            } else if (e.getClickCount() == 1
                    && (e.getModifiers() & InputEvent.BUTTON1_MASK)
                            == InputEvent.BUTTON1_MASK) {
                if (getSelectedItem() != obsList.getSelectedValue()) {
                    // Select the new item
                    setSelectedItem((SpItem) obsList.getSelectedValue());
                    DeferredProgramList.clearSelection();
                    NotePanel.setNote(ProgramTree.getCurrentItem());

                } else if (e.getClickCount() == 1) {
                    if (getSelectedItem() != obsList.getSelectedValue()) {
                        // Select the new item
                        setSelectedItem((SpItem) obsList.getSelectedValue());
                        DeferredProgramList.clearSelection();
                        NotePanel.setNote(ProgramTree.getCurrentItem());

                    } else {
                        obsList.clearSelection();
                        setSelectedItem(null);
                    }
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            DeferredProgramList.clearSelection();
            enableList(false);
        }

        public void mouseReleased(MouseEvent e) {
            enableList(true);
        }
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface
     *
     * @param evt <code>DropTargetDragEvent</code> event
     */
    public void dragEnter(DropTargetDragEvent evt) {
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface
     *
     * @param evt <code>DropTargetEvent</code> event
     */
    public void dragExit(DropTargetEvent evt) {
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface
     *
     * @param evt <code>DropTargetDragEvent</code> event
     */
    public void dragOver(DropTargetDragEvent evt) {
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface
     *
     * @param evt <code>DropTargetDropEvent</code> event
     */
    public void drop(DropTargetDropEvent evt) {
        SpObs itemForDrop;

        if (getSelectedItem() != null) {
            itemForDrop = (SpObs) getSelectedItem();
        } else {
            itemForDrop = (SpObs) DeferredProgramList.getCurrentItem();
        }

        if (itemForDrop != null && !itemForDrop.isOptional()) {
            JOptionPane.showMessageDialog(this,
                    "Can not delete a mandatory observation!");
        } else {
            evt.acceptDrop(DnDConstants.ACTION_MOVE);
            evt.getDropTargetContext().dropComplete(true);
        }
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface
     *
     * @param evt <code>DropTargetDragEvent</code> event
     */
    public void dropActionChanged(DropTargetDragEvent evt) {
    }

    /**
     * Implementation of <code>DragGestureListener</code> Interface
     *
     * @param event <code>DragGestureEvent</code> event
     *
     */
    public void dragGestureRecognized(DragGestureEvent event) {
        InputEvent ipe = event.getTriggerEvent();

        if (ipe.getModifiers() == InputEvent.BUTTON1_MASK) {
            Object selected = obsList.getSelectedValue();
            enableList(false);
            DeferredProgramList.clearSelection();
            setSelectedItem((SpItem) selected);

            if (selected != null) {
                SpItem deferredObs = QtTools.fixupDeferredObs(
                        getSelectedItem(), true);
                setObservationToDefer(deferredObs);

                StringSelection text =
                        new StringSelection(getSelectedItem().toString());

                // as the name suggests, starts the dragging
                dragSource.startDrag(event, DragSource.DefaultMoveNoDrop, text,
                        this);
            } else {
                logger.warn("nothing was selected to drag");
            }
        }
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     *
     * @param event <code>DragSourceDragEvent</code> event
     */
    public void dragEnter(DragSourceDragEvent event) {
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     *
     * @param evt <code>DragSourceDragEvent</code> event Change the cursor to
     *            indicate drop allowed
     */
    public void dragOver(DragSourceDragEvent evt) {
        evt.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     *
     * @param evt <code>DragSourceEvent</code> event
     */
    public void dragExit(DragSourceEvent evt) {
        evt.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     *
     * @param evt <code>DragSourceDragEvent</code> event
     */
    public void dropActionChanged(DragSourceDragEvent evt) {
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     *
     * @param evt <code>DragSourceDropEvent</code> event
     */
    public void dragDropEnd(DragSourceDropEvent evt) {
        if (evt.getDropSuccess()) {
            SpObs obs = (SpObs) obsList.getSelectedValue();

            if (obs != null) {
                if (obs.isOptional()) {
                    removeCurrentNode();
                    setSelectedItem(null);
                }
            }
        }

        enableList(true);
    }

    /**
     * Request whether we can shutdown the QT at this point.
     */
    public boolean shutDownRequest() {
        return true; // We can safely exit
    }

    public void enableList(boolean flag) {
        obsList.setEnabled(flag);
        repaint();
    }

    public JButton getRunButton() {
        return engButton;
    }
}
