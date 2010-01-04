package edu.jach.qt.gui ;

/* System imports */
import javax.swing.JList ;
import javax.swing.JScrollPane ;
import javax.swing.JPopupMenu ;
import javax.swing.JMenuItem ;
import javax.swing.DefaultListModel ;
import javax.swing.BorderFactory ;
import javax.swing.JPanel ;
import javax.swing.JOptionPane ;
import javax.swing.border.TitledBorder ;
import javax.swing.border.Border ;
import java.awt.GridBagConstraints ;
import java.awt.GridBagLayout ;
import java.awt.Font ;
import java.awt.Color ;
import java.awt.BorderLayout ;
import java.awt.Component ;
import java.awt.dnd.DropTargetListener ;
import java.awt.dnd.DragSourceListener ;
import java.awt.dnd.DragGestureListener ;
import java.awt.dnd.DropTarget ;
import java.awt.dnd.DragSource ;
import java.awt.dnd.DnDConstants ;
import java.awt.dnd.DropTargetDragEvent ;
import java.awt.dnd.DropTargetDropEvent ;
import java.awt.dnd.DragGestureEvent ;
import java.awt.dnd.DragSourceDragEvent ;
import java.awt.dnd.DragSourceDropEvent ;
import java.awt.dnd.DropTargetEvent ;
import java.awt.dnd.DragSourceEvent ;
import java.awt.datatransfer.StringSelection ;
import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.awt.event.MouseListener ;
import java.awt.event.MouseEvent ;
import java.awt.event.MouseAdapter ;
import javax.swing.event.ListSelectionListener ;
import javax.swing.event.ListSelectionEvent ;
import java.util.Vector ;
import java.util.HashMap ;
import java.util.TooManyListenersException ;
import java.util.Date ;
import java.util.Calendar ;
import java.util.TimeZone ;
import java.io.File ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.io.FileNotFoundException ;
import java.io.FileReader ;
import java.text.SimpleDateFormat ;

import edu.jach.qt.utils.JACLogger ;

import java.util.HashSet ;

/* Gemini imports */
import gemini.sp.SpItem ;
import gemini.sp.SpTreeMan ;
import gemini.sp.SpObs ;
import gemini.sp.SpInsertData ;
import gemini.sp.SpProg ;
import gemini.sp.SpFactory ;
import gemini.sp.SpType ;
import gemini.sp.obsComp.SpInstObsComp ;

/* ORAC imports */
import orac.util.SpInputXML ;

/* ORAC-OM imports */

/* QT imports */
import edu.jach.qt.utils.ObsListCellRenderer ;
import edu.jach.qt.utils.ErrorBox ;
import edu.jach.qt.utils.FileExtensionFilter ;
import edu.jach.qt.utils.FileUtils ;

/* Miscellaneous imports */

/**
 * Implements the Deferred List functionality.
 * @author $Author$
 * @version $Id$
 */
final public class DeferredProgramList extends JPanel implements DropTargetListener , DragSourceListener , DragGestureListener , ActionListener , ListSelectionListener
{
	private DropTarget dropTarget = null ;
	private DragSource dragSource = null ;
	private static JList obsList ;
	private GridBagConstraints gbc ;
	private JScrollPane scrollPane = new JScrollPane() ;
	private DefaultListModel model ;
	private static SpItem currentItem ;
	private static HashMap<SpItem,String> fileToObjectMap = new HashMap<SpItem,String>() ;
	private JPopupMenu engMenu = new JPopupMenu() ;
	private JMenuItem engItem = new JMenuItem( "Send for Engineering" ) ;
	private boolean _useQueue = true ;
	private static HashSet<SpItem> duplicates = new HashSet<SpItem>() ;
	static JACLogger logger = JACLogger.getLogger( DeferredProgramList.class ) ;
	
	private static final FileExtensionFilter xmlFilter = new FileExtensionFilter( ".xml" ) ;
	private static boolean nodefer = false ;

