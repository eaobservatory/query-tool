package edu.jach.qt.gui;

/* System imports */
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.DefaultListModel;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Vector;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import java.util.HashSet;

/* Gemini imports */
import gemini.sp.SpItem;
import gemini.sp.SpTreeMan;
import gemini.sp.SpObs;
import gemini.sp.SpInsertData;
import gemini.sp.SpProg;
import gemini.sp.SpFactory;
import gemini.sp.SpType;
import gemini.sp.obsComp.SpInstObsComp;

/* ORAC imports */
import orac.util.SpInputXML;

/* ORAC-OM imports */

/* QT imports */
import edu.jach.qt.utils.ObsListCellRenderer;
import edu.jach.qt.utils.ErrorBox;
import edu.jach.qt.utils.TimeUtils ;
import edu.jach.qt.utils.FileExtensionFilter ;

/* Miscellaneous imports */

/**
 * Implements the Deferred List functionality.
 * @author $Author$
 * @version $Id$
 */
final public class DeferredProgramList extends JPanel implements DropTargetListener , DragSourceListener , DragGestureListener , ActionListener
{
	private DropTarget dropTarget = null;
	private DragSource dragSource = null;
	private static JList obsList;
	private GridBagConstraints gbc;
	private JScrollPane scrollPane = new JScrollPane();
	private DefaultListModel model;
	private static SpItem currentItem;
	private static HashMap fileToObjectMap = new HashMap();
	private JPopupMenu engMenu = new JPopupMenu();
	private JMenuItem engItem = new JMenuItem( "Send for Engineering" );
	private boolean _useQueue = true;
	private static HashSet duplicates = new HashSet();
	static Logger logger = Logger.getLogger( DeferredProgramList.class );
	
	private static String knownUTCDate ;
	private static String deferredDirName ;
	private static String todaysDeferredDirName ;
	private static File deferredDir ;
	private static File todaysDeferredDir ;
	private static final FileExtensionFilter xmlFilter = new FileExtensionFilter( ".xml" ) ;

	/**
	 * Constructor.
	 * Creates the Interface and makes it a drop target.
	 */
	public DeferredProgramList()
	{
		Border border = BorderFactory.createMatteBorder( 2 , 2 , 2 , 2 , Color.white );
		setBorder( new TitledBorder( border , "Deferred MSBs" , 0 , 0 , new Font( "Roman" , Font.BOLD , 12 ) , Color.black ) );
		setLayout( new BorderLayout() );

		GridBagLayout gbl = new GridBagLayout();
		setLayout( gbl );
		gbc = new GridBagConstraints();

		model = new DefaultListModel();
		currentItem = null;

		dropTarget = new DropTarget();
		try
		{
			dropTarget.addDropTargetListener( this );
		}
		catch( TooManyListenersException tmle )
		{
			logger.error( "Too many drop target listeners" , tmle );
		}
		dragSource = new DragSource();

		engMenu.add( engItem );
		engItem.addActionListener( this );

		// Set up the initial drop target
		scrollPane.getViewport().setDropTarget( dropTarget );
		getCurrentList();
		displayList();
	}

	public static synchronized SpItem getCurrentItem()
	{
		return currentItem;
	}

	/**
	 * Updates the display of the deferred observations.
	 */
	public void reload()
	{
		fileToObjectMap.clear();
		model.clear();
		getCurrentList();
		displayList();
	}

	/**
	 * Gets the current set of deferred observations. These observations are stored on disk, and the directory is read looking for suitable observations.
	 */
	public void getCurrentList()
	{
		// Assume we save deferred obs in <diferred file dir>/<telescope>
		String[] deferredFiles = getFileList();

		// parse through the xml files and add each one to the current list
		for( int fileCounter = 0 ; fileCounter < deferredFiles.length ; fileCounter++ )
		{
			// Only look for files which are readable
			String currentFileName = deferredFiles[ fileCounter ];
			File currentFile = new File( currentFileName );
			
			if( currentFile.canRead() && // Make sure we can read it
			currentFile.isFile() && // Dont bother with subdirs
			currentFile.length() > 0 && // Make sure its worth bothering with
			!currentFile.isHidden() ) // Make sure it is not a hidden file
			{
				try
				{
					FileReader reader = new FileReader( currentFile );
					char[] chars = new char[ 1024 ];
					int readLength = 0 ;
					StringBuffer buffer = new StringBuffer() ;
					while( !reader.ready() )
						;
					while( ( readLength = reader.read( chars ) ) != -1 )
						buffer.append( chars , 0 , readLength ) ;
					reader.close();
					SpItem currentSpItem = ( new SpInputXML() ).xmlToSpItem( buffer.toString() );
					if( currentSpItem != null )
					{
						fileToObjectMap.put( currentSpItem , currentFileName );
						addElement( currentSpItem );
						NotePanel.setNote( currentSpItem );
					}
				}
				catch( FileNotFoundException fnf )
				{
					logger.error( "File not found!" , fnf );
				}
				catch( IOException ioe )
				{
					logger.error( "File read error!" , ioe );
				}
				catch( Exception x )
				{
					logger.error( "Can not convert file!" , x );
				}
			}
		}
	}

