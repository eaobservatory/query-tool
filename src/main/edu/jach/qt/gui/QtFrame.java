package edu.jach.qt.gui;

import edu.jach.qt.app.Querytool;
import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.TextReader;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

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
public class QtFrame extends JFrame implements MenuListener, ListSelectionListener{

   private static final String 
      WIDGET_CONFIG_FILE = System.getProperty("widgetFile");
   private final static String
      DUMMY_TABLE_DATA = System.getProperty("dummyTable");;

   private MSBQueryTableModel	   msbQTM;
   private JTable		   table;
   private int			   selRow;
   private JMenuItem		   saveItem;
   private JMenuItem		   saveAsItem;
   private JCheckBoxMenuItem	   readonlyItem;
   private JSplitPane		   splitPane;
   private GridBagConstraints	   gbc;
   private JTabbedPane		   tabbedPane;
   private OmpOM		   om;			

   private WidgetDataBag	   widgetBag;
   private Querytool		   localQuerytool;


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
      }
      catch(Exception e) {
	 e.printStackTrace();
      }

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
      InfoPanel infoPanel = new InfoPanel(msbQTM, localQuerytool, this);

      // Build split-pane view
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
	 System.out.println("Parse Failed" + e);
      }

   }

   private void tableSetup() {
      TableSorter sorter = new TableSorter(msbQTM);
      table = new JTable(sorter);
      sorter.addMouseListenerToHeaderInTable(table);
      table.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);

      ListSelectionModel listMod =  table.getSelectionModel();
      listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      listMod.addListSelectionListener(this);

      table.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
	       
	       if (e.getClickCount() == 2){
		  if (selRow != -1) {
		     System.out.println("ID is "+msbQTM.getSpSummaryId(selRow));
		     localQuerytool.fetchMSB(msbQTM.getSpSummaryId(selRow));
		     buildStagingPanel();
			
		  }
		  else
		     JOptionPane.showMessageDialog(null, "Must select a project summary first!");
	       }
	       if (SwingUtilities.isRightMouseButton(e)) {
	  
	       }
	    }
	 } );
   }
   

   public void buildStagingPanel() {
      if (om == null) {
	 om = new OmpOM();
	 tabbedPane = new JTabbedPane(SwingConstants.TOP);
	 remove(splitPane);
	 tabbedPane.addTab("Query", splitPane);
	 tabbedPane.addTab("MSB Queue", om.getTreePanel());
      }
      
      else {
	 om.resetTree();
	 tabbedPane.setComponentAt(1, om.getTreePanel());
      }
      tabbedPane.setSelectedIndex(1);
      add(tabbedPane, gbc, 1, 0, 1, 2);
      tabbedPane.show();
      validate();
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
      
      JMenuItem openItem = new JMenuItem("Open");
      saveItem = new JMenuItem("Save");
      saveAsItem = new JMenuItem("Save As");
      
      mbar.add(makeMenu (fileMenu, new Object[] {
	 "New", openItem, null, saveItem, saveAsItem,
	 null, "Exit" }, this)
	       );
      
      readonlyItem = new JCheckBoxMenuItem("Read-only");
      mbar.add(makeMenu("Edit", new Object[] {
	 new JMenuItem("Cut", new ImageIcon("icons/cut.gif")),
	 new JMenuItem("Copy", new ImageIcon("icons/copy.gif")),
	 new JMenuItem("Paste", new ImageIcon("icons/paste.gif")),
	 null,
	 makeMenu("Options", new Object[] {  readonlyItem, null }, this)
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
      saveItem.setEnabled(!readonlyItem.isSelected());
      saveAsItem.setEnabled(!readonlyItem.isSelected());
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
