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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;

import gemini.sp.SpItem;
import gemini.util.JACLogger;

import edu.jach.qt.utils.FileUtils;

/**
 * This class is a base class and should be extended to execute MSBs for each
 * telescope.
 */
public class Execute {
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

    private static File successFile = null;

    public File successFile() {
        if (successFile == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(FileUtils.getDeferredDirectoryName());
            buffer.append(".success-");
            buffer.append(nextRandom());
            String filename = buffer.toString();
            successFile = new File(filename);
            buffer = null;
        }

        if (!successFile.exists()) {
            try {
                File parent = successFile.getParentFile();
                if (!parent.canWrite()) {
                    logger.warn("Don't appear to be able to write to "
                            + parent.getAbsolutePath());
                }
                successFile.createNewFile();
                FileUtils.chmod(successFile);

            } catch (IOException ioe) {
                logger.error("Unable to create success file "
                        + successFile.getAbsolutePath());
            }
        }

        return successFile;
    }

    private static File failFile = null;

    public File failFile() {
        if (failFile == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(FileUtils.getDeferredDirectoryName());
            buffer.append(".failure-");
            buffer.append(nextRandom());
            String filename = buffer.toString();
            failFile = new File(filename);
            buffer = null;
        }

        if (!failFile.exists()) {
            try {
                File parent = failFile.getParentFile();

                if (!parent.canWrite()) {
                    logger.warn("Don't appear to be able to write to "
                            + parent.getAbsolutePath());
                }

                failFile.createNewFile();
                FileUtils.chmod(failFile);

            } catch (IOException ioe) {
                logger.error("Unable to create failure file "
                        + failFile.getAbsolutePath());
            }
        }

        return failFile;
    }

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

    protected int executeCommand(String command, byte[] stdout) {
        if (stdout == null) {
            stdout = new byte[1024];
        }

        byte[] stderr = new byte[1024];
        StringBuffer inputBuffer = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        BufferedWriter errorWriter = null;
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

            errorWriter = new BufferedWriter(new FileWriter(failFile()));
            errorWriter.write(errorBuffer.toString());
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

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
