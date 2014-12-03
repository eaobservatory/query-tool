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

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.URL;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import edu.jach.qt.utils.OMPTimer;
import edu.jach.qt.utils.OMPTimerListener;

/**
 * Associates an image with a label.
 *
 * In this case, the image is a satellite image derived from a configurable URL.
 *
 * Created: Mon Apr 8 10:18:45 2002
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class SatPanel extends JLabel implements OMPTimerListener {
    private TitledBorder satBorder;
    private static String currentWebPage;

    /**
     * Constructor.
     *
     * Sets the look and feel of the <code>JLabel</code> and
     * associates a timer with it to update the display.
     */
    public SatPanel() {
        setHorizontalAlignment(SwingConstants.CENTER);
        satBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 134, 206)),
                "Loading Sat...", TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION);
        satBorder.setTitleColor(new Color(51, 134, 206));
        setBorder(satBorder);

        if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
            currentWebPage = System.getProperty("satelliteWVPage");
        } else {
            currentWebPage = System.getProperty("satelliteIRPage");
        }

        refreshIcon();

        // Refresh every 10 minutes.
        OMPTimer.getOMPTimer().setTimer(600000, this);
    }

    // implementation of edu.jach.qt.gui.TimerListener interface

    /**
     * Implementation of edu.jach.qt.utils.OMPTimerListener interface
     */
    public void timeElapsed() {
        refreshIcon();
    }

    /**
     * Redraws the associated image.
     */
    public void refreshIcon() {
        URL url;
        try {
            url = new URL(currentWebPage);
        } catch (Exception mue) {
            System.out.println("Unable to convert to URL");
            url = null;
        }

        final URL thisURL = url;

        SwingWorker<TimedImage, Void> worker =
                new SwingWorker<TimedImage, Void>() {
            public TimedImage doInBackground() throws Exception {
                String imageSuffix = URLReader.getImageString(thisURL);

                // The timestamp is now at the end of the URL before the file
                // extension, separated by dots. It is 12 digits long (plus
                // one for the first dot.
                int last_dot = imageSuffix.lastIndexOf('.');
                int penu_dot = imageSuffix.lastIndexOf('.', last_dot - 1);

                String timeString = ((last_dot > 0) && (penu_dot > 0)
                                && (last_dot - penu_dot == 13))
                        ? imageSuffix.substring(penu_dot + 1, last_dot)
                        : "Unknown";

                return new TimedImage(timeString, ImageIO.read(new URL(
                        InfoPanel.IMG_PREFIX + imageSuffix)));
            }

            protected void done() {
                try {
                    TimedImage result = get();
                    // Make sure we scale the image
                    setIcon(new ImageIcon(result.image.getScaledInstance(112,
                            90, Image.SCALE_DEFAULT)));

                    satBorder.setTitle(result.time + " UTC");
                } catch (Exception e) {
                    System.err.println("Caught exception: " + e);
                }
            }
        };

        if (url != null) {
            worker.execute();
        }
    }

    /**
     * Method to set the currently displayed image.
     *
     * If the input string is "Water Vapour", a water vapour image is
     * displayed. Otherwise and infra-red image is displayed.
     *
     * @param image The type of satellite image to display.
     */
    public void setDisplay(String image) {
        if (image.equalsIgnoreCase("Water Vapour")) {
            currentWebPage = System.getProperty("satelliteWVPage");
        } else {
            currentWebPage = System.getProperty("satelliteIRPage");
        }

        refreshIcon();
    }

    private class TimedImage {
        public String time;
        public Image image;

        public TimedImage(String time, Image image) {
            this.time = time;
            this.image = image;
        }
    }
}

/**
 * Reads a URL.
 */
class URLReader {

    /**
     * Get the String associated with a URL.
     *
     * @param url The URL associated with the satellite image.
     * @return The name of the Image.
     * @exception Exception if unable to open the URL.
     */
    public static String getImageString(URL url) throws Exception {
        String imgString = "";
        String inputLine, html = "";
        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        while ((inputLine = in.readLine()) != null) {
            html = html + inputLine;
        }

        in.close();

        StringTokenizer st = new StringTokenizer(html);

        while (st.hasMoreTokens()) {
            String temp = st.nextToken();

            if (temp.startsWith("SRC")) {
                imgString = temp.substring(4, temp.indexOf('>'));
                break;
            }
        }

        return imgString;
    }
}
