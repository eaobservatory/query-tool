package edu.jach.qt.gui;

import javax.swing.table.*; 
import javax.swing.event.TableModelListener; 
import javax.swing.event.TableModelEvent; 

/** 
 * ****USED BY THE OMP-QT TO SORT THE COLUMNS OF THE RESULT TABLE.****
 *
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap 
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting 
 * a TableMap which has not been subclassed into a chain of table filters 
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

public class TableMap extends AbstractTableModel 
                      implements TableModelListener {
    protected TableModel model; 

    /**
     * Return the current <code>TableModel</code>.
     * @return    The current model.
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * Sets the current <code>TableModel</code>.
     * @param model    The model to set.
     */
    public void setModel(TableModel model) {
        this.model = model; 
        model.addTableModelListener(this); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    /**
     * Get a value at a specified row and column.
     * @param aRow    The row from which to get the data.
     * @param aColumn The column from which to get the data.
     * @return        The object at the specified row and colmn.
     */
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn); 
    }
        
    /**
     * Get a value at a specified row and column.
     * @param aValue  The object to be set.
     * @param aRow    The row from which to set the data.
     * @param aColumn The column from which to set the data.
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn); 
    }

    /**
     * Get the number of rows in the current model.
     * @return  The number of rows (0 if no model defined).
     */
    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    /**
     * Get the number of columns in the current model.
     * @return  The number of columns (0 if no model defined).
     */
    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    /**
     * Get the name associated with a specified column index.
     * @param aColumn   The column index.
     * @return          The name associated with the column index.
     */
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn); 
    }

    /**
     * Get the <code>Class</code> of the entries in a specified column.
     * @param aColumn   The column index.
     * @return          The <code>Class</code> associated with the column index.
     */
    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    /**
     * Determines if a particular cell in the <code>TableModel</code> is editable.
     * @param   row     The row of the required cell.
     * @param   column  The column of the required cell.
     *@return   <code>true</code> if the cell is editable; <code>false</code> otherwise.
     */
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }
//
// Implementation of the TableModelListener interface, 
//
    // By default forward all events to all the listeners. 
    /**
     * Implementation of the TableModelListener interface.
     * By default forward all events to all the listeners.
     * @param  e   A <code>TableModelEvent</code> event.
     */
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}
