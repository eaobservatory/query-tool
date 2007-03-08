package edu.jach.qt.djava;

import au.gov.aao.drama.DramaMonitor ;
import au.gov.aao.drama.DramaTask ;
import au.gov.aao.drama.Arg ;
import au.gov.aao.drama.DramaException ;
import edu.jach.qt.gui.TelescopeDataPanel;
import ocs.utils.CommandReceiver;

/**
 * <code>CSO_MonResponse</code> This class is used to handle
 * reponses to the monitor messages created by the GetPath Success
 * handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$ 
 */
public class QT_MonResponse extends MonitorResponse {

  
    /**
     * Constructor.
     * @param cr      A CommandReceiver Object
     */
  public QT_MonResponse(CommandReceiver cr) {
    super(cr);
  }

    /**
	 * This function is invoked when a monitored parameter changes.
	 * This is the core of parameter monitoring.
	 * @param monitor     A DramaMonoitor Object
	 * @param task        A DramaTask Object
	 * @param name        The name of the DRAMA value
	 * @param value       The value associated with the name.
	 * @exception         DramaException if the task fails.
	 */
	public void Changed( DramaMonitor monitor , DramaTask task , String name , Arg value ) throws DramaException
	{
		if( name.equals( "CSOTAU" ) )
		{
			logger.info( "Tau update: " + value.RealValue( name ) );
			TelescopeDataPanel.setTau( value.RealValue( name ) );
		}
		else if( name.equals( "CSOSRC" ) )
		{
			logger.info( "CSO Source updated to " + value.StringValue( name ) );
			TelescopeDataPanel.setTauTooltip( value.StringValue( name ) );
		}
		else if( name.equals( "AIRMASS" ) )
		{
			logger.info( "AIRMASS update: " + value.RealValue( name ) );
			TelescopeDataPanel.setAirmass( value.RealValue( name ) );
		}
		else
		{
			logger.info( "Unhandled " + name + " update: " + value.RealValue( name ) );
		}
	}
}
