package edu.jach.qt.gui;

import java.io.File;
import edu.jach.qt.utils.HTMLViewer ;

/**
 * Display the current help information in an external browser.
 * The help files are located inthe config directory in the
 * subdirectory Documents.  If the user specified a Browser
 * in the qtSystem config file, that browser will be used for
 * display.  If that fails, or no browser is specified, then
 * this will try to use some others.  Currently, ther supported
 * browsers are:
 *<ul>
 * <li> netscape 
 * <li> opera
 * <li> mosaic
 * <li> xemacs
 * <li> mozilla
 *</ul>
 *
 * @author $Author$
 * @version $Id$
 */
class HelpPage
{
	/**
	 * Constructor.
	 * Attempts to display the help page in a browser.
	 */
	public HelpPage()
	{
		File tmp = new File( System.getProperty( "qtConfig" ) );
		String dir = tmp.getAbsoluteFile().getParent();
		String helpFile = dir + File.separator + "Documents/AboutTheQT.html";
		
		HTMLViewer htmlViewer = new HTMLViewer( null , helpFile ) ;
	}

	public static void main( String[] args )
	{
		HelpPage helpPage = new HelpPage();
	}

}
