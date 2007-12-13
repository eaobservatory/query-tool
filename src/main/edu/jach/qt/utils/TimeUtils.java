package edu.jach.qt.utils;

import java.util.Calendar ;
import java.util.Date ;
import java.util.TimeZone ;
import java.text.SimpleDateFormat ;
import java.text.ParsePosition ;

/**
 * Time Utilities.
 *
 * @author  $Author$
 * @version $Id$
 */
public class TimeUtils
{
	private final String dateFormat = "yyyy-MM-dd";
	private final String timeFormat = "HH:mm:ss";
	private final String isoFormat = "yyyy-MM-dd'T'HH:mm:ss";
	private final SimpleDateFormat df = new SimpleDateFormat( dateFormat ) ;
	private final SimpleDateFormat tf = new SimpleDateFormat( timeFormat ) ;

	/**
	 * Constructor.
	 */
	public TimeUtils(){}

	/**
	 * Get the current local date.
	 * @return    Current date as yyyy-mm-dd
	 */
	public String getLocalDate()
	{
		return df.format( Calendar.getInstance().getTime() );
	}

	/** 
	 * Get the current local time.
	 *
	 * @return    Current time in HH:MM:SS format.
	 */
	public String getLocalTime()
	{
		return tf.format( Calendar.getInstance().getTime() );
	}

	/**
	 * Checks whether the date/time <code>String</code> is
	 * in ISO format.
	 * ISO format defined as YYYY-MM-DD'T'HH:MM:SS
	 *
	 * @param dateString    Date/Time string
	 * @return              <code>true</code> if valid; <code>false</code> otherwise.
	 */
	public boolean isValidDate( String dateString )
	{
		Date date = parseDate( dateString );
		return ( date != null ) ;
	}

	/**
	 * Convert a date/time string to the corresponding UTC.
	 *
	 * @param isoDate    Local Date/Time <code>String</code> in ISO format
	 * @return           UTC in ISO format.
	 */
	public String convertLocalISODatetoUTC( String isoDate )
	{
		// Parse the date to get the local Date
		Date date = parseDate( isoDate );
		String convertedDate = null;
		if( date != null )
		{
			Calendar cal = toCalendar( isoDate ) ;
			
			int hh = cal.get( Calendar.HOUR_OF_DAY ) ;
			int mm = cal.get( Calendar.MINUTE ) ;
			int ss = cal.get( Calendar.SECOND ) ;

			int millsecondsOfDay = ( hh * 3600 + mm * 60 + ss ) * 1000;

			TimeZone tz = TimeZone.getDefault();
			int tzOffset = tz.getOffset( cal.get( Calendar.ERA ) , cal.get( Calendar.YEAR ) , cal.get( Calendar.MONTH ) , cal.get( Calendar.DAY_OF_MONTH ) , cal.get( Calendar.DAY_OF_WEEK ) , millsecondsOfDay );
			cal.set( Calendar.MILLISECOND , cal.get( Calendar.MILLISECOND ) - tzOffset );

			SimpleDateFormat df = new SimpleDateFormat( isoFormat );
			convertedDate = df.format( cal.getTime() );
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
	public Calendar toCalendar( String isoDateTime )
	{
		Calendar cal = null;
		if( isValidDate( isoDateTime ) )
		{
			cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
			String[] split = isoDateTime.split( "[-T:]" ) ;
			
			int yyyy = Integer.parseInt( split[ 0 ] ) ;
			int mn = Integer.parseInt( split[ 1 ] ) ;
			int dd = Integer.parseInt( split[ 2 ] ) ;
			int hh = Integer.parseInt( split[ 3 ] ) ;
			int mm = Integer.parseInt( split[ 4 ] ) ;
			int ss = Integer.parseInt( split[ 5 ] ) ;
			
			cal.set( Calendar.YEAR , yyyy );
			cal.set( Calendar.MONTH , mn - 1 );
			cal.set( Calendar.DAY_OF_MONTH , dd );
			cal.set( Calendar.HOUR_OF_DAY , hh );
			cal.set( Calendar.MINUTE , mm );
			cal.set( Calendar.SECOND , ss );
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
	private Date parseDate( String dateString )
	{
		ParsePosition p = new ParsePosition( 0 );
		SimpleDateFormat df = new SimpleDateFormat( isoFormat );
		df.setLenient( false );
		Date date = df.parse( dateString , p );
		return date;
	}
}
