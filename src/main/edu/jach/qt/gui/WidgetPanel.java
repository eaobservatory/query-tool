package edu.jach.qt.gui;


import edu.jach.qt.app.Querytool;
import edu.jach.qt.utils.TextReader;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import javax.swing.*;

/**
 * WidgetPanel.java
 * 
 * This is the primary input panel for the QtFrame. It is responsible
 * for configuring the widgets determined in config/qtWidget.conf at
 * run-time.  There also exists two Hashtables.  One is responsible
 * for assigning abbreviations for widget names, the other is known to
 * the class as the WidgetDataBag.
 *
 * Created: Tue Mar 20 16:41:13 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class WidgetPanel extends JPanel 
   implements ActionListener {

   private TextReader tr;
   private Box b;
   private LinkedList radioList;
   private int numComponents = 0;
   private JCheckBox[] cb = new JCheckBox[3];
   private Hashtable abbrevTable;
   private WidgetDataBag widgetBag;

   /**
    * Describe variable <code>instrumentPanel</code> here.
    *
    */
   public ButtonPanel instrumentPanel;

   /**
    * Creates a new <code>WidgetPanel</code> instance.
    *
    * @param ht a <code>Hashtable</code> value
    * @param wdb a <code>WidgetDataBag</code> value
    */
   public WidgetPanel(Hashtable ht, WidgetDataBag wdb) {
      abbrevTable = ht;
      widgetBag = wdb;
   }

   /**
    * The <code>parseConfig</code> method is the single method
    * responsible for configuring Widgets at runtime.  It parses the
    * config/qtWidgets.conf file and sets up the determined widgets as 
    * described by the current layout manager.
    *
    * @param file a <code>String</code> value
    * @exception IOException if an error occurs
    */
   public void parseConfig(String file) throws IOException {

      GridBagLayout layout = new GridBagLayout();
      setLayout(layout);

      GridBagConstraints gbc = new GridBagConstraints();
      String widget, next, tmp;

      tr = new TextReader(file);
      while (tr.ready()) {

	 //skip over comments
	 while(tr.peek() == '#') {
	    tr.readLine();
	 }
	 
	 //which widget?
	 widget = tr.readWord();
	 
	 //JTextField
	 if (widget.equals("JTextField")) {
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.NORTH;
	    gbc.weightx = 100;
	    gbc.weighty = 0;
	    gbc.insets.top = 5;
	    gbc.insets.bottom = 5;
	    gbc.insets.left = 10;
	    gbc.insets.right = 5;
	    next = tr.readLine();
	    do {
	       next = tr.readLine();
	       if (next.equals("[EndSection]"))
		  break;
	       tmp = abbreviate(next);
	       //System.out.println("[next]:"+ next +"[tmp]: "+tmp);
	       abbrevTable.put(next, tmp);
       	       add(new LabeledTextField(abbrevTable, widgetBag, next),
		   gbc, 0, numComponents, 1, 1);
	       //widgetBag.put(tmp, "");
	    }while (true);
	 }

	 //JCheckBox
	 else if (widget.equals("JCheckBox")) {
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.anchor = GridBagConstraints.CENTER;
	    gbc.weightx = 100;
	    gbc.weighty = 0;
	    int num =0;
	    next = tr.readLine();
	    do {
	       next = tr.readLine();
	       if (next.equals("[EndSection]"))
		  break;
	       cb[num] = new JCheckBox(next);
	       cb[num].setHorizontalAlignment(SwingConstants.LEFT);

	       cb[num].addActionListener(this);
       	       add(cb[num], gbc, 1, radioList.size()+1+num, 1, 1);
	       num++;
	       tmp = abbreviate(next);
	       abbrevTable.put(next, tmp);
	       //widgetBag.put(tmp, "false");
	    }while (true);
	 }

	 //JRadioButton Panel
	 else if (widget.equals("JRadioButtonGroup")) {
	    radioList = new LinkedList();
	    String groupTitle = makeList(radioList);

	    RadioPanel jRadioPanel = new RadioPanel(abbrevTable, widgetBag, groupTitle, radioList);
	    jRadioPanel.setName(groupTitle);
	    gbc.insets.top = 0;
	    gbc.insets.bottom = 0;
	    gbc.insets.left = 0;
	    gbc.insets.right = 0;

	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.NORTH;
	    gbc.weightx = 100;
	    gbc.weighty = 0;
	    add(jRadioPanel, gbc, 1, 0, 1, radioList.size());
	 }

	 //JCheckBoxGroup
	 else if (widget.equals("JCheckBoxGroup")) {
	    LinkedList checkList = new LinkedList();
	    String groupTitle = makeList(checkList);
	    
	    instrumentPanel = new ButtonPanel(abbrevTable, widgetBag, groupTitle, checkList);
	    gbc.fill = GridBagConstraints.BOTH;
	    //gbc.anchor = GridBagConstraints.NORTH;
	    gbc.weightx = 100;
	    gbc.weighty = 100;
	    gbc.insets.left = 10;
	    add(instrumentPanel, gbc, 0, numComponents-3, 2, 1);
	 }

	 else if(!widget.equals("[Section]"))
	    break;

      }//end while
   }//parseConfig

   private String makeList (LinkedList list) {
      String next, title = "";
      next = tr.readLine();
      do{
	 next = tr.readLine();
	 if (next.equals("GroupTitle")) {
	    title = tr.readLine();
	    addTableEntry(title);
	 }
	 else if (next.equals("[EndSection]"))
	    break;
	 else {
	    list.add(next);
	    addTableEntry(next);
	    //widgetBag.put(tmp, "false");
	 }
      }while(true);
      return title;
   }

   /**
    * The <code>addTableEntry</code> method is used to keep track of
    * key:value relationships between the JLabel of a widget(Key) and its
    * abbreviation (value).
    *
    * @param entry a <code>String</code> value
    */
   public void addTableEntry(String entry) {
      String tmp = "";
      tmp = abbreviate(entry);
      abbrevTable.put(entry, tmp);
   }

   /**
    * The <code>abbreviate</code> method is used to convert the text
    * in a widget JLabel to an abbreviation used in the xml description.
    *
    * @param next a <code>String</code> value
    * @return a <code>String</code> value
    */
   public String abbreviate(String next) {
      String result = "ERROR";
      if (!next.equals("")) {
	 result = "";
	 StringTokenizer st = new StringTokenizer(next);
	 while (st.hasMoreTokens()) {
	    result += st.nextToken();
	 }
      }
      return result;
   }

   /**
    * The code>printTable</code> method here gives subJPanels the
    * ability to print the current abbreviation table.
    *
    */
   protected void printTable() {
      System.out.println(abbrevTable.toString());
   }
   
   /**
    * The <code>add</code> method here is a utility for adding widgets
    * or subJPanels to the WdgetPanel.  The layout manager is a
    * GridBag and the current contraints (gbc) are passed to this method.
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
      numComponents++;
   }

   /**
    * This <code>actionPerformed</code> method is mandated by
    * ActionListener and is need particularly for the 2 checkbox
    * widgets "Any Instrument" and "Photometric Whether Conditions".
    * All other objects in this JPanel are themselves sub-JPanels and
    * the actionPerformed methods are implemented in their respective
    * classes.  All subJPanels extend WidgetPanel.
    *
    * @param evt an <code>ActionEvent</code> value
    */
   public void actionPerformed( ActionEvent evt) {
      Object source = evt.getSource();
      
      if (source.equals(cb[0])) {
	 widgetBag.put(abbrevTable.get(cb[0].getLabel()), ""+cb[0].isSelected());
      }

      if (source.equals(cb[1])) {
	 if (cb[1].isSelected()) {
	    instrumentPanel.setSelected(true);
	    instrumentPanel.setEnabled(false);
	 }
	 else {
	    instrumentPanel.setEnabled(true);
	    instrumentPanel.setSelected(false);
	 }
	 widgetBag.put(abbrevTable.get(cb[1].getLabel()), ""+cb[1].isSelected());
      }
   }

   /**
    * Provided for convienince, <code>setAttribute</code> method with
    * this signature is supported but not encouraged.  All classes
    * using this methods should move towards the (String, LinkedList)
    * signature as meand of updateing widget state to the
    * WidgetDataBag object.
    *
    * @param key a <code>String</code> value
    * @param value a <code>String</code> value
    */
   public void setAttribute(String key, String value) {
      widgetBag.put(abbrevTable.get(key), value);
   }

   /**
    * The primary means of notifying observers of widget state
    * changes. The <code>setAttribute</code> method triggers all
    * observers' update method.  The update method of respective
    * observers can be implemented in different ways, however the only
    * known observer to date is app/Querytool which rewrites the XML
    * description of this panels state.
    *
    * @param title a <code>String</code> value
    * @param list a <code>LinkedList</code> value
    */
   public void setAttribute(String title, LinkedList list) {
      widgetBag.put(abbrevTable.get(title), list);
   }

}// WidgetPanel
