package edu.jach.qt.app;

/**
 * QuerytoolClient.java
 *
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */
import edu.jach.qt.utils.*;

import java.util.Vector;
import org.apache.soap.*;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.*;
import org.apache.soap.rpc.*;
import org.apache.soap.messaging.*;
import java.net.URL;
import org.apache.soap.util.xml.*;
import java.io.*;
import org.w3c.dom.*;
import org.apache.soap.util.*;
import java.lang.reflect.*;

public class QuerytoolClient {
   SOAPMappingRegistry smr = new SOAPMappingRegistry(Constants.NS_URI_CURRENT_SCHEMA_XSD);
   URL url = null;

   public static final String ENDPOINT = "http://www-private.jach.hawaii.edu:81/cgi-bin/msbsrv.pl";
   public static final String SOAP_ACTION = "urn:OMP::MSBServer";
   public static final String OBJECT_URI = "http://www.jach.hawaii.edu/OMP::MSBServer";

   public Header header = null;

   public QuerytoolClient () {
      try {
	 url = new URL(ENDPOINT);
      }catch(Exception e) {
	 e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      QuerytoolClient qtc = new QuerytoolClient();
      qtc.queryMSB("<Query><Moon>Dark</Moon></Query>");
      qtc.fetchMSB(new Integer("96"));
   }

   public void queryMSB(String xmlQueryString) {
      IntDeserializer intDser = new IntDeserializer();
      FloatDeserializer floatDser = new FloatDeserializer();
      StringDeserializer stringDser = new StringDeserializer();
      ArraySerializer arraySer = new ArraySerializer();
      DataSerializer dataSer = new DataSerializer();
      smr.mapTypes(Constants.NS_URI_SOAP_ENC, 
		   new QName(OBJECT_URI, "SOAPStruct"), 
		   Data.class, dataSer, dataSer);

      Parameter p1 = new Parameter("xmlquery ", String.class, xmlQueryString, null);
      smr.mapTypes(Constants.NS_URI_SOAP_ENC, new QName("", "Return"), null, null, stringDser);

      Integer i = new Integer(10);
      Parameter p2= new Parameter("maxreturn", Integer.class, i, null);
      smr.mapTypes(Constants.NS_URI_SOAP_ENC, new QName("", "Return"), null, null, intDser);

      Vector params = new Vector();
      params.addElement(p1);
      params.addElement(p2);
      doCall(url, "queryMSB", params);

   }

   public void fetchMSB(Integer msbId) {
      IntDeserializer intDser = new IntDeserializer();
      FloatDeserializer floatDser = new FloatDeserializer();
      StringDeserializer stringDser = new StringDeserializer();
      ArraySerializer arraySer = new ArraySerializer();
      DataSerializer dataSer = new DataSerializer();
      smr.mapTypes(Constants.NS_URI_SOAP_ENC, 
		   new QName(OBJECT_URI, "SOAPStruct"), 
		   Data.class, dataSer, dataSer);
      Vector params = new Vector();

      Parameter p1= new Parameter("key", Integer.class, msbId, null);
      smr.mapTypes(Constants.NS_URI_SOAP_ENC, new QName("", "Return"), null, null, intDser);

      params.addElement(p1);
      doCall(url, "fetchMSB", params);
   }

   public void doCall(URL url, String methodName, Vector params) {
      try {
	 Call call = new Call();
	 call.setSOAPMappingRegistry(smr);
	 call.setTargetObjectURI(SOAP_ACTION);
	 call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
	 call.setMethodName(methodName);
	 call.setParams(params);
	 if (header != null)
	    call.setHeader(header);
      
	 String soapAction = SOAP_ACTION;
	 if (true) {
	    soapAction = soapAction+"#" + methodName;
	 }
      
	 Response resp = call.invoke(url, soapAction);
      
	 // check response 
	 if (!resp.generatedFault()) {
	    Parameter ret = resp.getReturnValue();
	    Object output = (String) ret.getValue();
	    System.out.println("OUT: "+output);
	    if (methodName.equals("queryMSB")) {
	       FileWriter fw = new FileWriter("/home/mrippa/root/install/omp/QT/config/msbSummary.xml");
	       fw.write((String)output);
	       fw.close();
	    }
	    else if (methodName.equals("fetchMSB")) {
	       FileWriter fw = new FileWriter("/home/mrippa/root/install/omp/QT/config/msb.xml");
	       fw.write((String)output);
	       fw.close();
	    }
	 }
	 else {
	    Fault fault = resp.getFault ();
	    System.err.println (methodName + " generated fault: ");
	    System.out.println ("  Fault Code   = " + fault.getFaultCode());  
	    System.out.println ("  Fault String = " + fault.getFaultString());
	 }
      
      } catch (Exception e) {
	 e.printStackTrace();
      }
   }

}// QuerytoolClient
