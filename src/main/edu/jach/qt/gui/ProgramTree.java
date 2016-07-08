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
import orac.ukirt.inst.SpInstUIST;

/* QT imports */
import edu.jach.qt.utils.ErrorBox;
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
import javax.swing.SwingUtilities;

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
final public class ProgramTree extends JPanel implements ActionListener,
        KeyListener, DragSourceListener, DragGestureListener,
        DropTargetListener {
    static final JACLogger logger = JACLogger.getLogger(ProgramTree.class);
    public static final String BIN_IMAGE = System.getProperty("binImage");
    public static final String BIN_SEL_IMAGE = System.getProperty("binImage");
    private GridBagConstraints gbc;
    private static JButton engButton;
    private JButton xpand;
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
    private static final String editText = "Edit Attribute...";
    private static final String scaleText = "Scale Exposure Times...";
    private static String rescaleText = "Re-do Scale Exposure Times";
    private static final String engString = "Send for Engineering";
    private Vector<SpObs> haveScaled = new Vector<SpObs>();
    private Vector<Double> scaleFactors = new Vector<Double>();
    private JMenuItem edit;
    private JMenuItem scale;
    private JMenuItem scaleAgain;
    private JMenuItem sendToEng;
    private JPopupMenu scalePopup;
    private JPopupMenu msbDonePopup;
    private JMenuItem msbDoneMenuItem;
    private static final String msbDoneText = "Accept/Reject this MSB";
    private boolean uistIrpol = false;

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
        engButton.addActionListener(this);

        xpand = new JButton("Expand Observation");
        xpand.setMargin(new Insets(5, 10, 5, 10));
        xpand.addActionListener(this);

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
        edit = new JMenuItem(editText);
        edit.addActionListener(this);
        scalePopup.add(edit);
        scale = new JMenuItem(scaleText);
        scale.setEnabled(false);
        scale.addActionListener(this);
        scalePopup.add(scale);
        scaleAgain = new JMenuItem(rescaleText);
        scaleAgain.addActionListener(this);
        scaleAgain.setEnabled(false);
        scalePopup.add(scaleAgain);
        sendToEng = new JMenuItem(engString);
        sendToEng.addActionListener(this);
        scalePopup.add(sendToEng);

        msbDonePopup = new JPopupMenu();
        msbDoneMenuItem = new JMenuItem(msbDoneText);
        msbDoneMenuItem.addActionListener(this);
        msbDonePopup.add(msbDoneMenuItem);

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
     * Set the Trash Can image.
     *
     * @param label The <code>JLabel</code> with which to associate the image.
     * @exception Exception is unabe to set the image.
     */
    public void setImage(JLabel label) throws Exception {
        URL url = new URL("file://" + BIN_IMAGE);

        if (url != null) {
            label.setIcon(new ImageIcon(url));
        } else {
            label.setIcon(new ImageIcon(
                    ProgramTree.class.getResource("file://" + BIN_IMAGE)));
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

    /**
     * Public method to react to button actions.
     *
     * The reaction is mainly about to start a SGML translation,
     * then a "remote" frame to form a sequence console.
     *
     * @param ActionEvent
     * @return none
     * @throws none
     *
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == xpand) {
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

            itemToXpand = temp;

            if (tv == null) {
                tv = new TreeViewer(itemToXpand);
            } else {
                tv.update(itemToXpand);
            }
        } else if (source == engButton) {
            doExecute(true);
        }

        if (source instanceof JMenuItem) {
            JMenuItem thisItem = (JMenuItem) source;

            if (thisItem.getText().equals(editText)) {
                editAttributes();
            } else if (thisItem.getText().equals(scaleText)) {
                scaleAttributes();
            } else if (thisItem.getText().equals(rescaleText)) {
                rescaleAttributes();
            } else if (thisItem.getText().equals(engString)) {
                doExecute(false);
            }
        }
    }

    public void doExecute(boolean useQueue) {
        SpItem item = null;
        boolean isDeferred = false;
        boolean failed = false;

        Thread t = null;

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

        if (System.getProperty("telescope").equalsIgnoreCase("ukirt")) {
            SpInstObsComp instrumentContext = getContext(item);

            if (instrumentContext instanceof SpInstUIST) {
                SpInstUIST uist = (SpInstUIST) instrumentContext;

                if (uist.isPolarimetry() && !uistIrpol) {
                    int result = JOptionPane.showConfirmDialog(
                            null,
                            "You are attempting to do a Polarimetry"
                                    + " observation with UIST"
                                    + " - is the IRPOL arm in the beam ?",
                            "Is IRPOL arm in beam ?",
                            JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.NO_OPTION) {
                        JOptionPane.showMessageDialog(null,
                                "Queue submission cancelled.",
                                "Queue submission cancelled.",
                                JOptionPane.INFORMATION_MESSAGE);
                        setExecutable(true);
                        return;
                    }

                    uistIrpol = true;

                } else {
                    uistIrpol = false;
                }
            } else {
                uistIrpol = false;
            }

            try {
                ExecuteUKIRT execute = new ExecuteUKIRT(
                    item, isDeferred, useQueue);

                File failFile = execute.failFile();
                File successFile = execute.successFile();

                t = new Thread(execute);
                t.start();
                t.join();

                if (failFile.exists()) {
                    failed = true;

                    if (failFile.length() > 0) {
                        StringBuffer error = new StringBuffer();

                        try {
                            // Read the information from the filure file
                            BufferedReader rdr = new BufferedReader(
                                    new FileReader(failFile));
                            String line;

                            while ((line = rdr.readLine()) != null) {
                                error.append(line);
                                error.append('\n');
                            }

                            new ErrorBox(this,
                                    "Failed to Execute. Error was \n"
                                            + error.toString());
                        } catch (IOException ioe) {
                            // If we failed, output a default error message and
                            // reset the error buffer
                            new ErrorBox(this,
                                    "Failed to Execute. Check log using View>Log menu button.");
                        }
                    } else {
                        new ErrorBox(this,
                                "Failed to Execute. Check log using View>Log menu button.");
                    }
                }

                if (! useQueue) {
                    if (!isDeferred && !failed) {
                        markAsDone(obsList.getSelectedIndex());
                    } else if (!failed) {
                        DeferredProgramList.markThisObservationAsDone(item);
                    }
                }

                // done with status files
                if (failFile.exists()) {
                    failFile.delete();
                }

                if (successFile.exists()) {
                    successFile.delete();
                }

            } catch (Exception e) {
                logger.error("Failed to execute", e);

                if (t != null && t.isAlive()) {
                    logger.info("Last execution still running");
                }
            }

            setExecutable(true);

            if (!isDeferred && !failed) {
                obsList.setListData(new Vector());
                obsList.clearSelection();
            }
        } else if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
            try {
                ExecuteInThread ein;

                ein = new ExecuteInThread(item, isDeferred);

                ein.start();

            } catch (Exception e) {
                logger.error("Error running task", e);
                setExecutable(true);
            }
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
     * Implementation opf <code>KeyListener</code> interface.
     *
     * If the delete key is pressed, removes the currently selected item.
     */
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_DELETE)) {
            removeCurrentNode();
        }
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     */
    public void keyTyped(KeyEvent e) {
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
                    rescaleText = "Re-do Scale Exposure Times (x" + sf + ")";
                    scaleAgain.setText(rescaleText);
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

    public class ExecuteInThread extends Thread {
        private SpItem _item;
        private boolean _isDeferred;

        public ExecuteInThread(SpItem item, boolean deferred) {
            _item = item;
            _isDeferred = deferred;
        }

        public void run() {
            ExecuteJCMT execute;
            boolean failed = false;

            execute = new ExecuteJCMT(_item, _isDeferred);

            failed = execute.run();

            if (failed) {
                logger.info("Execution failed - Check log messages");

                new PopUp("JCMT Execution Failed",
                        "Failed to send project for execution;"
                                + " check log entries using"
                                + " the View>Log button",
                        JOptionPane.ERROR_MESSAGE);
            }

            if (!failed) {
                if (!_isDeferred) {
                    model.clear();
                    setCurrentItem(null);
                    setSelectedItem(null);

                } else {
                    DeferredProgramList.markThisObservationAsDone(
                            _item);
                }
            }

            setExecutable(true);

            if (!_isDeferred && !failed) {
                obsList.setListData(new Vector());
                obsList.clearSelection();
            }

            logger.debug("Enabling run button since the ExecuteJCMT task has"
                    + " completed");
        }

        public class PopUp {
            public PopUp(final String title, final String message,
                    final int errorLevel) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(null, message, title,
                                errorLevel);
                    }
                });
            }
        }
    }
}
