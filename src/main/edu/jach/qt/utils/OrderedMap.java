package edu.jach.qt.utils ;

import java.util.TreeMap ;
import java.util.Vector ;

public class OrderedMap
{
	
	final private TreeMap treeMap ;	
	final private Vector vector ;
	private int size ;
	
	public OrderedMap()
	{
		treeMap = new TreeMap() ;
		vector = new Vector() ;
	}
	
	public void add( final String key , final Object object )
	{
		vector.add( key ) ;
		treeMap.put( key , object ) ;
		size++ ;
	}

	public Object remove( final int index )
	{
		final Object name = vector.remove( index ) ;
		final Object object = treeMap.remove( name ) ;
		if( object != null )
			size-- ;
/*
		if( vector.size() != treeMap.size() )
			System.out.print( "Columns : Error in removing by index" ) ;
*/
		return object ;
	}

	public Object remove( final String name )
	{
		final Object object = treeMap.remove( name ) ;
        vector.remove( name ) ;
		if( object != null )
			size-- ;
/*
		if( vector.size() != treeMap.size() )
			System.out.print( "Columns : Error in removing by name" ) ;
*/
        return object ;
	}
	
	public Object find( final String name )
	{
		final Object object = treeMap.get( name ) ;
		return object ;
	}

	public Object find( int index )
    {
		final Object name = vector.elementAt( index ) ;
		Object object = treeMap.get( name ) ;
		return object ;
    }
	
	public String getNameForIndex( int index )
	{
		String returnValue = "" ;
		Object temp = vector.elementAt( index ) ;
		if( temp instanceof String )
			returnValue = ( String )temp ;
		return returnValue ;
	}
	
	public int getIndexForName( String name )
	{
		return vector.indexOf( name ) ;
	}
	
	public int size()
	{
/*
		if( vector.size() != treeMap.size() || size != vector.size() || size != treeMap.size() )
			System.out.print( "Columns : Size does not represent internal dimensions" ) ;
*/
		return size ;
	}

	public void clear()
	{
		vector.clear() ;
		treeMap.clear() ;
		size = 0 ;
	}
	
	public void move( String name , int index )
	{
		int current = getIndexForName( name ) ;
		if( current != index && current > -1 )
		{
			vector.remove( current ) ;
			vector.insertElementAt( name , index ) ;
		}
	}
	
	public void move( int currentIndex , int newIndex )
	{
		if( currentIndex == newIndex )
			return ;
		if( currentIndex > -1 && currentIndex < size() )
		{
			Object object = vector.remove( currentIndex ) ;
			newIndex = newIndex < size() ? newIndex : size() - 1 ;
			vector.insertElementAt( object , newIndex ) ;
		}
	}
}
