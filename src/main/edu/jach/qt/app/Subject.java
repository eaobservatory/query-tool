package edu.jach.qt.app;

/**
 * Subject.java
 *
 *
 * Created: Mon Jul 23 12:09:12 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public interface Subject {
   /**
    * Describe <code>addObserver</code> method here.
    *
    * @param o an <code>Observer</code> value
    */
   public void addObserver( Observer o );

   /**
    * Describe <code>removeObserver</code> method here.
    *
    * @param o an <code>Observer</code> value
    */
   public void removeObserver( Observer o );
}// Subject
