package edu.jach.qt.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.apache.xerces.parsers.DOMParser;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;  

/**
 * <B>XmlUtils</B> is a utility method to get information about a 
 * XML document.  It also retrieve data in an XML document. 
 *
 * @author       : Nazmul Idris: BeanFactory LLC,
 * modified by Mathew Rippa

 * @version      : 1.0
 */
public class XmlUtils {

  static Logger logger = Logger.getLogger(XmlUtils.class);

  
   /**
      Return an Element given an XML document, tag name, and index
     
      @param     doc     XML docuemnt
      @param     tagName a tag name
      @param     index   a index
      @return    an Element
   */
   public static Element getElement( Document doc , String tagName ,
				     int index ){
      //given an XML document and a tag
      //return an Element at a given index
      NodeList rows = doc.getDocumentElement().getElementsByTagName(
								    tagName );
      return (Element)rows.item( index );
   }

    /**
       Return an Element given an XML document, tag name, and index
       
       @param     doc     XML docuemnt
       @param     tagName a tag name
       @param     index   a index
       @return    an Element
    */
    public static Element getElement( Document doc , String tagName ,
				      int index, String selection ){
	//given an XML document and a tag
	//return an Element at a given index
	int selectionCount = -1;
      NodeList rows = doc.getDocumentElement().getElementsByTagName(
								    tagName );
      int count;
      for (count=0; count<rows.getLength(); count++) {
	  Element e = getElement(doc, tagName, count);
	  String projectId = (String)getValue(e, "projectid");
	  if (projectId.trim().equals(selection.trim())) {
	      selectionCount++;
	      if (selectionCount == index) {
		  break;
	      }
	  }
      }
      return (Element)rows.item( count );
    }

   /**
      Return the number of person in an XML document
     
      @param     doc     XML document
      @param     tagName a tag name
      @return    the number of person in an XML document
   */
   public static int getSize( Document doc , String tagName ){
      //given an XML document and a tag name
      //return the number of ocurances 
      NodeList rows = doc.getDocumentElement().getElementsByTagName(tagName);
      return rows.getLength();
   }

   /**
      Return the number of person in an XML document
     
      @param     doc     XML document
      @param     tagName a tag name
      @return    the number of person in an XML document
   */
   public static int getSize( Document doc , String tagName, String includeThis ){
      //given an XML document and a tag name
      //return the number of ocurances 
      int rowCount = 0;
      NodeList rows = doc.getDocumentElement().getElementsByTagName(tagName);
      for (int i=0; i<rows.getLength(); i++) {
	  Element e = getElement(doc, tagName, i);
	  String projectId = (String)getValue(e, "projectid");
	  if (projectId.trim().equals(includeThis.trim())) rowCount++;
      }
      return rowCount;
   }

   /**
      Given a person element, must get the element specified
      by the tagName, then must traverse that Node to get the
      value.
      Step1) get Element of name tagName from e
      Step2) cast element to Node and then traverse it for its
      non-whitespace, cr/lf value.
      Step3) return it!
        
      NOTE: Element is a subclass of Node
      
      @param    e   an Element
      @param    tagName a tag name
      @return   s   the value of a Node 
   */
   public static Object getValue( Element e , String tagName ){
      try{
	 //get node lists of a tag name from a Element
	 NodeList elements = e.getElementsByTagName( tagName );

	 Node node = elements.item( 0 );
	 NodeList nodes = node.getChildNodes();
        
	 //find a value whose value is non-whitespace
	 String s;
	 for( int i=0; i<nodes.getLength(); i++){
            s = ((Node)nodes.item( i )).getNodeValue().trim();
            if(s.equals("") || s.equals("\r")) {
		continue;
            }
	    else {
		return s;
	    }
	 }
      }
      catch(Exception ex){
	logger.error("XmlUtils getValueAt() threw exception", ex);
	//ex.printStackTrace();
      }
        
      return null;

   }

