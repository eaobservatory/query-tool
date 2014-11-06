/*
 * Copyright (C) 2007-2008 Science and Technology Facilities Council.
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
 *
 *	Author : Sam Hart ( JAC )
 *	Based on Perl code by Tim Jenness which uses SLA Lib by Pat Wallace
 */

package edu.jach.qt.utils;

import uk.ac.starlink.pal.Pal;
import uk.ac.starlink.pal.Observatory;
import uk.ac.starlink.pal.palTime;
import uk.ac.starlink.pal.palError;

import java.util.Calendar;
import java.util.TimeZone;

public class LocalSiderealTime {
    private Pal pal;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private double second;
    private double longitude;
    private Calendar calendar;
    private Observatory observatory;

    public LocalSiderealTime(String telescope) {
        pal = new Pal();
        setTelescope(telescope);
        setDate();
    }

    public String stime() {
        // file:///star/starjava/docs/pal/javadocs/uk/ac/starlink/pal/palTime.html

        double radians = ut2lst();
        palTime time = pal.Dr2tf(radians);
        String currentLST = time.toString(2);
        currentLST = currentLST.replace(' ', ':');
        return currentLST + " LST";
    }

    public double ut2lst() {
        // file:///star/starjava/docs/pal/javadocs/uk/ac/starlink/pal/Pal.html

        /*
         * Calculate sidereal time of greenwich Conversion from Universal Time
         * to Sidereal Time. #Gmst(double)
         */
        double modifiedJulianDate = getMJD();
        double sidereal = pal.Gmst(modifiedJulianDate);

        /*
         * Equation of the equinoxes (requires TT although makes very little
         * difference) Increment to be applied to Coordinated Universal Time UTC
         * to give Terrestrial Time TT (formerly Ephemeris Time ET).
         * #Dtt(double)
         */
        double terrestrialTime = modifiedJulianDate
                + (pal.Dtt(modifiedJulianDate) / 86400.);
        /*
         * Equation of the equinoxes (IAU 1994, double precision) #Eqeqx(double)
         */
        double equinox = pal.Eqeqx(terrestrialTime);
        /*
         * Local sidereal time = GMST + EQEQX + Longitude in radians Normalize
         * angle into range 0-2 . #Dranrm(double)
         */
        double radians = pal.Dranrm(sidereal + equinox + longitude);

        return radians;
    }

    public void setDate() {
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = (double) calendar.get(Calendar.SECOND);
    }

    public void setTelescope(String telescope) {
        if (telescope != null && !telescope.trim().equals("")) {
            try {
                observatory = pal.Obs(telescope.toUpperCase());
            } catch (NullPointerException npe) {
                throw new RuntimeException("Telescope " + telescope
                        + " unknown", npe);
            }
            longitude = observatory.getLong();
            // Convert longitude to west negative
            longitude *= -1.;
        } else {
            throw new RuntimeException("No telescope specified");
        }
    }

    private double getMJD() {
        // file:///star/starjava/docs/pal/javadocs/uk/ac/starlink/pal/Pal.html

        /*
         * Calculate fraction of day Convert hours, minutes, seconds to days.
         * #Dtf2d(int,%20int,%20double)
         */
        double fractionOfDay = 0.;
        try {
            fractionOfDay = pal.Dtf2d(hour, minute, second);
        } catch (palError pe) {
            throw new RuntimeException("Could not obtain fraction of day for "
                    + hour + ":" + minute + ":" + second, pe);
        }

        /*
         * Calculate modified julian date of UT day Gregorian calendar to
         * Modified Julian Date. #Cldj(int,%20int,%20int)
         */
        double modifiedJulianDate = 0.;
        try {
            modifiedJulianDate = pal.Cldj(year, month, day);
        } catch (palError pe) {
            throw new RuntimeException(
                    "Could not obtain modified julian date for " + month + "/"
                            + day + "/" + year, pe);
        }

        // Find MJD of current time (not just day)
        modifiedJulianDate += fractionOfDay;

        return modifiedJulianDate;
    }

    public String toString() {
        return "Telescope : " + observatory.getId() + " on "
                + calendar.getTime().toString() + " ( " + getMJD() + " )";
    }
}
