package edu.jach.qt.gui;

import java.util.*;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.*;


class ProjectTableSelectionModel 
    extends DefaultListSelectionModel
    implements ListSelectionListener {

    QtFrame _qtf;

    public ProjectTableSelectionModel (QtFrame parent) {
	_qtf = parent;
	this.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
	this.setSelectionInterval(0,0);
	addListSelectionListener(this);
	_qtf.getModel().setProjectId("All");

    }

    public void valueChanged(ListSelectionEvent e) {
	// Get the MSBQueryTableModel
	String projectID = (String)_qtf.getProjectModel().getValueAt(getMinSelectionIndex(), 0);
	if (projectID == null || projectID.equals("") || projectID.startsWith("-")) return;
//   	_qtf.updateColumnSizes();
	_qtf.getModel().setProjectId(projectID);
  	_qtf.setColumnSizes();
    }
}