    /**
     * Returns an array of strings representing the columns contained
     * in the MSB summary file.  This file must exist, so the application
     * needs to perform a query before this method can be invoked.
     * @param  summaryFile   The name of the XML file containing the MSB
     *                       summary information.
     * @return               A string array of column names, or null if an 
     *                       error is encountered.
     * @deprecated           Replaced by {@link edu.jach.qt.utils.MsbClient#getColumnNames()}
     */
    public static String [] getColumnNames(String summaryFile) {
	Document doc;
	try {
	    DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
	    //factory.setValidating(true);   
	    //factory.setNamespaceAware(true);
	 
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    doc = builder.parse( new File(summaryFile));
	}
	catch (SAXException sxe) {
	    Exception  x = sxe;
	    if (sxe.getException() != null)
		x = sxe.getException();
	    //x.printStackTrace();
	    logger.error("SAX Error generated during parsing", x);
	    return null;
	} 
	catch(ParserConfigurationException pce) {
	    logger.error("ParseConfiguration Error generated during parsing", pce);
	    //pce.printStackTrace();
	    return null;
	} 
	catch (IOException ioe) {
	    // I/O error
	    logger.error("IO Error generated attempting to build Document", ioe);
	    //ioe.printStackTrace();
	    return null;
	}

	if (doc == null) {
	    JOptionPane.showMessageDialog(null, 
					  "No observations found - unable to start",
					  "No Obs",
					  JOptionPane.ERROR_MESSAGE);
	    logger.error("No observation found - unable to build results table");
	}

	Vector names = new Vector();
	Node element = doc.getElementsByTagName("SpMSBSummary").item(0);
	if (element != null) {
	    NodeList summary = element.getChildNodes();
	    for (int i=0; i< summary.getLength(); i++) {
		String name = summary.item(i).getNodeName().trim();
		if (name.startsWith("#")) {
		    continue;
		}
		names.add((Object)name);
	    }
	}
	Object msbid    = names.remove(names.indexOf("msbid"));
	Object checksum = names.remove(names.indexOf("checksum"));
	names.add(msbid);
	names.add(checksum);
	String [] columns = new String [names.size()]; 
	for (int i=0; i<names.size(); i++) {
	    columns[i] = (String)names.get(i);
	}
	return columns;
    }

   /**
      For testing purpose, it print out Node list
     
      @param     rows    a Nodelist
   */
   public static void printNodeTypes( NodeList rows ) {
      logger.debug( "\tenumerating NodeList (of Elements):");
      logger.debug( "\tClass\tNT\tNV" );
      //iterate a given Node list
      for( int ri = 0 ; ri < rows.getLength() ; ri++){
	 Node n = (Node)rows.item( ri );
	 if( n instanceof Element) {
	    System.out.print( "\tElement" );
	 }
	 else System.out.print( "\tNode" );
    
	 //print out Node type and Node value
	 logger.debug(
		      "\t"+
		      n.getNodeType() + "\t" +
		      n.getNodeValue()
		      );
      }
      logger.debug("");
   }

