package edu.jach.qt.utils ;

import java.util.TreeMap ;
import java.util.Vector ;

public class OrderedMap
{
	
	private TreeMap treeMap ;	
	private Vector vector ;
	private int size ;
	
	public OrderedMap()
	{
		treeMap = new TreeMap() ;
		vector = new Vector() ;
	}
	
	public void add( String key , Object object )
	{
		vector.add( key ) ;
		treeMap.put( key , object ) ;
		size++ ;
	}

	public Object remove( int index )
	{
		Object name = vector.remove( index ) ;
		Object object = treeMap.remove( name ) ;
		if( object != null )
			size-- ;
		if( vector.size() != treeMap.size() )
			System.out.print( "Columns : Error in removing by index" ) ;
		return object ;
	}

	public Object remove( String name )
	{
		Object object = treeMap.remove( name ) ;
        vector.remove( name ) ;
		if( object != null )
			size-- ;
		if( vector.size() != treeMap.size() )
			System.out.print( "Columns : Error in removing by name" ) ;
        return object ;
	}

	public Object find( String name )
	{
		Object object = treeMap.get( name ) ;
		return object ;
	}

	public Object find( int index )
    {
		Object name = vector.elementAt( index ) ;
		Object object = treeMap.get( name ) ;
		return object ;
    }
	
	public int findIndex( String name )
	{
		return vector.indexOf( name ) ;
	}
	
	public int size()
	{
		if( vector.size() != treeMap.size() || size != vector.size() || size != treeMap.size() )
			System.out.print( "Columns : Size does not represent internal dimensions" ) ;
		return size ;
	}

	public void clear()
	{
		vector.clear() ;
		treeMap.clear() ;
	}
}
