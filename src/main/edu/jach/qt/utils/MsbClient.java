package edu.jach.qt.utils;

import java.lang.reflect.*;
import java.net.URL;
import java.io.FileWriter;

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

   public static void queryMSB(String xmlQueryString) {
      try {
	 URL url = new URL(System.getProperty("msbServer"));
	 addParameter("xmlquery", String.class, xmlQueryString);
	 //addParameter("maxreturn", Integer.class, new Integer(2));
	 //System.out.println(doCall(url, "urn:OMP::MSBServer", "queryMSB"));

	 FileWriter fw = new FileWriter(System.getProperty("msbSummary"));
	 fw.write((String) doCall(url, "urn:OMP::MSBServer", "queryMSB"));
	 fw.close();
      } catch (Exception e) {e.printStackTrace();}

   }

   public static void fetchMSB(Integer msbid) {
      try {
	 URL url = new URL("http://www.jach.hawaii.edu/JAClocal/cgi-bin/msbsrv.pl");
	 addParameter("key", Integer.class, msbid);
	 //System.out.println(doCall(url, "urn:OMP::MSBServer", "fetchMSB"));

	 FileWriter fw = new FileWriter(System.getProperty("msbFile"));
	 fw.write((String) doCall(url, "urn:OMP::MSBServer", "fetchMSB"));
	 fw.close();
	 
      } catch (Exception e) {e.printStackTrace();}
   }
}
