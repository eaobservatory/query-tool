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
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * The static portion of the image source URL.
     */
    public static final String IMG_PREFIX = System.getProperty("imagePrefix");

    private static final Pattern pattern_date = Pattern.compile("<a href=/satellite/anim.cgi?.*imgtime=(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d).*>ON</a>");
    private static final Pattern pattern_image = Pattern.compile("<img src=/(satellite/[-_A-Za-z0-9./]+\\.png)>");

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
                String date = null;
                String image_url = null;

                String inputLine, html = "";
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(thisURL.openStream()));

                while ((inputLine = in.readLine()) != null) {
                    html = html + inputLine;
                }

                in.close();

                Matcher matcher = pattern_date.matcher(html);
                if (matcher.find()) {
                    date = matcher.group(1)
                        + "-" + matcher.group(2)
                        + "-" + matcher.group(3)
                        + " " + matcher.group(4)
                        + ":" + matcher.group(5)
                        + " UTC";
                }
                else {
                    throw new Exception("Could not extract date from satellite page");
                }

                matcher = pattern_image.matcher(html);
                if (matcher.find()) {
                    image_url = matcher.group(1);
                }
                else {
                    throw new Exception("Could not extract image URL from satellite page");
                }

                BufferedImage image = ImageIO.read(new URL(IMG_PREFIX + image_url));

                // The current MKWC shows a larger area than the original (Big Island) view,
                // so extract a subimage from it.
                double scale = 0.2;
                double width = image.getWidth();
                double height = image.getHeight();
                image = image.getSubimage(
                    (int) Math.round(width * (0.586 - (scale / 2))), (int) Math.round(height * (0.49 - (scale / 2))),
                    (int) Math.round(width * scale), (int) Math.round(height * scale));

                // Make sure we scale the image
                return new TimedImage(date, image.getScaledInstance(112, 90, Image.SCALE_SMOOTH));
            }

            protected void done() {
                try {
                    TimedImage result = get();
                    setIcon(new ImageIcon(result.image));
                    satBorder.setTitle(result.time);
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
