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
