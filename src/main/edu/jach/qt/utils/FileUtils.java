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

package edu.jach.qt.utils ;

import java.io.File ;
import java.io.IOException ;

import gemini.util.JACLogger ;

public class FileUtils
{
	private static final JACLogger logger = JACLogger.getLogger( FileUtils.class ) ;
	private static String deferredDirName ;
	private static String knownUTCDate ;
	private static String todaysDeferredDirName ;
	private static File deferredDir ;
	private static File todaysDeferredDir ;

	/**
	 * Execute `chmod` in an external process. 
	 * The perms "666" are hardcoded for files.
	 * The perms "777" are hardcoded for directories.
	 * The latter is because un *NIX a directory has to be executable to be writable.
	 * @param file to chmod
	 * @return boolean indicating success.
	 */
	public static boolean chmod( File file )
	{
		boolean success = true ;
		try
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( "chmod" ) ;
			buffer.append( " " ) ;
			if( file.isFile() )
				buffer.append( "666" ) ;
			else
				buffer.append( "777" ) ;
			buffer.append( " " ) ;
			buffer.append( file.getAbsolutePath() ) ;
			String command = buffer.toString() ;
			Process process = Runtime.getRuntime().exec( command ) ;
			process.waitFor() ;
			int exitCode = process.exitValue() ;
			if( exitCode != 0 )
				success = false ;
			buffer = null ;
		}
		catch( IOException ioe )
		{
			success = false ;
		}
		catch( InterruptedException ie )
		{
			success = false ;
		}
		if( !success )
			logger.error( "Unable to change file access permissions " + file.getAbsolutePath() ) ;
		
		return success ;
	}	
	
	/**
	 * @return The parent directory name for deferred observations as a String.
	 */
	public static String getDeferredDirectoryName()
	{
		if( deferredDirName == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "telescope" ) ) ;
			buffer.append( "data" ) ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "deferredDir" ) ) ;
			deferredDirName = buffer.toString() ;
			buffer.delete( 0 , buffer.length() ) ;
			buffer = null ;
			deferredDirName = deferredDirName.toLowerCase() ;
		}
		return deferredDirName ;
	}
	
	/**
	 * @return The directory name of deferred observations for this UT date as a String.
	 */
	public static String getTodaysDeferredDirectoryName()
	{
		if( checkUTCDate() || todaysDeferredDirName == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( FileUtils.getDeferredDirectoryName() ) ;
			buffer.append( File.separator ) ;
			buffer.append( knownUTCDate ) ;
			todaysDeferredDirName = buffer.toString() ;
			buffer.delete( 0 , buffer.length() ) ;
			buffer = null ;
			todaysDeferredDirName = todaysDeferredDirName.toLowerCase() ;
		}
		return todaysDeferredDirName ;
	}
	
	public static File getDeferredDirectory()
	{
		if( deferredDir == null )
			deferredDir = createDirectory( getDeferredDirectoryName() ) ;
		
		return deferredDir ;
	}
	
	public static File getTodaysDeferredDirectory()
	{
		if( checkUTCDate() || todaysDeferredDir == null )
			todaysDeferredDir = createDirectory( getTodaysDeferredDirectoryName() ) ;
		
		return todaysDeferredDir ;
	}
	
	/**
	 * Check wether UTC date has changed
	 * @return boolean indicating a change
	 */
	private static boolean checkUTCDate()
	{
		boolean changed = true ;
		String UTCDate = TimeUtils.getUTCDate() ;
		if( knownUTCDate == null  )
			knownUTCDate = UTCDate ;
		else if( knownUTCDate.equals( UTCDate ) )
			changed = false ;
		return changed ;
	}
	
	/**
	 * Create a directory
	 * @param fileName to create
	 * @return directory created.
	 */
	public static File createDirectory( String fileName )
	{
		File dir = null ;
		if( fileName != null )
		{
			dir = new File( fileName ) ;
			if( !dir.exists() )
			{
				logger.info( "Creating deferred directory " + fileName ) ;
				if( !dir.mkdirs() )
				{
					logger.error( "Could not create directory " + fileName ) ;
					dir = null ;
				}
				else if( !chmod( dir ) )
				{
					dir = null ;
				}
			}
			else if( !dir.canRead() )
			{
				logger.error( "Unable to read deferred directory " + fileName ) ;
				dir = null ;
			}
			else if( !dir.isDirectory() )
			{
				logger.error( fileName + " is not a directory, deleting." ) ;
				dir.delete() ;
				dir = null ;
			}
		}
		return dir ;
	}
	
	/**
	 * Recursive file deletion method 
	 * @param file to delete
	 * @param skip, file to skip
	 * @return boolean indicating if deletion was successful 
	 */
	public static boolean delete( File file , File skip )
	{
		boolean success = true ;
		
		if( !file.equals( skip ) )
		{
			if( file.isDirectory() )
			{
				File[] files = file.listFiles() ;
				for( int index = 0 ; index < files.length ; index++ )
					delete( files[ index ] , skip ) ;
			}
			
			if( !file.delete() )
			{
				String diagnostic = "" ;
				if( !file.exists() )
					diagnostic = "does not exist" ;
				else if( !file.canWrite() )
					diagnostic = "cannot be written to" ;
				logger.error( "Could not delete " + file.getName() + " " + diagnostic + "." ) ;
				success = false ;
			}
			else
			{
				logger.warn( "Deleted " + file.getName() ) ;
			}
		}
		else
		{
			logger.warn( "Skipping " + file.getName() ) ;
		}
		
		return success ;
	}
}
