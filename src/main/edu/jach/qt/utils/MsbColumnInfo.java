/*
 * Copyright (C) 2006-2008 Science and Technology Facilities Council.
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

package edu.jach.qt.utils;

public class MsbColumnInfo {
    private String name;
    private Class<?> klass;
    private boolean visible = true;

    public MsbColumnInfo(String name, String klassType) {
        this.name = name;
        if (klassType.equalsIgnoreCase("Integer"))
            klass = Integer.class;
        else if (klassType.equalsIgnoreCase("Float"))
            klass = Number.class;
        else
            klass = String.class;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClassType() {
        return klass;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }
}
