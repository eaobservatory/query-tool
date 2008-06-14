package edu.jach.qt.gui ;

/*
 **  This is version II of DnDJTree. The first version allowed for what I 
 **  thought was a JDK oversight. However, we can set the cursor appropriately,
 **  relative to whether the current cursor location is a valid drop target.
 **
 **  If this is your first time reading the source code. Just ignore the above
 **  comment and ignore the "CHANGED" comments below. Otherwise, the 
 **  "CHANGED" comments will show where the code has changed.
 **
 **  Credit for finding this shortcoming in my code goes Laurent Hubert.
 **  Thanks Laurent.
 **
 **  Rob. [ rkenworthy@hotmail.com ]
 */

import javax.swing.JTree ;
import javax.swing.SwingUtilities ;
import javax.swing.JOptionPane ;
import javax.swing.event.TreeSelectionListener ;
import javax.swing.event.TreeSelectionEvent ;
import javax.swing.tree.TreePath ;
import javax.swing.tree.DefaultTreeModel ;

import java.awt.Cursor ;
import java.awt.Point ;
import java.awt.datatransfer.Transferable ;
import java.awt.datatransfer.UnsupportedFlavorException ;
import java.awt.dnd.DragGestureListener ;
import java.awt.dnd.DropTargetListener ;
import java.awt.dnd.DragSourceListener ;
import java.awt.dnd.DragGestureEvent ;
import java.awt.dnd.DropTargetEvent ;
import java.awt.dnd.DragSourceEvent ;
import java.awt.dnd.DragSource ;
import java.awt.dnd.DragGestureRecognizer ;
import java.awt.dnd.DnDConstants ;
import java.awt.dnd.DragSourceDropEvent ;
import java.awt.dnd.DragSourceDragEvent ;
import java.awt.dnd.DropTargetDropEvent ;
import java.awt.dnd.DropTargetDragEvent ;
import java.awt.event.InputEvent ;
import java.io.IOException ;

public class DnDJTree extends JTree implements TreeSelectionListener , DragGestureListener , DropTargetListener , DragSourceListener
{
	/** Stores the selected node info */
	protected TreePath selectedTreePath = null ;

	protected MsbNode selectedNode = null ;

	/** Variables needed for DnD */
	private DragSource dragSource = null ;

	/** Constructor 
	 @param root The root node of the tree
	 @param parent Parent JFrame of the JTree */
	public DnDJTree( MsbNode root )
	{
		super( root ) ;

		addTreeSelectionListener( this ) ;

		/* ********************** CHANGED ********************** */
		dragSource = DragSource.getDefaultDragSource() ;
		/* ****************** END OF CHANGE ******************** */

		DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer
		( 
				this , //DragSource
				DnDConstants.ACTION_COPY_OR_MOVE , //specifies valid actions
				this //DragGestureListener
		) ;

		/* 
		 * Eliminates right mouse clicks as valid actions - useful especially if you implement a JPopupMenu for the JTree
		 */
		dgr.setSourceActions( dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK ) ;

		// unnecessary, but gives FileManager look
		putClientProperty( "JTree.lineStyle" , "Angled" ) ;
	}

	/** Returns The selected node */
	public MsbNode getSelectedNode()
	{
		return selectedNode ;
	}

	///////////////////////// Interface stuff ////////////////////

	/** DragGestureListener interface method */
	public void dragGestureRecognized( DragGestureEvent e )
	{
		//Get the selected node
		MsbNode dragNode = getSelectedNode() ;
		if( dragNode != null )
		{
			//Get the Transferable Object
			Transferable transferable = ( Transferable )dragNode.getUserObject() ;
			/* ********************** CHANGED ********************** */

			//Select the appropriate cursor ;
			Cursor cursor = DragSource.DefaultCopyNoDrop ;
			int action = e.getDragAction() ;
			if( action == DnDConstants.ACTION_MOVE )
				cursor = DragSource.DefaultMoveNoDrop ;

			//In fact the cursor is set to NoDrop because once an action is rejected
			// by a dropTarget, the dragSourceListener are no more invoked.
			// Setting the cursor to no drop by default is so more logical, because 
			// when the drop is accepted by a component, then the cursor is changed by the
			// dropActionChanged of the default DragSource.
			/* ****************** END OF CHANGE ******************** */

			//begin the drag
			dragSource.startDrag( e , cursor , transferable , this ) ;
		}
	}

	/** DragSourceListener interface method */
	public void dragDropEnd( DragSourceDropEvent dsde ){}

	/** DragSourceListener interface method */
	public void dragEnter( DragSourceDragEvent dsde ){}

	/** DragSourceListener interface method */
	public void dragOver( DragSourceDragEvent dsde ){}

	/** DragSourceListener interface method */
	public void dropActionChanged( DragSourceDragEvent dsde ){}

