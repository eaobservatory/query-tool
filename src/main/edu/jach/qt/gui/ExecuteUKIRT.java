package edu.jach.qt.gui;

import edu.jach.qt.utils.QtTools ;
import gemini.sp.SpItem ;
import gemini.sp.SpTreeMan ;
import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.FileWriter ;
import java.io.BufferedWriter ;
import java.io.Serializable ;
import javax.swing.JOptionPane ;
import org.apache.log4j.Logger;

import gemini.sp.SpMSB ;
import edu.jach.qt.utils.SpQueuedMap ;


public class ExecuteUKIRT extends Execute implements Runnable {

    private static final Logger logger = Logger.getLogger( ExecuteUKIRT.class ) ;

    private boolean _useQueue;

    
    public ExecuteUKIRT(boolean useQueue) throws Exception {
	super();
	_useQueue = useQueue;
    };

    public void run()
	{
		System.out.println( "Starting execution..." );
		String deferredDirectory = System.getProperty( "deferredDirectory" ) ;
		final File success = new File( deferredDirectory + File.separator + ".success" );
		final File failure = new File( deferredDirectory + File.separator + ".failure" );
		success.delete();
		failure.delete();
		try
		{
			success.createNewFile();
			failure.createNewFile();
		}
		catch( IOException ioe )
		{
			if( TelescopeDataPanel.DRAMA_ENABLED )
			{
				logger.error( "Unable to create success/fail file" , ioe );
			}
			// return;
		}

		SpItem itemToExecute;
		if( isDeferred )
		{
			itemToExecute = DeferredProgramList.getCurrentItem() ;
			logger.info( "Executing observation from deferred list" );
		}
		else
		{
			if( _useQueue )
				itemToExecute = ProgramTree.getCurrentItem() ;
			else
				itemToExecute = ProgramTree.selectedItem ;
			logger.info( "Executing observation from Program List" );
		}		

		if( itemToExecute != null )
		{ 
			SpItem obs = itemToExecute ;
			SpItem child = itemToExecute.child() ;
			if( child instanceof SpMSB )
				obs = child ;
			SpQueuedMap.getSpQueuedMap().putSpItem( obs ) ;
		}
		
		String tname;
		if( _useQueue )
		{
			tname = QtTools.createQueueXML( itemToExecute );
		}
		else
		{
			SpItem inst = ( SpItem ) SpTreeMan.findInstrument( itemToExecute );
			if( inst == null )
			{
				logger.error( "No instrument found" );
				success.delete();
				return;
			}

			tname = QtTools.translate( itemToExecute , inst.type().getReadable() );
		}
		
		// Catch null sequence names - probably means translation
		// failed:
		if( tname == null )
		{
			logger.error( "Translation failed. Please report this!" );
			success.delete();
			return;
		}
		else
		{
			logger.info( "Trans OK" );
			logger.debug( "Translated file is " + tname );
		}

		BufferedWriter errorWriter = null;
		try
		{
			errorWriter = new BufferedWriter( new FileWriter( failure ) );
		}
		catch( IOException ioe )
		{
			logger.warn( "Unable to create error writer; messages will be logged but not displayed in warning" );
		}

		// Having successfully run through translation, now try
		// to submit the file to the ukirt instrument task
		if( TelescopeDataPanel.DRAMA_ENABLED )
		{
			final byte[] stdout = new byte[ 1024 ];
			final byte[] stderr = new byte[ 1024 ];
			try
			{
				Runtime rt = Runtime.getRuntime();
				String command;
				if( super.isDeferred )
				{
					command = "/jac_sw/omp/QT/bin/insertOCSQUEUE.ksh " + tname;
				}
				else
				{
					command = "/jac_sw/omp/QT/bin/loadUKIRT.ksh " + tname;
				}
				logger.debug( "Running command " + command );
				Process p = rt.exec( command );
				InputStream istream = p.getInputStream();
				InputStream estream = p.getErrorStream();
				istream.read( stdout );
				estream.read( stderr );
				p.waitFor();
				int rtn = p.exitValue();
				logger.info( command + " returned a value of " + rtn );
				logger.debug( "Output from " + command + ": " + new String( stdout ).trim() );
				if( rtn != 0 )
				{
					logger.error( "Error loading UKIRT task" );
					logger.error( new String( stderr ).trim() );
					if( errorWriter != null )
					{
						errorWriter.write( new String( stderr ).trim() );
						errorWriter.newLine();
						errorWriter.close();
					}
					success.delete();
					return;
				}
			}
			catch( IOException ioe )
			{
				logger.error( "Error executing loadUKIRT..." , ioe );
				logger.error( new String( stderr ).trim() );
				success.delete();
				return;
			}
			catch( InterruptedException ie )
			{
				logger.error( "loadUKIRT exited prematurely..." , ie );
				if( errorWriter != null )
				{
					try
					{
						errorWriter.write( new String( stderr ).trim() );
						errorWriter.newLine();
						errorWriter.close();
					}
					catch( IOException ioe )
					{
						// Do nothing since the message should be in th elog anyway
					}
				}
				success.delete();
				return;
			}
			catch( Exception ex )
			{
				logger.error( "Got an unknown exception when running loadUKIRT..." , ex );
				if( errorWriter != null )
				{
					try
					{
						errorWriter.write( new String( stderr ).trim() );
						errorWriter.newLine();
						errorWriter.close();
					}
					catch( IOException ioe )
					{
						// Do nothing since the message should be in the log anyway
					}
				}
				success.delete();
			}
		}
		failure.delete();
		return;
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
