package edu.jach.qt.gui;

import edu.jach.qt.utils.HTMLViewer;

import gemini.sp.SpItem;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import gemini.sp.SpMSB;
import edu.jach.qt.utils.SpQueuedMap;
import edu.jach.qt.utils.FileUtils ;

/**
 * Implements the executable method for JCMT.  It simply sends either a 
 * single deferred observation, or an entire science project to the OCSQUEUE.
 * This is currently only usable for all OCSQUEUE CONFIG based observations.
 * @see edu.jach.qt.gui.Execute
 * Implements <code>Runnable</code>
 * @author $Author$
 * @version $Id$
 */
public class ExecuteJCMT extends Execute
{
	static Logger logger = Logger.getLogger( ExecuteJCMT.class );
	private static SpItem _itemToExecute;
	private static String jcmtDir = null;
	static boolean isRunning = false;
	private static ExecuteJCMT _instance;

	/**
	 * Constructor.
	 * @param  item      The item to send to OCSQUEUE
	 * @throws Exception From the base class.
	 */
	private ExecuteJCMT() throws Exception{}

	public static synchronized ExecuteJCMT getInstance( SpItem item )
	{
		try
		{
			if( _instance == null )
				_instance = new ExecuteJCMT();
			_itemToExecute = item;
		}
		catch( Exception e )
		{
			logger.error( "Unable to construct" );
			return null;
		}
		if( isRunning )
		{
			logger.error( "Already running" );
			return null;
		}
		return _instance;
	}

	public static boolean isRunning()
	{
		return isRunning;
	}

	private String jcmtDir()
	{
		if( jcmtDir == null )
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append( File.separator );
			buffer.append( "jcmtdata" );
			buffer.append( File.separator );
			buffer.append( "orac_data" );
			jcmtDir = buffer.toString();
			buffer = null;
		}
		return jcmtDir;
	}

	private byte[] translate( File file )
	{
		byte[] stdout = new byte[ 1024 ];
		final String translator = System.getProperty( "jcmtTranslator" );
		if( translator != null )
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append( translator );
			buffer.append( " " );
			buffer.append( file.getPath() );
			String command = buffer.toString();
			buffer = null;
			int rtn = executeCommand( command , stdout );
			if( rtn != 0 )
				stdout = null;
		}
		else
		{
			logger.error( "No translation process defined" );
			stdout = null;
		}
		file.delete();
		return stdout;
	}

	private boolean sendToQueue( byte[] stdout )
	{
		boolean failure = false;

		if( stdout == null )
			stdout = new byte[ 0 ];
		String fileName = new String( stdout );
		fileName = fileName.trim(); // clear up any whitespace that might give us a false positive
		String[] split = fileName.split( "\n" );

		fileName = split[ split.length - 1 ]; // Tim assures me it *should* be the last entry
		fileName = fileName.trim(); // clean up line

		File file = new File( fileName );
		boolean fileAvailable = file.exists() && file.canRead();

		if( fileAvailable && TelescopeDataPanel.DRAMA_ENABLED )
		{
			if( fileName.toLowerCase().endsWith( "html" ) )
			{
				HTMLViewer viewer = new HTMLViewer( null , fileName );
			}
			else
			{
				String command;
				StringBuffer buffer = new StringBuffer();
				buffer.append( "/jac_sw/omp/QT/bin/" );
				if( super.isDeferred )
					buffer.append( "insertJCMTQUEUE.ksh" );
				else
					buffer.append( "loadJCMT.ksh" );
				buffer.append( " " );
				buffer.append( fileName );
				command = buffer.toString();
				buffer = null;
				logger.debug( "Running command " + command );
				int rtn = executeCommand( command , null );
				if( rtn != 0 )
					failure = true;
				if( failure )
					logger.error( "Problem sending to queue" );
			}
		}
		else
		{
			if( fileAvailable )
				logger.info( "DRAMA not enabled" );
			else
				logger.error( "The following file does not appear to be available : " + file.getAbsolutePath() );
			failure = true;
		}

		if( _itemToExecute != null && !failure )
		{
			SpItem obs = _itemToExecute;
			SpItem child = _itemToExecute.child();
			if( child instanceof SpMSB )
				obs = child;
			SpQueuedMap.getSpQueuedMap().putSpItem( obs );
		}
		return failure;
	}

	private File convertProgramToXML()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( jcmtDir() );
		buffer.append( File.separator );
		buffer.append( "Execute-" );
		buffer.append( nextRandom() );
		buffer.append( ".xml" );
		String filename = buffer.toString();
		buffer = null;
		File file = new File( filename );
		try
		{
			final FileWriter writer = new FileWriter( file );
			writer.write( _itemToExecute.toXML() );
			writer.flush();
			writer.close();
			FileUtils.chmod( file );
		}
		catch( IOException ioe )
		{
			logger.error( "Error writing translation file " + file.getAbsolutePath() );
			file = null;
		}
		return file;
	}

	/**
	 * Implementation of the <code>Runnable</code> interface.
	 * The success or failure of the file is determined by a
	 * file called .success or .failure left in a defined directory
	 * when the method ends.  Thus it is important to make sure that
	 * when this method is run as a thread, the caller joins the thread.
	 * 
	 * If the item is a science project, it overwrites the current contents
	 * of the queue.  If it is a deferred observation, it is inserted into
	 * the queue at the next convinient point.
	 * <bold>Note:</bold>  This method currently uses hard coded path
	 * names for the files and for the commands to execute the queue.
	 */
	public boolean run()
	{
		isRunning = true;

		logger.info( "Executing observation " + _itemToExecute.getTitle() );

		File XMLFile = null;
		byte[] stdout = null;
		boolean failure = false;

		resetCheckpoint();
		checkpoint( "Converting to XML" );

		XMLFile = convertProgramToXML();

		checkpoint( "XML" );
		checkpoint( "Translating" );

		if( XMLFile != null )
			stdout = translate( XMLFile );
		else
			failure = true;

		checkpoint( "Translated" );
		checkpoint( "Sending to queue" );

		if( stdout != null )
			failure = sendToQueue( stdout );
		else
			failure = true;

		checkpoint( "Sent to queue" );
		checkpoint( "Success ? " + !failure );

		isRunning = false;

		if( failure )
			successFile().delete();
		else
			failFile().delete();

		return failure;
	}
}