	/**
	 * Constructor.
	 * Creates the Interface and makes it a drop target.
	 */
	public DeferredProgramList()
	{
		nodefer = System.getProperty( "NODEFER" ) != null ;
		
		Border border = BorderFactory.createMatteBorder( 2 , 2 , 2 , 2 , Color.white ) ;
		setBorder( new TitledBorder( border , "Deferred MSBs" , 0 , 0 , new Font( "Roman" , Font.BOLD , 12 ) , Color.black ) ) ;
		setLayout( new BorderLayout() ) ;

		GridBagLayout gbl = new GridBagLayout() ;
		setLayout( gbl ) ;
		gbc = new GridBagConstraints() ;

		model = new DefaultListModel() ;
		currentItem = null ;

		dropTarget = new DropTarget() ;
		try
		{
			dropTarget.addDropTargetListener( this ) ;
		}
		catch( TooManyListenersException tmle )
		{
			logger.error( "Too many drop target listeners" , tmle ) ;
		}
		dragSource = new DragSource() ;

		engMenu.add( engItem ) ;
		engItem.addActionListener( this ) ;

		// Set up the initial drop target
		scrollPane.getViewport().setDropTarget( dropTarget ) ;
		getCurrentList() ;
		displayList() ;
	}

	public static void setCurrentItem( SpItem item )
	{
		currentItem = item ;
	}
	
	public static SpItem getCurrentItem()
	{
		return currentItem ;
	}

	/**
	 * Updates the display of the deferred observations.
	 */
	public void reload()
	{
		fileToObjectMap.clear() ;
		model.clear() ;
		getCurrentList() ;
		displayList() ;
	}