    public static Vector getNewModel (Document doc, String tag) {
	if (doc == null ) {
	    return null;
	}

	boolean usingNewModel = false;

	// Get all of the summary nodes
	NodeList rows = doc.getDocumentElement().getElementsByTagName(tag);
	if (rows.getLength() == 0) {
	    return null;
	}

	// Get the number of children in the summary - representing the number
	// of columns to display
	NodeList children = rows.item(0).getChildNodes();
	int nColumns = 0;
	for (int i=0; i<children.getLength(); i++) {
	    if (children.item(i).getNodeName().startsWith("#")) {
		continue;
	    }
	    nColumns++;
	}

	// Create a vector containing the data for each project
	Vector projectData = new Vector();

	// Loop thru all the elements in the document and start populating
	// the new model.
	for (int i=0; i<rows.getLength(); i++) {
	    Element e = getElement(doc, tag, i);
	    String currentProject = (String)getValue(e, "projectid");
	    // Loop thru all the models to see if there is one we can use
	    int index;
	    MSBTableModel currentModel;
	    for (index=0; index<projectData.size(); index++) {
		if ( ((MSBTableModel)projectData.elementAt(index)).getProjectId().equals(currentProject) ) {
		    break;
		}
	    }
	    if (index == projectData.size()) {
		// Need a new model
		currentModel = new MSBTableModel (currentProject, nColumns);
		usingNewModel = true;
	    }
	    else {
		// We are adding to an existing model
		currentModel = (MSBTableModel) projectData.elementAt(index);
	    }

	    // Now we have the model, loop thru the elements and add these to the model
	    children = rows.item(i).getChildNodes();
	    int addHere = 0;
	    for (int j=0; j<children.getLength(); j++) {
		String name = children.item(j).getNodeName().trim();
		if (name.startsWith("#")) {
		    continue;
		}
		String value = (String)getValue(e, name);
		currentModel.insertData(addHere, value);
		addHere++;
	    }

	    if (usingNewModel) {
		projectData.add(currentModel);
		usingNewModel = false;
	    }
	}

	return projectData;
    }



    public static Vector [] fillColumnDataVectors(Document doc, String project, String tag) {
	NodeList rows = doc.getDocumentElement().getElementsByTagName(tag);
	int nColumns=0;
	NodeList children = rows.item(0).getChildNodes();
	for (int i=0; i<children.getLength(); i++) {
	    if (children.item(i).getNodeName().startsWith("#")) {
		continue;
	    }
	    nColumns++;
	}
	
	// Initialise each vector and add it to a linked list.
	Vector [] vArray = new Vector [nColumns];
	for (int i=0; i<vArray.length; i++) {
	    vArray[i] = new Vector();
	}

	// Loop thru all the nodes looking for ones with the required project id
	for (int i=0; i<rows.getLength(); i++) {
	    Element e = getElement(doc, tag, i);
	    if (((String)getValue(e, "projectid")).trim().equals(project)) {
		// Get all of the cildren for this element
		children = e.getChildNodes();
		int vCount = 0;
		for (int j=0; j<children.getLength(); j++) {
		    String name = children.item(j).getNodeName().trim();
		    if (name.startsWith("#")) {
			continue;
		    }
		    String value = (String)getValue(e, name);
		    vArray[vCount].add(value);
		    vCount++;
		}
	    }
	}
	return vArray;
    }

    public static Vector [] getProjectData () {

	return getProjectData(System.getProperty("msbSummary")+"."+System.getProperty("user.name"));

// 	SAXHandler saxHandler = new SAXHandler();

// 	SAXParserFactory factory = SAXParserFactory.newInstance();
// 	try {
// 	    SAXParser parser = factory.newSAXParser();
// 	    parser.parse( new File(System.getProperty("msbSummary")+"."+System.getProperty("user.name")), saxHandler);
// 	}
// 	catch (Exception t) {
// 	    t.printStackTrace();
// 	}

// 	Vector projectIds = saxHandler.getProjectIds();
// 	Vector priorities = saxHandler.getPriorities();

// 	// Add a special project "All" to be used for displaying
// 	// all of the returned MSBs
// 	projectIds.add(0, "All");
// 	priorities.add(0, new Integer(0));

// 	Vector [] data = {projectIds, priorities};

// 	return data;
    }

