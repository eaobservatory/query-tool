package edu.jach.qt.app;

/**
 * Interface <code>Subject</code> is implemented by the WidgetDataBag
 * class.  
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */

public interface Subject {
   /**
    * The <code>addObserver</code> method adds Observers to 
    * the list that will be notified of subject changes.
    *
    * @param o an <code>Observer</code> value
    */
   public void addObserver( Observer o );

   /**
    * The <code>removeObserver</code> method removes Observers 
    * from the list that will be notified of subject changes.
    *
    * @param o an <code>Observer</code> value
    */
   public void removeObserver( Observer o );
}// Subject
