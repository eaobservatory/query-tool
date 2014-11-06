/*
 * Copyright (C) 2001-2013 Science and Technology Facilities Council.
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

package edu.jach.qt.app;

/* JSKY imports */

/* ORAC imports */
/* QT imports */
import edu.jach.qt.gui.QtFrame;
import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.MsbClient;
import edu.jach.qt.utils.QtTools;
import edu.jach.qt.utils.Splash;

/* Standard imports */

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.AWTError;
import javax.swing.SwingUtilities;
import jsky.app.ot.OtCfg;
import gemini.util.JACLogger;

/**
 * This is the top most OMP-QT class. Upon init it instantiates the Querytool
 * and QtFrame classes, in that order. These two classes define the structure of
 * the OMP-QT design. There has been defined a partition between the Graphical
 * User Interface (GUI) and the logic behind the application. Hence, the
 * directory structure shows a 'qt/gui' and a 'qt/app'. There also is an
 * 'qt/utils' directory which is a repository of utility classes needed for both
 * 'app' and 'gui' specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
final public class QT implements Runnable {
    static JACLogger logger = JACLogger.getLogger(QT.class);

    /**
     * Creates a new <code>QT</code> instance which starts a Querytool, the app
     * itself, and a QtFrame, the user interface. The frame is also set be
     * centered on the screen.
     */
    public QT() {
        logger.info("-------WELCOME TO THE QT----------");

        QtTools.loadConfig(System.getProperty("qtConfig"));

        // Fetch the MSB column info so that the MsbClient class
        // has a cached copy ready to use later when constructing
        // the GUI in the Swing thread.
        Splash splash = new Splash("Waiting for database ...");
        try {
            MsbClient.getColumnInfo();
        } catch (Exception e) {
            logger.error("Unable to fetch MSB column information", e);
            e.printStackTrace();
            System.exit(1);
        }
        splash.done();

        // Since the whole process of launching the QT includes
        // usage of Swing, and since Swing is not thread-safe,
        // we must have this action performed in the Swing
        // event dispatch thread.
        SwingUtilities.invokeLater(this);
    }

    /**
     * Thread run method to construct the QT GUI.
     */
    public void run() {
        try {
            OtCfg.init();
        } catch (Exception e) {
            logger.fatal("PreTranslator error starting the QT", e);
            System.exit(1);
        } catch (ClassCircularityError cce) {
            logger.fatal(
                    "Talk to SHAUN!!!: PreTranslator ClassCircularityError starting the QT",
                    cce);
            System.exit(1);
        }

        WidgetDataBag wdb = new WidgetDataBag();
        Querytool qt = new Querytool(wdb);
        QtFrame qtf = new QtFrame(wdb, qt);

        qtf.setSize(1150, 620);
        qtf.setTitle("OMP Query Tool Observation Manager");

        Dimension screenSize;
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            screenSize = tk.getScreenSize();
        } catch (AWTError awe) {
            screenSize = new Dimension(640, 480);
        }
        Dimension frameSize = qtf.getSize();

        /* Fill screen if the screen is smaller that qtfSize. */
        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        /* Center the screen */
        int x = screenSize.width / 2 - frameSize.width / 2;
        int y = screenSize.height / 2 - frameSize.height / 2;
        qtf.setLocation(x, y);
        qtf.validate();
        qtf.setVisible(true);

        logger.info("QT should now be visible");

        // Set the top panel's preferred size to its
        // current size to prevent it expanding if
        // an excessive amount of text is inserted
        // into the notes text pane.
        //
        // This is intended to fix fault: 20060807.007
        qtf.topPanel.setPreferredSize(qtf.topPanel.getSize());
        logger.info("Top panel size fixed");

        qtf.fetchCalibrations();

        String bigTelescope = System.getProperty("TELESCOPE");
        if (bigTelescope == null || bigTelescope.equals(""))
            System.setProperty("TELESCOPE", System.getProperty("telescope"));
    }

    /**
     * Currently we take no args at startup. Just get the LookAndFeel from the
     * UIManager and start the Main QT class.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args) {
        try {
            new QT();
        } catch (RuntimeException rte) {
            logger.fatal("Caught a run-time exception from main", rte);
        } catch (Exception e) {
            logger.fatal("Caught an unexpected exception in main", e);
        }
    }
}
