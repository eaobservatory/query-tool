/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
 * Copyright (C) 2016 East Asian Observatory.
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

package edu.jach.qt.gui;

import edu.jach.qt.utils.QtTools;
import gemini.sp.SpItem;
import gemini.sp.SpTreeMan;
import gemini.util.JACLogger;

public abstract class ExecuteUKIRT extends Execute {
    private static final JACLogger logger =
            JACLogger.getLogger(ExecuteUKIRT.class);
    protected boolean useQueue;

    public ExecuteUKIRT(SpItem item, boolean isDeferred,
            boolean useQueue) {
        super(item, isDeferred);
        this.useQueue = useQueue;
    }

    /**
     * Implementation for the SwingWorker abstract class.
     *
     * Returns true on success.
     */
    @Override
    public Void doInBackground() throws SendToQueueException {
        System.out.println("Starting execution...");

        String tname = null;
        if (useQueue) {
            tname = QtTools.createQueueXML(itemToExecute);
        } else {
            SpItem inst = (SpItem) SpTreeMan.findInstrument(itemToExecute);
            if (inst == null) {
                logger.error("No instrument found");
                throw new SendToQueueException("No instrument found.");
            } else {
                tname = QtTools.translate(itemToExecute, inst.type()
                        .getReadable());
            }
        }

        // Catch null sequence names - probably means translation failed:
        if (tname == null) {
            logger.error("Translation failed. Please report this!");
            throw new SendToQueueException("Translation failed.");
        }

        logger.info("Trans OK");
        logger.debug("Translated file is " + tname);

        /*
         * Having successfully run through translation, now try to submit the
         * file to the ukirt instrument task
         */
        sendToQueue(tname);

        addToQueuedMap(itemToExecute);

        return null;
    }
}