	/**
	 * Add a new observation to the deferred list.
	 * @param obs  The observation to add.
	 */
	public void addElement( SpItem obs )
	{
		model.addElement( obs );
		displayList();
	}

	/**
	 * Add a calibration observation from the Calibrations Menu to the list.
	 * 
	 * @param cal
	 *            The calibration observation to add.
	 */
	public static void addCalibration( SpItem cal )
	{
		cal.getTable().set( "project" , "CAL" );
		cal.getTable().set( "msbid" , "CAL" );
		cal.getTable().set( ":msb" , "true" );
		// A calibration observation is an SpProg - need to convert to an SpObs
		Vector theseObs = SpTreeMan.findAllItems( cal , "gemini.sp.SpObs" );
		SpItem thisObs = ( SpItem )theseObs.firstElement();
		if( thisObs != cal )
		{
			thisObs.getTable().set( "project" , "CAL" );
			thisObs.getTable().set( "msbid" , "CAL" );
			thisObs.getTable().set( ":msb" , "true" );

			if( !duplicates.contains( thisObs ) )
			{
				SpInstObsComp inst = SpTreeMan.findInstrument( thisObs );
				SpInsertData insertable = SpTreeMan.evalInsertInside( inst , thisObs );
				if( insertable != null )
					SpTreeMan.insert( insertable );
				duplicates.add( thisObs );
			}

			if( thisObs.getTitleAttr().equals( "Observation" ) )
				thisObs.setTitleAttr( cal.getTitleAttr() );
		}

		( ( SpObs )thisObs ).setOptional( true );

		if( !isDuplicate( thisObs ) )
		{
			makePersistent( thisObs );
			( ( DefaultListModel )obsList.getModel() ).addElement( thisObs );
		}
		// This is a hack to fix fault [20021030.002]. It shouldn't happen but hopefully this will make sure...
		obsList.setEnabled( true );
		obsList.setSelectedIndex( obsList.getModel().getSize() - 1 );
		currentItem = ( SpItem )obsList.getSelectedValue();
		NotePanel.setNote( currentItem );
		ProgramTree.clearSelection();
	}

	/**
	 * Deselects the currently selected deferred observation.
	 */
	public static void clearSelection()
	{
		obsList.clearSelection();
		currentItem = null;
	}

