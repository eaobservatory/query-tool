package edu.jach.qt.gui;


/* Gemini imports */
import gemini.sp.*;
import gemini.sp.obsComp.*;

/* JSKY imports */
import jsky.app.ot.*;

/* ORAC imports */
import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;

/* ORAC-OM imports */
import om.console.*;
import om.util.*;

/* QT imports */
import edu.jach.qt.utils.*;

/* Standard imports */
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DragSourceContext;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

/* Miscellaneous imports */
import ocs.utils.*;
import org.apache.log4j.Logger;


/**
   final public class programTree is a panel to select
   an observation from a JTree object. 

   @version 1.0 1st June 1999
   @author M.Tan@roe.ac.uk, modified by Mathew Rippa
*/
final public class ProgramTree extends JPanel implements 
    TreeSelectionListener, 
    ActionListener,
    KeyListener,
    DragSourceListener,
    DragGestureListener, 
    DropTargetListener {

  static Logger logger = Logger.getLogger(ProgramTree.class);

  public static final String BIN_IMAGE = System.getProperty("binImage");
  public static final String BIN_SEL_IMAGE = System.getProperty("binImage");

  private GridBagConstraints		gbc;
  private JButton			run;
  private JTree				tree;
  private static JList			        obsList;
  private DefaultListModel		model;
  private JScrollPane			scrollPane = new JScrollPane();;
  private SpItem			_spItem;
  private DefaultMutableTreeNode	root;
  private DefaultTreeModel		treeModel;
  private TreePath			path;
  private String			projectID, checkSum;
  private SequenceManager		scm;
    private DropTarget                  dropTarget=null;
    private DragSource                  dragSource=null;
    private TrashCan                    trash=null;
    public static  SpItem          selectedItem=null;

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

    // Ensure nothing is selected 
    selectedItem = null;

    Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
    setBorder(new TitledBorder(border, "Fetched Science Program (SP)", 
			       0, 0, new Font("Roman",Font.BOLD,12),Color.black));
    setLayout(new BorderLayout() );

    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    gbc = new GridBagConstraints();

    run=new JButton("Send for Execution");
    run.setMargin(new Insets(5,10,5,10));
    if (TelescopeDataPanel.DRAMA_ENABLED) {
	run.setEnabled(true);
    }
    else {
	run.setEnabled(false);
    }
    run.addActionListener(this);

    dropTarget=new DropTarget();
    try{
	dropTarget.addDropTargetListener(this);
    }catch(TooManyListenersException tmle){System.out.println("Too many listeners");}

    trash = new TrashCan();
    trash.setDropTarget(dropTarget);

    dragSource = new DragSource();

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
    this.checkSum = checksum;
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
      SpItem item;
      boolean isDeferredObs = false;
      if (selectedItem != null && DeferredProgramList.currentItem != null) {
// 	  new ErrorBox("You may only select one observation!"+
// 		       "\nPlease deselect an observation.");
	  JOptionPane.showMessageDialog(null,
					"You may only select one observation!",
					"Please deselect an observation.",
					JOptionPane.ERROR_MESSAGE);
	  // logger.error("Multiple observations selected!");
	  return;
      }
      else if (selectedItem == null && DeferredProgramList.currentItem == null){
// 	  new ErrorBox("You have not selected an observation!"+
// 		       "\nPlease select an observation.");
	  JOptionPane.showMessageDialog(null,
					"You have not selected an observation!",
					"Please select an observation.",
					JOptionPane.ERROR_MESSAGE);
	  logger.error("You have not selected an MSB!");
	  return;
      }
      else if (selectedItem != null) {
	  item = selectedItem;
	  selectedItem=null;
      }
      else {
	  item = DeferredProgramList.currentItem;
	  DeferredProgramList.currentItem=null;
	  isDeferredObs =  true;
      }

    // Switch to "og" to check if selection is an MSB. However, we can only send
    // type "ob" observations since an MSB can have multiple instruments and we 
    // execute them individually.
    if(!item.typeStr().equals("ob")) {
//       new ErrorBox("Your selection: "+item.getTitle()+ " is not an observation"+
// 		   "\nPlease select an observation.");
	  JOptionPane.showMessageDialog(null,
					"Your selection: "+item.getTitle()+ " is not an observation",
					"Please select an observation.",
					JOptionPane.ERROR_MESSAGE);
      logger.error("Your selection: "+item.getTitle()+ " is not an MSB."+ 
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
	  logger.error("Translation failed. Please report this!");
	  run.setEnabled(true);
	  return;
	}else{
	  logger.info("Trans OK");

	  if (!isDeferredObs) {
	      model.remove(obsList.getSelectedIndex());
	  }
	  else {
	      DeferredProgramList.markThisObservationAsDone(item);
	  }

	  if ( model.isEmpty()) {
	    MsbClient.doneMSB(projectID, checkSum);
	    JOptionPane.showMessageDialog(null, "The MSB with \n"+
					  "Project ID: "+projectID+"\n"+
					  "CheckSum: "+checkSum+"\n"+
					  "has been marked as done!");
	  } // end of if ()
	  
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

	if ( System.getProperty("os.name").equals("SunOS")) {
	  QtTools.loadDramaTasks(inst.type().getReadable());
	  DcHub.getHandle().register("OOS_LIST");
	}
	
	//DcHub.getHandle().register("OOS_LIST");
	
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
    // root= new DefaultMutableTreeNode(sp);

    getItems(sp, root);
            
    // Create a new tree control
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
      

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
    MouseListener ml = new MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		if (e.getClickCount() == 2)
		    {
			execute();
		    }
		else if (e.getClickCount() == 1)
		    {
			if (selectedItem != obsList.getSelectedValue() ) {
			    // Select the new item
			    selectedItem = (SpItem) obsList.getSelectedValue();
			    DeferredProgramList.clearSelection();
			}
			else {
			    obsList.clearSelection();
			    selectedItem = null;
			}
		    }
	    }
	};
    obsList.addMouseListener(ml);

    dragSource.createDefaultDragGestureRecognizer(obsList,
						  DnDConstants.ACTION_MOVE,
						  this);
    
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

    public static void clearSelection() {
	obsList.clearSelection();
	selectedItem = null;
    }

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

	Object item = obsList.getSelectedValue();

	if (item == null)  {
	    JOptionPane.showMessageDialog(null,
					  "No Observation to remove",
					  "Message", JOptionPane.INFORMATION_MESSAGE);
	    return;
	}

	((DefaultListModel)obsList.getModel()).removeElementAt(obsList.getSelectedIndex());
	
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

    /* Drop Target Interface */
    public void dragEnter(DropTargetDragEvent evt){
    }
  
    /* Drop Target Interface */
    public void dragExit(DropTargetEvent evt){
    }

    /* Drop Target Interface */
    public void dragOver(DropTargetDragEvent evt){
    }

    /* Drop Target Interface */
    public void drop(DropTargetDropEvent evt){
	SpObs itemForDrop;
	if (selectedItem != null) {
	    itemForDrop = (SpObs)selectedItem;
	}
	else {
	    itemForDrop = (SpObs)DeferredProgramList.currentItem;
	}

	if (itemForDrop != null && !itemForDrop.isOptional()) {
	    JOptionPane.showMessageDialog(null,
					  "Can not delete a mandatory observation!"
					  );
	    selectedItem = null;
	    obsList.clearSelection();
	    return;
	}
	
	evt.acceptDrop(DnDConstants.ACTION_MOVE);
	evt.getDropTargetContext().dropComplete(true);
	return;
    }

    /* Drop Target Interface */
    public void dropActionChanged(DropTargetDragEvent evt){
    }

    /**
     * a drag gesture has been initiated
     * 
     */
  
    public void dragGestureRecognized( DragGestureEvent event) {
	Object selected = obsList.getSelectedValue();
	selectedItem = (SpItem)selected;
	if ( selected != null ){
	    StringSelection text = new StringSelection( selected.toString());
        
	    // as the name suggests, starts the dragging
	    dragSource.startDrag (event, DragSource.DefaultMoveNoDrop, text, this);
	} else {
	    System.out.println( "nothing was selected");   
	}
    }

    public void dragEnter (DragSourceDragEvent event) {
    }

    public void dragOver(DragSourceDragEvent evt){
	/* Chnage the cursor to indicate drop allowed */
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    public void dragExit(DragSourceEvent evt){
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    public void dropActionChanged(DragSourceDragEvent evt){
    }

    public void dragDropEnd(DragSourceDropEvent evt){
	if (evt.getDropSuccess() == true) {
	    SpObs obs = (SpObs) obsList.getSelectedValue();
	    if (obs != null) {
		if (obs.isOptional() == true) {
		    removeCurrentNode();
		    selectedItem=null;
		}
	    }
	}
    }

  public JButton getRunButton () {return run;}

}
