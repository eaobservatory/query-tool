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

import java.awt.GridLayout;
import java.awt.Color;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.text.SimpleDateFormat;

import edu.jach.qt.utils.LocalSiderealTime;

import edu.jach.qt.utils.OMPTimer;
import edu.jach.qt.utils.OMPTimerListener;

/**
 * Class associating local time with a label for display on an interface.
 *
 * Created: Fri Apr 20 14:55:26 2001
 *
 * @author <a href="mailto:">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class TimePanel extends JPanel implements OMPTimerListener {
    JLabel local;
    JLabel universal;
    SimpleDateFormat localDateFormatter;
    SimpleDateFormat universalDateFormatter;
    LocalSiderealTime localSiderealTime;
    JLabel lst;

    /**
     * Constructor.
     *
     * Sets a timer running and adds a listener.
     */
    public TimePanel() {
        setBackground(Color.black);

        local = new JLabel();
        universal = new JLabel();
        lst = new JLabel();

        local.setHorizontalAlignment(SwingConstants.CENTER);
        universal.setHorizontalAlignment(SwingConstants.CENTER);
        lst.setHorizontalAlignment(SwingConstants.CENTER);

        local.setBackground(Color.black);
        universal.setBackground(Color.black);
        lst.setBackground(Color.black);
        local.setForeground(Color.green);
        universal.setForeground(Color.green);
        lst.setForeground(Color.green);

        local.setOpaque(true);
        universal.setOpaque(true);
        lst.setOpaque(true);

        setLayout(new GridLayout(3, 1));

        add(local);
        add(universal);
        add(lst);

        localDateFormatter = new SimpleDateFormat("kk.mm.ss z");
        universalDateFormatter = new SimpleDateFormat("kk.mm.ss z");
        universalDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        localSiderealTime = new LocalSiderealTime(System.getProperty(
                "telescope").trim());

        OMPTimer.getOMPTimer().setTimer(1000, this);
    }

    /**
     * Implenetation of the <code>timeElapsed</code> interface.
     *
     * Updates the associated label.
     *
     * @param evt the <code>TimerEvent</code> to consume.
     */
    public void timeElapsed() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        localSiderealTime.setDate();
        String localTime = localDateFormatter.format(date);
        String universalTime = universalDateFormatter.format(date);
        local.setText(localTime);
        universal.setText(universalTime);
        lst.setText(localSiderealTime.stime());
    }
}
