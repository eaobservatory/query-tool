package edu.jach.qt.gui;

/* QT imports */
import gemini.sp.*;


/* Miscellaneous imports */
/* Standard imports */
import edu.jach.qt.app.*;
import edu.jach.qt.gui.*;
import edu.jach.qt.utils.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.apache.log4j.Logger;
import sun.misc.*;

/**
 * The <code>QtFrame</code> is responsible for how the main JFrame
 * is to look.  It starts 2 panel classes InfoPanel and InputPanel
 * adding them to the left side and top right resrectively.  Also,
 * a JTable is created with a sort model that is sensititve to column 
 * header clicks.  Each click sorts rows in decending order relative to 
 * the column clicked.  A shift-click has the effect of an acsending 
 * sort.  The JTable is placed in the bottom right of the JFrame.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class QtFrame 
    extends JFrame 
    implements PopupMenuListener, ActionListener, MenuListener, ListSelectionListener {

  private static final String 
    WIDGET_CONFIG_FILE = System.getProperty("widgetFile");
  private final static String
    DUMMY_TABLE_DATA = System.getProperty("dummyTable");;

  static Logger logger = Logger.getLogger(QtFrame.class);

  private MSBQueryTableModel	msbQTM;
  private JTable                projectTable;
  private JTable		table;
  private int			selRow;
  private JMenuItem		saveItem;
  private JMenuItem		saveAsItem;
  private JMenuItem		exitItem;
  private JCheckBoxMenuItem     disableAll;
  private JCheckBoxMenuItem	observability;
  private JCheckBoxMenuItem	remaining;
  private JCheckBoxMenuItem	allocation;
//   private JSplitPane		splitPane;
  private GridBagConstraints	gbc;
  private JTabbedPane		tabbedPane;
  private OmpOM			om;
  private WidgetDataBag		widgetBag;
  private Querytool		localQuerytool;
  private InfoPanel		infoPanel;
  private JPopupMenu		popup;
  private TreeMap               calibrationList;
  private JMenu                 calibrationMenu = new JMenu("Calibrations");
  private WidgetPanel           _widgetPanel;
  private int []                tableColumnSizes;
  private boolean               queryExpired = false;


  SwingWorker msbWorker;

  /**
   * Creates a new <code>QtFrame</code> instance.
   *
   * @param wdb a <code>WidgetDataBag</code> value
   * @param qt a <code>Querytool</code> value
   */
  public QtFrame(WidgetDataBag wdb, Querytool qt) {
    widgetBag = wdb;
    localQuerytool = qt;

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    GridBagLayout layout = new GridBagLayout();
    getContentPane().setLayout(layout);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener( new WindowAdapter () {
	    public void windowClosing(WindowEvent e) {
		exitQT();
	    }
	} );

    SignalHandler handler = new SignalHandler () {
	    public void handle (Signal sig) {
		// Handle SIGTERM
		setVisible(false);
		dispose();
		System.exit(0);
	    }
	};
    Signal.handle(new Signal("TERM"), handler);


    try {
//       compInit();
//       splitPane.validate();
//       validateTree();

      om = new OmpOM();
      om.addNewTree(null);

      compInit();
      validateTree();
      tabbedPane.setSelectedIndex(0);


    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * On exit, prompt the user if they want to save any deferred observation, then
   * shutdown.
   */
  public void exitQT() {
      logger.info("QT shutdown by user");
      boolean canExit = true;
      // See if there are any outstanding observations and ask the user what to do with them...
      if (om != null) {
	  canExit = om.checkProgramTree();
      }
      if (!canExit) {
	  JOptionPane.showMessageDialog ( this,
					  "Can not exit the QT until the current MSB is accepted/rejected",
					  "Can not exit QT",
					  JOptionPane.WARNING_MESSAGE);
	  return;
      }
      
      if (DeferredProgramList.deferredFilesExist()) {
	  Object [] options = {"Save", "Delete"};
	  int selection = JOptionPane.showOptionDialog( this,
							"Deferred observations currently exist. Save?",
							"Deferred observations exist",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]
							);
	  if (selection == 1) {
	      // This should be the delete option I think
	      DeferredProgramList.deleteAllFiles();
	  }
      }
      File cacheDir = new File ("/tmp/last_user");
      if (cacheDir.exists() && cacheDir.isDirectory() ) {
	  File [] files = cacheDir.listFiles();
	  for (int i=0; i<files.length; i++) {
	      if (files[i].isFile()) {
		  files[i].delete();
	      }
	  }
      }
      setVisible(false);
      dispose();
      System.exit(0);
  }

  /**
   * Component initialization.
   * Initialises all of the components on the frame.
   * @exception Exception on error.
   */
  private void compInit() throws Exception  {
    gbc = new GridBagConstraints();

    // Check whether deferred Observations currently exist and ask the user if he wants to
    // use these.  If they don't then delete the current files
    if (DeferredProgramList.obsExist()) {
	JOptionPane pane = new JOptionPane ( "Use current deferred Observations?",
		                             JOptionPane.QUESTION_MESSAGE,
					     JOptionPane.YES_NO_OPTION);

	final JDialog dialog = pane.createDialog (this, "Deferred Observations Exist");
	dialog.addWindowListener( new WindowAdapter () {
		 public void windowDeactivated(WindowEvent we) {
		     dialog.toFront();
		 }
        });
	dialog.show();
	int selection;
	Object selectedValue = pane.getValue();
	if ( selectedValue == null ) {
	    // User didn't make a choice so take the safe option and assume
	    // they didn't want to delete
	    selection = JOptionPane.YES_OPTION;
	}
	else {
	    selection = ( (Integer)selectedValue).intValue();
	}
	
	if (selection == JOptionPane.NO_OPTION) {
	    DeferredProgramList.deleteAllFiles();
	}
    }

    //Input Panel Setup
    WidgetPanel inputPanel = new WidgetPanel(new Hashtable(), widgetBag);
    _widgetPanel = inputPanel;
    buildStagingPanel();
    //Table setup
    try {
	msbQTM = new MSBQueryTableModel();
    }
    catch (Exception e) {
	logger.error("Unable to create table model", e);
	exitQT();
    }
    infoPanel = new InfoPanel (msbQTM, localQuerytool, this);


    ProjectTableModel ptm = new ProjectTableModel();
    projectTableSetup(ptm);
    tableSetup();
    JScrollPane resultsPanel = new JScrollPane(table);
    resultsPanel.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE); 
    JScrollPane projectPane = new JScrollPane(projectTable);
    projectPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);

    JSplitPane tablePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					   projectPane,
					   resultsPanel);
    tablePanel.setDividerSize(0);
    tablePanel.validate();


