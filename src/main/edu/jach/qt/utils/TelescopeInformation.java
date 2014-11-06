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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import gemini.util.JACLogger;
import gemini.util.ObservingToolUtilities;

/**
 * Read information about a specific telescope.
 *
 * The information, in XML format, is contained in the config file
 * telescopedata.xml. Currently, only latitude and longitude are contained
 * in this file, but the class should be extensible to allow adding of
 * new attributes without changing this class.
 */
public class TelescopeInformation {
    private Hashtable<String, String> data = new Hashtable<String, String>();
    static final JACLogger logger = JACLogger
            .getLogger(TelescopeInformation.class);

    /**
     * Constructor.
     *
     * Requires the System parameters qtConfig and telescopeConfig to be set.
     * In the future this may throw an exception if it can not be constructed.
     * Data is held internally is a <code>Hashtable</code>.
     *
     * @param name The name of the telescope.
     */
    public TelescopeInformation(String name) {
        Document doc = null;

        // Get the name of the current config directory:
        String configDir = System.getProperty("qtConfig");
        int index = configDir.lastIndexOf(File.separatorChar);
        if (index > 0)
            configDir = configDir.substring(0, index) + File.separatorChar;
        else
            configDir = "";

        String configFile = configDir + System.getProperty("telescopeConfig");

        // Now try to build a document from this file
        Reader reader = null;
        final URL url = ObservingToolUtilities.resourceURL(configFile);
        if (url != null) {
            InputStream is;
            try {
                is = url.openStream();
                reader = new InputStreamReader(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File dataFile = new File(configFile);
            if (!dataFile.exists() || !dataFile.canRead())
                logger.error("Telescope data file does not exist:" + configFile);
            try {
                reader = new FileReader(dataFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            char[] chars = new char[1024];
            int readLength = 0;
            StringBuffer buffer = new StringBuffer();
            while ((readLength = reader.read(chars)) != -1)
                buffer.append(chars, 0, readLength);
            reader.close();
            String buffer_z = buffer.toString();

            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.setFeature(
                    "http://apache.org/xml/features/dom/include-ignorable-whitespace",
                    false);
            parser.parse(new InputSource(new StringReader(buffer_z)));

            doc = parser.getDocument();
        } catch (SAXNotRecognizedException snre) {
            logger.error("Unable to ignore white-space text.", snre);
        } catch (SAXNotSupportedException snse) {
            logger.error("Unable to ignore white-space text.", snse);
        } catch (SAXException sex) {
            logger.error("SAX Exception on parse.", sex);
        } catch (IOException ioe) {
            logger.error("IO Exception on parse.", ioe);
        }

        // Now we have the document - start playing...
        NodeList list = doc.getElementsByTagName("Telescope");

        for (int oloop = 0; oloop < list.getLength(); oloop++) {
            String telescope = list.item(oloop).getAttributes()
                    .getNamedItem("name").getNodeValue();
            if (telescope.equalsIgnoreCase(name)) {
                Node telNode = list.item(oloop);
                NodeList children = telNode.getChildNodes();
                for (int iloop = 0; iloop < children.getLength(); iloop++)
                    data.put(children.item(iloop).getNodeName().trim()
                            .toLowerCase(), children.item(iloop)
                            .getFirstChild().getNodeValue().trim());
            }
        }
    }

    /**
     * See if the required information exists in the data.
     *
     * @param key The information required (e.g. latitude).
     * @return <code>true</code> if the information exists ; <code>false</code>
     *         otherwise.
     */
    public boolean hasKey(Object key) {
        return data.containsKey(((String) key).toLowerCase());
    }

    /**
     * Return the value associated with a specific Key.
     *
     * This method will return a <code>String</code>, <code>Double</code> or
     * <code>Integer</code> class dependent on the value. The calling routine
     * is responsible for interpretation.
     *
     * @param key The key for the entry in the <code>Hashtable</code>.
     * @return The value associated with the key as an appropriate
     *         <code>Object</code>
     */
    public Object getValue(Object key) {
        boolean returnInt = false;
        boolean returnDbl = false;
        boolean returnStr = true;

        String thisKey = ((String) key).toLowerCase();
        String value = null;

        if (hasKey(key)) {
            value = data.get(thisKey).toString();
            // Convert this to a char array to work out what we need to return
            // it as...
            char[] datum = value.toCharArray();
            for (int i = 0; i < datum.length; i++) {
                if (Character.isLetter(datum[i])) {
                    // If any of the character is a letter, treat the return as
                    // a String
                    returnStr = true;
                    returnInt = false;
                    returnDbl = false;
                    break;
                } else if (datum[i] == '.') {
                    // If we find a decimal point assume this is a double, but
                    // keep checking in case a letter follows
                    returnStr = false;
                    returnInt = false;
                    returnDbl = true;
                } else if (Character.isDigit(datum[i]) && returnDbl == false) {
                    // If the charaacter is a number and we have not already
                    // assumed that the value is a double,
                    // assume it is an Integer but keep checking in case we have
                    // a string
                    returnStr = false;
                    returnInt = true;
                    returnDbl = false;
                }
            }
        }

        // Return the appropriate type of Object
        if (returnStr)
            return value;
        else if (returnInt)
            return new Integer(value);
        else
            return new Double(value);
    }
}
