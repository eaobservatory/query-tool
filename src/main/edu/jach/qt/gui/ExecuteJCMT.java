package edu.jach.qt.gui;

import edu.jach.qt.utils.HTMLViewer ;

import gemini.sp.SpItem ;

import java.io.File ;
import java.io.IOException ;
import java.io.FileWriter ;
import java.io.BufferedWriter ;
import java.io.InputStream ;
import java.io.Serializable ;

import javax.swing.JOptionPane ;

import org.apache.log4j.Logger;


/**
 * Implements the executable method for JCMT.  It simply sends either a 
 * single deferred observation, or an entire science project to the OCSQUEUE.
 * This is currently only usable for all OCSQUEUE CONFIG based observations.
 * @see edu.jach.qt.gui.Execute
 * Implements <code>Runnable</code>
 * @author $Author$
 * @version $Id$
 */
public class ExecuteJCMT extends Execute {

    static Logger logger = Logger.getLogger(ExecuteJCMT.class);
    private static SpItem _itemToExecute;
    private static String jcmtDir = File.separator +
	"jcmtdata" + File.separator + "orac_data";
    static boolean isRunning = false;
    static ExecuteJCMT _instance;
    
    /**
     * Constructor.
     * @param  item      The item to send to OCSQUEUE
     * @throws Exception From the base class.
     */
    private ExecuteJCMT() throws Exception {
    }

    public static ExecuteJCMT getInstance (SpItem item)
    {
	if (isRunning) {
	    logger.error("Already running");
	    return null;
	}
	try {
	    _instance = new ExecuteJCMT ();
	    _itemToExecute = item;
	}
	catch (Exception e) {
	    logger.error("Unable to construct");
	    return null;
	}
	return _instance;
    }

    public static boolean isRunning() {
	return isRunning;
    }

