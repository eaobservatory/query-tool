package edu.jach.qt.djava;

import au.gov.aao.drama.*;
import ocs.utils.CommandReceiver;
import org.apache.log4j.Logger;

/**
 * <code>MonitorResponse</code> is used to handle reponses to the
 * monitor messages created by the GetPath Success handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * @version $Id$ */
public abstract class MonitorResponse extends DramaMonitor.MonResponse {

  protected static Logger logger = Logger.getRootLogger();


  protected CommandReceiver cr;
  public final boolean DEBUG = "true".equals(System.getProperty("debug", "false"));

    /**
     * Constructor.
     * @param cr   CommandReceiver Object
     */
  public MonitorResponse(CommandReceiver cr) {
    super();
    this.cr = cr;
  }

  /**
   * Handles a sucessfull completion of the monitoring operation.
   * This should not actually be invoked in this example, as the
   * monitor is never cancelled.
   * @param monitor     A DramaMonitor Object
   * @param task        A DramaTask Object
   * @return            <code>false</code> always
   * @exception         DramaException if task fails.
   */
  public boolean SuccessCompletion(DramaMonitor monitor, DramaTask task )
    throws DramaException {
    task.MsgOut("Monitor SucessCompletion invoked");

    return false;
  }
                                          
  /**
   * Handles an error completion of the monitoring operation.
   * This may be invoked if the message to start monitoring fails
   * or if the task dies whilst we are monitoring it.
   * @param monitor     A DramaMonitor Object
   * @param task        A DramaTask Object
   * @return            <code>false</code> always
   * @exception         DramaException if task fails.
   */
  public boolean ErrorCompletion(DramaMonitor monitor, DramaTask task )
    throws DramaException {
    DramaStatus status = task.GetEntStatus();
    task.MsgOut("Monitor Completed with error - " + status);

    return false;
  }


  /**
   * This method is invoked when the monitor starts. It
   *  is used to set the GUI into its runtime state.
   * @param monitor     A DramaMonitor Object
   * @param task        A DramaTask Object
   * @return            <code>false</code> always
   * @exception         DramaException if task fails.
   */
  public void Started(DramaMonitor monitor, DramaTask task ) throws DramaException {
    task.MsgOut("CSO Parameter monitoring started");

    cr.setPathLock(false);
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
  public abstract void Changed(DramaMonitor monitor, DramaTask task, String name, Arg value)
    throws DramaException;
}
