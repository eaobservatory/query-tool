package edu.jach.qt.gui ;

import java.io.File ;
import edu.jach.qt.utils.HTMLViewer ;

/**
 * Display the current help information in local browser.
 * The help files are located inthe config directory in the
 * subdirectory Documents.
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
		File tmp = new File( System.getProperty( "qtConfig" ) ) ;
		String dir = tmp.getAbsoluteFile().getParent() ;
		String helpFile = dir + File.separator + "Documents/AboutTheQT.html" ;

		HTMLViewer htmlViewer = new HTMLViewer( null , helpFile ) ;
	}

	public static void main( String[] args )
	{
		HelpPage helpPage = new HelpPage() ;
	}
}
