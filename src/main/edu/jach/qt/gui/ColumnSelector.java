package edu.jach.qt.gui;

import java.awt.event.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.*;
import java.util.*;
import javax.swing.*;

import edu.jach.qt.utils.MsbClient;


/**
 * This class is used to display a series of checkboxes which allow a
 * user to define a subset of columns which they want to appear in a table.
 * Extends <code>JFrame</code> and implements <code>ActionListener</code>.
 * @author  $Author$
 * @version $Id$
 */
public class ColumnSelector 
    extends JFrame 
    implements ActionListener {

    private JPanel columnPanel;
    private QtFrame parent;
    private MSBQueryTableModel _msbqtm;

    /**
     * Default constructor
     */
    public ColumnSelector() {
    }

    /**
     * Normal constructor.  This should be used for all calls.
     * @param  frame  The parent <code>JFrame</code>
     */
    public ColumnSelector (QtFrame frame) {
	parent = frame;
	_msbqtm = parent.getModel();
	this.setSize(150,300);
	this.getContentPane().setLayout(new BorderLayout());
	this.addCheckBoxes();
	this.addCloseButton();
	this.setVisible(true);
    }
    

    /**
     * Alternate constructor.  This can be used in place of the standard
     * constructor, but the resulting table may be displayed incorrectly.
     * @param model    The model used to generate the table.
     */
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

    /**
     * <code>ActionEvent</code> handler.
     * This is run when the "Accept" button is selected.  It tells the model
     * to update its columns.  The selected columns are passed as a 
     * <code>BitSet</code>.  The window is then dismissed.
     * @param  evt  The default <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent evt) {
	BitSet selected = new BitSet(_msbqtm.getRealColumnCount());
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
