package edu.jach.qt.djava;

import au.gov.aao.drama.*;
import ocs.utils.CommandReceiver;

/**
 * <code>MonitorResponse</code> is used to handle reponses to the
 * monitor messages created by the GetPath Success handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$ */
public abstract class MonitorResponse extends DramaMonitor.MonResponse {

  protected CommandReceiver cr;
  public final boolean DEBUG = "true".equals(System.getProperty("debug", "false"));

  public MonitorResponse(CommandReceiver cr) {
    super();
    this.cr = cr;
  }

  /**Handles a sucessfull completion of the monitoring operation.
   * This should not actually be invoked in this example, as the
   * monitor is never cancelled.
   */
  public boolean SuccessCompletion(DramaMonitor monitor, DramaTask task )
    throws DramaException {
    task.MsgOut("Monitor SucessCompletion invoked");

    return false;
  }
                                          
  /** Handles an error completion of the monitoring operation.
   * This may be invoked if the message to start monitoring fails
   * or if the task dies whilst we are monitoring it.
   */
  public boolean ErrorCompletion(DramaMonitor monitor, DramaTask task )
    throws DramaException {
    DramaStatus status = task.GetEntStatus();
    task.MsgOut("Monitor Completed with error - " + status);

    return false;
  }


  /** This method is invoked when the monitor starts. It
   *  is used to set the GUI into its runtime state.
   */
  public void Started(DramaMonitor monitor, DramaTask task ) throws DramaException {
    task.MsgOut("CSO Parameter monitoring started");

    cr.setPathLock(false);
  }
                                             
  /** This function is invoked when a monitored parameter changes.
   *  This is the core of parameter monitoring.
   */
  public abstract void Changed(DramaMonitor monitor, DramaTask task, String name, Arg value)
    throws DramaException;
}
