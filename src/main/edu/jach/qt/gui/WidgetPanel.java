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
 *
 * Created: Tue Mar 20 16:41:13 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
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

   public ButtonPanel instrumentPanel;

   /**
    * Creates a new <code>WidgetPanel</code> instance.
    *
    */
   public WidgetPanel(Hashtable ht, WidgetDataBag wdb) {
      abbrevTable = ht;
      widgetBag = wdb;
   }

   /**
    * Describe <code>parseConfig</code> method here.
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
       	       add(new LabeledTextField(abbrevTable, widgetBag, next), gbc, 0, numComponents, 1, 1);
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


   public String makeList (LinkedList list) {
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

   public void addTableEntry(String entry) {
      String tmp = "";
      tmp = abbreviate(entry);
      abbrevTable.put(entry, tmp);
   }

   /**
    * Describe <code>abbreviate</code> method here.
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

   protected void printTable() {
      System.out.println(abbrevTable.toString());
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
      numComponents++;
   }

   /**
    * Describe <code>actionPerformed</code> method here.
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
    * Describe <code>setAttribute</code> method here.
    *
    * @param key a <code>String</code> value
    * @param value a <code>String</code> value
    */
   public void setAttribute(String key, String value) {
      widgetBag.put(abbrevTable.get(key), value);
   }

   public void setAttribute(String title, LinkedList list) {
      widgetBag.put(abbrevTable.get(title), list);
   }

}// WidgetPanel
