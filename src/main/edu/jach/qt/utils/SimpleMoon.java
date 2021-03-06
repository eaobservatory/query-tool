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

package edu.jach.qt.utils;

import java.util.Calendar;
import java.util.TimeZone;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Used to derive location and illumination information about the moon.
 */
public class SimpleMoon implements OMPTimerListener {
    // Set up some stuff we are going to use
    private static final double JD2000 = 2451545.0;
    private static final double DINR = 360.0 / (2 * Math.PI);
    private static final double HINR = 24.0 / (2 * Math.PI);

    // Hard code the latitude and longitude of UKIRT for now...
    private static double longitude = -155.4717;
    private static double latitude = 19.8267;
    public static double currentRA;
    public static double currentDec;
    public static double geoCentricDistance;
    private GeocentricCoords gc;
    private TopocentricCoords tc;
    private TelescopeInformation ti;
    private Calendar _cal;
    private static SimpleMoon simpleMoon = null;
    private static HashSet<MoonChangeListener> hashSet =
            new HashSet<MoonChangeListener>();

    /**
     * Default constructor.
     *
     * Calulates the Right Ascension and Declination of the moon for the
     * current local time and location.
     *
     * @see TelescopeInformation
     */
    private SimpleMoon() {
        ti = new TelescopeInformation(System.getProperty("telescope"));
        latitude = (Double) ti.getValue("latitude");
        longitude = (Double) ti.getValue("longitude");
        getCurrentPosition();
        OMPTimer.getOMPTimer().setTimer(60 * 1000, this);
    }

    public static synchronized SimpleMoon getInstance() {
        if (simpleMoon == null) {
            simpleMoon = new SimpleMoon();
        }

        return simpleMoon;
    }

    public void set(String isoDateTime) {
        simpleMoon.getCurrentPosition(isoDateTime);
    }

    public void reset() {
        simpleMoon.getCurrentPosition();
    }

    private boolean wasUp = false;

    private int wasIlluminated = 0;

    public void timeElapsed() {
        boolean tmpBool = isUp();

        if (tmpBool != wasUp) {
            wasUp = tmpBool;
            stateChanged();
            return;
        }

        double tmpDouble = getIllumination();
        int tmpInt = (int) (tmpDouble * 100.);

        if (tmpInt != wasIlluminated) {
            wasIlluminated = tmpInt;
            stateChanged();
            return;
        }
    }

    private void stateChanged() {
        MoonChangeListener listener;
        Iterator<MoonChangeListener> iterator = hashSet.iterator();

        while (iterator.hasNext()) {
            listener = iterator.next();
            listener.moonChanged();
        }
    }

    public static void addChangeListener(MoonChangeListener listener) {
        hashSet.add(listener);
    }

    public static void removeChangeListener(MoonChangeListener listener) {
        if (hashSet.contains(listener)) {
            hashSet.remove(listener);
        }
    }

    /**
     * Returns the fraction of the moon illuminated.
     *
     * Calulated by getting the angle subtended by the moon and sun positions.
     *
     * @see #Sun
     *
     * @return <code>double</code> indicating the fraction of the moon
     *         illuminated.
     */
    public double getIllumination() {
        // Get the current position of the Sun
        Sun sun = new Sun(_cal);

        double x1 = Math.cos(currentRA / HINR) * Math.cos(currentDec / DINR);
        double y1 = Math.sin(currentRA / HINR) * Math.cos(currentDec / DINR);
        double z1 = Math.sin(currentDec / DINR);

        double x2 = Math.cos(sun.getRA() / HINR)
                * Math.cos(sun.getDec() / DINR);
        double y2 = Math.sin(sun.getRA() / HINR)
                * Math.cos(sun.getDec() / DINR);
        double z2 = Math.sin(sun.getDec() / DINR);

        double angleSubtended = Math.acos(x1 * x2 + y1 * y2 + z1 * z2);
        if (angleSubtended < 1.0e-5) {
            /* seldom the case, so don't combine test */
            if (Math.abs(currentDec) < (Math.PI / 2.0 - 0.001)
                    && Math.abs(sun.getDec()) < (Math.PI / 2.0 - 0.001)) {
                /* recycled variables here... */
                x1 = (sun.getRA() - currentRA)
                        * Math.cos((currentDec + sun.getDec()) / 2.0);
                x2 = sun.getDec() - currentDec;
                angleSubtended = Math.sqrt(x1 * x1 + x2 * x2);
            }
        }

        double illuminated = 0.5 * (1.0 - Math.cos(angleSubtended));

        return illuminated;
    }

