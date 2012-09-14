package edu.jach.qt.djava;

import au.gov.aao.drama.DramaPath;
import au.gov.aao.drama.DramaMonitor;
import au.gov.aao.drama.DramaTask;
import au.gov.aao.drama.DramaException;
import ocs.utils.CommandReceiver ;

/**
 * <code>WVMPathResponseHandler</code> This class is used to
 * handle responses to the GetPath() method.
 */
public class WVMPathResponseHandler extends CSOPathResponseHandler
{
	public WVMPathResponseHandler(DramaPath p, CommandReceiver cr) {
		super(p, cr);
	}

	/**
	 * Success is invoked when we have completed the get path operation.
	 * @param path     A DramaPath Object
	 * @param task     A DramaTask Object
	 * @return         <code>true</code> always.
	 * @exception      DramaException if the monitor task fails.
	 */
	public boolean Success(DramaPath path, DramaTask task) throws DramaException {
		// Informational message
		logger.info( "Got path to task " + path.TaskName() + "." ) ;

		String[] params = new String[] { "DYN_STATE" } ;

		// Start the monitor operation.
		DramaMonitor Monitor = new DramaMonitor( path , new QT_MonResponse( cr ) , true , params ) ;

		// We have sent a new message, so return true.
		return true ;
	}
}
