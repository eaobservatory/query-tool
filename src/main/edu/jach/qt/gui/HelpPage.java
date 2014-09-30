/*
 * Copyright (C) 2002-2009 Science and Technology Facilities Council.
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

import java.io.File ;
import edu.jach.qt.utils.HTMLViewer ;

/**
 * Display the current help information in local browser.
 * The help files are located in the configuration directory in the
 * sub directory Documents.
 */
class HelpPage
{
	/**
	 * Constructor.
	 * Attempts to display the help page in a browser.
	 */
	public HelpPage()
	{
		File tmp = new File( System.getProperty( "qtConfig" ) ) ;
		String dir = tmp.getAbsoluteFile().getParent() ;
		String helpFile = dir + File.separator + "Documents/AboutTheQT.html" ;

		new HTMLViewer( null , helpFile ) ;
	}

	public static void main( String[] args )
	{
		new HelpPage() ;
	}
}