	/**
	 * Gets the current set of deferred observations. 
	 * These observations are stored on disk, 
	 * and the directory is read looking for suitable observations.
	 */
	public void getCurrentList()
	{
		if( !nodefer )
		{
			String[] deferredFiles = getFileList() ;
	
			// parse through the xml files and add each one to the current list
			for( int fileCounter = 0 ; fileCounter < deferredFiles.length ; fileCounter++ )
			{
				// Only look for files which are readable
				String currentFileName = deferredFiles[ fileCounter ] ;
				File currentFile = new File( currentFileName ) ;
				
				if( currentFile.canRead() && currentFile.isFile() && currentFile.length() > 0 && !currentFile.isHidden() )
				{
					try
					{
						FileReader reader = new FileReader( currentFile ) ;
						char[] chars = new char[ 1024 ] ;
						int readLength = 0 ;
						StringBuffer buffer = new StringBuffer() ;
						while( !reader.ready() )
							;
						while( ( readLength = reader.read( chars ) ) != -1 )
							buffer.append( chars , 0 , readLength ) ;
						reader.close() ;
						SpItem currentSpItem = ( new SpInputXML() ).xmlToSpItem( buffer.toString() ) ;
						if( currentSpItem != null )
						{
							fileToObjectMap.put( currentSpItem , currentFileName ) ;
							addElement( currentSpItem ) ;
							NotePanel.setNote( currentSpItem ) ;
						}
					}
					catch( FileNotFoundException fnf )
					{
						logger.error( "File not found!" , fnf ) ;
					}
					catch( IOException ioe )
					{
						logger.error( "File read error!" , ioe ) ;
					}
					catch( Exception x )
					{
						logger.error( "Can not convert file!" , x ) ;
					}
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
		model.addElement( obs ) ;
		displayList() ;
	}

	/**
	 * Add a calibration observation from the Calibrations Menu to the list.
	 * 
	 * @param cal The calibration observation to add.
	 */
	public static void addCalibration( SpItem cal )
	{
		cal.getTable().set( "project" , "CAL" ) ;
		cal.getTable().set( "msbid" , "CAL" ) ;
		cal.getTable().set( ":msb" , "true" ) ;
		// A calibration observation is an SpProg - need to convert to an SpObs
		Vector<SpItem> theseObs = SpTreeMan.findAllItems( cal , SpObs.class.getName() ) ;
		SpItem thisObs = theseObs.firstElement() ;
		if( thisObs != cal )
		{
			thisObs.getTable().set( "project" , "CAL" ) ;
			thisObs.getTable().set( "msbid" , "CAL" ) ;
			thisObs.getTable().set( ":msb" , "true" ) ;

			if( !duplicates.contains( thisObs ) )
			{
				SpInstObsComp inst = SpTreeMan.findInstrument( thisObs ) ;
				SpInsertData insertable = SpTreeMan.evalInsertInside( inst , thisObs ) ;
				if( insertable != null )
					SpTreeMan.insert( insertable ) ;
				duplicates.add( thisObs ) ;
			}

			if( thisObs.getTitleAttr().equals( "Observation" ) )
				thisObs.setTitleAttr( cal.getTitleAttr() ) ;
		}

		(( SpObs )thisObs).setOptional( true ) ;

		if( !isDuplicate( thisObs ) )
		{
			makePersistent( thisObs ) ;
			( ( DefaultListModel )obsList.getModel() ).addElement( thisObs ) ;
		}
		// This is a hack to fix fault [20021030.002]. It shouldn't happen but hopefully this will make sure...
		obsList.setEnabled( true ) ;
		obsList.setSelectedIndex( obsList.getModel().getSize() - 1 ) ;
		setCurrentItem( ( SpItem )obsList.getSelectedValue() ) ;
		NotePanel.setNote( getCurrentItem() ) ;
		ProgramTree.clearSelection() ;
	}

	/**
	 * Deselects the currently selected deferred observation.
	 */
	public static void clearSelection()
	{
		obsList.clearSelection() ;
		setCurrentItem( null ) ;
	}

	/**
	 * Display the current list of deferred observations.
	 */
	public void displayList()
	{
		obsList = new JList( model ) ;
		obsList.setCellRenderer( new ObsListCellRenderer() ) ;

		// Make this list and drag source and drop target
		obsList.setDropTarget( dropTarget ) ;
		dragSource.createDefaultDragGestureRecognizer( obsList , DnDConstants.ACTION_MOVE , this ) ;

		MouseListener ml = new MouseAdapter()
		{
			public void mouseClicked( MouseEvent e )
			{
				if( e.getClickCount() == 2 )
					execute() ;
			}

			public void mousePressed( MouseEvent e )
			{
				if( e.isPopupTrigger() && getCurrentItem() != null )
					engMenu.show( e.getComponent() , e.getX() , e.getY() ) ;
				else
					ProgramTree.clearSelection() ;
			}
		} ;
		obsList.addMouseListener( ml ) ;
		obsList.addListSelectionListener( this ) ;
		// Add the listbox to a scrolling pane
		scrollPane.getViewport().removeAll() ;
		scrollPane.getViewport().add( obsList ) ;
		scrollPane.getViewport().setOpaque( true ) ;

		gbc.fill = GridBagConstraints.BOTH ;
		gbc.insets.bottom = 5 ;
		gbc.insets.left = 10 ;
		gbc.insets.right = 5 ;
		gbc.weightx = 100 ;
		gbc.weighty = 100 ;
		add( scrollPane , gbc , 0 , 0 , 2 , 1 ) ;
	}

	/**
	 * Append a new observation to the current list.
	 * 
	 * @param obs The observation to append.
	 */
	public void appendItem( SpItem obs )
	{
		obs.getTable().set( "project" , "CAL" ) ;
		obs.getTable().set( "msbid" , "CAL" ) ;
		obs.getTable().set( ":msb" , "true" ) ;
		obs = makeNewObs( obs ) ;
		if( isDuplicate( obs ) == false )
		{
			makePersistent( obs ) ;
			addElement( obs ) ;
		}
	}

	/**
	 * This a fix to get over historical problems with XML
	 * @param current SpItem
	 * @return same SpItem, without associated problems.
	 */
	private static SpItem makeNewObs( SpItem current )
	{
		SpItem newItem = current ;
		try
		{
			String xmlString = current.toXML() ;
			newItem = ( new SpInputXML() ).xmlToSpItem( xmlString ) ;
		}
		catch( Exception e ){}
		return newItem ;
	}

	/**
	 * Check whether the current observation is already in the deferred list.
	 * 
	 * @param obs The observation to compare.
	 * @return <code>true</code> is the observation already exists ; <code>false</code> otherwise.
	 */
	public static boolean isDuplicate( SpItem obs )
	{
		boolean isDuplicate = false ;

		String currentObsXML = obs.toXML() ;
		for( int i = 0 ; i < ( ( DefaultListModel )obsList.getModel() ).size() ; i++ )
		{
			SpItem thisObs = ( SpItem )(( DefaultListModel )obsList.getModel()).elementAt( i ) ;
			String thisObsXML = thisObs.toXML() ;
			if( thisObsXML.equals( currentObsXML ) )
			{
				isDuplicate = true ;
				break ;
			}
		}
		return isDuplicate ;
	}

	/*
	 * This more or less mimics the doExecute() method the ProgramTree.java. 
	 * Its real purpose is to let users do calibrations even if the "Send for Execution" button is disabled.
	 */
	private void execute()
	{
		Thread t = null ;

		if( getCurrentItem() != null )
		{
			/*
			 * If we have items selected on both the ProgramList and Deferred List 
			 * let the execute method handle the problem.
			 */
			if( System.getProperty( "telescope" ).equalsIgnoreCase( "ukirt" ) )
			{
				try
				{
					logger.info( "Sending observation " + getCurrentItem().getTitle() + " for execution." ) ;
					ExecuteUKIRT execute = new ExecuteUKIRT( _useQueue ) ;
					execute.setDeferred( true ) ;
					File failFile = execute.failFile() ;
					File successFile = execute.successFile() ;
					t = new Thread( execute , "UKIRT Execution Thread" ) ;
					
					// Start the process and wait for it to complete
					t.start() ;
					t.join() ;
					// Reset _useQueue
					_useQueue = true ;
					// Now check the result
					if( failFile.exists() )
					{
						new ErrorBox( this , "Failed to Execute. Check messages." ) ;
						logger.warn( "Failed to execute observation" ) ;
					}
					else if( successFile.exists() )
					{
						// Mark this observation as having been done
						markThisObservationAsDone( getCurrentItem() ) ;
						logger.info( "Observation executed successfully" ) ;
					}
					else
					{
						// Neither file exists - report an error to the user
						new ErrorBox( "Unable to determine success status - assuming failed." ) ;
						logger.error( "Unable to determine success status for observation." ) ;
					}
					
					// done with status files
					if( failFile.exists() )
						failFile.delete() ;
					if( successFile.exists() )
						successFile.delete() ;
				}
				catch( Exception e )
				{
					logger.error( "Failed to execute thread." , e ) ;
					if( t != null && t.isAlive() )
					{
						logger.error( "Last observation still seems to be running" ) ;
					}
				}
			}
			else if( System.getProperty( "telescope" ).equalsIgnoreCase( "jcmt" ) )
			{
				new ExecuteInThread( getCurrentItem() , true ).start() ;
			}
		}
	}

	/**
	 * Add a component to the <code>GridBagConstraints</code>
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
		gbc.gridx = x ;
		gbc.gridy = y ;
		gbc.gridwidth = w ;
		gbc.gridheight = h ;
		add( c , gbc ) ;
	}
	
	/**
	 * Stores this observation in it own unique file in the deferred directory.
	 * @param item
	 */
	private static void makePersistent( SpItem item )
	{
		if( !nodefer )
		{
			String fName = makeFilenameForDeferredObservation() ;
			FileWriter fw = null ;
			try
			{
				fw = new FileWriter( fName ) ;
				fw.write( item.toXML() ) ;
				fw.flush() ;
			}
			catch( IOException ioe )
			{
				logger.error( "Error writing file " + fName , ioe ) ;
			}
			finally
			{
				if( fw != null )
				{
					try
					{
						fw.close() ;
					}
					catch( IOException ioe ){}
				}
			}
			fileToObjectMap.put( item , fName ) ;
		}
	}

	/**
	 * Create a full-path-name for a deferred observation.
	 * @return fullpath name for an observation as a String.
	 */
	private static String makeFilenameForDeferredObservation()
	{
		StringBuffer buffer = new StringBuffer() ;
		buffer.append( FileUtils.getTodaysDeferredDirectoryName() ) ;
		buffer.append( File.separator ) ;
		Date now = new Date() ;
		buffer.append( now.getTime() ) ;
		buffer.append( ".xml" ) ;
		String fName = buffer.toString() ;
		buffer.delete( 0 , buffer.length() ) ;
		buffer = null ;
		return fName ;
	}

	/**
	 * Return a String array of full-path-names for deferred observations.
	 */
	private static String[] getFileList()
	{
		String deferredFiles[] = {} ;
		File deferredDir = FileUtils.getTodaysDeferredDirectory() ;
		String todaysDeferredDirName = FileUtils.getTodaysDeferredDirectoryName() ;
		if( deferredDir != null )
		{
			String[] directoryContents = deferredDir.list( xmlFilter ) ;
			if( directoryContents != null )
				deferredFiles = directoryContents ;
			
			String currentFileName ;
			
			for( int i = 0 ; i < deferredFiles.length ; i++ )
			{
				currentFileName = deferredFiles[ i ] ;
				if( !currentFileName.contains( File.separator ) )
					deferredFiles[ i ] = todaysDeferredDirName + File.separator + currentFileName ;
			}
		}
		return deferredFiles ;
	}

	/**
	 * Delete all deferred observation files and clear the contents of the Deferred Program List.
	 */
	public static void cleanup()
	{
		File deferredParent = FileUtils.getDeferredDirectory() ;
		File deferred = FileUtils.getTodaysDeferredDirectory() ;
		
		if( deferredParent != null )
		{
			File[] files = deferredParent.listFiles() ;
			for( int index = 0 ; index < files.length ; index++ )
			{
				File file = files[ index ] ;
				if( !FileUtils.delete( file , deferred ) )
					logger.error( "Could not delete " + file.getName() ) ;
			}
		}
		// Now clear the hashmap, just for completeness
		if( !fileToObjectMap.isEmpty() )
			fileToObjectMap.clear() ;
	}

	/**
	 * Mark an observation as done. This is done by timestamping the title.
	 */
	public static void markThisObservationAsDone( SpItem thisObservation )
	{
		// Add a dat-time stamp to the title
		String currentTitle = thisObservation.getTitle() ;
		String[] split = currentTitle.split( "_" ) ;
		String baseName = "" ;
		for( int index = 0 ; index < split.length ; index++ )
		{
			String subStr = split[ index ] ;
			if( subStr.equals( "done" ) )
				break ;
			baseName += subStr ;
		}
		currentTitle = baseName ;
		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd'T'HHmmss" ) ;
		Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ) ;
		String observationTime = df.format( cal.getTime() ) ;
		String newTitle = currentTitle + "_done_" + observationTime ;
		thisObservation.setTitleAttr( newTitle ) ;

		// Delete the old entry and replace by the current
		String fileToRemove = fileToObjectMap.get( obsList.getSelectedValue() ) ;
		if( fileToRemove != null )
		{	
			File f = new File( fileToRemove ) ;
			f.delete() ;
			fileToObjectMap.remove( obsList.getSelectedValue() ) ;
		}
		int index = obsList.getSelectedIndex() ;
		if( index > -1 )
			( ( DefaultListModel )obsList.getModel() ).removeElementAt( index ) ;
		setCurrentItem( null ) ;

		makePersistent( thisObservation ) ;

		(( DefaultListModel )obsList.getModel()).addElement( thisObservation ) ;
	}

	/*
	 * Add the event listeners
	 */
	/* DROP TARGET EVENTS */
	public void dragEnter( DropTargetDragEvent evt ){}
	public void dragExit( DropTargetEvent evt ){}
	public void dragOver( DropTargetDragEvent evt ){}
	public void dropActionChanged( DropTargetDragEvent evt ){}

	/**
	 * Implementation of <code>DropTargetListener</code> interface. 
	 * Adds the currently selected item from the Program List to the deferred list.
	 * 
	 * @param evt A <code>DropTargetDropEvent</code> object.
	 */
	public void drop( DropTargetDropEvent evt )
	{
		if( this.dropTarget.isActive() )
		{
			evt.acceptDrop( DnDConstants.ACTION_MOVE ) ;
			SpItem thisObs = ProgramTree.getObservationToDefer() ;
			if( (( SpObs )thisObs).isOptional() )
			{
				appendItem( thisObs ) ;
				evt.getDropTargetContext().dropComplete( true ) ;
			}
			else
			{
				JOptionPane.showMessageDialog( this , "Can not defer mandatory observations!" ) ;
				evt.getDropTargetContext().dropComplete( false ) ;
			}
		}
		else
		{
			evt.rejectDrop() ;
			evt.dropComplete( false ) ;
		}
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
			// Delete the file
			String fileToRemove = fileToObjectMap.get( obsList.getSelectedValue() ) ;
			if( fileToRemove != null )
			{
				File f = new File( fileToRemove ) ;
				f.delete() ;
			}

			// Delete the entry from the map
			fileToObjectMap.remove( obsList.getSelectedValue() ) ;

			// Remove the entry from the list
			int index = obsList.getSelectedIndex() ;
			if( index > -1 )
				( ( DefaultListModel )obsList.getModel() ).removeElementAt( index ) ;

			setCurrentItem( null ) ;
		}
		obsList.setEnabled( true ) ;
		this.dropTarget.setActive( true ) ;
	}

	public void dragEnter( DragSourceDragEvent evt ){}

	public void dragExit( DragSourceEvent evt )
	{
		evt.getDragSourceContext().setCursor( DragSource.DefaultMoveNoDrop ) ;
	}

	public void dragOver( DragSourceDragEvent evt )
	{
		evt.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop ) ;
	}

