package edu.jach.qt.utils;


import java.io.*;
import java.net.*;
import gemini.sp.SpItem;
import gemini.sp.SpProg;
import orac.util.SpItemDOM;
import orac.util.SpItemUtilities;

import java.lang.reflect.*;
import orac.util.*;

/**
 * MsbClient.java
 *
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author Mathew Rippa, modified by Martin Folger
 * @version
 */
public class MsbClient extends SoapClient {

   public static void main(String[] args) {
      MsbClient.queryMSB("<Query><Moon>Dark</Moon></Query>");
      MsbClient.fetchMSB(new Integer(96));
   }

   public static boolean queryMSB(String xmlQueryString) {
      try {
	 URL url = new URL(System.getProperty("msbServer"));
	 addParameter("xmlquery", String.class, xmlQueryString);
	 //addParameter("maxreturn", Integer.class, new Integer(2));
	 //System.out.println(doCall(url, "urn:OMP::MSBServer", "queryMSB"));

	 FileWriter fw = new FileWriter(System.getProperty("msbSummary"));
	 Object tmp = doCall(url, "urn:OMP::MSBServer", "queryMSB");

	 if (tmp != null ) {

	   fw.write( (String)tmp );
	   fw.close();
	 } // end of if ()

	 else 
	   return false;
	 
      } catch (Exception e) {
	e.printStackTrace();
	return false;
      }
      return true;

   }

   public static SpItem fetchMSB(Integer msbid) {

     SpItem spItem = null;
      try {
	System.out.println(""+msbid);
	
	 URL url = new URL("http://www.jach.hawaii.edu/JAClocal/cgi-bin/msbsrv.pl");
	 addParameter("key", Integer.class, msbid);
	 //System.out.println(doCall(url, "urn:OMP::MSBServer", "fetchMSB"));

	 FileWriter fw = new FileWriter(System.getProperty("msbFile"));
	 String spXML = (String) doCall(url, "urn:OMP::MSBServer", "fetchMSB");

	 if (spXML != null ) {
	   
	   //System.out.println("The msb says:\n "+ spXML);
	   
	   StringReader r = new StringReader(spXML);
	   SpItemDOM dom = new SpItemDOM (r);
	   spItem = dom.getSpItem();

	   fw.write( (String)spXML );
	   fw.close();
	 } // end of if ()

	 else 
	   return spItem;
	 
      } catch (Exception e) {
	e.printStackTrace();
	return spItem;
      }

      return spItem;
   }

  public static void doneMSB(String projID, String checksum) {
      try {
	
	 URL url = new URL("http://www.jach.hawaii.edu/JAClocal/cgi-bin/msbsrv.pl");
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
}
