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

import gemini.sp.SpInsertData ;
import gemini.sp.SpTreeMan ;
import gemini.sp.SpObs ;
import gemini.sp.SpItem ;
import gemini.sp.SpTranslationNotSupportedException ;
import gemini.sp.obsComp.SpDRObsComp ;
import gemini.sp.obsComp.SpInstObsComp ;
import gemini.sp.obsComp.SpTelescopeObsComp ;

import java.io.FileInputStream ;
import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.FileWriter ;
import java.io.InputStreamReader ;
import java.io.File ;

import java.util.Properties ;
import java.util.Calendar ;
import java.util.GregorianCalendar ;
import java.util.Vector ;
import java.util.TimeZone ;

import java.text.SimpleDateFormat ;

import gemini.util.ConfigWriter ;
import gemini.util.JACLogger ;

/***
 * QtTools.java
 * A set  of static tools for the QT-OM
 *
 * Created: Wed Sep 19 11:09:17 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

public class QtTools
{

	/** COPIED FROM ORAC3-OM suite:
	 public static void loadConfig(String filename) is a private method
	 reads a configuration file and set up configuration. It is done by
	 putting things into java system properties which is program-system-wide visible.
	 
	 @param String args
	 @return  none
	 @throws none
	 */

	public static final int OOS_STATE_UNKNOWN = -1 ;
	public static final int OOS_STATE_IDLE = 0 ;
	public static final int OOS_STATE_STOPPED = 1 ;
	public static final int OOS_STATE_PAUSED = 2 ;
	public static final int OOS_STATE_RUNNING = 3 ;
	public static final int OOS_ACTIVE = 4 ;
	public static final int OOS_INACTIVE = 5 ;

	static final JACLogger logger = JACLogger.getLogger( QtTools.class ) ;

	public static void loadConfig( String filename )
	{
		try
		{
			String line_str ;
			FileInputStream is = new FileInputStream( filename ) ;
			BufferedReader d = new BufferedReader( new InputStreamReader( is ) ) ;

			Properties temp = System.getProperties() ;
			int lineno = 0 ;

			while( ( line_str = d.readLine() ) != null )
			{
				lineno++ ;
				if( line_str.length() > 0 )
				{
					if( line_str.charAt( 0 ) == '#' )
						continue ;

					try
					{
						int colonpos = line_str.indexOf( ":" ) ;
						temp.put( line_str.substring( 0 , colonpos ).trim() , line_str.substring( colonpos + 1 ).trim() ) ;

					}
					catch( IndexOutOfBoundsException e )
					{
						logger.fatal( "Problem reading line " + lineno + ": " + line_str ) ;
						d.close() ;
						is.close() ;
						System.exit( 1 ) ;
					}
				}
			}
			d.close() ;
			is.close() ;

		}
		catch( IOException e )
		{
			logger.error( "File error: " + e ) ;
		}
	}

	/**
	 * Execute a DRAMA command
	 *
	 * @param someExec a <code>String[]</code> value
	 */
	public static int execute( String[] cmd , boolean wait , boolean output )
	{
		ExecDtask task = new ExecDtask( cmd ) ;
		task.setWaitFor( wait ) ;

		task.setOutput( output ) ;
		task.run() ;

		// Check for errors from the script
		int status = task.getExitStatus() ;
		if( status != 0 )
			logger.error( "Error reported by instrument startup script, code was: " + status ) ;

		try
		{
			task.join() ;
		}
		catch( InterruptedException e )
		{
			logger.error( "Load task join interrupted! " + e.getMessage() ) ;
		}

		return status ;
	}

	/**
	 Wrapper around the translator method which generates XML for the
	 new queue processing.  The file will be stored in the same location
	 as the execs for now.

	 @param SpItem  and MSB
	 @return String a filename
	 */
	public static String createQueueXML( SpItem item )
	{
		// We are going to take some shortcuts, like assuming the telescope is UKIRT

		// File will go into exec path and be called ukirt_yyyymmddThhmmss.xml
		String opDir = System.getProperty( "EXEC_PATH" ) ;
		if( "false".equalsIgnoreCase( System.getProperty( "DRAMA_ENABLED" ) ) )
			opDir = System.getProperty( "user.home" ) ;

		Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) ) ;
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd'T'HHmmss" ) ;
		StringBuffer fileName = new StringBuffer( sdf.format( cal.getTime() ) ) ;
		fileName.append( ".xml" ) ;
		fileName.insert( 0 , File.separatorChar ) ;
		fileName.insert( 0 , opDir ) ;
		System.out.println( "QUEUE filename is " + fileName.toString() ) ;

		try
		{
			FileWriter fw = new FileWriter( fileName.toString() ) ;
			fw.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" ) ;
			fw.write( "<QueueEntries  telescope=\"UKIRT\">\n" ) ;

			// Now we need to get (a) the sequence for each obs in the MSB and
			// (b) the estimated duration of the obs and (c) the instrument name for the obs.
			Vector<SpItem> obs = SpTreeMan.findAllItems( item , SpObs.class.getName() ) ;
			for( SpItem obsItem : obs )
			{
				SpObs currentObs = ( SpObs )obsItem ;
				String inst = SpTreeMan.findInstrument( currentObs ).type().getReadable() ;
				double time = currentObs.getElapsedTime() ;
				fw.write( "  <Entry totalDuration=\"" + time + "\"  instrument=\"" + inst + "\">\n" ) ;
				// if we are using the old translator, we need to add the exec path, otherwise we don't
				String tName = translate( ( SpItem )currentObs , inst ) ;
				if( tName != null && tName.indexOf( opDir ) == -1 )
					fw.write( "    " + opDir + File.separatorChar + tName + "\n" ) ;
				else
					fw.write( "    " + tName + "\n" ) ;

				fw.write( "  </Entry>\n" ) ;
			}
			// Close off the entry
			fw.write( "</QueueEntries>\n" ) ;
			fw.flush() ;
			fw.close() ;
		}
		catch( IOException ioe )
		{
			String message = "Unable to write queue file " + fileName.toString() ;
			logger.error( message , ioe ) ;
			fileName = new StringBuffer() ;
		}

		return fileName.toString() ;
	}

	/**
	 * String trans (SpItem observation) is a private method to translate an observation java object 
	 * into an exec string and write it into a ascii file where is located in "EXEC_PATH" directory 
	 * and has a name stored in "execFilename"
	 * 
	 * @param SpItem
	 *            observation
	 * @return String a filename
	 * 
	 */
	public static String translate( SpItem observation , String inst )
	{
		String tname = null ;
		try
		{
			if( observation == null )
				throw new NullPointerException( "Observation passed to translate() is null" ) ;

			SpObs spObs = null ;
			if( observation instanceof SpObs )
			{
				spObs = ( SpObs )observation ;
			}
			else
			{
				while( observation != null )
				{
					observation = observation.parent() ;
					if( ( observation != null ) && ( observation instanceof SpObs ) )
					{
						spObs = ( SpObs )observation ;
						break ;
					}
				}
			}
			if( spObs == null )
				throw new NullPointerException( "Observation passed to translate() not translatable" ) ;

			spObs.translate( new Vector<String>() ) ;
			
			tname = ConfigWriter.getCurrentInstance().getExecName() ;
			logger.debug( "Translated file set to: " + tname ) ;
			String fileProperty = new String( inst + "ExecFilename" ) ;
			Properties properties = System.getProperties() ;
			properties.put( fileProperty , tname ) ;
			logger.debug( "System property " + fileProperty + " now set to " + tname ) ;
		}
		catch( NullPointerException e )
		{
			logger.fatal( "Translation failed!, Missing value " + e ) ;
		}
		catch( SpTranslationNotSupportedException sptnse )
		{
			logger.fatal( "Translation failed! " + sptnse ) ;
		}
		
		return tname ;
	}

	/**
	 * Loads a DRAMA task
	 * 
	 * @param name
	 *            a <code>String</code> value
	 */
	public static int loadDramaTasks( String name )
	{
		//starting the drama tasks
		String[] script = new String[ 6 ] ;

		script[ 0 ] = System.getProperty( "LOAD_DHSC" ) ;
		script[ 1 ] = new String( name ) ;
		script[ 2 ] = "-" + System.getProperty( "QUICKLOOK" , "noql" ) ;
		script[ 3 ] = "-" + System.getProperty( "SIMULATE" , "simTel" ) ;
		script[ 4 ] = "-" + System.getProperty( "ENGINEERING" , "eng" ) ;
		script[ 5 ] = "-omp" ;

		logger.info( "About to start script " + script[ 0 ] + " " + script[ 1 ] + " " + script[ 2 ] + " " + script[ 3 ] + " " + script[ 4 ] + " " + script[ 5 ] ) ;

		int status = QtTools.execute( script , false , true ) ;

		return status ;
	}

	public static int oosTest( String[] cmd )
	{
		String argList = "" ;
		for( int i = 0 ; i < cmd.length ; i++ )
			argList += ( " " + cmd[ i ] ) ;

		logger.debug( "oosTest argList is: " + argList ) ;

		int status = -1 ;
		try
		{
			Process p = Runtime.getRuntime().exec( cmd ) ;
			BufferedReader stdOut = new BufferedReader( new InputStreamReader( p.getInputStream() ) ) ;

			String s = null ;
			while( ( s = stdOut.readLine() ) != null )
			{
				System.err.println( s ) ;
				logger.debug( "oosTest output: >>>" + s + "<<<" ) ;
				if( s.endsWith( "IS INACTIVE" ) )
				{
					status = QtTools.OOS_INACTIVE ;
				}
				else if( s.endsWith( "IS ACTIVE" ) )
				{
					status = QtTools.OOS_ACTIVE ;
				}
				else if( s.endsWith( "Idle " ) )
				{
					status = QtTools.OOS_STATE_IDLE ;
				}
				else if( s.endsWith( "Stopped " ) )
				{
					status = QtTools.OOS_STATE_STOPPED ;
				}
				else if( s.endsWith( "Paused " ) )
				{
					status = QtTools.OOS_STATE_PAUSED ;
				}
				else if( s.endsWith( "Running " ) )
				{
					status = QtTools.OOS_STATE_RUNNING ;
				}
				else
				{
					logger.error( "UNKNOWN OOS STATE." ) ;
					status = QtTools.OOS_STATE_UNKNOWN ;
				}
			}

			p.waitFor() ;
		}
		catch( IOException e )
		{
			logger.error( "StdOut got IO exception:" + e.getMessage() ) ;
		}
		catch( InterruptedException ie )
		{
			logger.error( "oosTest process was interrupted abnormally." , ie ) ;
		}

		return status ;
	}

	/**
	 * Fix up deferred observations to include items from the original MSB.
	 *
	 * Items currently being sought : Instrument, Target list and DR recipes.
	 * @param obs item to be cloned.
	 * @return cloned item.
	 */
	public static SpItem fixupDeferredObs( SpItem obs , boolean duplicate )
	{
		SpItem deferredObs = null ;
		if( duplicate )
			deferredObs = obs.deepCopy() ;
		else
			deferredObs = obs ;

		SpInstObsComp inst = SpTreeMan.findInstrument( obs ) ;
		if( inst != null )
			insert( inst , deferredObs ) ;

		SpTelescopeObsComp obsComp = SpTreeMan.findTargetList( obs ) ;
		if( obsComp != null )
			insert( obsComp , deferredObs ) ;

		SpDRObsComp recipe = SpTreeMan.findDRRecipe( obs ) ;
		if( recipe != null )
			insert( recipe , deferredObs ) ;
		return deferredObs ;
	}

	/**
	 * Convenience method.
	 * Clone an item for insertion, and insert.
	 * @param insert item to insert.
	 * @param insertInto item to insert item into.
	 * @return boolean indicating success.
	 */
	public static boolean insert( SpItem insert , SpItem insertInto )
	{
		boolean success = true ;
		SpItem clonedItem = insert.deepCopy() ;
		SpInsertData spid = SpTreeMan.evalInsertInside( clonedItem , insertInto ) ;
		if( spid != null )
			SpTreeMan.insert( spid ) ;
		else
			success = false ;
		return success ;
	}
}
