package edu.jach.qt.gui;

import java.lang.*;
import java.io.*;
import javax.swing.*;


/**
 * Display the current help information in an external browser.
 * The help files are located inthe config directory in the
 * subdirectory Documents.  If the user specified a Browser
 * in the qtSystem config file, that browser will be used for
 * display.  If that fails, or no browser is specified, then
 * this will try to use some others.  Currently, ther supported
 * browsers are:
 *<ul>
 * <li> netscape 
 * <li> opera
 * <li> mosaic
 * <li> xemacs
 * <li> mozilla
 *</ul>
 *
 * @author $Author$
 * @version $Id$
 */
class HelpPage {

    private String helpFile;
    private final String [] browserList = {"netscape",
					   "opera",
					   "mosaic",
					   "xemacs -f w3",
					   "mozilla"
    };

    /**
     * Constructor.
     * Attempts to display the help page in a browser.
     */
    public HelpPage() {
	File tmp  = new File (System.getProperty("qtConfig"));
	String dir = tmp.getAbsoluteFile().getParent();
	helpFile = dir + File.separator + "Documents/AboutTheQT.html";
	String browser = System.getProperty("Browser");
	String command = browser + " file://" + helpFile;
	Runtime rt = Runtime.getRuntime();
	try {
	    Process proc = rt.exec(command);
	    rt.addShutdownHook(new ShutdownThread(proc));
	}
	catch (IOException ioe) {
	    Process proc = this.tryAlternate(rt);
	    if (proc != null) {
		rt.addShutdownHook(new ShutdownThread(proc));
	    }
	}
    }

    private Process tryAlternate(Runtime rt) {
	int i;
	Process proc = null;
 	for (i=0; i<browserList.length; i++ ) {
	    String command = browserList[i] + " file://" + helpFile;
	    try {
		proc = rt.exec(command);
		break;
	    }
	    catch (IOException ioe) {continue;}
	}
	if (i == browserList.length) {
	    JOptionPane.showMessageDialog(null, 
					  "No suitable browser found",
					  "ERROR - No Browser",
					  JOptionPane.ERROR_MESSAGE);
	}
	return proc;
    }

    class ShutdownThread extends Thread {
	private Process _proc;
	ShutdownThread(Process proc) {
	    _proc = proc;
	}
	public void run () {
	    _proc.destroy();
	}
    }

    public static void main (String [] args) {
	System.setProperty("Browser", "/usr/lib/mozilla/mozilla-bin");
	System.setProperty("Browser", "xemacs -f w3");
	System.setProperty("Browser", "fred");
	HelpPage helpPage = new HelpPage();
    }


}