    /**
     * Implementation of the <code>Runnable</code> interface.
     * The success or failure of the file is determined by a
     * file called .success or .failure left in a defined directory
     * when the mothod ends.  Thus it is important to make sure that
     * when this method is run as a thread, the caller joins the thread.
     * If the item is a science project, it overwrites the current contents
     * of the queue.  If it is a deferred observation, it is inserted into
     * the queue at the next convinient point.
     * <bold>Note:</bold>  This method currently uses hard coded path
     * names for the files and for the commands to execute the queue.
     */
    public boolean run()
	{
		isRunning = true;
		// To execute JCMT, we write the execution to a file
		final File success = new File( "/jcmtdata/orac_data/deferred/.success" );
		final File failure = new File( "/jcmtdata/orac_data/deferred/.failure" );
		success.delete();
		failure.delete();
		try
		{
			success.createNewFile();
			failure.createNewFile();
			Runtime.getRuntime().exec( "chmod 666 /jcmtdata/orac_data/deferred/.success" );
			Runtime.getRuntime().exec( "chmod 666 /jcmtdata/orac_data/deferred/.failure" );
		}
		catch( IOException ioe )
		{
			logger.error( "Unable to create success/fail file" );
			isRunning = false;
			return true;
		}

		logger.info( "Executing observation " + _itemToExecute.getTitle() );
		final File file = new File( jcmtDir + File.separator + "ExecuteMe.xml" );
		try
		{
			final FileWriter writer = new FileWriter( file );
			writer.write( _itemToExecute.toXML() );
			writer.flush() ;
			writer.close();
			Runtime.getRuntime().exec( "chmod 666 " + file.getAbsolutePath() );
		}
		catch( IOException ioe )
		{
			logger.error( "Error writing translation file " + file.getAbsolutePath() );
			success.delete();
			isRunning = false;
			return true;
		}

		// Now send this file as an argument to the translate process
		final String translator = System.getProperty( "jcmtTranslator" );
		if( translator == null )
		{
			logger.error( "No translation process defined" );
			success.delete();
			isRunning = false;
			return true;
		}
		byte[] odfFile = new byte[ 1024 ];
		byte[] errorMessage = new byte[ 1024 ];
		Runtime rt;

		// Writer to add errors to log file before exiting
		BufferedWriter errorWriter = null;
		try
		{
			errorWriter = new BufferedWriter( new FileWriter( failure ) );
		}
		catch( IOException ioe )
		{
			logger.warn( "Unable to create error writer; messages will be logged but not displayed in warning" );
		}
		// Do the translation
		try
		{
			rt = Runtime.getRuntime();
			String command = translator + " " + file.getPath();
			logger.debug( "Running command " + command );
			Process p = rt.exec( command );
			InputStream istream = p.getInputStream();
			istream.read( odfFile );
			InputStream estream = p.getErrorStream();
			estream.read( errorMessage );
			int rtn = p.waitFor();
			logger.info( "Translator returned with exit status " + rtn );
			logger.debug( "Output from translator: " + new String( odfFile ).trim() );
			logger.debug( "Error from translator: " + new String( errorMessage ).trim() );
			if( rtn != 0 )
			{
				logger.error( "Returning with non-zero error status following translation" );
				if( errorWriter != null )
				{
					errorWriter.write( new String( errorMessage ).trim() );
					errorWriter.newLine();
					errorWriter.flush() ;
					errorWriter.close();
				}
				success.delete();
				isRunning = false;
				return true;
			}
		}
		catch( InterruptedException ie )
		{
			logger.error( "Translation exited prematurely..." , ie );
			success.delete();
			if( errorWriter != null )
			{
				try
				{
					errorWriter.write( new String( errorMessage ).trim() );
					errorWriter.newLine();
					errorWriter.flush() ;
					errorWriter.close();
				}
				catch( IOException ioe )
				{
					// If we can't write to the file, this doesn't matter since
					// it should go to the log anyway
				}
			}
			isRunning = false;
			return true;
		}
		catch( IOException ioe )
		{
			logger.error( "Error executing translator..." , ioe );
			success.delete();
			isRunning = false;
			return true;
		}

		if( TelescopeDataPanel.DRAMA_ENABLED )
		{
			try
			{
				String fName = new String( odfFile );
				fName = fName.trim();
				if( fName.toLowerCase().endsWith( "html" ) )
				{
					HTMLViewer viewer = new HTMLViewer( null , fName );
					failure.delete();
				}
				else
				{
					rt = Runtime.getRuntime();
					String command;
					if( super.isDeferred )
					{
						command = "/jac_sw/omp/QT/bin/insertOCSQUEUE.ksh " + new String( odfFile ).trim();
					}
					else
					{
						command = "/jac_sw/omp/QT/bin/loadUKIRT.ksh " + new String( odfFile ).trim();
					}
					logger.debug( "Running command " + command );
					Process p = rt.exec( command );
					InputStream istream = p.getInputStream();
					InputStream estream = p.getErrorStream();
					istream.read( odfFile );
					estream.read( errorMessage );
					p.waitFor();
					int rtn = p.exitValue();
					logger.info( "LoadOCSQUEUE returned with exit status " + rtn );
					logger.debug( "Output from LoadOCSQUEUE: " + new String( odfFile ).trim() );
					logger.debug( "Error from LoadOCSQUEUE: " + new String( errorMessage ).trim() );
					if( rtn != 0 )
					{
						logger.error( "Error loading queue" );
						if( errorWriter != null )
						{
							errorWriter.write( new String( errorMessage ).trim() );
							errorWriter.newLine();
							errorWriter.flush() ;
							errorWriter.close();
						}
						success.delete();
						isRunning = false;
						return true;
					}
				}
			}
			catch( IOException ioe )
			{
				logger.error( "Error executing LOADQ..." , ioe );
				success.delete();
				isRunning = false;
				return true;
			}
			catch( InterruptedException ie )
			{
				logger.error( "LOADQ exited prematurely..." , ie );
				if( errorWriter != null )
				{
					try
					{
						errorWriter.write( new String( errorMessage ).trim() );
						errorWriter.newLine();
						errorWriter.flush() ;
						errorWriter.close();
					}
					catch( IOException ioe )
					{
						// Do nothing; the message should be written to the log file anyway
					}
				}
				success.delete();
				isRunning = false;
				return true;
			}
		}
		isRunning = false;
		failure.delete();		
		
		return false;
	}

    public class PopUp extends Thread implements Serializable{
	String _message;
	String _title;
        int    _errLevel;
	public PopUp (String title, String message, int errorLevel) {
	    _message=message;
	    _title = title;
	    _errLevel=errorLevel;
	}

	public void run() {
	    JOptionPane.showMessageDialog(null,
					  _message,
					  _title,
					  _errLevel);
	}
    }

}
