/*
 * Copyright (C) 2008 Science and Technology Facilities Council.
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

import java.io.FilenameFilter;
import java.io.File;

public class FileExtensionFilter implements FilenameFilter {
    String extension;

    public FileExtensionFilter(String fileExtension) {
        if (fileExtension == null) {
            throw new RuntimeException("You asked for a null file extension.");
        }

        fileExtension = fileExtension.trim();

        if (fileExtension.equals("")) {
            throw new RuntimeException("You asked an empty file extension.");
        }

        if (fileExtension.lastIndexOf(".") > 0) {
            throw new RuntimeException("The file extension you gave contained"
                    + " '.'s other than the initial one.");
        }

        if (!fileExtension.startsWith(".")) {
            extension = "." + fileExtension;
        } else {
            extension = fileExtension;
        }
    }

    public boolean accept(File dir, String name) {
        return (name.endsWith(extension));
    }
}
