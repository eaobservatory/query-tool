package edu.jach.qt.gui;

import java.io.StringReader ;
import java.io.File ;
import java.io.IOException ;
import java.util.StringTokenizer ;
import java.util.Vector ;
import javax.swing.JOptionPane ;
import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.DocumentBuilder ;

import org.xml.sax.InputSource ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.parsers.DOMParser;

import org.apache.log4j.Logger;
import org.w3c.dom.Element ;
import org.w3c.dom.Document ;
import org.w3c.dom.NodeList ;
import org.w3c.dom.Node ;
import org.xml.sax.SAXException; 

import edu.jach.qt.utils.OrderedMap ;
import edu.jach.qt.utils.MSBTableModel ;

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

    public static String getChecksum(String xmlString) {
	// Conver string to document
	Document doc;
	String   checksum = null;
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    doc = builder.parse( new InputSource( new StringReader(xmlString) ) );
	}
	catch (Exception e) {
	    return checksum;
	}
	// Get the MSB element
	Element msb = getElement(doc, "SpMSB", 0);
	// Get the attribute checksum
	if (msb != null) {
	    checksum = msb.getAttribute("checksum");
	}
	else {
	    // See if this is an obs which is a MSB
	    Element obs = getElement(doc,"SpObs", 0);
	    if (obs != null) {
		checksum = obs.getAttribute("checksum");
	    }
	}
	if (checksum == null) {
	    logger.error("Unable to determine checksum");
	}
	return checksum;
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

    private static String convertStringToTime(String oldTime) {
	if ( oldTime == null  || oldTime.length() == 0 ) {
	    return "00h00m00s";
	}
	boolean hasHours   = (oldTime.indexOf('h') != -1);
	boolean hasMinutes = (oldTime.indexOf('m') != -1);
	boolean hasSeconds = (oldTime.indexOf('s') != -1);
	StringBuffer  rtn = new StringBuffer(oldTime);
	// Delete the seconds fields and replace other chars with colons
	rtn.deleteCharAt(oldTime.indexOf('s'));
	if ( hasMinutes ) {
	    rtn.setCharAt( oldTime.indexOf('m'), ':');
	}
	else {
	    rtn.insert(0, "00:"); 
	}

	if ( hasHours ) {
	    rtn.setCharAt( oldTime.indexOf('h'), ':');
	}
	else {
	    rtn.insert(0, "00:"); 
	}
	StringTokenizer st = new StringTokenizer (rtn.toString(), ":");
	int index = 1;
	String hh="00";
	String mm="00";
	String ss="00";
	while ( st.hasMoreTokens() ) {
	    String toke = st.nextToken();
	    switch (index) {
	    case 1:
		if ( toke.length() < 2 ) {
		    hh = "0"+toke;
		}
		else {
		    hh = toke;
		}
		break;
	    case 2:
		if ( toke.length() < 2 ) {
		    mm = "0"+toke;
		}
		else {
		    mm = toke;
		}
		break;
	    case 3:
		if ( toke.length() < 2 ) {
		    ss = "0"+toke;
		}
		else {
		    ss = toke;
		}
		break;
	    }
	    index++;
	}
	return hh+"h"+mm+"m"+ss+"s";

    }

    public static OrderedMap getNewModel( Document doc , String tag )
	{
		if( doc == null )
			return null;

		// Get all of the summary nodes
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tag );
		if( rows.getLength() == 0 )
			return null;

		NodeList children ;
		// Create an OrderedMap containing the data for each project
		OrderedMap projectData = new OrderedMap() ;

		// Loop thru all the elements in the document and start populating
		// the new model.
		int rowsLength = rows.getLength() ;
		Element e ;
		String currentProject ;
		MSBTableModel currentModel;
		for( int i = 0 ; i < rowsLength ; i++ )
		{
			e = getElement( doc , tag , i );
			currentProject = ( String )getValue( e , "projectid" );
			// Loop thru all the models to see if there is one we can use
			if( projectData.find( currentProject ) != null )
			{
				currentModel = ( MSBTableModel )projectData.find( currentProject ) ;
			}
			else
			{
				currentModel = new MSBTableModel( currentProject ) ;
				projectData.add( currentProject , currentModel ) ;
			}

			currentModel.bumpIndex() ;
			
			// Now we have the model, loop thru the elements and add these to the model
			children = rows.item( i ).getChildNodes();
			int numberOfChildren = children.getLength() ;
			String name ;
			String value ;
			for( int j = 0 ; j < numberOfChildren ; j++ )
			{
				name = children.item( j ).getNodeName().trim();
				if( name.startsWith( "#" ) )
					continue;
				value = ( String ) getValue( e , name );
				if( "timeest".equals( name ) )
					value = convertStringToTime( value );
				currentModel.insertData( name , value );
			}
		}
		return projectData;
	}

    public static Vector[] getProjectData()
	{
		return getProjectData( System.getProperty( "msbSummary" ) + "." + System.getProperty( "user.name" ) );
	}

    public static Vector [] getProjectData(String xmlFileName) {
	Document  projectDoc = null;
	DOMParser parser     = new DOMParser();
	try {
	    parser.parse(xmlFileName);
	    projectDoc = parser.getDocument();
	}
	catch (IOException ioe) {
	    System.err.println("WARNING: IOException Reading File "+xmlFileName+".");
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

    public static void clearProjectData() {
	File pdf = new File (System.getProperty("msbSummary")+"."+System.getProperty("user.name"));
	pdf.delete();
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

}//end class

