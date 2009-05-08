package edu.jach.qt.gui ;

import java.util.Hashtable ;

/**
 * WidgetDataBag.java
 * 
 * This class provides a gateway between the gui and app
 * partitions. The data describing this class is just a hashtable
 * consisting of widget names as keys and the objects that define them
 * as the values.  The WidgetDataBag is instantiated in the WidgetPanel
 * class where widgets are added to the bag.  This essentially
 * registers the widget to be observed by the Querytool class.
 *
 * Created: Mon Jul 23 13:02:58 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class WidgetDataBag
{
	private Hashtable<String,Object> table = new Hashtable<String,Object>() ;

	/**
	 * The <code>put</code> method adds the key value pair to the
	 * Hashtable and notifies subscribed observers.
	 *
	 * @param key an <code>Object</code> value
	 * @param value an <code>Object</code> value
	 * @return the previous <code>Object</code> value of the specified key, or null if it did not have one.
	 */
	public Object put( String key , Object value )
	{
		Object o = null ;
		if( key != null )
			o = table.put( key , value ) ;
		return o ;
	}

	/**
	 * The <code>toString</code> method represents this Hashtable as a
	 * string. The result is a list of comma delimited Key=value pairs
	 * enclosed in curly braces.
	 *
	 * @return a <code>String</code> value
	 */
	public String toString()
	{
		return table.toString() ;
	}

	/**
	 * Describe <code>getHash</code> method here.
	 *
	 * @return a <code>Hashtable</code> value
	 */
	public Hashtable<String,Object> getHash()
	{
		return table ;
	}

}// WidgetDataBag
