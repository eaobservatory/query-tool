package edu.jach.qt.gui;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jach.qt.utils.MsbColumns;
import edu.jach.qt.utils.MsbClient;
import edu.jach.qt.utils.SpQueuedMap;

public class QtTable extends JTable
{
	private TableModel tableModel;
	private DefaultTableCellRenderer stringTableCellRenderer;
	private DefaultTableCellRenderer numberTableCellRenderer;
	private MsbColumns columns = MsbClient.getColumnInfo();
	private SpQueuedMap spQueuedMap = SpQueuedMap.getSpQueuedMap();
	
	private static final Color unintrusiveGrey = new Color( 0xE8 , 0xE8 , 0xE8 ) ;

	public QtTable( TableModel model )
	{
		super( model );
		tableModel = model;
		stringTableCellRenderer = ( DefaultTableCellRenderer )getDefaultRenderer( String.class );
		numberTableCellRenderer = ( DefaultTableCellRenderer )getDefaultRenderer( Number.class );

	}

	public Object getValueAt( int row , int column )
	{
		Object object = tableModel.getValueAt( row , column );
		int checksumIndex = columns.getIndexForName( "checksum" );
		String checksum;
		if( column != checksumIndex )
			checksum = ( String )tableModel.getValueAt( row , checksumIndex );
		else
			checksum = ( String )object;
		Color colour = Color.white;
		if( spQueuedMap.containsMsbChecksum( checksum ) != null )
			colour = Color.orange;
		else if( spQueuedMap.seen( checksum ) )
			colour = unintrusiveGrey ;
		stringTableCellRenderer.setBackground( colour );
		numberTableCellRenderer.setBackground( colour );
		return object;
	}
}