    /**
     * Returns whether the moon is currently above the horizon.
     *
     * Currently does not take account of elevation, though this may be added
     * at a later date.
     *
     * @return <code>true</code> if the moon is up ; <code>false</code>
     *         otherwise.
     */
    public boolean isUp() {
        // Calculates the current altitude - if -ve returns false
        boolean up = true;

        // get the current HA
        double localJD = toJulianDate(_cal);
        double lst = getST(localJD) + longitude / 15.0;
        double haMoon = lst - currentRA;

        double sinAlt = Math.sin(latitude / DINR) * Math.sin(currentDec / DINR)
                + Math.cos(latitude / DINR) * Math.cos(currentDec / DINR)
                * Math.cos(haMoon / HINR);

        double altitude = Math.asin(sinAlt);
        if (altitude < 0.0) {
            up = false;
        }

        return up;
    }

    /**
     * Calculate the current right ascension and declination of the moon.
     *
     * Uses the formaula in Astronomical Almanacs for calculating the
     * approximate position.
     */
    private void getCurrentPosition() {
        // Calculate the JD corresponding to the current UT
        _cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getCurrentPosition(_cal);
    }

    /**
     * Calculate the right ascension and declination of the moon at a apecified
     * date.
     *
     * Uses the formaula in Astronomical Almanacs for calculating the
     * approximate position.
     *
     * @param isoDateTime <code>String</code> giving the current time in ISO
     *            format (yyyy-mm-dd'T'HH:MM:SS)
     */
    private void getCurrentPosition(String isoDateTime) {
        // Calculate the JD corresponding to the current UT
        _cal = TimeUtils.toCalendar(isoDateTime);
        getCurrentPosition(_cal);
    }

    /**
     * Calculate the right ascension and declination of the moon at a apecified
     * date.
     *
     * Uses the formaula in Astronomical Almanacs for calculating the
     * approximate position.
     *
     * @param _cal <code>Calendar</code> giving the current time.
     */
    private void getCurrentPosition(Calendar _cal) {
        double jDate = toJulianDate(_cal);

        // Get the local siderial time
        double gst = getST(jDate);
        double lst = gst + longitude / 15.0;

        double jCent = (jDate - JD2000) / 36525;

        /*
         * Use formula in Ast. Alm. to compute ecliptic longitude (lamda),
         * ecliptic latitude (beta) and horizontal paralax (pi)
         */
        double lambda = 218.32 + 481267.883 * jCent + 6.29
                * Math.sin((134.9 + 477198.85 * jCent) / DINR) - 1.27
                * Math.sin((259.2 - 413335.38 * jCent) / DINR) + 0.66
                * Math.sin((235.7 + 890534.23 * jCent) / DINR) + 0.21
                * Math.sin((269.9 + 954397.70 * jCent) / DINR) - 0.19
                * Math.sin((357.5 + 35999.05 * jCent) / DINR) - 0.11
                * Math.sin((186.6 + 966404.05 * jCent) / DINR);

        double beta = 5.13 * Math.sin((93.3 + 483202.03 * jCent) / DINR) + 0.28
                * Math.sin((228.2 + 960400.87 * jCent) / DINR) - 0.28
                * Math.sin((318.3 + 6003.18 * jCent) / DINR) - 0.17
                * Math.sin((217.6 - 407332.20 * jCent) / DINR);

        double pi = 0.9508 + 0.0518
                * Math.cos((134.9 + 477198.85 * jCent) / DINR) + 0.0095
                * Math.cos((259.2 - 413335.38 * jCent) / DINR) + 0.0078
                * Math.cos((235.7 + 890534.23 * jCent) / DINR) + 0.0028
                * Math.cos((269.9 + 954397.70 * jCent) / DINR);

        // Convert these to geocentric coordinates
        gc = new GeocentricCoords(lambda, beta, pi);
        geoCentricDistance = gc.getd();

        // Form the topocentric coordinate
        double lstInDeg = lst * 15.0;
        tc = new TopocentricCoords(gc.getx(), gc.gety(), gc.getz(), latitude,
                lstInDeg);

        // Now we can calculate the current RA and Dec
        double ra = Math.atan2(tc.gety(), tc.getx());
        if (ra < 0.0) {
            ra = ra + 2.0 * Math.PI;
        }

        currentRA = ra * HINR;

        double declination = Math.asin(tc.getz() / tc.getd());
        currentDec = declination * DINR;
    }

    /**
     * Converts a <code>Calendar</code> object to a Julian Date.
     *
     * Uses the eqn 7.1 in Meeus.
     *
     * @param c <code>Calendar</code> object to convert
     * @return The Julian date.
     */
    private double toJulianDate(Calendar c) {
        //
        // Using eqn by Meeus (eqn 7.1)
        //
        int yr = c.get(Calendar.YEAR);
        int mn = c.get(Calendar.MONTH) + 1;
        int dy = c.get(Calendar.DAY_OF_MONTH);

        if (mn <= 2) {
            yr = yr - 1;
            mn = mn + 12;
        }

        int a = yr / 100;
        int b = 2 - a + a / 4;

        double jd = Math.floor(365.25 * (yr + 4716));
        jd += Math.floor(30.60001 * (mn + 1));
        jd += dy + b - 1524.5;
        jd += c.get(Calendar.HOUR_OF_DAY) / 24.0 + c.get(Calendar.MINUTE)
                / 1440.0 + c.get(Calendar.SECOND) / 86400.0;

        return jd;
    }

