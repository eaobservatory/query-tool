package edu.jach.qt.utils;


// /* Gemini imports */
import gemini.sp.SpItem;

// /* ORAC imports */
import orac.util.*;

// /* Standard imports */
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

// /* Miscellaneous imports */
import org.apache.log4j.Logger;

/**
 * MsbClient.java
 *
 * This is a utility class used to send SOAP messages to the MsbServer.
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>,
 * modified by Martin Folger

 * $Id$ 
 */
public class MsbClient extends SoapClient {

  static Logger logger = Logger.getLogger(MsbClient.class);

  /**
   * <code>queryMSB</code>
   * Perform a query to the MsbServer with the given query
   * String. Success will return a true value and write the msbSummary
   * xml to file.  A unique file will be written for each user to allow
   * multiple users on th same machine.  This file should not be read by
   * the code.
   *
   * @param xmlQueryString a <code>String</code> value. The xml representing the query.
   * @return a <code>boolean</code> value indicating success.
   */
  public static boolean queryMSB(String xmlQueryString) {
      try {
	
	logger.debug("Sending queryMSB: "+xmlQueryString);
	URL url = new URL(System.getProperty("msbServer"));
	flushParameter();
	addParameter("xmlquery", String.class, xmlQueryString);

	String fileName = System.getProperty("msbSummary") + "." +
	    System.getProperty("user.name");

	FileWriter fw = new FileWriter(fileName);
	Object tmp = doCall(url, "urn:OMP::MSBServer", "queryMSB");

	if (tmp != null ) {
	  fw.write( (String)tmp );
	  fw.close();
	}

	else 
	  return false;
	 
      } catch (Exception e) {
	logger.error("queryMSB threw Exception", e);
	e.printStackTrace();
	return false;
      }
      return true;

  }

    /**
     * <code>queryCalibration</code>
     * Perform a query to the MsbServer with the special calibration query
     * String. Success will return a true value and write the msbSummary
     * xml to file.  A unique file will be written for each user to allow
     * multiple users on th same machine.  This file should not be read by
     * the code.
     *
     * @param xmlQueryString a <code>String</code> value. The xml representing the query.
     * @return a <code>String</code> value of the results in XML.
     */
    public static String queryCalibration(String xmlQueryString) {
	Object tmp;
	try {
	    logger.debug("Sending queryMSB: "+xmlQueryString);
	    URL url = new URL(System.getProperty("msbServer"));
	    flushParameter();
	    addParameter("xmlquery", String.class, xmlQueryString);
	    tmp = doCall(url, "urn:OMP::MSBServer", "queryMSB");	    
	}catch (Exception e) {return null;}
	return tmp.toString();
    }

  /**
   * <code>fetchMSB</code> Fetch the msb indicated by the msbid. The
   * SpItem will return null on failure. In the future this should
   * throw an exception instead.
   *
   * @param msbid an <code>Integer</code> value of the MSB
   * @return a <code>SpItem</code> value representing the MSB.
   */
  public static SpItem fetchMSB(Integer msbid) {

    SpItem spItem = null;
    try {
	
      logger.debug("Sending fetchMSB: "+msbid);
      URL url = new URL(System.getProperty("msbServer"));
      flushParameter();
      addParameter("key", Integer.class, msbid);
	
      FileWriter fw = new FileWriter(System.getProperty("msbFile")+"."+System.getProperty("user.name"));
      String spXML = (String) doCall(url, "urn:OMP::MSBServer", "fetchMSB");
	
      if (spXML != null ) {
	fw.write( (String)spXML );
	fw.close();
 	StringReader r = new StringReader(spXML);
	spItem = (SpItem)(new SpInputXML()).xmlToSpItem(r);
     }
	
      else 
	return spItem;
	
    } catch (Exception e) {
      logger.error("queryMSB threw Exception", e);
      e.printStackTrace();
      return spItem;
    }
      
    return spItem;
  }


    /**
     * Method to get the list of columns in an MSB Summary.  Requires the
     * <code>telescope</code> system parameter to be set.
     * @return      A string array of column names.
     */
    public static String [] getColumnNames () {
	String [] columns;
	try {
	    URL url = new URL(System.getProperty("msbServer"));
	    flushParameter();
	    addParameter("telescope", String.class, System.getProperty("telescope"));
	    Object o = doCall(url, "urn:OMP::MSBServer", "getResultColumns");
	    columns = (String [])o;
	}
	catch (Exception e) {
	    logger.error("getColumnNames threw exception", e);
	    columns = null;
	}
	return columns;
    }

    /**
     * Method to get the list of column types in an MSB Summary.  Requires the
     * <code>telescope</code> system parameter to be set.
     * @return      A string array of column types (eg Integer, String, etc).
     */
    public static String [] getColumnClasses () {
	String [] columns;
	try {
	    URL url = new URL(System.getProperty("msbServer"));
	    flushParameter();
	    addParameter("telescope", String.class, System.getProperty("telescope"));
	    Object o = doCall(url, "urn:OMP::MSBServer", "getTypeColumns");
	    columns = (String [])o;
	}
	catch (Exception e) {
	    logger.error("getColumnNames threw exception", e);
	    columns = null;
	}
	return columns;
    }


  /**
   * <code>doneMSB</code> Mark the given project ID as done in the database.
   *
   * @param projID a <code>String</code> the project ID.
   * @param checksum a <code>String</code> the checksum for this project.
   */
  public static void doneMSB(String projID, String checksum) {
    try {

      logger.debug("Sending doneMSB "+projID+ " "+checksum);

      URL url = new URL(System.getProperty("msbServer"));
      flushParameter();
      addParameter("projID", String.class, projID);
      addParameter("checksum", String.class, checksum);

      Object tmp = doCall(url, "urn:OMP::MSBServer", "doneMSB");

      if (tmp != null ) {
	// tmp has something with success
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }


  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String[]</code> value
   */
  public static void main(String[] args) {
    MsbClient.queryMSB("<Query><Moon>Dark</Moon></Query>");
    MsbClient.fetchMSB(new Integer(96));
  }

}
