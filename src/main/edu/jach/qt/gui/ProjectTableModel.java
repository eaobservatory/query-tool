package edu.jach.qt.gui;

import java.io.*;
import java.util.Vector;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;  

public class ProjectTableModel 
    extends AbstractTableModel
    implements Runnable,  TableModelListener {

    static Logger logger = Logger.getLogger(ProjectTableModel.class);
    private static final String ROOT_ELEMENT_TAG = "SpMSBSummary";
    private static final String MSB_SUMMARY = System.getProperty("msbSummary")+"."+System.getProperty("user.name");

    private static String [] colName = {
	"projectid", "priority"};
    private static Class [] colClass = {
	String.class, Integer.class};

    private Vector projectIds = new Vector();
    private Vector priorities = new Vector();

    private Document doc;
    private boolean  docIsNull = true;;

    public ProjectTableModel() {
 	addTableModelListener(this);
    }

    public void run () {

	fireTableChanged(null);
    }

    public int getColumnCount() {
	return colName.length;
    }

    public Class getColumnClass(int index) {
	return colClass[index];
    }

    public String getColumnName(int index) {
	return colName[index];
    }

    public int getRowCount() {
	return projectIds.size();
    }

    public Object getValueAt(int r, int c) {
	if (r < 0 || projectIds.size() == 0) {
	    return "----";
	}
	if (colName[c].equalsIgnoreCase("projectid")) {
	    return projectIds.elementAt(r);
	}
	else if (colName[c].equalsIgnoreCase("priority")) {
	    return priorities.elementAt(r);
	}
	else {
	    return "---";
	}
    }

    public void setValueAt(Object value, int r, int c) {
    }

    public void tableChanged(TableModelEvent evt) {
	projectIds.clear();
	priorities.clear();
	Vector [] data = XmlUtils.getProjectData();
	projectIds = data[0];
	priorities = data[1];
    }

    public void clear() {
	projectIds.clear();
	priorities.clear();
	XmlUtils.clearProjectData();
	fireTableChanged(null);
    }
}
