package edu.jach.qt.utils;

import gemini.sp.SpTreeMan ;
import gemini.sp.SpObs ;
import gemini.sp.SpItem ;
import gemini.sp.SpTranslationNotSupportedException ;

import java.io.FileInputStream ;
import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.FileWriter ;
import java.io.InputStreamReader ;

import java.util.Properties ;
import java.util.Calendar ;
import java.util.GregorianCalendar ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.TimeZone ;

import java.text.SimpleDateFormat ;

import org.apache.log4j.Logger ;

import gemini.util.ConfigWriter ;


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
    Wrapper around the translator method which generates XML for the
    new queue processing.  The file will be stored in the same location
    as the execs for now.

    @param SpItem  and MSB
    @return String a filename
    */
  public static String createQueueXML( SpItem item ) {
      // We are going to take some shortcuts, like assuming
      // the telescope is UKIRT

      // File will go into exec path and be called
      // ukirt_yyyymmddThhmmss.xml
      String opDir = System.getProperty("EXEC_PATH");
      if ( "false".equalsIgnoreCase(System.getProperty("DRAMA_ENABLED"))) {
          opDir = System.getProperty("user.home");
      }
      Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
      StringBuffer fileName = new StringBuffer (sdf.format(cal.getTime()));
      fileName.append(".xml");
      fileName.insert(0,"/" );
      fileName.insert(0, opDir);
      System.out.println("QUEUE filename is " + fileName.toString());

      try {
          FileWriter fw = new FileWriter (fileName.toString());
	  fw.write ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
          fw.write ("<QueueEntries  telescope=\"UKIRT\">\n");

          // Now we need to get (a) the sequence for each obs in the MSB and
          // (b) the estimated duration of the obs and (c) the instrument name
          // for the obs.
          Vector obs = SpTreeMan.findAllItems(item, "gemini.sp.SpObs");
          Enumeration e = obs.elements();
          while ( e.hasMoreElements() ) {
	     SpObs currentObs = (SpObs) e.nextElement();
	     String inst = SpTreeMan.findInstrument(currentObs).type().getReadable();
	     double time = currentObs.getElapsedTime();
	     fw.write("  <Entry totalDuration=\"" + time + "\"  instrument=\"" + inst +"\">\n");
             // if we are using the old translator, we need to add the exec path, otherwise we don't
             String tName = translate((SpItem)currentObs, inst);
             if ( tName != null && tName.indexOf(System.getProperty("EXEC_PATH")) == -1 ) {
                 fw.write("    " + System.getProperty("EXEC_PATH") + "/" + tName + "\n");
             }
             else {
                 fw.write("    " + tName + "\n");
             }
	     fw.write("  </Entry>\n");
          }
          // Close off the entry
          fw.write("</QueueEntries>\n");
          fw.close();
      }
      catch (IOException ioe) {
	  String message = "Unable to write queue file " + fileName.toString();
	  logger.error(message, ioe);
	  fileName = new StringBuffer();
      }

      return fileName.toString();
  }


  /**
	 * String trans (SpItem observation) is a private method to translate an observation java object into an exec string and write it into a ascii file where is located in "EXEC_PATH" directory and has a name stored in "execFilename"
	 * 
	 * @param SpItem
	 *            observation
	 * @return String a filename
	 * 
	 */
	public static String translate( SpItem observation , String inst )
	{		
		String tname = null ;
		try
		{			
			if( observation == null )
				throw new NullPointerException( "Observation passed to translate() is null" ) ;

			SpObs spObs = null ;
			if( observation instanceof SpObs )
			{
				spObs = ( SpObs )observation ;
			}
			else
			{
				while( observation != null )
				{
					observation = observation.parent() ;
					if( ( observation != null ) && ( observation instanceof SpObs ) )
					{
						spObs = ( SpObs )observation ;
						break ;
					}
				}
			}
			if( spObs == null )
				throw new NullPointerException( "Observation passed to translate() not translatable" ) ;
			
			spObs.translate( new Vector() ) ;
		}
		catch( NullPointerException e )
		{
			logger.fatal( "Translation failed!, Missing value " + e );
		}
		catch( SpTranslationNotSupportedException sptnse )
		{
			logger.fatal( "Translation failed! " + sptnse );
		}
		FileWriter fw = null ;
		try
		{
			tname = ConfigWriter.getCurrentInstance().getExecName() ;
			logger.debug( "Translated file set to: " + tname );
			String fileProperty = new String( inst + "ExecFilename" ) ;
			Properties properties = System.getProperties() ;
			properties.put( fileProperty , tname ) ;
			logger.debug( "System property " + fileProperty + " now set to " + tname ) ;
			String tel = null ;
			if( properties.get( "telescope" ).equals( "Ukirt" ) )
				tel = "/ukirtdata" ;
			else
				tel = "/jcmtdata" ;
			fw = new FileWriter( tel + "/epics_data/smLogs/transFile" );
			fw.write( tname );
			fw.flush() ;
		}
		catch( IOException ioe )
		{
			logger.fatal( "Writing translated file name to transFile failed " , ioe );
		}
		catch( Exception e )
		{
			logger.fatal( "Translation failed!, exception was " + e , e );
		}
		finally
		{
			if( fw != null )
			{
				try
				{
					fw.close() ;
				}
				catch( IOException ioe )
				{
					logger.fatal( "Error closing file writer " , ioe );
				}
			}
			else
			{
				System.out.println( "File handle was null" ) ;
			}
		}
		return tname;
	}


  /**
	 * Loads a DRAMA task
	 * 
	 * @param name
	 *            a <code>String</code> value
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
