package edu.jach.qt.utils;


import gemini.sp.*;
import gemini.sp.obsComp.*;

import jsky.app.ot.*;

import java.io.*;
import java.util.Properties;
//import om.util.ExecDtask;
import orac.ukirt.util.SpTranslator;
import org.apache.log4j.Logger;

import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;


/***
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

  public static final int OOS_STATE_UNKNOWN	= -1;
  public static final int OOS_STATE_IDLE	= 0;
  public static final int OOS_STATE_STOPPED	= 1;
  public static final int OOS_STATE_PAUSED	= 2;
  public static final int OOS_STATE_RUNNING	= 3;
  public static final int OOS_ACTIVE		= 4;
  public static final int OOS_INACTIVE		= 5;
  
  static Logger logger = Logger.getLogger(QtTools.class);

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
	    logger.fatal("Problem reading line "+lineno+": "+line_str);
	    d.close();
	    is.close();
	    System.exit(1);
	  }
	}
      }
      d.close();
      is.close();

    } catch (IOException e) {
      logger.error("File error: " + e);
    }
  }

  /**
   * Execute a DRAMA command
   *
   * @param someExec a <code>String[]</code> value
   */
  public static int execute(String[] cmd, boolean wait, boolean output) {

 
    ExecDtask task = new ExecDtask(cmd);
    task.setWaitFor(wait);

    // Set the output depending on whether it is requested in the cfg file.
    //boolean debug = System.getProperty("SCR_MESS","OFF").equalsIgnoreCase("ON");
    task.setOutput(output);
    task.run();

    // Check for errors from the script
    int status = task.getExitStatus();
    if (status != 0) {
      //errorBox = new ErrorBox ("Error reported by instrument startup script, code was: "+err);
      logger.error("Error reported by instrument startup script, code was: "+status);
    }

    task.stop();
    try {
      task.join();
    }catch (InterruptedException e) {
      logger.error("Load task join interrupted! "+e.getMessage());
    }
    
    return status;
  }
 

  /**
     String trans (SpItem observation) is a private method
     to translate an observation java object into an exec string
     and write it into a ascii file where is located in "EXEC_PATH"
     directory and has a name stored in "execFilename"
     
     @param SpItem observation
     @return  String a filename
     
  */
  public static String translate(SpItem observation, String inst) {
    SpTranslator spt = new SpTranslator((SpObs)observation);
    spt.setSequenceDirectory(System.getProperty("EXEC_PATH"));
    spt.setConfigDirectory(System.getProperty("CONF_PATH"));
      
    Properties temp = System.getProperties();
    String tname = null;
    String fileProperty = new String(inst+"ExecFilename");

    try {
      String tel = null;
      if ( System.getProperties().get("telescope") . equals("Ukirt")) {
	tel = "/ukirtdata";
      } else {
	tel = "/jcmtdata";
      } 
      
      FileWriter fw = new FileWriter(tel+"/epics_data/smLogs/transFile");

      tname=spt.translate();
      logger.debug("Translated file set to: "+System.getProperty("EXEC_PATH")+"/"+tname);

      temp.put(fileProperty,tname);
      fw.write(tname);
      fw.close();
      logger.debug("System property "+fileProperty+" now set to "+
		   System.getProperty("EXEC_PATH")+"/"+tname);
      
    } catch (NullPointerException e) {
      logger.fatal("Translation failed!, exception was "+e);
    } catch ( IOException ioe) {
      logger.fatal("Writting translated file name to transFile failed ", ioe);
    } catch (Exception e) {
      logger.fatal("Translation failed!, Missing value "+e);
    }
    return tname;
  }


  /**
   * Loads a DRAMA task
   *
   * @param name a <code>String</code> value
   */
  public static int loadDramaTasks(String name) {
    //starting the drama tasks
    String[] script = new String[6];
      
    script[0] = System.getProperty("LOAD_DHSC");
    script[1] = new String(name);
    script[2] = "-"+System.getProperty("QUICKLOOK", "noql");
    script[3] = "-"+System.getProperty("SIMULATE","simTel");
    script[4] = "-"+System.getProperty("ENGINEERING","eng");
    script[5] = "-omp";
    
    logger.info("About to start script "
		+script[0]
		+			" "+script[1] 
		+			" "+script[2] 
		+			" "+script[3] 
		+                   " "+script[4]
		+                   " "+script[5]
		);

    int status = QtTools.execute(script, false, true);

    return status;
  }


  public static int oosTest(String[] cmd) {
      
    String argList = "";
    for (int  i = 0; i<cmd.length; i++) {
      argList += (" " +cmd[i]);
    }
    logger.debug("oosTest argList is: "+argList);
	    
    int status = -1;
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader stdOut = 
	new BufferedReader(new InputStreamReader(p.getInputStream()));

      String s = null;
      while ((s = stdOut.readLine()) != null) {
	System.err.println(s);
	logger.debug("oosTest output: >>>"+s+"<<<");
	if ( s.endsWith("IS INACTIVE")) {
	  status = QtTools.OOS_INACTIVE;
	}
	else if ( s.endsWith("IS ACTIVE")) {
	  status = QtTools.OOS_ACTIVE;
	}
	else if ( s.endsWith("Idle ")) {
	  status = QtTools.OOS_STATE_IDLE;
	}
	else if ( s.endsWith("Stopped ")) {
	  status = QtTools.OOS_STATE_STOPPED;
	}
	else if ( s.endsWith("Paused ")) {
	  status = QtTools.OOS_STATE_PAUSED;
	}
	else if ( s.endsWith("Running ")) {
	  status = QtTools.OOS_STATE_RUNNING;
	}
	else {
	  logger.error("UNKNOWN OOS STATE.");
	  status = QtTools.OOS_STATE_UNKNOWN;
	} 
	
      }

      int rtn = p.waitFor();

    } 
    catch (IOException e) {
      logger.error("StdOut got IO exception:"+e.getMessage());
    }
    catch (InterruptedException ie) {
	logger.error("oosTest process was interrupted abnormally.", ie);
    }

    return status;
  }

}