    /**
     * Calculate the siderial time corresponding to a particular Julian Date.
     *
     * Always returns the local siderial time, unless the Julian Date is derived
     * from UTC.
     *
     * @param jDate The Julian Date to use
     * @return The Siderial Time is decimal hours.
     */
    private double getST(double jDate) {
        double jDays = Math.floor(jDate);
        double jFrac = jDate - jDays;
        double jd0;
        double ut;

        if (jFrac < 0.5) {
            jd0 = jDays - 0.5;
            ut = jFrac + 0.5;
        } else {
            jd0 = jDays + 0.5;
            ut = jFrac - 0.5;
        }

        double t = (jd0 - JD2000) / 36525;
        double gst0 = (24110.54841
                        + 8640184.812866 * t
                        + 0.093104 * t * t
                        - 6.2e-6 * t * t * t)
                / 86400.0;
        gst0 = gst0 - Math.floor(gst0);
        double gst = gst0 + 1.0027379093 * ut;
        gst = (gst - Math.floor(gst)) * 24.0;
        if (gst < 0.0) {
            gst = gst + 24.0;
        }

        return gst;
    }

    /**
     * Inner class which will gold the geocentric position of an object.
     *
     * It holds the directional cosines (l, m and n), and then rectangular
     * coordinates (x, y, z), as well as the distance in earth radii
     *
     * Public methods exist to get each parameter.
     */
    class GeocentricCoords {
        private double _l;
        private double _m;
        private double _n;
        private double _x;
        private double _y;
        private double _z;
        private double _d;

        /**
         * Contructor.
         *
         * Calculates the directional cosines, distance and rectangular
         * coordinates of an object at the specified ecliptic position.
         *
         * @param l ecliptic longitude
         * @param b ecliptic latitude
         * @param p hoizontal parallax
         */
        public GeocentricCoords(double l, double b, double p) {
            _l = Math.cos(l / DINR) * Math.cos(b / DINR);
            _m = 0.9175 * Math.cos(b / DINR) * Math.sin(l / DINR) - 0.3978
                    * Math.sin(b / DINR);
            _n = 0.3978 * Math.cos(b / DINR) * Math.sin(l / DINR) + 0.9175
                    * Math.sin(b / DINR);

            _d = 1.0 / Math.sin(p / DINR);

            _x = _d * _l;
            _y = _d * _m;
            _z = _d * _n;
        }

        /**
         * Get the x coordinate.
         *
         * x axis is from centre of earth through the Greenwich Meridian.
         *
         * @return The distance of an object along the x-axis in earth radii
         */
        public double getx() {
            return _x;
        }

        /**
         * Get the y coordinate.
         *
         * y axis is from centre of earth through 90 degrees east of the
         * Greenwich Meridian.
         *
         * @return The distance of an object along the y-axis in earth radii
         */
        public double gety() {
            return _y;
        }

        /**
         * Get the z coordinate.
         *
         * z axis is from centre of earth through North Pole.
         *
         * @return The distance of an object along the z-axis in earth radii
         */
        public double getz() {
            return _z;
        }

        /**
         * Get the l directional cosine.
         *
         * @return The l directional cosine
         */
        public double getl() {
            return _l;
        }

        /**
         * Get the m directional cosine.
         *
         * @return The m directional cosine
         */
        public double getm() {
            return _m;
        }

        /**
         * Get the n directional cosine.
         *
         * @return The n directional cosine
         */
        public double getn() {
            return _n;
        }

        /**
         * Get the distance from the centre of the earth.
         *
         * @return The distance in earth radii
         */
        public double getd() {
            return _d;
        }

        /**
         * Print the directional cosines and rectangular coordinates.
         */
        public void print() {
            System.out.println("Directional Cosines: (" + _l + "," + _m + ","
                    + _n + ")");
            System.out.println("Rectangluar coords : (" + _x + "," + _y + ","
                    + _z + ")");
        }
    }

    /**
     * Inner class which will gold the topocentric position of an object.
     *
     * It holds the directional cosines (l, m and n), and then rectangular
     * coordinates (x, y, z), as well as the distance in earth radii.
     *
     * Public methods exist to get each parameter.
     */
    class TopocentricCoords {
        private double _x;
        private double _y;
        private double _z;
        private double _l;
        private double _m;
        private double _n;
        private double _d;

