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

import om.console.*;
import om.util.*;

import java.util.*;
import java.lang.*;
import java.io.*;
import javax.swing.*;

import ocs.utils.*;

import org.apache.log4j.Logger;


public class ExecuteJCMT extends Execute implements Runnable {

    static Logger logger = Logger.getLogger(ExecuteJCMT.class);
    private SpItem _itemToExecute;
    private static String jcmtDir = File.separator +
	"jcmtdata" + File.separator + "orac_data";
    
    public ExecuteJCMT(SpItem item) throws Exception {
	_itemToExecute = item;
    };

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
	    logger.error("No transation process defined");
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
	    System.out.println("Running command "+command);
	    Process p = rt.exec(command);
	    InputStream istream = p.getInputStream();
	    istream.read(odfFile);
	    InputStream estream = p.getErrorStream();
	    estream.read(errorMessage);
	    int rtn = p.waitFor();
	    System.out.println("Translator returned with exit status "+rtn);
	    System.out.println("Output from translator: "+new String(odfFile));
	    System.out.println("Error from translator: "+new String(errorMessage));
	    if (rtn != 0) return;
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
// 		command = "/home/dewitt/bin/loadSCUQUEUE.ksh "+ new String (odfFile);
		String command = "/jac_sw/omp/QT/bin/loadSCUQUEUE.ksh "+ new String (odfFile);
		System.out.println ("Running command "+command+" &");
		Process p = rt.exec(command);
		InputStream istream = p.getInputStream();
		InputStream estream = p.getErrorStream();
		istream.read(odfFile);
		estream.read(errorMessage);
		int rtn = p.exitValue();
		System.out.println("LoadSCUQUEUE returned with exit status "+rtn);
		System.out.println("Output from LoadSCUQUEUE: "+new String(odfFile));
		System.out.println("Error from LoadSCUQUEUE: "+new String(errorMessage));
		if (rtn != 0) {
		    logger.error("Non-zero exit condition returned from LOADQ - "+rtn);
		    success.delete();
		}
	    }
	    catch (IOException ioe) {
		logger.error("Error executing LOADQ...", ioe);
		success.delete();
		return;
	    }
	}
	failure.delete();
	return;
    }
}
