package edu.jach.qt.utils;

import java.util.*;
import java.text.*;

/**
 * Time Utilities.
 *
 * @author  $Author$
 * @version $Id$
 */
public class TimeUtils {
    private final String dateFormat = "yyyy-MM-dd";
    private final String timeFormat = "HH:mm:ss";
    private final String isoFormat  = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Constructor.
     */
    public TimeUtils() {
    }

    /**
     * Get the current local date.
     * @return    Current date as yyyy-mm-dd
     */
    public String getLocalDate() {
	Calendar localCal = Calendar.getInstance();
	SimpleDateFormat df = new SimpleDateFormat(dateFormat);
	return df.format(localCal.getTime());
    }

    /** 
     * Get the current local time.
     *
     * @return    Current time in HH:MM:SS format.
     */
    public String getLocalTime() {
	Calendar localCal = Calendar.getInstance();
	SimpleDateFormat df = new SimpleDateFormat(timeFormat);
	return df.format(localCal.getTime());
    }

    /**
     * Checks whether the date/time <code>String</code> is
     * in ISO format.
     * ISO format defined as YYYY-MM-DD'T'HH:MM:SS
     *
     * @param dateString    Date/Time string
     * @return              <code>true</code> if valid; <code>false</code> otherwise.
     */
    public boolean isValidDate(String dateString) {
	boolean valid = true;
	Date date = parseDate(dateString);
	if (date == null) {
	    valid = false;
	}
	return valid;
    }

    /**
     * Convert a date/time string to the corresponding UTC.
     *
     * @param isoDate    Local Date/Time <code>String</code> in ISO format
     * @return           UTC in ISO format.
     */
    public String convertLocalISODatetoUTC(String isoDate) {
	// Parse the date to get the local Date
	Date date = parseDate(isoDate);
	String convertedDate = null;
	if (date != null) {
	    // Break the string into tokens
	    StringTokenizer st = new StringTokenizer(isoDate, "-T:");
	    String yyyy  = st.nextToken();
	    String mn    = st.nextToken();
	    String dd    = st.nextToken();
	    String hh    = st.nextToken();
	    String mm    = st.nextToken();
	    String ss    = st.nextToken();

	    int millsecondsOfDay = (Integer.parseInt(hh)*3600 +
				    Integer.parseInt(mm)*60 +
				    Integer.parseInt(ss))*1000;

	    Calendar cal = Calendar.getInstance();
	    cal.set (Calendar.YEAR, Integer.parseInt(yyyy));
	    cal.set (Calendar.MONTH, Integer.parseInt(mn) - 1);
	    cal.set (Calendar.DAY_OF_MONTH, Integer.parseInt(dd));
	    cal.set (Calendar.HOUR_OF_DAY, Integer.parseInt(hh));
	    cal.set (Calendar.MINUTE, Integer.parseInt(mm));
	    cal.set (Calendar.SECOND, Integer.parseInt(ss));

	    TimeZone tz = TimeZone.getDefault();
	    int tzOffset = tz.getOffset (cal.get(Calendar.ERA),
					 cal.get(Calendar.YEAR),
					 cal.get(Calendar.MONTH),
					 cal.get(Calendar.DAY_OF_MONTH),
					 cal.get(Calendar.DAY_OF_WEEK),
					 millsecondsOfDay);
	    cal.set(Calendar.MILLISECOND, 
		    cal.get(Calendar.MILLISECOND) - tzOffset);

	    SimpleDateFormat df = new SimpleDateFormat(isoFormat);
	    convertedDate = df.format(cal.getTime());
	}
	return convertedDate;
    }

    /**
     * Convert an ISO format date/time string into a <code>Calendar</code>
     * object.
     * @see java.util.Calendar
     *
     * @param isoDateTime     Date/Time <code>String</code> is ISO format.
     * @return                Corrsponding <code>Calendar</code> object
     */
    public Calendar toCalendar(String isoDateTime) {
	Calendar cal = null;
	if (isValidDate(isoDateTime)) {
	    cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    StringTokenizer st = new StringTokenizer(isoDateTime, "-:T");
	    String yyyy  = st.nextToken();
	    String mn    = st.nextToken();
	    String dd    = st.nextToken();
	    String hh    = st.nextToken();
	    String mm    = st.nextToken();
	    String ss    = st.nextToken();
	    cal.set (Calendar.YEAR, Integer.parseInt(yyyy));
	    cal.set (Calendar.MONTH, Integer.parseInt(mn) - 1);
	    cal.set (Calendar.DAY_OF_MONTH, Integer.parseInt(dd));
	    cal.set (Calendar.HOUR_OF_DAY, Integer.parseInt(hh));
	    cal.set (Calendar.MINUTE, Integer.parseInt(mm));
	    cal.set (Calendar.SECOND, Integer.parseInt(ss));
	}
	return cal;
    }

    /**
    * Convert a date/time string to a <code>Date</code> object.
    * date/time must be in ISO format.
    * @see java.util.Date
    * @param dateString       Date/Time string to convert
    * @return                 Corresponding <code>Date</code> object or
    *                         <code>null</code> on failure.
    */
    private Date parseDate(String dateString) {
	ParsePosition p = new ParsePosition(0);
	SimpleDateFormat df = new SimpleDateFormat(isoFormat);
	df.setLenient(false);
	Date date =  df.parse(dateString, p);
	return date;
    }
}
