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
import java.io.*;
import javax.swing.*;

import ocs.utils.*;

import org.apache.log4j.Logger;


public class ExecuteUKIRT extends Execute implements Runnable {

    static Logger logger = Logger.getLogger(ExecuteUKIRT.class);
    
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
	    logger.error("Unable to create success/fail file");
	    return;
	}

	SpItem itemToExecute;
	if (!isDeferred) {
	    itemToExecute = ProgramTree.selectedItem;
	    logger.info("Executing observatin from Program List");
	}
	else {
	    itemToExecute = DeferredProgramList.currentItem;
	    logger.info("Executing observation from deferred list");
	}

	//SequenceManager scm = SequenceManager.getHandle();
	
	SpItem inst = (SpItem) SpTreeMan.findInstrument(itemToExecute);
	if (inst == null) {
	    logger.error("No instrument found");
	    success.delete();
	    return;
	}
	
	// *TODO* Replace this crap!
	Translating tFlush = new Translating();
	tFlush.start();
	
	String tname = QtTools.translate(itemToExecute, inst.type().getReadable());
	JFrame frame =  tFlush.getFrame();
	if (frame == null) {
	    logger.error("Failed to get frame...");
	    success.delete();
	    return;
	}
	frame.hide();
	tFlush.stop();
	
	// Catch null sequence names - probably means translation
	// failed:
	if (tname == null) {
	    //new ErrorBox ("Translation failed. Please report this!");
	    logger.error("Translation failed. Please report this!");
	    success.delete();
	    return;
	}else{
	    logger.info("Trans OK");
	}
	
	
	// Prevent IRCAM3 and CGS4 from running together
	// figure out if the same inst. is already in use or
	// whether IRCAM3 and CGS4 would be running together
	//SequenceConsole console;
	//Vector consoleList = scm.getConsoleList().getList();

	//for(int i=0; i<consoleList.size(); i++) {
	//console = (SequenceConsole)consoleList.elementAt(i);
	    
	  if ( true ) {
	    System.out.println("Sending the oosTest");
	    
	    //int status = QtTools.execute(new String[] {"/jac_sw/omp/QT/bin/oosTest",inst.type().getReadable()}, false, true);
	    
	    //System.out.println("Status is: "+status);
	    
	    //if(inst.type().getReadable().equals(console.getInstrument())) {
	    //logger.info("TNAME IS: "+tname);

	    //console.resetObs(itemToExecute.getTitle(), tname);
	    //JOptionPane dialog = new JOptionPane();
	    //int selection = dialog.showConfirmDialog
	    //(null,"New Observation for: "+inst.type().getReadable(), "Comfirmation",
	    // JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null );
	
	    //if(selection==dialog.NO_OPTION) {
	    //return;
	    //}
		
	    //if(selection==dialog.YES_OPTION) {
	      //Run script that calls 'oosLoad OOS_<itemToExecute.getTitle()> <tname>'
	    //}
	    String myInst = inst.type().getReadable();
	    String[] cmd = {"/jac_sw/omp/QT/bin/oosTest",myInst};
	    
	    int status = -1;
	    try {
	      Process p = Runtime.getRuntime().exec(cmd);
	      BufferedReader stdOut = 
		new BufferedReader(new InputStreamReader(p.getInputStream()));

	      String s = null;
	      while ((s = stdOut.readLine()) != null) {
		System.err.println(s);
		if ( s.endsWith("NOT RUNNING")) {
		  status = 0;
		}
		else if ( s.endsWith("IS RUNNING")) {
		  status = 1;
		}
	      }
	    } catch (IOException e) {
		logger.error("StdOut got IO exception:"+e.getMessage());
	    }
	    
	    if ( status == 0 ) {
	      System.out.println("not running... send oosLoad");
	    }
	    else {
	      System.out.println("instrument running ... loadORAC deferred!");

	      String strippedTname = tname.substring(0, tname.indexOf("."));
	      String [] myCmd = {"ditscmd", "OOS_"+myInst, "load", strippedTname};
	      logger.info("Sending command: "
			  +myCmd[0]
			  +" "+myCmd[1] 
			  +" "+myCmd[2] 
			  +" "+myCmd[3] );

	      int loadStatus = QtTools.execute(myCmd, false, true);

	      failure.delete();
	      return;
	    }
    	  }
	  
	  // 	  if(inst.type().getReadable().equals("IRCAM3") && 
	  // 	     console.getInstrument().equals("CGS4")) {
	  // 	    JOptionPane.showMessageDialog (null,
	  // 					   "IRCAM3 and CGS4 cannot run at the same time.",
	  // 					   "",
	  // 					   JOptionPane.ERROR_MESSAGE);
	  // 	    success.delete();
	  // 	    return;
	  // 	  }

	  // 	  if(inst.type().getReadable().equals("CGS4") && 
	  // 	     console.getInstrument().equals("IRCAM3")) {
	  // 	    JOptionPane.showMessageDialog (null,
	  // 					   "IRCAM3 and CGS4 cannot run at the same time.",
	  // 					   "",
	  // 					   JOptionPane.ERROR_MESSAGE);
	  // 	    success.delete();
	  // 	    return;
	  // 	  }
	  //}

	if ( System.getProperty("os.name").equals("SunOS") && TelescopeDataPanel.DRAMA_ENABLED) {
	  QtTools.loadDramaTasks(inst.type().getReadable());
	    //DcHub.getHandle().register("OOS_LIST");
	}
	
	//DcHub.getHandle().register("OOS_LIST");
	
	//scm.showSequenceFrame();
	failure.delete();
    }
}
