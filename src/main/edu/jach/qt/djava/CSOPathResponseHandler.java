package edu.jach.qt.djava;

import au.gov.aao.drama.*;
import ocs.utils.CommandReceiver;

/**
 * <code>CSOPathResponseHandler</code> This class is used to
 * handle responses to the GetPath() method.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$ */
public class CSOPathResponseHandler extends DramaPath.ResponseHandler {

  private CommandReceiver cr;
  public CSOPathResponseHandler(DramaPath p, CommandReceiver cr) {
    super(p);
    this.cr = cr;
  }

  /** 
   * Sucess is invoked when we have completed the get path operation.
   */
  public boolean Success(DramaPath path, DramaTask task) throws DramaException {
    
    // Informational message
    task.MsgOut("Got path to task "+path.TaskName() +".");
    
    // Start the monitor operation.
    DramaMonitor Monitor = new DramaMonitor(path, new CSO_MonResponse(cr), true, "CSOTAU");

    // We have sent a new message, so return true.
    return true;
  }

  /** 
   * Invoked if the GetPath operation fails
   */
  public boolean Error(DramaPath path, DramaTask task)  throws DramaException {
    DramaStatus status = task.GetEntStatus();
    DramaErs.Report("Failed to get path to task \"" + path + "\"");
    DramaErs.Report("Failed with status - " + status);

    cr.setPathLock(false);

    return false;
  }

}

