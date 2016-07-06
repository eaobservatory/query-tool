/*
 * Copyright (C) 2002-2010 Science and Technology Facilities Council.
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

import gemini.sp.SpMSB;
import edu.jach.qt.utils.SpQueuedMap;

public class ExecuteUKIRT extends Execute implements Runnable {
    private static final JACLogger logger =
            JACLogger.getLogger(ExecuteUKIRT.class);
    private boolean _useQueue;

    public ExecuteUKIRT(boolean useQueue) throws Exception {
        super();
        _useQueue = useQueue;
    }

    public void run() {
        System.out.println("Starting execution...");

        SpItem itemToExecute;
        if (isDeferred) {
            itemToExecute = DeferredProgramList.getCurrentItem();
            logger.info("Executing observation from deferred list");
        } else {
            if (_useQueue) {
                itemToExecute = ProgramTree.getCurrentItem();
            } else {
                itemToExecute = ProgramTree.getSelectedItem();
            }
            logger.info("Executing observation from Program List");
        }

        if (itemToExecute != null) {
            SpItem obs = itemToExecute;
            SpItem child = itemToExecute.child();

            if (child instanceof SpMSB) {
                obs = child;
            }

            SpQueuedMap.getSpQueuedMap().putSpItem(obs);
        }

        String tname = null;
        if (_useQueue) {
            tname = QtTools.createQueueXML(itemToExecute);
        } else {
            SpItem inst = (SpItem) SpTreeMan.findInstrument(itemToExecute);
            if (inst == null) {
                logger.error("No instrument found");
                successFile().delete();
            } else {
                tname = QtTools.translate(itemToExecute, inst.type()
                        .getReadable());
            }
        }

        // Catch null sequence names - probably means translation failed:
        if (tname == null) {
            logger.error("Translation failed. Please report this!");
            successFile().delete();
            return;
        } else {
            logger.info("Trans OK");
            logger.debug("Translated file is " + tname);
        }

        /*
         * Having successfully run through translation, now try to submit the
         * file to the ukirt instrument task
         */
        if (TelescopeDataPanel.DRAMA_ENABLED) {
            String command;
            StringBuffer buffer = new StringBuffer();
            buffer.append("/jac_sw/omp/QT/bin/");

            if (super.isDeferred) {
                buffer.append("insertOCSQUEUE.sh");
            } else {
                buffer.append("loadUKIRT.sh");
            }

            buffer.append(" ");
            buffer.append(tname);
            command = buffer.toString();
            buffer = null;

            logger.debug("Running command " + command);

            int rtn = executeCommand(command, null);
            if (rtn != 0) {
                logger.error("Error loading UKIRT task");
                successFile().delete();
                return;
            }
        }

        failFile().delete();
    }
}
