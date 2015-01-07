/*
 * Copyright (C) 2005-2014 Science and Technology Facilities Council.
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

import gemini.sp.SpAvTable;
import gemini.sp.SpRootItem;
import gemini.sp.SpProg;
import gemini.sp.SpItem;
import gemini.sp.SpObs;
import gemini.sp.SpTreeMan;

import gemini.sp.obsComp.SpInstObsComp;

import jsky.app.ot.OtFileIO;
import jsky.app.ot.OtCfg;

import java.util.Enumeration;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class UkirtTranslator {
    SpRootItem _root;
    static boolean _useClassic = false;
    static boolean _useQueue = false;
    static File _inputFile = null;
    static String _outDir = null;

    public UkirtTranslator() throws Exception {
        OtFileIO.setXML(System.getProperty("OMP") != null);

        // Load the QT configuration file. This should define
        // the output paths EXEC_PATH and CONF_PATH.
        QtTools.loadConfig(System.getProperty("qtConfig"));

        // If the user has actually specified an output directory,
        // then override these properties in the QT configuration.
        if (_outDir != null) {
            System.setProperty("EXEC_PATH", _outDir);
            System.setProperty("CONF_PATH", _outDir);
        }

        OtCfg.init();
        BufferedReader rdr = new BufferedReader(new FileReader(_inputFile));
        try {
            _root = OtFileIO.fetchSp(rdr);
        } catch (Exception e) {
            System.out.println("Can not access file " + _inputFile.getName());
            System.exit(1);
        }

        prepareForTranslation(_root);

        if (_useQueue) {
            String queueFile = QtTools.createQueueXML(_root);
            System.out.println("Wrote queue file: " + queueFile);
        } else {
            getObservations(_root);
        }
    }

    /**
     * Prepare a science program for translation.
     *
     * When the QT fetches an MSB from the database (for execution), the OMP
     * actually edits the observations in the MSB to include additional
     * information. The lack of this information can cause a problem for the
     * stand-alone UKIRT translator because it needs to be able to handle files
     * which have been saved directly by the OT, and which have therefore not
     * been through the OMP system.
     *
     * This method currently gets the project ID from the science program and
     * inserts it into each observation.
     */
    private void prepareForTranslation(SpRootItem root) {
        // We can only determine the project if we have a proper
        // science program.
        String project = null;
        if (root instanceof SpProg) {
            SpProg prog = (SpProg) root;
            project = prog.getProjectID();
            // Unfortunately getProjectID returns "" instead of
            // null if it is not set.
            if (project.equals("")) {
                project = null;
            }
        }

        // Now iterate over observations and apply any corrections
        // we need to them.
        for (Object obsobj : SpTreeMan.findAllItems(root, SpObs.class.getName())) {
            SpItem obs = (SpItem) obsobj;
            SpAvTable table = obs.getTable();

            // Insert project if we found one in the program and
            // there is not one already present in the
            // observation.
            if ((project != null) && (!table.exists("project"))) {
                table.set("project", project);
            }
        }
    }

    private void getObservations(SpItem root) {
        Enumeration<SpItem> children = root.children();
        String rootTitle = root.getTitleAttr();
        if (rootTitle == null || rootTitle.equals("")) {
            rootTitle = root.typeStr();
        }

        while (children.hasMoreElements()) {
            SpItem child = children.nextElement();

            if (child.getClass().getName().endsWith("SpObs")) {
                doTranslate(rootTitle, (SpObs) child);
            } else if (child.getClass().getName().endsWith("SpMSB")) {
                getObservations(child);
            }
        }
    }

    private void doTranslate(String parentName, SpObs obs) {
        String obsName = obs.getTitleAttr();

        if (obsName == null || obsName.equals("")) {
            obsName = obs.typeStr();
        }

        String tname;

        try {
            SpInstObsComp inst = SpTreeMan.findInstrument(obs);

            if (inst == null) {
                throw new Exception("No instrument selected");
            }

            String instName = inst.type().getReadable();
            tname = QtTools.translate(obs, instName);
        } catch (Exception e) {
            System.out.println("Error translating " + parentName + ":"
                    + obsName);
            e.printStackTrace(System.out);
            return;
        }

        System.out.println("Translation for \"" + parentName + ":" + obsName
                + "\" stored in " + tname);
    }

    public static void main(String[] args) {
        // Should take one argument - the name of the input XML file
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-classic")) {
                    _useClassic = true;
                } else if (args[i].equals("-queue")) {
                    _useQueue = true;
                } else if (args[i].equals("-i")
                        || args[i].equals("--inputFile")) {
                    _inputFile = new File(args[++i]);
                } else if (args[i].equals("-o")
                        || args[i].equals("--outDir")) {
                    _outDir = args[++i];
                } else {
                    System.out.println("Unknown option " + args[i]
                            + " ignored");
                }
            }
        } catch (Exception e) {
            System.out.println("Incorrect usage;"
                    + " ukirtTranslator (-classic) (-queue)"
                    + " (-i inputFile) (-o outputDir)");
            System.exit(1);
        }

        if (_inputFile == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File[] files = tmpDir.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().startsWith("msb.xml")) {
                    if (_inputFile == null) {
                        _inputFile = files[i];
                    } else {
                        if (_inputFile.lastModified()
                                < files[i].lastModified()) {
                            _inputFile = files[i];
                        }
                    }
                }
            }
        }

        try {
            new UkirtTranslator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
