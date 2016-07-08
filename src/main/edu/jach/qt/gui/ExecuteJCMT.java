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

import gemini.sp.SpInsertData;
import gemini.sp.SpItem;
import gemini.sp.SpFactory;
import gemini.sp.SpProg;
import gemini.sp.SpTreeMan;
import gemini.sp.SpType;

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
     * Create dummy science program containing a given item.
     *
     * To invoke the JCMT translator we need to be able to write a complete
     * science program to disk.  This method takes a SpItem and places it
     * in a new program which is returned.
     */
    private static SpProg createDummyProgram(SpItem item) {
        SpProg root = (SpProg) SpFactory.create(SpType.SCIENCE_PROGRAM);

        root.setPI("observer");
        root.setCountry("JAC");
        root.setProjectID("CAL");
        root.setTelescope();
        root.setTitleAttr(item.getTitleAttr());

        SpInsertData spID = SpTreeMan.evalInsertInside(item, root);

        if (spID != null) {
            SpTreeMan.insert(spID);
        }

        return root;
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
        // If this is a deferred observation, then we need to convert the
        // supplied item, which is an SpObs into an SpProg.
        SpItem item;
        if (isDeferred) {
            item = createDummyProgram(itemToExecute);
        } else {
            item = itemToExecute;
        }

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
            writer.write(item.toXML());
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
     * Implementation for the SwingWorker abstract class.
     *
     * If the item is a science project, it overwrites the current contents of
     * the queue. If it is a deferred observation, it is inserted into the queue
     * at the next convinient point. <bold>Note:</bold> This method currently
     * uses hard coded path names for the files and for the commands to execute
     * the queue.
     *
     * Returns true on success.
     */
    @Override
    public Boolean doInBackground() {
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

        return ! failure;
    }
}
