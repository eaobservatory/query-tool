package edu.jach.qt.gui;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.parsers.*;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;  

import edu.jach.qt.utils.MsbClient;

/**
 * MSBQueryTableModel.java
 *
 *
 * Created: Tue Aug 28 16:49:16 2001
 *
 */

public class MSBQueryTableModel extends AbstractTableModel implements Runnable {

  static Logger logger = Logger.getLogger(MSBQueryTableModel.class);
  private String _projectId = null;
  public static final String ROOT_ELEMENT_TAG = "SpMSBSummary";

  public static final String MSB_SUMMARY = System.getProperty("msbSummary")+"."+System.getProperty("user.name");
  public static final String MSB_SUMMARY_TEST = System.getProperty("msbSummaryTest");

   
    public static String[] colNames;           // Array of column names
    public static Class [] colClasses;         // Array of class types for each column
    private int            colCount;           // The number of columns TO DISPLAY
                                               // This may be less than the actual number of columns
    public static int      PROJECTID;          // Index of column containing the project ID
    public static int      CHECKSUM;           // Index of column containing the project checksum
    public static int      MSBID;              // Index of column containing the MSB Id
    private BitSet         currentBitSet;      // Bit mask showing which columns to display
    private Vector         model;
    private Vector         modelIndex = new Vector();
    private XmlUtils.MSBTableModel currentModel;
      
  //DATA
  //DOM object to hold XML document contents
  protected Document doc;
  protected Element msbIndex;
  public Integer[] projectIds;
  boolean docIsNull;

  //used to hold a list of TableModelListeners
  protected java.util.List tableModelListeners = 
    new ArrayList();        

    /**
     * Constructor.
     * Constructs a tabe model with 200 possible entries.
     */
  public MSBQueryTableModel() throws Exception{

    // Do a query to get the names of the columns
    colNames = MsbClient.getColumnNames();
    if (colNames == null) {
	throw new Exception("No results returned");
    }
    // Set the column count to the total number -2 since we will
    // by default hide the MSB id and checksum, since these mean
    // nothing to the observer and are for internal use only.
    colCount = colNames.length - 2;

    // Do the query to get the classes for each column
    String [] colClassNames = MsbClient.getColumnClasses();
    colClasses = new Class [ colNames.length ];
    Vector vectorOfNames = new Vector();
    currentBitSet = new BitSet(colNames.length);
    // Loop over each column
    for (int i=0; i< colNames.length; i++) {
	if (colClassNames[i].equalsIgnoreCase("Integer")) {
	    colClasses[i] = Integer.class;
	}
	else if (colClassNames[i].equalsIgnoreCase("Float")) {
	    colClasses[i] = Number.class; // Does not seem to like Float.class - don't know why
	}
	else {
	    colClasses[i] = String.class;
	}
	vectorOfNames.add((Object)colNames[i]);
	// TJs code guarantees that the msbid and checksum are the
	// last two columns returned in a query, so we don't need
	// to do any explicit checking.  This will need to be modified
	// in the event that this changes.
	if (i<colCount) {
	    currentBitSet.set(i);
	}
	else {
	    currentBitSet.clear(i);
	}
    }
    updateColumns(currentBitSet);
    adjustColumnData(currentBitSet);

    docIsNull = true;
    projectIds = new Integer[200];
  }


    /**
     * Set the current project id.
     * @param project   The name of the current project
     */
    public void setProjectId(String project) {
	_projectId = project;
	fireTableChanged(null);
    }

    /**
     * Impelmentation of <code>Runnable</code> interface.
     * Creates a DOM document for populating the table.
     */
  public void run() {
      // Clear the current model
      if (model != null) {
	  model.clear();
      }
      modelIndex.clear();

      // Parse the MSB summary which should have already been generated from the query.
      try {
	  DocumentBuilderFactory factory =
	      DocumentBuilderFactory.newInstance();
	  
	  DocumentBuilder builder = factory.newDocumentBuilder();
	  doc = builder.parse( new File(MSB_SUMMARY));
	  docIsNull = false;
	  
      } 
      catch (SAXException sxe) {
	  Exception  x = sxe;
	  if (sxe.getException() != null)
	      x = sxe.getException();
	  logger.error("SAX Error generated during parsing", x);
	  
      } 
      catch(ParserConfigurationException pce) {
	  logger.error("ParseConfiguration Error generated during parsing", pce);
      } 
      catch (IOException ioe) {
	  logger.error("IO Error generated attempting to build Document", ioe);
      }
      
      // If the document exists, build a new model so we don't need to keep
      // going back to the XML.
      if (doc != null) {
	  logger.info("Building new model");
	  model = XmlUtils.getNewModel(doc, ROOT_ELEMENT_TAG);
	  // Move the columns around to the current bitset.
	  adjustColumnData(currentBitSet);
	  if (model != null) {
	      logger.info("Model has "+model.size()+" projects");
	      // Create an internal map of projects to MSBs
	      for (int i=0; i<model.size();i++) {
		  modelIndex.add(((XmlUtils.MSBTableModel)model.elementAt(i)).getProjectId());
	      }
	  }
      }
  }
    
    /**
     * Return the current DOM document.
     */
  public Document getDoc() {
    return doc;
  }

  //
  // TableModel implementation
  //

  /**
     Return the number of columns for the model.

     @return    the number of columns in the model
  */
  public int getColumnCount() {
    return colCount;
  }

    /**
     * Get the real number of columns in the model.  This may be
     * less than the number of columns displayed on the associated
     * table.
     * @return   The number of columns in the model.
     */
  public int getRealColumnCount() {
    return colNames.length;
  }


