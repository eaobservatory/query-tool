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
    failure.delete();
    /*	
    SpItem inst = (SpItem) SpTreeMan.findInstrument(itemToExecute);
    if (inst == null) {
      logger.error("No instrument found");
      success.delete();
      return;
    }
	
    myInst = inst.type().getReadable();
    String[] oosStateCmd = {"/jac_sw/omp/QT/bin/oosTest", 
			    " -instrument", 
			    " OOS_"+myInst, 
			    " -state", 
			    " State"};
    int oosState = QtTools.oosTest(oosStateCmd);

    // If this oos is all ready in a Running state then abort.
    if ( (oosState == QtTools.OOS_STATE_RUNNING) ||
	 (oosState == QtTools.OOS_STATE_PAUSED))  {
      logger.error(myInst +" is Running or Paused. Sequence Console needs to be stopped first. Aborting Execution.");
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
	    
    //if ( true ) {
    logger.debug("Sending the oosTest");

    String[] oosActiveCmd = {"/jac_sw/omp/QT/bin/oosTest", " -instrument", " OOS_"+myInst, " -active"};

    if ( QtTools.oosTest(oosActiveCmd) == QtTools.OOS_INACTIVE ) {
      logger.debug("not running... send oosLoad");
    }
    else {
      logger.debug("instrument running ... loadORAC deferred!");

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
    //}
	  
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
      if (QtTools.loadDramaTasks(inst.type().getReadable()) == 0) {
	failure.delete();
      }
      else {
	success.delete();
      }
    }
    */
	
  }

}
