package edu.jach.qt.utils;

import java.awt.event.*;
import java.awt.Window;

/**
 * Class implementing a window listener.
 */
public class BasicWindowMonitor extends WindowAdapter {
    
    /**
     * Impelentation of the WindowClosing method.
     *
     * @see     java.awt.Window
     *
     * @param   e    A window event
     */
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
