package edu.jach.qt.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.beans.*;

import calpa.html.CalHTMLPane;
import org.apache.log4j.Logger;


public class HTMLViewer extends JFrame implements ActionListener {
    private static final String _title      = "Heterodyne Observation";
    private static URL          _baseURL    = null;
    private static CalHTMLPane  _calPane;
    private static JOptionPane  _optPane;


    private JPanel     buttonPanel = new JPanel();
    private JButton    _homeButton = new JButton("Return to Translation");
    private JButton    _exitButton = new JButton("Dismiss");

    static Logger logger = Logger.getLogger(HTMLViewer.class);

    private static boolean closing = false;
    
    public HTMLViewer (String fName) {
	super (_title);
	_calPane = new CalHTMLPane();
	loadFile(fName);
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(_calPane, BorderLayout.CENTER);
	getContentPane().add(_homeButton, BorderLayout.SOUTH);
	_homeButton.addActionListener(this);
	setSize(950,500);
	validateTree();
	show();
    }

    public void loadFile(String fName) {
	URL url = null;
	try {
	    url = new URL("file://"+fName);
	}
	catch (Exception e) {
	    logger.error("Unable to convert file "+fName+" to URL");
	}
	_calPane.showHTMLDocument(url);
	_baseURL = url;
	
    }

    public void actionPerformed(ActionEvent e) {
	_calPane.showHTMLDocument(_baseURL);
    }
}
