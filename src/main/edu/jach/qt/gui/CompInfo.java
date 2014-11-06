/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
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

import java.util.LinkedList;

/**
 * A linked list of interface components.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
class CompInfo {
    private String title = "";
    private int view = -1;
    private LinkedList<String> list;

    /**
     * Constructor.
     */
    public CompInfo() {
        list = new LinkedList<String>();
    }

    /**
     * Add an object to the list
     *
     * @param obj The object to add.
     */
    public void addElem(String obj) {
        list.add(obj);
    }

    /**
     * Get an object from the list.
     *
     * @param i The index of the element to retrieve.
     * @return The object at the i'th position in the list.
     */
    public Object getElem(int i) {
        return list.get(i);
    }

    /**
     * Get the entrie list of objects.
     *
     * @return All of objects in the list.
     */
    public LinkedList<String> getList() {
        return list;
    }

    /**
     * Get the number of items in the list.
     *
     * @return The number of objects in the list.
     */
    public int getSize() {
        return list.size();
    }

    /**
     * Set whether the objects are viewable. -1 indicates not viewable.
     *
     * @param view Value indicating whether an object is viewable.
     */
    public void setView(int view) {
        this.view = view;
    }

    /**
     * Get whether the objects are viewable.
     *
     * @return A integer indicating whether the list contains viewable objects.
     */
    public int getView() {
        return view;
    }

    /**
     * Set a title to assocaite with this list..
     *
     * @param title The title to associate with this list.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the value of title.
     *
     * @return value of title.
     */
    public String getTitle() {
        return title;
    }
}