        /**
         * Constructor.
         *
         * @param x Geocentric x location
         * @param y Geocentric y location
         * @param z Geocentric z location
         * @param lat Geocentric latitude of required location
         * @param lst Local Siderial time at required location
         */
        public TopocentricCoords(double x, double y, double z, double lat,
                double lst) {
            _x = x - Math.cos(lat / DINR) * Math.cos(lst / DINR);
            _y = y - Math.cos(lat / DINR) * Math.sin(lst / DINR);
            _z = z - Math.sin(lat / DINR);

            _d = Math.sqrt(_x * _x + _y * _y + _z * _z);

            _l = _x / _d;
            _m = _y / _d;
            _n = _z / _d;

        }

        /**
         * Get the x coordinate.
         *
         * x axis is from centre of earth through the Greenwich Meridian.
         *
         * @return The distance of an object along the x-axis in earth radii
         *         from the current location
         */
        public double getx() {
            return _x;
        }

        /**
         * Get the y coordinate.
         *
         * y axis is from centre of earth through the Greenwich Meridian.
         *
         * @return The distance of an object along the y-axis in earth radii
         *         from the current location
         */
        public double gety() {
            return _y;
        }

        /**
         * Get the z coordinate.
         *
         * z axis is from centre of earth through the Greenwich Meridian.
         *
         * @return The distance of an object along the z-axis in earth radii
         *         from the current location
         */
        public double getz() {
            return _z;
        }

        /**
         * Get the l directional cosine.
         *
         * @return The l directional cosine
         */
        public double getl() {
            return _l;
        }

        /**
         * Get the m directional cosine.
         *
         * @return The m directional cosine
         */
        public double getm() {
            return _m;
        }

        /**
         * Get the n directional cosine.
         *
         * @return The n directional cosine
         */
        public double getn() {
            return _n;
        }

        /**
         * Get the distance to the object from the current location.
         *
         * @return The distance in eath radii.
         */
        public double getd() {
            return _d;
        }

        /**
         * Print the rectangular topcentric coordinates.
         */
        public void print() {
            System.out.println("Rectangluar coords : (" + _x + "," + _y + ","
                    + _z + ")");
        }
    }

    /**
     * Inner class holding information on the loation of the sun.
     *
     * Needed for calculating the illuminated fraction of the moon. Has a
     * public contructor which gets the current RA and DEC and methods for
     * getting the RA and Dec.
     */
    class Sun {
        public double currentRA;
        public double currentDec;

        /**
         * Constructor calculates the current RA and Dec of the Sun.
         */
        public Sun() {
            getPosition(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        }

        public Sun(Calendar cal) {
            getPosition(cal);
        }

        /**
         * Calaculate the current RA and Dec of the Sun.
         *
         * Uses the approximation equations contained in the
         * Astronomical Almanac.
         */
        public void getPosition(Calendar cal) {
            double jDate = toJulianDate(cal);
            double deltaT = (jDate - JD2000);

            double meanLongitude = 280.460 + 0.9856474 * deltaT;
            double meanAnomaly = 357.528 + 0.9856003 * deltaT;

            double eclipticLongitude = meanLongitude + 1.915
                    * Math.sin(meanAnomaly / DINR) + 0.02
                    * Math.sin(2.0 * meanAnomaly / DINR);

            double obliquity = 23.439 - 0.0000004 * deltaT;

            double x = Math.cos(eclipticLongitude / DINR);
            double y = Math.cos(obliquity / DINR)
                    * Math.sin(eclipticLongitude / DINR);

            double ra = Math.atan2(y, x);
            if (ra < 0.0) {
                ra += 2. * Math.PI;
            }

            double declination = Math.asin(Math.sin(obliquity / DINR)
                    * Math.sin(eclipticLongitude / DINR));

            currentRA = ra * HINR;
            currentDec = declination * DINR;
        }

        /**
         * Get the Right Ascension of the sun.
         *
         * @return The right ascension in decimal hours
         */
        public double getRA() {
            return currentRA;
        }

        /**
         * Get the Declination of the sun.
         *
         * @return The declination in decimal degrees.
         */
        public double getDec() {
            return currentDec;
        }
    }

    public static void main(String[] args) {

        System.setProperty("telescope", "ukirt");
        System.setProperty("qtConfig",
                "/home/dewitt/omp/QT/config/qtSystem.conf");
        System.setProperty("telescopeConfig", "telescopedata.xml");
        SimpleMoon moon = new SimpleMoon();
        System.out.println("RA: " + SimpleMoon.currentRA);
        System.out.println("Dec: " + SimpleMoon.currentDec);
        System.out.println("Up: " + moon.isUp());
        System.out.println("Illum: " + moon.getIllumination());
    }
}
