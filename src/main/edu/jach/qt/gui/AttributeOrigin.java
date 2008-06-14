/*
 * AttributeOrigin.java
 *
 * Created on 05 March 2002, 10:24
 * by David Clarke
 */

package edu.jach.qt.gui ;

import gemini.sp.SpItem ;

/**
 * An AttributeOrigin represents where a particular AVPair or
 * AIVTriplet came from. It consists of the following fields:
 *    SpItem item - the item in the observation sequence from
 *                  which the attibute came
 *    String name - the name of the attibute in the SpItem (which
 *                  might be different from the attribute in the
 *                  n-tuple due to vector expansion and/or *Iter
 *                  suffix handling.
 *    int index   - SpItem's attributes have vector values, this
 *                  is the index into that vector that the n-tuple
 *                  got its value from.
 *
 * @author David Clarke (dac)
 **/
public class AttributeOrigin
{
	private SpItem _item ;
	private String _name ;
	private int _index ;

	/** 
	 * Basic constructor 
	 * @param item  The originating item.
	 * @param name  The name of the attribute
	 * @param index The index to associate with this attribute.
	 */
	public AttributeOrigin( SpItem item , String name , int index )
	{
		_item = item ;
		_name = name ;
		_index = index ;
	}

	/** Return the item */
	public SpItem item()
	{
		return _item ;
	}

	/** Return the name */
	public String name()
	{
		return _name ;
	}

	/** Return the index */
	public int index()
	{
		return _index ;
	}

	/** Set the value of the originating attribute */
	public void setValue( String value )
	{
		item().getTable().set( name() , value , index() ) ; // Yukk    
		// Set an override flag on the attribute so it does not get over-ridden by calls to updateDAConf
		String overide = "override_" + name() ;
		item().getTable().set( overide , true , index() ) ;
	}

	/** Convert to a string */
	public String toString()
	{
		return _item.getTitle() + ":" + _name + "[" + _index + "]" ;
	}
}
