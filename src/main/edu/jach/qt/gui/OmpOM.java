package edu.jach.qt.gui;

/**
 * OmpOM.java
 *
 *
 * Created: Wed Sep  5 19:12:04 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

import javax.swing.*;
import javax.swing.event.*;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.util.Properties;
import jsky.app.ot.OtCfg;
import jsky.app.ot.OtFileIO;
import orac.util.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import gemini.sp.iter.*;
import gemini.sp.obsComp.*;
import gemini.sp.SpRootItem;
import java.awt.Dimension;
import java.io.*;
import gemini.sp.SpItem;


public class OmpOM extends JFrame{
   public OmpOM (){
      setSize(new Dimension(300, 400));
      setTitle("OMP --OBSERVATION MANAGER--");
      OtCfg.init();
      OtFileIO.setXML(System.getProperty("OMP") != null);

      //Load OM specific configs
      loadConfig(new String("/home/mrippa/root/install/omp/QT/config/om.cfg"));

      // Need to construct UKIRT-specific items so that their SpTypes are
      // statically initialised.  Otherwise the sp classes won't know about 
      // their types.  AB 19-Apr-2000
      SpItem spItem = new SpInstUFTI();
      spItem = new SpInstCGS4();
      spItem = new SpInstIRCAM3();
      spItem = new SpInstMichelle();
      spItem = new SpDRRecipe();
      spItem = new SpIterBiasObs();
      spItem = new SpIterDarkObs();
      spItem = new SpIterCGS4();
      spItem = new SpIterIRCAM3();
      spItem = new SpIterMichelle();
      spItem = new SpIterUFTI();
      spItem = new SpIterCGS4CalUnit();
      spItem = new SpIterCGS4CalObs();
      spItem = new SpIterMichelleCalObs();
      spItem = new SpIterFP();
      spItem = new SpIterIRPOL();
      spItem = new SpIterNod();
      spItem = new SpIterObserve();
      spItem = new SpIterSky();
      spItem = new SpSiteQualityObsComp();

      startOM();
   }

   private void startOM() {
      File file = new File("/home/mrippa/root/src/omp/omp/QT/config/msb.xml");
      String dir = file.getParent();
      String name = file.getName();
      SpRootItem spItem = OtFileIO.fetchSp(dir, name);

      ProgramTree ptree = new ProgramTree();
      getContentPane().add(ptree);
      show();

      ptree.addTree(spItem);
   }

   /** private static void loadConfig(String filename) is a private method
    reads a configuration file and set up configuration. It is done by
    putting things into java system properties which is program-system-wide visible.

    @param String args
    @return  none
    @throws none
   */
   private static void loadConfig(String filename) {
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


}// OmpOM
