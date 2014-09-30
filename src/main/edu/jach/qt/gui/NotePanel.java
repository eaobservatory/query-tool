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

import java.awt.GridBagConstraints ;
import java.awt.GridBagLayout ;
import java.awt.Color ;
import java.awt.Font ;
import java.awt.BorderLayout ;
import java.awt.Component ;
import java.util.ArrayList ;
import java.util.Vector ;
import javax.swing.JPanel ;
import javax.swing.JTextPane ;
import javax.swing.JScrollPane ;
import javax.swing.BorderFactory ;
import javax.swing.border.TitledBorder ;
import javax.swing.border.Border ;
import javax.swing.text.Style ;
import javax.swing.text.StyleConstants ;
import javax.swing.text.StyleContext ;
import javax.swing.text.Document ;

import gemini.sp.SpTreeMan ;
import gemini.sp.SpItem ;
import gemini.sp.SpNote ;

/**
 * Constructs a scrollable text panel.
 */
@SuppressWarnings( "serial" )
final public class NotePanel extends JPanel
{
	private GridBagConstraints gbc ;
	private static JTextPane textPanel = new JTextPane() ;

	/**
	 * Constructs a scrollable non-editable text panel. Sets the label of "Observer Notes", and the line wrapping convention.
	 */
	public NotePanel()
	{
		Border border = BorderFactory.createMatteBorder( 2 , 2 , 2 , 2 , Color.white ) ;
		setBorder( new TitledBorder( border , "Observer Notes" , 0 , 0 , new Font( "Roman" , Font.BOLD , 12 ) , Color.black ) ) ;
		setLayout( new BorderLayout() ) ;

		GridBagLayout gbl = new GridBagLayout() ;
		setLayout( gbl ) ;
		gbc = new GridBagConstraints() ;

		textPanel.setEditable( false ) ;

		JScrollPane scrollPane = new JScrollPane( textPanel ) ;
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ) ;

		gbc.fill = GridBagConstraints.BOTH ;
		gbc.insets.bottom = 5 ;
		gbc.insets.left = 10 ;
		gbc.insets.right = 5 ;
		gbc.weightx = 100 ;
		gbc.weighty = 100 ;
		add( scrollPane , gbc , 0 , 0 , 2 , 1 ) ;
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
		gbc.gridx = x ;
		gbc.gridy = y ;
		gbc.gridwidth = w ;
		gbc.gridheight = h ;
		add( c , gbc ) ;
	}

	/**
	 * Sets the text in the panel. Uses <code>SpNote.isObserveInstruction() </code> to locate Observer Note.
	 * 
	 * @param sp
	 *            the SpItem tree which may contain an Observer Note.
	 */
	public static void setNote( SpItem sp )
	{
		if( sp != null )
		{
			ArrayList<String> notes = new ArrayList<String>() ;
			ArrayList<String> styles = new ArrayList<String>() ;
	
			Vector<SpItem> noteVector = SpTreeMan.findAllItems( sp , SpNote.class.getName() ) ;
			for( SpItem item : noteVector )
			{
				SpNote thisNote = ( SpNote )item ;
				if( thisNote.isObserveInstruction() )
				{
					String[] instructions = thisNote.getInstructions() ;
					if( instructions != null )
					{
						for( int i = 0 ; i < instructions.length ; i++ )
						{
							notes.add( instructions[ i ] + "\n" ) ;
							styles.add( "bold" ) ;
						}
					}
					notes.add( "\n" + thisNote.getNote() + "\n" ) ;
					styles.add( "regular" ) ;
				}
			}
			initStyles() ;
	
			Document doc = textPanel.getDocument() ;
			try
			{
				doc.remove( 0 , doc.getLength() ) ;
				for( int i = 0 ; i < notes.size() ; i++ )
					doc.insertString( doc.getLength() , notes.get( i ) , textPanel.getStyle( styles.get( i ) ) ) ;
			}
			catch( Exception ex )
			{
				System.out.println( "Could not insert observer notes" ) ;
			}
			textPanel.setCaretPosition( 0 ) ;
			textPanel.repaint() ;
		}
	}

	private static void initStyles()
	{
		if( textPanel == null )
			textPanel = new JTextPane() ;

		StyleContext styleContext = StyleContext.getDefaultStyleContext() ;
		Style def = styleContext.getStyle( StyleContext.DEFAULT_STYLE ) ;

		Style regular = textPanel.addStyle( "regular" , def ) ;
		StyleConstants.setFontFamily( def , "SansSerif" ) ;

		Style s = textPanel.addStyle( "bold" , regular ) ;
		StyleConstants.setBold( s , true ) ;
	}
}
