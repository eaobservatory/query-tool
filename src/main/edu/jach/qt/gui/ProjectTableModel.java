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

import java.util.Vector;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import edu.jach.qt.utils.ProjectData;

import gemini.util.JACLogger;

@SuppressWarnings("serial")
public class ProjectTableModel extends AbstractTableModel implements Runnable,
        TableModelListener {
    static final JACLogger logger =
            JACLogger.getLogger(ProjectTableModel.class);
    private static String[] colName = {"projectid", "country", "semester", "priority"};
    private static Class<?>[] colClass = {String.class, String.class, String.class, Integer.class};
    private Vector<String> projectIds = new Vector<String>();
    private Vector<String> countries = new Vector<String>();
    private Vector<String> semesters = new Vector<String>();
    private Vector<Integer> priorities = new Vector<Integer>();

    public ProjectTableModel() {
        addTableModelListener(this);
    }

    public void run() {
        fireTableChanged(null);
    }

    public int getColumnCount() {
        return colName.length;
    }

    public Class<?> getColumnClass(int index) {
        return colClass[index];
    }

    public String getColumnName(int index) {
        return colName[index];
    }

    public int getRowCount() {
        return projectIds.size();
    }

    public Object getValueAt(int r, int c) {
        if (r < 0 || projectIds.size() == 0) {
            return "----";
        }

        if (colName[c].equalsIgnoreCase("projectid")) {
            return projectIds.elementAt(r);
        } else if (colName[c].equalsIgnoreCase("country")) {
            return countries.elementAt(r);
        } else if (colName[c].equalsIgnoreCase("semester")) {
            return semesters.elementAt(r);
        } else if (colName[c].equalsIgnoreCase("priority")) {
            return priorities.elementAt(r);
        } else {
            return "---";
        }
    }

    public void setValueAt(Object value, int r, int c) {
    }

    public void tableChanged(TableModelEvent evt) {
        projectIds.clear();
        countries.clear();
        semesters.clear();
        priorities.clear();
        Vector<ProjectData> data = XmlUtils.getProjectData();

        if (data != null) {
            for (ProjectData projectData : data) {
                projectIds.add(projectData.projectID);
                countries.add(projectData.country);
                semesters.add(projectData.semester);
                priorities.add(projectData.priority);
            }
        }
    }

    public void clear() {
        if (projectIds.size() != 0) {
            projectIds.clear();
            countries.clear();
            semesters.clear();
            priorities.clear();
            XmlUtils.clearProjectData();
            fireTableChanged(null);
        }
    }
}
