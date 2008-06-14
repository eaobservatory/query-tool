package edu.jach.qt.gui ;

import java.util.LinkedList ;

/**
 * A linked list of interface components..
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
class CompInfo
{
	private String title = "" ;
	private int view = -1 ;
	private LinkedList list ;

	/**
	 * Constructor.
	 */
	public CompInfo()
	{
		list = new LinkedList() ;
	}

	/**
	 * Add an object to the list
	 * @param obj  The object to add.
	 */
	public void addElem( Object obj )
	{
		list.add( obj ) ;
	}

	/**
	 * Get an object from the list.
	 * @param i  The index of the element to retrieve.
	 * @return   The object at the i'th position in the list.
	 */
	public Object getElem( int i )
	{
		return list.get( i ) ;
	}

	/**
	 * Get the entrie list of objects.
	 * @return All of objects in the list.
	 */
	public LinkedList getList()
	{
		return list ;
	}

	/**
	 * Get the number of items in the list.
	 * @return The number of objects in the list.
	 */
	public int getSize()
	{
		return list.size() ;
	}

	/**
	 * Set whether the objects are viewable.
	 * -1 indicates not viewable.
	 * @param view  Value indicating whether an object is viewable.
	 */
	public void setView( int view )
	{
		this.view = view ;
	}

	/**
	 * Get whether the objects are viewable.
	 * @return A integer indicating whether the list contains viewable objects.
	 */
	public int getView()
	{
		return view ;
	}

	/**
	 * Set a title to assocaite with this list..
	 * @param title  The title to associate with this list.
	 */
	public void setTitle( String title )
	{
		this.title = title ;
	}

	/**
	 * Get the value of title.
	 * @return value of title.
	 */
	public String getTitle()
	{
		return title ;
	}
}
