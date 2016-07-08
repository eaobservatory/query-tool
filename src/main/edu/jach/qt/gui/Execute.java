/*
 * Copyright (C) 2002-2013 Science and Technology Facilities Council.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;

import gemini.sp.SpItem;
import gemini.sp.SpMSB;
import gemini.util.JACLogger;

import edu.jach.qt.utils.FileUtils;
import edu.jach.qt.utils.SpQueuedMap;

/**
 * This class is a base class and should be extended to execute MSBs for each
 * telescope.
 */
public abstract class Execute extends SwingWorker<Boolean, Void> {
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
     * Retrieves the status (success or failure) and shows an error message
     * on failure.  Then calls doAfterExecute with the boolean success
     * value.
     */
    @Override
    protected final void done() {
        boolean success = false;

        try {
            success = get();
        } catch (Exception e) {
        }

        if (! success) {
            logger.info("Execution failed - Check log messages");

            JOptionPane.showMessageDialog(null,
                    "Failed to send project for execution;"
                            + " check log entries using"
                            + " the View>Log button",
                    "Send to Queue failed",
                    JOptionPane.ERROR_MESSAGE);
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
    protected boolean sendToQueue(String fileName) {
        File file = new File(fileName);
        boolean fileAvailable = file.exists() && file.canRead();

        if (fileAvailable && TelescopeDataPanel.DRAMA_ENABLED) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("/jac_sw/omp/QT/bin/");

            if (System.getProperty("telescope").equalsIgnoreCase("ukirt")) {
                if (isDeferred) {
                    buffer.append("insertOCSQUEUE.sh");
                } else {
                    buffer.append("loadUKIRT.sh");
                }

            } else if (System.getProperty("telescope").equalsIgnoreCase("jcmt")) {
                if (isDeferred) {
                    buffer.append("insertJCMTQUEUE.sh");
                } else {
                    buffer.append("loadJCMT.sh");
                }
            } else {
                logger.error("Problem sending to queue: unknown telescope");
                return false;
            }

            buffer.append(" ");
            buffer.append(fileName);

            String command = buffer.toString();

            logger.debug("Running command " + command);
            int rtn = executeCommand(command, null);

            if (rtn != 0) {
                logger.error("Problem sending to queue: command failed");
                return false;
            }
        } else {
            if (fileAvailable) {
                logger.info("Problem sending to queue: DRAMA not enabled");
            } else {
                logger.error("The following file does not appear to be available : "
                        + file.getAbsolutePath());
            }

            return false;
        }

        return true;
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

    protected int executeCommand(String command, byte[] stdout) {
        if (stdout == null) {
            stdout = new byte[1024];
        }

        byte[] stderr = new byte[1024];
        StringBuffer inputBuffer = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        Runtime rt;
        int rtn = -1;

        try {
            rt = Runtime.getRuntime();
            Process p = rt.exec(command);
            InputStream istream = p.getInputStream();
            InputStream estream = p.getErrorStream();
            int inputLength, errorLength;
            boolean inputFinished = false;
            boolean errorFinished = false;
            inputBuffer.delete(0, inputBuffer.length());
            errorBuffer.delete(0, errorBuffer.length());

            while (!(inputFinished && errorFinished)) {
                if (!inputFinished) {
                    inputLength = istream.read(stdout);

                    if (inputLength == -1) {
                        inputFinished = true;
                        istream.close();
                    } else {
                        String out = new String(stdout).trim();
                        inputBuffer.append(out, 0, out.length());
                    }
                }

                if (!errorFinished) {
                    errorLength = estream.read(stderr);

                    if (errorLength == -1) {
                        errorFinished = true;
                        estream.close();
                    } else {
                        String err = new String(stderr).trim();
                        errorBuffer.append(err, 0, err.length());
                    }
                }
            }

            p.waitFor();
            rtn = p.exitValue();
            logger.info(command + " returned with exit status " + rtn);
            logger.info("Output from " + command + ": "
                    + inputBuffer.toString());

            if (rtn != 0) {
                logger.error("Error from " + command + ": "
                        + errorBuffer.toString());
            }

            // TODO: replace failFile with exception containing the message
            // text from errorBuffer.toString()

        } catch (IOException ioe) {
            logger.error("Error executing ...", ioe);
        } catch (InterruptedException ie) {
            logger.error("Exited prematurely...", ie);
        }

        return rtn;
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
}
