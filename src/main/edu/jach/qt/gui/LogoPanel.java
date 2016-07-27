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
public class LogoPanel extends JLabel implements ActionListener {
    private int frameNumber = -1;
    private Timer timer;
    /**
     * Icon to use when not performing animation.
     */
    private ImageIcon image_still;
    private Vector<ImageIcon> images;

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

            int fps = 40;

            // How many milliseconds between frames?
            int delay = (fps > 0) ? (1000 / fps) : 100;

            // Set up a timer that calls this object's action handler
            timer = new javax.swing.Timer(delay, this);
            timer.setInitialDelay(0);
            timer.setCoalesce(true);

            image_still = new ImageIcon(
                ClassLoader.getSystemResource("QtLogo.png"));
            setIcon(image_still);
        } catch (Exception e) {
        }
    }

    /**
     * Starts the QT logo animation sequence.
     */
    public synchronized void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    /**
     * Stops the QT logo animation sequence.
     *
     * Should only be run from the event dispatching thread since it sets
     * the icon to the still image.
     */
    public synchronized void stop() {
        if (timer.isRunning()) {
            timer.stop();
        }

        try {
            setIcon(image_still);
            frameNumber = -1;
        } catch (Exception e) {
        }
    }

    /**
     * Implementation of <code>ActionListener</code> interface.
     *
     * Shows the next image in sequence.
     *
     * @param e An <code>ActionEvent</code>.
     */
    public void actionPerformed(ActionEvent e) {
        frameNumber++;
        setIcon(images.elementAt(frameNumber % 10));
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
