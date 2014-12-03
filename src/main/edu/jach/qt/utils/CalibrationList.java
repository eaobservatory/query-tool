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

package edu.jach.qt.utils;

/* Gemini imports */
import gemini.sp.SpProg;
import gemini.sp.SpSurveyContainer;
import gemini.sp.SpAND;
import gemini.sp.SpMSB;
import gemini.sp.SpItem;
import gemini.sp.SpObs;
import gemini.util.JACLogger;

/* Standard imports */
import java.util.Enumeration;

import orac.util.SpInputXML;
import orac.util.OrderedMap;

/**
 * This class returns a <code>OrderedMap</code> of calibrations.
 *
 * Each entry in the OrderedMap takes the form (String title, SpItem cal),
 * where title is the title of the Observation and cal is calibration
 * observation. Calibration entries are expected to be in the database and
 * belong to a project called "CAL". This project must be unique and ONLY
 * contain calibration observations.
 */
public class CalibrationList {
    static final JACLogger logger = JACLogger.getLogger(CalibrationList.class);

    /**
     * Constructor
     */
    private CalibrationList() {
    }

    public static OrderedMap<String, OrderedMap<String, SpItem>> getCalibrations() {
        OrderedMap<String, OrderedMap<String, SpItem>> orderedMap =
                new OrderedMap<String, OrderedMap<String, SpItem>>();

        try {
            String scienceProgramString = MsbClient.fetchCalibrationProgram();
            SpItem spItem = new SpInputXML().xmlToSpItem(scienceProgramString);
            SpProg scienceProgram = null;

            if (spItem instanceof SpProg) {
                scienceProgram = (SpProg) spItem;
            }

            if (scienceProgram != null) {
                orderedMap = pickApart(orderedMap, scienceProgram);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return orderedMap;
    }

    private static OrderedMap<String, SpItem> folder = null;

    private static OrderedMap<String, OrderedMap<String, SpItem>> pickApart(
            OrderedMap<String, OrderedMap<String, SpItem>> orderedMap,
            SpItem spItem) {
        Enumeration<SpItem> enumeration = spItem.children();
        String telescope = System.getProperty("telescope");
        SpItem object;

        while (enumeration.hasMoreElements()) {
            object = enumeration.nextElement();

            if (object instanceof SpAND) {
                SpAND and = (SpAND) object;
                String title = and.getTitle();
                folder = new OrderedMap<String, SpItem>();
                orderedMap.add(title, folder);

            } else if (object instanceof SpObs
                    && "UKIRT".equalsIgnoreCase(telescope)) {
                SpObs obs = (SpObs) object;
                String title = obs.getTitleAttr();
                if (folder != null) {
                    folder.add(title, obs);
                }

            } else if (object instanceof SpMSB && !(object instanceof SpObs)) {
                SpMSB msb = (SpMSB) object;
                String title = msb.getTitleAttr();
                if (folder != null) {
                    folder.add(title, msb);
                }

            } else if (object instanceof SpSurveyContainer) {
                continue;
            }

            orderedMap = pickApart(orderedMap, object);
        }

        return orderedMap;
    }
}
