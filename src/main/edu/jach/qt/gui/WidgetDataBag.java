package edu.jach.qt.gui;

import java.util.Hashtable;
import edu.jach.qt.app.Subject;
import edu.jach.qt.app.Observer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * WidgetDataBag.java
 *
 *
 * Created: Mon Jul 23 13:02:58 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class WidgetDataBag implements Subject {

   private Hashtable table = new Hashtable();
   private ArrayList observers = new ArrayList();

   /**
    * Describe <code>put</code> method here.
    *
    * @param key an <code>Object</code> value
    * @param value an <code>Object</code> value
    * @return an <code>Object</code> value
    */
   public Object put(Object key, Object value) {
      Object o = table.put(key, value);
      notifyObservers();
      return o;
   }

   /**
    * Describe <code>toString</code> method here.
    *
    * @return a <code>String</code> value
    */
   public String toString() {
      return table.toString();
   }

   /**
    * Describe <code>getHash</code> method here.
    *
    * @return a <code>Hashtable</code> value
    */
   public Hashtable getHash() {
      return table;
   }

   /**
    * Describe <code>remove</code> method here.
    *
    * @param key a <code>String</code> value
    * @return a <code>String</code> value
    */
   public String remove(String key) {
      if(table.containsKey(key)) {
	 String s = (String) table.remove(key);
	 notifyObservers();
	 return s;
      }
      return null;
   }

   /**
    * Describe <code>addObserver</code> method here.
    *
    * @param o an <code>Observer</code> value
    */
   public void addObserver( Observer o ) {
      observers.add( o ); 
   }
   
   /**
    * Describe <code>removeObserver</code> method here.
    *
    * @param o an <code>Observer</code> value
    */
   public void removeObserver( Observer o ) {
      observers.remove( o ); 
   }

   private void notifyObservers() { 
      // loop through and notify each observer 
      Iterator i = observers.iterator(); 
      while( i.hasNext() ) { 
	 Observer o = ( Observer ) i.next(); 
	 o.update( this ); 
      } 
   } 

}// WidgetDataBag
