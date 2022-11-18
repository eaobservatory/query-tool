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
import gemini.sp.SpInsertData;
import gemini.sp.SpTreeMan;
import gemini.sp.obsComp.SpTelescopeObsComp;
import gemini.util.JACLogger;

/* Standard imports */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import orac.util.SpInputXML;

/**
 * This class returns a <code>Map</code> of calibrations.
 *
 * Each entry in the Map takes the form (String title, SpItem cal),
 * where title is the title of the Observation and cal is calibration
 * observation. Calibration entries are expected to be in the database and
 * belong to a project called "CAL". This project must be unique and ONLY
 * contain calibration observations.
 */
public class CalibrationList {
    private static final JACLogger logger = JACLogger.getLogger(CalibrationList.class);
    private static final String TARGET_PREFIX = "Target Information: ";
    private static final String AND_PREFIX = "AND Folder: ";

    /**
     * Constructor
     */
    private CalibrationList() {
    }

    public static Map<String, List<SpItem>> getCalibrations() {
        Map <String, List<SpItem>> map = new LinkedHashMap<String, List<SpItem>>();

        try {
            String scienceProgramString = MsbClient.fetchCalibrationProgram();
            SpItem spItem = new SpInputXML().xmlToSpItem(scienceProgramString);
            SpProg scienceProgram = null;

            if (spItem instanceof SpProg) {
                scienceProgram = (SpProg) spItem;
            }

            if (scienceProgram != null) {
                pickApart(scienceProgram, map, null, null);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return map;
    }

    private static void pickApart(
            SpItem spItem,
            Map<String, List<SpItem>> map,
            List<SpItem> folder,
            SpSurveyContainer surveyContainer) {
        Enumeration<SpItem> enumeration = spItem.children();
        String telescope = System.getProperty("telescope");

        while (enumeration.hasMoreElements()) {
            SpItem object = enumeration.nextElement();

            if (object instanceof SpAND) {
                folder = new ArrayList<SpItem>();
                String key = ((SpAND) object).getTitle();

                if (key.startsWith(AND_PREFIX)) {
                    key = key.substring(AND_PREFIX.length());
                }

                map.put(key, folder);

            } else if (object instanceof SpMSB && !(object instanceof SpObs)) {
                addMsbToFolder((SpMSB) object, folder, surveyContainer);

            } else if (object instanceof SpSurveyContainer) {
                surveyContainer = (SpSurveyContainer) object;
            }

            pickApart(object, map, folder, surveyContainer);
        }
    }

    private static void addMsbToFolder(SpMSB msb, List<SpItem> folder,
            SpSurveyContainer surveyContainer) {
        if (folder == null) {
            return;
        }

        if (surveyContainer == null) {
            folder.add(msb);
        }
        else {
            // Normally the OMP would override the target with one from the
            // survey container when an individual MSB is fetched.  However
            // since the QT deals with the whole calibration program, we must
            // do that step at this point.  (Having the OMP expand everything
            // would negate any smaller-XML benefit from using survey
            // containers in calibration programs.)
            String title = msb.getTitleAttr();
            int n = surveyContainer.size();

            for (int i = 0; i < n; i ++) {
                SpTelescopeObsComp override = surveyContainer.getSpTelescopeObsComp(i);

                String name = override.getTitle();
                if (name.startsWith(TARGET_PREFIX)) {
                    name = name.substring(TARGET_PREFIX.length());
                }

                String fullTitle = name + " " + title;

                try {
                    SpMSB copy = (SpMSB) msb.deepCopy();

                    // Set the title.
                    copy.setTitleAttr(fullTitle);

                    // Insert the target component.
                    SpInsertData spid = SpTreeMan.evalInsertInside(override, copy);
                    if (spid != null) {
                        SpTreeMan.insert(spid);
                    }

                    folder.add(copy);
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
