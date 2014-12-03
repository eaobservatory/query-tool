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

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

@SuppressWarnings("serial")
class ProjectTableSelectionModel extends DefaultListSelectionModel implements
        ListSelectionListener {

    QtFrame _qtf;

    public ProjectTableSelectionModel(QtFrame parent) {
        _qtf = parent;
        this.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        this.setSelectionInterval(0, 0);
        addListSelectionListener(this);
        _qtf.getModel().setProjectId("All");

    }

    public void valueChanged(ListSelectionEvent e) {
        // Get the MSBQueryTableModel
        String projectID = (String) _qtf.getProjectModel().getValueAt(
                getMinSelectionIndex(), 0);

        if (projectID == null || projectID.equals("")
                || projectID.startsWith("-")) {
            return;
        }

        _qtf.updateColumnHeaders();
        _qtf.getModel().setProjectId(projectID);
        _qtf.setColumnSizes();
    }
}
