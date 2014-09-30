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

package edu.jach.qt.utils ;

// Gemini imports
import gemini.sp.SpItem ;
import gemini.util.JACLogger ;

// ORAC imports
import orac.util.SpInputXML ;

// OMP imports
import omp.SoapClient ;

// Standard imports
import java.io.FileWriter ;
import java.net.URL ;
import java.net.MalformedURLException ;

// Miscellaneous imports

/**
 * MsbClient.java
 *
 * This is a utility class used to send SOAP messages to the MsbServer.
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>,
 * modified by Martin Folger
 */
public class MsbClient extends SoapClient
{
	static JACLogger logger = JACLogger.getLogger( MsbClient.class ) ;
	private static URL url = null ;

	private static URL getURL()
	{
		if( url == null )
		{
			try
			{
				url = new URL( System.getProperty( "msbServer" ) ) ;
			}
			catch( MalformedURLException mue )
			{
				logger.error( "getURL threw Exception" , mue ) ;
				mue.printStackTrace() ;
			}
		}
		return url ;
	}

	private static String filename = null ;

	private static String getFilename()
	{
		if( filename == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( System.getProperty( "msbSummary" ) ) ;
			buffer.append( "." ) ;
			buffer.append( System.getProperty( "user.name" ) ) ;
			filename = buffer.toString() ;
			buffer = null ;
		}
		return filename ;
	}

	/**
	 * <code>queryMSB</code>
	 * Perform a query to the MsbServer with the given query
	 * String. Success will return a true value and write the msbSummary
	 * xml to file.  A unique file will be written for each user to allow
	 * multiple users on th same machine.  This file should not be read by
	 * the code.
	 *
	 * @param xmlQueryString a <code>String</code> value. The xml representing the query.
	 * @return a <code>boolean</code> value indicating success.
	 */
	public static boolean queryMSB( String xmlQueryString )
	{
		boolean success = false ;

		logger.debug( "Sending queryMSB: " + xmlQueryString ) ;
		logger.info( "Connecting to: " + getURL().toString() ) ;
		flushParameter() ;
		addParameter( "xmlquery" , String.class , xmlQueryString ) ;
		try
		{
			FileWriter fw = new FileWriter( getFilename() ) ;
			Object tmp = doCall( getURL() , "urn:OMP::MSBServer" , "queryMSB" ) ;

			if( tmp != null )
			{
				fw.write( ( String )tmp ) ;
				fw.flush() ;
				fw.close() ;
			}
			success = true ;
		}
		catch( Exception e )
		{
			logger.error( "queryMSB threw Exception" , e ) ;
			e.printStackTrace() ;
		}
		return success ;
	}

	/**
	 * <code>queryCalibration</code>
	 * Perform a query to the MsbServer with the special calibration query
	 * String. Success will return a true value and write the msbSummary
	 * xml to file.  A unique file will be written for each user to allow
	 * multiple users on th same machine.  This file should not be read by
	 * the code.
	 *
	 * @param xmlQueryString a <code>String</code> value. The xml representing the query.
	 * @return a <code>String</code> value of the results in XML.
	 */
	public static String queryCalibration( String xmlQueryString )
	{
		String returnString = null ;
		logger.debug( "Sending queryMSB: " + xmlQueryString ) ;
		flushParameter() ;
		addParameter( "xmlquery" , String.class , xmlQueryString ) ;
		addParameter( "maxcount" , Integer.class , new Integer( 2000 ) ) ;
		try
		{
			Object tmp = doCall( getURL() , "urn:OMP::MSBServer" , "queryMSB" ) ;
			returnString = tmp.toString() ;
		}
		catch( Exception e )
		{
			logger.error( "queryCalibration threw Exception" , e ) ;
			e.printStackTrace() ;
		}
		return returnString ;
	}

	public static String fetchCalibrationProgram()
	{
		String xml = null ;
		String telescope = System.getProperty( "telescope" ) ;
		logger.debug( "Sending fetchCalProgram: " + telescope ) ;
		flushParameter() ;
		addParameter( "telescope" , String.class , telescope ) ;
		addParameter( "compress" , String.class , "gzip" ) ;
		try
		{
			Object tmp = doCall( getURL() , "urn:OMP::MSBServer" , "fetchCalProgram" ) ;
			if( tmp instanceof byte[] )
				xml = new String( ( byte[] )tmp ) ;
		}
		catch( Exception e )
		{
			logger.error( "fetchCalibrationProgram threw Exception" , e ) ;
			e.printStackTrace() ;
		}
		return xml ;
	}

