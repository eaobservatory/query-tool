package edu.jach.qt.app;

/* Gemini imports */
import gemini.sp.SpItem;

/* QT imports */
import edu.jach.qt.gui.WidgetDataBag ;
import edu.jach.qt.gui.LabeledTextField ;
import edu.jach.qt.gui.LabeledRangeTextField ;
import edu.jach.qt.gui.WidgetPanel ;
import edu.jach.qt.utils.NoSuchParameterException ;
import edu.jach.qt.utils.MsbClient ;
import edu.jach.qt.utils.SimpleMoon ;
import edu.jach.qt.utils.TimeUtils ;

/* Standard imports */
import java.awt.Color;
import java.io.StringWriter ;
import java.util.Hashtable ;
import java.util.ListIterator ;
import java.util.LinkedList ;
import java.util.Enumeration ;
import javax.swing.JToggleButton ;
import javax.swing.JCheckBox ;
import javax.swing.JRadioButton ;
import javax.swing.JComboBox ;
import java.util.StringTokenizer;

/* Miscellaneous imports */
import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl ;
import org.apache.xml.serialize.XMLSerializer ;
import org.apache.xml.serialize.OutputFormat ;
import org.w3c.dom.Document ;
import org.w3c.dom.Element ;
import org.w3c.dom.NodeList ;

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
    private final String ZONE_OF_AVOIDANCE_DISABLED		= "zoa" ;

    private boolean remaining, observability, allocation,_q,zoneofavoidance;
    private String _queue;

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
     * Describe <code>setZoneOfAvoidanceConstraint</code> method here.
     *
     * @param flag a <code>boolean</code> value that sets the state of
     * the zone of avoidance constraint.
     */
    public void setZoneOfAvoidanceConstraint(boolean flag) {
        zoneofavoidance = flag;
        buildXML(bag.getHash());
    }
    
    public void setQueue(String q) {
        if (q != null && q != "") {
            _q = true;
            _queue = q;
        }
        else {
            _q = false;
        }
        buildXML(bag.getHash());
        return;
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


            if (_q) {
                item = doc.createElement("semester");
                item.appendChild( doc.createTextNode(_queue) );
                root.appendChild(item);
            }

            if (observability) {
                item = doc.createElement("disableconstraint");
                item.appendChild( doc.createTextNode(OBSERVABILITY_DISABLED) );
                root.appendChild(item);
            }

            if (allocation) {
                item = doc.createElement("disableconstraint");
                item.appendChild( doc.createTextNode(ALLOCATION_DISABLED) );
                root.appendChild(item);
            }

            if (remaining) {
                item = doc.createElement("disableconstraint");
                item.appendChild( doc.createTextNode(REMAINING_DISABLED) );
                root.appendChild(item);
            }

            if (zoneofavoidance) {
                item = doc.createElement("disableconstraint");
                item.appendChild( doc.createTextNode(ZONE_OF_AVOIDANCE_DISABLED) );
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
                            if ( !abstractButton.getText().startsWith("Any") ) {
                                item.appendChild( doc.createTextNode( abstractButton.getText() ));
                                root.appendChild(item);
                            }
                        }
                    }
                }
                else if (next.equalsIgnoreCase("semesters") ) {
                    for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0);
                            iter.hasNext();
                            iter.nextIndex()) {
                        abstractButton = (JCheckBox) (iter.next());
                        if (abstractButton.isSelected()) {
                            item = doc.createElement("semester");
                            if ( !abstractButton.getText().equalsIgnoreCase("current") ) {
                                item.appendChild( doc.createTextNode( abstractButton.getText() ));
                                root.appendChild(item);
                            }
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
                                moon = "2";
                            }
                            else {
                                moon = "26";
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
                            String tmpCloud = abstractButton.getText().trim();
                            String cloud = "";
                            if ( tmpCloud.equals("Clear")) {
                                cloud = "0";
                            }
                            else if (tmpCloud.equals("Thin")) {
                                cloud = "20";
                            }
                            else if (tmpCloud.equals("Thick")) {
                                cloud = "100";
                            }

                            else {
                                throw (new NoSuchParameterException ("Clouds does not contain element "+tmpCloud));
                            } 

                            item.appendChild(doc.createTextNode(cloud));
                        }
                    }
                }
                else if(next.equalsIgnoreCase("Atmospheric Conditions")) {
                    item = doc.createElement(next);
                    for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
                            iter.hasNext(); 
                            iter.nextIndex()) {

                        logger.debug("ATMOS: "+(String)iter.next());

                    }
                }
                else if (next.equalsIgnoreCase("country")) {
                    item = doc.createElement(next);
                    for (ListIterator iter = ((LinkedList)(ht.get(next))).listIterator(0); 
                            iter.hasNext(); 
                            iter.nextIndex()) {
                        abstractButton = (JRadioButton) (iter.next());
                        if (abstractButton.isSelected()) {
                            if (abstractButton.getText().equalsIgnoreCase("any")) {
                            }
                            else {
                                item.appendChild( doc.createTextNode( abstractButton.getText() ));
                                root.appendChild(item);
                            }
                        }
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
                        {
                            item = doc.createElement("name");
                        }

                        else if ( next.equalsIgnoreCase("project") )
                        {
                            item = doc.createElement("projectid");
                        }

                        else if ( next.equalsIgnoreCase("seeing") )
                        {
                            item = doc.createElement("seeing");
                        }

                        else if ( next.equalsIgnoreCase("tau" ) )
                        {
                            item = doc.createElement("tau");
                        }

                        else if ( next.equalsIgnoreCase("airmass") )
                        {
                            item = doc.createElement("airmass");
                        }
                        else if ( next.equalsIgnoreCase("brightness") ) {
                            item = doc.createElement("sky");
                        }
                        tmpStr = (String)n.nextElement();
                        item.appendChild(doc.createTextNode(tmpStr.trim()));
                        root.appendChild( item );
                    } // end of while ()
                }

                else if (ht.get(next) instanceof LabeledRangeTextField ) {

                    LabeledRangeTextField lrtf = (LabeledRangeTextField) (ht.get(next));
                    String tmpStr;

                    // Temporary and very inefficient code fix.  
                    // Gets over a problem of removing these from the item from the bag.
                    if (lrtf.getLowerText().equals("") &&
                            lrtf.getUpperText().equals(""))
                    {
                        continue;
                    }


                    if ( next.equalsIgnoreCase("duration")) {
                        item = doc.createElement("timeest");
                        item.setAttribute("units","minutes");
                    } 
                    else if (next.equalsIgnoreCase("observation")) {
                        root = processDate(lrtf, doc, root);
                        continue;
                    }
                    else {
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
        } 

        catch ( Exception ex ) {
            logger.error(ex.getMessage(), ex);
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

    private Element processDate (LabeledRangeTextField lrtf,
            Document doc,
            Element root) {
        Element item;
        String tmpStr;

        // Make sure the specified time is in a valid format and 
        // if not, "make it so number 1"
        String time = lrtf.getUpperText();
        StringTokenizer st = new StringTokenizer(time, ":");
        if (st.countTokens() == 1) {
            time = time + ":00:00";
        }
        else if (st.countTokens() == 2) {
            time = time + ":00";
        }

        tmpStr = lrtf.getLowerText()+"T"+time;

        TimeUtils tu = new TimeUtils();
        if ( !lrtf.timerRunning() ) {
            if (tu.isValidDate(tmpStr) ) {
                item = doc.createElement("date");
                tmpStr = tu.convertLocalISODatetoUTC(tmpStr);
                item.appendChild (doc.createTextNode(tmpStr.trim()));
                root.appendChild (item); 
            }
        }
        else {
            // We will use the current date, so set execution to true
        }
        // Recalculate the moon if the user has not overridden the default,
        // otherwise leave it as it is
        if ( WidgetPanel.getMoonPanel() != null && WidgetPanel.getMoonPanel().getBackground() != Color.darkGray ) {
            SimpleMoon moon;
            if ( lrtf.timerRunning() || !tu.isValidDate(tmpStr) ) {
                moon = new SimpleMoon();
            }
            else {
                moon = new SimpleMoon(tmpStr);
            }
            double moonValue = 0;
            if ( moon.isUp() ) {
                moonValue = moon.getIllumination()*100;
            }
            // Delete any existing value and repalce with the new
            NodeList list = root.getElementsByTagName("moon");
            if (list.getLength() != 0) {
                root.removeChild(list.item(0));
            }
            item = doc.createElement("moon");
            item.appendChild( doc.createTextNode(""+moonValue) );
            root.appendChild (item);
        }
    return root;
    }

}// Querytool
