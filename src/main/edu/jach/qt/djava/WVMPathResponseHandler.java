/*
 * Copyright (C) 2012 Science and Technology Facilities Council.
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
import ocs.utils.CommandReceiver;

/**
 * <code>WVMPathResponseHandler</code> This class is used to handle responses to
 * the GetPath() method.
 */
public class WVMPathResponseHandler extends CSOPathResponseHandler {
    public WVMPathResponseHandler(DramaPath p, CommandReceiver cr) {
        super(p, cr);
    }

    /**
     * Success is invoked when we have completed the get path operation.
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

        String[] params = new String[]{"DYN_STATE"};

        // Start the monitor operation.
        DramaMonitor Monitor = new DramaMonitor(path, new QT_MonResponse(cr),
                true, params);

        // We have sent a new message, so return true.
        return true;
    }
}
