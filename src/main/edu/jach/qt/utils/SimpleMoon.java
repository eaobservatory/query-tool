package edu.jach.qt.utils;

import java.util.*;


public class SimpleMoon {

    // Set up some stuff we are going to use
    private static final double JD2000 = 2451545.;
    private static final double DINR   = 360./(2*Math.PI);
    private static final double HINR   = 24./(2*Math.PI);
    private static final double AU     = 23454.785; // In Earth radii

    // Hard code the latitude and longitude of UKIRT for now...
    private static double longitude  = -155.4717;
    private static double latitude   = 19.8267;

    // static orbital elements of the moon:
    private final double INCLINATION = 5.1453964; // In degress
    private final double SEMIMAJORAXIS = 60.2666; // Earth radii
    private final double ECCENTRICITY = 0.054900;

    public static  double currentRA;
    public static  double currentDec;
    public static  double geoCentricDistance;
    private GeocentricCoords gc;
    private TopocentricCoords tc;

    private TelescopeInformation ti;

    // Constructor - sets up current information
    public SimpleMoon() {
	ti = new TelescopeInformation(System.getProperty("telescope"));
	latitude = ((Double)(ti.getValue("latitude"))).doubleValue();
	longitude = ((Double)(ti.getValue("longitude"))).doubleValue();
	getCurrentPosition();
    }

    // Constructor - sets up information based on a specified time
    public SimpleMoon (String isoDateTime) {
	ti = new TelescopeInformation(System.getProperty("telescope"));
	latitude = ((Double)(ti.getValue("latitude"))).doubleValue();
	longitude = ((Double)(ti.getValue("longitude"))).doubleValue();
	getCurrentPosition(isoDateTime);
    }

    // Get the faction illuminated
    public double getIllumination() {
	// Get the current position of the Sun
	Sun sun = new Sun();

	double x1 = Math.cos(currentRA/HINR)*Math.cos(currentDec/DINR);
	double y1 = Math.sin(currentRA/HINR)*Math.cos(currentDec/DINR);
	double z1 = Math.sin(currentDec/DINR);
	
	double x2 = Math.cos(sun.getRA()/HINR)*Math.cos(sun.getDec()/DINR);
	double y2 = Math.sin(sun.getRA()/HINR)*Math.cos(sun.getDec()/DINR);
	double z2 = Math.sin(sun.getDec()/DINR);

	double angleSubtended = Math.acos(x1*x2+y1*y2+z1*z2);
	if(angleSubtended < 1.0e-5) {  /* seldom the case, so don't combine test */
		if(Math.abs(currentDec)   < (Math.PI/2. - 0.001) &&
		   Math.abs(sun.getDec()) < (Math.PI/2. - 0.001))    {
		    /* recycled variables here... */
		    x1 = (sun.getRA() - currentRA) * Math.cos((currentDec+sun.getDec())/2.);
		    x2 = sun.getDec() - currentDec;
		    angleSubtended = Math.sqrt(x1*x1 + x2*x2);
		}
	}

	double illuminated = 0.5*(1. - Math.cos(angleSubtended));

	return illuminated;
    }

    // Find out whether the moon is up
    public boolean isUp() {
	// Calculates the current altitude - if -ve returns false
	boolean up = true;

	// get the current HA
	Calendar local = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	double localJD = toJulianDate(local);
	double lst = getST(localJD) + longitude/15.;
	double haMoon = lst - currentRA;

	double sinAlt = Math.sin(latitude/DINR)*Math.sin(currentDec/DINR) +
	    Math.cos(latitude/DINR)*Math.cos(currentDec/DINR)*Math.cos(haMoon/HINR);

	double altitude = Math.asin(sinAlt);
	if ( altitude < 0.0 ) {
	    up = false;
	}

	return up;
    }

    // Get the current RA and Dec os the moon
    private void getCurrentPosition() {
	//
	// Calculate the JD corresponding to the current UT
	//
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	double jDate = toJulianDate(cal);

	// Get the local siderial time
	double gst = getST(jDate);
	double lst = gst + longitude/15.;

	double jCent = (jDate-JD2000)/36525;

	/* 
	 * Use formula in Ast. Alm. to compute ecliptic longitude (lamda),
	 * ecliptic latitude (beta) and horizontal paralax (pi)
	 */
	double lambda = 218.32 + 481267.883 * jCent
	    + 6.29 * Math.sin((134.9 + 477198.85 * jCent) / DINR)
	    - 1.27 * Math.sin((259.2 - 413335.38 * jCent) / DINR)
	    + 0.66 * Math.sin((235.7 + 890534.23 * jCent) / DINR)
	    + 0.21 * Math.sin((269.9 + 954397.70 * jCent) / DINR)
	    - 0.19 * Math.sin((357.5 +  35999.05 * jCent) / DINR)
	    - 0.11 * Math.sin((186.6 + 966404.05 * jCent) / DINR);

	double beta = 
	      5.13 * Math.sin((93.3 + 483202.03 * jCent) / DINR)
	    + 0.28 * Math.sin((228.2 + 960400.87 * jCent) / DINR)
	    - 0.28 * Math.sin((318.3 +   6003.18 * jCent) / DINR)
	    - 0.17 * Math.sin((217.6 - 407332.20 * jCent) / DINR);

	double pi = 0.9508
	    + 0.0518 * Math.cos((134.9 + 477198.85 * jCent) / DINR)
	    + 0.0095 * Math.cos((259.2 - 413335.38 * jCent) / DINR)
	    + 0.0078 * Math.cos((235.7 + 890534.23 * jCent) / DINR)
	    + 0.0028 * Math.cos((269.9 + 954397.70 * jCent) / DINR);

	// Convert these to geocentric coordinates
	gc = new GeocentricCoords(lambda, beta, pi);
	geoCentricDistance = gc.getd();

	// Form the topocentric coordinate
	double lstInDeg = lst*15.;
	tc = new TopocentricCoords ( gc.getx(),
				     gc.gety(),
				     gc.getz(),
				     latitude,
				     lstInDeg);
	
	// Now we can calculate the current RA and Dec
	double ra = Math.atan2(tc.gety(), tc.getx());
	if (ra < 0.0) ra = ra + 2.*Math.PI;
	currentRA = ra * HINR;

	double declination = Math.asin(tc.getz()/tc.getd());
	currentDec = declination * DINR;

    }

