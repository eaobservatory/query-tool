package edu.jach.qt.gui;


//import orac.jcmt.inst.*;
//import orac.jcmt.iter.*;

/* Gemini imports */
import gemini.sp.*;
import gemini.sp.iter.*;
import gemini.sp.obsComp.*;

/* JSKY imports */
import jsky.app.ot.*;

/* ORAC imports */
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;

/* QT imports */
import edu.jach.qt.gui.*;
import edu.jach.qt.utils.*;

/* Standard imports */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/* Miscellaneous imports */
import org.apache.log4j.Logger;

/**
 * This is the top most class of the OMP-OM.  This 
 * starts off all subsequent OMP-OM specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class OmpOM extends JPanel{

  static Logger logger = Logger.getLogger(OmpOM.class);

  private ProgramTree	      ptree;
  private File		      file;
  private SpItem	      spItem;
  private Hashtable	      ptreeHashtable;

  /**
   * Creates a new <code>OmpOM</code> instance.
   *
   */
  public OmpOM (){
    ptreeHashtable = new Hashtable();

    //Load OM specific configs
    OtFileIO.setXML(System.getProperty("OMP") != null);
    QtTools.loadConfig(new String(System.getProperty("omCfg")));

    /* Need to construct UKIRT-specific items so that their SpTypes are
     * statically initialised.  Otherwise the sp classes won't know about 
     * their types.  AB 19-Apr-2000
     */

    /* Init UKIRT Instruments */
    SpItem spItem = new SpInstUFTI();
    spItem = new SpInstCGS4();
    spItem = new SpInstIRCAM3();
    spItem = new SpInstMichelle();
    spItem = new SpDRRecipe();

    /*OMP Specific*/
    spItem = new SpIterChop();

    /* Init UKIRT SpTypes */
    spItem = new SpIterBiasObs();
    spItem = new SpIterBiasObs();
    spItem = new SpIterCGS4();
    spItem = new SpIterCGS4CalUnit();
    spItem = new SpIterCGS4CalObs();
    spItem = new SpIterCalUnit();
    spItem = new SpIterDarkObs();
    spItem = new SpIterFP();
    spItem = new SpIterIRCAM3();
    spItem = new SpIterIRPOL();
    spItem = new SpIterMichelle();
    spItem = new SpIterMichelleCalObs();
    spItem = new SpIterNod();
    spItem = new SpIterNodObs();
    spItem = new SpIterUFTI();

    /* Init GEMINI Miscellaneous */
    spItem = new SpIterRepeat();
    spItem = new SpIterOffset();
    spItem = new SpIterObserve();
    spItem = new SpIterSky();
    spItem = new SpSchedConstObsComp();
    spItem = new SpSiteQualityObsComp();

    /* Init JCMT Instruments */
    //spItem = new SpInstSCUBA();
    //spItem = new SpInstHeterodyne();

    /* Init JCMT SpTypes */
    //spItem = new SpIterFocusObs();
    //spItem = new SpIterFrequency();
    //spItem = new SpIterJiggleObs();
    //spItem = new SpIterRasterObs();
    //spItem = new SpIterSkydipObs();
    //spItem = new SpIterStareObs();
    //spItem = new SpIterScanObs();
    //spItem = new SpIterPointingObs();
    //spItem = new SpIterPhotomObs();

    /* Init JCMT Miscellaneous */
    //spItem = new orac.jcmt.obsComp.SpSiteQualityObsComp();

    logger.info("SpItems initialized");

    ptree = new ProgramTree();
  }

  public void setProjectID(String projectID) {
    ptree.setProjectID(projectID);
  }

  public void setChecksum(String checksum) {
    ptree.setChecksum(checksum);
  }

  public void setSpItem(SpItem item) {

    spItem = item;
  }
  
  public String getProgramName() {
    
    Vector progVector = SpTreeMan.findAllItems(spItem, "gemini.sp.SpMSB");

    logger.debug("progVector "+progVector);

    try {
       
      if ( progVector == null  || progVector.size() == 0) {
	progVector = SpTreeMan.findAllItems(spItem, "gemini.sp.SpObs");

	if ( progVector != null && progVector.size() >0) {
	  return (String) ((SpObs) (progVector.firstElement())).getTitle();  
	}

	else {
	  return "Title Not Found";
	}
      }

      else {
	return (String) ((SpMSB) (progVector.firstElement())).getTitle();
      }
      
    } catch (NoSuchElementException nse) {
      logger.warn("Title Not Found");
      nse.printStackTrace();
      return "Title Not Found";
    }

  }


  /**
   * Describe <code>addNewTree</code> method here. This adds a
   * <code>ProgramTree</code>, referrenced by the msbID, to the list
   * of trees.
   *
   * @param msbID an <code>int</code> value
   */
  public void addNewTree(Integer msbID) {

    ptree.addList(spItem);
    ptree.setMinimumSize(new Dimension(400,550) );
    //ptreeHashtable.put(msbID, ptree);
  }

  /**
   * Describe <code>addNewTree</code> method here.
   * The method used for debugging.  It loads in a hard-wired MSB file
   * to use as the ProgramTree object.
   */
  public void addNewTree() {
    file = 
      new File(System.getProperty("arrayTests", 
				  "/home/mrippa/install/omp/QT/config/array_tests.xml"));
    spItem = OtFileIO.fetchSp(file.getParent(), file.getName());
 
    //ptree.addTree(spItem);
    ptree.addList(spItem);
    ptree.setMinimumSize(new Dimension(400,550) );

    ptreeHashtable.put(new Integer(41), ptree);
  }

  public void resetTree() {
    spItem = OtFileIO.fetchSp(file.getParent(), file.getName());
    ptree.addList(spItem);
  }

  public JSplitPane getTreePanel() {
      
    //CalibrationArea ca = new CalibrationArea();
    //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptree, ca);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptree, null);
    return splitPane;
  }

  public JSplitPane getDragTreePanel() {
      
    DragDropObject ddo = new DragDropObject(spItem);
    MsbNode root = new MsbNode(ddo);
    getItems(spItem, root);

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
    DragDropObject ddo = new DragDropObject(spItem);
    MsbNode root = new MsbNode(ddo);
    getItems(spItem, root);
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
    QtTools.loadConfig(System.getProperty("qtConfig"));
    QtTools.loadConfig(System.getProperty("omConfig"));

    JFrame f = new JFrame();
    OmpOM om = new OmpOM();
    om.addNewTree();
    f.getContentPane().add(om.getTreePanel());
    f.setSize(400, 300);
    f.setVisible(true);
  }
}// OmpOM
