package edu.jach.qt.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;

import javax.swing.JOptionPane ;

import org.apache.log4j.Logger;


/**
 * This class is a base class and should be extended to execute MSBs for
 * each telescope.  It basically is used to decide whether the project
 * being executed comes from the deferred or project list.
 * @author $Author$
 * @version $Id$
 */
public class Execute {

    /**
     * Indicates whether a observation is from the deferred list or the
     * project list.
     */
    protected boolean isDeferred = false;
    
    private static Random random = new Random() ;
	private static String deferredDirPath = null ;
	
	private static final Logger logger = Logger.getLogger( Execute.class ) ;

    /**
     * Default constructor.
     * @throws Exception   When no or multiple items selected.
     */
    protected Execute() throws Exception
	{
		if( ProgramTree.getSelectedItem() == null && DeferredProgramList.getCurrentItem() == null )
		{
			new PopUp( "You have not selected an observation!" , "Please select an observation." , JOptionPane.ERROR_MESSAGE ).start() ;
			throw new Exception( "No Item Selected" );
		}
		else if( ProgramTree.getSelectedItem() != null && DeferredProgramList.getCurrentItem() != null )
		{
			new PopUp( "You may only select one observation!" , "Please deselect an observation." , JOptionPane.ERROR_MESSAGE ).start() ;
			throw new Exception( "Multiple Items Selected" );
		}
		else if( DeferredProgramList.getCurrentItem() != null )
		{
			isDeferred = true;
		}
	}
    
	public void setDeferred( boolean deferred )
	{
		isDeferred = deferred ;
	}
	
	
	protected boolean chmod( File file )
	{
		boolean success = true ;
		try
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( "chmod 666" ) ;
			buffer.append( " " ) ;
			buffer.append( file.getAbsolutePath() ) ;
			String command = buffer.toString() ;
			Runtime.getRuntime().exec( command ) ;
			buffer = null ;
		}
		catch( IOException ioe )
		{
			logger.error( "Unable to change file access permissions " + file.getAbsolutePath() ) ;
			success = false ;
		}
		return success ;
	}
	
	public static String deferredDirPath()
	{
		if( deferredDirPath == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "telescope" ).toLowerCase() ) ;
			buffer.append( "data" ) ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "deferredDir" ) ) ; 
			buffer.append( File.separator ) ;
			deferredDirPath = buffer.toString() ;
			buffer = null ;
		}
		return deferredDirPath ;
	}

	private static File successFile = null ;
	
	public File successFile()
	{
		if( successFile == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( deferredDirPath() ) ;
			buffer.append( ".success-" ) ;
			buffer.append( random.nextLong() ) ;
			String filename = buffer.toString() ;
			successFile = new File( filename ) ;
			buffer = null ;
		}
		
		if( !successFile.exists() )
		{
			try
			{
				File parent = successFile.getParentFile() ;
				if( !parent.canWrite() )
					logger.warn( "Don't appear to be able to write to " + parent.getAbsolutePath() ) ;
				successFile.createNewFile() ;
				chmod( successFile ) ;
			}
			catch( IOException ioe )
			{
				logger.error( "Unable to create success file " + successFile.getAbsolutePath() ) ;
			}
		}
		return successFile ;
	}
	
	private static File failFile = null ;
	
	public File failFile()
	{
		if( failFile == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( deferredDirPath() ) ;
			buffer.append( ".failure-" ) ;
			buffer.append( random.nextLong() ) ;
			String filename = buffer.toString() ;
			failFile = new File( filename ) ;
			buffer = null ;
		}
		
		if( !failFile.exists() )
		{
			try
			{
				File parent = failFile.getParentFile() ;
				if( !parent.canWrite() )
					logger.warn( "Don't appear to be able to write to " + parent.getAbsolutePath() ) ;
				failFile.createNewFile() ;
				chmod( failFile ) ;
			}
			catch( IOException ioe )
			{
				logger.error( "Unable to create failure file " + failFile.getAbsolutePath() ) ;
			}
		}
		return failFile ;		
	}	
	
	protected int executeCommand( String command )
	{
		byte[] stdout = new byte[ 1024 ] ;
		byte[] stderr = new byte[ 1024 ] ;
		StringBuffer inputBuffer = new StringBuffer() ;
		StringBuffer errorBuffer = new StringBuffer() ;
		BufferedWriter errorWriter = null ;
		Runtime rt;
		int rtn = -1 ;
		try
		{
			rt = Runtime.getRuntime();	
			Process p = rt.exec( command );
			InputStream istream = p.getInputStream();
			InputStream estream = p.getErrorStream();
			int inputLength , errorLength ;
			boolean inputFinished = false ;
			boolean errorFinished = false ;
			inputBuffer.delete( 0 , inputBuffer.length() ) ;
			errorBuffer.delete( 0 , errorBuffer.length() ) ;
			while( !( inputFinished && errorFinished ) )
			{
				if( !inputFinished )
				{
					inputLength = istream.read( stdout ) ;
					if( inputLength == -1 )
						inputFinished = true ;
					else
						inputBuffer.append( new String( stdout ).trim() ) ;
				}
				
				if( !errorFinished )
				{
					errorLength = estream.read( stderr ) ;
					if( errorLength == -1 )
						errorFinished = true ;
					else
						errorBuffer.append( new String( stderr ).trim() ) ;
				}			
			}
			p.waitFor();
			rtn = p.exitValue();
			logger.info( command + " returned with exit status " + rtn );
			logger.debug( "Output from " + command + ": " + inputBuffer.toString() ) ;
			logger.debug( "Error from " + command + ": " + errorBuffer.toString() ) ;
			errorWriter = new BufferedWriter( new FileWriter( failFile() ) ) ;
			errorWriter.write( errorBuffer.toString() ) ;
			errorWriter.newLine() ;
			errorWriter.flush() ;
			errorWriter.close() ;
		}
		catch( IOException ioe )
		{
			logger.error( "Error executing ..." , ioe ) ;
		}
		catch( InterruptedException ie )
		{
			logger.error( "Exited prematurely..." , ie ) ;
		}

		return rtn ;
	}

    public class PopUp extends Thread implements Serializable
	{
		String _message;

		String _title;

		int _errLevel;

		public PopUp( String title , String message , int errorLevel )
		{
			_message = message;
			_title = title;
			_errLevel = errorLevel;
		}

		public void run()
		{
			JOptionPane.showMessageDialog( null , _message , _title , _errLevel );
		}
	}
	
}
