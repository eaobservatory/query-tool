package edu.jach.qt.djava;

import au.gov.aao.drama.*;
import ocs.utils.CommandReceiver;
import org.apache.log4j.Logger;

/**
 * <code>CSOPathResponseHandler</code> This class is used to
 * handle responses to the GetPath() method.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$ */
public class CSOPathResponseHandler extends DramaPath.ResponseHandler {

  static Logger logger = Logger.getRootLogger();

  private CommandReceiver cr;
  public CSOPathResponseHandler(DramaPath p, CommandReceiver cr) {
    super(p);
    this.cr = cr;
    logger.debug(logger.getClass().getName());
  }

  /** 
   * Sucess is invoked when we have completed the get path operation.
   */
  public boolean Success(DramaPath path, DramaTask task) throws DramaException {
    
    // Informational message
    //task.MsgOut("Got path to task "+path.TaskName() +".");
    logger.info("Got path to task "+path.TaskName() +".");
    
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
    logger.warn("Failed to get path to task \"" + path + "\"");
    logger.warn("Failed with status - " + status);

    cr.setPathLock(false);

    return false;
  }

}

/*
 * $Log$
 * Revision 1.6  2002/04/20 02:52:17  mrippa
 * Updated Log
 *
 * Revision 1.5  2002/04/20 02:41:24  mrippa
 * Added log4j functionality.
 *
 * Revision 1.4  2002/04/01 21:55:37  mrippa
 * Modified the setTaskLock() name to setPathLock()
 *
 * Revision 1.3  2002/03/07 20:27:43  mrippa
 * Unlock the CommandReceiver on error.!
 *
 * Revision 1.2  2002/03/05 22:12:10  mrippa
 * Provides CommanderReceiver to callback to.
 *
 * Revision 1.1  2002/02/24 06:55:06  mrippa
 * Added Drama support for monitoring tau values.
 *
 */
