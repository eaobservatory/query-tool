/*
* Copied out of edu/jach/qt/gui/XmlUtils.java
*/

package edu.jach.qt.utils ;

import java.util.Vector ;

public class MSBTableModel
{
	private String _projectId;
	private MsbColumns _columnData ;		
	
	public MSBTableModel( String project )
	{
		_projectId = project;
		_columnData = MsbClient.getColumnInfo() ;
	}

	public void clear()
	{
			_columnData.clear() ;
	}

	public String getProjectId()
	{
		return _projectId;
	}

	public void insertData( String column , Object data )
	{
		MsbColumnInfo msbColumnInfo = _columnData.findName( column ) ;
		msbColumnInfo.addToVector( data ) ;
	}

	public Vector getColumn( int index )
	{
		return _columnData.findIndex( index ).getVector() ;
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
		Vector temp = ( Vector )_columnData.findIndex( column ).getVector() ;
		return temp.elementAt( row ) ;
	}
	
	public boolean isVisible( int index )
	{
		return _columnData.getVisibility( index ) ;
	}
	
	public int getWidth()
	{
		return _columnData.size() ;
	}
}