	/** DragSourceListener interface method */
	public void dragExit( DragSourceEvent dsde ){}

	/** DropTargetListener interface method - What we do when drag is released */
	public void drop( DropTargetDropEvent e )
	{
		try
		{
			Transferable tr = e.getTransferable() ;

			//flavor not supported, reject drop
			if( !tr.isDataFlavorSupported( DragDropObject.DATA_FLAVOR ) )
				e.rejectDrop() ;

			//cast into appropriate data type
			DragDropObject childInfo = ( DragDropObject )tr.getTransferData( DragDropObject.DATA_FLAVOR ) ;

			//get new parent node
			Point loc = e.getLocation() ;
			TreePath destinationPath = getPathForLocation( loc.x , loc.y ) ;

			final String msg = testDropTarget( destinationPath , selectedTreePath ) ;
			if( msg != null )
			{
				e.rejectDrop() ;

				SwingUtilities.invokeLater( new Runnable()
				{
					public void run()
					{
						JOptionPane.showMessageDialog( null , msg , "Error Dialog" , JOptionPane.ERROR_MESSAGE ) ;
					}
				} ) ;
				return ;
			}
			MsbNode newParent = ( MsbNode )destinationPath.getLastPathComponent() ;

			//get old parent node
			MsbNode oldParent = ( MsbNode )getSelectedNode().getParent() ;
			int action = e.getDropAction() ;
			boolean copyAction = ( action == DnDConstants.ACTION_COPY ) ;

			//make new child node
			MsbNode newChild = new MsbNode( childInfo ) ;

			try
			{
				if( !copyAction )
					oldParent.remove( getSelectedNode() ) ;
				newParent.add( newChild ) ;

				if( copyAction )
					e.acceptDrop( DnDConstants.ACTION_COPY ) ;
				else
					e.acceptDrop( DnDConstants.ACTION_MOVE ) ;
			}
			catch( java.lang.IllegalStateException ils )
			{
				e.rejectDrop() ;
			}

			e.getDropTargetContext().dropComplete( true ) ;

			//expand nodes appropriately - this probably isnt the best way...
			DefaultTreeModel model = ( DefaultTreeModel )getModel() ;
			model.reload( oldParent ) ;
			model.reload( newParent ) ;
			TreePath parentPath = new TreePath( newParent.getPath() ) ;
			expandPath( parentPath ) ;
		}
		catch( IOException io )
		{
			e.rejectDrop() ;
		}
		catch( UnsupportedFlavorException ufe )
		{
			e.rejectDrop() ;
		}
	} //end of method

	/** DropTaregetListener interface method */
	public void dragEnter( DropTargetDragEvent e ){}

	/** DropTaregetListener interface method */
	public void dragExit( DropTargetEvent e ){}

	/** DropTaregetListener interface method */
	public void dragOver( DropTargetDragEvent e )
	{
		/* ********************** CHANGED ********************** */
		//set cursor location. Needed in setCursor method
		Point cursorLocationBis = e.getLocation() ;
		TreePath destinationPath = getPathForLocation( cursorLocationBis.x , cursorLocationBis.y ) ;

		// if destination path is okay accept drop......otherwise reject drop
		if( testDropTarget( destinationPath , selectedTreePath ) == null )
			e.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE ) ;
		else
			e.rejectDrag() ;
		/* ****************** END OF CHANGE ******************** */
	}

	/** DropTaregetListener interface method */
	public void dropActionChanged( DropTargetDragEvent e ){}

	/** TreeSelectionListener - sets selected node */
	public void valueChanged( TreeSelectionEvent evt )
	{
		selectedTreePath = evt.getNewLeadSelectionPath() ;
		if( selectedTreePath == null )
			selectedNode = null ;
		else
			selectedNode = ( MsbNode )selectedTreePath.getLastPathComponent() ;
	}

	/** Convenience method to test whether drop location is valid
	 @param destination The destination path 
	 @param dropper The path for the node to be dropped
	 @return null if no problems, otherwise an explanation
	 */
	private String testDropTarget( TreePath destination , TreePath dropper )
	{
		//Typical Tests for dropping

		//Test 1.
		boolean destinationPathIsNull = destination == null ;
		if( destinationPathIsNull )
			return "Invalid drop location." ;

		//Test 2.
		MsbNode node = ( MsbNode )destination.getLastPathComponent() ;
		if( !node.getAllowsChildren() )
			return "This node does not allow children" ;

		if( destination.equals( dropper ) )
			return "Destination cannot be same as source" ;

		//Test 3.
		if( dropper.isDescendant( destination ) )
			return "Destination node cannot be a descendant." ;

		//Test 4.
		if( dropper.getParentPath().equals( destination ) )
			return "Destination node cannot be a parent." ;

		return null ;
	}

	//program entry point
	public static final void main( String args[] ){}

} //end of DnDJTree
