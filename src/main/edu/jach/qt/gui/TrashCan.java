package edu.jach.qt.gui;


import javax.swing.*;
import java.util.*;
import java.net.*;


public class TrashCan extends JLabel
{
    public static final String BIN_IMAGE = System.getProperty("binImage");
    public static final String BIN_SEL_IMAGE = System.getProperty("binImage");


    public TrashCan()
    {
	try {
	    URL url = new URL("file://"+BIN_IMAGE);
	    setIcon(new ImageIcon(url));
	}
	catch (MalformedURLException mue)
	    {
		setIcon(new ImageIcon(ProgramTree.class.getResource("file://"+BIN_IMAGE)));
	    }
    }

}
