package edu.jach.qt.gui ;

import java.util.Vector ;
import javax.swing.event.TableModelListener ;
import javax.swing.event.TableModelEvent ;
import javax.swing.table.AbstractTableModel ;

import edu.jach.qt.utils.JACLogger ;

public class ProjectTableModel extends AbstractTableModel implements Runnable , TableModelListener
{
	static final JACLogger logger = JACLogger.getLogger( ProjectTableModel.class ) ;
	private static String[] colName = { "projectid" , "priority" } ;
	private static Class<?>[] colClass = { String.class , Integer.class } ;
	private Vector<String> projectIds = new Vector<String>() ;
	private Vector<Integer> priorities = new Vector<Integer>() ;

	public ProjectTableModel()
	{
		addTableModelListener( this ) ;
	}

	public void run()
	{

		fireTableChanged( null ) ;
	}

	public int getColumnCount()
	{
		return colName.length ;
	}

	public Class<?> getColumnClass( int index )
	{
		return colClass[ index ] ;
	}

	public String getColumnName( int index )
	{
		return colName[ index ] ;
	}

	public int getRowCount()
	{
		return projectIds.size() ;
	}

	public Object getValueAt( int r , int c )
	{
		if( r < 0 || projectIds.size() == 0 )
			return "----" ;
		if( colName[ c ].equalsIgnoreCase( "projectid" ) )
			return projectIds.elementAt( r ) ;
		else if( colName[ c ].equalsIgnoreCase( "priority" ) )
			return priorities.elementAt( r ) ;
		else
			return "---" ;
	}

	public void setValueAt( Object value , int r , int c ){}

	public void tableChanged( TableModelEvent evt )
	{
		projectIds.clear() ;
		priorities.clear() ;
		Vector[] data = XmlUtils.getProjectData() ;
		if( data != null )
		{
			projectIds = data[ 0 ] ;
			priorities = data[ 1 ] ;
		}
	}

	public void clear()
	{
		if( projectIds.size() != 0 )
		{
			projectIds.clear() ;
			priorities.clear() ;
			XmlUtils.clearProjectData() ;
			fireTableChanged( null ) ;
		}
	}
}
