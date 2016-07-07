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

import gemini.sp.SpItem;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import gemini.util.JACLogger;

import gemini.sp.SpMSB;
import edu.jach.qt.utils.SpQueuedMap;
import edu.jach.qt.utils.FileUtils;

/**
 * Implements the executable method for JCMT.
 *
 * It simply sends either a single deferred observation, or an entire science
 * project to the OCSQUEUE. This is currently only usable for all OCSQUEUE
 * CONFIG based observations.
 *
 * @see edu.jach.qt.gui.Execute Implements <code>Runnable</code>
 */
public class ExecuteJCMT extends Execute {
    static final JACLogger logger = JACLogger.getLogger(ExecuteJCMT.class);
    private static String jcmtDir = null;

    public ExecuteJCMT(SpItem item, boolean isDeferred) {
        super(item, isDeferred);
    }

    private String jcmtDir() {
        if (jcmtDir == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(File.separator);
            buffer.append("jcmtdata");
            buffer.append(File.separator);
            buffer.append("orac_data");
            jcmtDir = buffer.toString();
            buffer = null;
        }

        return jcmtDir;
    }

    /**
     * Translate the given science program file.
     *
     * Returns the name of the queue manifest file returned by the
     * translator, or null on failure.
     */
    private String translate(File file) {
        byte[] stdout = new byte[1024];
        final String translator = System.getProperty("jcmtTranslator");

        if (translator != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(translator);
            buffer.append(" ");
            buffer.append(file.getPath());
            String command = buffer.toString();
            buffer = null;
            int rtn = executeCommand(command, stdout);
            if (rtn != 0) {
                stdout = null;
            }
        } else {
            logger.error("No translation process defined");
            stdout = null;
        }

        file.delete();

        if (stdout == null) {
            return null;
        }

        String fileName = new String(stdout);

        // clear up any whitespace that might give us a false positive
        fileName = fileName.trim();

        String[] split = fileName.split("\n");

        // Tim assures me it *should* be the last entry
        fileName = split[split.length - 1];

        // clean up line
        fileName = fileName.trim();

        return fileName;
    }

    private File convertProgramToXML() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(jcmtDir());
        buffer.append(File.separator);
        buffer.append("Execute-");
        buffer.append(nextRandom());
        buffer.append(".xml");
        String filename = buffer.toString();
        buffer = null;
        File file = new File(filename);

        try {
            final FileWriter writer = new FileWriter(file);
            writer.write(itemToExecute.toXML());
            writer.flush();
            writer.close();
            FileUtils.chmod(file);
        } catch (IOException ioe) {
            logger.error("Error writing translation file "
                    + file.getAbsolutePath());
            file = null;
        }

        return file;
    }

    /**
     * Implementation of the <code>Runnable</code> interface. The success or
     * failure of the file is determined by a file called .success or .failure
     * left in a defined directory when the method ends. Thus it is important to
     * make sure that when this method is run as a thread, the caller joins the
     * thread.
     *
     * If the item is a science project, it overwrites the current contents of
     * the queue. If it is a deferred observation, it is inserted into the queue
     * at the next convinient point. <bold>Note:</bold> This method currently
     * uses hard coded path names for the files and for the commands to execute
     * the queue.
     */
    public boolean run() {
        logger.info("Executing observation " + itemToExecute.getTitle());

        File XMLFile = null;
        String queueFile = null;
        boolean failure = false;

        resetCheckpoint();
        checkpoint("Converting to XML");

        XMLFile = convertProgramToXML();

        checkpoint("XML");
        checkpoint("Translating");

        if (XMLFile != null) {
            queueFile = translate(XMLFile);
        } else {
            failure = true;
        }

        checkpoint("Translated");
        checkpoint("Sending to queue");

        if (queueFile != null) {
            failure = ! sendToQueue(queueFile);
        } else {
            failure = true;
        }

        checkpoint("Sent to queue");
        checkpoint("Success ? " + !failure);

        if (itemToExecute != null && !failure) {
            SpItem obs = itemToExecute;
            SpItem child = itemToExecute.child();

            if (child instanceof SpMSB) {
                obs = child;
            }

            SpQueuedMap.getSpQueuedMap().putSpItem(obs);
        }

        if (failure) {
            successFile().delete();
        } else {
            failFile().delete();
        }

        return failure;
    }
}
