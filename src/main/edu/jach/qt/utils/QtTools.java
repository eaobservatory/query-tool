package edu.jach.qt.utils;

import java.io.*;
import java.util.Properties;
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
	 DataInputStream ds = new DataInputStream(is);

	 Properties temp=System.getProperties();
	 int lineno = 0;

	 while ((line_str = ds.readLine()) != null) {
	    lineno++;
	    if(line_str.length()>0) {
	       if(line_str.charAt(0)=='#') continue;

	       try {
		  int colonpos = line_str.indexOf(":");
		  temp.put(line_str.substring(0,colonpos).trim(),
			   line_str.substring(colonpos+1).trim());

	       }catch (IndexOutOfBoundsException e) {
		  System.out.println ("Problem reading line "+lineno+": "+line_str);
		  ds.close();
		  is.close();
		  System.exit(1);
	       }
	    }
	 }
	 ds.close();
	 is.close();

      } catch (IOException e) {
	 System.out.println("File error: " + e);
      }
   }
}
