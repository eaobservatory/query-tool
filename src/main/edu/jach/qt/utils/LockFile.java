package edu.jach.qt.utils ;

import java.io.File ;
import java.io.IOException ;

/**
 * Class to handle lock files in java.
 * The class creates a temporary lock file in a common
 * directory.  This file gets deleted when the JVM exits.
 *
 * @author   $Author$
 * @version  $Revision$
 * @deprecated No replacement. Now handled by startup script.
 */
public class LockFile
{
	private static boolean _exists ;
	private static String _owner ;
	private static String tmpFileDirName = File.separator + System.getProperty( "telescope" ) + "data" + File.separator + System.getProperty( "deferredDir" ) ;
	private static String lockFileDirName = tmpFileDirName.toLowerCase() ;

	/**
	 * Constructor.
	 * Makes surew the directory exists or tries to create it if not. Loops
	 * through directory contents to make sure a lock does not already exist
	 * and warns the user if it does.
	 */
	private LockFile()
	{
		_exists = false ;
		_owner = "" ;
		File lockFileDir = new File( lockFileDirName ) ;
		if( !lockFileDir.exists() )
		{
			lockFileDir.mkdir() ;
		}
		else if( lockFileDir.isDirectory() && lockFileDir.canRead() )
		{
			String[] fileList = lockFileDir.list() ;
			// Now loop over hidden files looking for one starting with .lock
			for( int fileCounter = 0 ; fileCounter < fileList.length ; fileCounter++ )
			{
				if( fileList[ fileCounter ].startsWith( ".lock" ) )
				{
					_exists = true ;
					String[] split = fileList[ fileCounter ].split( "_" ) ;
					int i = 0 ;
					while( i < split.length )
						_owner = split[ i++ ] ;
				}
			}
		}
	}

	/**
	 * Checks whether a lockfile currently exists.
	 *
	 * @return       <code>true</code> if a lockfile exists ; <code>false</code> otherwise.
	 */
	public static boolean exists()
	{
		return LockFile._exists ;
	}

	/**
	 * Get the current owner of the lockfile.
	 *
	 * @returns     The username of the person currently holding the lock.
	 */
	public static String owner()
	{
		return _owner ;
	}

	/**
	 * Create a lockfile.  The lockfile name is of the form lock_<usermname>.
	 */
	public static void createLock()
	{
		String lockFileName = lockFileDirName + File.separator + ".lock_" + System.getProperty( "user.name" ) ;
		File lockFile = new File( lockFileName ) ;
		try
		{
			lockFile.createNewFile() ;
		}
		catch( IOException ioe )
		{
			System.out.println( "Unable to create lock file " + lockFileName ) ;
		}
		lockFile.deleteOnExit() ;
	}
}
