/*
 * Copyright (C) 2001-2009 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.gui;

import java.util.Hashtable;

/**
 * This class provides a gateway between the gui and app partitions.
 *
 * The data describing this class is just a hashtable consisting of widget
 * names as keys and the objects that define them as the values. The
 * WidgetDataBag is instantiated in the WidgetPanel class where widgets are
 * added to the bag. This essentially registers the widget to be observed by
 * the Querytool class.
 *
 * Created: Mon Jul 23 13:02:58 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
public class WidgetDataBag {
    private Hashtable<String, Object> table = new Hashtable<String, Object>();

    /**
     * Add the key value pair to the Hashtable and notifies subscribed
     * observers.
     *
     * @param key a <code>String</code> value
     * @param value an <code>Object</code> value
     * @return the previous <code>Object</code> value of the specified key, or
     *         null if it did not have one.
     */
    public Object put(String key, Object value) {
        Object o = null;

        if (key != null) {
            o = table.put(key, value);
        }

        return o;
    }

    /**
     * Represents this Hashtable as a string.
     *
     * The result is a list of comma delimited Key=value pairs enclosed in curly
     * braces.
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
    public Hashtable<String, Object> getHash() {
        return table;
    }
}
