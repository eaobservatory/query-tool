package edu.jach.qt.gui;

import gemini.sp.*;
import gemini.sp.obsComp.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

import om.console.*;
import om.util.*;

import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;

import edu.jach.qt.utils.*;
import jsky.app.ot.*;
import ocs.utils.*;


/**
   final public class programTree is a panel to select
   an observation from a JTree object. 

   @version 1.0 1st June 1999
   @author M.Tan@roe.ac.uk, modified by Mathew Rippa
*/
final public class ProgramTree extends JPanel 
  implements TreeSelectionListener,ActionListener,KeyListener {

  /** public programTree(menuSele m) is the constructor. The class
      has only one constructor so far.  a few thing are done during
      the construction. They are mainly about adding a run button and
      setting up a listener
      
      @param  none
      @return none
      @throws none 
  */
  public ProgramTree()  {
    scm = SequenceManager.getHandle();

    Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
    setBorder(new TitledBorder(border, "Fetched Science Program (SP)", 
			       0, 0, new Font("Roman",Font.BOLD,12),Color.black));
    setLayout(new BorderLayout() );

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    gbc = new GridBagConstraints();

    run=new JButton("Send for Execution");
    run.setMargin(new Insets(5,10,5,10));
    run.setEnabled(true);
    run.addActionListener(this);

    JLabel trash = new JLabel();
    try {
      setImage(trash);
    } catch(Exception e) {
      e.printStackTrace();
    }

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.weighty = 100;
    gbc.insets.left = 10;
    gbc.insets.right = 0;
    add(trash, gbc, 1, 1, 1, 1);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 100;
    gbc.weighty = 0;
    gbc.insets.left = 0;
    gbc.insets.right = 0;
    add(run, gbc, 0, 1, 1, 1);

  }

  public void setProjectID(String projectID) {
    this.projectID = projectID;
  }

  public void setChecksum(String checksum) {
    this.checkSum = checkSum;
  }

  public void setImage(JLabel label) throws Exception {
    URL url = new URL("file://"+BIN_IMAGE);
    if(url != null) {
      label.setIcon(new ImageIcon(url));
    }
    else {
      label.setIcon(new ImageIcon(ProgramTree.class.getResource("file://"+BIN_IMAGE)));
    }
  }

  /**
   * Describe <code>add</code> method here.
   *
   * @param c a <code>Component</code> value
   * @param gbc a <code>GridBagConstraints</code> value
   * @param x an <code>int</code> value
   * @param y an <code>int</code> value
   * @param w an <code>int</code> value
   * @param h an <code>int</code> value
   */
  public void add(Component c, GridBagConstraints gbc, 
		  int x, int y, int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    add(c, gbc);      
  }

  
  /** public void actionPerformed(ActionEvent evt) is a public method
      to react button actions. The reaction is mainly about to start a
      SGML translation, then a "remote" frame to form a sequence console.
      
      @param ActionEvent
      @return  none
      @throws none
      
  */
  public void actionPerformed (ActionEvent evt) {
    Object source = evt.getSource();
    if (source == run) {
      execute();
    }
  }
  
  /**
   * The <code>execute</code> method kick off a sequencer.  The
   * following things will occur:
   * 1. It first translates the SP into an exec and sets the resulting
   *    file name as a system property.
   * 2. It checks to see what other sequencers are running by running
   *    ditscmd -g OOS_LIST OOSLIST.
   * 3. The sequence console is run up reflecting results of test in (2).
   *
   */
  private void execute() {

    /**
     * @param item is the selected observation
     */
    //SpItem item = findItem(_spItem, path.getLastPathComponent().toString());
    
    SpItem item = (SpItem) obsList.getSelectedValue();

    if (item == null) {
      new ErrorBox("You have not selected an observation!"+
		   "\nPlease select an observation.");
      System.err.print("You have not selected an MSB!");
      return;
    }

    // Switch to "og" to check if selection is an MSB. However, we can only send
    // type "ob" observations since an MSB can have multiple instruments and we 
    // execute them individually.
    if(!item.typeStr().equals("ob")) {
      new ErrorBox("Your selection: "+item.getTitle()+
		   " is not an observation"+
		   "\nPlease select an observation.");
      System.err.print("Your selection: "+item.getTitle()+ 
		       " is not an MSB."+ 
		       "\nPlease select an MSB.");
      return;
    } else {
      run.setEnabled(false);
      
      SpItem observation = item;
      
      if(!observation.equals(null)) {
	SpItem inst = (SpItem) SpTreeMan.findInstrument(observation);
	
	// *TODO* Replace this crap!
	Translating tFlush = new Translating();
	tFlush.start();
	String tname = QtTools.translate(observation, inst.type().getReadable());
	tFlush.getFrame().dispose();
	tFlush.stop();
	
	// Catch null sequence names - probably means translation
	// failed:
	if (tname == null) {
	  //new ErrorBox ("Translation failed. Please report this!");
	  System.err.println("Translation failed. Please report this!");
	  run.setEnabled(true);
	  return;
	}else{
	  System.out.println ("Trans OK");

	  if ( obsList.getSelectedIndex() ==  obsList.getLastVisibleIndex()) {

	    MsbClient.doneMSB(projectID, checkSum);
	    JOptionPane.showMessageDialog(null, "MSB DONE!");
	  } // end of if ()
	  
	  model.remove(obsList.getSelectedIndex());

	}
      

	// Prevent IRCAM3 and CGS4 from running together
	// figure out if the same inst. is already in use or
	// whether IRCAM3 and CGS4 would be running together
	SequenceConsole console;
	Vector consoleList = scm.getConsoleList().getList();

	for(int i=0; i<consoleList.size(); i++) {
	  console = (SequenceConsole)consoleList.elementAt(i);
	  
	  if(inst.type().getReadable().equals(console.getInstrument())) {
	    console.resetObs(observation.getTitle(), tname);
	    run.setEnabled(true);
	    run.setForeground(Color.black);
	    return;
	  }
	  
	  if(inst.type().getReadable().equals("IRCAM3") && 
	     console.getInstrument().equals("CGS4")) {
	    new AlertBox ("IRCAM3 and CGS4 cannot run at the same time.");
	    run.setEnabled(true);
	    return;
	  }

	  if(inst.type().getReadable().equals("CGS4") && 
	     console.getInstrument().equals("IRCAM3")) {
	    new AlertBox ("CGS4 and IRCAM3 cannot run at the same time.");
	    run.setEnabled(true);
	    return;
	  }
	}

	QtTools.loadDramaTasks(inst.type().getReadable());
	DcHub.getHandle().register("OOS_LIST");
	
	scm.showSequenceFrame();
	run.setEnabled(true);
      }
    }
  }
  

  /**
     public void addTree(String title,SpItem sp) is a public method
     to set up a JTree GUI bit for a science program object in the panel
     and to set up a listener too
     
     @param String title and SpItem sp
     @return  none
     @throws none
     
  */
  public void addTree(SpItem sp)
  {
    _spItem=sp;

    // Create data for the tree
    root= new DefaultMutableTreeNode(sp);

    //DragDropObject ddo = new DragDropObject(sp);
    //myObs = new MsbNode(ddo);
      
    getItems(sp, root);
            
    // Create a new tree control
    treeModel = new DefaultTreeModel(root);
    tree = new JTree( treeModel);
      

    MyTreeCellRenderer tcr = new MyTreeCellRenderer();
    // Tell the tree it is being rendered by our application
    tree.setCellRenderer(tcr);
    tree.addTreeSelectionListener(this);
    tree.addKeyListener(this);

    // Add the listbox to a scrolling pane
    scrollPane.getViewport().removeAll();
    scrollPane.getViewport().add(tree);
    scrollPane.getViewport().setOpaque(false);

    gbc.fill = GridBagConstraints.BOTH;
    //gbc.anchor = GridBagConstraints.EAST;
    gbc.insets.bottom = 5;
    gbc.insets.left = 10;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(scrollPane, gbc, 0, 0, 2, 1);
      
    this.repaint();
    this.validate();
  }

  public void addList(SpItem sp) {

    model = new DefaultListModel();

    Vector obsVector =  SpTreeMan.findAllItems(sp, "gemini.sp.SpObs");
    
    Enumeration e = obsVector.elements();
    while (e.hasMoreElements() ) {
      model.addElement(e.nextElement());
    } // end of while ()

    obsList = new JList(model);
    obsList.setCellRenderer(new ObsListCellRenderer());
    
    // Add the listbox to a scrolling pane
    scrollPane.getViewport().removeAll();
    scrollPane.getViewport().add(obsList);
    scrollPane.getViewport().setOpaque(false);

    gbc.fill = GridBagConstraints.BOTH;
    //gbc.anchor = GridBagConstraints.EAST;
    gbc.insets.bottom = 5;
    gbc.insets.left = 10;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(scrollPane, gbc, 0, 0, 2, 1);
    
  }

  //public MsbNode getMsbNode() {
  //   return myObs;
  // }

  public JTree getTree() {
    return tree;
  }
  
  /**
     public void removeTree( ) is a public method
     to remove a JTree GUI bit for a science program object in the panel
     and to set up a listener too
      
     @param none
     @return  none
     @throws none
      
  */
  public void removeTree()
  {
    this.remove(scrollPane);
  }
  

  /**
     public void valueChanged( TreeSelectionEvent event) is a public method
     to handle tree item selections
     
     @param TreeSelectionEvent event
     @return  none
     @throws none
     
  */
  public void valueChanged(TreeSelectionEvent event)
  {
    if( event.getSource() == tree )
      {
	// Display the full selection path
	path = tree.getSelectionPath();

	// The next section is with a view to possible
	// exposure time changes. Don't use until we know want we want
	// for sure.
	// 	  if(path.getLastPathComponent().toString().length()>14) {
	// 	    if(path.getLastPathComponent().toString().substring(0,14).equals("ot_ukirt.inst.")) {
	// 	      new newExposureTime(_spItem);
	// 	    }
	// 	  }
      }
  }

  public void keyPressed(KeyEvent e) {
    if( (e.getKeyCode() == KeyEvent.VK_DELETE))
      removeCurrentNode();
      
  }

  public void keyReleased(KeyEvent e) { }

  public void keyTyped(KeyEvent e) { }

  /** Remove the currently selected node. */
  public void removeCurrentNode() {

    SpItem item = findItem(_spItem, path.getLastPathComponent().toString());

    if( (item != null) && (item.getTitle().equals("array_tests")) ) {

      //TreePath currentSelection = tree.getSelectionPath();
	 
      if (path != null) { 
	DefaultMutableTreeNode currentNode = 
	  (DefaultMutableTreeNode)(path.getLastPathComponent());
	MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
	if (parent != null) {
	  int n = JOptionPane.showConfirmDialog(null, 
						"Are you shure you want to delete "+item.getTitle()+" ?", 
						"Deletion Requested", 
						JOptionPane.YES_NO_OPTION);
	  if(n == JOptionPane.YES_OPTION)
	    treeModel.removeNodeFromParent(currentNode);
	  return;
	}
      }
      // Either there was no selection, or the root was selected.
      //toolkit.beep();
    }
    JOptionPane.showMessageDialog(null, 
				  "You can only delete 'array_tests' at this time", 
				  "Message", JOptionPane.ERROR_MESSAGE);
  }
   
  /** public void getItems (SpItem spItem,DefaultMutableTreeNode node)
      is a public method to add ALL the items of a sp object into the
      JTree *recursively*.
      
      @param SpItem spItem,DefaultMutableTreeNode node
      @return  none
      @throws none
      
  */
  private void getItems (SpItem spItem,DefaultMutableTreeNode node)
  {
    Enumeration children = spItem.children();
    while (children.hasMoreElements())
      {
	SpItem  child = (SpItem) children.nextElement();
	  
	DefaultMutableTreeNode temp
	  = new DefaultMutableTreeNode(child);
	  
	node.add(temp);
	getItems(child,temp);
      }
  }
  
  
  /** 
      public void getItems (SpItem spItem,DefaultMutableTreeNode node)
      is a public method to get an item in a sp.
      
      @param SpItem spItem, String name
      @return  SpItem
      @throws none
      
  */
  private SpItem findItem (SpItem spItem, String name) {
    int index = 0;
    Enumeration children = spItem.children();
    SpItem tmpItem = null;
    while (children.hasMoreElements()) {
      SpItem  child = (SpItem) children.nextElement();
      if(child.toString().equals(name))
	return child;
      tmpItem = findItem(child,name);
      if(tmpItem != null)
	return tmpItem;
    }
    return null;
  }
  
  public JButton getRunButton () {return run;}
   
  private GridBagConstraints gbc;
  private JButton run;
  private JTree tree;
  private JList obsList;
  private DefaultListModel model;

  private JScrollPane scrollPane= new JScrollPane();;
  private SpItem _spItem;

  private DefaultMutableTreeNode root;
  private DefaultTreeModel treeModel;
  private TreePath path;

  private String projectID, checkSum;

  private SequenceManager scm;
  public static final String BIN_IMAGE = System.getProperty("binImage");
  public static final String BIN_SEL_IMAGE = System.getProperty("binImage");
}
