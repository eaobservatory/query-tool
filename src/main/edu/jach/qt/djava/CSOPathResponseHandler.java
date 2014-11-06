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

package edu.jach.qt.djava;

import au.gov.aao.drama.DramaPath;
import au.gov.aao.drama.DramaMonitor;
import au.gov.aao.drama.DramaTask;
import au.gov.aao.drama.DramaException;
import au.gov.aao.drama.DramaStatus;
import ocs.utils.CommandReceiver;
import gemini.util.JACLogger;

/**
 * <code>CSOPathResponseHandler</code> This class is used to handle responses to
 * the GetPath() method.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
public class CSOPathResponseHandler extends DramaPath.ResponseHandler {
    static JACLogger logger = JACLogger.getRootLogger();
    protected CommandReceiver cr;

    /**
     * Constructor.
     *
     * @param p A DramaPath Object
     * @param cr A CommandReceiver Object
     */
    public CSOPathResponseHandler(DramaPath p, CommandReceiver cr) {
        super(p);
        this.cr = cr;
        logger.debug(logger.getClass().getName());
    }

    /**
     * Sucess is invoked when we have completed the get path operation.
     *
     * @param path A DramaPath Object
     * @param task A DramaTask Object
     * @return <code>true</code> always.
     * @exception DramaException if the monitor task fails.
     */
    public boolean Success(DramaPath path, DramaTask task)
            throws DramaException {
        // Informational message
        logger.info("Got path to task " + path.TaskName() + ".");

        String[] params = new String[]{"CSOSRC", "CSOTAU"};

        // Start the monitor operation.
        DramaMonitor Monitor = new DramaMonitor(path, new QT_MonResponse(cr),
                true, params);

        // We have sent a new message, so return true.
        return true;
    }

    /**
     * Invoked if the GetPath operation fails.
     *
     * @param path A DramaPath Object
     * @param task A DramaTask Object
     * @return <code>false</code> always.
     * @exception DramaException if the monitor task fails.
     */
    public boolean Error(DramaPath path, DramaTask task) throws DramaException {
        DramaStatus status = task.GetEntStatus();
        logger.warn("Failed to get path to task \"" + path + "\"");
        logger.warn("Failed with status - " + status);

        cr.setPathLock(false);

        return false;
    }
}
