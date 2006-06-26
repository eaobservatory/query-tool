/*
* Copied out of edu/jach/qt/gui/XmlUtils.java
*/

package edu.jach.qt.utils ;

import java.util.Vector ;
import java.util.TreeMap ;

public class MSBTableModel
{
	private String _projectId;
	private MsbColumns _columnData ;
	private TreeMap treeMap ;
	
	int rowCount ;
	boolean rowCountCached = false ;
	
	int widthCount ;
	boolean widthCached = false ;
	
	Vector indexes ;
	static int currentIndices ;
	
	public MSBTableModel( String project )
	{
		_projectId = project;
		_columnData = MsbClient.getColumnInfo() ;
		treeMap = new TreeMap() ; 
		
		indexes = new Vector() ;
	}

	public void clear()
	{
		int vectorSize = 0 ;
		Vector vector ;
		while( treeMap.size() != 0 )
		{
			vector = ( Vector )treeMap.remove( treeMap.firstKey() ) ;
			vectorSize = vector.size() ;
			vector.clear() ;
		}
		rowCountCached = false ;
		
		currentIndices -= vectorSize ;
		indexes.clear() ;
	}

	public String getProjectId()
	{
		return _projectId;
	}

	public void insertData( String column , Object data )
	{
		Vector vector = null ; 
		vector = ( Vector )treeMap.get( column ) ;
		if( vector == null )
		{
			vector = new Vector() ;
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
			Vector vector = ( Vector )treeMap.get( treeMap.firstKey() ) ;
			rowCount = vector.size() ;
			rowCountCached = true ;
		}
		return rowCount ;
	}
	
	public void moveColumnToEnd( int index )
	{
		if( index >= _columnData.size() )
			return ;
		MsbColumnInfo tmp = _columnData.removeIndex( index ) ;
		_columnData.add( tmp ) ;
	}

	public Object getData( int row , int column )
	{
		String name = _columnData.getNameForIndex( column ) ;
		Vector vector = null ; 
		vector = ( Vector )treeMap.get( name ) ;
		if( vector == null )
			return null ;
		if( row > vector.size() )
			return null ;
		return vector.elementAt( row ) ;
	}
	
	public boolean isVisible( int index )
	{
		return _columnData.getVisibility( index ) ;
	}
	
	public int getWidth()
	{
		if( !widthCached )
			widthCount = _columnData.size() ;
		return widthCount ;
	}
	
	public Vector getIndices()
	{
		return indexes ;
	}
}
