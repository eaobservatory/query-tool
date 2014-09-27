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

import javax.swing.tree.DefaultMutableTreeNode ;

/** This class forces "male" nodes to have leaf icons and
 forbids male childbaring ability 
 Should be deprecated and not replaced, however it's used everywhere.
 */
@SuppressWarnings( "serial" )
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
