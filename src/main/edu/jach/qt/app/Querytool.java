package edu.jach.qt.app;

import  org.w3c.dom.*;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xerces.dom.DOMImplementationImpl;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.Serializer;
import  org.apache.xml.serialize.SerializerFactory;
import  org.apache.xml.serialize.XMLSerializer;
import  java.io.*;

import edu.jach.qt.gui.WidgetDataBag;
import java.util.*;
import javax.swing.*;
/**
 * Querytool.java
 *
 *
 * Created: Sat Mar 24 12:51:56 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class Querytool implements Observer {

   private static String xmlString;
   private WidgetDataBag bag;

   /**
    * Creates a new <code>Querytool</code> instance.
    *
    * @param bag a <code>WidgetDataBag</code> value
    */
   public Querytool (WidgetDataBag bag) {
      this.bag = bag;
      bag.addObserver(this);
   }

   /**
    * Describe <code>update</code> method here.
    *
    * @param o a <code>Subject</code> value
    */
   public void update(Subject o) {
      if (o == bag) {
	 buildXML(bag.getHash());
      }
   }

   /**
    * Describe <code>buildXML</code> method here.
    *
    * @param ht a <code>Hashtable</code> value
    */
   public void buildXML(Hashtable ht) {
      try {
	 String next = "";
	 Document doc = new DocumentImpl();
	 Element root = doc.createElement("query");
	 Element item, sub;
	 JToggleButton abstractButton;
	 Object obj;

	 //System.out.println("WidgetHash: "+ht.toString());
	 
	 for(Enumeration e = ht.keys(); e.hasMoreElements() ; ) {
	    next = (String) e.nextElement();
	    item = doc.createElement(next);
	    if (next.equals("Instruments")) {
	       for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
		    iter.hasNext(); 
		    iter.nextIndex()) {
		  abstractButton = (JCheckBox) (iter.next());
		  if (abstractButton.isSelected()) {
		     sub = doc.createElement("instrument");
		     sub.appendChild( doc.createTextNode( abstractButton.getLabel() ));
		     item.appendChild(sub);
		  }
	       }
	    }
	    else if(next.equals("Moon")) {
	       for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
		    iter.hasNext(); 
		    iter.nextIndex()) {
		     abstractButton = (JRadioButton) (iter.next());
		     if (abstractButton.isSelected()) {
			item.appendChild( doc.createTextNode(abstractButton.getLabel() ));
		     }
	       }
	    }
	    else if (ht.get(next) instanceof LinkedList ) {
	       for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
		    iter.hasNext(); 
		    iter.nextIndex()) {

		  obj = iter.next();
		  //System.out.println("Object: "+obj.toString());
		  //System.out.println("Object is JComboBox: "+ (obj instanceof JComboBox));
		  if (obj instanceof JComboBox) {
		     String textField = (String) (iter.next());
		     obj = (JComboBox)obj;
		     if(!textField.equals("")) {
		     sub = doc.createElement((String) (((JComboBox)obj).getSelectedItem()) ) ;
		     sub.appendChild( doc.createTextNode(textField));
		     item.appendChild(sub);
		     }
		  }
	       }
	    }
	    else {
	       //
	       item.appendChild( doc.createTextNode((String)ht.get(next)) );
	       //
	    }

	    root.appendChild( item );
	 }
	 doc.appendChild(root);

	 OutputFormat    format  = new OutputFormat( doc );   //Serialize DOM
	 StringWriter  stringOut = new StringWriter();        //Writer will be a String
	 XMLSerializer    serial = new XMLSerializer( stringOut, format );
	 serial.asDOMSerializer();                            // As a DOM Serializer
	 serial.serialize( doc.getDocumentElement() );
	 
	 xmlString = stringOut.toString();
      } catch ( Exception ex ) {
	 ex.printStackTrace();
      }
   }

   public static String getXML() {
      return xmlString;
   }

   public static void printXML() {
      System.out.println( xmlString ); //Spit out DOM as a String
   }

   public boolean XMLisNull() {
      return (xmlString == null);
   }
}// Querytool
