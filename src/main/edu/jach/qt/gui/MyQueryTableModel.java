package edu.jach.qt.gui;

// MyQueryTableModel.java
//
import javax.swing.table.*;
import javax.swing.*;
import java.util.Vector;

/**
 * Describe class <code>MyQueryTableModel</code> here.
 *
 * @author <a href="mailto:mrippa@kapili">Mathew Rippa</a>
 */
public class MyQueryTableModel extends AbstractTableModel {
   Vector cache;  // will hold String[] objects . . .
   int colCount;
   int[] stockIndices = new int[0];
   String[] headers = {"PATT", "PI", "ObjectName", "RA", "DEC",
		       "LST", "WeatherQueues", "Alloc", "TimeRem", "Freq./wavelength", "Obs.Mode"};
   String myFile;
   
   /**
    * Creates a new <code>MyQueryTableModel</code> instance.
    *
    * @param file a <code>String</code> value
    */
   public MyQueryTableModel(String file) {
      myFile = file;
      cache = new Vector();
   }

   /**
    * Describe <code>getColumnName</code> method here.
    *
    * @param i an <code>int</code> value
    * @return a <code>String</code> value
    */
   public String getColumnName(int i) { return headers[i]; }

   /**
    * Describe <code>getColumnCount</code> method here.
    *
    * @return an <code>int</code> value
    */
   public int getColumnCount() { return colCount; }

   /**
    * Describe <code>getRowCount</code> method here.
    *
    * @return an <code>int</code> value
    */
   public int getRowCount() { return cache.size();}

   /**
    * Describe <code>getValueAt</code> method here.
    *
    * @param row an <code>int</code> value
    * @param col an <code>int</code> value
    * @return an <code>Object</code> value
    */
   public Object getValueAt(int row, int col) { 
      return ((String[])cache.elementAt(row))[col];
   }
   
   // All the real work happens here
   // Consider performing the query in a separate thread.
   /**
    * Describe <code>setQuery</code> method here.
    *
    */
   public void setQuery() {
      cache = new Vector();
      try {
	 // Execute the query and store the result set and its metadata
	 MyResultSet rs = new MyResultSet(myFile);
	 //ResultSet rs = statement.executeQuery(q);
	 //ResultSetMetaData meta = rs.getMetaData();
	 colCount = headers.length;

	 // Now we must rebuild the headers array with the new column names
	 //headers = new String[colCount];
	 //for (int h=1; h <= colCount; h++) {
	 //   headers[h-1] = meta.getColumnName(h);
	 //}

	 // and file the cache with the records from our query.  This would not be
	 // practical if we were expecting a few million records in response to our
	 // query, but we aren't, so we can do this.
	 while (rs.next()) {
	    String[] record = new String[colCount];
	    for (int i=0; i < colCount; i++) {
	       record[i] = rs.getString(i);
	    }
	    cache.addElement(record);
	 }
	 fireTableChanged(null); // notify everyone that we have a new table.
      }
      catch(Exception e) {
	 cache = new Vector(); // blank it out and keep going.
	 e.printStackTrace();
      }
   }


}
