package edu.jach.qt.gui;


import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import gemini.sp.*;


final public class NotePanel extends JPanel {

    private GridBagConstraints		gbc;
    private JTextPane textPanel;

    public NotePanel() {
	Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
	setBorder(new TitledBorder(border, "Observer Notes", 
				   0, 0, new Font("Roman",Font.BOLD,12),Color.black));
	setLayout(new BorderLayout() );
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	gbc = new GridBagConstraints();
	
	textPanel = new JTextPane();
	textPanel.setEditable(false);
	
	gbc.fill = GridBagConstraints.BOTH;
	//gbc.anchor = GridBagConstraints.EAST;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 5;
	gbc.weightx = 100;
	gbc.weighty = 100;
	add(textPanel, gbc, 0, 0, 2, 1);
    }
    
    public void add(Component c, GridBagConstraints gbc, 
		    int x, int y, int w, int h) {
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = w;
	gbc.gridheight = h;
	add(c, gbc);      
    }

    public void setNote(SpItem sp) {
	Vector noteVector = SpTreeMan.findAllItems(sp, "gemini.sp.SpNote");
	Enumeration e = noteVector.elements();
	while (e.hasMoreElements()) {
	    SpNote thisNote = (SpNote)e.nextElement();
	    if ( thisNote.isObserveInstruction() ) {
		textPanel.setText ( thisNote.getNote() );
		break;
	    }
	}
    }



}
