package edu.jach.qt.utils ;

import java.io.InputStreamReader ;
import java.io.BufferedReader ;
import java.io.IOException ;
import org.apache.log4j.Logger ;

/**
 * ExecDtask is for staring a drama task to the client connection it
 * has one constructor only. This is a platform-dependent class
 * running under Solaris

 * @author M.Tan@roe.ac.uk
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class ExecDtask extends Thread
{
	static Logger logger = Logger.getLogger( ExecDtask.class ) ;

	/**
	 public ExecDtask(String task) is
	 the class constructor. The class has only one constructor so far.
	 the two things are done during the construction.
	 */
	public ExecDtask( String[] task )
	{
		_task = task ;
		temp = Runtime.getRuntime() ;
		this.setPriority( NORM_PRIORITY - 1 ) ;
	}

	/**
	 public void run() is a public method
	 it starts an external process on a Solaris machine
	 This process is a drama task for the ORAC OM in our case

	 @param none
	 @return none
	 @throws IOException
	 */
	public void run()
	{
		try
		{
			p = temp.exec( _task ) ;

			// add in new features to control task execution redirect output from a Java "shell"to a terminal screen via a separate thread.  
			// This seems to work!
			if( output )
			{
				et = new Thread()
				{
					BufferedReader stdError = new BufferedReader( new InputStreamReader( p.getErrorStream() ) ) ;

					public void run()
					{
						String s = null ;

						logger.info( "Stderr Thread started for: " + getTaskString() ) ;
						this.setPriority( MAX_PRIORITY ) ;
						try
						{
							while( ( s = stdError.readLine() ) != null )
							{
								logger.error( s ) ;
								yield() ;
							}
						}
						catch( IOException e )
						{
							logger.error( "Stderr got IO exception:" + e.getMessage() ) ;
						}
						logger.info( "Stderr Thread completed for: " + getTaskString() ) ;
					}
				} ;

				pt = new Thread()
				{
					BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ) ) ;

					public void run()
					{
						String s = null ;

						logger.info( "Stdout Thread started for: " + getTaskString() ) ;
						this.setPriority( MAX_PRIORITY ) ;
						try
						{
							while( ( s = stdInput.readLine() ) != null )
							{
								logger.info( s ) ;
								yield() ;
							}
						}
						catch( IOException e )
						{
							logger.error( "Stdout got IO exception:" + e.getMessage() ) ;
						}
						logger.info( "Stdout Thread completed for: " + getTaskString() ) ;
					}
				} ;

				pt.start() ;
				et.start() ;
			}
			// See if we need to wait for completion, and if so also get the completion status. Added by AB 17-Apr-00
			if( waitFor )
			{
				try
				{
					p.waitFor() ;
					exitValue = p.exitValue() ;
					System.out.println( "Exit value was " + exitValue ) ;
					p.destroy() ;
				}
				catch( InterruptedException e )
				{
					System.out.println( "Load process was interrupted!" ) ;
				}
			}
		}
		catch( IOException e )
		{
			new ErrorBox( _task[ 0 ] + " has failed to start due to: " + e.toString() + "\n" ) ;
		}
		catch( Exception e )
		{
			new ErrorBox( _task[ 0 ] + " has failed to start due to: " + e.toString() + "\n" ) ;
		}
	}

	/**
	 public Process getProcess () is a public method
	 it returns a process object
	 This process is a drama task for the ORAC OM in our case

	 @param none
	 @return Process
	 @throws IOException
	 */
	public Process getProcess()
	{
		return p ;
	}

	public int getExitStatus()
	{
		return exitValue ;
	}

	public void setOutput( boolean o )
	{
		output = o ;
	}

	public void setWaitFor( boolean w )
	{
		waitFor = w ;
	}

	private String getTaskString()
	{
		String argList = "" ;

		for( int i = 0 ; i < _task.length ; i++ )
			argList += ( " " + _task[ i ] ) ;

		return argList ;
	}

	private String[] _task ;
	private Process p ;
	private Runtime temp ;
	private boolean output = true ;
	private boolean waitFor = true ;
	private int exitValue = 0 ;
	private Thread pt ;
	private Thread et ;
}
