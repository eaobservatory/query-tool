package edu.jach.qt.app;

import java.io.*;
import java.util.*;
import javax.swing.*;

import edu.jach.qt.gui.WidgetDataBag;
import edu.jach.qt.utils.*;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.*;
import org.w3c.dom.Document;

/**
 * The <code>Querytool</code> is main driver for the application side
 * of the OMP-QT.  It <em>Observes</em> the WidgetDataBag class 
 * (or Subject)on the gui side and is called an "observer" class.  As
 * such, it implements the Observer interface.  As an effect, the Querytool 
 * class has knowledge of changes to the primary attribute of the 
 * WidgetDataBag class, simply a Hashtable tracking the state of all 
 * Widgets contained in the bag.

 * This Observer Subject relationship allows instantaneous updates of the 
 * state of the GUI.  The state data is represented in XML and a new  
 * xmlString is written upon a gui state change.  As this seems inefficient,
 * I see it as the only way to get instantaneous results.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * @version 1.0
 */
public class Querytool implements Observer {

   private String xmlString;
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
    * The <code>update</code> method is used to trigger
    * an action if a change is "Observed" in the "Subject".
    * This is the only method mandated by the Observer 
    * interface.
    *
    * @param o a <code>Subject</code> value
    */
   public void update(Subject o) {
      if (o == bag) {
	 buildXML(bag.getHash());
      }
   }

   /**
    * The <code>buildXML</code> method is triggerd by any 
    * Subject update.  If the gui state changes, this method
    * rebuilds the xmlString.
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

   /**
    * The <code>getXML</code> method returns the xmlString.
    *
    * @return a <code>String</code> value
    */
   public String getXML() {
      return xmlString;
   }

   /**
    * The <code>printXML</code> method is a utility to the current
    * xmlString.
    */
   public void printXML() {
      System.out.println( xmlString ); //Spit out DOM as a String
   }

   /**
    * The <code>queryMSB</code> method starts the SOAP client.
    * A successful query will write all MSB Summaries to file.
    */
   public void queryMSB() {
      //QuerytoolClient qtc = new QuerytoolClient();
      MsbClient.queryMSB(xmlString);
   }

   /**
    * The <code>fetchMSB</code> method starts the SOAP client.
    * A successful fetch will start the lower level OMP-OM sequence.
    *
    * @param i an <code>Integer</code> value
    */
   public void fetchMSB(Integer i) {
      //QuerytoolClient qtc = new QuerytoolClient();
      MsbClient.fetchMSB(i);
   }

   /**
    * <code>XMLisNull</code> is a test for a null xmlString.
    *
    * @return a <code>boolean</code> value
    */
   public boolean XMLisNull() {
      return (xmlString == null);
   }

}// Querytool
