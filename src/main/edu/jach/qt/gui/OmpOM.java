package edu.jach.qt.gui;

import java.awt.Dimension;
import java.io.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.*;

import edu.jach.qt.utils.*;

import gemini.sp.SpItem;
import gemini.sp.SpRootItem;
import gemini.sp.iter.*;
import gemini.sp.obsComp.*;
import jsky.app.ot.OtCfg;
import jsky.app.ot.OtFileIO;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;

/**
 * This is the top most class in the <code>OmpOM</code>.  This 
 * starts off all subsequent OMP-OM specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class OmpOM extends JFrame{
   /**
    * Creates a new <code>OmpOM</code> instance.
    *
    */
   public OmpOM (){
      setSize(new Dimension(300, 400));
      setTitle("OMP --OBSERVATION MANAGER--");
      //OtCfg.init();
      OtFileIO.setXML(System.getProperty("OMP") != null);

      //Load OM specific configs
      QtTools.loadConfig(new String("/home/mrippa/netroot/install/omp/QT/config/om.cfg"));

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
      File file = new File("/home/mrippa/netroot/install/omp/QT/config/msb.xml");
      String dir = file.getParent();
      String name = file.getName();
      SpRootItem spItem = OtFileIO.fetchSp(dir, name);

      ProgramTree ptree = new ProgramTree();
      getContentPane().add(ptree);
      show();

      ptree.addTree(spItem);
   }

}// OmpOM
