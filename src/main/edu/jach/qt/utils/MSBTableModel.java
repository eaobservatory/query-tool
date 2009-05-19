/*
 * Copied out of edu/jach/qt/gui/XmlUtils.java
 */

package edu.jach.qt.utils ;

import java.util.Vector ;
import java.util.TreeMap ;

public class MSBTableModel
{
	final private String _projectId ;
	final private MsbColumns _columnData ;
	final private TreeMap<String,Vector<Object>> treeMap ;
	private int rowCount ;
	private boolean rowCountCached = false ;
	private int widthCount ;
	private boolean widthCached = false ;
	final private Vector<Integer> indexes ;
	static int currentIndices ;

	public MSBTableModel( String project )
	{
		_projectId = project ;
		_columnData = MsbClient.getColumnInfo() ;
		treeMap = new TreeMap<String,Vector<Object>>() ;

		indexes = new Vector<Integer>() ;
	}

	public void clear()
	{
		final int vectorSize = getRowCount() ;
		Vector<Object> vector ;
		while( treeMap.size() != 0 )
		{
			vector = treeMap.remove( treeMap.firstKey() ) ;
			vector.clear() ;
		}
		rowCountCached = false ;

		currentIndices -= vectorSize ;
		indexes.clear() ;
	}

	public String getProjectId()
	{
		return _projectId ;
	}

	public void insertData( final String column , final Object data )
	{
		Vector<Object> vector = null ;
		vector = treeMap.get( column ) ;
		if( vector == null )
		{
			vector = new Vector<Object>() ;
			treeMap.put( column , vector ) ;
		}
		vector.add( data ) ;
	}

	public void bumpIndex()
	{
		indexes.add( new Integer( currentIndices++ ) ) ;
	}

	public int getRowCount()
	{
		if( !rowCountCached )
		{
			final Vector<Object> vector = treeMap.get( treeMap.firstKey() ) ;
			rowCount = vector.size() ;
			rowCountCached = true ;
		}
		return rowCount ;
	}

	public void moveColumnToEnd( final int index )
	{
		if( index >= _columnData.size() )
			return ;
		final MsbColumnInfo tmp = _columnData.removeIndex( index ) ;
		_columnData.add( tmp ) ;
	}

	public Object getData( final int row , final int column )
	{
		final String name = _columnData.getNameForIndex( column ) ;
		Vector<Object> vector = null ;
		vector = treeMap.get( name ) ;
		if( vector == null )
			return null ;
		if( row > vector.size() )
			return null ;
		return vector.elementAt( row ) ;
	}

	public boolean isVisible( final int index )
	{
		return _columnData.getVisibility( index ) ;
	}

	public int getWidth()
	{
		if( !widthCached )
			widthCount = _columnData.size() ;
		return widthCount ;
	}

	public Vector<Integer> getIndices()
	{
		return indexes ;
	}
}
