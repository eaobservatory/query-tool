package edu.jach.qt.gui;

import org.w3c.dom.*;

/**
 * <B>XmlUtils</B> is a utility method to get information about a 
 * XML document.  It also retrieve data in an XML document. 
 *
 * @author       : Nazmul Idris: BeanFactory LLC,
 * modified by Mathew Rippa

 * @version      : 1.0
 */
public class XmlUtils {
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
	       if(tagName.equals("remaining"))
		  return (new Integer(s));

	       else if(tagName.equals("obscount"))
		  return (new Integer(s));

	       else if(tagName.equals("priority"))
		  return (new Integer(s));

	       else if(tagName.equals("tagpriority"))
		  return (new Integer(s));

	       else if(tagName.equals("msbid"))
		  return (new Integer(s));

	       return s;
	    }
	 }
      }
      catch(Exception ex){
	 System.out.println( ex );
	 ex.printStackTrace();
      }
        
      return null;

   }

   /**
      For testing purpose, it print out Node list
     
      @param     rows    a Nodelist
   */
   public static void printNodeTypes( NodeList rows ) {
      System.out.println( "\tenumerating NodeList (of Elements):");
      System.out.println( "\tClass\tNT\tNV" );
      //iterate a given Node list
      for( int ri = 0 ; ri < rows.getLength() ; ri++){
	 Node n = (Node)rows.item( ri );
	 if( n instanceof Element) {
	    System.out.print( "\tElement" );
	 }
	 else System.out.print( "\tNode" );
    
	 //print out Node type and Node value
	 System.out.println(
			    "\t"+
			    n.getNodeType() + "\t" +
			    n.getNodeValue()
			    );
      }
      System.out.println();
   }


}//end class

