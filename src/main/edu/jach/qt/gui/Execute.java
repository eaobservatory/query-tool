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
import javax.swing.*;


/**
 * This class is a base class and should be extended to execute MSBs for
 * each telescope.  It basically is used to decide whether the project
 * being executed comes from the deferred or project list.
 * @author $Author$
 * @version $Id$
 */
public class Execute {

    /**
     * Indicates whether a observation is from the deferred list or the
     * project list.
     */
    protected boolean isDeferred = false;

    /**
     * Default constructor.
     * @throws Exception   When no or multiple items selected.
     */
    protected Execute () throws Exception {
	if (ProgramTree.selectedItem == null &&
	    DeferredProgramList.currentItem == null) {
	    JOptionPane.showMessageDialog(null,
					  "You have not selected an observation!",
					  "Please select an observation.",
					  JOptionPane.ERROR_MESSAGE);
	    throw new Exception("No Item Selected");	    
	}
	else if (ProgramTree.selectedItem != null &&
		 DeferredProgramList.currentItem != null) {
	    JOptionPane.showMessageDialog(null,
					  "You may only select one observation!",
					  "Please deselect an observation.",
					  JOptionPane.ERROR_MESSAGE);
	    throw new Exception("Multiple Items Selected");	    
	}
	else if (DeferredProgramList.currentItem  != null) {
	    isDeferred = true;
	}
    }
}
