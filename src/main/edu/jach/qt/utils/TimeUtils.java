package edu.jach.qt.utils;

import java.util.*;
import java.text.*;


public class TimeUtils {
    private final String dateFormat = "yyyy-MM-dd";
    private final String timeFormat = "HH:mm:ss";
    private final String isoFormat  = "yyyy-MM-dd'T'HH:mm:ss";

    public TimeUtils() {
    }

    public String getLocalDate() {
	Calendar localCal = Calendar.getInstance();
	SimpleDateFormat df = new SimpleDateFormat(dateFormat);
	return df.format(localCal.getTime());
    }

    public String getLocalTime() {
	Calendar localCal = Calendar.getInstance();
	SimpleDateFormat df = new SimpleDateFormat(timeFormat);
	return df.format(localCal.getTime());
    }

    public boolean isValidDate(String dateString) {
	boolean valid = true;
	Date date = parseDate(dateString);
	if (date == null) {
	    valid = false;
	}
	return valid;
    }

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

    private Date parseDate(String dateString) {
	ParsePosition p = new ParsePosition(0);
	SimpleDateFormat df = new SimpleDateFormat(isoFormat);
	df.setLenient(false);
	Date date =  df.parse(dateString, p);
	return date;
    }
}
