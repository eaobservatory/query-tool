package edu.jach.qt.gui;

import edu.jach.qt.app.*;
import edu.jach.qt.gui.*;
import edu.jach.qt.utils.*;
import gemini.sp.SpItem;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import ocs.utils.*;

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
public class QtFrame extends JFrame implements ActionListener, MenuListener, ListSelectionListener{

  private static final String 
    WIDGET_CONFIG_FILE = System.getProperty("widgetFile");
  private final static String
    DUMMY_TABLE_DATA = System.getProperty("dummyTable");;

  private MSBQueryTableModel	msbQTM;
  private JTable		table;
  private int			selRow;
  private JMenuItem		saveItem;
  private JMenuItem		saveAsItem;
  private JCheckBoxMenuItem	observability;
  private JCheckBoxMenuItem	remaining;
  private JCheckBoxMenuItem	allocation;
  private JSplitPane		splitPane;
  private GridBagConstraints	gbc;
  private JTabbedPane		tabbedPane;
  private OmpOM			om;
  private WidgetDataBag		widgetBag;
  private Querytool		localQuerytool;
  private InfoPanel		infoPanel;

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
    GridBagLayout layout = new GridBagLayout();
    getContentPane().setLayout(layout);

    try {
      compInit();
      splitPane.validate();
      validateTree();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

  public void exitQT() {
    setVisible(false);
    dispose();
    System.exit(0);
  }

  /**Component initialization*/
  private void compInit() throws Exception  {
    gbc = new GridBagConstraints();

    //Build Menu
    buildMenu();

    //Table setup
    msbQTM = new MSBQueryTableModel();
    tableSetup();
    JScrollPane resultsPanel = new JScrollPane(table);

    //Input Panel Setup
    WidgetPanel inputPanel = new WidgetPanel(new Hashtable(), widgetBag);
    infoPanel = new InfoPanel(msbQTM, localQuerytool, this);

    // Build split-pane view
    //inputPanel.setMinimumSize(new Dimension(770,275) );
    splitPane =  new JSplitPane( JSplitPane.VERTICAL_SPLIT,
				 inputPanel,
				 resultsPanel);

    gbc.fill = GridBagConstraints.BOTH;
    //gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 0;
    gbc.weighty = 100;
    add(infoPanel, gbc, 0, 0, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(splitPane, gbc, 1, 0, 1, 2);

    //Read the config to determine the widgets
    try{
      inputPanel.parseConfig(WIDGET_CONFIG_FILE);
    }
    catch(IOException e){ 
      System.out.println(">>>>>Widget Panel Parse Failed");
      e.printStackTrace();
    }

  }

  private void tableSetup() {
    final TableSorter sorter = new TableSorter(msbQTM);
    table = new JTable(sorter);
    sorter.addMouseListenerToHeaderInTable(table);
    table.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);
    table.setMinimumSize(new Dimension(770,275) );

    ListSelectionModel listMod =  table.getSelectionModel();
    listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listMod.addListSelectionListener(this);

    table.addMouseListener(new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	       
	  if (e.getClickCount() == 2) {
	    if (TelescopeDataPanel.DRAMA_ENABLED) {

	      if (selRow != -1) {

		if (om == null)
		  om = new OmpOM();

		//final Led blinker = infoPanel.getBlinker();
		
		final SwingWorker worker = new SwingWorker() {
		    Boolean isStatusOK;
		    Integer msbID = (Integer)sorter.getValueAt(selRow, MSBQueryTableModel.MSBID);

		    public Object construct() {
		    
		      try {
			om.setSpItem( localQuerytool.fetchMSB(msbID));
			isStatusOK = new Boolean(true);
		      } catch (NullPointerException e) {
			isStatusOK = new Boolean(false);
		      }

		      return isStatusOK;  //not used yet
		    }

		    //Runs on the event-dispatching thread.
		    public void finished() { 
		      //blinker.blinkLed(false);
		      InfoPanel.logoPanel.stop();

		      if ( isStatusOK.booleanValue()) {
			om.addNewTree(msbID);
			//System.out.println("ID is "+msbID);
			buildStagingPanel();
		      }

		      else {
			System.out.println("No msb ID retrieved!");
		      }
		    }
		  }; //End inner class

		String projectid = (String) sorter.getValueAt(selRow, MSBQueryTableModel.PROJECTID);
		String checksum = (String) sorter.getValueAt(selRow, MSBQueryTableModel.CHECKSUM);

		//System.out.println(">>>>>>MSB INFO is: "+projectid+", "+checksum);
		om.setProjectID(projectid);
		om.setChecksum(checksum);

		//blinker.blinkLed(true);
		//blinkThread.start();
		InfoPanel.logoPanel.start();
		worker.start();  //required for SwingWorker 3

	      }
	      else
		JOptionPane.showMessageDialog(null, "Must select a project summary first!");
	    }
	    else 
	      JOptionPane.showMessageDialog(null, "NOT A DRAMA SYSTEM. MSB EXECUTION DISABLED.");
	  }

	  if (SwingUtilities.isRightMouseButton(e)) {

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
   

  public void buildStagingPanel() {

    if (tabbedPane == null) {
      tabbedPane = new JTabbedPane(SwingConstants.TOP);
      remove(splitPane);
      tabbedPane.addTab("Query", splitPane);
      tabbedPane.addTab(om.getProgramName(), om.getTreePanel());
      add(tabbedPane, gbc, 1, 0, 1, 2);
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
    saveItem = new JMenuItem("Save");
    saveAsItem = new JMenuItem("Save As");
      
    mbar.add(makeMenu (fileMenu, new Object[] {
      "New", openItem, null, saveItem, saveAsItem,
      null, "Exit" }, this)
	     );
      
    observability = new JCheckBoxMenuItem("Observability",true);
    remaining     = new JCheckBoxMenuItem("Remaining",true);
    allocation    = new JCheckBoxMenuItem("Allocation",true);

    mbar.add( makeMenu("Edit", new Object[] {
      new JMenuItem("Cut", new ImageIcon("icons/cut.gif")),
      new JMenuItem("Copy", new ImageIcon("icons/copy.gif")),
      new JMenuItem("Paste", new ImageIcon("icons/paste.gif")),
      null, makeMenu("Constraints", new Object[] { 
	observability,
	remaining,
	allocation,
	null 
      }, this) 
    }, this));
    
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic('H');

    mbar.add(makeMenu ( helpMenu, 
			new Object[] { new JMenuItem("Index", 'I'), 
				       new JMenuItem("About", 'A') },
			this
			));
  }

  /**
   * <code>menuSelected</code> method is an action triggered when a 
   * menu is selected.
   *
   * @param evt a <code>MenuEvent</code> value
   */
  public void menuSelected(MenuEvent evt) {  
    //saveItem.setEnabled(!readonlyItem.isSelected());
    //saveAsItem.setEnabled(!readonlyItem.isSelected());
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

  public void actionPerformed(ActionEvent evt) {

    localQuerytool.setAllocationConstraint(!allocation.isSelected());
    localQuerytool.setRemainingConstraint(!remaining.isSelected());
    localQuerytool.setObservabilityConstraint(!observability.isSelected());
    
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
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }

}//QtFrame
