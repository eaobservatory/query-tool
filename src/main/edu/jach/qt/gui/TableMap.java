/*
 * Copyright (c) 1997-1999 by Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package edu.jach.qt.gui ;

import javax.swing.table.AbstractTableModel ;
import javax.swing.table.TableModel ;
import javax.swing.event.TableModelListener ;
import javax.swing.event.TableModelEvent ;

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
 * @author Philip Milne */

@SuppressWarnings( "serial" )
public class TableMap extends AbstractTableModel implements TableModelListener
{
	protected TableModel model ;

	/**
	 * Return the current <code>TableModel</code>.
	 * @return    The current model.
	 */
	public TableModel getModel()
	{
		return model ;
	}

	/**
	 * Sets the current <code>TableModel</code>.
	 * @param model    The model to set.
	 */
	public void setModel( TableModel model )
	{
		this.model = model ;
		model.addTableModelListener( this ) ;
	}

	// By default, implement TableModel by forwarding all messages to the model. 

	/**
	 * Get a value at a specified row and column.
	 * @param aRow    The row from which to get the data.
	 * @param aColumn The column from which to get the data.
	 * @return        The object at the specified row and colmn.
	 */
	public Object getValueAt( int aRow , int aColumn )
	{
		return model.getValueAt( aRow , aColumn ) ;
	}

	/**
	 * Get a value at a specified row and column.
	 * @param aValue  The object to be set.
	 * @param aRow    The row from which to set the data.
	 * @param aColumn The column from which to set the data.
	 */
	public void setValueAt( Object aValue , int aRow , int aColumn )
	{
		model.setValueAt( aValue , aRow , aColumn ) ;
	}

	/**
	 * Get the number of rows in the current model.
	 * @return  The number of rows (0 if no model defined).
	 */
	public int getRowCount()
	{
		return ( model == null ) ? 0 : model.getRowCount() ;
	}

	/**
	 * Get the number of columns in the current model.
	 * @return  The number of columns (0 if no model defined).
	 */
	public int getColumnCount()
	{
		return ( model == null ) ? 0 : model.getColumnCount() ;
	}

	/**
	 * Get the name associated with a specified column index.
	 * @param aColumn   The column index.
	 * @return          The name associated with the column index.
	 */
	public String getColumnName( int aColumn )
	{
		return model.getColumnName( aColumn ) ;
	}

	/**
	 * Get the <code>Class</code> of the entries in a specified column.
	 * @param aColumn   The column index.
	 * @return          The <code>Class</code> associated with the column index.
	 */
	public Class<?> getColumnClass( int aColumn )
	{
		return model.getColumnClass( aColumn ) ;
	}

	/**
	 * Determines if a particular cell in the <code>TableModel</code> is editable.
	 * @param   row     The row of the required cell.
	 * @param   column  The column of the required cell.
	 *@return   <code>true</code> if the cell is editable ; <code>false</code> otherwise.
	 */
	public boolean isCellEditable( int row , int column )
	{
		return model.isCellEditable( row , column ) ;
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
	public void tableChanged( TableModelEvent e )
	{
		fireTableChanged( e ) ;
	}
}
