package edu.jach.qt.app;

/**
 * Interface <code>Observer</code> is implemented by the Querytool
 * object.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
public interface Observer
{
	/**
	 * The <code>update</code> method is a callback for a 
	 * any Subject noting a change in its sensitive attributes.
	 *
	 * @param o a <code>Subject</code> value
	 */
	public void update( Subject o );
}// Observer
