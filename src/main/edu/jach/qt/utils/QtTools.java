package edu.jach.qt.utils;

import java.io.*;
import java.util.Properties;
import om.dramaSocket.ExecDtask;

/**
 * QtTools.java
 * A set  of static tools for the QT-OM
 *
 * Created: Wed Sep 19 11:09:17 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

public class QtTools {
   
  /** COPIED FROM ORAC3-OM suite:
      public static void loadConfig(String filename) is a private method
      reads a configuration file and set up configuration. It is done by
      putting things into java system properties which is program-system-wide visible.
       
      @param String args
      @return  none
      @throws none
  */
  public static void loadConfig(String filename) {
    try {
      String line_str;
      int line_number;
      FileInputStream is = new FileInputStream(filename);
      //DataInputStream ds = new DataInputStream(is); //  Deprecated readLine
      BufferedReader d = new BufferedReader(new InputStreamReader(is));

      Properties temp=System.getProperties();
      int lineno = 0;

      while ((line_str = d.readLine()) != null) {
	lineno++;
	if(line_str.length()>0) {
	  if(line_str.charAt(0)=='#') continue;

	  try {
	    int colonpos = line_str.indexOf(":");
	    temp.put(line_str.substring(0,colonpos).trim(),
		     line_str.substring(colonpos+1).trim());

	  }catch (IndexOutOfBoundsException e) {
	    System.out.println ("Problem reading line "+lineno+": "+line_str);
	    d.close();
	    is.close();
	    System.exit(1);
	  }
	}
      }
      d.close();
      is.close();

    } catch (IOException e) {
      System.out.println("File error: " + e);
    }
  }

  /**
   * Describe <code>execute</code> method here.
   *
   * @param someExec a <code>String[]</code> value
   */
  public static int execute(String[] someExec) {
    ExecDtask task = new ExecDtask(someExec);
    task.setWaitFor(true);

    // Set the output depending on whether it is requested in the cfg file.
    boolean debug = System.getProperty("SCR_MESS","OFF").equalsIgnoreCase("ON");
    task.setOutput(debug);
    task.run();

    // Check for errors from the script
    int status = task.getExitStatus();
    if (status != 0) {
      //errorBox = new ErrorBox ("Error reported by instrument startup script, code was: "+err);
      System.err.println("Error reported by instrument startup script, code was: "+status);
    }

    task.stop();
    try {
      task.join();
    }catch (InterruptedException e) {
      System.out.println ("Load task join interrupted! "+e.getMessage());
    }
    
    return status;
  }
 
  
}
