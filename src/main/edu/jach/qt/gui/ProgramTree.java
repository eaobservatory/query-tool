package edu.jach.qt.gui;


import edu.jach.qt.utils.MyTreeCellRenderer;
import edu.jach.qt.utils.QtTools;
import edu.jach.qt.utils.Translating; //Translating flasher
import gemini.sp.*;
import gemini.sp.SpItem;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;
import jsky.app.ot.*;
import om.console.*;      //this is for the sequence console
import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.ukirt.util.SpTranslator;

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

    run=new JButton("Send for execution");
    run.setMargin(new Insets(5,10,5,10));
    run.setEnabled(true);
    run.addActionListener(this);

    JLabel trash = new JLabel();
    try {
      setImage(trash);
    } catch(Exception e) {e.printStackTrace();}


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
      if (path == null) {
	//errorBox =new ErrorBox("You have not selected an observation!"+
	//"\nPlease select an observation.");
	System.err.print("You have not selected an MSB!");
	return;
      }
      // 	  try {
      // 	    System.out.println("TEST:"+path.toString());
      // 	  } catch (NullPointerException e)
      // 	    {
      // 	      errorBox =new ErrorBox("You have not selected an observation!"+
      // 				     "\nPlease select an observation object.");
      // 	      return;
      // 	    }
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
    SpItem item = findItem(_spItem, path.getLastPathComponent().toString());
      
    if (item == null) {
      //errorBox =new ErrorBox("You have not selected an observation!"+
      //                       "\nPlease select an observation.");
      System.err.print("You have not selected an MSB!");
      return;
    }

    // Switch to "og" to check if selection MSB
    if(!item.typeStr().equals("ob")) {
      //  	    errorBox =new ErrorBox("Your selection: "+item.getTitle()+
      //  				   " is not an observation"+
      //  				   "\nPlease select an observation.");
      System.err.print("Your selection: "+item.getTitle()+ 
		       " is not an MSB."+ 
		       "\nPlease select an MSB.");
      return;
    } else {
      run.setEnabled(false);
      //run.setForeground(Color.white);

      SpItem observation=item;

      if(!observation.equals(null)) {
	SpItem inst= (SpItem) SpTreeMan.findInstrument(item);
	Translating tFlush = new Translating();
	tFlush.start();
	String tname=trans(observation);
	tFlush.getFrame().dispose();
	tFlush.stop();

	// Catch null sequence names - probably means translation
	// failed:
	if (tname == null) {
	  //errorBox = new ErrorBox ("Translation failed. Please report this!");
	  System.err.println("Translation failed. Please report this!");
	  run.setEnabled(true);
	  run.setForeground(Color.black);		    
	  return;
	}else{
	  System.out.println ("Trans OK");
	}

	// Prevent IRCAM3 and CGS4 from running together

	int status =
	  QtTools.execute(new String[] {"/jac_sw/drama/drama-v1.3b6/bin/solaris/ditscmd",
					"OOS_LIST",
					"LISTOOS"});
	//figure out if the same inst. is already in use or
	//whether IRCAM3 and CGS4 would be running together
	//  	SequenceFrame f;
	//  	for(int i=0;i<scm.getFrameList().getList().size();i++) {
	//  	  f = (SequenceFrame)consoleFrames.getList().elementAt(i);
		
	//  	  if(inst.type().getReadable().equals(f.getInstrument())) {
	//  	    f.resetObs(observation.getTitle(),tname);
	//  	    run.setEnabled(true);
	//  	    run.setForeground(Color.black);
	//  	    return;
	//  	  }
		
	//  	  if(inst.type().getReadable().equals("IRCAM3") && f.getInstrument().equals("CGS4")) {
	//  	    //new AlertBox ("IRCAM3 and CGS4 cannot run at the same time.");
	//  	    run.setEnabled(true);
	//  	    run.setForeground(Color.black);		    
	//  	    return;
	//  	  }
		
	//  	  if(inst.type().getReadable().equals("CGS4") && f.getInstrument().equals("IRCAM3")) {
	//  	    //new AlertBox ("CGS4 and IRCAM3 cannot run at the same time.");
	//  	    run.setEnabled(true);
	//  	    run.setForeground(Color.black);		    
	//  	    return;
	//  	  }
	//  	}
	scm.spawnSequenceConsole(observation.getTitle(), inst.type().getReadable(), false);
	run.setEnabled(true);
      }
    }
  }

  //    /**  
  //         private void creatNewRemoteFrame(SpItem observation,SpItem inst) 
  //         is a private method to start a NEW sequence console. This is mainly
  //         about to start a "remote" frame to form a sequence console and 
  //         rebind it into a RMI registry.
  //         it also starts a set of drama task which is platform-dependent.
       
  //         @param SpItem observation,SpItem inst
  //         @return  none
  //         @throws RemoteException,MalformedURLException
       
  //    */
  //    private void creatNewRemoteFrame(SpItem observation, SpItem inst) {
  //      try {
  //        frame = new RemoteFrame(inst.type().getReadable(),
  //  			      observation.getTitle(), consoleFrames);
	  
  //        //String instStr = inst.type().getReadable();

  //      } 
  //      catch (RemoteException re) {
  //        System.out.println ("Exception in ProgramTree:"+re);
  //      } 
  //      catch (NullPointerException e) {
  //        System.out.println("NullPointerException in programTree:" + e);
  //      }
      
  //      //add inst into the instrument list on the OM frame
  //      //      if (menu.getActiveInstrumentList().getItemCount()>0) {
  //      //        String activeInst = 
  //      //  	menu.getActiveInstrumentList().getItemAt(0).toString();
  //      //        if (activeInst.substring(0,4).equalsIgnoreCase("None") ||
  //      //  	  activeInst.substring(0,4).equals("    ")) {
  //      //  	menu.getActiveInstrumentList().removeAllItems();
  //      //        }
  //      //      }

  //      try {
  //        //menu.getActiveInstrumentList().addItem(inst.type().getReadable());
  //        consoleFrames.addFrameList (frame.getFrame());
  //        loadDramaTasks (inst.type().getReadable());
	 
  //        //connect it to the TCS if it is the first instrument
  //        if (consoleFrames.getList().size()==1) {
  //  	SequenceFrame sf = (SequenceFrame) consoleFrames.getList().elementAt(0);
  //  	sf.connectTCS();
  //        }
  //        if (inst.type().getReadable().equalsIgnoreCase("UFTI")) {
  //  	//new AlertBox ("Ask your TSS to datum filter wheels and shutter. Then open shutter");
  //        }
  //        run.setEnabled(true);
  //        run.setForeground(Color.black);
	
  //      } catch (NullPointerException e) {
  //        System.out.println("NullPointerException in programTree");
  //        e.printStackTrace();
  //      }
  //    }
  
  
  /**
     String trans (SpItem observation) is a private method
     to translate an observation java object into an exec string
     and write it into a ascii file where is located in "EXEC_PATH"
     directory and has a name stored in "execFilename"
     
     @param SpItem observation
     @return  String a filename
     @throws RemoteException,MalformedURLException
     
  */
  private String trans (SpItem observation) {
    SpTranslator translation = new SpTranslator((SpObs)observation);
    translation.setSequenceDirectory(System.getProperty("EXEC_PATH"));
    translation.setConfigDirectory(System.getProperty("CONF_PATH"));
      
    Properties temp = System.getProperties();
    String tname = null;
    try {
      tname=translation.translate();

      System.out.println("exec: "+System.getProperty("EXEC_PATH")+"/"+tname);
      
      temp.put(new String("execFilename"),tname);
    }catch (NullPointerException e) {
      System.out.println ("Translation failed!, exception was "+e);
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println ("Translation failed!, Missing value "+e);
      //e.printStackTrace();
    }
    return tname;
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
  private JScrollPane scrollPane= new JScrollPane();;
  private SpItem _spItem;

  private DefaultMutableTreeNode root;
  private DefaultTreeModel treeModel;
  private TreePath path;

  private SequenceManager scm;
  public static final String BIN_IMAGE = System.getProperty("binImage");
  public static final String BIN_SEL_IMAGE = System.getProperty("binImage");
}

