package edu.jach.qt.utils;

import java.awt.event.*;
import java.awt.Window;
import java.io.File;
import org.apache.log4j.Logger;

/**
 * Class implementing a window listener.
 */
public class BasicWindowMonitor extends WindowAdapter {
    
    static Logger logger = Logger.getLogger(BasicWindowMonitor.class);

    /**
     * Impelentation of the WindowClosing method.
     *
     * @see     java.awt.Window
     *
     * @param   e    A window event
     */
    public void windowClosing(WindowEvent e) {
	logger.info ("Shutting down due to WindowClosing event");
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
