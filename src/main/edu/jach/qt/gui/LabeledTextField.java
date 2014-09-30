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

import java.awt.Color ;
import java.awt.GridLayout ;
import java.awt.event.KeyListener ;
import java.awt.event.KeyEvent ;
import java.util.Vector ;
import java.util.Hashtable ;
import javax.swing.JTextField ;
import javax.swing.JLabel ;
import javax.swing.event.DocumentListener ;
import javax.swing.event.DocumentEvent ;

/**
 * LabeledTextField.java
 * This offers a JTextField with a JLabel packaged up in a JPanel.
 *
 * Created: Thu Mar 22 11:04:49 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 */

@SuppressWarnings( "serial" )
public class LabeledTextField extends WidgetPanel implements KeyListener , DocumentListener
{
	protected JTextField textField ;
	protected JLabel label ;
	protected String name ;

	/**
	 * Creates a new <code>LabeledTextField</code> instance.
	 *
	 * @param parent a <code>WidgetPanel</code> value
	 * @param text a <code>String</code> value for the name of the LabeledTextField
	 */
	public LabeledTextField( Hashtable<String,String> ht , WidgetDataBag wdb , String text )
	{
		super( ht , wdb ) ;
		textField = new JTextField( "" ) ;
		label = new JLabel( text + ": " , JLabel.LEADING ) ;
		setup() ;
	}
	
	public LabeledTextField( Hashtable<String,String> ht , WidgetDataBag wdb , String text , String toolTip )
	{
		super( ht , wdb ) ;
		textField = new JTextField( "" ) ;
		label = new JLabel( text + ": " , JLabel.LEADING ) ;
		if( toolTip != null && toolTip.trim().length() != 0 )
		{
			textField.setToolTipText( toolTip ) ;
			label.setToolTipText( toolTip ) ;
		}
		setup() ;
	}

	private void setup()
	{
		name = label.getText().trim() ;
		textField.setHorizontalAlignment( JTextField.LEFT ) ;
		label.setForeground( Color.black ) ;

		this.setLayout( new GridLayout() ) ;
		add( label ) ;

		add( textField ) ;
		textField.getDocument().addDocumentListener( this ) ;
		textField.addKeyListener( this ) ;
	}

	/**
	 * Get the abbreviated name of the current <code>LabeledTextField</code>.
	 * The abbreviated name is the name on the interface up to, but not including
	 * any white space characters.
	 * @return The abbreviated name of the text field.
	 */
	public String getName()
	{
		return abbreviate( name ) ;
	}

	/**
	 * Get the text currently contained in the <code>LabeledTextField</code>.
	 * @return The information in the text field.
	 */
	public String getText()
	{
		return textField.getText() ;
	}

	/**
	 * Set the text currently contained in the <code>LabeledTextField</code>.
	 * @param val  The value to be set.
	 */
	public void setText( String val )
	{
		textField.setText( val ) ;
	}

	/*
	 * Get the current text as a <code>Vector</code> list.
	 * A list may be given by typing comma separated values.
	 * @return A <code>Vector</code> containing each item in the list.
	 */
	public Vector<String> getList()
	{
		String tmpStr = getText() ;
		Vector<String> result = new Vector<String>() ;
		String[] split = tmpStr.split( "," ) ;
		int index = 0 ;
		
		while( index < split.length )
			result.add( split[ index++ ] ) ;
		return result ;
	}

	/**
	 * The <code>insertUpdate</code> adds the current text to the
	 * WidgetDataBag.  All observers are notified.
	 *
	 * @param e a <code>DocumentEvent</code> value
	 */
	public void insertUpdate( DocumentEvent e )
	{
		setAttribute( name.substring( 0 , name.length() - 1 ) , this ) ;
	}

	/**
	 * The <code>removeUpdate</code> adds the current text to the
	 * WidgetDataBag.  All observers are notified.
	 *
	 * @param e a <code>DocumentEvent</code> value
	 */
	public void removeUpdate( DocumentEvent e )
	{
		setAttribute( name.substring( 0 , name.length() - 1 ) , this ) ;
	}

	/**
	 * The <code>changedUpdate</code> method is not implemented.
	 *
	 * @param e a <code>DocumentEvent</code> value
	 */
	public void changedUpdate( DocumentEvent e ){}

	// implementation of java.awt.event.KeyListener interface

	/**
	 * Implementation of <code>java.awt.event.KeyListener</code> interface.
	 * If a CR character is pressed a Search is performed.
	 * @param param1 A <code>KeyEvent</code> object.
	 */
	public void keyTyped( KeyEvent param1 )
	{
		// TODO: implement this java.awt.event.KeyListener method

		if( param1.getKeyChar() == 10 )
			InfoPanel.searchButton.doClick() ;
	}

	/**
	 * Implementation of <code>java.awt.event.KeyListener</code> interface.
	 * @param param1 A <code>KeyEvent</code> object.
	 *
	 */
	public void keyPressed( KeyEvent param1 )
	{
	// TODO: implement this java.awt.event.KeyListener method
	}

	/**
	 * Implementation of <code>java.awt.event.KeyListener</code> interface.
	 * @param param1 A <code>KeyEvent</code> object.
	 *
	 */
	public void keyReleased( KeyEvent param1 )
	{
	// TODO: implement this java.awt.event.KeyListener method
	}

}// LabeledTextField