    // Get the RA and Dec of the moon at a specified local time
    private void getCurrentPosition(String isoDateTime) {
	//
	// Calculate the JD corresponding to the current UT
	//
	TimeUtils tu = new TimeUtils();
	Calendar cal = tu.toCalendar(isoDateTime);

	double jDate = toJulianDate(cal);

	// Get the local siderial time
	double gst = getST(jDate);
	double lst = gst + longitude/15.;

	double jCent = (jDate-JD2000)/36525;

	/* 
	 * Use formula in Ast. Alm. to compute ecliptic longitude (lamda),
	 * ecliptic latitude (beta) and horizontal paralax (pi)
	 */
	double lambda = 218.32 + 481267.883 * jCent
	    + 6.29 * Math.sin((134.9 + 477198.85 * jCent) / DINR)
	    - 1.27 * Math.sin((259.2 - 413335.38 * jCent) / DINR)
	    + 0.66 * Math.sin((235.7 + 890534.23 * jCent) / DINR)
	    + 0.21 * Math.sin((269.9 + 954397.70 * jCent) / DINR)
	    - 0.19 * Math.sin((357.5 +  35999.05 * jCent) / DINR)
	    - 0.11 * Math.sin((186.6 + 966404.05 * jCent) / DINR);

	double beta = 
	      5.13 * Math.sin((93.3 + 483202.03 * jCent) / DINR)
	    + 0.28 * Math.sin((228.2 + 960400.87 * jCent) / DINR)
	    - 0.28 * Math.sin((318.3 +   6003.18 * jCent) / DINR)
	    - 0.17 * Math.sin((217.6 - 407332.20 * jCent) / DINR);

	double pi = 0.9508
	    + 0.0518 * Math.cos((134.9 + 477198.85 * jCent) / DINR)
	    + 0.0095 * Math.cos((259.2 - 413335.38 * jCent) / DINR)
	    + 0.0078 * Math.cos((235.7 + 890534.23 * jCent) / DINR)
	    + 0.0028 * Math.cos((269.9 + 954397.70 * jCent) / DINR);

	// Convert these to geocentric coordinates
	gc = new GeocentricCoords(lambda, beta, pi);
	geoCentricDistance = gc.getd();

	// Form the topocentric coordinate
	double lstInDeg = lst*15.;
	tc = new TopocentricCoords ( gc.getx(),
				     gc.gety(),
				     gc.getz(),
				     latitude,
				     lstInDeg);
	
	// Now we can calculate the current RA and Dec
	double ra = Math.atan2(tc.gety(), tc.getx());
	if (ra < 0.0) ra = ra + 2.*Math.PI;
	currentRA = ra * HINR;

	double declination = Math.asin(tc.getz()/tc.getd());
	currentDec = declination * DINR;

    }


    // Convert a calendar class to a Julian Date
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

	int a = yr/100;
	int b = 2 - a + a/4;

	double jd = Math.floor(365.25*(yr+4716));
	jd = jd + Math.floor(30.60001*(mn+1));
	// 	jd = jd + dy + b -1524.5;
	jd = jd + dy + b - 1524.;
	jd = jd + c.get(Calendar.HOUR)/24. +
	    c.get(Calendar.MINUTE)/1440. +
	    c.get(Calendar.SECOND)/86400.;
	