	/**
	 * Implementation of the <code>DragSourceListener</code> interface.
	 * 
	 * @param evt A <code>DragSourceDragEvent</code> object.
	 */
	public void dropActionChanged( DragSourceDragEvent evt ){}

	/**
	 * Implementation of the <code>DragGestureListener</code> interface. Disables the current list from being a drop target temporarily so that we cant drop items from the list back on to the list.
	 * 
	 * @param event A <code>DragGestureEvent</code> object.
	 */
	public void dragGestureRecognized( DragGestureEvent event )
	{
		SpItem selected = ( SpItem )obsList.getSelectedValue() ;
		ProgramTree.clearSelection() ;
		obsList.setEnabled( false ) ;
		if( selected != null )
		{
			StringSelection text = new StringSelection( selected.toString() ) ;
			// Disable dropping on this window
			this.dropTarget.setActive( false ) ;
			dragSource.startDrag( event , DragSource.DefaultMoveNoDrop , text , this ) ;
		}
	}

	public void actionPerformed( ActionEvent e )
	{
		if( e.getSource() == engItem )
		{
			_useQueue = false ;
			execute() ;
		}
	}

	public class ExecuteInThread extends Thread
	{
		SpProg _item = ( SpProg )SpFactory.create( SpType.SCIENCE_PROGRAM ) ;

