package edu.jach.qt.app;

/* Gemini imports */
import gemini.sp.SpItem;

/* QT imports */
import edu.jach.qt.gui.*;
import edu.jach.qt.utils.*;

/* Standard imports */
import java.io.*;
import java.util.*;
import javax.swing.*;

/* Miscellaneous imports */
import org.apache.log4j.Logger;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

/**
 * The <code>Querytool</code> is main driver for the application side
 * of the OMP-QT.  It <em>Observes</em> the WidgetDataBag class 
 * (or Subject)on the gui side and is called an "observer" class.  As
 * such, it implements the Observer interface.  As an effect, the Querytool 
 * class has knowledge of changes to the primary attribute of the 
 * WidgetDataBag class, simply a Hashtable tracking the state of all 
 * Widgets contained in the bag.

 * This Observer Subject relationship allows instantaneous updates of
 * the state of the GUI.  The state data is represented in XML and a
 * new _xmlString is written upon a gui state change.  As this seems
 * somewhat inefficient, I see it as the only way to get instantaneous
 * results.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * @version 1.0 */
public class Querytool implements Runnable, Observer {

  static Logger logger = Logger.getLogger(Querytool.class);

  private String _xmlString;
  private WidgetDataBag bag;

  private final String OBSERVABILITY_DISABLED = "observability";
  private final String REMAINING_DISABLED     = "remaining";
  private final String ALLOCATION_DISABLED    = "allocation";

  private boolean remaining, observability, allocation;

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
   * Describe <code>setObservabilityConstraint</code> method here.
   *
   * @param flag a <code>boolean</code> value that sets the state of
   * the observability constraint.
   */
  public void setObservabilityConstraint(boolean flag) {
    observability = flag;
    buildXML(bag.getHash());
  }

  /**
   * Describe <code>setRemainingConstraint</code> method here.
   *
   * @param flag a <code>boolean</code> value that sets the state of
   * the remaining constraint.
   */
  public void setRemainingConstraint(boolean flag) {
    remaining = flag;
    buildXML(bag.getHash());
  }

