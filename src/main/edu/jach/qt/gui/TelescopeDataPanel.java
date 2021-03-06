/*
 * Copyright (C) 2001-2014 Science and Technology Facilities Council.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ListIterator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import ocs.utils.HubImplementor;
import ocs.utils.DcHub;
import ocs.utils.ObeyNotRegisteredException;
import gemini.util.JACLogger;
import edu.jach.qt.utils.SimpleMoon;
import javax.swing.JOptionPane;

/**
 * A Data Panel on the QT.
 *
 * All information is displayed on this panel, and can be used for searching,
 * fetching and setting query parameters.
 *
 * Created: Tue Sep 25 18:02:01 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class TelescopeDataPanel extends JPanel implements ActionListener {
    static final JACLogger logger =
            JACLogger.getLogger(TelescopeDataPanel.class);
    public static boolean DRAMA_ENABLED = "true".equals(
            System.getProperty("DRAMA_ENABLED"));
    public static String tauString = "-----";
    private static JLabel csoTauValue;
    private static JLabel airmassValue;
    private JLabel csoTau;
    private JLabel seeing;
    private JLabel seeingValue;
    private JLabel airmass;
    private JButton updateButton;
    private InfoPanel infoPanel;
    private DcHub hub;
    private HubImplementor csomonHI, closeHI, enviroHI;
    private static String lastCSOValue = "";
    private static boolean acceptUpdates = true;
    private static boolean haveWvmReading = false;
    private static boolean haveCsoReading = false;
    private static boolean haveWvmToolTip = false;
    private static boolean haveCsoToolTip = false;
    private final static boolean preferWvm = true;

    /**
     * Constructor.
     *
     * This constructor does the following tasks:
     * <ul>
     * <li>Checks to see if the QT is locked and if so converts to scenario
     * mode.
     * <li>Gets handles for the DRAMA monitired tasks.
     * </ul>
     *
     * @param panel the <code>InfoPanel</code> Object contructing this panel.
     */
    public TelescopeDataPanel(InfoPanel panel) {
        csoTau = new JLabel("225 GHz Tau: ", JLabel.LEADING);
        seeing = new JLabel("Seeing: ", JLabel.LEADING);
        airmass = new JLabel("Airmass: ", JLabel.LEADING);
        csoTauValue = new JLabel(tauString, JLabel.LEADING);
        seeingValue = new JLabel(tauString, JLabel.LEADING);
        airmassValue = new JLabel(tauString, JLabel.LEADING);
        updateButton = new JButton("Set Default");
        this.infoPanel = panel;

        if (TelescopeDataPanel.DRAMA_ENABLED) {
            hub = DcHub.getHandle();
            hub.setParentComponent(this); // Use for ErrorBox dialogs in the
                                          // hub.

            // CSOMON
            csomonHI = new HubImplementor("CSOMON", "localhost");
            csomonHI.setCallBack("edu.jach.qt.djava.CSOPathResponseHandler");
            csomonHI.setBuffers(900, 5, 1800, 12);

            hub.register(csomonHI);

            // ENVIRO
            if ("true".equals(System.getProperty("connectToEnviro", "false"))) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                enviroHI = new HubImplementor("ENVIRO", "jcmt-aux");
                enviroHI.setCallBack("edu.jach.qt.djava.WVMPathResponseHandler");
                enviroHI.setBuffers(900, 5, 1800, 12);

                hub.register(enviroHI);
            }

            // Closing
            closeHI = new HubImplementor("CloseDcHub");
        }
        config();
    }

    /**
     * Set the Tau value on the appropriate <code>JLabel</code>
     *
     * This method receives new values from the drama system.
     *
     * @param val the value to set as a decimal number.
     */
    public static void setCsoTau(double val) {
        if ((!preferWvm) || (!haveWvmReading)) {
            haveCsoReading = true;
            setTau(val);
            if (!haveCsoToolTip) {
                setTauTooltip("Changed to CSO tau");
                haveCsoToolTip = true;
                haveWvmToolTip = false;
            }
        }
    }

    /**
     * Actually set the tau display, based on a value either from CSO or the
     * JCMT WVM.
     *
     * Calling setTextField() ends up calling JTextField.setText() which is
     * thread-safe according to the documentation.
     */
    private static void setTau(double val) {
        // The arg in the following constructor is a pattern for formating
        DecimalFormat myFormatter = new DecimalFormat("0.000");
        String output = myFormatter.format(val);

        if (acceptUpdates && !output.equals(tauString)) {
            if (lastCSOValue.equals("")) {
                lastCSOValue = output;
                if (WidgetPanel.getAtmospherePanel() != null) {
                    WidgetPanel.getAtmospherePanel().setTextField("tau:",
                            output);
                }
            } else if (WidgetPanel.getAtmospherePanel() != null
                    && lastCSOValue.equals(WidgetPanel.getAtmospherePanel()
                            .getText("tau:"))) {
                lastCSOValue = output;
                WidgetPanel.getAtmospherePanel().setTextField("tau:", output);
            } else if (WidgetPanel.getAtmospherePanel() != null
                    && !(lastCSOValue.equals(WidgetPanel.getAtmospherePanel()
                            .getText("tau:")))) {
                acceptUpdates = false;
            }
        }

        csoTauValue.setText(output);
    }

    /**
     * Set the Tau to a value from the JCMT WVM.
     *
     * This method receives new values from the drama system.
     */
    public static void setWvmTau(double val) {
        if (preferWvm || (!haveCsoReading)) {
            haveWvmReading = true;
            setTau(val);

            if (!haveWvmToolTip) {
                setTauTooltip("Changed to JCMT WVM");
                haveWvmToolTip = true;
                haveCsoToolTip = false;
            }
        }
    }

    /**
     * Set the time at which the tau was received from the JCMT WVM.
     *
     * This method receives new values from the drama system.
     */
    public static void setWvmTauTime(String time) {
        if (preferWvm || (!haveCsoReading)) {
            if (time == null || time.equals("")) {
                setTauTooltip("JCMT WVM, time = unknown");
            } else {
                setTauTooltip("JCMT WVM, time = " + time);
            }

            haveWvmToolTip = true;
            haveCsoToolTip = false;
        }
    }

    /**
     * Set the Airmass value on the appropriate <code>JLabel</code>
     *
     * @param val the value to set as a decimal number.
     */
    public static void setAirmass(double val) {
        // The arg in the following constructor is a pattern for formating
        DecimalFormat myFormatter = new DecimalFormat("0.000");
        String output = myFormatter.format(val);

        TelescopeDataPanel.airmassValue.setText("" + output);
    }

    /**
     * Set the source of the 'CSO' tau reading.
     *
     * This method receives new values from the drama system.
     */
    public static void setCsoTauSource(String source) {
        if ((!preferWvm) || (!haveWvmReading)) {
            if (source == null || source.equals("")) {
                setTauTooltip("Source = unknown");
            } else {
                setTauTooltip("Source = " + source);
            }

            haveCsoToolTip = true;
            haveWvmToolTip = false;
        }
    }

    /**
     * Actually set the tau tooltip, to show either the 'CSO' tau source or the
     * latest WVM reading time.
     */
    private static void setTauTooltip(final String tip) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                csoTauValue.setToolTipText(tip);
            }
        });
    }

    /**
     * Builds the components of the interface.
     *
     * Adds buttons, labels and other panels.
     */
    public void config() {
        setBackground(Color.black);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 134, 206)),
                "Current Info", TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION);
        border.setTitleColor(new Color(51, 134, 206));
        setBorder(border);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        csoTau.setForeground(java.awt.Color.white);
        seeing.setForeground(java.awt.Color.white);
        airmass.setForeground(java.awt.Color.white);
        csoTauValue.setForeground(java.awt.Color.green);
        seeingValue.setForeground(java.awt.Color.green);
        airmassValue.setForeground(java.awt.Color.green);

        updateButton.setBackground(java.awt.Color.gray);
        updateButton.addActionListener(this);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.weightx = 0;
        gbc.weighty = 0;

        add(csoTau, gbc, 0, 0, 1, 1);
        add(csoTauValue, gbc, 1, 0, 1, 1);

        add(seeing, gbc, 0, 1, 1, 1);
        add(seeingValue, gbc, 1, 1, 1, 1);

        add(airmass, gbc, 0, 2, 1, 1);
        add(airmassValue, gbc, 1, 2, 1, 1);

        add(updateButton, gbc, 0, 3, 2, 1);
    }

    /**
     * Returns the current DRAMA hub.
     *
     * @return The current DRAMA hub created by the constructor.
     */
    public void closeHub() {
        try {
            Thread hubThread = hub.getThread();

            hub.closeDcHub(closeHI);
            hubThread.join();
        } catch (ObeyNotRegisteredException onr) {
        } catch (InterruptedException ie) {
        }
    }

    /**
     * Gets the tau value currently displayed on this panel.
     *
     * @return The current CSO tau value.
     */
    public static String getCSO() {
        return csoTauValue.getText();
    }

    /**
     * Describe <code>add</code> method here.
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
     * Implementation of java.awt.event.ActionListener interface.
     *
     * This will update the following fields:
     * <ul>
     * <li>The Tau value
     * <li>The airmass value
     * <li>The Date and Time fields
     * <li>The information about the moon.
     * </ul>
     *
     * @param param1 ActionEvent created by the "Set Current" button.
     */
    public void actionPerformed(ActionEvent param1) {
        logger.debug("New tau: " + csoTauValue.getText());

        // Ignore case where the tau value is set to the default
        if (!csoTauValue.getText().equals(tauString)) {
            acceptUpdates = true;
            lastCSOValue = "";
            WidgetPanel.getAtmospherePanel().setTextField("tau:",
                    TelescopeDataPanel.csoTauValue.getText());
        }

        SimpleMoon moon = SimpleMoon.getInstance();
        moon.reset();
        boolean dark = false;
        boolean grey = false;
        boolean bright = false;

        if (moon.isUp() == false) {
            dark = true;
        } else if (moon.getIllumination() < 0.25) {
            grey = true;
        } else {
            bright = true;
        }

        infoPanel.getFrame().getWidgets().setMoonUpdatable(true);
        RadioPanel moonPanel = WidgetPanel.getMoonPanel();
        if (moonPanel != null) {
            ListIterator<JRadioButton> iter =
                    moonPanel.radioElems.listIterator(0);

            while (iter.hasNext()) {
                JToggleButton abstractButton = iter.next();

                if (abstractButton.getText().equalsIgnoreCase("dark")
                        && dark == true) {
                    abstractButton.setSelected(true);
                } else if (abstractButton.getText().equalsIgnoreCase("Grey")
                        && grey == true) {
                    abstractButton.setSelected(true);
                } else if (abstractButton.getText().equalsIgnoreCase("Bright")
                        && bright == true) {
                    abstractButton.setSelected(true);
                }
            }
        }

        infoPanel.getFrame().setMenuDefault();

        WidgetPanel widgetPanel = infoPanel.getFrame().getWidgets();
        Component[] components = widgetPanel.getComponents();

        for (int i = 0; i < widgetPanel.getComponentCount(); i++) {
            if (components[i] instanceof LabeledTextField) {
                ((LabeledTextField) components[i]).setText("");

            } else if (components[i] instanceof LabeledRangeTextField) {
                LabeledRangeTextField lrtf = (LabeledRangeTextField) components[i];

                if (components[i].getName().equalsIgnoreCase("airmass")) {
                    if (!TelescopeDataPanel.airmassValue.getText().equals(
                            tauString)) {
                        String zCurrentAirmass =
                                TelescopeDataPanel.airmassValue.getText();
                        Double currentAirmass;
                        try {
                            currentAirmass = new Double(zCurrentAirmass);
                            double upperLimit = currentAirmass;
                            upperLimit = upperLimit - 20.0 * upperLimit / 100.0;
                            if (upperLimit < 1.0) {
                                upperLimit = 1.0;
                            }

                            double lowerLimit = currentAirmass;
                            lowerLimit = lowerLimit + 20.0 * lowerLimit / 100.0;
                            if (lowerLimit > 3.0) {
                                lowerLimit = 3.0;
                            }

                            lrtf.setLowerText(new Double(upperLimit));
                            lrtf.setUpperText(new Double(lowerLimit));
                        } catch (NumberFormatException nfe) {
                        }
                    } else {
                        lrtf.setLowerText("");
                        lrtf.setUpperText("");
                    }
                } else if (components[i].getName().equalsIgnoreCase(
                        "observation")) {
                    lrtf.restartTimer();

                    // Wait for the timer to restart
                    while (!(lrtf.timerRunning())) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            // break out of the loop to stop any serious
                            // problems
                            logger.debug("Interrupted wating for timer!", ie);
                            break;
                        }
                    }

                    // Wait a bit more to make sure things have settled down
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        logger.debug("Interrupted wating for timer!", ie);
                    }

                    infoPanel.getFrame().resetCurrentMSB();

                } else {
                    LabeledRangeTextField temp =
                            (LabeledRangeTextField) components[i];
                    temp.setLowerText("");
                    temp.setUpperText("");
                }
            }
        }

        int runSearch = JOptionPane.showConfirmDialog(this,
                "Perform fresh search with defaults ?",
                "Perform fresh search with defaults ?",
                JOptionPane.YES_NO_OPTION);

        if (runSearch == 0) {
            InfoPanel.searchButton.doClick();
        }
    }
}
