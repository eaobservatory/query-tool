/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
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

import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.awt.BorderLayout ;
import java.awt.GridLayout ;
import javax.swing.JFrame ;
import javax.swing.JPanel ;
import javax.swing.JCheckBox ;
import javax.swing.JButton ;

import edu.jach.qt.utils.MsbClient ;

import edu.jach.qt.utils.MsbColumnInfo ;
import edu.jach.qt.utils.MsbColumns ;

/**
 * This class is used to display a series of checkboxes which allow a
 * user to define a subset of columns which they want to appear in a table.
 * Extends <code>JFrame</code> and implements <code>ActionListener</code>.
 * @author  $Author$
 * @version $Id$
 */
@SuppressWarnings( "serial" )
public class ColumnSelector extends JFrame implements ActionListener
{
	private JPanel columnPanel ;
	private QtFrame parent ;
	private MSBQueryTableModel _msbqtm ;

	/**
	 * Default constructor
	 */
	public ColumnSelector(){}

	/**
	 * Normal constructor.  This should be used for all calls.
	 * @param  frame  The parent <code>JFrame</code>
	 */
	public ColumnSelector( QtFrame frame )
	{
		parent = frame ;
		_msbqtm = parent.getModel() ;
		this.setSize( 150 , 300 ) ;
		this.setLayout( new BorderLayout() ) ;
		this.addCheckBoxes() ;
		this.addCloseButton() ;
		this.setVisible( true ) ;
	}

	/**
	 * Alternate constructor.  This can be used in place of the standard
	 * constructor, but the resulting table may be displayed incorrectly.
	 * @param model    The model used to generate the table.
	 */
	public ColumnSelector( MSBQueryTableModel model )
	{
		// Disable the parent
		_msbqtm = model ;
		this.setSize( 150 , 300 ) ;
		this.setLayout( new BorderLayout() ) ;
		this.addCheckBoxes() ;
		this.addCloseButton() ;
		this.setVisible( true ) ;
	}

	private void addCheckBoxes()
	{
		columnPanel = new JPanel( new GridLayout( 0 , 1 ) ) ;
		final MsbColumns columns = MsbClient.getColumnInfo() ;
		JCheckBox checkBox ;
		for( int i = 0 ; i < columns.size() ; i++ )
		{
			final MsbColumnInfo columnInfo = columns.find( i ) ;
			checkBox = new JCheckBox( columnInfo.getName() ) ;
			if( columnInfo.getVisible() )
				checkBox.setSelected( true ) ;
			columnPanel.add( checkBox ) ;
		}
		this.add( columnPanel , BorderLayout.CENTER ) ;
	}

	private void addCloseButton()
	{
		JButton closeButton = new JButton( "Accept" ) ;
		closeButton.setLocation( 375 , 275 ) ;
		closeButton.addActionListener( this ) ;
		this.add( closeButton , BorderLayout.SOUTH ) ;
	}

	/**
	 * <code>ActionEvent</code> handler.
	 * This is run when the "Accept" button is selected.  It tells the model
	 * to update its columns.  The selected columns are passed as a 
	 * <code>BitSet</code>.  The window is then dismissed.
	 * @param  evt  The default <code>ActionEvent</code>
	 */
	public void actionPerformed( final ActionEvent evt )
	{
		final MsbColumns columns = MsbClient.getColumnInfo() ;
		for( int i = 0 ; i < columnPanel.getComponentCount() ; i++ )
		{
			if( columnPanel.getComponent( i ) instanceof JCheckBox )
			{
				JCheckBox checkBox = ( JCheckBox )columnPanel.getComponent( i ) ;
				String name = checkBox.getText() ;
				MsbColumnInfo columnInfo = columns.findName( name ) ;
				if( columnInfo != null )
					columnInfo.setVisible( checkBox.isSelected() ) ;
			}
		}

		_msbqtm.updateColumns() ;
		_msbqtm.adjustColumnData() ;
		if( parent != null )
			parent.setTableToDefault() ;
		this.dispose() ;
	}
}