//     infoPanel.setMinimumSize(new Dimension(175, 450));
//     tabbedPane.setMinimumSize(new Dimension(-1, 450));
    tablePanel.setMinimumSize(new Dimension (-1, 100));
//     tablePanel.setPreferredSize(new Dimension (-1, 300));

    JSplitPane topPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT,
					  infoPanel,
					  tabbedPane);
    topPanel.setMinimumSize(new Dimension(-1, 450));
    topPanel.setDividerSize(0);
    topPanel.validate();


    //Build Menu
    buildMenu();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(topPanel, BorderLayout.NORTH);
    getContentPane().add(tablePanel, BorderLayout.CENTER);

//     gbc.fill = GridBagConstraints.HORIZONTAL;
//     gbc.anchor = GridBagConstraints.NORTH;
//     gbc.weightx = 1.0;
//     gbc.weighty = 0.0;
//     add(topPanel, gbc, 0, 0, 2, 2);

//     gbc.fill = GridBagConstraints.NONE;
// //     gbc.anchor = GridBagConstraints.NORTHWEST;
//     gbc.weightx = 0;
//     gbc.weighty = 0.0;
//     add(infoPanel, gbc, 0, 0, 1, 1);

//     gbc.fill = GridBagConstraints.BOTH;
// //     gbc.anchor = GridBagConstraints.NORTH;
//     gbc.weightx = 0.1;
//     gbc.weighty = 0;
//     add(tabbedPane, gbc, GridBagConstraints.RELATIVE, 0, GridBagConstraints.REMAINDER, 1);

