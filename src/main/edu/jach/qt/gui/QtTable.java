/*
 * Copyright (C) 2006-2013 Science and Technology Facilities Council.
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

import java.awt.Color ;

import javax.swing.JTable ;
import javax.swing.table.TableModel ;
import javax.swing.table.DefaultTableCellRenderer ;

import edu.jach.qt.utils.MsbColumns ;
import edu.jach.qt.utils.MsbClient ;
import edu.jach.qt.utils.SpQueuedMap ;

@SuppressWarnings( "serial" )
public class QtTable extends JTable
{
	private TableModel tableModel ;
	private DefaultTableCellRenderer stringTableCellRenderer ;
	private DefaultTableCellRenderer numberTableCellRenderer ;
	private MsbColumns columns = MsbClient.getColumnInfo() ;
	private SpQueuedMap spQueuedMap = SpQueuedMap.getSpQueuedMap() ;
	
	private static final Color unintrusiveGrey = new Color( 0xE8 , 0xE8 , 0xE8 ) ;
	private static final Color orFolderColor = new Color(0xAA, 0xFF, 0xCC);

	public QtTable( TableModel model )
	{
		super( model ) ;
		tableModel = model ;
		stringTableCellRenderer = ( DefaultTableCellRenderer )getDefaultRenderer( String.class ) ;
		numberTableCellRenderer = ( DefaultTableCellRenderer )getDefaultRenderer( Number.class ) ;

	}

	public Object getValueAt( int row , int column )
	{
		Object object = tableModel.getValueAt( row , column ) ;
		int checksumIndex = columns.getIndexForKey( "checksum" ) ;
		String checksum ;
		if( column != checksumIndex )
			checksum = ( String )tableModel.getValueAt( row , checksumIndex ) ;
		else
			checksum = ( String )object ;
		Color colour = Color.white ;
		if( spQueuedMap.containsMsbChecksum( checksum ) != null )
			colour = Color.orange ;
		else if( spQueuedMap.seen( checksum ) )
			colour = unintrusiveGrey ;
                else if (checksum.contains("O")) {
                    // This MSB is in an OR folder, so check whether we have
                    // queued other OR folder MSBs from the same project.
                    // (Because we don't have a way to determine which
                    // specific OR folder an MSB is in.)
                    int projectIndex = columns.getIndexForKey("projectid");
                    String project;

                    if (column != projectIndex) {
                        project = (String) tableModel.getValueAt(row, projectIndex);
                    }
                    else {
                        project = (String) object;
                    }

                    if (spQueuedMap.usedOrFolder(project)) {
                        colour = orFolderColor;
                    }
                }
		stringTableCellRenderer.setBackground( colour ) ;
		numberTableCellRenderer.setBackground( colour ) ;
		return object ;
	}
}