    public static Vector [] getProjectData(String xmlFileName) {
	Document  projectDoc = null;
	DOMParser parser     = new DOMParser();
	try {
	    parser.parse(xmlFileName);
	    projectDoc = parser.getDocument();
	}
	catch (IOException ioe) {
	    System.err.println("IOException Reading File "+xmlFileName+".");
	    ioe.printStackTrace();
	    return null;
	}
	catch (SAXException sxe) {
	    System.err.println("SAXException Reading File "+xmlFileName+".");
	    sxe.printStackTrace();
	    return null;
	}
	Vector projectIds = new Vector();
	Vector priorities = new Vector();
	if (projectDoc != null) {
	    int nElements = getSize(projectDoc, "SpMSBSummary");
	    for (int i=0; i<nElements; i++) {
		Object value = getValue( getElement(projectDoc, "SpMSBSummary", i), "projectid");
		if (!(projectIds.contains(value))) {
		    projectIds.add(value);
		    String priority = (String)getValue( getElement(projectDoc, "SpMSBSummary", i), "priority");
		    StringTokenizer st = new StringTokenizer(priority, ".");
		    priority = st.nextToken();
		    Integer iPriority = new Integer(priority);
		    priorities.add(iPriority);
		}
	    }
	}
	projectIds.add(0, "All");
	priorities.add(0, new Integer(0));
	Vector [] data = {projectIds, priorities};
	return data;
    }


    public static int getProjectCount() {
	return getProjectData()[0].size();
    }

    public static String getProjectAt(int      index) {
	Vector [] data = getProjectData();
	return (String) data[0].elementAt(index);
    }

    public static String getPriorityAt(int      index) {
	// Get the data
	Vector [] data = getProjectData();

	return data[1].elementAt(index).toString();
    }

    public static class SAXHandler extends DefaultHandler {
	private String _currentName;
	private final String _titleElement = "projectid";
	private final String _priority = "priority";
	private boolean getNextPriority = false;
	private Vector ids = new Vector();
	private Vector priorities = new Vector();

	int idCount = 0;

	public void startDocument() {
	    ids.clear();
	    priorities.clear();
	}

	public void startElement(String namespaceURI,
			    String localName,
			    String qName,
			    Attributes attr) {
	    _currentName = qName;
	}

	public void characters (char [] ch, int start, int length) {
	    if (_currentName.equals(_titleElement)) {
		idCount++;
		String s = new String(ch);
		s = s.substring(start, start+length);
		s= s.trim();
		if (s.equals("") || ids.contains(s)) {
		}
		else if (start == 0 || length == 1) {
		    // This was added to trap some random problems
		    // with the SAX parser.  I dont really know
		    // why it happens, but this trap should get
		    // rid of them
		}
		else {
		    ids.add(s);
		    getNextPriority = true;
		}
	    }
	    else if (_currentName.equals(_priority) && getNextPriority == true) {
		String s = new String(ch);
		s = s.substring(start, start+length);
		StringTokenizer st = new StringTokenizer(s, ".");
		s = st.nextToken();
		priorities.add(new Integer(s));
		getNextPriority = false;
	    }
	}

	public Vector getProjectIds() {
	    return ids;
	}

	public Vector getPriorities() {
	    return priorities;
	}
	
    }

    public static class MSBTableModel {
	private String _projectId;
	private Vector [] _columnData;

	public MSBTableModel (String project, int nColumns) {
	    _projectId = project;
	    _columnData = new Vector [nColumns];
	    for (int i=0; i< nColumns; i++) {
		_columnData[i] = new Vector();
	    }
	}

	public  String getProjectId () {
	    return _projectId;
	}

	public  void insertData (int column, Object data) {
	    if (column < 0 || column >= _columnData.length) {
		logger.warn("Error inserting column data into column "+column);
		return;
	    }
	    _columnData[column].add(data);
	}

	public  Vector getColumn(int index) {
	    return _columnData[index];
	}

	public void moveColumnToEnd(int index) {
	    Vector tmp = _columnData[index];
	    for (int i=index+1; i<_columnData.length; i++) {
		_columnData[i-1] = _columnData[i];
	    }
	    _columnData[_columnData.length-1] = tmp;
	}

	public  Object getData(int row, int column) {
	    return _columnData[column].elementAt(row);
	}
    }


}//end class

