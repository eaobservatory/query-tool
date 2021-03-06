/*
 * Copyright (C) 2006-2013 Science and Technology Facilities Council.
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

import gemini.sp.SpItem;
import gemini.sp.SpMSB;
import gemini.sp.SpObs;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * A map that handles the caching of SpItems sent to the queue.
 *
 * Its aim is to flag items that are about to be sent too many times.
 */
public class SpQueuedMap {
    private static SpQueuedMap queuedMap = null;
    protected TreeMap<String, String> treeMap = null;
    protected TreeSet<String> treeSet = null;
    protected TreeSet<String> projectsWithOrFolders = null;

    private SpQueuedMap() {
        treeMap = new TreeMap<String, String>();
        treeSet = new TreeSet<String>();
        projectsWithOrFolders = new TreeSet<String>();
    }

    public static synchronized SpQueuedMap getSpQueuedMap() {
        if (queuedMap == null) {
            queuedMap = new SpQueuedMap();
        }

        return queuedMap;
    }

    public boolean putSpItem(final SpItem item) {
        if (item == null) {
            return false;
        }

        boolean replacement = false;
        final String checksum = msbChecksum(item);

        if (!checksum.equals("")) {
            replacement = treeMap.containsKey(checksum);

            if (((SpMSB) item).getNumberRemaining() < 2) {
                treeMap.put(checksum, "" + System.currentTimeMillis());
            }

            writeChecksumToDisk((SpMSB) item);
            treeSet.add(checksum);

            // Check whether this is in an OR folder and if so
            // record the project ID.
            if (checksum.contains("O")) {
                String project = msbProject(item);

                if (!project.equals("")) {
                    projectsWithOrFolders.add(project);
                }
            }
        }

        return replacement;
    }

    public boolean seen(String checksum) {
        return treeSet.contains(checksum);
    }

    public boolean containsSpItem(SpItem item) {
        if (item == null) {
            return false;
        }

        item = getCorrectItem(item);
        String checksum = msbChecksum(item);

        return treeMap.containsKey(checksum);
    }

    /**
     * Checks whether the given project is in the list of projects for which
     * items from OR folders have been observed.
     */
    public boolean usedOrFolder(String project) {
        return projectsWithOrFolders.contains(project);
    }

    public String containsMsbChecksum(String checksum) {
        if (checksum == null) {
            return null;
        }

        if (treeMap.containsKey(checksum)) {
            return convertTimeStamp(treeMap.get(checksum));
        }

        String onDisk = isChecksumOnDisk(checksum);

        if (onDisk != null) {
            treeMap.put(checksum, onDisk);
        }

        return convertTimeStamp(onDisk);
    }

    private String msbChecksum(final SpItem item) {
        String checksum = "";

        if (item instanceof SpMSB) {
            final SpMSB msb = (SpMSB) item;
            checksum = msb.getChecksum();
            String id = null;
            Vector<SpItem> obses = gemini.sp.SpTreeMan.findAllItems(msb,
                    SpObs.class.getName());

            if (obses.size() != 0) {
                SpItem obs = obses.remove(0);
                id = obs.getTable().get("msbid", 0);
            }

            if (id != null) {
                checksum = id;
            }
        }

        return checksum;
    }

    /**
     * Determine the project ID for this item.
     *
     * This is a version of msbChecksum which finds the project ID instead of
     * the checksum.
     */
    private String msbProject(final SpItem item) {
        String project = "";

        if (item instanceof SpMSB) {
            final SpMSB msb = (SpMSB) item;
            String id = null;
            Vector<SpItem> obses = gemini.sp.SpTreeMan.findAllItems(msb,
                    SpObs.class.getName());

            if (obses.size() != 0) {
                id = obses.remove(0).getTable().get("project", 0);
            }

            if (id != null) {
                project = id;
            }
        }

        return project;
    }

    private SpItem getCorrectItem(SpItem item) {
        SpItem spitem = item;

        if (item instanceof SpObs) {
            final SpItem parent = spitem.parent();

            if (parent instanceof SpMSB) {
                spitem = parent;
            }
        }

        return spitem;
    }

    private boolean writeChecksumToDisk(SpMSB item) {
        boolean success = false;
        String path = getChecksumCachePath();
        String checksum = msbChecksum(item);
        int remaining = item.getNumberRemaining();

        StringBuffer buffer = new StringBuffer();
        buffer.append(path);
        buffer.append(checksum);
        buffer.append("_");
        buffer.append(remaining);
        buffer.append("_");
        buffer.append(System.currentTimeMillis());
        String fileName = buffer.toString();

        File file = new File(fileName);

        try {
            success = file.createNewFile();
        } catch (IOException ioe) {
        }

        return success;
    }

