package edu.jach.qt.utils;

import java.lang.reflect.*;
import java.net.URL;
import java.io.FileWriter;
import gemini.sp.SpItem;
import orac.util.SpItemDOM;
import gemini.sp.SpRootItem;
import java.io.StringReader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * SpClient.java
 *
 */
public class SpClient extends SoapClient {

   public static void main(String[] args) {
      QtTools.loadConfig("/home/mrippa/netroot/install/omp/QT/config/qt.conf");

      //SpClient.fetchProgram("116", "abc");

      if(args.length == 0) {
	 try {
	    
	    LineNumberReader reader = new LineNumberReader(new
	       InputStreamReader(System.in));
	    String line = reader.readLine();

	    String spXml = line;

	    while(line != null) {
	       line = reader.readLine();
	       
	       if(line != null) {
		  spXml += line + "\n";
	       }
	    }
	    
	    System.out.println("Storing " + spXml + "\n...");
	    SpClient.storeProgram(spXml, "abc");
    
	 }
	 catch(IOException e) {
	    System.err.println("Problem reading from stdin: " + e);
	 }
	 catch(Exception e) {
	    System.err.println(e);
	 }
      }

      else {
	 if (args[0].equals("struct"))
	    try {
	       System.out.println(SpClient.returnStruct());
	    } catch(Exception e) {System.err.println(e+" You blew it!");}
	 else if (args[0].equals("array"))
	    try {
	       String[] arr = SpClient.returnArray();
	       
	       System.out.println(arr[0]);
	       System.out.println(arr[1]);
	       
	    } catch(Exception e) {System.err.println(e);}
	 else if (args[0].equals("list"))
	    try {
	       System.out.println(SpClient.returnList());
	    } catch(Exception e) {System.err.println(e);}
      }
   }

   public static void fetchProgram(String id, String pass) {
      try {
	 URL url = new URL(System.getProperty("spServer"));

	 addParameter("projectid", String.class, id);
	 addParameter("password", String.class, pass);
	 
	 System.out.println("" + (new SpItemDOM(new StringReader((String) doCall(url, "urn:OMP::SpServer", "fetchProgram")))).getSpItem());

      } catch (Exception e) {e.printStackTrace();}

   }

   public static void storeProgram(SpItem spItem, String pass) {
      try {
	 URL url = new URL(System.getProperty("spServer"));
	  
	 String sp = (new SpItemDOM(spItem)).toString();
	 addParameter("sp", String.class, sp);
	 addParameter("password", String.class, pass);

	 System.out.println(doCall(url, "urn:OMP::SpServer", "storeProgram"));
	 
	 
      } catch (Exception e) {e.printStackTrace();}
   }

   public static String storeProgram(String sp, String pass) throws Exception {
      URL url = new URL(System.getProperty("spServer"));
      addParameter("sp", String.class, sp);
      addParameter("password", String.class, pass);
	
      return (String) doCall(url, "urn:OMP::SpServer", "storeProgram");
   }
   
   public static String returnStruct() throws Exception {
      URL url = new URL(System.getProperty("spServer"));
      return (String) doCall(url, "urn:OMP::SpServer", "returnStruct");
   }

   public static String returnList() throws Exception {
      URL url = new URL(System.getProperty("spServer"));
      return (String) doCall(url, "urn:OMP::SpServer", "returnList");
   }

   public static String[] returnArray() throws Exception {
      URL url = new URL(System.getProperty("spServer"));
      Object temp = doCall(url, "urn:OMP::SpServer", "returnArray");
      
      Object[] objArr = (Object[])temp;
      String[] result = new String[2];

      for (int i=0; i<objArr.length; i++) {
	 result[i] = objArr[i].toString();
      }
      return result;
   }

}