	/**
	 * <code>fetchMSB</code> Fetch the msb indicated by the msbid. The SpItem will return null on failure. In the future this should throw an exception instead.
	 * 
	 * @param msbid
	 *            an <code>Integer</code> value of the MSB
	 * @return a <code>SpItem</code> value representing the MSB.
	 */
	public static SpItem fetchMSB( Integer msbid )
	{
		SpItem spItem = null ;
		String spXML = null ;
		logger.debug( "Sending fetchMSB: " + msbid ) ;
		flushParameter() ;
		addParameter( "key" , Integer.class , msbid ) ;
		addParameter( "compress" , String.class , "gzip" ) ;
		try
		{
			FileWriter fw = new FileWriter( getFilename() ) ;
			Object o = doCall( getURL() , "urn:OMP::MSBServer" , "fetchMSB" ) ;

			if( o != null )
			{
				if( o instanceof byte[] )
					spXML = new String( ( byte[] )o ) ;
				else if( o instanceof String )
					spXML = ( String )o ;
				fw.write( spXML ) ;
				fw.flush() ;
				fw.close() ;
				SpInputXML spInputXML = new SpInputXML() ;
				spItem = spInputXML.xmlToSpItem( spXML ) ;
			}
		}
		catch( Exception e )
		{
			logger.error( "fetchMSB threw Exception" , e ) ;
			e.printStackTrace() ;
		}

		return spItem ;
	}

	private static MsbColumns columns ;

	public synchronized static MsbColumns getColumnInfo()
	{
		if( columns == null )
			columns = new MsbColumns() ;
		else
			return columns ;

		String[] names = getColumnNames() ;
		String[] types = getColumnClasses() ;

		String hiddenColumns = System.getProperty( "hiddenColumns" ) ;
		String[] hidden = new String[ 0 ] ;
		if( hiddenColumns != null )
			hidden = hiddenColumns.split( "%" ) ;

		if( names != null && types != null )
		{
			if( names.length == types.length )
			{
				MsbColumnInfo columnInfo ;
				for( int index = 0 ; index < names.length ; index++ )
				{
					String name = names[ index ] ;
					String type = types[ index ] ;
					columnInfo = new MsbColumnInfo( name , type ) ;
					for( int i = 0 ; i < hidden.length ; i++ )
					{
						if( hidden[ i ].equalsIgnoreCase( name ) )
							columnInfo.setVisible( false ) ;
					}
					columns.add( columnInfo ) ;
				}
			}
		}
		else
		{
			columns = new MsbColumns() ;
		}
		return columns ;
	}

	/**
	 * Method to get the list of columns in an MSB Summary. Requires the <code>telescope</code> system parameter to be set.
	 * 
	 * @return A string array of column names.
	 */
	private static String[] columnNames ;

	private static String[] getColumnNames()
	{
		if( columnNames != null )
			return columnNames ;
		flushParameter() ;
		addParameter( "telescope" , String.class , System.getProperty( "telescope" ) ) ;
		try
		{
			Object o = doCall( getURL() , "urn:OMP::MSBServer" , "getResultColumns" ) ;
			columnNames = ( String[] )o ;
		}
		catch( Exception e )
		{
			logger.error( "getColumnNames threw exception" , e ) ;
		}
		return columnNames ;
	}

	/**
	 * Method to get the list of column types in an MSB Summary. Requires the <code>telescope</code> system parameter to be set.
	 * 
	 * @return A string array of column types (eg Integer, String, etc).
	 */
	private static String[] columnClasses ;

	private static String[] getColumnClasses()
	{
		if( columnClasses != null )
			return columnClasses ;
		flushParameter() ;
		addParameter( "telescope" , String.class , System.getProperty( "telescope" ) ) ;
		try
		{
			Object o = doCall( getURL() , "urn:OMP::MSBServer" , "getTypeColumns" ) ;
			columnClasses = ( String[] )o ;
		}
		catch( Exception e )
		{
			logger.error( "getColumnNames threw exception" , e ) ;
		}
		return columnClasses ;
	}

	/**
	 * Describe <code>main</code> method here.
	 *
	 * @param args a <code>String[]</code> value
	 */
	public static void main( String[] args )
	{
		MsbClient.queryMSB( "<Query><Moon>Dark</Moon></Query>" ) ;
		MsbClient.fetchMSB( new Integer( 96 ) ) ;
	}
}
