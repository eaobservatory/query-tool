package edu.jach.qt.utils;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

/**
 * Read information about a specific telescope.
 * The information, in XML format, is contained in the config file
 * telescopedata.xml.  Currently, only latitude and longitude are contained
 * in this file, but the class should be extensible to allow adding of new 
 * attributes without changing this class.
 *
 * @author    $Author$
 * @version   $Id$
 */
public class TelescopeInformation {
    private Hashtable data = new Hashtable();

    static Logger logger = Logger.getLogger(TelescopeInformation.class);

    /**
     * Constructor.
     * Requires the System parameters qtConfig and telescopeConfig to be
     * set.  In the future this may throw an exception if it can not
     * be constructed.
     * Data is held internally is a <code>Hashtable</code>.
     *
     * @param name     The name of the telescope.
     */
    public TelescopeInformation (String name) {
	Document doc = null;

	// Get the name of the current config directory:
	String configDir = System.getProperty("qtConfig");
	configDir = configDir.substring(0, configDir.lastIndexOf(File.separatorChar));
	
	String configFile = configDir + File.separatorChar +
	    System.getProperty("telescopeConfig");

	// Now try to build a document from this file
	File dataFile = new File(configFile);
	if ( !dataFile.exists() || !dataFile.canRead() ) {
	    logger.error("Telescope data file does not exist:" + configFile);
	}

	try {
	    FileReader reader = new FileReader(dataFile);
	    char [] buffer = new char [(int)dataFile.length()];
	    reader.read(buffer);
	    String buffer_z = new String(buffer);

	    DOMParser parser = new DOMParser();
	    parser.setIncludeIgnorableWhitespace(false);
	    parser.parse(new InputSource(new StringReader(buffer_z)));

	    doc = parser.getDocument();
	} 
	catch (SAXNotRecognizedException snre) {
	    logger.error("Unable to ignore white-space text.", snre);
	}
	catch (SAXNotSupportedException snse) {
	    logger.error("Unable to ignore white-space text.", snse);
	}
	catch (SAXException sex) {
	    logger.error("SAX Exception on parse.", sex);
	}
	catch (IOException ioe) {
	    logger.error("IO Exception on parse.", ioe);
	}

	// Now we have the document - start playing...
	NodeList list = doc.getElementsByTagName("Telescope");
	String value;
	
	for (int oloop = 0; oloop < list.getLength(); oloop++) {
	    String telescope = list.item(oloop).getAttributes().getNamedItem("name").getNodeValue();
	    if (telescope.equalsIgnoreCase(name)) {
		Node telNode = list.item(oloop);
		NodeList children = telNode.getChildNodes();
		for (int iloop=0; iloop < children.getLength(); iloop ++) {
		    data.put(children.item(iloop).getNodeName().trim().toLowerCase(),
			     children.item(iloop).getFirstChild().getNodeValue().trim());
		}
	    }
	}
    }

    // Find out whether the specified key exists in the data
    /**
     * See if the required information exists in the data.
     *
     * @param key         The information required (e.g. latitude).
     * @return            <code>true</code> if the information exists;
     *                    <code>false</code> otherwise.
     */
    public boolean hasKey(Object key) {
	return data.containsKey(((String)key).toLowerCase());
    }

    // get the value associated with a specific key
    /**
     * Return the value associated with a specific Key.  this method
     * will return a <code>String</code>, <code>Double</code> or
     * <code>Integer</code> class depndent on the value.  The calling 
     * routine is responsible for interpretation.
     *
     * @param key    The key for the entry in the <code>Hashtable</code>.
     * @return       The value associated with the key as an appropriate
     *               <code>Object</code>
     */
    public Object getValue(Object key) {
	boolean returnInt = false;
	boolean returnDbl = false;
	boolean returnStr = true;

	String thisKey = ((String)key).toLowerCase();
	String value=null;
    
	if (hasKey(key)) {
	    value = data.get(thisKey).toString();
	    // Convert this to a char array to work out what we need to return it as...
	    char [] datum = value.toCharArray();
	    for (int i=0; i<datum.length; i++) {
		if (Character.isLetter(datum[i])) {
		    // If any of the character is a letter, treat the return as a String
		    returnStr = true;
		    returnInt = false;
		    returnDbl = false;
		    break;
		}
		else if (datum[i] == '.') {
		    // If we find a decimal point assume this is a double, but keep
		    // checking in case a letter follows
		    returnStr = false;
		    returnInt = false;
		    returnDbl = true;
		}
		else if (Character.isDigit(datum[i]) && returnDbl == false) {
		    // If the charaacter is a number and we have not already
		    // assumed that the value is a double, assume it is an Integer
		    // but keep checking in case we have a string
		    returnStr = false;
		    returnInt = true;
		    returnDbl = false;
		}
	    }
	}

	// Return the appropriate type of Object
	if (returnStr) {
	    return value;
	}
	else if (returnInt) {
	    return new Integer(value);
	}
	else {
	    return new Double(value);
	}
	
    }

    /*
    public static void main (String [] args) {
	System.setProperty("qtConfig", 
			   "/home/dewitt/omp/QT/config/qtSystem.conf");
	System.setProperty("telescopeConfig",
			   "telescopedata.xml");

	TelescopeInformation ti = new TelescopeInformation("ukirt");
	System.out.println("Latitude: "+ti.getValue("latitude"));
	System.out.println("Longitude: "+ti.getValue("Longitude"));
	System.out.println("Elevation: "+ti.getValue("elevation"));
    }
    */
}