//     gbc.fill = GridBagConstraints.BOTH;
//     gbc.anchor = GridBagConstraints.CENTER;
// //     gbc.weightx = 0;
// //     gbc.weighty =0.0 ;
//     add(tablePanel, gbc, 0, 2, 2, 1);

    //Read the config to determine the widgets
    try{
      inputPanel.parseConfig(WIDGET_CONFIG_FILE);
    }
    catch(IOException e){ 
      logger.fatal("Widget Panel Parse Failed", e);
    }

  }

    private void projectTableSetup(ProjectTableModel ptm) {
	Vector columnNames = new Vector();
	columnNames.add("projectid");
	columnNames.add("priority");
	projectTable = new JTable (ptm);
	projectTable.setPreferredScrollableViewportSize( new Dimension (150, -1));
	ToolTipManager.sharedInstance().unregisterComponent(projectTable);
	ToolTipManager.sharedInstance().unregisterComponent(projectTable.getTableHeader());
	projectTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	
	projectTable.setSelectionModel(new ProjectTableSelectionModel(this));

	projectTable.setVisible(true);
    }


  private void tableSetup() {
    final TableSorter sorter = new TableSorter(msbQTM);
    table = new JTable(sorter);
    ToolTipManager.sharedInstance().unregisterComponent(table);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
    sorter.addMouseListenerToHeaderInTable(table);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.setMinimumSize(new Dimension(770,275) );

    // Add a mouse motion listener to the header to cature drag events
    table.getTableHeader().addMouseMotionListener ( new MouseMotionAdapter() {
	    public void mouseDragged(MouseEvent e) {
		updateColumnSizes();
	    }
	});
	    

    ListSelectionModel listMod =  table.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(this);

    popup = new JPopupMenu("MSB");
    JMenuItem menuSendMSB = new JMenuItem("Send MSB to Staging Area" );
    popup.add( menuSendMSB );
    table.add(popup);
    menuSendMSB.addActionListener( this );

    table.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	  
	  msbWorker = new SwingWorker() {
	      Boolean isStatusOK;
	      Integer msbID = new Integer ((String)sorter.getValueAt(selRow, MSBQueryTableModel.MSBID));;

	      public Object construct() {
		    
		InfoPanel.logoPanel.start();
		logger.info("Setting up staging panel for the first time.");
		om.enableList(false);

		if (om == null)
		  om = new OmpOM();

		try {
		  om.setSpItem( localQuerytool.fetchMSB(msbID));
		  isStatusOK = new Boolean(true);
		} catch (NullPointerException e) {
		  isStatusOK = new Boolean(false);
		  om.enableList(true);
		}

		return isStatusOK;  //not used yet
	      }

	      //Runs on the event-dispatching thread.
	      public void finished() { 
		InfoPanel.logoPanel.stop();
		om.enableList(true);

		if ( isStatusOK.booleanValue()) {
		  om.addNewTree(msbID);
		  buildStagingPanel();

		  String projectid = (String) sorter.getValueAt(selRow, MSBQueryTableModel.PROJECTID);
		  String checksum = (String) sorter.getValueAt(selRow, MSBQueryTableModel.CHECKSUM);

		  logger.info("MSB "+msbID+" INFO is: "+projectid+", "+checksum);
		  om.setProjectID(projectid);
		  om.setChecksum(checksum);
		}
		
		else {
		  logger.error("No msb ID retrieved!");
		}
	      }
	    }; //End inner class

	  if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
	      if (selRow != -1) {
		  sendToStagingArea();
	      }

	      else {
		JOptionPane.showMessageDialog(null, "Must select a project summary first!");

	      }
	  }

	  else if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
	    
	    logger.debug("Right Mouse Hit");
	      if ( selRow != -1) {
		popup.show((Component)e.getSource(), e.getX(), e.getY());
	    
	      }
	    
	  }
	}

	public void mousePressed(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    //popup.show((Component)e.getSource(), e.getX(), e.getY());
	  }
	}
	public void mouseReleased(MouseEvent e) {
	  if (e.isPopupTrigger()) {
	    //popup.show((Component)e.getSource(),e.getX(), e.getY());
	  }
	}


      } );
    table.setVisible(true);
  }

    public void initProjectTable() {
	projectTable.getSelectionModel().setSelectionInterval(0,0);
    }

    /**
     * Method used to set the current column sizes.  This should be called
     * before each query.
     */
    public void updateColumnSizes() {
	TableColumnModel tcm = table.getColumnModel();
	tableColumnSizes = new int [table.getColumnCount()];
	for (int i=0; i<msbQTM.getColumnCount(); i++) {
	    tableColumnSizes[i] = tcm.getColumn(i).getWidth();
	}
    }

    /**
     * Method to set the column sizes following a query.
     */
    public void setColumnSizes() {
	TableColumnModel tcm = table.getColumnModel();
	for (int i=0; i<tableColumnSizes.length; i++) {
	    tcm.getColumn(i).setPreferredWidth(tableColumnSizes[i]);
	}
	table.setColumnModel(tcm);
	table.updateUI();
    }

    /**
     * Method to redistribute the column widths to the default values.
     */
    public void setTableToDefault() {
	TableColumnModel tcm = table.getColumnModel();
	for (int i=0; i<msbQTM.getColumnCount(); i++) {
	    tcm.getColumn(i).setPreferredWidth(-1);
	}
	table.setColumnModel(tcm);
    }

    /**
     * Method to get the current table model.
     * @return   The current table model.
     */
    public MSBQueryTableModel getModel() {
	return msbQTM;
    }

    public ProjectTableModel getProjectModel() {
	return (ProjectTableModel)projectTable.getModel();
    }


  /**
   * Sends the selected MSB to the Staging Area.
   * The MSB must have frist been retriebed and selected from the results table 
   * on the QT interface.
   */
  public void sendToStagingArea () {
      if (queryExpired == true) {
	  String [] options = {"Resubmit", "New Query"};
	  int rtn = JOptionPane.showOptionDialog (this,
						  "Query has Expired;\nNew Query Required",
						  null,
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.WARNING_MESSAGE,
						  null,
						  options,
						  options[0]
						  );
	  if (rtn == JOptionPane.YES_OPTION) {
	      // Resubmit the existing query
	      InfoPanel.searchButton.doClick();
	      return;
	  }
	  else {
	      // Reset the panel to the search panel
	      tabbedPane.setSelectedIndex(0);
	      // Clear the summary model
	      msbQTM.clear();
	      // Clear the project model
	      ((ProjectTableModel)projectTable.getModel()).clear();
	      initProjectTable();
	      return;
	  }
      }				
					

    if (table.getSelectedRow() != -1) {

      msbWorker.start();
      om.updateDeferredList();
    }

    else {
      JOptionPane.showMessageDialog(this, "Must select a project summary first!");

    }
    
  }

    /**
     * Method to get the currently selected tab.
     * @return  The integer value of the selected tab.
     */
    public int getSelectedTab() {
	return tabbedPane.getSelectedIndex();
    }

    /**
     * Method to set the selected tab.
     * @param tab   The tab to select.
     */
    public void setSelectedTab(int tab) {
	if ( (tabbedPane.getTabCount() - 1) >= tab) {
	    tabbedPane.setSelectedIndex(tab);
	}
    }
   

    /**
     * Build the Staging Area GUI.
     */
  public void buildStagingPanel() {

    if (tabbedPane == null) {
      tabbedPane = new JTabbedPane(SwingConstants.TOP);
//       remove(splitPane);
      tabbedPane.addTab("Query", _widgetPanel);
      tabbedPane.addTab(om.getProgramName(), om.getTreePanel());
//       add(tabbedPane, gbc, 1, 0, 1, 2);
      validate();
      tabbedPane.setVisible(true);
    }
    
    else {
      tabbedPane.remove(1);
      tabbedPane.addTab(om.getProgramName(), om.getTreePanel());
    }

    tabbedPane.setSelectedIndex(1);
  }


    /**
     *  Method to set the observational parameter to default.
     */
    public void setMenuDefault() {
	if ( disableAll.isSelected() ) {
	    disableAll.doClick();
	}
	if ( !(observability.isSelected()) ) {
	    observability.doClick();
	}
	if ( !(remaining.isSelected()) ) {
	    remaining.doClick();
	}
	if ( !(allocation.isSelected()) ) {
	    allocation.doClick();
	}
    }

    public void setQueryExpired (boolean flag) {
	queryExpired = flag;
    }


  /**
   * The <code>valueChanged</code> method is mandated by the 
   * ListSelectionListener. Called whenever the value of 
   * the selection changes.
   *
   * @param e a <code>ListSelectionEvent</code> value
   */
  public void valueChanged(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {        
      selRow = table.getSelectedRow();
    }
  }
   
  private void printDebugData(JTable table) {
    int numRows = table.getRowCount();
    int numCols = table.getColumnCount();
    javax.swing.table.TableModel model = table.getModel();
 
    System.out.println("Value of data: ");
    for (int i=0; i < numRows; i++) {
      System.out.print("    row " + i + ":");
      for (int j=0; j < numCols; j++) {
	System.out.print("  " + model.getValueAt(i, j));
      }
      System.out.println();
    }
    System.out.println("--------------------------");
  }

  /**
   * This <code>add</code> method is a utility method to add
   * the current Component to the grid bag.
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
    getContentPane().add(c, gbc);      
  }
   
  /**
   * The <code>buildMenu</code> method builds the menu system.
   *
   */
  public void buildMenu() {
    JMenuBar mbar = new JMenuBar();
    setJMenuBar(mbar);
      
    JMenu fileMenu = new JMenu("File");
    fileMenu.addMenuListener(this);

    //JMenu constraints = 
      

    JMenuItem openItem = new JMenuItem("Open");
    openItem.setEnabled(false);
    JMenuItem newItem = new JMenuItem("New");
    newItem.setEnabled(false);    
    saveItem   = new JMenuItem("Save");
    saveItem.setEnabled(false);
    saveAsItem = new JMenuItem("Save As");
    saveAsItem.setEnabled(false);
    exitItem   = new JMenuItem("Exit");
      
    mbar.add(makeMenu (fileMenu, new Object[] {
      newItem, openItem, null, saveItem, saveAsItem,
      null, exitItem }, this)
	     );


    JMenu viewMenu = new JMenu ("View");
    mbar.add(viewMenu);
    JMenuItem columnItem = new JMenuItem("Columns...");
    columnItem.addActionListener(this);
    JMenuItem logItem = new JMenuItem("Log...");
    logItem.addActionListener(this);
    JMenu satMenu = new JMenu("Satellite Image");
    JMenuItem irItem = new JMenuItem("Infra Red");
    irItem.addActionListener(this);
    JMenuItem wvItem = new JMenuItem("Water Vapour");
    wvItem.addActionListener(this);
    satMenu.add(irItem);
    satMenu.add(wvItem);
    viewMenu.add(columnItem);
    viewMenu.add(logItem);
    viewMenu.add(satMenu);


      
    observability = new JCheckBoxMenuItem("Observability",true);
    remaining     = new JCheckBoxMenuItem("Remaining",true);
    allocation    = new JCheckBoxMenuItem("Allocation",true);
    disableAll    = new JCheckBoxMenuItem("Disable All", false);
    JMenuItem cutItem = new JMenuItem("Cut", new ImageIcon("icons/cut.gif"));
    cutItem.setEnabled(false);
    JMenuItem copyItem = new JMenuItem("Copy", new ImageIcon("icons/copy.gif"));
    copyItem.setEnabled(false);
    JMenuItem pasteItem = new JMenuItem("Paste", new ImageIcon("icons/paste.gif"));
    pasteItem.setEnabled(false);

    mbar.add( makeMenu("Edit", new Object[] {
	cutItem, copyItem, pasteItem,
	    null, makeMenu("Constraints", new Object[] { 
		observability,
		    remaining,
		    allocation,
		    null,
		    disableAll
		    }, this) 
	    }, this));
    
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');

    mbar.add(makeMenu ( helpMenu, 
			new Object[] { new JMenuItem("Index", 'I'), 
				       new JMenuItem("About", 'A') },
			this
			));

    calibrationList = CalibrationList.getCalibrations(System.getProperty("telescope"));
    // Get the set of keys:
    Set keys = calibrationList.keySet();
    Iterator keyIter = keys.iterator();
    while ( keyIter.hasNext()  ) {
	JMenuItem item = new JMenuItem((String)keyIter.next());
	item.addActionListener(this);
	calibrationMenu.add(item);
    }
    calibrationMenu.addMenuListener(this);
    mbar.add(calibrationMenu);
  }

  /**
   * <code>menuSelected</code> method is an action triggered when a 
   * menu is selected.
   *
   * @param evt a <code>MenuEvent</code> value
   */
  public void menuSelected(MenuEvent evt) {  
      JMenu source = (JMenu)evt.getSource();
      if (source.getText().equals("Calibrations")) {
	  Component [] cals = calibrationMenu.getMenuComponents();
//       	  if (tabbedPane != null && tabbedPane.getSelectedIndex() > 0) {
      	  if ( tabbedPane != null ) {
	      for (int iloop=0;iloop<cals.length; iloop++) {
		  cals[iloop].setEnabled(true);
	      }
	  }
	  else {
	      for (int iloop=0;iloop<cals.length; iloop++) {
		  cals[iloop].setEnabled(false);
	      }
	  }
      }
      else if (source.getText().equals("View...")) {
	  ColumnSelector colSelector = new ColumnSelector(msbQTM);
	  ((JMenuItem)source).setSelected(false);
	  return;
      }
  }


  /**
   * <code>menuDeselected</code> method is an action trggered when a 
   * menu is deselected.
   *
   * @param evt a <code>MenuEvent</code> value
   */
  public void menuDeselected(MenuEvent evt) {

  }

  /**
   * The <code>menuCanceled</code> method is an action triggered when a 
   * menu is canceled.
   *
   * @param evt a <code>MenuEvent</code> value
   */
  public void menuCanceled(MenuEvent evt) {

  }

  /**
   * Implementation of the ActionListener interface.
   * Called on changing a Check Box, Selecting a Menu Item,or
   * pressing a button.
   *
   * @param evt an <code>ActionEvent</code> value
   */
  public void actionPerformed(ActionEvent evt) {

    Object source = evt.getSource();

    if ( source instanceof JCheckBoxMenuItem) {
	if ((JCheckBoxMenuItem)source == disableAll) {
	    if (disableAll.isSelected()) {
		allocation.setSelected(false);
		remaining.setSelected(false);
		observability.setSelected(false);
	    }
	    else {
		allocation.setSelected(true);
		remaining.setSelected(true);
		observability.setSelected(true);
	    }
	}
	else {
	    disableAll.setSelected(false);
        }
      localQuerytool.setAllocationConstraint(!allocation.isSelected());
      localQuerytool.setRemainingConstraint(!remaining.isSelected());
      localQuerytool.setObservabilityConstraint(!observability.isSelected());
      if ( allocation.isSelected() && remaining.isSelected() && observability.isSelected() ) {
          // If all selected - set to green light
          java.net.URL url = ClassLoader.getSystemResource("green_light1.gif");
          ImageIcon icon = new ImageIcon(url);
          InfoPanel.searchButton.setIcon( icon );
          table.setBackground (Color.WHITE);
      }
      else if ( !allocation.isSelected() && !remaining.isSelected() && !observability.isSelected() ) {
          // No constraints disabled - set to red
          java.net.URL url = ClassLoader.getSystemResource("red_light1.gif");
          ImageIcon icon = new ImageIcon(url);
          InfoPanel.searchButton.setIcon( icon );
          table.setBackground (Color.RED.darker());
      }
      else {
          // Some constraints diabled - set to amber
          java.net.URL url = ClassLoader.getSystemResource("amber_light1.gif");
          ImageIcon icon = new ImageIcon(url);
          InfoPanel.searchButton.setIcon( icon );
          table.setBackground (Color.YELLOW.darker());
      }
    } 
    
    else if ( source instanceof JMenuItem) {
	JMenuItem thisItem = (JMenuItem)source;
	// Check to see if this came from the calibration list
	if (calibrationList.containsKey(thisItem.getText())) {
	    // Get the "MSB" that this represents
	    SpItem item = MsbClient.fetchMSB((Integer) calibrationList.get(thisItem.getText()));
	    // Add it to the deferred queue
	    DeferredProgramList.addCalibration(item);
	    // Set the tabbed pane to show the Staging Area
	    if (tabbedPane.getTabCount() > 1) {
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
	    }
	}
	else if ( thisItem.getText().equalsIgnoreCase("Index") ) {
	    HelpPage helpPage = new HelpPage();
	}
	else if (thisItem.getText().equalsIgnoreCase("Exit") ) {
	    exitQT();
	}
	else if (thisItem.getText().equalsIgnoreCase("Columns...") ) {
	    ColumnSelector colSelector = new ColumnSelector(this);
	    return;
	}
	else if (thisItem.getText().equalsIgnoreCase("Log...") ) {
	    LogViewer viewer = new LogViewer();
	    viewer.showLog( System.getProperty("QT_LOG_DIR")+"/QT.log" );
	    return;
	}
	else if (thisItem.getText().equalsIgnoreCase("Infra Red") ) {
	    infoPanel.getSatPanel().setDisplay(thisItem.getText());
	}
	else if (thisItem.getText().equalsIgnoreCase("Water Vapour")) {
	    infoPanel.getSatPanel().setDisplay(thisItem.getText());
	}
    }
    else if (source instanceof JButton) {
	JButton thisButton = (JButton) source;

	if (thisButton.getText().equals("Exit"))
	    {
		exitQT();
	    }
	else
	    {
		logger.debug("Popup send MSB");
		msbWorker.start();
	    }
    }
  }

  /**
   * The <code>makeMenu</code> method is a blackbox to make a 
   * generic menu.
   *
   * @param parent an <code>Object</code> value
   * @param items an <code>Object[]</code> value
   * @param target an <code>Object</code> value
   * @return a <code>JMenu</code> value
   */
  public static JMenu makeMenu(Object parent, Object[] items, Object target) {
    JMenu m = null;
    if (parent instanceof JMenu)
      m = (JMenu)parent;
    else if (parent instanceof String)
      m = new JMenu((String)parent);
    else
      return null;
      
    for (int i = 0; i < items.length; i++) {
      if (items[i] == null)
	m.addSeparator();
      else
	m.add(makeMenuItem(items[i], target));
    }

    return m;
  }

  /**
   * The <code>makeMenuItem</code> method is a blackbox for a 
   * generic menu item.
   *
   * @param item an <code>Object</code> value
   * @param target an <code>Object</code> value
   * @return a <code>JMenuItem</code> value
   */
  public static JMenuItem makeMenuItem(Object item, Object target) {
    JMenuItem r = null;
    if (item instanceof String)
      r = new JMenuItem((String)item);
    else if (item instanceof JMenuItem)
      r = (JMenuItem)item;
    else return null;
      
    if (target instanceof ActionListener)
      r.addActionListener((ActionListener)target);
    return r;
  }

  /**
   * Overridden so we can exit when window is closed
   * @param e a <code>WindowEvent</code> value
   */
