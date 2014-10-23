/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.gui ;

/* Gemini imports */
import gemini.sp.SpItem ;
import gemini.sp.SpObs;
import gemini.sp.SpTreeMan ;
import gemini.sp.SpMSB ;
import gemini.util.JACLogger ;

/* JSKY imports */
import jsky.app.ot.OtFileIO ;

/* ORAC imports */

/* QT imports */
import edu.jach.qt.utils.QtTools ;

/* Standard imports */
import java.awt.Dimension ;
import java.io.File ;
import java.util.Hashtable ;
import java.util.NoSuchElementException ;
import java.util.Vector ;
import java.util.Enumeration ;
import javax.swing.JPanel ;
import javax.swing.JSplitPane ;
import javax.swing.JFrame ;

/* Miscellaneous imports */

/**
 * This is the top most class of the OMP-OM.  This 
 * starts off all subsequent OMP-OM specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings( "serial" )
public class OmpOM extends JPanel
{
	static final JACLogger logger = JACLogger.getLogger( OmpOM.class ) ;
	private ProgramTree ptree ;
	private File file ;
	private SpItem spItem ;
	private Hashtable<Integer,ProgramTree> ptreeHashtable ;
	private DeferredProgramList deferredList ;
	public NotePanel notes ;

	/**
	 * Creates a new <code>OmpOM</code> instance.
	 *
	 */
	public OmpOM()
	{
		ptreeHashtable = new Hashtable<Integer,ProgramTree>() ;
		logger.info( "SpItems initialized" ) ;
		ptree = new ProgramTree() ;
	}

	/**
	 * Set the SpItem.
	 * Set the SpItem to that passed in.
	 * @param item   The value of the SpItem to set.
	 */
	public void setSpItem( SpItem item )
	{
		spItem = item ;
	}

	/** 
	 * Set whether the current SpItems can be executed.
	 * @param flag   <code>true</code> if items can be executed.
	 */
	public void setExecutable( boolean flag )
	{
		ProgramTree.setExecutable( flag ) ;
	}

	/**
	 * Get the name of the program.
	 * Gets the name of the program from Title field in the first observation.
	 * @return The program name, <italic>'No Observations'</italic> if no program, or <italic>'Title Not Found'</italic> on error.
	 */
	public String getProgramName()
	{
		String returnString = "Title Not Found" ;
		SpItem currentItem = ProgramTree.getCurrentItem() ;
		if( currentItem != null )
		{
			Vector<SpItem> progVector = SpTreeMan.findAllItems( currentItem , SpMSB.class.getName() ) ;
			if( progVector == null || progVector.size() == 0 )
				progVector = SpTreeMan.findAllItems( currentItem , SpObs.class.getName() ) ;
			try
			{
				if( progVector != null && progVector.size() > 0 )
				{
					SpMSB spMsb = ( SpMSB )progVector.firstElement() ;
					returnString = spMsb.getTitle() ;
				}
			}
			catch( NoSuchElementException nse )
			{
				logger.warn( returnString ) ;
				nse.printStackTrace() ;
			}
		}
		else
		{
			returnString = "No Observations" ;
		}
		return returnString ;
	}

	/**
	 * This adds a
	 * <code>ProgramTree</code>, referrenced by the msbID, to the list
	 * of trees.
	 *
	 * @param msbID an <code>int</code> value
	 */
	public void addNewTree( Integer msbID )
	{
		if( msbID == null )
			ptree.addList( null ) ;
		else
			ptree.addList( spItem ) ;

		ptree.setMinimumSize( new Dimension( 400 , 550 ) ) ;
	}

	/**
	 * 
	 * The method used for debugging.  It loads in a hard-wired MSB file
	 * to use as the ProgramTree object.
	 */
	public void addNewTree()
	{
		file = new File( System.getProperty( "arrayTests" , "/home/mrippa/install/omp/QT/config/array_tests.xml" ) ) ;
		spItem = OtFileIO.fetchSp( file.getParent() , file.getName() ) ;

		ptree.addList( spItem ) ;
		ptree.setMinimumSize( new Dimension( 400 , 550 ) ) ;

		ptreeHashtable.put( new Integer( 41 ) , ptree ) ;
	}

	/**
	 * Reloads the current program.
	 */
	public void resetTree()
	{
		spItem = OtFileIO.fetchSp( file.getParent() , file.getName() ) ;
		ptree.addList( spItem ) ;
	}

	/**
	 * Construct the Staging Panel. Builds the deferred list and Observer Notes panel and displaays as a tabbed pane.
	 * 
	 * @return The <code>JSplitPanel</code> staging area.
	 */
	public JSplitPane getTreePanel()
	{
		deferredList = new DeferredProgramList() ;
		notes = new NotePanel() ;
		JSplitPane dsp = new JSplitPane( JSplitPane.VERTICAL_SPLIT , deferredList , notes ) ;
		dsp.setDividerLocation( 150 ) ;
		JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , ptree , dsp ) ;
		updateNotes() ;
		return splitPane ;
	}

	public void updateNotes()
	{
		if( ProgramTree.getCurrentItem() != null )
			NotePanel.setNote( ProgramTree.getCurrentItem() ) ;
		else if( DeferredProgramList.getCurrentItem() != null )
			NotePanel.setNote( DeferredProgramList.getCurrentItem() ) ;
		else
			NotePanel.setNote( spItem ) ;
	}

	/**
	 * See if we can shutdown the QT.  Checks whether the program tree still has
	 * an MSB which requires actioning.
	 */
	public boolean checkProgramTree()
	{
		boolean safeToExit = true ;
		if( ptree != null )
			safeToExit = ptree.shutDownRequest() ;

		return safeToExit ;
	}

	/** public void getItems (SpItem spItem,DefaultMutableTreeNode node)
	 is a public method to add ALL the items of a sp object into the
	 JTree *recursively*.
	 
	 @param SpItem spItem,DefaultMutableTreeNode node
	 @return  none
	 @throws none
	 
	 */
	private void getItems( SpItem spItem , MsbNode node )
	{
		Enumeration<SpItem> children = spItem.children() ;
		while( children.hasMoreElements() )
		{
			SpItem childNode = children.nextElement() ;
			DragDropObject ddo = new DragDropObject( childNode ) ;
			MsbNode temp = new MsbNode( ddo ) ;
			node.add( temp ) ;
			getItems( childNode , temp ) ;
		}
	}

	public void updateDeferredList()
	{
		deferredList.reload() ;
	}

	public void enableList( boolean flag )
	{
		ptree.enableList( flag ) ;
	}

	public static void main( String[] args )
	{
		QtTools.loadConfig( System.getProperty( "qtConfig" ) ) ;
		QtTools.loadConfig( System.getProperty( "omConfig" ) ) ;

		JFrame f = new JFrame() ;
		OmpOM om = new OmpOM() ;
		om.addNewTree() ;
		f.add( om.getTreePanel() ) ;
		f.setSize( 400 , 300 ) ;
		f.setVisible( true ) ;
	}
}// OmpOM
