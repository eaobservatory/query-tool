/*
 * Copyright (C) 2006-2009 Science and Technology Facilities Council.
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

import orac.util.OrderedMap;

public class MsbColumns extends OrderedMap<String, MsbColumnInfo> {
    public void add(MsbColumnInfo msbColumnInfo) {
        super.add(msbColumnInfo.getName(), msbColumnInfo);
    }

    public MsbColumnInfo removeIndex(final int index) {
        final MsbColumnInfo msbColumnInfo = super.remove(index);
        return msbColumnInfo;
    }

    public MsbColumnInfo removeName(final String name) {
        final MsbColumnInfo msbColumnInfo = super.remove(name);
        return msbColumnInfo;
    }

    public MsbColumnInfo findName(final String name) {
        final MsbColumnInfo msbColumnInfo = super.find(name);
        return msbColumnInfo;
    }

    public MsbColumnInfo findIndex(final int index) {
        final MsbColumnInfo msbColumnInfo = super.find(index);
        return msbColumnInfo;
    }

    public void setVisibility(String name, boolean visible) {
        final MsbColumnInfo msbColumnInfo = findName(name);
        if (msbColumnInfo != null) {
            msbColumnInfo.setVisible(visible);
        }
    }

    public void setVisibility(int index, boolean visible) {
        MsbColumnInfo msbColumnInfo = findIndex(index);
        if (msbColumnInfo != null) {
            msbColumnInfo.setVisible(visible);
        }
    }

    public boolean getVisibility(String name) {
        MsbColumnInfo msbColumnInfo = findName(name);
        if (msbColumnInfo != null) {
            return msbColumnInfo.getVisible();
        }

        return false;
    }

    public boolean getVisibility(int index) {
        MsbColumnInfo msbColumnInfo = findIndex(index);
        if (msbColumnInfo != null) {
            return msbColumnInfo.getVisible();
        }

        return false;
    }
}
