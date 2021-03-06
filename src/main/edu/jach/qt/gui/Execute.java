/*
 * Copyright (C) 2002-2013 Science and Technology Facilities Council.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import gemini.sp.SpItem;
import gemini.sp.SpMSB;
import gemini.util.JACLogger;
import gemini.util.SelectableDialog;

import edu.jach.qt.utils.FileUtils;
import edu.jach.qt.utils.SpQueuedMap;

/**
 * This class is a base class and should be extended to execute MSBs for each
 * telescope.
 */
public abstract class Execute extends SwingWorker<Void, Void> {
    /**
     * Item to be executed.
     */
    protected SpItem itemToExecute;

    /**
     * Indicates whether a observation is from the deferred list or the project
     * list.
     */
    protected boolean isDeferred = false;
    private static Random random = new Random();
    private static final JACLogger logger = JACLogger.getLogger(Execute.class);

    /**
     * Default constructor.
     */
    public Execute(SpItem item, boolean isDeferred) {
        itemToExecute = item;
        this.isDeferred = isDeferred;
    }

    /**
     * Implementation for the SwingWorker abstract class.
     *
     * This method is called in the Swing event handling thread after
     * the doInBackground method is complete.
     *
     * Retrieves the status (based on lack of exceptions) and shows
     * an error message on failure.
     *
     * Then calls doAfterExecute with the boolean success value.
     */
    @Override
    protected final void done() {
        boolean success = false;
        // Prepare StringBuffer to contain informational messages in case
        // of error.
        StringBuffer messageBuffer = new StringBuffer(
            "Failed to send observation for execution:\n");

        try {
            get();
            success = true;
        }
        catch (InterruptedException e) {
            logger.error("Execution thread interrupted");
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof SendToQueueException) {
                    // Caught our expected exception class: show the message.
                    logger.error("Caught SendToQueueException");
                    messageBuffer.append(cause.getMessage());
                }
                else {
                    // Caught unexpected exception: show full toString info.
                    logger.error("Caught unexpected exception:" +
                                 cause.toString());
                    messageBuffer.append("Unexpected error:\n");
                    messageBuffer.append(cause.toString());
                }
            }
            else {
                // Null cause: show full toString info of original exception.
                logger.error("Caught ExecutionException with null cause:" +
                             e.toString());
                messageBuffer.append("Null error received:\n");
                messageBuffer.append(e.toString());
            }
        }

        if (! success) {
            logger.error("Execution failed - Check log messages");

            SelectableDialog.showError(
                    "Send to Queue failed",
                    messageBuffer.toString());
        }

        doAfterExecute(success);
    }

    /**
     * Method to be called after execution is complete.
     *
     * Subclasses should override this to implement any behavior required
     * after an observation has been sent to the queue.  This is called in
     * the Swing event handling thread.
     */
    protected abstract void doAfterExecute(boolean success);


    /**
     * Send observations to the queue.
     *
     * Returns true on success.
     */
    protected void sendToQueue(String fileName) throws SendToQueueException {
        File file = new File(fileName);

        if (! (file.exists() && file.canRead())) {
            logger.error("The following file does not appear to be available : "
                    + file.getAbsolutePath());
            throw new SendToQueueException("Queue manifest file not available.");
        }
        else if (! TelescopeDataPanel.DRAMA_ENABLED) {
            logger.info("Problem sending to queue: DRAMA not enabled");
            throw new SendToQueueException("DRAMA is not enabled.");
        }
        else {
            StringBuffer buffer = new StringBuffer();
            buffer.append(System.getProperty("qtBinDir"));

            if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
                if (isDeferred) {
                    buffer.append("insertJCMTQUEUE.sh");
                } else {
                    buffer.append("loadJCMT.sh");
                }
            } else {
                logger.error("Problem sending to queue: unknown telescope");
                throw new SendToQueueException("Unknown telescope.");
            }

            buffer.append(" ");
            buffer.append(fileName);

            String command = buffer.toString();

            logger.debug("Running command " + command);
            try {
                executeCommand(command);
            }
            catch (SendToQueueException e) {
                logger.error("Problem sending to queue: command failed");
                throw new SendToQueueException(
                    "Send to queue command failed: " + e.getMessage());
            }
        }
    }

    /**
     * Add the given SpItem to the SpQueuedMap.
     *
     * Does nothing if the given item is null.  If it has a SpMSB child then
     * that child is added to the map instead.
     */
    protected static void addToQueuedMap(SpItem obs) {
        if (obs != null) {
            SpItem child = obs.child();

            if (child instanceof SpMSB) {
                obs = child;
            }

            SpQueuedMap.getSpQueuedMap().putSpItem(obs);
        }
    }

    protected String executeCommand(String command)
            throws SendToQueueException {
        StreamReader stdout;
        StreamReader stderr;

        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(command);
            stdout = new StreamReader(p.getInputStream());
            stderr = new StreamReader(p.getErrorStream());
            (new Thread(stdout)).start();
            (new Thread(stderr)).start();

            p.waitFor();
            int rtn = p.exitValue();
            logger.info(command + " returned with exit status " + rtn);
            logger.info("Output from " + command + ": "
                    + stdout.getText());

            if (rtn != 0) {
                logger.error("Error from " + command + ": "
                        + stderr.getText());

                throw new SendToQueueException("Command output: " +
                                               stderr.getText());
            }

        } catch (IOException e) {
            logger.error("Error executing ...", e);
            throw new SendToQueueException("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Exited prematurely...", e);
            throw new SendToQueueException("Command interrupted: " +
                                           e.getMessage());
        }

        return stdout.getText();
    }

    protected long nextRandom() {
        return Math.abs(random.nextLong());
    }

    static long lastTime = 0;

    protected void checkpoint(String log) {
        String entry = "";
        if (log != null) {
            entry = log;
        }

        long time = System.currentTimeMillis();

        long difference = time - lastTime;

        if (lastTime == 0) {
            logger.info(entry + " - " + "Checkpoint reset");
        } else {
            logger.info(entry + " - " + difference
                    + " milliseconds since last checkpoint.");
        }

        lastTime = time;
    }

    protected void resetCheckpoint() {
        lastTime = 0;
    }

    /**
     * Exception class representing errors encountered during the process
     * of sending an observation to the queue for execution.
     */
    protected static class SendToQueueException extends Exception {
        public SendToQueueException(String message) {
            super(message);
        }
    }

    /**
     * Runnable class to read from the given input stream.
     */
    protected static class StreamReader implements Runnable {
        private BufferedReader in;
        private StringBuffer text = new StringBuffer();

        public StreamReader(InputStream in) {
            this.in = new BufferedReader(new InputStreamReader(in));
        }

        public synchronized void run() {
            char[] buff = new char[1024];
            int length;

            try {
                while (true) {
                    length = in.read(buff, 0, 1024);
                    if (length == -1) {
                        break;
                    }
                    text.append(buff, 0, length);
                }

                in.close();
            }
            catch (IOException e) {
            }
        }

        public synchronized String getText() {
            return text.toString();
        }
    }
}