	/**
	 * Display the current list of deferred observations.
	 */
	public void displayList()
	{
		obsList = new JList( model );
		obsList.setCellRenderer( new ObsListCellRenderer() );

		/* Make this list and drag source and drop target */
		obsList.setDropTarget( dropTarget );
		dragSource.createDefaultDragGestureRecognizer( obsList , DnDConstants.ACTION_MOVE , this );

		MouseListener ml = new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() == 2 )
				{
					execute();
				}
				else
				{
					if( currentItem != obsList.getSelectedValue() )
					{
						// Select the new item
						currentItem = ( SpItem )obsList.getSelectedValue();
						NotePanel.setNote( currentItem );
						ProgramTree.clearSelection();
					}
					else
					{
						obsList.clearSelection();
						currentItem = null;
					}
				}
			}

			public void mousePressed( MouseEvent e )
			{
				if( e.isPopupTrigger() && currentItem != null )
					engMenu.show( e.getComponent() , e.getX() , e.getY() );
				else
					ProgramTree.clearSelection();
			}
		};
		obsList.addMouseListener( ml );
		// Add the listbox to a scrolling pane
		scrollPane.getViewport().removeAll();
		scrollPane.getViewport().add( obsList );
		scrollPane.getViewport().setOpaque( true );

		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets.bottom = 5;
		gbc.insets.left = 10;
		gbc.insets.right = 5;
		gbc.weightx = 100;
		gbc.weighty = 100;
		add( scrollPane , gbc , 0 , 0 , 2 , 1 );
	}

	/**
	 * Append a new observation to the current list.
	 * 
	 * @param obs
	 *            The observation to append.
	 */
	public void appendItem( SpItem obs )
	{
		obs.getTable().set( "project" , "CAL" );
		obs.getTable().set( "msbid" , "CAL" );
		obs.getTable().set( ":msb" , "true" );
		obs = makeNewObs( obs );
		if( isDuplicate( obs ) == false )
		{
			makePersistent( obs );
			addElement( obs );
		}
	}

	private static SpItem makeNewObs( SpItem current )
	{
		// The is a fix to get over historical problems with XML
		SpItem newItem = current;
		try
		{
			String xmlString = current.toXML();
			newItem = ( new SpInputXML() ).xmlToSpItem( xmlString );
		}
		catch( Exception e )
		{
			return current;
		}
		return newItem;
	}

	/**
	 * Check whether the current observation is already in the deferred list.
	 * 
	 * @param obs
	 *            The observation to compare.
	 * @return <code>true</code> is the observation already exists; <code>false</code> otherwise.
	 */
	public static boolean isDuplicate( SpItem obs )
	{
		boolean isDuplicate = false;

		String currentObsXML = obs.toXML();
		for( int i = 0 ; i < ( ( DefaultListModel )obsList.getModel() ).size() ; i++ )
		{
			SpItem thisObs = ( SpItem )(( DefaultListModel )obsList.getModel()).elementAt( i ) ;
			String thisObsXML = thisObs.toXML();
			if( thisObsXML.equals( currentObsXML ) )
			{
				isDuplicate = true;
				break;
			}
		}
		return isDuplicate;
	}

	/*
	 * This more or less mimics the doExecute() method the ProgramTree.java. Its real purpose is to let users do calibrations even if the "Send for Execution" button is disabled.
	 */
	private void execute()
	{
		Thread t = null;

		// If we have nothing selected. don't bother doing anything
		if( currentItem == null )
			return;

		// If we have items selected on both the ProgramList and Deferred List the let the execute method handle the problem.

		// Call the appropriate Execute method
		if( System.getProperty( "telescope" ).equalsIgnoreCase( "ukirt" ) )
		{
			try
			{
				logger.info( "Sending observation " + currentItem.getTitle() + " for execution." );
				ExecuteUKIRT execute = new ExecuteUKIRT( _useQueue );
				execute.setDeferred( true );
				t = new Thread( execute , "UKIRT Execution Thread" );

				// Start the process and wait for it to complete
				t.start();
				t.join();
				// Reset _useQueue
				_useQueue = true;
				// Now check the result
				File failFile = execute.failFile() ;
				File successFile = execute.successFile() ;
				if( failFile.exists() )
				{
					new ErrorBox( this , "Failed to Execute. Check messages." );
					logger.warn( "Failed to execute observation" );
				}
				else if( successFile.exists() )
				{
					// Mark this observation as having been done
					markThisObservationAsDone( currentItem );
					logger.info( "Observation executed successfully" );
				}
				else
				{
					// Neither file exists - report an error to the user
					new ErrorBox( "Unable to determine success status - assuming failed." );
					logger.error( "Unable to determine success status for observation." );
				}
				
				// done with status files
				if( failFile.exists() )
					failFile.delete() ;
				if( successFile.exists() )
					successFile.delete() ;
			}
			catch( Exception e )
			{
				logger.error( "Failed to execute thread." , e );
				if( t != null && t.isAlive() )
				{
					logger.error( "Last observation still seems to be running" );
				}
			}
		}
		else if( System.getProperty( "telescope" ).equalsIgnoreCase( "jcmt" ) )
		{
			new ExecuteInThread( currentItem , true ).start();
		}
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
	public void add( Component c , GridBagConstraints gbc , int x , int y , int w , int h )
	{
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		add( c , gbc );
	}
	
	/**
	 * @return The parent directory name for deferred observations as a String.
	 */
	public static String getDeferredDirectoryName()
	{
		if( deferredDirName == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "telescope" ) ) ;
			buffer.append( "data" ) ;
			buffer.append( File.separator ) ;
			buffer.append( System.getProperty( "deferredDir" ) ) ;
			deferredDirName = buffer.toString() ;
			buffer.delete( 0 , buffer.length() ) ;
			buffer = null ;
			deferredDirName = deferredDirName.toLowerCase() ;
		}
		return deferredDirName ;
	}
	
	/**
	 * @return The directory name of deferred observations for this UT date as a String.
	 */
	public static String getTodaysDeferredDirectoryName()
	{
		if( checkUTCDate() || todaysDeferredDirName == null )
		{
			StringBuffer buffer = new StringBuffer() ;
			buffer.append( getDeferredDirectoryName() ) ;
			buffer.append( File.separator ) ;
			buffer.append( knownUTCDate ) ;
			todaysDeferredDirName = buffer.toString() ;
			buffer.delete( 0 , buffer.length() ) ;
			buffer = null ;
			todaysDeferredDirName = todaysDeferredDirName.toLowerCase() ;
		}
		return todaysDeferredDirName ;
	}
	
	public static File getDeferredDirectory()
	{
		if( deferredDir == null )
			deferredDir = createDirectory( getDeferredDirectoryName() ) ;
		
		return deferredDir ;
	}
	
	public static File getTodaysDeferredDirectory()
	{
		if( checkUTCDate() || todaysDeferredDir == null )
			todaysDeferredDir = createDirectory( getTodaysDeferredDirectoryName() ) ;
		
		return todaysDeferredDir ;
	}
	
	private static File createDirectory( String fileName )
	{
		File dir = new File( fileName ) ;
		if( !dir.exists() )
		{
			logger.info( "Creating deferred directory " + dir ) ;
			if( !dir.mkdirs() )
			{
				logger.error( "Could not create directory " + dir ) ;
				dir = null ;
			}
		}
		else if( !dir.canRead() )
		{
			logger.error( "Unable to read deferred directory " + dir ) ;
			dir = null ;
		}
		else if( !dir.isDirectory() )
		{
			logger.error( dir + " is not a directory, deleting." ) ;
			dir.delete() ;
			dir = null ;
		}
		return dir ;
	}
	
	// check wether UTC date has changed
	private static boolean checkUTCDate()
	{
		boolean changed = true ;
		String UTCDate = TimeUtils.getUTCDate() ;
		if( knownUTCDate == null  )
			knownUTCDate = UTCDate ;
		else if( knownUTCDate.equals( UTCDate ) )
			changed = false ;
		return changed ;
	}
	
	private static void makePersistent( SpItem item )
	{
		// Stores this observation in it own unique file in the deferred directory. First make the filename
		String fName = makeFilenameForThisItem( item ) ;
		FileWriter fw = null;
		// Now create a new file write to write to this file
		try
		{
			fw = new FileWriter( fName );
			fw.write( item.toXML() );
			fw.flush();
		}
		catch( IOException ioe )
		{
			logger.error( "Error writing file " + fName , ioe );
		}
		finally
		{
			if( fw != null )
			{
				try
				{
					fw.close();
				}
				catch( IOException ioe ){}
			}
		}
		fileToObjectMap.put( item , fName );
	}

	private static String makeFilenameForThisItem( SpItem item )
	{
		StringBuffer buffer = new StringBuffer() ;
		buffer.append( getTodaysDeferredDirectoryName() ) ;
		buffer.append( File.separator ) ;
		Date now = new Date();
		buffer.append( now.getTime() ) ;
		buffer.append( ".xml" ) ;
		String fName = buffer.toString() ;
		buffer.delete( 0 , buffer.length() ) ;
		buffer = null ;
		return fName.toLowerCase() ; // unnecessary 
	}

	/**
	 * Checks whether any deferred observation files exist.
	 * These do not have to have been read into the current
	 * list of displayed observations.
	 * @return <code>true</code> if deferred observatio files exist; <code>false</code> otherwise.
	 */
	public static boolean obsExist()
	{
		return getFileList().length != 0 ;
	}

	private static String[] getFileList()
	{
		String deferredFiles[] = {};
		File deferredDir = getTodaysDeferredDirectory() ;

		deferredFiles = deferredDir.list( xmlFilter ) ;
		
		String currentFileName ;
		
		for( int i = 0 ; i < deferredFiles.length ; i++ )
		{
			currentFileName = deferredFiles[ i ] ;
			if( !currentFileName.contains( File.separator ) )
				deferredFiles[ i ] = todaysDeferredDirName + File.separator + currentFileName ;
		}

		return deferredFiles;
	}

	/**
	 * Delete all deferred observation files and clear the contents of the Deferred Program List.
	 */
	public static void cleanup()
	{		
		File deferredParent = getDeferredDirectory() ;
		File deferred = getTodaysDeferredDirectory() ;
		
		File[] files = deferredParent.listFiles() ;
		for( int index = 0 ; index < files.length ; index++ )
		{
			File file = files[ index ] ;
			if( !delete( file , deferred ) )
				logger.error( "Could not delete " + file.getName() ) ;
		}
		
		// Now clear the hashmap, just for completeness
		if( !fileToObjectMap.isEmpty() )
			fileToObjectMap.clear();
	}
	
	private static boolean delete( File file , File skip )
	{
		boolean success = true ;
		
		if( !file.equals( skip ) )
		{
			if( file.isDirectory() )
			{
				File[] files = file.listFiles() ;
				for( int index = 0 ; index < files.length ; index++ )
					delete( files[ index ] , skip ) ;
			}
			
			if( !file.delete() )
			{
				String diagnostic = "" ;
				if( !file.exists() )
					diagnostic = "does not exist" ;
				else if( !file.canWrite() )
					diagnostic = "cannot be written to" ;
				logger.error( "Could not delete " + file.getName() + " " + diagnostic + "." ) ;
				success = false ;
			}
			else
			{
				logger.warn( "Deleted " + file.getName() ) ;
			}
		}
		else
		{
			logger.warn( "Skipping " + file.getName() ) ;
		}
		
		return success ;
	}

	/**
	 * Mark an observation as done. This is done by timestamping the title.
	 */
	public static void markThisObservationAsDone( SpItem thisObservation )
	{
		// Add a dat-time stamp to the title
		String currentTitle = thisObservation.getTitle();
		String[] split = currentTitle.split( "_" );
		String baseName = "";
		for( int index = 0 ; index < split.length ; index++ )
		{
			String subStr = split[ index ];
			if( subStr.equals( "done" ) )
				break;
			baseName += subStr;
		}
		currentTitle = baseName;
		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd'T'HHmmss" );
		Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
		String observationTime = df.format( cal.getTime() );
		String newTitle = currentTitle + "_done_" + observationTime;
		thisObservation.setTitleAttr( newTitle );

		// Delete the old entry and replace by the current
		String fileToRemove = ( String )fileToObjectMap.get( obsList.getSelectedValue() );
		File f = new File( fileToRemove );
		f.delete();
		fileToObjectMap.remove( obsList.getSelectedValue() );
		int index = obsList.getSelectedIndex();
		if( index > -1 )
			( ( DefaultListModel )obsList.getModel() ).removeElementAt( index );
		currentItem = null;

		makePersistent( thisObservation );

		(( DefaultListModel )obsList.getModel()).addElement( thisObservation );
	}

	/*
	 * Add the event listeners
	 */
	/* DROP TARGET EVENTS */
	/**
	 * Implementation of <code>DropTargetListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DropTargetDragEvent</code> object.
	 */
	public void dragEnter( DropTargetDragEvent evt ){}

	/**
	 * Implementation of <code>DropTargetListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DropTargetEvent</code> object.
	 */
	public void dragExit( DropTargetEvent evt ){}

	/**
	 * Implementation of <code>DropTargetListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DropTargetDragEvent</code> object.
	 */
	public void dragOver( DropTargetDragEvent evt ){}

	/**
	 * Implementation of <code>DropTargetListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DropTargetDragEvent</code> object.
	 */
	public void dropActionChanged( DropTargetDragEvent evt ){}

	/**
	 * Implementation of <code>DropTargetListener</code> interface. Adds the currently selected item from the Program List to the deferred list.
	 * 
	 * @param evt
	 *            A <code>DropTargetDropEvent</code> object.
	 */
	public void drop( DropTargetDropEvent evt )
	{
		if( this.dropTarget.isActive() )
		{
			evt.acceptDrop( DnDConstants.ACTION_MOVE );
			SpItem thisObs = ProgramTree.obsToDefer;
			if( (( SpObs )thisObs).isOptional() )
			{
				appendItem( thisObs );
				evt.getDropTargetContext().dropComplete( true );
			}
			else
			{
				JOptionPane.showMessageDialog( this , "Can not defer mandatory observations!" );
				evt.getDropTargetContext().dropComplete( false );
			}
		}
		else
		{
			evt.rejectDrop();
			evt.dropComplete( false );
		}
	}

	/**
	 * Deletes an observation from the current list.
	 * 
	 * @param thisObservation
	 *            The observation to remove.
	 */
	public static void removeThisObservation( SpItem thisObservation )
	{
		String fileToRemove = ( String )fileToObjectMap.get( obsList.getSelectedValue() );
		// Delete the file
		File f = new File( fileToRemove );
		f.delete();

		// Delete the entry from the map
		fileToObjectMap.remove( obsList.getSelectedValue() );

		// Remove the entry from the list
		int index = obsList.getSelectedIndex();
		if( index > -1 )
			( ( DefaultListModel )obsList.getModel() ).removeElementAt( index );
	}

	/* DRAG SOURCE EVENTS */
	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * Removes the entry from the list on successful drop.
	 * @param evt  A <code>DragSourceDropEvent</code> object.
	 */
	public void dragDropEnd( DragSourceDropEvent evt )
	{
		if( evt.getDropSuccess() )
		{
			String fileToRemove = ( String )fileToObjectMap.get( obsList.getSelectedValue() );
			// Delete the file
			File f = new File( fileToRemove );
			f.delete();

			// Delete the entry from the map
			fileToObjectMap.remove( obsList.getSelectedValue() );

			// Remove the entry from the list
			int index = obsList.getSelectedIndex();
			if( index > -1 )
				( ( DefaultListModel )obsList.getModel() ).removeElementAt( index );

			currentItem = null;
		}
		obsList.setEnabled( true );
		this.dropTarget.setActive( true );
	}

	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DragSourceDragEvent</code> object.
	 */
	public void dragEnter( DragSourceDragEvent evt ){}

	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DragSourceEvent</code> object.
	 */
	public void dragExit( DragSourceEvent evt )
	{
		evt.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop );
	}

	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * @param evt  A <code>DragSourceDragEvent</code> object.
	 */
	public void dragOver( DragSourceDragEvent evt )
	{
		evt.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop );
	}

	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * 
	 * @param evt
	 *            A <code>DragSourceDragEvent</code> object.
	 */
	public void dropActionChanged( DragSourceDragEvent evt ){}

	/**
	 * Implementation of the <code>DragGestureListener</code> interface. Disables the current list from being a drop target temporarily so that we cant drop items from the list back on to the list.
	 * 
	 * @param event
	 *            A <code>DragGestureEvent</code> object.
	 */
	public void dragGestureRecognized( DragGestureEvent event )
	{
		SpItem selected = ( SpItem )obsList.getSelectedValue();
		ProgramTree.clearSelection();
		obsList.setEnabled( false );
		if( selected != null )
		{
			StringSelection text = new StringSelection( selected.toString() );
			// Disable dropping on this window
			this.dropTarget.setActive( false );
			dragSource.startDrag( event , DragSource.DefaultMoveNoDrop , text , this );
		}
	}

	public void actionPerformed( ActionEvent e )
	{
		if( e.getSource() == engItem )
		{
			_useQueue = false;
			execute();
		}
	}

	public class ExecuteInThread extends Thread
	{
		SpProg _item = ( SpProg )SpFactory.create( SpType.SCIENCE_PROGRAM );

		boolean _isDeferred;

		public ExecuteInThread( SpItem item , boolean deferred )
		{
			// Make the obs into an SpProg
			_item.setPI( "observer" );
			_item.setCountry( "JAC" );
			_item.setTelescope();
			if( _item.getProjectID() == null || _item.getProjectID().equals( "" ) )
				_item.setProjectID( "CAL" );
			_item.setTitleAttr( item.getTitleAttr() );
			SpInsertData spID = SpTreeMan.evalInsertInside( item , _item );
			if( spID != null )
				SpTreeMan.insert( spID );
			_isDeferred = deferred;
		}

		public void run()
		{
			ExecuteJCMT execute = null;
			boolean failed = false;

			execute = ExecuteJCMT.getInstance( _item );
			if( execute == null )
				return;
			execute.setDeferred( _isDeferred );
			failed = execute.run();

			if( failed )
			{
				new ErrorBox( "Failed to Execute. Check messages." );
				logger.warn( "Failed to execute observation" );
			}
			else
			{
				// Mark this observation as having been done
				markThisObservationAsDone( currentItem );
				logger.info( "Observation executed successfully" );
			}
		}
	}
}