  /**
     Return the number of persons in an XML document
 
     @return    the number or rows in the model
  */
  public int getRowCount() {
      int rowCount = 0;
      if (model == null || model.size() == 0 || _projectId == null) {
	  return rowCount;
      }
      if (_projectId.equalsIgnoreCase("all")) {
	  // Get the total number of rows returned
	  for ( int index=0; index < model.size(); index++) {
	      rowCount += ((XmlUtils.MSBTableModel)model.elementAt(index)).getColumn(0).size();
	  }
      }
      else {
	  // Get the total number of rows for the specified project
	  int index = modelIndex.indexOf(_projectId);
	  if (index != -1) {
	      rowCount =  ((XmlUtils.MSBTableModel)model.elementAt(index)).getColumn(0).size();
	  }
      }
      return rowCount;
  }

  /**
     Return an XML data given its location
 
     @param	    r   the row whose value is to be looked up
     @param	    c 	the column whose value is to be looked up
     @return	the value Object at the specified cell
  */
  public Object getValueAt(int r, int c) {
      if (_projectId.equalsIgnoreCase("all")) {
	  // Need to get data for all the MSBs returned...
	  int rowCount = 0;
	  for ( int index=0; index < model.size(); index++) {
	      //Get the number of rows in the current model
	      rowCount = ((XmlUtils.MSBTableModel)model.elementAt(index)).getColumn(0).size();
	      if (rowCount <= r ) {
		  // We have the right model, so get the data
		  r = r-rowCount;
		  continue;
	      }
	      return ((XmlUtils.MSBTableModel)model.elementAt(index)).getData(r, c);
	  }
      }
      else {
	  int index = modelIndex.indexOf(_projectId);
	  if (index != -1) {
	      return ((XmlUtils.MSBTableModel)model.elementAt(index)).getData(r, c);
	  }
      }
      return null;
  }
    
    /**
     * Get the Summary Identifier of the current row.
     * @param row  The selected row of the table.
     * @return The SpSummaryId from the selected row.
     */
    public Integer getSpSummaryId(int row) {
	return projectIds[row];
    }
    
    /**
       Return the name of column for the table.
       
       @param	    c   the index of column
       @return    the name of the column
    */
    public String getColumnName(int c) {
	return colNames[ c ];
    }
    /**
       Return column class
       
       @parm      c the index of column
       @return    the common ancestor class of the object values in the model.
    */
    public Class getColumnClass(int c) {
	return colClasses[ c ];
    }

    /**
       Return false - table is not editable
       
       @param	    r	the row whose value is to be looked up
       @param	    c	the column whose value is to be looked up
       @return	<code>false</code> always..
    */
    public boolean isCellEditable(int r, int c) {
	return false;
    }
    
    /**
       This method is not implemented, because the table is not editable.
       
     @param	    value		 the new value
     @param	    r	 the row whose value is to be changed
     @param	    c 	 the column whose value is to be changed
    */
    public void setValueAt(Object value, int r, int c) {
    }



    /**
     * Method to select a subset of columns in the model to display
     * on the associated table.  The <code>BitSet</code> input must
     * be in the same order as that returned from a 
     * <code>getColumnNames</code> query.  If a bit is set, it is assumed
     * that column should be displayed.
     * @see edu.jach.qt.utils.MsbClient#getColumnNames()
     * @param  colSet  The set of columns to display.
     */
    public void updateColumns(BitSet colSet) {
	int nHidden = 0;
	currentBitSet = colSet;
	Vector colVector = new Vector();
	Vector classVector = new Vector();
	// Initialsise the vector
	colNames = MsbClient.getColumnNames();
	String [] colClassName = MsbClient.getColumnClasses();
	for (int i=0; i< colNames.length; i++) {
	    colVector.add((Object)colNames[i]);
	    classVector.add((Object)colClassName[i]);
	}
	// Now manipulate the vector
	for (int i=colNames.length-1; i >= 0; i--) {
	    if (!colSet.get(i)) {
		nHidden++;
		// Get the contents and move them to the end...
		Object o = colVector.remove(i);
		colVector.add(o);
		// Make sure the classes stay linked to the names
		o = classVector.remove(i);
		classVector.add(o);
		// And make sure that the model vector is maintained...
	    }		
	}
	// Set the column count
	colCount = colNames.length - nHidden;
	for (int i=0; i< colNames.length; i++) {
	    colNames[i] = (String)colVector.get(i);
	    if (((String)classVector.get(i)).equalsIgnoreCase("Integer")) {
		colClasses[i] = Integer.class;
	    }
	    else if (((String)classVector.get(i)).equalsIgnoreCase("Float")) {
		colClasses[i] = Number.class;
	    }
	    else {
		colClasses[i] = String.class;
	    }
	}

	// reset the identifiers
	this.MSBID     = colVector.indexOf("msbid");
	this.CHECKSUM  = colVector.indexOf("checksum");
	this.PROJECTID = colVector.indexOf("projectid");

	fireTableChanged(null);
    }

    public BitSet getBitSet() {
	return currentBitSet;
    }

    public void adjustColumnData(BitSet colSet) {
	// Get the raw model in case we have previously manipulated it.
	model = XmlUtils.getNewModel(doc, ROOT_ELEMENT_TAG);
	if (model == null) return;
	// Loop through each submodel
	for (int i=0; i< model.size(); i++) {
	    XmlUtils.MSBTableModel current = (XmlUtils.MSBTableModel) model.elementAt(i);
	    for (int j = colNames.length-1; j>=0; j--) {
		if (!colSet.get(j)) {
		    // Move the column to the end to hide it.
		    current.moveColumnToEnd(j);
		}
	    }
	}
    }

    public void clear() {
	model.removeAllElements();
	modelIndex.removeAllElements();
	updateColumns(currentBitSet);
    }

}// MSBQueryTableModel
