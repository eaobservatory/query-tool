package edu.jach.qt.gui ;

import javax.swing.tree.DefaultMutableTreeNode ;

/** This class forces "male" nodes to have leaf icons and
 forbids male childbaring ability 
 Should be deprecated and not replaced, however it's used everywhere.
 */
public class MsbNode extends DefaultMutableTreeNode
{
	public MsbNode( DragDropObject info )
	{
		super( info ) ;
	}

	/** Override a few methods... */
	public boolean isLeaf()
	{
		return false ;
	}

	public boolean getAllowsChildren()
	{
		return true ;
	}

	public void add( DefaultMutableTreeNode child )
	{
		super.add( child ) ;

		DragDropObject childPI = ( DragDropObject )( ( MsbNode )child ).getUserObject() ;

		DragDropObject newParent = ( DragDropObject )getUserObject() ;

		newParent.add( childPI ) ;
	}

	public void remove( DefaultMutableTreeNode child )
	{
		super.remove( child ) ;

		DragDropObject childDDO = ( DragDropObject )( ( MsbNode )child ).getUserObject() ;

		DragDropObject parentDDO = ( DragDropObject )getUserObject() ;
		if( parent != null )
			parentDDO.remove( childDDO ) ;
	}
}
