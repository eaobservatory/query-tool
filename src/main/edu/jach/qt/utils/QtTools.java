/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
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

package edu.jach.qt.utils;

import gemini.sp.SpInsertData;
import gemini.sp.SpTreeMan;
import gemini.sp.SpObs;
import gemini.sp.SpItem;
import gemini.sp.obsComp.SpDRObsComp;
import gemini.sp.obsComp.SpInstObsComp;
import gemini.sp.obsComp.SpTelescopeObsComp;

import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.File;

import java.util.Properties;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.TimeZone;

import java.text.SimpleDateFormat;

import gemini.util.JACLogger;

/**
 * A set of static tools for the QT-OM
 *
 * Created: Wed Sep 19 11:09:17 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
public class QtTools {
    static final JACLogger logger = JACLogger.getLogger(QtTools.class);

    /**
     * Reads a configuration file and set up configuration.
     *
     * It is done by putting things into java system properties
     * which is program-system-wide visible.
     *
     * COPIED FROM ORAC3-OM suite
     *
     * @param String args
     * @return none
     * @throws none
     */
    public static void loadConfig(String filename) {
        try {
            String line_str;
            FileInputStream is = new FileInputStream(filename);
            BufferedReader d = new BufferedReader(new InputStreamReader(is));

            Properties temp = System.getProperties();
            int lineno = 0;

            while ((line_str = d.readLine()) != null) {
                lineno++;

                if (line_str.length() > 0) {
                    if (line_str.charAt(0) == '#') {
                        continue;
                    }

                    try {
                        int colonpos = line_str.indexOf(":");
                        temp.put(line_str.substring(0, colonpos).trim(),
                                line_str.substring(colonpos + 1).trim());

                    } catch (IndexOutOfBoundsException e) {
                        logger.fatal("Problem reading line " + lineno + ": "
                                + line_str);
                        d.close();
                        is.close();
                        System.exit(1);
                    }
                }
            }

            d.close();
            is.close();

        } catch (IOException e) {
            logger.error("File error: " + e);
        }
    }

    /**
     * Fix up deferred observations to include items from the original MSB.
     *
     * Items currently being sought: Instrument, Target list and DR recipes.
     *
     * @param obs item to be cloned.
     * @return cloned item.
     */
    public static SpItem fixupDeferredObs(SpItem obs, boolean duplicate) {
        SpItem deferredObs = null;

        if (duplicate) {
            deferredObs = obs.deepCopy();
        } else {
            deferredObs = obs;
        }

        SpInstObsComp inst = SpTreeMan.findInstrument(obs);
        if (inst != null) {
            insert(inst, deferredObs);
        }

        SpTelescopeObsComp obsComp = SpTreeMan.findTargetList(obs);
        if (obsComp != null) {
            insert(obsComp, deferredObs);
        }

        SpDRObsComp recipe = SpTreeMan.findDRRecipe(obs);
        if (recipe != null) {
            insert(recipe, deferredObs);
        }

        return deferredObs;
    }

    /**
     * Clone an item for insertion, and insert.
     *
     * Convenience method.
     *
     * @param insert item to insert.
     * @param insertInto item to insert item into.
     * @return boolean indicating success.
     */
    public static boolean insert(SpItem insert, SpItem insertInto) {
        boolean success = true;
        SpItem clonedItem = insert.deepCopy();
        SpInsertData spid = SpTreeMan.evalInsertInside(clonedItem, insertInto);

        if (spid != null) {
            SpTreeMan.insert(spid);
        } else {
            success = false;
        }

        return success;
    }
}