		boolean _isDeferred ;

		public ExecuteInThread( SpItem item , boolean deferred )
		{
			// Make the obs into an SpProg
			_item.setPI( "observer" ) ;
			_item.setCountry( "JAC" ) ;
			_item.setTelescope() ;
			if( _item.getProjectID() == null || _item.getProjectID().equals( "" ) )
				_item.setProjectID( "CAL" ) ;
			_item.setTitleAttr( item.getTitleAttr() ) ;
			SpInsertData spID = SpTreeMan.evalInsertInside( item , _item ) ;
			if( spID != null )
				SpTreeMan.insert( spID ) ;
			_isDeferred = deferred ;
		}

		public void run()
		{
			ExecuteJCMT execute = null ;
			boolean failed = false ;

			execute = ExecuteJCMT.getInstance( _item ) ;
			if( execute == null )
				return ;
			execute.setDeferred( _isDeferred ) ;
			failed = execute.run() ;

			if( failed )
			{
				new ErrorBox( "Failed to Execute. Check messages." ) ;
				logger.warn( "Failed to execute observation" ) ;
			}
			else
			{
				markThisObservationAsDone( getCurrentItem() ) ;
				logger.info( "Observation executed successfully" ) ;
			}
		}
	}

	public void valueChanged( ListSelectionEvent e )
	{
		Object source = e.getSource() ;
		if( source == obsList )
		{
			SpItem item = ( SpItem )obsList.getSelectedValue() ;
			if( item != null && getCurrentItem() != item )
			{
				setCurrentItem( item ) ;
				NotePanel.setNote( getCurrentItem() ) ;
				ProgramTree.clearSelection() ;
			}
		}
	}
}
