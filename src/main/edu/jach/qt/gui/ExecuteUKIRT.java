package edu.jach.qt.gui;

import edu.jach.qt.utils.*;
import gemini.sp.*;
import gemini.sp.obsComp.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import ocs.utils.*;
//import om.console.*;
//import om.util.*;
import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;
import org.apache.log4j.Logger;


public class ExecuteUKIRT extends Execute implements Runnable {

    static Logger logger = Logger.getLogger(ExecuteUKIRT.class);

    private String myInst;

    
    public ExecuteUKIRT() throws Exception {
    };

    public void run () {
	System.out.println("Starting execution...");
	File success = new File ("/ukirtdata/orac_data/deferred/.success");
	File failure = new File ("/ukirtdata/orac_data/deferred/.failure");
	success.delete();
	failure.delete();
	try {
	    success.createNewFile();
	    failure.createNewFile();
	}
	catch (IOException ioe) {
	    logger.error("Unable to create success/fail file", ioe);
	    return;
	}

	SpItem itemToExecute;
	if (!isDeferred) {
	    itemToExecute = ProgramTree.selectedItem;
	    logger.info("Executing observation from Program List");
	}
	else {
	    itemToExecute = DeferredProgramList.currentItem;
	    logger.info("Executing observation from deferred list");
	}
	
	SpItem inst = (SpItem) SpTreeMan.findInstrument(itemToExecute);
	if (inst == null) {
	    logger.error("No instrument found");
	    success.delete();
	    return;
	}    
	
	String tname = QtTools.translate(itemToExecute, inst.type().getReadable());
	
	// Catch null sequence names - probably means translation
	// failed:
	if (tname == null) {
	    //new ErrorBox ("Translation failed. Please report this!");
	    logger.error("Translation failed. Please report this!");
	    success.delete();
	    return;
	}
	else{
	    logger.info("Trans OK");
	    logger.debug("Translated file is "+tname);
	}

	// Having successfully run through translation, now try
	// to submit the file to the ukirt instrument task
	if (TelescopeDataPanel.DRAMA_ENABLED) {
	    byte [] stdout = new byte [1024];
	    byte [] stderr = new byte [1024];
	    try {
		Runtime rt = Runtime.getRuntime();
		String command = "/jac_sw/omp/QT/bin/loadUKIRT.ksh "+tname;
		logger.debug ("Running command "+command);
		Process p = rt.exec(command);
		InputStream istream = p.getInputStream();
		InputStream estream = p.getErrorStream();
		istream.read(stdout);
		estream.read(stderr);
		p.waitFor();
		int rtn = p.exitValue();
		logger.info ("loadUKIRT task returned a value of "+rtn);
		logger.debug("Output from loadUKIRT: "+new String(stdout).trim());
		if (rtn != 0) {
		    logger.error("Error loading UKIRT task");
		    new PopUp ("Load Error",
			       new String (stderr).trim(),
			       JOptionPane.ERROR_MESSAGE).start();
		    success.delete();
		    return;
		}	 
	    }
	    catch (IOException ioe) {
		logger.error("Error executing loadUKIRT...", ioe);
		new PopUp ("Error loading UKIRT task",
			   new String(stderr).trim(),
			   JOptionPane.ERROR_MESSAGE).start();
		success.delete();
		return;
	    }
	    catch (InterruptedException ie) {
		logger.error ("loadUKIRT exited prematurely...", ie);
		success.delete();
		return;
	    }
	    catch (Exception ex) {
		logger.error("Got an unknown exception when running loadUKIRT...",ex);
		success.delete();
	    }
	}
	failure.delete();
	return;
    }

    public class PopUp extends Thread implements Serializable{
	String _message;
	String _title;
        int    _errLevel;
	public PopUp (String title, String message, int errorLevel) {
	    _message=message;
	    _title = title;
	    _errLevel=errorLevel;
	}

	public void run() {
	    JOptionPane.showMessageDialog(null,
					  _message,
					  _title,
					  _errLevel);
	}
    }
}
