/*
 * Copyright (C) 2010 Science and Technology Facilities Council.
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

package edu.jach.qt.app ;

import java.awt.BorderLayout ;
import java.lang.reflect.InvocationTargetException ;

import javax.swing.JApplet ;
import javax.swing.SwingUtilities ;

import edu.jach.qt.gui.QtFrame ;
import edu.jach.qt.gui.WidgetDataBag ;

import jsky.app.ot.OtCfg ;
import jsky.app.ot.VersionSelector ;

@SuppressWarnings( "serial" )
public class QtApplet extends JApplet
{
	public void init()
	{
		VersionSelector.checkVersions() ;
		try
		{
			OtCfg.init() ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}

		System.setProperty( "msbServer" , "http://omp-private.jach.hawaii.edu/cgi-bin/msbsrv.pl" ) ;
		String bigTelescope = System.getProperty( "TELESCOPE" ) ;
		if( bigTelescope == null || bigTelescope.equals( "" ) )
			System.setProperty( "TELESCOPE" , System.getProperty( "telescope" ) ) ;
		String telescope = System.getProperty( "TELESCOPE" ) ;
		String widgetFile = "qt" + telescope.substring( 0 , 1 ).toUpperCase() + telescope.substring( 1 ).toLowerCase() + "Widget.conf" ;
		System.setProperty( "widgetFile" , widgetFile ) ;
		System.setProperty( "qtConfig" , "qtSystem.conf." + telescope.toLowerCase() ) ;
		System.setProperty( "telescopeConfig" , "telescopedata.xml" ) ;
		System.setProperty( "queryTimeout" , "15" ) ;

		WidgetDataBag wdb = new WidgetDataBag() ;
		Querytool qt = new Querytool( wdb ) ;
		final QtFrame qtf = new QtFrame( wdb , qt ) ;

		try
        {
	        SwingUtilities.invokeAndWait( new Runnable()
	        {
	        	public void run()
	        	{
	        		setLayout( new BorderLayout() ) ;
	        		add( qtf.topPanel , BorderLayout.NORTH ) ;
	        		add( qtf.tablePanel , BorderLayout.CENTER ) ;
	        	}
	        } ) ;
        }
        catch( InterruptedException e )
        {
	        e.printStackTrace() ;
        }
        catch( InvocationTargetException e )
        {
	        e.printStackTrace() ;
        }
	}
}
