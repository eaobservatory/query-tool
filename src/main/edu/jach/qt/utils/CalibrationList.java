package edu.jach.qt.utils;

/* Gemini imports */
import gemini.sp.*;

/* Standard imports */
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import orac.util.*;
import om.util.*;

import edu.jach.qt.gui.*;


/* Miscellaneous imports */
import org.apache.log4j.Logger;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;  

public class CalibrationList {

    private static final String OBSERVABILITY_DISABLED = "observability";
    private static final String REMAINING_DISABLED     = "remaining";
    private static final String ALLOCATION_DISABLED    = "allocation";
    private static final String ALL_DISABLED           = "all";
    public  static final String ROOT_ELEMENT_TAG = "SpMSBSummary";

    private CalibrationList() {
    }

    public static Hashtable getCalibrations(String telescope) {
	SpItem sp;
	Hashtable myCalibrations = new Hashtable();
	Document doc = new DocumentImpl();
	Element root = doc.createElement("MSBQuery");
	Element item;

	item = doc.createElement("disableconstraint");
	item.appendChild( doc.createTextNode(ALL_DISABLED) );
	root.appendChild(item);

	item = doc.createElement("telescope");
	item.appendChild( doc.createTextNode(System.getProperty("telescope")) );
	root.appendChild(item);

	String calibrationProject = telescope.toUpperCase() + "CAL";
	item = doc.createElement("projectid");
	item.appendChild(doc.createTextNode(calibrationProject));
	root.appendChild(item);

	doc.appendChild(root);

	OutputFormat  fmt    = new OutputFormat(doc, "UTF-8", true);
	StringWriter  writer = new StringWriter();
	XMLSerializer serial = new XMLSerializer(writer, fmt);
	try {
	    serial.asDOMSerializer();
	    serial.serialize( doc.getDocumentElement() );
	} catch (IOException ioe) {return null;}

	String result = MsbClient.queryCalibration(writer.toString());

	try {
	    File tmpFile = File.createTempFile("calibration",".xml");
	    FileWriter fw = new FileWriter(tmpFile);
	    fw.write(result);
	    fw.close();

	    doc=null;
	    DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
	    
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    doc = builder.parse( tmpFile );

	    if (doc != null) {
		for (int node=0; node < XmlUtils.getSize( doc , ROOT_ELEMENT_TAG ); node++) {
		    item = XmlUtils.getElement( doc , ROOT_ELEMENT_TAG , node );
		    myCalibrations.put((String) XmlUtils.getValue(item, "title"),
				       new Integer (item.getAttribute("id")));
		}
	    }
	    tmpFile.delete();
	    
	} catch (SAXException sxe) {
	    Exception  x = sxe;
	    if (sxe.getException() != null)
		x = sxe.getException();
	    System.out.println("SAX Error generated during parsing");
	    
	} catch(ParserConfigurationException pce) {
	    System.out.println("ParseConfiguration Error generated during parsing");
	} catch (IOException ioe) {
	    System.out.println("IO Error generated attempting to build Document");
	}
	    
	return myCalibrations;
    }


}
