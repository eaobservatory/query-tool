/*
 * AVPair.java
 *
 * Created on 05 March 2002, 10:24
 * by David Clarke
 */

package edu.jach.qt.gui;

import gemini.sp.*;
import gemini.sp.iter.*;

/**
 * An AVPair represents an attribute-value pair. It has the following
 * fields:
 *    String attribute - the name of the attribute in question
 *    String value     - a String representation of the value
 *                       of the attribute.
 *    AttributeOrigin origin - whence the attribute came
 *
 * @author David Clarke (dac)
 **/
public class AVPair {

  private String          _attribute;
  private String          _value;
  private AttributeOrigin _origin;

  /**
   * Basic constructor 
   * @param  attribute   The name of the attribute.
   * @param  value       The value of the attribute.
   * @param  origin      The originating source of the attribute.
   */
  public AVPair(String attribute,
		String value,
		AttributeOrigin origin) {
    _attribute = attribute;
    _value     = value;
    _origin    = origin;
  }

  /**
   * Basic constructor 
   * @param  attribute   The name of the attribute.
   * @param  value       The value of the attribute.
   * @param  item        The item which is the source of the attribute
   * @param  name        The name associated with the <code>item</code>
   * @param  index       An index to associate with the <code>item</code>.
   */
  public AVPair(String attribute,
		String value,
		SpItem item,
		String name,
		int    index) {
    _attribute = attribute;
    _value     = value;
    _origin    = new AttributeOrigin(item, name, index);
  }

  /** Default constructor 
   */
  public AVPair() {
  }

  /** Return the attribute */
  public String attribute() {
    return _attribute;
  }

  /** Return the value */
  public String value() {
    return _value;
  }

  /** Return the origin */
  public AttributeOrigin origin() {
    return _origin;
  }

  /** String representation */
  public String toString() {
    return "(" + attribute() + " = " + value() + ")";
  }

//  /** Set the attribute */
//  public void setAttribute(String attribute) {
//    setFirst(attribute);
//  }
//
//  /** Set the value */
//  public void setValue(String value) {
//    setSecond(value);
//  }

}
    
  
