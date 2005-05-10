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
// import om.console.*;
// import om.util.*;

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
//import ocs.utils.*;
import org.apache.log4j.Logger;
import edu.jach.qt.utils.MSBDoneDialog;


/**
   final public class programTree is a panel to select
   an observation from a List object. 

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
    private static JButton		run;
    private static JButton		engButton;
    private JButton                     xpand;
    private JTree			tree;
    private static JList		obsList;
    private DefaultListModel		model;
    private JScrollPane			scrollPane = new JScrollPane();;
    private static SpItem		_spItem;
    private DefaultMutableTreeNode	root;
    private DefaultTreeModel		treeModel;
    private TreeViewer                  tv = null;
    private TreePath			path;
    private String			projectID    = null;
    private String                      checkSum     = null;
    private DropTarget                  dropTarget   = null;
    private DragSource                  dragSource   = null;
    private TrashCan                    trash        = null;
    public static  SpItem               selectedItem = null;
    public static  SpItem               obsToDefer;
    private SpItem                      instrumentContext;
    private Vector                      targetContext;
    private final String                editText = "Edit Attribute...";
    private final String                scaleText = "Scale Exposure Times...";
    private String                      rescaleText = "Re-do Scale Exposure Times";
    private final String                engString = "Send for Engineering";
    private Vector                      haveScaled   = new Vector(); 
    private Vector                      scaleFactors = new Vector(); 
    private JMenuItem                   edit;
    private JMenuItem                   scale;
    private JMenuItem                   scaleAgain;
    private JMenuItem                   sendToEng;
    private JPopupMenu                  scalePopup;

    private JPopupMenu                  msbDonePopup;
    private JMenuItem                   msbDoneMenuItem;
    private final String                msbDoneText = "Accept/Reject this MSB";

    private boolean                     anObservationHasBeenDone = false;
    private boolean                     msbDone = false;  // The MSB has been either accepted or rejected
    private boolean                     _useQueue = true;
    private File                        msbPendingFile;
    private String                      msbPendingDir = File.separator +
	                                                System.getProperty("telescope").trim().toLowerCase() +
	                                                "data" + 
	                                                File.separator +
	                                                "orac_data" +
	                                                File.separator;

    /** public programTree() is the constructor. The class
	has only one constructor so far.  a few thing are done during
	the construction. They are mainly about adding a run button and
	setting up listeners.
      
	@param  none
	@return none
	@throws none 
    */
    public ProgramTree()  {

	//     scm = SequenceManager.getHandle();

	// Ensure nothing is selected 
	selectedItem = null;

	Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
	setBorder(new TitledBorder(border, "Fetched Science Program (SP)", 
				   0, 0, new Font("Roman",Font.BOLD,12),Color.black));
	setLayout(new BorderLayout() );

	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	gbc = new GridBagConstraints();
      
	engButton = new JButton("Send to Queue");
	engButton.setMargin(new Insets(5,10,5,10));
	engButton.setEnabled(true);
	engButton.addActionListener(this);

	xpand = new JButton("Expand Observation");
	xpand.setMargin(new Insets(5,10,5,10));
	xpand.addActionListener(this);

	dropTarget=new DropTarget();
	try{
	    dropTarget.addDropTargetListener(this);
	}catch(TooManyListenersException tmle){
	    logger.error("Too many drop target listeners", tmle);
	}

	trash = new TrashCan();
	trash.setDropTarget(dropTarget);

	dragSource = new DragSource();

	// Create a popup menu 
	scalePopup = new JPopupMenu();
	edit = new JMenuItem (editText);
	edit.addActionListener(this);
	scalePopup.add (edit);
	scale = new JMenuItem (scaleText);
	scale.setEnabled(false);
	scale.addActionListener(this);
	scalePopup.add (scale);
	scaleAgain = new JMenuItem (rescaleText);
	scaleAgain.addActionListener(this);
	scaleAgain.setEnabled(false);
	scalePopup.add (scaleAgain);
	sendToEng = new JMenuItem(engString);
	sendToEng.addActionListener(this);
	scalePopup.add (sendToEng);

	msbDonePopup = new JPopupMenu();
	msbDoneMenuItem = new JMenuItem(msbDoneText);
	msbDoneMenuItem.addActionListener(this);
	msbDonePopup.add(msbDoneMenuItem);

	gbc.fill = GridBagConstraints.NONE;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.left = 10;
	gbc.insets.right = 0;
	add(trash, gbc, 1, 1, 1, 1);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.left = 0;
	gbc.insets.right = 0;
	gbc.insets.bottom = 20;
	add(engButton, gbc, 0, 1, 1, 1);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.left = 0;
	gbc.insets.right = 0;
	gbc.insets.bottom = 20;
	add(xpand, gbc, 0, 2, 1, 1);
    }

    /**
     * Set the <code>projectID</code> to a specified value.
     * @param projectID  The value to set.
     */
    public void setProjectID(String projectID) {
// 	this.projectID = projectID;
	this.projectID = ((SpProg)_spItem).getProjectID();
	if (this.projectID == null)
	    this.projectID = projectID;
    }

    public static SpItem getCurrentItem() {
	return _spItem;
    }

    /**
     * Set the <code>checkSum</code> to a specified value.
     * @param checksum  The value to set.
     */
    public void setChecksum(String checksum) {
// 	this.checkSum = checksum;
	this.checkSum = XmlUtils.getChecksum(_spItem.toXML());
	if (this.checkSum == null)
	    this.checkSum = checksum;
    }

    /**
     * Set the "Send for Execution" to (dis)abled.
     * @param  flag  <code>true</code> to enable execution.
     */
    public static void setExecutable (boolean flag) {
// 	if (TelescopeDataPanel.DRAMA_ENABLED) {
	    logger.debug ( "In ProgramTree.setExecutable(); setting run.enabled to "+flag);
	    engButton.setEnabled(flag);
	    if ( flag == false ) {
		engButton.setToolTipText ( "Disabled due to edited time constraint" );
		ToolTipManager.sharedInstance().setInitialDelay(250);
	    }
	    else {
		engButton.setToolTipText ( null );
	    }
// 	}
    }

    /**
     * Set the Trash Can image.
     * @param label  The <code>JLabel</code> with which to associate the image.
     * @exception Exception is unabe to set the image.
     */
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
     * Add a compnent to the <code>GridBagConstraints</code>
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
	if (source == xpand) {
	    SpItem itemToXpand;
	    if (selectedItem == null && DeferredProgramList.currentItem == null) {
		return;
	    }
	    else if (selectedItem == null) {
		itemToXpand = DeferredProgramList.currentItem;
	    }
	    else {
		itemToXpand = selectedItem;
	    }

	    if (tv == null) {
		tv = new TreeViewer(itemToXpand);
	    }
	    else {
		tv.update(itemToXpand);
	    }
	}
	else if ( source == engButton ) {
	    _useQueue = true;
	    doExecute();
	}

	if (source instanceof JMenuItem) {
	    JMenuItem thisItem = (JMenuItem) source;
	    if ( thisItem.getText().equals(editText) ) {
		editAttributes();
	    } 
	    else if ( thisItem.getText().equals(scaleText) ) {
		scaleAttributes();
	    } 
	    else if ( thisItem.getText().equals(rescaleText) ) {
		rescaleAttributes();   
	    }
	    else if ( thisItem.getText().equals( engString ) ) {
		_useQueue = false;
                doExecute();
            }
	    else if (thisItem.getText().equals(msbDoneText)) {
		if (projectID != null && 
		    projectID != ""   &&
		    checkSum  != null && 
		    checkSum  != ""   &&
		    ( System.getProperty("telescope").equalsIgnoreCase("ukirt") || instrumentContext instanceof SpInstHeterodyne )  && 
		    anObservationHasBeenDone == true &&
		    msbDone == false &&
		    TelescopeDataPanel.DRAMA_ENABLED) { 
		    msbDone = showMSBDoneDialog();
		}
	    }
	}
    }

    public void doExecute() {
	SpItem item = null;
	boolean isDeferred = false;
	boolean failed = false;

	Thread t = null;

	if ( selectedItem == null && DeferredProgramList.currentItem == null ) {
	    JOptionPane.showMessageDialog(null,
		    "You have not selected an observation!",
		    "Please select an observation.",
		    JOptionPane.ERROR_MESSAGE);
	    return;
	}
	else if (selectedItem == null) {
	    isDeferred =  true;
	    item = DeferredProgramList.currentItem;
	}
	setExecutable(false);
	engButton.setToolTipText("Run button disabled during execution");
	if (System.getProperty("telescope").equalsIgnoreCase("ukirt")) {
	    try {
		ExecuteUKIRT execute = new ExecuteUKIRT(_useQueue);
		t = new Thread(execute);
		t.start();
		t.join();
		File failFile = new File ("/ukirtdata/orac_data/deferred/.failure");
		if (failFile.exists()) {
		    failed = true;
		    if ( failFile.length() > 0 ) {
			StringBuffer error = new StringBuffer();
			try {
			    // Read the information from the filure file
			    BufferedReader rdr = new BufferedReader( new FileReader(failFile) );
			    String line;
			    while ( ( line = rdr.readLine() ) != null ) {
				error.append(line);
				error.append('\n');
			    }
			    new ErrorBox( this, "Failed to Execute. Error was \n" + error.toString() );
			}
			catch ( IOException ioe ) {
			    // If we failed, output a default error message and reset the error buffer
			    new ErrorBox(this, "Failed to Execute. Check log using View>Log menu button.");
			}		    
		    }
		    else {
			new ErrorBox(this, "Failed to Execute. Check log using View>Log menu button.");
		    }
		}
		if ( ! _useQueue ) {
		    if (!isDeferred && !failed) {
		        msbPendingFile = new File (msbPendingDir+projectID+"_"+checkSum+".pending");
 		        anObservationHasBeenDone = true;
		        markAsDone(obsList.getSelectedIndex());
		    }
		    else if (!failed) {
		        DeferredProgramList.markThisObservationAsDone(item);
		    }
	
		    if ( !isDeferred && 
		         IsModelEmpty() && 
		         TelescopeDataPanel.DRAMA_ENABLED) {
		         msbDone = showMSBDoneDialog();
		    } // end of if ()
		}
		else if ( !(failed) ) {
		    // We are using the queue, so assime the observation is done
		    msbDone = true;
		}

		setExecutable(true);
	    }
	    catch (Exception e) {
		logger.error("Failed to execute",e);
		if ( t!=null && t.isAlive() ) {
		    logger.info("Last execution still running");
		}
		setExecutable(true);
		return;
	    }
	    setExecutable(true);
	}
	else if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
	    if ( ExecuteJCMT.isRunning() ) {
		setExecutable (false);
		logger.debug ( "Disabling run button since ExecuteJCMT is still running" );
		engButton.setToolTipText("Button disabled during execution");
		return;
	    }
	    else {
		try {
		    if (isDeferred) {
			new ExecuteInThread (item, isDeferred).start();
		    }
		    else {
			new ExecuteInThread (_spItem, isDeferred).start();
		    }
		}
		catch (Exception e) {
		    logger.error("Error running task", e);
		    setExecutable(true);
		}
	    }
	    setExecutable(true);
	}
   }

    private void markAsDone(int index) {
	SpObs obs = (SpObs)model.getElementAt(index);
	String title = obs.getTitle();
	// Search through and see if we have a remaining of the form "(nX)"
	if (title.endsWith("X)")) {
	    // Find the index of the last space character
	    int lastSpace = title.lastIndexOf(" ");
	    title = title.substring(0, lastSpace);
	}

	if (! (title.endsWith("*")) ) {
	    title = title+"*";
	    obs.setTitleAttr(title);
	    model.setElementAt(obs, index);
	}
    }

    private boolean IsModelEmpty() {
	for (int i=0; i<model.getSize(); i++) {
	    SpObs obs = (SpObs)model.getElementAt(i);
	    String obsName = obs.getTitle();
	    if (obsName.endsWith("X)")) {
		// Find the index of the last space character
		int lastSpace = obsName.lastIndexOf(" ");
		obsName = obsName.substring(0, lastSpace);
	    }
	    
	    if (!(obsName.endsWith("*"))) {
		return false;
	    }
	}
	return true;
    }

    private int getRemainingObservations() {
	int nRemaining=0;
	for (int i=0; i<model.getSize(); i++) {
	    SpObs obs = (SpObs)model.getElementAt(i);
	    String obsName = obs.getTitle();
	    if (obsName.endsWith("X)")) {
		// Find the index of the last space character
		int lastSpace = obsName.lastIndexOf(" ");
		obsName = obsName.substring(0, lastSpace);
	    }
	    if (!(obsName.endsWith("*"))) {
		nRemaining++;
	    }
	}
	return nRemaining;
    }
  

    /**
       public void addTree(String title,SpItem sp) is a public method
       to set up a JTree GUI bit for a science program object in the panel
       and to set up a listener too
     
       @param String title and SpItem sp
       @return  none
       @throws none
       @deprecated  Replaced by {@link #addList(SpItem)}
     
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

    /**
     * Set up the List GUI and populate it with the results of a query.
     * @param sp  The list of obervations in the MSB.
     */
     public void addList(SpItem sp) {

	// Check if there is already an existing model and whether it still has
	// observations to perform
	 if ( instrumentContext instanceof SpInstHeterodyne && HTMLViewer.visible() ) {
	     return;
	 }

	if ( model != null   && 
	     msbDone == false &&
	     ( System.getProperty("telescope").equalsIgnoreCase("ukirt") || instrumentContext instanceof SpInstHeterodyne) && 
	     anObservationHasBeenDone == true &&
	     TelescopeDataPanel.DRAMA_ENABLED ) {
	    if (sp != null) {
		msbDone = showMSBDoneDialog();
		// Now check whether we can proceed
		if (!msbDone) {
		    logger.info("Cancelled loading of new MSB");
		    return;
		}
	    }
	    else {
		// If the input is null we are trying to empty the list.  This
		// should only happen at startup (in which case model should be
		// null and we should never get here), or when the user has clicked
		// on the "Set Default" button, in which case they were probably
		// searching for programs to do later in the night.
		return;
	    }
	}

	// If we get here we should reinitialise with the new argument
	anObservationHasBeenDone = false;
	msbDone = false;
	_spItem = sp;

	getContext(sp);
	model = new DefaultListModel();

	if ( _spItem != null ) {
	    Vector obsVector =  SpTreeMan.findAllItems(sp, "gemini.sp.SpObs");
	    
	    Enumeration e = obsVector.elements();
	    while (e.hasMoreElements() ) {
		model.addElement(e.nextElement());
	    } // end of while ()
	}
	else {
	    model.clear();
	}

	obsList = new JList(model);
// 	obsList.addFocusListener ( new FocusAdapter() {} );
 	ToolTipManager.sharedInstance().registerComponent(obsList);
	ToolTipManager.sharedInstance().setDismissDelay(3000);
	obsList.setCellRenderer(new ObsListCellRenderer());
	obsList.setToolTipText( "<html>Optional observations are shown in GREEN<br>Calibrations which have been done are shown in BLUE<br>Suspended MSBs are shown in RED</html>");
	MouseListener ml = new MouseAdapter()
	    {
		public void mouseClicked(MouseEvent e)
		{
		    if (e.getClickCount() == 2) {
			_useQueue = true;
			doExecute();
		    }
		    else if (e.getClickCount() == 1 && 
			     (e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK ) {
			if (selectedItem != obsList.getSelectedValue() ) {
			    // Select the new item
			    selectedItem = (SpItem) obsList.getSelectedValue();
			    DeferredProgramList.clearSelection();
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
		}

		public void mousePressed(MouseEvent e) {
		    DeferredProgramList.clearSelection();
		    enableList(false);
		}
		public void mouseReleased(MouseEvent e) {
		    enableList(true);
		}
	    };
	obsList.addMouseListener(ml);
	MouseListener popupListener = new PopupListener();
	obsList.addMouseListener(popupListener);
	if (model.size() > 0) {
	    obsList.setSelectedIndex(0);
	    selectedItem = (SpItem) obsList.getSelectedValue();
	}

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

    /**
     * Clear the selection from the Prgram Tree List.
     */
    public static void clearSelection() {
	obsList.clearSelection();
	selectedItem = null;
    }

    /**
     * Get the current <code>JTree</code>.
     * @return The current tree structure.
     * @deprecated - this class now implements a list, not a tree. Not Replaced
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
     @deprecated Not replaced.
      
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
       @deprecated Not replaced.
     
    */
    public void valueChanged(TreeSelectionEvent event)
    {
	if( event.getSource() == tree )
	    {
		// Display the full selection path
		path = tree.getSelectionPath();
            }
    }

    /**
     * Implementation opf <code>KeyListener</code> interface.
     * If the delete key is pressed, removes the currently selected item.
     */
    public void keyPressed(KeyEvent e) {
	if( (e.getKeyCode() == KeyEvent.VK_DELETE))
	    removeCurrentNode();
      
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     */
    public void keyReleased(KeyEvent e) { }

    /**
     * Implementation of <code>KeyListener</code> interface.
     */
    public void keyTyped(KeyEvent e) { }

    /**
     * Remove the currently selected node. 
     */
    public void removeCurrentNode() {

	SpObs item = (SpObs)obsList.getSelectedValue();

 	Vector obsV = SpTreeMan.findAllItems(_spItem, "gemini.sp.SpObs");
	int i;
	SpObs [] obsToDelete = {(SpObs)obsV.elementAt(obsList.getSelectedIndex())};
	try {
	    if ( item != null && SpTreeMan.evalExtract(obsToDelete) == true) {
		SpTreeMan.extract(obsToDelete);
		((DefaultListModel)obsList.getModel()).removeElementAt(obsList.getSelectedIndex());
	    }
	    else if (item == null) {
		JOptionPane.showMessageDialog(this,
					      "No Observation to remove",
					      "Message", JOptionPane.INFORMATION_MESSAGE);
		return;
	    }
	    else {
		JOptionPane.showMessageDialog(this,
					      "Encountered a problem deleting this observation",
					      "Message", JOptionPane.WARNING_MESSAGE);
	    }
	}
	catch (Exception e) {
	    logger.error ("Exception encountered while deleting observation", e);
	}
    }
   
    /**
     * public void getItems (SpItem spItem,DefaultMutableTreeNode node)
     * is a public method to add ALL the items of a sp object into the
     * JTree *recursively*.
     *   
     *   @param SpItem spItem,DefaultMutableTreeNode node
     *   @return  none
     *   @throws none
      
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
	public void findItems (SpItem spItem,DefaultMutableTreeNode node)
	is a public method to find a named item in the SpItem list.
      
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

    /**
     * private void editAttributes()
     *
     * Invokes the attribute editor on the current item, as long as that
     * item is an observation.
     **/
    private void editAttributes() {
	
	// Recheck that this is an observation
	if (selectedItem.type()==SpType.OBSERVATION) {

	    setExecutable(false);

	    SpObs observation = (SpObs) selectedItem;

	    try {
		if (!observation.equals(null)) {
		    new AttributeEditor(observation, new javax.swing.JFrame(), true).show();
		} 
		else {
		    JOptionPane.showMessageDialog(this,
						  "Current selection is not an observation.",
						  "Not an Obs!",
						  JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	    catch (Exception e) {
		logger.error("Error instantiating AttributeEditor", e);
	    }
	    finally {
		setExecutable(true);
	    }
	}
    }
 
    /**
     * private void scaleAttributes()
     *
     * Invokes the attribute scaler on the current item, as long as that
     * item is an observation.
     **/
    private void scaleAttributes() {
	if (selectedItem == null || selectedItem.type() != SpType.OBSERVATION ) {
	    return;
	}
	
	setExecutable(false);
	    
	SpObs observation = (SpObs) selectedItem;
	if (!observation.equals(null)) {
	    new AttributeEditor(observation, new javax.swing.JFrame(), true,
				"EXPTIME",
				haveScaled.contains(observation),
				lastScaleFactor(),
				false).show();
	    double sf = AttributeEditor.scaleFactorUsed();
	    if (sf > 0) {
		haveScaled.addElement(observation);
		scaleFactors.addElement(new Double(sf));
		scaleAgain.setEnabled(true);
		rescaleText = "Re-do Scale Exposure Times (x" + sf + ")";
		scaleAgain.setText(rescaleText);
	    }
	} 
	else {
	    JOptionPane.showMessageDialog(this,
					  "Current selection is not an observation.",
					  "Not an Obs!",
					  JOptionPane.INFORMATION_MESSAGE);
	}
	setExecutable(true);
    }
    
  
    /**
     * private void rescaleAttributes()
     *
     * Reinvokes the attribute scaler on the current item, as long as that
     * item is an observation.
     **/
    private void rescaleAttributes() {
	if (selectedItem == null || selectedItem.type() != SpType.OBSERVATION ) {
	    return;
	}
	
	setExecutable(false);
	
	SpObs observation = (SpObs) selectedItem;
	if (!observation.equals(null)) {
	    new AttributeEditor(observation, new javax.swing.JFrame(), true,
				"EXPTIME",
				haveScaled.contains(observation),
				lastScaleFactor(),
				true).show();
	    double sf = AttributeEditor.scaleFactorUsed();
	    if (sf > 0) {
		haveScaled.addElement(observation);
		scaleFactors.addElement(new Double(sf));
	    }
	} 
	else {
	    JOptionPane.showMessageDialog(this,
					  "Current selection is not an observation.",
					  "Not an Obs!",
					  JOptionPane.INFORMATION_MESSAGE);
	}
	setExecutable(true);
    }

    private Double lastScaleFactor() {
	if (scaleFactors.size() == 0) {
	    if (AttributeEditor.scaleFactorUsed() > 0) {
		return new Double(AttributeEditor.scaleFactorUsed());
	    } 
	    else {
		return new Double(1.0); 
	    }
	} 
	else {
	    return (Double)scaleFactors.elementAt(scaleFactors.size()-1);
	}
    }

    private void getContext(SpItem item) {
	if (item == null) {
	    instrumentContext = null;
	    targetContext     = null;
	}
	else {
	    Vector obs  = SpTreeMan.findAllItems(item, "gemini.sp.SpObs");
	    instrumentContext = SpTreeMan.findInstrument((SpObs)obs.firstElement());
	    targetContext     = SpTreeMan.findAllItems(item, "gemini.sp.obsComp.SpTelescopeObsComp");
	}
    }

    /**
     * Convert eacg observation in an SpMSB to a standalone thing.
     * @param xmlString  The SpProg as an XML string.
     * @return           The translated SpProg, or the original input on failure.
     */
    public static SpItem convertObs(SpItem item) {
	/*
	 * Get all of the observation, instrument and target fields
	 */
	SpItem _item = item;
	SpItem msb = ((SpItem)SpTreeMan.findAllItems(_item, "gemini.sp.SpObs").firstElement()).parent();
	Vector obs  = SpTreeMan.findAllItems(_item, "gemini.sp.SpObs");
	Vector targ = SpTreeMan.findAllItems(_item, "gemini.sp.obsComp.SpTelescopeObsComp");
	Vector iter = SpTreeMan.findAllItems(msb, "gemini.sp.iter.SpIterFolder");
	// 	SpObsContextItem msb  = (SpObsContextItem)((SpItem)obs.firstElement()).parent();
	if (msb == null) {
	    logger.warn("Current Tree does not seem to contain an observation context!");
	    return item;
	}
	SpItem inst = SpTreeMan.findInstrument((SpObs)obs.firstElement());
	if (inst == null) {
	    logger.warn("Current Tree does not seem to contain an instrument!");
	    return item;
	}
	
	SpItem localInst;
	SpItem localTarget;
	SpInsertData spid;
	Object [] objArray = obs.toArray();
	SpObs  [] newObs = new SpObs [obs.size()];
	for (int i=0; i<obs.size(); i++) {
	    newObs[i] = (SpObs)objArray[i];
	}
	SpItem [] iterator = new SpItem [iter.size()];
	objArray = iter.toArray();
	for (int i=0; i<iter.size(); i++) {
	    iterator[i] = (SpItem)objArray[i];
	}
	
       	SpTreeMan.extract((SpItem []) newObs);
       	SpTreeMan.extract((SpItem) msb);
       	SpTreeMan.extract(iterator);
	for (int i=0; i<obs.size(); i++) {
	    if (SpTreeMan.findInstrumentInContext((SpObs)newObs[i]) == null) {
		/*
		 * The current observation does not contain an instrument
		 * so add the already found one.
		 */
		spid = SpTreeMan.evalInsertInside(inst, (SpObs)newObs[i]);
		SpTreeMan.insert(spid);
	    }
	    if (SpTreeMan.findTargetListInContext((SpObs)newObs[i]) == null) {
		/*
		 * The current observation does not contain a target
		 * so add the already found one.
		 */
		spid = SpTreeMan.evalInsertInside((SpItem)targ.firstElement(), (SpObs)newObs[i]);
		SpTreeMan.insert(spid);
	    }
	    /*
	     * Now we have updated all of the obs, try and replace the obs in the tree
	     */
	    if (SpTreeMan.evalExtract(item) == true) {
		spid = SpTreeMan.evalInsertInside(newObs[i], (SpItem)msb);
		SpTreeMan.insert(spid);
	    }
	}
	
	return item;
    }
  

    class PopupListener extends MouseAdapter {

	public void mousePressed (MouseEvent e) {

	    // If this was not the right button just return immediately.
	    if (! e.isPopupTrigger() ) {
		return;
	    }

	    if (selectedItem == null && 
		( System.getProperty("telescope").equalsIgnoreCase("ukirt") || instrumentContext instanceof SpInstHeterodyne) &&
			TelescopeDataPanel.DRAMA_ENABLED) {
		msbDone = showMSBDoneDialog();
	    }
	    // If this is an observation then show the popup
	    else if (selectedItem != null &&
		     selectedItem.type()==SpType.OBSERVATION) {
		scalePopup.show (e.getComponent(), e.getX(), e.getY());
	    }   
	}	
    }    

    /**
     * Implementation of <code>DropTargetListener</code> Interface 
     * @param evt  <code>DropTargetDragEvent</code> event
     */
    public void dragEnter(DropTargetDragEvent evt){
    }
  
    /**
     * Implementation of <code>DropTargetListener</code> Interface 
     * @param evt  <code>DropTargetEvent</code> event
     */
    public void dragExit(DropTargetEvent evt){
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface 
     * @param evt  <code>DropTargetDragEvent</code> event
     */
    public void dragOver(DropTargetDragEvent evt){
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface 
     * @param evt  <code>DropTargetDropEvent</code> event
     */
    public void drop(DropTargetDropEvent evt){
	SpObs itemForDrop;
	if (selectedItem != null) {
	    itemForDrop = (SpObs)selectedItem;
	}
	else {
	    itemForDrop = (SpObs)DeferredProgramList.currentItem;
	}

	if (itemForDrop != null && !itemForDrop.isOptional()) {
	    JOptionPane.showMessageDialog(this,
					  "Can not delete a mandatory observation!"
					  );
	    return;
	}
	
	evt.acceptDrop(DnDConstants.ACTION_MOVE);
	evt.getDropTargetContext().dropComplete(true);
	return;
    }

    /**
     * Implementation of <code>DropTargetListener</code> Interface 
     * @param evt  <code>DropTargetDragEvent</code> event
     */
    public void dropActionChanged(DropTargetDragEvent evt){
    }

    /**
     * Implementation of <code>DragGestureListener</code> Interface
     * @param event  <code>DragGestureEvent</code> event
     * 
     */
  
    public void dragGestureRecognized( DragGestureEvent event) {
	InputEvent ipe = event.getTriggerEvent();
	if (ipe.getModifiers() != ipe.BUTTON1_MASK ) {
	    return;
	}
	Object selected = obsList.getSelectedValue();
	enableList(false);
	DeferredProgramList.clearSelection();
	selectedItem = (SpItem)selected;
	if ( selected != null ){
	    SpItem tmp = _spItem.deepCopy();
	    obsToDefer   = selectedItem.deepCopy();
	    SpInsertData spid;
	    if (SpTreeMan.findInstrumentInContext(obsToDefer) == null && instrumentContext !=  null) {
		spid = SpTreeMan.evalInsertInside(instrumentContext, obsToDefer);
		SpTreeMan.insert(spid);
	    }
	    if ( SpTreeMan.findTargetListInContext(obsToDefer) == null  && 
		 targetContext != null &&
		 targetContext.size() != 0) {
		spid = SpTreeMan.evalInsertInside((SpItem)targetContext.firstElement(), obsToDefer);
		SpTreeMan.insert(spid);
	    }
	    _spItem = tmp;
	    StringSelection text = new StringSelection( obsToDefer.toString());
        
	    // as the name suggests, starts the dragging
	    dragSource.startDrag (event, DragSource.DefaultMoveNoDrop, text, this);
	} else {
	    logger.warn( "nothing was selected to drag");   
	}
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     * @param event  <code>DragSourceDragEvent</code> event
     * 
     */
    public void dragEnter (DragSourceDragEvent event) {
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     * @param evt  <code>DragSourceDragEvent</code> event
     * 
     */
    public void dragOver(DragSourceDragEvent evt){
	/* Chnage the cursor to indicate drop allowed */
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     * @param evt  <code>DragSourceEvent</code> event
     * 
     */
    public void dragExit(DragSourceEvent evt){
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     * @param evt  <code>DragSourceDragEvent</code> event
     * 
     */
    public void dropActionChanged(DragSourceDragEvent evt){
    }

    /**
     * Implementation of <code>DragSourceListener </code> Interface
     * @param evt  <code>DragSourceDropEvent</code> event
     * 
     */
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
	enableList(true);
    }

    /**
     * Request whether we can shutdown the QT at this point.  It basically calls the MSBDoneDialog
     * and will return "true" if we can shutdown, or false otherwise.
     */
    public boolean shutDownRequest() {
	if ( ( System.getProperty("telescope").equalsIgnoreCase("ukirt") || // We are using ukirt
	       instrumentContext instanceof SpInstHeterodyne )  &&          // We have a heterodyne component
	     anObservationHasBeenDone &&                                    // At least one observation has been 
                                                                            // done from the current MSB
	     !msbDone &&                                                    // The MSB has not been marked as done
	     TelescopeDataPanel.DRAMA_ENABLED ) {                           // We are not running scenario.
	    return ( showMSBDoneDialog() );
	}
	else {
	    return true;  // We can safely exit
	}
    }

    private boolean showMSBDoneDialog() {
	boolean done = false;

	Container parent = this.getParent();
	while (parent != null &&
	       (!(parent instanceof java.awt.Frame))) {
	    parent = parent.getParent();
	}
	// Create the comm files
	File cancelFile = new File ("/tmp/cancel");
	File acceptFile = new File ("/tmp/accept");
	File rejectFile = new File ("/tmp/reject");
	File noDataFile = new File ("/tmp/noData");
// 	String title = ((SpProg)_spItem).getTitle();
	String title = "<unknown>";
	if ( SpTreeMan.findAllItems(_spItem, "gemini.sp.SpMSB").size() > 0) {
	    title = ((SpMSB) SpTreeMan.findAllItems(_spItem, "gemini.sp.SpMSB").firstElement()).getTitle();
	    checkSum = ((SpMSB) SpTreeMan.findAllItems(_spItem, "gemini.sp.SpMSB").firstElement()).getChecksum();
	}
	else if ( SpTreeMan.findAllItems(_spItem, "gemini.sp.SpObs").size() > 0) {
	    title = ((SpObs) SpTreeMan.findAllItems(_spItem, "gemini.sp.SpObs").firstElement()).getTitle();
	    checkSum = ((SpObs) SpTreeMan.findAllItems(_spItem, "gemini.sp.SpObs").firstElement()).getChecksum();
	}
	projectID = ((SpProg)_spItem).getProjectID();
	try {
	    cancelFile.delete();
	    cancelFile.createNewFile();
	    acceptFile.delete();
	    acceptFile.createNewFile();
	    rejectFile.delete();
	    rejectFile.createNewFile();
	    noDataFile.delete();
	    noDataFile.createNewFile();
	}
	catch (IOException ioe) {
	    logger.warn ("Unable to create one of the MSBDoneDialog com files");
	}
	String msg = "Creating MSB Done popup for Project "+projectID+", MSB "+title+", chksum = "+checkSum;
	logger.info (msg);
	MSBDoneDialog mdd = new MSBDoneDialog ((Frame)parent, 
					       projectID, 
					       title,
					       checkSum);
	// See which comms file exist after accept/reject
	if (cancelFile.exists()) {
	    cancelFile.delete();
	    logger.info ("User opted to cancel");
	}
	else if (noDataFile.exists()) {
	    noDataFile.delete();
	    done=true;
	    anObservationHasBeenDone = false;
	    ((DefaultListModel)obsList.getModel()).clear();
	    logger.info ("User selected 'No Data Taken'");
	}
	else if (acceptFile.exists()) {
	    acceptFile.delete();
	    done = true;
	    anObservationHasBeenDone = false;
	    InfoPanel.searchButton.doClick();
	    ((DefaultListModel)obsList.getModel()).clear();
	    logger.info("User accepted MSB");
	}
	else if (rejectFile.exists()) {
	    rejectFile.delete();
	    done = true;
	    anObservationHasBeenDone = false;
	    InfoPanel.searchButton.doClick();
	    logger.info ("User rejected MSB");
	}
	if ( !anObservationHasBeenDone && msbPendingFile != null ) {
	    msbPendingFile.delete();
	}
	return done;
    }


    public void enableList(boolean flag) {
	obsList.setEnabled(flag);
	repaint();
    }


    public JButton getRunButton () {return engButton;}

    public class ExecuteInThread extends Thread {
	SpItem _item;
        SpItem _deferredItem;
	boolean _isDeferred;

	public ExecuteInThread ( SpItem item, boolean deferred ) {
	    // if this is a deferred observation, then we need to
	    // convert the supplied item, which is an SpObs into
	    // an SpProg
	    if ( deferred ) {
		SpProg root = (SpProg) SpFactory.create(SpType.SCIENCE_PROGRAM);
		root.setPI("observer");
		root.setCountry("JAC");
		root.setProjectID("CAL");
		SpInsertData spID = SpTreeMan.evalInsertInside(item, root);
		if ( spID != null ) {
		    SpTreeMan.insert(spID);
		}
                _deferredItem = item;
		_item = (SpItem)root;
	    }
	    else {
	        _item = item;
	    }
	    _isDeferred = deferred;
	}

	public void run () {
	    ExecuteJCMT execute;
	    boolean failed = false;
	    msbDone = false;

	    File failFile = new File ("/jcmtdata/orac_data/deferred/.failure");
	    execute = ExecuteJCMT.getInstance(_item);
	    if ( execute == null ) return;
	    try {
		failed = execute.run();
	    }
	    catch (Exception e) {
		if (!failFile.exists()) {
		    try { 
			failFile.createNewFile();
		    }
		    catch (IOException ioe) {
			logger.error( "Execution failed and could not return normally" );
			setExecutable (true);
			return;
		    }
		}
	    }

	    if (failFile.exists()) {
		logger.info("Execution failed - Check log messages");
		if ( failFile.length() > 0 ) {
		    StringBuffer error = new StringBuffer();
		    try {
			// Read the information from the filure file
			BufferedReader rdr = new BufferedReader( new FileReader(failFile) );
			String line;
			while ( ( line = rdr.readLine() ) != null ) {
			    error.append(line);
			    error.append('\n');
			}		    
			new PopUp ("JCMT Execution Failed",
				   "Failed to send project for execution; Error was \n" + error.toString(),
				   JOptionPane.ERROR_MESSAGE).start();
		    }
		    catch ( IOException ioe ) {
			// If we failed, output a default error message and reset the error buffer
			new PopUp ("JCMT Execution Failed",
				   "Failed to send project for execution; check log entries using the View>Log button",
				   JOptionPane.ERROR_MESSAGE).start();
		    }
		}
		else {
		    new PopUp ("JCMT Execution Failed",
			       "Failed to send project for execution; check log entries using the View>Log button",
			       JOptionPane.ERROR_MESSAGE).start();
		}
		failed = true;
	    }
	    if (!_isDeferred && !failed) {
		if (instrumentContext instanceof SpInstSCUBA) {
		    model.clear();
		    _spItem = null;
		    selectedItem = null;
		}
		else {
		    // See if the HTMLViewer is still visible.  If it is, put
		    // this thread to sleep for a while.
		    while ( HTMLViewer.visible() ) {
			try {
			    Thread.sleep(500);
			}
			catch ( Exception x) {}
			continue;
		    }
		    // For heterodyne, mark all the observation as done and bring up the popup
		    for (int i=0; i<obsList.getModel().getSize(); i++) {
			markAsDone(i);
			if (TelescopeDataPanel.DRAMA_ENABLED &&
		            System.getProperty("DOMAIN").equals("JAC.jcmt") ) {
			    anObservationHasBeenDone = true;
			    msbPendingFile = new File (msbPendingDir+projectID+"_"+checkSum+".pending");
			    msbDone = showMSBDoneDialog();
			}
		    }
		}
	    }
	    else if (!failed) {
		DeferredProgramList.markThisObservationAsDone(_deferredItem);
	    }
            _deferredItem = null;
	    setExecutable (true);
	    logger.debug ( "Enabling run button since the ExecuteJCMT task has completed" );
	}

	public class PopUp extends Thread{
	    String _message;
	    String _title;
	    int    _errLevel;
	    
	    public PopUp (String title, String message, int errorLevel) {
		_message=message;
		_title = title;
		_errLevel=errorLevel;
	    }

	    public void run() {
		JOptionPane.showMessageDialog(null,
					      _message,
					      _title,
					      _errLevel);
	    }
	}
	    
    }

}
