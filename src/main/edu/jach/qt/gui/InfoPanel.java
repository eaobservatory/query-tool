/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
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

/* Standard imports */
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.net.URL;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;

/* OT imports */
import gemini.util.JACLogger;
import gemini.util.ObservingToolUtilities;

/* QT imports */
import edu.jach.qt.app.Querytool;
import edu.jach.qt.utils.SpQueuedMap;
import edu.jach.qt.utils.OMPTimer;
import edu.jach.qt.utils.OMPTimerListener;

/**
 * InfoPanel.
 *
 * Created: Tue Apr 24 16:28:12 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class InfoPanel extends JPanel implements ActionListener,
        OMPTimerListener {
    private static final JACLogger logger =
            JACLogger.getLogger(InfoPanel.class);

    /**
     * Specifies the String location for the QT logo image.
     */
    public static final String LOGO_IMAGE = System.getProperty("qtLogo");

    /**
     * Specifies the webpage containing the String of the latest image to show.
     */
    public static final String SAT_WEBPAGE =
            System.getProperty("satellitePage");

    /**
     * The static portion of the image source URL.
     */
    public static final String IMG_PREFIX = System.getProperty("imagePrefix");

    /**
     * The button clicked to start a query.
     */
    public static JButton searchButton = new JButton();

    /**
     * A reference to the easily get the LogoPanel.
     */
    public static LogoPanel logoPanel = new LogoPanel();
    private TelescopeDataPanel telescopeInfoPanel;
    private MSBQueryTableModel msb_qtm;
    private TimePanel timePanel;
    private SatPanel satPanel;
    private Querytool localQuerytool;
    private QtFrame qtf;
    private JButton exitButton;
    private JButton fetchMSB;
    private TimerTask queryTask;
    final private InfoPanel infoPanel;
    final private Cursor busyCursor = new Cursor(Cursor.WAIT_CURSOR);
    final private Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * Creates a new <code>InfoPanel</code> instance.
     *
     * @param msbQTM a <code>MSBQueryTableModel</code> value
     * @param qt a <code>Querytool</code> value
     * @param qtFrame a <code>QtFrame</code> value
     */
    public InfoPanel(MSBQueryTableModel msbQTM, Querytool qt, QtFrame qtFrame) {
        super();
        localQuerytool = qt;
        msb_qtm = msbQTM;
        qtf = qtFrame;

        infoPanel = this;

        MatteBorder matte = new MatteBorder(3, 3, 3, 3, Color.green);
        GridBagLayout gbl = new GridBagLayout();

        setBackground(Color.black);
        setBorder(matte);
        setLayout(gbl);
        setMinimumSize(new Dimension(174, 450));
        setPreferredSize(new Dimension(174, 450));
        setMaximumSize(new Dimension(174, 450));

        compInit();
    }

    private void compInit() {
        final GridBagConstraints gbc = new GridBagConstraints();

        exitButton = new JButton();
        fetchMSB = new JButton();
        timePanel = new TimePanel();
        satPanel = new SatPanel();
        telescopeInfoPanel = new TelescopeDataPanel(this);

        /* Setup the SEARCH button */
        InfoPanel.searchButton.setText("Search");
        InfoPanel.searchButton.setName("Search");
        final URL url = ObservingToolUtilities.resourceURL("green_light1.gif");
        final ImageIcon icon = new ImageIcon(url);
        InfoPanel.searchButton.setIcon(icon);
        blinkIcon();
        InfoPanel.searchButton.setHorizontalTextPosition(
                SwingConstants.LEADING);
        InfoPanel.searchButton.setToolTipText(
                "Red icon - all constraints disabled");
        InfoPanel.searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchButton.setEnabled(false);
                qtf.setCursor(busyCursor);
                qtf.getWidgets().setButtons();
                qtf.updateColumnSizes();
                qtf.repaint(0);

                ChecksumCacheThread checksumCacheThread =
                        new ChecksumCacheThread();
                checksumCacheThread.start();

                final SwingWorker<Boolean, Void> worker =
                        new SwingWorker<Boolean, Void>() {
                    public Boolean doInBackground() {
                        Boolean isStatusOK = new Boolean(
                                localQuerytool.queryMSB());
                        return isStatusOK;
                    }

                    // Runs on the event-dispatching thread.
                    protected void done() {
                        logoPanel.stop();
                        qtf.setCursor(normalCursor);
                        searchButton.setEnabled(true);

                        boolean isStatusOK = false;
                        try {
                            isStatusOK = get();
                        } catch (Exception e) {
                        }

                        if (isStatusOK) {
                            Thread tableFill = new Thread(msb_qtm);
                            tableFill.start();

                            try {
                                tableFill.join();
                            } catch (InterruptedException iex) {
                                logger.warn("Problem joining tablefill thread");
                            }

                            synchronized (this) {
                                Thread projectFill = new Thread(
                                        qtf.getProjectModel());
                                projectFill.start();
                                try {
                                    projectFill.join();
                                } catch (InterruptedException iex) {
                                    logger.warn("Problem joining projectfill thread");
                                }

                                qtf.initProjectTable();
                            }

                            msb_qtm.setProjectId("All");
                            qtf.setColumnSizes();
                            qtf.resetScrollBars();
                            logoPanel.stop();
                            qtf.setCursor(normalCursor);

                            if (queryTask != null) {
                                queryTask.cancel();
                            }

                            qtf.setQueryExpired(false);
                            String queryTimeout = System.getProperty(
                                    "queryTimeout");
                            System.out.println("Query expiration: "
                                    + queryTimeout);
                            Integer timeout = new Integer(queryTimeout);
                            if (timeout != 0) {
                                // Conversion from minutes of milliseconds
                                int delay = timeout * 60 * 1000;
                                queryTask = OMPTimer.getOMPTimer().setTimer(
                                        delay, infoPanel, false);
                            }
                        }
                    }
                };

                logger.info("Query Sent");

                localQuerytool.printXML();
                logoPanel.start();
                worker.execute();
            }
        });

        InfoPanel.searchButton.setBackground(java.awt.Color.gray);

        fetchMSB.setText("Fetch MSB");
        fetchMSB.setName("Fetch MSB");
        fetchMSB.setBackground(java.awt.Color.gray);
        fetchMSB.addActionListener(this);

        /* Setup the EXIT button */
        exitButton.setText("Exit");
        exitButton.setName("Exit");
        exitButton.setBackground(java.awt.Color.gray);
        exitButton.addActionListener(this);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 0.0;
        add(logoPanel, gbc, 0, 0, 1, 1);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(2, 15, 2, 15);
        gbc.weighty = 0.0;

        /* Add all the buttons */
        add(InfoPanel.searchButton, gbc, 0, 5, 1, 1);
        add(fetchMSB, gbc, 0, 10, 1, 1);
        add(exitButton, gbc, 0, 15, 1, 1);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 100;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.weighty = 0;
        add(telescopeInfoPanel, gbc, 0, 20, 1, 1);

        add(satPanel, gbc, 0, 30, 1, 1);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        gbc.weighty = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        add(timePanel, gbc, 0, 40, 1, 1);
    }

    /**
     * Get the parent frame.
     *
     * @return The parent QT Frame object.
     */
    public QtFrame getFrame() {
        return qtf;
    }

    /**
     * Get the current query.
     *
     * @return The current <code>QueryTool</code> object.
     */
    public Querytool getQuery() {
        return localQuerytool;
    }

    private void add(Component c, GridBagConstraints gbc, int x, int y, int w,
            int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        add(c, gbc);
    }

    /**
     * Get the String that contains the current XML defining the query.
     *
     * @return a <code>String</code> representing the query.
     */
    public String getXMLquery() {
        return localQuerytool.getXML();
    }

    /**
     * Get the <code>TelescopeDataPanel</code>
     *
     * @return the current telescope panel.
     */
    public TelescopeDataPanel getTelescopeDataPanel() {
        return telescopeInfoPanel;
    }

    /**
     * Get the <code>SatPanel</code>
     *
     * @return the current satellite panel.
     */
    public SatPanel getSatPanel() {
        return satPanel;
    }

    /**
     * Satisfies the ActionListener interface.
     *
     * This is called when any ActionEvents are triggered by registered
     * ActionListeners. In this case it's either the exit button or the
     * fetchMSB button.
     *
     * @param e an <code>ActionEvent</code> value
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == exitButton) {
            if (TelescopeDataPanel.DRAMA_ENABLED) {
                telescopeInfoPanel.closeHub();
            }

            qtf.exitQT();

        } else if (source == fetchMSB) {
            qtf.sendToStagingArea();
        }
    }

    public void timeElapsed() {
        System.out.println("Query has expired");
        qtf.setQueryExpired(true);

        if (queryTask != null) {
            queryTask.cancel();
        }
    }

    private void blinkIcon() {
        javax.swing.Timer t = new javax.swing.Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImageIcon imageIcon =
                        (ImageIcon) InfoPanel.searchButton.getIcon();
                if (imageIcon == null) {
                    return;
                }

                String iconName = imageIcon.toString();
                if (iconName == null || iconName.indexOf("green_light") != -1) {
                    return;
                } else {
                    if (iconName.indexOf("_light1") != -1) {
                        // Set light to light 2
                        try {
                            java.net.URL url = new java.net.URL(
                                    iconName.replaceAll("light1", "light2"));
                            ImageIcon icon = new ImageIcon(url);
                            InfoPanel.searchButton.setIcon(icon);
                        } catch (Exception x) {
                            // Ignore
                        }
                    } else if (iconName.indexOf("_light2") != -1) {
                        // Set light to light 1
                        try {
                            java.net.URL url = new java.net.URL(
                                    iconName.replaceAll("light2", "light1"));
                            ImageIcon icon = new ImageIcon(url);
                            InfoPanel.searchButton.setIcon(icon);
                        } catch (Exception x) {
                            // Ignore
                        }
                    } else {
                        // Do nothing
                    }
                }
            }
        });

        t.start();
    }

    public class ChecksumCacheThread extends Thread {
        public void run() {
            SpQueuedMap map = SpQueuedMap.getSpQueuedMap();
            map.fillCache();
        }
    }
}

