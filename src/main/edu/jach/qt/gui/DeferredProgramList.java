package edu.jach.qt.gui;

/* System imports */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.*;

import org.apache.log4j.Logger;

/* Gemini imports */
import gemini.sp.*;
import gemini.sp.obsComp.*;

/* ORAC imports */
import orac.jcmt.inst.*;
import orac.jcmt.iter.*;
import orac.jcmt.obsComp.*;
import orac.ukirt.inst.*;
import orac.ukirt.iter.*;
import orac.util.*;

/* ORAC-OM imports */
import om.console.*;
import om.util.*;

/* QT imports */
import edu.jach.qt.utils.*;

/* Miscellaneous imports */
import ocs.utils.*;

/**
 * Implements the Deferred List functionality.
 * @author $Author$
 * @version $Id$
 */
final public class DeferredProgramList extends JPanel implements
    DropTargetListener,
    DragSourceListener,
    DragGestureListener
{
    private DropTarget                  dropTarget=null;
    private DragSource                  dragSource=null;
    private static JList		obsList;
    private GridBagConstraints		gbc;
    private String                      deferredFileName;
    private JScrollPane			scrollPane = new JScrollPane();
    private DefaultListModel		model;
    public  static SpItem               currentItem;
    private static HashMap              fileToObjectMap = new HashMap();

    static Logger logger = Logger.getLogger(DeferredProgramList.class);


    /**
     * Constructor.
     * Creates the Interface and makes it a drop target.
     */
    public DeferredProgramList()
    {
	Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
	setBorder(new TitledBorder(border, 
				   "Deferred Science Program (SP)", 
				   0, 0, 
				   new Font("Roman",Font.BOLD,12),
				   Color.black));
	setLayout(new BorderLayout() );

	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	gbc = new GridBagConstraints();

	model = new DefaultListModel();
	currentItem = null;

	dropTarget=new DropTarget();
	try
	{
	    dropTarget.addDropTargetListener(this);
	}
	catch(TooManyListenersException tmle)
	{
	    logger.error("Too many drop target listeners", tmle);
	}
	dragSource = new DragSource();

	// Set up the initial drop target
	scrollPane.getViewport().setDropTarget(dropTarget);
	getCurrentList();
	displayList();
    }

    /**
     * Gets the current set of deferred observations.
     * These observations are stored on disk, and the directory is read looking for
     * suitable observations.
     */
    public void getCurrentList()
    {
	

	// Assume we save deferred obs in <diferred file dir>/<telescope>
	String [] deferredFiles = getFileList();

	// parse through the xml files and add each one to the current list
	for (int fileCounter=0;fileCounter<deferredFiles.length; fileCounter++) {
	    // Only look for files which are readable and have a .xml suffix
	    String currentFileName = deferredFiles[fileCounter];
	    File currentFile = new File(currentFileName);
	    if (currentFile.canRead()     &&  // Make sure we can read it
		currentFile.isFile()      &&  // Dont bother with subdirs
		currentFile.length() > 0  &&  // Make sure its worth bothering with
		!(currentFile.isHidden()) &&  // Make sure it is not a hidden file
		currentFileName.endsWith(".xml") ) { // Make sure it is an XML file
		    
		try {
		    FileReader reader = new FileReader(currentFile);
		    char [] buffer    = new char [(int)currentFile.length()];
		    reader.read(buffer);
		    SpItem currentItem = (new SpInputXML()).xmlToSpItem(String.valueOf(buffer));
		    if (currentItem != null) {
			fileToObjectMap.put( currentItem, currentFileName );
			addElement(currentItem);
		    }
		}
		catch (FileNotFoundException fnf) {
		    logger.error("File not found!", fnf);
		}
		catch (IOException ioe) {
		    logger.error("File read error!", ioe);
		}
		catch (Exception x) {
		    logger.error("Can not convert file!", x);
		}
	    }
	}
	return;
    }
    
    /**
     * Add a new observation to the deferred list.
     * @param obs  The observation to add.
     */
    public void addElement(SpItem obs)
    {
	model.addElement(obs);
	displayList();
    }

    /**
     * Add a calibration observation from the Calibrations Menu to the list.
     * @parsm cal  The calibration observation to add.
     */
    public static void addCalibration (SpItem cal) {
	cal.getTable().set("project", "CAL");
	cal.getTable().set("msbid", "CAL");
	// A calibration observation is an SpProg - need to convert to an SpObs
	Vector theseObs = SpTreeMan.findAllItems(cal, "gemini.sp.SpObs");
	SpItem thisObs = (SpItem)theseObs.firstElement();
	thisObs.getTable().set("project", "CAL");
	thisObs.getTable().set("msbid", "CAL");
	if (!isDuplicate(thisObs)) {
	    makePersistent(thisObs);
	    ((DefaultListModel)obsList.getModel()).addElement(thisObs);
	}
    }

    /**
     * Deselects the currently selected deferred observation.
     */
    public static void clearSelection() {
	obsList.clearSelection();
	currentItem = null;
    }

    /**
     * Display the current list of deferred observations.
     */
    public void displayList()
    {
	obsList = new JList (model);
	obsList.setCellRenderer(new ObsListCellRenderer());

	/* Make this list and drag source and drop target */
	obsList.setDropTarget(dropTarget);
	dragSource.createDefaultDragGestureRecognizer ( obsList,
							DnDConstants.ACTION_MOVE,
							this
							);

	MouseListener ml = new MouseAdapter()
	    {
		public void mouseClicked(MouseEvent e)
		{
		    if (e.getClickCount() == 2)
			{
			    execute();
			}
		    else {
			if (currentItem != obsList.getSelectedValue() ) {
			    // Select the new item
			    currentItem = (SpItem) obsList.getSelectedValue();
			    ProgramTree.clearSelection();
			}
			else {
			    obsList.clearSelection();
			    currentItem = null;
			}
		    }
		}
	    };
	obsList.addMouseListener(ml);
	// Add the listbox to a scrolling pane
	scrollPane.getViewport().removeAll();
	scrollPane.getViewport().add(obsList);
	scrollPane.getViewport().setOpaque(true);
	
	gbc.fill = GridBagConstraints.BOTH;
	//gbc.anchor = GridBagConstraints.EAST;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 5;
	gbc.weightx = 100;
	gbc.weighty = 100;
	add(scrollPane, gbc, 0, 0, 2, 1);
	
    }

    /**
     * Append a new observation to the current list.
     * @param obs  The observation to append.
     */
    public void appendItem(SpItem obs)
    {
	obs.getTable().set("project", "CAL");
	obs.getTable().set("msbid", "CAL");
	obs = makeNewObs(obs);
	if (isDuplicate(obs) == false) {
	    makePersistent(obs);
	    addElement(obs);
	}
    }

    private SpItem makeNewObs(SpItem current)
    {
	// The is a fix to get over historical problems with XML
	// What we need to do is write the current observation to a 
	// temporary file and then reread it to get it into the correct 
	// format.
	// Create a temporary file
	SpItem newItem = current;
	try {
	    File tmpFile = File.createTempFile("Observation", ".xml");
	    FileWriter fw = new FileWriter(tmpFile);
	    fw.write(current.toXML());
	    fw.close();

	    FileReader fr = new FileReader(tmpFile);
	    char [] fileContents = new char [(int)tmpFile.length()];
	    fr.read(fileContents);
	    fr.close();
	    tmpFile.delete();

	    String xmlString = String.valueOf(fileContents);
	    newItem = (new SpInputXML()).xmlToSpItem(xmlString);
	}
	catch (IOException ioe) {return current;}
	catch (Exception e) {return current;}

	return newItem;
	
	
    }

    /**
     * Check whether the current observation is already in the deferred list.
     * @param obs  The observation to compare.
     * @return     <code>true</code> is the observation already exists; <code>false</code> otherwise.
     */
    public static boolean isDuplicate (SpItem obs)
    {
	boolean isDuplicate=false;
	obs.getTable().set("project", "CAL");
	obs.getTable().set("msbid", "CAL");
	String currentObs = obs.toXML();
	for (int i=0; i<((DefaultListModel)obsList.getModel()).size();i++) {
	    String thisObs = ((SpItem)(((DefaultListModel)obsList.getModel()).elementAt(i))).toXML();
	    if (thisObs.equals(currentObs)){
		isDuplicate=true;
		break;
	    }
	}
	return isDuplicate;
    }



    private void execute() {
    }


    /**
     * Add a compnent to the <code>GridBagConstraints</code>
     *
     * @param c a <code>Component</code> value
     * @param gbc a <code>GridBagConstraints</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     */
    public void add(Component c, GridBagConstraints gbc, 
		    int x, int y, int w, int h) {
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = w;
	gbc.gridheight = h;
	add(c, gbc);      
    }

    private static void makePersistent(SpItem item)
    {
	// Stores this observation in it own unique file in the deferred directory
	// First make the filename
	String fName = 
	    File.separator +
	    System.getProperty("telescope") + "data" +
	    File.separator +
	    System.getProperty("deferredDir") +
	    File.separator +
	    makeFilenameForThisItem(item);

	fName = fName.toLowerCase();
	

	// Now create a new file write to write to this file
	try {
	    FileWriter fw = new FileWriter(fName);
	    fw.write(item.toXML());
	    fw.flush();
	    fw.close();
	}
	catch (IOException ioe) {
	    logger.error("Error writing file " + fName, ioe);
	}
	fileToObjectMap.put(item, fName);
    }

    private static String makeFilenameForThisItem(SpItem item)
    {
	String fName = new String();
	Date now = new Date();
// 	String title = item.getTitle();
// 	StringTokenizer st = new StringTokenizer(title, " /\());
// 	while (st.hasMoreTokens()) {
// 	    fName = fName+st.nextToken();
// 	}
	fName = now.getTime() + ".xml";
	return fName;
    }

    /**
     * Check to see whether any deferre observations exist.
     * @return <code>true</code> if deferred observations exist; <code>false> otherwise.
     */
    public static boolean deferredFilesExist()
    {
	boolean filesExist=true;

	switch (getNumberOfDeferredItems()) {
	case 0:
	    filesExist = false;
	    break;
	default:
	    break;
	}
	return filesExist;
    }

    /**
     * Get the number of deferred observations.
     * @return  The number of deferred observations.
     */
    private static int getNumberOfDeferredItems()
    {
	return fileToObjectMap.size();
    }

    /**
     * Checks whether any deferred observation files exist.
     * These do nat have to have been read into the current
     * list of displayed observations.
     * @return <code>true</code> if deferred observatio files exist; <code>false</code> otherwise.
     */
    public static boolean obsExist()
    {
	boolean exists=false;

	String[] deferredFiles = getFileList();
	if (deferredFiles.length != 0) {
	    for (int fileCounter=0;fileCounter<deferredFiles.length; fileCounter++) {
		if (deferredFiles[fileCounter].endsWith(".xml")) {
		    exists=true;
		    break;
		}
	    }
	}
	return exists;
    }

    private static String[] getFileList()
    {
	// Assume we save deferred obs in <diferred file dir>/<telescope>
	String deferredFiles [] = {};
	String deferredDirName = 
	    File.separator +
	    System.getProperty("telescope") + "data" +
	    File.separator +
	    System.getProperty("deferredDir");
	deferredDirName = deferredDirName.toLowerCase();
	
	File deferredDir = new File (deferredDirName);
	if (! deferredDir.exists() ) {
	    // Try to create the directory
	    logger.info ("Creating deferred directory " + deferredDirName);
	    deferredDir.mkdir();
	    return deferredFiles;
	}
	else if (deferredDir.canRead() && deferredDir.isDirectory() ) {
	    deferredFiles = deferredDir.list();
	}
	for (int i=0;i<deferredFiles.length; i++) {
	    deferredFiles[i] = deferredDirName+File.separator+deferredFiles[i];
	}
	return deferredFiles;

    }

    /**
     * Delete all deferred observation files and clear the contents of the
     * Deferred Program List.
     */
    public static void deleteAllFiles()
    {
	// Does not assume a current hashmap exists - get
	// the directory and trawl through all the .xml files
	String [] deferredFiles = getFileList();
	if (deferredFiles.length != 0) {
	    for (int fileCounter=0;fileCounter<deferredFiles.length; fileCounter++) {
		String currentFileName=deferredFiles[fileCounter];
		if (currentFileName.endsWith(".xml")) {
		    File currentFile = new File(currentFileName);
		    currentFile.delete();
		}
	    }
	}
	// Now clear the hashmap, just for completeness
	if (!fileToObjectMap.isEmpty()) {
	    fileToObjectMap.clear();
	}
	return;
    }

    /**
     * Mark an observation as done.
     * This is done by timestamping the title.
     */
    public static void markThisObservationAsDone (SpItem thisObservation)
    {
	// Add a dat-time stamp to the title
	String currentTitle = thisObservation.getTitle();
	StringTokenizer st = new StringTokenizer(currentTitle, "_");
	String baseName = "";
	while (st.hasMoreTokens()) {
	    String subStr = st.nextToken();
	    if (subStr.equals("done")) break;
	    baseName = baseName + subStr;
	}
	currentTitle = baseName;
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");	
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	String observationTime = df.format(cal.getTime());
	String newTitle = currentTitle + "_done_" + observationTime;
	thisObservation.setTitleAttr(newTitle);

	// Delete the old entry and replace by the current
	String fileToRemove = (String) fileToObjectMap.get(obsList.getSelectedValue());
	File f = new File (fileToRemove);
	f.delete();
	fileToObjectMap.remove(obsList.getSelectedValue());
	((DefaultListModel)obsList.getModel()).removeElementAt(obsList.getSelectedIndex());
	currentItem = null;

	makePersistent(thisObservation);

	((DefaultListModel)obsList.getModel()).addElement(thisObservation);
    }


    /*
     * Add the event listeners
     */
    /* DROP TARGET EVENTS */
    /**
     * Implementation of <code>DropTargetListener</code> interface.
     * @param evt A <code>DropTargetDragEvent</code> object.
     */
    public void dragEnter(DropTargetDragEvent evt)
    {
    }

    /**
     * Implementation of <code>DropTargetListener</code> interface.
     * @param evt A <code>DropTargetEvent</code> object.
     */
    public void dragExit(DropTargetEvent evt)
    {
    }

    /**
     * Implementation of <code>DropTargetListener</code> interface.
     * @param evt A <code>DropTargetDragEvent</code> object.
     */
    public void dragOver(DropTargetDragEvent evt)
    {
    }

    /**
     * Implementation of <code>DropTargetListener</code> interface.
     * @param evt A <code>DropTargetDragEvent</code> object.
     */
    public void dropActionChanged(DropTargetDragEvent evt)
    {
    }

    /**
     * Implementation of <code>DropTargetListener</code> interface.
     * Adds the currently selected item from the Program List to
     * the deferred list.
     * @param evt A <code>DropTargetDropEvent</code> object.
     */
    public void drop (DropTargetDropEvent evt)
    {
	if (this.dropTarget.isActive()) {
	    Transferable t = evt.getTransferable();
	    evt.acceptDrop(DnDConstants.ACTION_MOVE);
	    SpItem thisObs = ProgramTree.obsToDefer;
	    if (((SpObs)thisObs).isOptional()) {
		appendItem(thisObs);
		evt.getDropTargetContext().dropComplete(true);
	    }
	    else {
		JOptionPane.showMessageDialog(null,
					      "Can not defer mandatory observations!"
					      );
		evt.getDropTargetContext().dropComplete(false);
	    }
	}
	else {
	    evt.rejectDrop();
	    evt.dropComplete(false);
	}
    }

    /**
     * Deletes an observation from the current list.
     * @param thisObservation  The observation to remove.
     */
    public static void removeThisObservation (SpItem thisObservation)
    {
	String fileToRemove = (String) fileToObjectMap.get(obsList.getSelectedValue());
	// Delete the file
	File f = new File (fileToRemove);
	f.delete();
	
	// Delete the entry from the map
	fileToObjectMap.remove(obsList.getSelectedValue());
	
	// Remove the entry from the list
	((DefaultListModel)obsList.getModel()).removeElementAt(obsList.getSelectedIndex());
    }


    /* DRAG SOURCE EVENTS */
    /**
     * Implementation of the <code>DragSourceListener</code> interface.
     * Removes the entry from the list on successful drop.
     * @param evt  A <code>DragSourceDropEvent</code> object.
     */
    public void dragDropEnd (DragSourceDropEvent evt)
    {
	if (evt.getDropSuccess() ) {
	    String fileToRemove = (String) fileToObjectMap.get(obsList.getSelectedValue());
	    // Delete the file
	    File f = new File (fileToRemove);
	    f.delete();

	    // Delete the entry from the map
	    fileToObjectMap.remove(obsList.getSelectedValue());

	    // Remove the entry from the list
	    ((DefaultListModel)obsList.getModel()).removeElementAt(obsList.getSelectedIndex());

	    currentItem = null;
	}
	this.dropTarget.setActive(true);
    }

    /**
     * Implementation of the <code>DragSourceListener</code> interface.
     * @param evt  A <code>DragSourceDragEvent</code> object.
     */
    public void dragEnter (DragSourceDragEvent evt)
    {
    }

    /**
     * Implementation of the <code>DragSourceListener</code> interface.
     * @param evt  A <code>DragSourceEvent</code> object.
     */
    public void dragExit(DragSourceEvent evt)
    {
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    /**
     * Implementation of the <code>DragSourceListener</code> interface.
     * @param evt  A <code>DragSourceDragEvent</code> object.
     */
    public void dragOver(DragSourceDragEvent evt)
    {
	evt.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    /**
     * Implementation of the <code>DragSourceListener</code> interface.
     * @param evt  A <code>DragSourceDragEvent</code> object.
     */
    public void dropActionChanged(DragSourceDragEvent evt)
    {
    }

    /**
     * Implementation of the <code>DragGestureListener</code> interface.
     * Disables the current list from being a drop target temporarily so that
     * we cant drop items from the list back on to the list.
     *
     * @param event  A <code>DragGestureEvent</code> object.
     */
    public void dragGestureRecognized( DragGestureEvent event) 
    {
	SpItem selected = (SpItem) obsList.getSelectedValue();
	if (selected != null) {
	    StringSelection text = new StringSelection( selected.toString());
	    // Disable dropping on this window
	    this.dropTarget.setActive(false);
	    dragSource.startDrag(event,
				 DragSource.DefaultMoveNoDrop,
				 text,
				 this
				 );
	}
    }

}