//   protected void processWindowEvent(WindowEvent e) {
//     super.processWindowEvent(e);
//     if (e.getID() == WindowEvent.WINDOW_CLOSING) {
// 	// Delete the user cache file
// 	File cacheDir = new File ("/tmp/last_user");
// 	if (cacheDir.exists() && cacheDir.isDirectory() ) {
// 	    File [] files = cacheDir.listFiles();
// 	    for (int i=0; i<files.length; i++) {
// 		if (files[i].isFile()) {
// 		    files[i].delete();
// 		}
// 	    }
// 	}
// 	exitQT();
//     }
//   }

    /**
     * Return the (code>WidgetPanel</code> from this frame.
     */
    public WidgetPanel getWidgets() {
	return _widgetPanel;
    }

  // implementation of javax.swing.event.PopupMenuListener interface

  /**
   * Implementation of the PopupMenuListener interface.
   *
   * @param param1 <description>
   */
  public void popupMenuWillBecomeVisible(PopupMenuEvent param1) {
    // TODO: implement this javax.swing.event.PopupMenuListener method
  }

  /**
   * Implementation of the PopupMenuListener interface.
   *
   * @param param1 <description>
   */
  public void popupMenuWillBecomeInvisible(PopupMenuEvent param1) {
    // TODO: implement this javax.swing.event.PopupMenuListener method
  }

  /**
   * Implementation of the PopupMenuListener interface.
   *
   * @param param1 <description>
   */
  public void popupMenuCanceled(PopupMenuEvent param1) {
    // TODO: implement this javax.swing.event.PopupMenuListener method
  }


    public void resetCurrentMSB() {
	if (om != null) {
	    om.addNewTree(null);
	}
    }

}//QtFrame