    private String isChecksumOnDisk(String checksum) {
        String success = null;
        String path = getChecksumCachePath();
        File filePath = new File(path);
        FileGlobFilter filter = new FileGlobFilter("^" + checksum
                + "_-?\\d+_\\d*");
        String[] directoryContents = filePath.list(filter);
        int directoryContentsLength = 0;
        if (directoryContents != null)
            directoryContentsLength = directoryContents.length;
        int highestRepeatCount = 0;
        long nearestDate = 0;

        for (int index = 0; index < directoryContentsLength; index++) {
            String[] split = directoryContents[index].split("_");

            try {
                int repeatCount = Integer.parseInt(split[1]);

                if (repeatCount > highestRepeatCount) {
                    highestRepeatCount = repeatCount;
                }

                long date = Long.parseLong(split[2]);

                if (date > nearestDate) {
                    nearestDate = date;
                }
            } catch (NumberFormatException nfe) {
                highestRepeatCount = 0;
            }
        }

        if (directoryContentsLength != 0
                && highestRepeatCount <= directoryContentsLength) {
            success = "" + nearestDate;
        }

        return success;
    }

    String cachePath = null;

    private String getChecksumCachePath() {
        if (cachePath == null) {
            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            calendar.setTimeZone(timeZone);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DATE);

            StringBuffer buffer = new StringBuffer();
            buffer.append(year);
            buffer.append(month);
            buffer.append(day);
            buffer.append(File.separator);
            String date = buffer.toString();

            buffer.delete(0, buffer.length());

            buffer.append(File.separator);
            buffer.append(System.getProperty("telescope").toLowerCase());
            buffer.append("data");
            buffer.append(File.separator);
            buffer.append(System.getProperty("cacheFiles"));
            String cacheFiles = buffer.toString();

            String home = System.getProperty("user.home");

            if (cacheFiles == null || cacheFiles.equals("")) {
                cacheFiles = home;
            }

            buffer.delete(0, buffer.length());

            buffer.append(cacheFiles);

            if (!cacheFiles.endsWith(File.separator)) {
                buffer.append(File.separator);
            }

            buffer.append("QT");
            buffer.append(File.separator);

            String QTCacheFiles = buffer.toString();

            buffer.append(date);
            cachePath = buffer.toString();
            File directory = new File(cachePath);

            if (!directory.exists()) {
                File QTHomeDirectory = new File(QTCacheFiles);
                deleteDirectory(QTHomeDirectory);

                if (!directory.mkdirs()) {
                    System.out.println("Unable to create " + cachePath
                            + " using " + home);
                    cachePath = home;

                    if (!cachePath.endsWith(File.separator)) {
                        cachePath += File.separator;
                    }
                }
            }
        }

        return cachePath;
    }

    private void deleteDirectory(File directory) {
        if (directory.exists() && directory.canWrite()
                && directory.isDirectory()) {
            File[] contents = directory.listFiles();
            int contentsLength = 0;

            if (contents != null) {
                contentsLength = contents.length;
            }

            File temp;

            for (int i = 0; i < contentsLength; i++) {
                temp = contents[i];

                if (temp.isDirectory()) {
                    deleteDirectory(temp);
                }

                temp.delete();
            }

            directory.delete();
        }
    }

    private String convertTimeStamp(String time) {
        String returnString = null;

        try {
            long parsedTime = Long.parseLong(time);
            StringBuffer buffer = new StringBuffer();
            long currentTime = System.currentTimeMillis();
            long difference = currentTime - parsedTime;
            long seconds = difference / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            minutes %= 60;

            if (hours != 0) {
                buffer.append(hours);
                buffer.append(" ");
                buffer.append("hour");

                if (hours > 1) {
                    buffer.append("s");
                }

                buffer.append(" ");
            }

            if (minutes != 0) {
                buffer.append(minutes);
                buffer.append(" ");
                buffer.append("minute");

                if (minutes > 1) {
                    buffer.append("s");
                }

                buffer.append(" ");
            }

            if (buffer.toString().equals("")) {
                buffer.append(seconds);
                buffer.append(" ");
                buffer.append("second");

                if (seconds > 1) {
                    buffer.append("s");
                }

                buffer.append(" ");
            }

            buffer.append("ago");
            returnString = buffer.toString();

        } catch (NumberFormatException nfe) {
        }

        return returnString;
    }

    public void fillCache() {
        String path = getChecksumCachePath();
        File filePath = new File(path);
        FileGlobFilter filter = new FileGlobFilter("^\\w+_-?\\d+_\\d*");
        String[] directoryContents = filePath.list(filter);
        int directoryContentsLength = 0;

        if (directoryContents != null) {
            directoryContentsLength = directoryContents.length;
        }

        for (int index = 0; index < directoryContentsLength; index++) {
            String[] split = directoryContents[index].split("_");
            String checksum = split[0];
            treeSet.add(checksum);
            String timestamp = isChecksumOnDisk(checksum);

            if (timestamp != null) {
                treeMap.put(checksum, timestamp);
            }
        }
    }
}