	return jd;
    }

    // Get the siderial time corresponding to a specified Julian Date
    private double getST(double jDate) {

	double jDays = Math.floor(jDate);
	double jFrac = jDate - jDays;
	double jd0;
	double ut;
	if (jFrac < 0.5) {
	    jd0 = jDays-0.5;
	    ut  = jFrac+0.5;
	}
	else {
	    jd0 = jDays+0.5;
	    ut  = jFrac-0.5;
	}

	double t = (jd0 - JD2000)/36525;
	double gst0 = (24110.54841+8640184.812866*t+0.093104*t*t-6.2e-6*t*t*t)/86400.;
	gst0 = gst0 - Math.floor(gst0);
	double gst = gst0 + 1.0027379093*ut;
	gst = (gst - Math.floor(gst))*24.;
	if (gst < 0.) gst = gst+24.0;

	return gst;
    }

    /*
     * Inner class which will gold the geocentric position of an object.
     * It holds the directional cosines (l, m and n), and then rectangular
     * coordinates (x, y, z), as well as the distance in earth radii
     *
     * Public methods exist to get each parameter
     */
    class GeocentricCoords {
	private double _l;
	private double _m;
	private double _n;
	private double _x;
	private double _y;
	private double _z;
	private double _d;
	
	public GeocentricCoords ( double l,
				  double b,
				  double p )
	{
	    _l = Math.cos(l/DINR)*Math.cos(b/DINR);
	    _m = 0.9175*Math.cos(b/DINR)*Math.sin(l/DINR) -
		0.3978*Math.sin(b/DINR);
	    _n = 0.3978*Math.cos(b/DINR)*Math.sin(l/DINR) +
		0.9175*Math.sin(b/DINR);

	    _d = 1./Math.sin(p/DINR);

	    _x = _d * _l;
	    _y = _d * _m;
	    _z = _d * _n;
	}
	public double getx() {
	    return _x;
	}
	public double gety() {
	    return _y;
	}
	public double getz() {
	    return _z;
	}
	public double getl() {
	    return _l;
	}
	public double getm() {
	    return _m;
	}
	public double getn() {
	    return _n;
	}
	public double getd() {
	    return _d;
	}
	public void print() {
	    System.out.println("Directional Cosines: ("+_l+","+_m+","+_n+")");
	    System.out.println("Rectangluar coords : ("+_x+","+_y+","+_z+")");
	}
    } // End of Inner class GeocentricCoords

    /*
     * Inner class which will gold the topocentric position of an object.
     * It holds the directional cosines (l, m and n), and then rectangular
     * coordinates (x, y, z), as well as the distance in earth radii
     *
     * Public methods exist to get each parameter
     */
    class TopocentricCoords {
	private double _x;
	private double _y;
	private double _z;
	private double _l;
	private double _m;
	private double _n;
	private double _d;

	public TopocentricCoords (double x,
				  double y,
				  double z,
				  double lat,
				  double lst)
	{
	    _x = x - Math.cos(lat/DINR)*Math.cos(lst/DINR);
	    _y = y - Math.cos(lat/DINR)*Math.sin(lst/DINR);
	    _z = z - Math.sin(lat/DINR);

	    _d = Math.sqrt(_x*_x + _y*_y + _z*_z);

	    _l = _x/_d;
	    _m = _y/_d;
	    _n = _z/_d;
	    
	}
	public double getx() {
	    return _x;
	}
	public double gety() {
	    return _y;
	}
	public double getz() {
	    return _z;
	}
	public double getl() {
	    return _l;
	}
	public double getm() {
	    return _m;
	}
	public double getn() {
	    return _n;
	}
	public double getd() {
	    return _d;
	}
	public void print() {
	    System.out.println("Rectangluar coords : ("+_x+","+_y+","+_z+")");
	}
    } // End of Inner class TopocentricCoords

    /*
     * Inner class holding information on the loation of the sun.  Needed for
     * calculating the illuminated fraction of the moon.  Has a public contructor
     * which gets the current RA and DEC and methods for getting the RA and Dec.
     */
    class Sun {
	public double currentRA;
	public double currentDec;

	public Sun() {
	    getPosition();
	}

	public void getPosition() {
	    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    double jDate = toJulianDate(cal);
	    double deltaT = (jDate-JD2000);
	    
	    double meanLongitude = 280.460 + 0.9856474*deltaT;
	    double meanAnomaly   = 357.528 + 0.9856003*deltaT;

	    double eclipticLongitude = meanLongitude + 
		1.915*Math.sin(meanAnomaly/DINR) +
		0.02*Math.sin(2.*meanAnomaly/DINR);
	    
	    double obliquity = 23.439 - 0.0000004*deltaT;

	    double x = Math.cos(eclipticLongitude/DINR);
	    double y = Math.cos(obliquity/DINR)*Math.sin(eclipticLongitude/DINR);
	    double z = Math.sin(obliquity/DINR)*Math.sin(eclipticLongitude/DINR);

	    double ra = Math.atan2(y, x);
	    if (ra < 0.0) ra = ra + 2.*Math.PI;
	    double declination = Math.asin(Math.sin(obliquity/DINR)*Math.sin(eclipticLongitude/DINR));

	    currentRA  = ra*HINR;
	    currentDec = declination*DINR;
	}

	public double getRA() {
	    return currentRA;
	}
	public double getDec() {
	    return currentDec;
	}
    } // End of Inner Class Sun

    public static void main (String [] args) {

	SimpleMoon moon = new SimpleMoon();
	System.out.println("RA: "+moon.currentRA);
	System.out.println("Dec: "+ moon.currentDec);
	System.out.println("Up: "+moon.isUp());
	System.out.println("Illum: "+moon.getIllumination());
    }

}
