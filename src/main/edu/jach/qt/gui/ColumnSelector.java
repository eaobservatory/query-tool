package edu.jach.qt.gui;

import java.awt.event.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.*;
import java.util.*;
import javax.swing.*;

import edu.jach.qt.utils.MsbClient;


public class ColumnSelector 
    extends JFrame 
    implements ActionListener {

    private JPanel columnPanel;
    private QtFrame parent;
    private MSBQueryTableModel _msbqtm;

    public ColumnSelector() {
    }

    public ColumnSelector (QtFrame frame) {
	parent = frame;
	_msbqtm = parent.getModel();
	this.setSize(150,300);
	this.getContentPane().setLayout(new BorderLayout());
	this.addCheckBoxes();
	this.addCloseButton();
	this.setVisible(true);
    }
    

    public ColumnSelector (MSBQueryTableModel model) {
	// Disable the parent
	_msbqtm = model;
	this.setSize(150,300);
	this.getContentPane().setLayout(new BorderLayout());
	this.addCheckBoxes();
	this.addCloseButton();
	this.setVisible(true);
    }

    private void addCheckBoxes( ) {
	columnPanel = new JPanel(new GridLayout(0,1));
	String [] colNames = MsbClient.getColumnNames();
	BitSet bits = _msbqtm.getBitSet();
	for (int i=0; i<colNames.length; i++) {
	    JCheckBox cb = new JCheckBox(colNames[i]);
	    if (bits.get(i)) {
		cb.setSelected(true);
	    }
	    columnPanel.add(cb);
	}
	this.getContentPane().add(columnPanel, BorderLayout.CENTER);
    }

    private void addCloseButton() {
	JButton closeButton = new JButton ("Accept");
	closeButton.setLocation(375, 275);
	closeButton.addActionListener(this);
	this.getContentPane().add(closeButton, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent evt) {
	BitSet selected = new BitSet(_msbqtm.getRealColumnCount());
	System.out.println("Number of components: "+columnPanel.getComponentCount());
	for (int i=0; i<columnPanel.getComponentCount(); i++) {
	    if (columnPanel.getComponent(i) instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox)columnPanel.getComponent(i);
		if (cb.isSelected()) {
		    selected.set(i);
		}
		else {
		    selected.clear(i);
		}
	    }
	}

	_msbqtm.updateColumns(selected);
	if (parent != null) {
	    parent.setTableToDefault();
	}
	this.dispose();
    }
}
