package edu.jach.qt.gui ;

import java.util.Hashtable ;
import edu.jach.qt.app.Subject ;
import edu.jach.qt.app.Observer ;
import java.util.ArrayList ;
import java.util.Iterator ;

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
public class WidgetDataBag implements Subject
{
	private Hashtable<String,Object> table = new Hashtable<String,Object>() ;
	private ArrayList<Observer> observers = new ArrayList<Observer>() ;

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
		{
			o = table.put( key , value ) ;
			notifyObservers() ;
		}
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

	/**
	 * The <code>addObserver</code> method adds an observer to the
	 * ArrayList of observers.
	 *
	 * @param o an <code>Observer</code> value
	 */
	public void addObserver( Observer o )
	{
		observers.add( o ) ;
	}

	/**
	 * The <code>removeObserver</code> method removes an observer from
	 * the ArrayList of observers.
	 *
	 * @param o an <code>Observer</code> value
	 */
	public void removeObserver( Observer o )
	{
		observers.remove( o ) ;
	}

	private void notifyObservers()
	{
		// loop through and notify each observer 
		Iterator<Observer> i = observers.iterator() ;
		while( i.hasNext() )
		{
			Observer o = i.next() ;
			o.update( this ) ;
		}
	}

}// WidgetDataBag
