package edu.jach.qt.gui;


import edu.jach.qt.utils.*;
import gemini.sp.SpItem;
import gemini.sp.SpRootItem;
import gemini.sp.iter.*;
import gemini.sp.obsComp.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import jsky.app.ot.FileInfo;
import jsky.app.ot.OtCfg;
import jsky.app.ot.OtFileIO;
import jsky.app.ot.OtTreeWidget;
import jsky.app.ot.OtWindow;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;
import javax.swing.JSplitPane;
import edu.jach.qt.gui.DragDropObject;
import javax.swing.border.*;

/**
 * This is the top most class of the OMP-OM.  This 
 * starts off all subsequent OMP-OM specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class OmpOM extends JPanel{


   private ProgramTree	      ptree;
   private File		      file;
   private SpRootItem	      spRootItem;

   /**
    * Creates a new <code>OmpOM</code> instance.
    *
    */
   public OmpOM (){

      OtFileIO.setXML(System.getProperty("OMP") != null);

      //Load OM specific configs
      QtTools.loadConfig(new String(System.getProperty("omCfg")));

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

      file = new File(System.getProperty("msbFile"));
      spRootItem = OtFileIO.fetchSp(file.getParent(), file.getName());

      ptree = new ProgramTree();
      ptree.addTree(spRootItem);
      ptree.setMinimumSize(new Dimension(400,550) );

   }

   public void resetTree() {
      spRootItem = OtFileIO.fetchSp(file.getParent(), file.getName());
      ptree.addTree(spRootItem);
   }

   public JSplitPane getTreePanel() {
      
      CalibrationArea ca = new CalibrationArea();
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
					    ptree, ca);
      return splitPane;
   }

   public JSplitPane getDragTreePanel() {
      
      DragDropObject ddo = new DragDropObject(spRootItem);
      MsbNode root = new MsbNode(ddo);
      getItems(spRootItem, root);

      DnDJTree ddt = new DnDJTree(root);
      ddt.setCellRenderer(new DragTreeCellRenderer());
      //JPanel p = new JPanel();
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
					    ptree, ddt);

      return splitPane;
   }

   public void test() {
      
      JFrame f = new JFrame();
      f.setSize(400,300);
      DragDropObject ddo = new DragDropObject(spRootItem);
      MsbNode root = new MsbNode(ddo);
      getItems(spRootItem, root);
      DnDJTree ddt = new DnDJTree(root);
      ddt.setCellRenderer(new MyTreeCellRenderer());
      f.getContentPane().add(ddt);
      //f.pack();
      f.show();
   }


   /** public void getItems (SpItem spItem,DefaultMutableTreeNode node)
       is a public method to add ALL the items of a sp object into the
       JTree *recursively*.
      
       @param SpItem spItem,DefaultMutableTreeNode node
       @return  none
       @throws none
      
   */
   private void getItems (SpItem spItem, MsbNode node) {
      Enumeration children = spItem.children();
      while (children.hasMoreElements()) {
	 
	 SpItem  childNode = (SpItem) children.nextElement();
	 DragDropObject ddo = new DragDropObject(childNode);
	 MsbNode temp = new MsbNode(ddo);
	 node.add(temp);
	 getItems(childNode,temp);
      }
   }


   public static void main(String[] args) {
      QtTools.loadConfig("/home/mrippa/netroot/install/omp/QT/config/qt.conf");
      OmpOM om = new OmpOM();
   }
}// OmpOM
