package edu.jach.qt.utils;

import java.util.TreeMap;
import java.util.Vector;

/**
 * 
 * OrderedMap is a map, but retains the order items were placed into the structure.
 * Items fetched by name have the lookup speed of a map, with the slight overhead 
 * of calling the map's methods indirectly. Items can be found by name or by index.
 * 
 * Nb. OrderedMap does not check for duplication, what can happen is that a duplicate 
 * key will be overridden by the first instance if searching for it's index, and the 
 * object will have been replaced by the duplicate ( most recent addition of. )
 * Multiple indices will point to the duplicated object.
 *
 */

public class OrderedMap
{
	final private TreeMap treeMap;
	final private Vector vector;
	private int size;

	public OrderedMap()
	{
		treeMap = new TreeMap();
		vector = new Vector();
	}

	public synchronized void add( final String key , final Object object )
	{
		synchronized( treeMap )
		{
			synchronized( vector )
			{
				treeMap.put( key , object );
				vector.add( key );
				size++ ;
			}
		}
	}

	public synchronized Object remove( final int index )
	{
		synchronized( vector )
		{
			synchronized( treeMap )
			{
				final Object name = vector.remove( index );
				final Object object = treeMap.remove( name );
				if( object != null )
					size-- ;
				return object;
			}
		}
	}

	public synchronized Object remove( final String name )
	{
		synchronized( treeMap )
		{
			synchronized( vector )
			{
				final Object object = treeMap.remove( name );
				vector.remove( name );
				if( object != null )
					size-- ;
				return object;
			}
		}
	}

	public Object find( final String name )
	{
		final Object object = treeMap.get( name );
		return object;
	}

	public Object find( int index )
	{
		final Object name = vector.elementAt( index );
		Object object = treeMap.get( name );
		return object;
	}

	public String getNameForIndex( int index )
	{
		String returnValue = "";
		Object temp = vector.elementAt( index );
		if( temp instanceof String )
			returnValue = ( String )temp;
		return returnValue;
	}

	public int getIndexForName( String name )
	{
		return vector.indexOf( name );
	}

	public int size()
	{
		return size;
	}

	public void clear()
	{
		vector.clear();
		treeMap.clear();
		size = 0;
	}

	public synchronized void move( String name , int index )
	{
		int current = getIndexForName( name );
		if( current != index && current > -1 )
		{
			vector.remove( current );
			vector.insertElementAt( name , index );
		}
	}

	public synchronized void move( int currentIndex , int newIndex )
	{
		if( currentIndex == newIndex )
			return;
		if( currentIndex > -1 && currentIndex < size() )
		{
			Object object = vector.remove( currentIndex );
			newIndex = newIndex < size() ? newIndex : size() - 1;
			vector.insertElementAt( object , newIndex );
		}
	}
}
