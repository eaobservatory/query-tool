package edu.jach.qt.gui;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  
import java.io.File;
import java.io.IOException;
import org.w3c.dom.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;

/**
 * MSBQueryTableModel.java
 *
 *
 * Created: Tue Aug 28 16:49:16 2001
 *
 */

public class MSBQueryTableModel extends AbstractTableModel {

   public static final String ROOT_ELEMENT_TAG = "SpMSBSummary";

   public static final String MSB_SUMMARY = System.getProperty("msbSummary");

   /*public static final String[] colNames ={
      "ProjectID",
      "SourceName",
      "Instrument",
      "Wavelength",
      "ExposureTime",
      "PI"
      };*/
   
   public static final String[] colNames ={
      "projectid",
      "checksum",
      "remaining",
      "filename"
      };
   
   public static final Class[] colClasses ={
      String.class,
      String.class,
      Integer.class,
      String.class,
   };

   public static final int 
      PROJECTID          = 0,
      SOURCENAME           = 1,
      INSTRUMENT          = 2,
      WAVELENGTH           = 3,
      EXPOSURETIME        =4,
      PI                  =5;

   //DATA
   //DOM object to hold XML document contents
   protected Document doc;
   public Integer[] projectIds;
   boolean docIsNull;

   //used to hold a list of TableModelListeners
   protected java.util.List tableModelListeners = 
      new ArrayList();        

   public MSBQueryTableModel() {
      docIsNull = true;
      projectIds = new Integer[100];
   }

   public void setQuery() {
   /**
      Constructor - create a DOM
   */

      try {
	 DocumentBuilderFactory factory =
	    DocumentBuilderFactory.newInstance();
	 //factory.setValidating(true);   
	 //factory.setNamespaceAware(true);
	 
	 DocumentBuilder builder = factory.newDocumentBuilder();
	 doc = builder.parse( new File(MSB_SUMMARY));
	 docIsNull = false;
      } catch (SAXException sxe) {
	 // Error generated during parsing)
	 Exception  x = sxe;
	 if (sxe.getException() != null)
	    x = sxe.getException();
	 x.printStackTrace();

      } catch(ParserConfigurationException pce) {
	 pce.printStackTrace();

      } catch (IOException ioe) {
	 // I/O error
	 ioe.printStackTrace();
      }
      
      fireTableChanged(null);
   }

   //
   // TableModel implementation
   //

   /**
      Return the number of columns for the model.

      @return    the number of columns in the model
   */
   public int getColumnCount() {
      return colClasses.length;
   }
   /**
      Return the number of persons in an XML document
 
      @return    the number or rows in the model
   */
   public int getRowCount() {
      if (docIsNull) return 0;
      return XmlUtils.getSize( doc , ROOT_ELEMENT_TAG );
   }

   /**
      Return an XML data given its location
 
      @param	    r   the row whose value is to be looked up
      @param	    c 	the column whose value is to be looked up
      @return	the value Object at the specified cell
   */
   public Object getValueAt(int r, int c) {

      //must get row first
      Element row = XmlUtils.getElement( doc , ROOT_ELEMENT_TAG , r );
      projectIds[r] = new Integer(row.getAttribute("id"));

      //must get value for column in this row
      return XmlUtils.getValue( row , colNames[c] );
   }

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
      @return	true if the cell is editable.
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

}// MSBQueryTableModel
