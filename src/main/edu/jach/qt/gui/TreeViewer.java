package edu.jach.qt.gui;

import java.util.*;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/* Gemini imports */
import gemini.sp.*;
import gemini.sp.obsComp.*;

/* OT imports */
import jsky.app.ot.*;
import jsky.app.ot.gui.*;

import edu.jach.qt.utils.MyTreeCellRenderer;


/**
 * Class to display an Observation as a tree in a new frame.
 */
class TreeViewer  {

    private JFrame frame;
    private JScrollPane scrollPane;


    /**
     * Constructor.
     * Creates a tree view of the input.
     * @param item  An observation (SpItem class)
     */
    public TreeViewer(SpItem item) {

	// Construct a new tree
	OtTreeWidget otTree = new OtTreeWidget ();
	SpItem [] itemArray = {item};

	// Create a science program to insert this into.
	SpItem root = SpFactory.create(SpType.SCIENCE_PROGRAM);
	otTree.resetProg((SpRootItem)root);
	otTree.spItemsAdded(root, itemArray, (SpItem)null);
	otTree.setPreferredSize(new Dimension(360,400));

	scrollPane = new JScrollPane(otTree);

	frame = new JFrame();
	frame.setSize(400,200);
	frame.getContentPane().add(scrollPane);
	frame.setTitle(item.getTitle());
	frame.setVisible(true);
    }

}
