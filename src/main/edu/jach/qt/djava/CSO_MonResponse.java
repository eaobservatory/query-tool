package edu.jach.qt.djava;

import au.gov.aao.drama.*;
import edu.jach.qt.gui.TelescopeDataPanel;

/**
 * <code>CSO_MonResponse</code> This class is used to handle
 * reponses to the monitor messages created by the GetPath Success
 * handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$ */
public class CSO_MonResponse extends MonitorResponse {

  public CSO_MonResponse() {
    super();
  }

  // This function is invoked when a monitored parameter changes.
  //  This is the core of parameter monitoring.
  public void Changed(DramaMonitor monitor, DramaTask task, String name, Arg value)
    throws DramaException {

    if (name.equals("CSOTAU")) {
      
      System.out.println(value.RealValue(name));
      
      TelescopeDataPanel.setTau(value.RealValue(name));
    }
    
  }
}
