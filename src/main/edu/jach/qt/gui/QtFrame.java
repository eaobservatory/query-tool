package edu.jach.qt.gui;

import edu.jach.qt.app.Querytool;
import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.TextReader;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.JEditorPane;
import javax.swing.JSplitPane;
import javax.swing.event.*;


/**
 * Title:        QT
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      JAC
 * @author Mathew Rippa
 * @version 1.0
 */

public class QtFrame extends JFrame implements MenuListener {

   private static final String 
      WIDGET_CONFIG_FILE = "/home/mrippa/netroot/src/QT/config/qt.conf";
   private final static String
      DUMMY_TABLE_DATA = "/home/mrippa/netroot/src/QT/config/querySet.txt";
   
   /**
    * Describe variable <code>medu.jach.qt.</code> here.
    *
    */
   public MyQueryTableModel mqtm;

   private WidgetPanel inputPanel;
   private WidgetDataBag widgetBag;
   public  InfoPanel infoPanel;
   private JMenuItem saveItem;
   private JMenuItem saveAsItem;
   private JCheckBoxMenuItem readonlyItem;

   /**
    * Creates a new <code>QtFrame</code> instance.
    *
    */
   public QtFrame(WidgetDataBag wdb) {
      widgetBag = wdb;
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      GridBagLayout layout = new GridBagLayout();
      getContentPane().setLayout(layout);

      try {
	 compInit();
      }
      catch(Exception e) {
	 e.printStackTrace();
      }

      //Read the config to determine the widgets
      try{
	 inputPanel.parseConfig(WIDGET_CONFIG_FILE);
      }
      catch(IOException e){ 
	 System.out.println("Parse Failed" + e);
      }
   }

   /**Component initialization*/
   private void compInit() throws Exception  {
      GridBagConstraints gbc = new GridBagConstraints();

      buildMenu();

      setSize(new Dimension(950, 550));
      setTitle("QT QUERY-TOOL");

      mqtm = new MyQueryTableModel(DUMMY_TABLE_DATA);
      JTable table = new JTable(mqtm);
      table.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);
      JScrollPane resultsPanel = new JScrollPane(table);

      inputPanel = new WidgetPanel(new Hashtable(), widgetBag);
      infoPanel = new InfoPanel(this, mqtm);

      // Build split-pane view
      JSplitPane splitPane = 
	 new JSplitPane( JSplitPane.VERTICAL_SPLIT,
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
      getContentPane().add(c, gbc);      
   }
   

   /**
    * Describe <code>buildMenu</code> method here.
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
    * Describe <code>menuSelected</code> method here.
    *
    * @param evt a <code>MenuEvent</code> value
    */
   public void menuSelected(MenuEvent evt) {  
      saveItem.setEnabled(!readonlyItem.isSelected());
      saveAsItem.setEnabled(!readonlyItem.isSelected());
   }

   /**
    * Describe <code>menuDeselected</code> method here.
    *
    * @param evt a <code>MenuEvent</code> value
    */
   public void menuDeselected(MenuEvent evt) {

   }

   /**
    * Describe <code>menuCanceled</code> method here.
    *
    * @param evt a <code>MenuEvent</code> value
    */
   public void menuCanceled(MenuEvent evt) {

   }

   /**
    * Describe <code>makeMenu</code> method here.
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
    * Describe <code>makeMenuItem</code> method here.
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
   
   public static void main (String[] args) {
      QtFrame qtf = new QtFrame(new WidgetDataBag());
   }
   
}
