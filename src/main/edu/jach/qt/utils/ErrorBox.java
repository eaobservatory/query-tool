/*
 * Copyright 1999 United Kingdom Astronomy Technology Centre, an
 * establishment of the Science and Technology Facilities Council.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
 */

package edu.jach.qt.utils ;

import java.awt.Component ;
import javax.swing.JFrame ;
import javax.swing.JOptionPane ;

/** final public class ErrorBox is to bring up a box
 with a string to show errors
 Please note this is shown in a separate frame
 and there is no error handling treatment here.

 @version 1.0 1st June 1999
 @author M.Tan@roe.ac.uk

 */
@SuppressWarnings( "serial" )
final public class ErrorBox extends JFrame
{
	/**  public ErrorBox(String _m) is
	 the constructor. The class has only one constructor so far.

	 @param   Strong m 
	 @return  none
	 @throws none
	 */

	public ErrorBox( String _m )
	{
		m = _m ;
		JOptionPane.showMessageDialog( this , m , "Error Message" , JOptionPane.ERROR_MESSAGE ) ;
	}

	public ErrorBox( Component parent , String _m )
	{
		m = _m ;
		JOptionPane.showMessageDialog( parent , m , "Error Message" , JOptionPane.ERROR_MESSAGE ) ;
	}

	private String m = new String() ;
}
