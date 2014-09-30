/*
 * Copyright (C) 2002-2012 Science and Technology Facilities Council.
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

package edu.jach.qt.djava ;

import au.gov.aao.drama.DramaMonitor ;
import au.gov.aao.drama.DramaTask ;
import au.gov.aao.drama.Arg ;
import au.gov.aao.drama.SdsID ;
import au.gov.aao.drama.DramaException ;
import edu.jach.qt.gui.TelescopeDataPanel ;
import ocs.utils.CommandReceiver ;

/**
 * <code>CSO_MonResponse</code> This class is used to handle
 * reponses to the monitor messages created by the GetPath Success
 * handler.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
public class QT_MonResponse extends MonitorResponse
{
	/**
	 * Constructor.
	 * @param cr      A CommandReceiver Object
	 */
	public QT_MonResponse( CommandReceiver cr )
	{
		super( cr ) ;
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
			logger.info( "Tau update: " + value.RealValue( name ) ) ;
			TelescopeDataPanel.setCsoTau(value.RealValue(name));
		}
		else if( name.equals( "CSOSRC" ) )
		{
			logger.info( "CSO Source updated to " + value.StringValue( name ) ) ;
			TelescopeDataPanel.setCsoTauSource(value.StringValue(name));
		}
		else if( name.equals( "AIRMASS" ) )
		{
			logger.info( "AIRMASS update: " + value.RealValue( name ) ) ;
			TelescopeDataPanel.setAirmass( value.RealValue( name ) ) ;
		}
		else if (name.equals("DYN_STATE")) {
                        logger.info("Got DYN_STATE structure...");
                        try {
                                SdsID tau = new SdsID(value, "JCMT_TAU");
                                double[] ary = new double[1];
                                tau.Get(0, ary);
                                logger.info("JCMT tau update: " + ary[0]);
			        TelescopeDataPanel.setWvmTau(ary[0]);
                        }
                        catch (DramaException e) {
                                logger.error("DRAMA error reading JCMT_TAU: " + e.toString());
                        }
                        try {
                                SdsID tautime = new SdsID(value, "JCMT_TAU_TIME");
                                String time = tautime.Get(0);
                                logger.info("JCMT tau time update: " + time);
			        TelescopeDataPanel.setWvmTauTime(time);
                        }
                        catch (DramaException e) {
                                logger.error("DRAMA error reading JCMT_TAU_TIME: " + e.toString());
                        }
		}
		else if( name.equals( "EXIT" ) )
		{
			logger.info( "Received exit request" ) ;
			System.exit( 0 ) ;
		}
		else
		{
			logger.debug( "Unhandled " + name + " update: " + value.RealValue( name ) ) ;
		}
	}
}
