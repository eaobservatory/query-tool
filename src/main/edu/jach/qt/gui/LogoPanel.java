/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * LogoPanel.
 *
 * Created: Mon Apr 8 09:43:18 2002
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class LogoPanel extends JLabel implements Runnable, ActionListener {
    static int frameNumber = -1;
    int delay;
    Thread animatorThread;
    static boolean frozen = false;
    Timer timer;
    Vector<ImageIcon> images;

    /**
     * Constructor.
     *
     * Builds the QT logo and user interface.
     */
    public LogoPanel() {
        setHorizontalAlignment(SwingConstants.CENTER);

        images = new Vector<ImageIcon>();
        try {
            for (int i = 1; i <= 10; i++) {
                images.add(i - 1, new ImageIcon(ClassLoader.getSystemResource(
                        "QtLogo" + i + ".png")));
            }

            buildUI();

            setIcon(new ImageIcon(ClassLoader.getSystemResource(
                    "QtLogo.png")));
        } catch (Exception e) {
        }
    }

    // Note: Container must use BorderLayout, which is the default layout
    // manager for content panes.
    /**
     * Sets up animation of the QT interface.
     */
    void buildUI() {
        int fps = 40;

        // How many milliseconds between frames?
        delay = (fps > 0) ? (1000 / fps) : 100;

        // Set up a timer that calls this object's action handler
        timer = new javax.swing.Timer(delay, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
    }

    /**
     * Starts the QT logo animation sequence.
     */
    public void start() {
        startAnimation();
    }

    /**
     * Stops the QT logo animation sequence.
     */
    public void stop() {
        stopAnimation();

        try {
            setIcon(new ImageIcon(ClassLoader.getSystemResource(
                    "QtLogo.png")));
            frameNumber = -1;
        } catch (Exception e) {
        }
    }

    /**
     * Starts the QT logo animation sequence.
     */
    public synchronized void startAnimation() {
        if (frozen) {
            // Do nothing. The user has requested that we stop changing the
            // image.
        } else {
            // Start animating!
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }

    /**
     * Stops the QT logo animation sequence.
     */
    public synchronized void stopAnimation() {
        // Stop the animating thread.
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * Implementation of <code>ActionListener</code> interface.
     *
     * Stops the interface scrolling.
     *
     * @param e An <code>ActionEvent</code>.
     */
    public void actionPerformed(ActionEvent e) {
        frameNumber++;
        setIcon(images.elementAt(LogoPanel.frameNumber % 10));
    }

    /**
     * Implementation of the <code>Runnable</code> interface.
     */
    public void run() {
    }

    // Invoked only when this is run as an application.
    public static void main(String[] args) {
        JFrame f = new JFrame("ImageSequenceTimer");
        JButton b = new JButton("Start");
        final LogoPanel logoPanel = new LogoPanel();

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.add(logoPanel);
        f.setVisible(true);
        f.add(b, "South");

        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logoPanel.start();
            }
        });
    }
}
