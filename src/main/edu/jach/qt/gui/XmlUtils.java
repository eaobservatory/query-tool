package edu.jach.qt.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;

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


}//end class

