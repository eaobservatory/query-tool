package edu.jach.qt.gui;

import edu.jach.qt.utils.*;

import gemini.sp.*;
import gemini.sp.obsComp.*;

import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;

//import om.console.*;
//import om.util.*;

import java.util.*;
import java.lang.*;
import java.io.*;
import javax.swing.*;

import ocs.utils.*;

import org.apache.log4j.Logger;


/**
 * Implements the executable method for JCMT.  It simply sends either a 
 * single deferred observation, or an entire science project to the SCUQUEUE.
 * This is currently only usable for SCUBA observations.
 * @see edu.jach.qt.gui.Execute
 * Implements <code>Runnable</code>
 * @author $Author$
 * @version $Id$
 */
public class ExecuteJCMT extends Execute implements Runnable {

    static Logger logger = Logger.getLogger(ExecuteJCMT.class);
    private SpItem _itemToExecute;
    private static String jcmtDir = File.separator +
	"jcmtdata" + File.separator + "orac_data";
    
    /**
     * Constructor.
     * @param  item      The item to send to SCUQUEUE
     * @throws Exception From the base class.
     */
    public ExecuteJCMT(SpItem item) throws Exception {
	_itemToExecute = item;
    };

    /**
     * Implementation of the <code>Runnable</code> interface.
     * The success or failure of the file is determined by a
     * file called .success or .failure left in a defined directory
     * when the mothod ends.  Thus it is important to make sure that
     * when this method is run as a thread, the caller joins the thread.
     * If the item is a science project, it overwrites the current contents
     * of the queue.  If it is a deferred observation, it is inserted into
     * the queue at the next convinient point.
     * <bold>Note:</bold>  This method currently uses hard coded path
     * names for the files and for the commands to execute the queue.
     */
    public void run() {
	// To execute JCMT, we write the execution to a file
	File success = new File ("/jcmtdata/orac_data/deferred/.success");
	File failure = new File ("/jcmtdata/orac_data/deferred/.failure");
	success.delete();
	failure.delete();
	try {
	    success.createNewFile();
	    failure.createNewFile();
	}
	catch (IOException ioe) {
	    logger.error("Unable to create success/fail file");
	    return;
	}

	logger.info("Executing observation "+_itemToExecute.getTitle());
	File file = new File (jcmtDir + File.separator + "ExecuteMe.xml");
	try {
	    FileWriter writer = new FileWriter (file);
	    writer.write(_itemToExecute.toXML());
	    writer.close();
	}
	catch (IOException ioe) {
	    logger.error("Error writing translation file");
	    file.delete();
	    success.delete();
	    return;
	}

	// Now send this file as an argument to the translate process
	String translator = System.getProperty("jcmtTranslator");
	if (translator == null) {
	    logger.error("No translation process defined");
	    file.delete();
	    success.delete();
	    return;
	}
	byte [] odfFile = new byte [1024];
	byte [] errorMessage = new byte [1024];
	Runtime rt;
	// Do the translation
	try {
	    rt = Runtime.getRuntime();
	    String command = translator +" "+ file.getPath();
	    logger.debug("Running command "+command);
	    Process p = rt.exec(command);
	    InputStream istream = p.getInputStream();
	    istream.read(odfFile);
	    InputStream estream = p.getErrorStream();
	    estream.read(errorMessage);
	    int rtn = p.waitFor();
	    logger.info("Translator returned with exit status "+rtn);
	    logger.debug("Output from translator: "+new String(odfFile).trim());
	    logger.debug("Error from translator: "+new String(errorMessage).trim());
	    if (rtn != 0) {
		logger.error("Returning with non-zero error status following translation");
		new PopUp("Translation Error",
			  new String(errorMessage).trim(),
			  JOptionPane.ERROR_MESSAGE).start();
		success.delete();
		return;
	    }
	}
	catch (InterruptedException ie) {
	    logger.error ("Translation exited prematurely...", ie);
	    success.delete();
	    return;
	}
	catch (IOException ioe) {
	    logger.error("Error executing translator...", ioe);
	    success.delete();
	    return;
	}
	file.delete();

	if (TelescopeDataPanel.DRAMA_ENABLED) {
	    try {
		rt = Runtime.getRuntime();
		String command;
		if (super.isDeferred) {
 		    command = "/jac_sw/omp/QT/bin/insertSCUQUEUE.ksh "+ new String(odfFile).trim();
		}
		else {
		    command = "/jac_sw/omp/QT/bin/loadSCUQUEUE.ksh "+ new String(odfFile).trim();
		}
		logger.debug ("Running command "+command);
		Process p = rt.exec(command);
		InputStream istream = p.getInputStream();
		InputStream estream = p.getErrorStream();
		istream.read(odfFile);
		estream.read(errorMessage);
		p.waitFor();
		int rtn = p.exitValue();
		logger.info("LoadSCUQUEUE returned with exit status "+rtn);
		logger.debug("Output from LoadSCUQUEUE: "+new String(odfFile).trim());
		logger.debug("Error from LoadSCUQUEUE: "+new String(errorMessage).trim());
		if (rtn != 0) {
		    logger.error("Error loading queue");
		    new PopUp ("Load Error",
			       new String(errorMessage).trim(),
			       JOptionPane.ERROR_MESSAGE).start();
		    success.delete();
		    return;
		}
	    }
	    catch (IOException ioe) {
		logger.error("Error executing LOADQ...", ioe);
		new PopUp ("Error Loading Queue",
			   new String(errorMessage),
			   JOptionPane.ERROR_MESSAGE).start();
		success.delete();
		return;
	    }
	    catch (InterruptedException ie) {
		logger.error ("LOADQ exited prematurely...", ie);
		success.delete();
		return;
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
