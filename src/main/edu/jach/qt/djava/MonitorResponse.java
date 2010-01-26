package edu.jach.qt.djava ;

import au.gov.aao.drama.DramaMonitor ;
import au.gov.aao.drama.DramaTask ;
import au.gov.aao.drama.Arg ;
import au.gov.aao.drama.DramaException ;
import au.gov.aao.drama.DramaStatus ;
import ocs.utils.CommandReceiver ;
import gemini.util.JACLogger ;

/**
 * <code>MonitorResponse</code> is used to handle reponses to the
 * monitor messages created by the GetPath Success handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * @version $Id$ */
public abstract class MonitorResponse extends DramaMonitor.MonResponse
{
	protected static JACLogger logger = JACLogger.getRootLogger() ;
	protected CommandReceiver cr ;
	public final boolean DEBUG = "true".equals( System.getProperty( "debug" , "false" ) ) ;

	/**
	 * Constructor.
	 * @param cr   CommandReceiver Object
	 */
	public MonitorResponse( CommandReceiver cr )
	{
		super() ;
		this.cr = cr ;
	}

	/**
	 * Handles a sucessfull completion of the monitoring operation. This should not actually be invoked in this example, as the monitor is never cancelled.
	 * 
	 * @param monitor
	 *            A DramaMonitor Object
	 * @param task
	 *            A DramaTask Object
	 * @return <code>false</code> always
	 * @exception DramaException
	 *                if task fails.
	 */
	public boolean SuccessCompletion( DramaMonitor monitor , DramaTask task ) throws DramaException
	{
		String statusMessage = "Monitor SucessCompletion invoked" ;
		task.MsgOut( statusMessage ) ;
		logger.info( statusMessage ) ;
		return false ;
	}

	/**
	 * Handles an error completion of the monitoring operation. This may be invoked if the message to start monitoring fails or if the task dies whilst we are monitoring it.
	 * 
	 * @param monitor
	 *            A DramaMonitor Object
	 * @param task
	 *            A DramaTask Object
	 * @return <code>false</code> always
	 * @exception DramaException
	 *                if task fails.
	 */
	public boolean ErrorCompletion( DramaMonitor monitor , DramaTask task ) throws DramaException
	{
		DramaStatus status = task.GetEntStatus() ;
		String statusMessage = "Monitor Completed with error - " + status ;
		task.MsgOut( statusMessage ) ;
		logger.error( statusMessage ) ;
		return false ;
	}

	/**
	 * This method is invoked when the monitor starts. It is used to set the GUI into its runtime state.
	 * 
	 * @param monitor
	 *            A DramaMonitor Object
	 * @param task
	 *            A DramaTask Object
	 * @return <code>false</code> always
	 * @exception DramaException
	 *                if task fails.
	 */
	public void Started( DramaMonitor monitor , DramaTask task ) throws DramaException
	{
		String statusMessage = "CSO Parameter monitoring started" ;
		task.MsgOut( statusMessage ) ;
		logger.info( statusMessage ) ;
		cr.setPathLock( false ) ;
	}

	/**
	 * This function is invoked when a monitored parameter changes.
	 *  This is the core of parameter monitoring.
	 * @param monitor     A DramaMonitor Object
	 * @param task        A DramaTask Object
	 * @param name        Name of the parameter to monitor
	 * @param value       Value of the monitored parameter
	 * @exception         DramaException if task fail
	 */
	public abstract void Changed( DramaMonitor monitor , DramaTask task , String name , Arg value ) throws DramaException ;
}