  /**
   * Describe <code>setAllocationConstraint</code> method here.
   *
   * @param flag a <code>boolean</code> value that sets the state of
   * the allocation constraint.
   */
  public void setAllocationConstraint(boolean flag) {
    allocation = flag;
    buildXML(bag.getHash());
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
   * rebuilds the _xmlString.
   *
   * @param ht a <code>Hashtable</code> value
   */
  public void buildXML(Hashtable ht) throws NullPointerException {
    try {
      String next = "";
      Document doc = new DocumentImpl();
      Element root = doc.createElement("MSBQuery");
      Element item, sub;
      JToggleButton abstractButton;
      Object obj;

      item = doc.createElement("telescope");
      item.appendChild( doc.createTextNode(System.getProperty("telescope")) );
      root.appendChild(item);

      if (observability) {
	item = doc.createElement("disableconstraint");
	item.appendChild( doc.createTextNode(OBSERVABILITY_DISABLED) );
	root.appendChild(item);
      }

      if (allocation) {
	item = doc.createElement("disableconstraint");
	item.appendChild( doc.createTextNode(REMAINING_DISABLED) );
	root.appendChild(item);
      }
      
      if (remaining) {
	item = doc.createElement("disableconstraint");
	item.appendChild( doc.createTextNode(ALLOCATION_DISABLED) );
	root.appendChild(item);
      }
      
      for(Enumeration e = ht.keys(); e.hasMoreElements() ; ) {
	next = ((String)e.nextElement());

	if (next.equalsIgnoreCase("instruments")) {
	  for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
	       iter.hasNext(); 
	       iter.nextIndex()) {
	    abstractButton = (JCheckBox) (iter.next());
	    if (abstractButton.isSelected()) {
	      item = doc.createElement("instrument");
	      item.appendChild( doc.createTextNode( abstractButton.getText() ));
	      root.appendChild(item);
	    }
	  }
	}
	else if(next.equalsIgnoreCase("Moon")) {

	  item = doc.createElement(next);
	  for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
	       iter.hasNext(); 
	       iter.nextIndex()) {
	    abstractButton = (JRadioButton) (iter.next());
	    if (abstractButton.isSelected()) {
	      String tmpMoon = abstractButton.getText().trim();
	      String moon = "";
	      if ( tmpMoon.equals("Dark")) {
		moon = "0";
	      }
	      else if (tmpMoon.equals("Grey")) {
		moon = "1";
	      }
	      else {
		moon = "2";
	      } // end of else
		  
	      item.appendChild(doc.createTextNode(moon));
	    }
	  }
	}
	else if(next.equalsIgnoreCase("Clouds")) {

	  item = doc.createElement("cloud");
	  for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
	       iter.hasNext(); 
	       iter.nextIndex()) {
	    abstractButton = (JRadioButton) (iter.next());
	    if (abstractButton.isSelected()) {
	      String tmpMoon = abstractButton.getText().trim();
	      String cloud = "";
	      if ( tmpMoon.equals("Photometric")) {
		cloud = "0";
	      }
	      else if (tmpMoon.equals("Thin")) {
		cloud = "1";
	      }
	      else {
		cloud = "2";
	      } // end of else
		  
	      item.appendChild(doc.createTextNode(cloud));
	    }
	  }
	}
	else if(next.equalsIgnoreCase("Atmospheric Conditions")) {
	  item = doc.createElement(next);
	  for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
	       iter.hasNext(); 
	       iter.nextIndex()) {

	    //abstractButton = (JRadioButton) (iter.next());
	    logger.debug("ATMOS: "+(String)iter.next());
		
	    //  		if (abstractButton.isSelected()) {
	    //  		  String seeing = abstractButton.getText();
	    //  		  item.appendChild(doc.createTextNode(moon));
	  }
	}
	else if (ht.get(next) instanceof LinkedList ) {
	  item = doc.createElement(next);
	  for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
	       iter.hasNext(); 
	       iter.nextIndex()) {
		
	    obj = iter.next();
	    if (obj instanceof JComboBox) {
	      String textField = (String) (iter.next());
	      obj = (JComboBox)obj;
	      if(!textField.equals("")) {
		sub = doc.createElement((String) (((JComboBox)obj).getSelectedItem())) ;
		sub.appendChild( doc.createTextNode(textField));
		item.appendChild(sub);
	      }
	    }
	  }
	}
	else if (ht.get(next) instanceof LabeledTextField ) {
	  LabeledTextField ltf = (LabeledTextField) (ht.get(next));
	  Enumeration n = ltf.getList().elements();
	  String tmpStr;

	  while (n.hasMoreElements()) {
	    if ( next.equalsIgnoreCase("pi"))
	      item = doc.createElement("name");

	    else if ( next.equalsIgnoreCase("project") )
	      item = doc.createElement("projectid");

	    else if ( next.equalsIgnoreCase("seeing") )
	      item = doc.createElement("seeing");

	    else if ( next.equalsIgnoreCase("tau" ) )
	      item = doc.createElement("tau");

	    else if ( next.equalsIgnoreCase("airmass") )
	      item = doc.createElement("airmass");

	    else if ( next.equalsIgnoreCase("semester") )
	      item = doc.createElement("semester");

	    tmpStr = (String)n.nextElement();
	    item.appendChild(doc.createTextNode(tmpStr.trim()));
	    root.appendChild( item );
	  } // end of while ()
	}

	else if (ht.get(next) instanceof LabeledRangeTextField ) {
	  
	  LabeledRangeTextField lrtf = (LabeledRangeTextField) (ht.get(next));
	  String tmpStr;

	  if ( next.equalsIgnoreCase("duration")) {
	    item = doc.createElement("timeest");
	    item.setAttribute("units","minutes");
	  } else {
	    item = doc.createElement(next);
	  }
	  
	  if ( next.equals("hour")) {
	    item = doc.createElement("ha");
	  } // end of if ()
	  
	  
	  tmpStr = lrtf.getLowerText();
	  sub = doc.createElement("min") ;
	  sub.appendChild( doc.createTextNode(tmpStr.trim().toLowerCase()));
	  item.appendChild(sub);

	
	  tmpStr = lrtf.getUpperText();
	  sub = doc.createElement("max");
	  sub.appendChild(doc.createTextNode(tmpStr.trim().toLowerCase()));
	  item.appendChild(sub);

	  root.appendChild( item );
	}
	
	//  	else if (next.equalsIgnoreCase("photometric")) {
	//  	  item = doc.createElement("cloud");
	//  	  String tmp = (String)ht.get(next);

	//  	  if (tmp.equals("true") ) {
	//  	    item.appendChild( doc.createTextNode("0"));
	//  	  }else {
	//  	    item.appendChild( doc.createTextNode("1"));
	//  	  }
	//  	}
	
	
	else {
	  item = null;
	  throw (new NullPointerException("A widget in the InputPanel has data, but has not been set!"));
	} // end of else
	

	root.appendChild( item );
      }

      doc.appendChild(root);

      OutputFormat    format  = new OutputFormat( doc,"UTF-8",true );   //Serialize DOM
      StringWriter  stringOut = new StringWriter();        //Writer will be a String
      XMLSerializer    serial = new XMLSerializer( stringOut, format );
      serial.asDOMSerializer();                            // As a DOM Serializer
      serial.serialize( doc.getDocumentElement() );
	 
      _xmlString = stringOut.toString();
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }

  /**
   * The <code>getXML</code> method returns the _xmlString.
   *
   * @return a <code>String</code> value
   */
  public String getXML() {
    return _xmlString;
  }

  /**
   * The <code>printXML</code> method is a utility to the current
   * _xmlString.
   */
  public void printXML() {
    logger.debug( _xmlString ); //Spit out DOM as a String
  }

  /**
   * The <code>queryMSB</code> method starts the SOAP client.
   * A successful query will write all MSB Summaries to file.
   */
  public void run() {
    //QuerytoolClient qtc = new QuerytoolClient();
    MsbClient.queryMSB(_xmlString);
  }

  /**
   * The <code>queryMSB</code> method starts the SOAP client.
   * A successful query will write all MSB Summaries to file and 
   * return true.
   */
  public boolean queryMSB() {
    return MsbClient.queryMSB(_xmlString);
  }

  /**
   * The <code>fetchMSB</code> method starts the SOAP client.
   * A successful fetch will start the lower level OMP-OM sequence.
   *
   * @param i an <code>Integer</code> value
   */
  public SpItem fetchMSB(Integer i) throws NullPointerException {

    SpItem spItem = MsbClient.fetchMSB(i);

    if ( spItem == null) {
      throw (new NullPointerException());
    } // end of if ()

    return spItem;
  }

  /**
   * <code>XMLisNull</code> is a test for a null _xmlString.
   *
   * @return a <code>boolean</code> value
   */
  public boolean XMLisNull() {
    return (_xmlString == null);
  }

}// Querytool
