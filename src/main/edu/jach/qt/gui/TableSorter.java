
package edu.jach.qt.gui;

// Imports for picking up mouse events from the JTable. 
import java.awt.event.*;
import java.awt.Cursor;
import java.util.*;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import javax.swing.*;

import org.apache.log4j.Logger;

/**
 * ****USED BY THE OMP-QT TO SORT THE COLUMNS OF THE RESULT TABLE.****
 *    
 * A sorter for TableModels. The sorter has a model (conforming to TableModel) 
 * and itself implements TableModel. TableSorter does not store or copy 
 * the data in the TableModel, instead it maintains an array of 
 * integers which it keeps the same size as the number of rows in its 
 * model. When the model changes it notifies the sorter that something 
 * has changed eg. "rowsAdded" so that its internal array of integers 
 * can be reallocated. As requests are made of the sorter (like 
 * getValueAt(row, col) it redirects them to its model via the mapping 
 * array. That way the TableSorter appears to hold another copy of the table 
 * with the rows in a different order. The sorting algorthm used is stable 
 * which means that it does not move around rows when its comparison 
 * function returns 0 to denote that they are equivalent. 
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

public class TableSorter extends TableMap {
   int             indexes[];
   Vector          sortingColumns = new Vector();
   boolean         ascending = true;
   int compares;
  static Logger logger = Logger.getLogger(WidgetPanel.class);

    /**
     * Contructor.
     * Default contstructor which creates a 0 size <code>TableMap</code>
     */
   public TableSorter() {
      indexes = new int[0]; // for consistency
   }

    /**
     * Constructor.
     * Uses an existing model to contruct the <code>TableMap</code>.
     * @param model   The <code>TableModel</code> to use to construct the Map.
     */
   public TableSorter(TableModel model) {
      setModel(model);
   }

   /**
    * Set the model for the <code>TableMap</code>.
    * @param model   The <code>TableModel</code> to use to construct the Map.
    */
   public void setModel(TableModel model) {
      super.setModel(model); 
      reallocateIndexes(); 
   }

    /**
     * Compare the values is a specfic table column in two rows.
     * Works independently of the type of data in the column.
     * @param row1   The first row to use.
     * @param row2   The second row to use.
     * @param column The column of data we are comparing.
     * @return       -1 if value in row1 < value in row2, 
     *               1 if the value in row 1 > value in row2
     *               0 if the values are identical.
     */
   public int compareRowsByColumn(int row1, int row2, int column) {
      Class type = model.getColumnClass(column);
      TableModel data = model;

      // Check for nulls.

      Object o1 = data.getValueAt(row1, column);
      Object o2 = data.getValueAt(row2, column);

      // If both values are null, return 0.
      if (o1 == null && o2 == null) {
	 return 0; 
      } else if (o1 == null) { // Define null less than everything. 
	 return -1; 
      } else if (o2 == null) { 
	 return 1; 
      }

      if (o1.toString().equals("??") || o1.toString().equals("??"))
	  {
	      return -1;
	  }

      /*
       * We copy all returned values from the getValue call in case
       * an optimised model is reusing one object to return many
       * values.  The Number subclasses in the JDK are immutable and
       * so will not be used in this way but other subclasses of
       * Number might want to do this to save space and avoid
       * unnecessary heap allocation.
       */

      if (type.getSuperclass() == java.lang.Number.class ||
	  type == java.lang.Number.class) {
	 Number n1 = new Double (data.getValueAt(row1, column).toString());
	 Number n2 = new Double (data.getValueAt(row2, column).toString());
	 double d1;
	 double d2;
	 if ( type == java.lang.Integer.class ) {
	     d1 = n1.intValue();
	     d2 = n2.intValue();
	 }
	 else {
	     d1 = n1.doubleValue();
	     d2 = n2.doubleValue();
	 }
	   
	 if (d1 < d2) {
	    return -1;
	 } else if (d1 > d2) {
	    return 1;
	 } else {
	    return 0;
	 }
      } else if (type == java.util.Date.class) {
	 Date d1 = (Date)data.getValueAt(row1, column);
	 long n1 = d1.getTime();
	 Date d2 = (Date)data.getValueAt(row2, column);
	 long n2 = d2.getTime();

	 if (n1 < n2) {
	    return -1;
	 } else if (n1 > n2) {
	    return 1;
	 } else {
	    return 0;
	 }
      } else if (type == String.class) {
	 String s1 = (String)data.getValueAt(row1, column);
	 String s2    = (String)data.getValueAt(row2, column);
	 int result = s1.compareTo(s2);

	 if (result < 0) {
	    return -1;
	 } else if (result > 0) {
	    return 1;
	 } else {
	    return 0;
	 }
      } else if (type == Boolean.class) {
	 Boolean bool1 = (Boolean)data.getValueAt(row1, column);
	 boolean b1 = bool1.booleanValue();
	 Boolean bool2 = (Boolean)data.getValueAt(row2, column);
	 boolean b2 = bool2.booleanValue();

	 if (b1 == b2) {
	    return 0;
	 } else if (b1) { // Define false < true
	    return 1;
	 } else {
	    return -1;
	 }
      } else {
	 Object v1 = data.getValueAt(row1, column);
	 String s1 = v1.toString();
	 Object v2 = data.getValueAt(row2, column);
	 String s2 = v2.toString();
	 int result = s1.compareTo(s2);

	 if (result < 0) {
	    return -1;
	 } else if (result > 0) {
	    return 1;
	 } else {
	    return 0;
	 }
      }
   }

    /**
	 * Compare values in two rows from a <code>sortingColumns</code>.
	 * 
	 * @param row1
	 *            The first row to use in the comparison.
	 * @param row2
	 *            The second row to use in the comparison.
	 * @returns (-1, 0, 1) depending on the values in the two rows and whether the table is being sorted in ascending or descending order.
	 * @see #compareRowsByColumn(int, int, int)
	 */
	public int compare( int row1 , int row2 )
	{
		compares++;
		for( int level = 0 ; level < sortingColumns.size() ; level++ )
		{
			Integer column = ( Integer ) sortingColumns.elementAt( level );
			int result = compareRowsByColumn( row1 , row2 , column.intValue() );
			if( result != 0 )
			{
				return ascending ? result : -result;
			}
		}
		return 0;
	}

    /**
	 * Sets up a new array of indexes with the right number of elements for the current <code>model</code>.
	 */
   public void reallocateIndexes() {
      int rowCount = model.getRowCount();

      // Set up a new array of indexes with the right number of elements
      // for the new data model.
      indexes = new int[rowCount];

      // Initialise with the identity mapping.
      for (int row = 0; row < rowCount; row++) {
	 indexes[row] = row;
      }
   }

    /**
     * Implementation of the <code>TableModelListener</code> interface.
     */
   public void tableChanged(TableModelEvent e) {
      logger.debug("Sorter: tableChanged"); 
      reallocateIndexes();

      super.tableChanged(e);
   }

    /**
     * Checks whether the number of indices allocated matches the size
     * of the current Table model.
     */
   public void checkModel() {
      if (indexes.length != model.getRowCount()) {
	 logger.error("Sorter not informed of a change in model.");
      }
   }

    /**
     * Starts the sorting process.
     * @param sender    The object that sent the sort request (normally a colmun header).
     */
   public void sort(Object sender) {
      checkModel();

      compares = 0;
//       qsort();
      n2sort();
//       qsort(0, indexes.length-1);
//       shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
//       logger.debug("Compares: "+compares);
   }

    /**
     * Performs a quicksort on a table
     */
    public void qsort() {
    }

    /**
	 * Perform an N-squared sort on a table.
	 */
	public void n2sort()
	{
		int rowCount = getRowCount() ;
		for( int i = 0 ; i < rowCount ; i++ )
		{
			for( int j = i + 1 ; j < rowCount ; j++ )
			{
				if( compare( indexes[ i ] , indexes[ j ] ) == 1 )
				{
					swap( i , j );
				}
			}
		}
	}

    /**
     *  This is a home-grown implementation which we have not had time
     * to research - it may perform poorly in some circumstances. It
     * requires twice the space of an in-place algorithm and makes
     * NlogN assigments shuttling the values between the two
     * arrays. The number of compares appears to vary between N-1 and
     * NlogN depending on the initial order but the main reason for
     * using it here is that, unlike qsort, it is stable.
     * @param from   ?
     * @param to     ?
     * @param low    ?
     * @param high   ?
     * @deprecated Replaced by {@link #n2sort()}
     */
   public void shuttlesort(int from[], int to[], int low, int high) {
      if (high - low < 2) {
	 return;
      }
      int middle = (low + high)/2;
      shuttlesort(to, from, low, middle);
      shuttlesort(to, from, middle, high);

      int p = low;
      int q = middle;

      /* This is an optional short-cut; at each recursive call,
	 check to see if the elements in this subset are already
	 ordered.  If so, no further comparisons are needed; the
	 sub-array can just be copied.  The array must be copied rather
	 than assigned otherwise sister calls in the recursion might
	 get out of sinc.  When the number of elements is three they
	 are partitioned so that the first set, [low, mid), has one
	 element and and the second, [mid, high), has two. We skip the
	 optimisation when the number of elements is three or less as
	 the first compare in the normal merge will produce the same
	 sequence of steps. This optimisation seems to be worthwhile
	 for partially ordered lists but some analysis is needed to
	 find out how the performance drops to Nlog(N) as the initial
	 order diminishes - it may drop very quickly.  */

      if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
	 for (int i = low; i < high; i++) {
	    to[i] = from[i];
	 }
	 return;
      }

      // A normal merge. 

      for (int i = low; i < high; i++) {
	 if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
	    to[i] = from[p++];
	 }
	 else {
	    to[i] = from[q++];
	 }
      }
   }

    /**
     * Swap to rows in a table.
     * @param i  Index of first row.
     * @param j  Index of second row.
     */
   public void swap(int i, int j) {
      int tmp = indexes[i];
      indexes[i] = indexes[j];
      indexes[j] = tmp;
   }

   // The mapping only affects the contents of the data rows.
   // Pass all requests to these rows through the mapping array: "indexes".

    /**
     * Retrieve the value at a particular row and column in the table.
     * @param aRow    The row from which to get the data.
     * @param aColumn The column from which to get the data.
     * @return        The Object entry at the required point.
     */
   public Object getValueAt(int aRow, int aColumn) {
      checkModel();
      return model.getValueAt(indexes[aRow], aColumn);
   }

    /**
     * Set a value at a particular row and column in the table.
     * @param aRow    The row from which to set the data.
     * @param aColumn The column from which to set the data.
     * @param aValue  The Object entry at the required point.
     */
   public void setValueAt(Object aValue, int aRow, int aColumn) {
      checkModel();
      model.setValueAt(aValue, indexes[aRow], aColumn);
   }

    /**
     * Sort the table by the specified column.
     * @param column   The index of the column to sort by.
     */
   public void sortByColumn(int column) {
      sortByColumn(column, true);
   }

    /**
	 * Sort the table by the specified column in either ascending or
	 * descending order.
	 * @param column     The index of the column to sort by.
	 * @param ascending  <code>true</code> to sort in ascending order.
	 */
	public void sortByColumn( int column , boolean ascending )
	{
		this.ascending = ascending;
		sortingColumns.removeAllElements();
		sortingColumns.addElement( new Integer( column ) );
		sort( this );
		super.tableChanged( new TableModelEvent( this ) );
	}

	// There is no-where else to put this. 
	// Add a mouse listener to the Table to trigger a table sort 
	// when a column heading is clicked in the JTable. 
	/**
	 * Add a mouse listener to the column headers.
	 * @param table   The table to add the listener to.
	 */
	public void addMouseListenerToHeaderInTable( final JTable table )
	{
		final TableSorter sorter = this;
		final JTable tableView = table;
		ToolTipManager.sharedInstance().unregisterComponent( table );
		ToolTipManager.sharedInstance().unregisterComponent( table.getTableHeader() );
		tableView.setColumnSelectionAllowed( false );
		MouseAdapter listMouseListener = new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX( e.getX() );
				int column = tableView.convertColumnIndexToModel( viewColumn );
				if( e.getClickCount() == 1 && column != -1 )
				{
					logger.debug( "Sorting table..." );
					int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
					boolean ascending = ( shiftPressed == 0 );
					sorter.sortByColumn( column , ascending );
				}
			}
		};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener( listMouseListener );
	}
}
