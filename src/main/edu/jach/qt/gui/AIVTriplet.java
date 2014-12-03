/*
 * Copyright 2002 United Kingdom Astronomy Technology Centre, an
 * establishment of the Science and Technology Facilities Council.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Created on 05 March 2002, 10:24
 * by David Clarke
 */

package edu.jach.qt.gui;

import gemini.sp.SpItem;

/**
 * An AIVTriplet represents an attribute-value pair within an iterator.
 *
 * It has the following fields:
 *     String attribute       - the name of the attribute in question
 *     String iterator        - the name of the iterator in which the
 *                              attribute resides
 *     String value           - a String representation of the value of the
 *                              attribute.
 *     AttributeOrigin origin - whence the attribute came
 *
 * @author David Clarke (dac)
 */
public class AIVTriplet {
    private String _attribute;
    private String _iterator;
    private String _value;
    private AttributeOrigin _origin;

    /**
     * Basic constructor.
     *
     * @param iterator The name of the iterator
     * @param attribute The name of the attribute
     * @param value The value of the attribute
     * @param origin The original source of the attribute.
     */
    public AIVTriplet(String iterator, String attribute, String value,
            AttributeOrigin origin) {
        _attribute = attribute;
        _iterator = iterator;
        _value = value;
        _origin = origin;
    }

    /**
     * Basic constructor.
     */
    public AIVTriplet(String iterator, String attribute, String value,
            SpItem item, String name, int index) {
        _attribute = attribute;
        _iterator = iterator;
        _value = value;
        _origin = new AttributeOrigin(item, name, index);
    }

    /**
     * Empty constructor.
     */
    public AIVTriplet() {
    }

    /**
     * Return the iterator.
     */
    public String iterator() {
        return _iterator;
    }

    /**
     * Return the attribute.
     */
    public String attribute() {
        return _attribute;
    }

    /**
     * Return the value.
     */
    public String value() {
        return _value;
    }

    /**
     * Return the origin.
     */
    public AttributeOrigin origin() {
        return _origin;
    }

    /**
     * String representation.
     */
    public String toString() {
        return "(" + iterator() + ":" + attribute() + " = " + value() + ")";
    }
}
