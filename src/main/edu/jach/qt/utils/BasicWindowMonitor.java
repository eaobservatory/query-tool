package edu.jach.qt.utils;

import java.awt.event.*;
import java.awt.Window;

public class BasicWindowMonitor extends WindowAdapter {
    
    public void windowClosing(WindowEvent e) {
	if (e != null) {
	    Window w = e.getWindow();
	    if (w != null) {
		w.setVisible(false);
		w.dispose();
	    }
	}
        System.exit(0);
    }
    
} // BasicWindowMonito
