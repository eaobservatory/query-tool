package edu.jach.qt.app ;

/* Gemini imports */
import gemini.sp.SpItem ;

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
import java.awt.Color ;
import java.io.StringWriter ;
import java.util.Hashtable ;
import java.util.ListIterator ;
import java.util.LinkedList ;
import java.util.Enumeration ;
import javax.swing.JToggleButton ;
import javax.swing.JCheckBox ;
import javax.swing.JRadioButton ;
import javax.swing.JComboBox ;
import java.util.StringTokenizer ;

/* Miscellaneous imports */
import org.apache.log4j.Logger ;
import org.apache.xerces.dom.DocumentImpl ;
import org.apache.xml.serialize.XMLSerializer ;
import org.apache.xml.serialize.OutputFormat ;
import org.w3c.dom.Document ;
import org.w3c.dom.Element ;
import org.w3c.dom.NodeList ;

/**
 * The <code>Querytool</code> is main driver for the application side
 * of the OMP-QT.  It generates XML based on changes to the WidgetDataBag class.
 * 
 * Previously an observer pattern was used to update the XML on any UI change.
 * This was completely inefficient and was noted as such at the time, while
 * the basic idea was right, it prevented the QT from appearing when, what
 * might be regarded as a race condition though was really a timing bug, 
 * caused events to get backed up, and then attempts to access the window would
 * freeze all UI code. The code has been changed so that XML updates only take 
 * place when the XMl is required.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * @version 1.0 
 */
public class Querytool implements Runnable
{
	static Logger logger = Logger.getLogger( Querytool.class ) ;
	private String _xmlString ;
	private WidgetDataBag bag ;
	private final String OBSERVABILITY_DISABLED = "observability" ;
	private final String REMAINING_DISABLED = "remaining" ;
	private final String ALLOCATION_DISABLED = "allocation" ;
	private final String ZONE_OF_AVOIDANCE_DISABLED = "zoa" ;
	private boolean remaining , observability , allocation , _q , zoneofavoidance ;
	private String _queue ;
	private String DISABLE_CONSTRAINT = "disableconstraint" ;

	/**
	 * Creates a new <code>Querytool</code> instance.
	 *
	 * @param bag a <code>WidgetDataBag</code> value
	 */
	public Querytool( WidgetDataBag bag )
	{
		this.bag = bag ;
	}

	/**
	 * Describe <code>setObservabilityConstraint</code> method here.
	 *
	 * @param flag a <code>boolean</code> value that sets the state of
	 * the observability constraint.
	 */
	public void setObservabilityConstraint( boolean flag )
	{
		observability = flag ;
	}

	/**
	 * Describe <code>setRemainingConstraint</code> method here.
	 *
	 * @param flag a <code>boolean</code> value that sets the state of
	 * the remaining constraint.
	 */
	public void setRemainingConstraint( boolean flag )
	{
		remaining = flag ;
	}

	/**
	 * Describe <code>setAllocationConstraint</code> method here.
	 *
	 * @param flag a <code>boolean</code> value that sets the state of
	 * the allocation constraint.
	 */
	public void setAllocationConstraint( boolean flag )
	{
		allocation = flag ;
	}

	/**
	 * Describe <code>setZoneOfAvoidanceConstraint</code> method here.
	 *
	 * @param flag a <code>boolean</code> value that sets the state of
	 * the zone of avoidance constraint.
	 */
	public void setZoneOfAvoidanceConstraint( boolean flag )
	{
		zoneofavoidance = flag ;
	}

	public void setQueue( String q )
	{
		_q = ( q != null && q != "" ) ;
		if( _q )
			_queue = q ;
	}

	/**
	 * The <code>buildXML</code> method is triggered by any Subject update. If the gui state changes, this method rebuilds the _xmlString.
	 * 
	 * @param ht
	 *            a <code>Hashtable</code> value
	 */
	private void buildXML( Hashtable<String,Object> ht ) throws NullPointerException
	{
		try
		{
			String next = "" ;
			final Document doc = new DocumentImpl() ;
			Element root = doc.createElement( "MSBQuery" ) ;
			Element item , sub ;
			JToggleButton abstractButton ;
			Object obj ;

			item = doc.createElement( "telescope" ) ;
			item.appendChild( doc.createTextNode( System.getProperty( "telescope" ) ) ) ;
			root.appendChild( item ) ;

			if( _q )
			{
				item = doc.createElement( "semester" ) ;
				item.appendChild( doc.createTextNode( _queue ) ) ;
				root.appendChild( item ) ;
			}

			if( observability )
			{
				item = doc.createElement( DISABLE_CONSTRAINT ) ;
				item.appendChild( doc.createTextNode( OBSERVABILITY_DISABLED ) ) ;
				root.appendChild( item ) ;
			}

			if( allocation )
			{
				item = doc.createElement( DISABLE_CONSTRAINT ) ;
				item.appendChild( doc.createTextNode( ALLOCATION_DISABLED ) ) ;
				root.appendChild( item ) ;
			}

			if( remaining )
			{
				item = doc.createElement( DISABLE_CONSTRAINT ) ;
				item.appendChild( doc.createTextNode( REMAINING_DISABLED ) ) ;
				root.appendChild( item ) ;
			}

			if( zoneofavoidance )
			{
				item = doc.createElement( DISABLE_CONSTRAINT ) ;
				item.appendChild( doc.createTextNode( ZONE_OF_AVOIDANCE_DISABLED ) ) ;
				root.appendChild( item ) ;
			}

			for( final Enumeration<String> e = ht.keys() ; e.hasMoreElements() ; )
			{
				next = e.nextElement() ;

				if( next.equalsIgnoreCase( "instruments" ) )
				{
					for( final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ; iter.hasNext() ; iter.nextIndex() )
					{
						abstractButton = ( JCheckBox )( iter.next() ) ;
						if( abstractButton.isSelected() )
						{
							item = doc.createElement( "instrument" ) ;
							if( !abstractButton.getText().startsWith( "Any" ) )
							{
								item.appendChild( doc.createTextNode( abstractButton.getText() ) ) ;
								root.appendChild( item ) ;
							}
						}
					}
				}
				else if( next.equalsIgnoreCase( "semesters" ) )
				{
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
					{
						abstractButton = ( JCheckBox )( iter.next() ) ;
						if( abstractButton.isSelected() )
						{
							item = doc.createElement( "semester" ) ;
							if( !abstractButton.getText().equalsIgnoreCase( "current" ) )
							{
								item.appendChild( doc.createTextNode( abstractButton.getText() ) ) ;
								root.appendChild( item ) ;
							}
						}
					}
				}
				else if( next.equalsIgnoreCase( "Moon" ) )
				{
					item = doc.createElement( next ) ;
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
					{
						abstractButton = ( JRadioButton )( iter.next() ) ;
						if( abstractButton.isSelected() )
						{
							String tmpMoon = abstractButton.getText().trim() ;
							String moon = "" ;
							if( tmpMoon.equals( "Dark" ) )
								moon = "0" ;
							else if( tmpMoon.equals( "Grey" ) )
								moon = "2" ;
							else
								moon = "26" ;

							item.appendChild( doc.createTextNode( moon ) ) ;
						}
					}
				}
				else if( next.equalsIgnoreCase( "Clouds" ) )
				{
					item = doc.createElement( "cloud" ) ;
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
					{
						abstractButton = ( JRadioButton )( iter.next() ) ;
						if( abstractButton.isSelected() )
						{
							String tmpCloud = abstractButton.getText().trim() ;
							String cloud = "" ;
							if( tmpCloud.equals( "Clear" ) )
								cloud = "0" ;
							else if( tmpCloud.equals( "Thin" ) )
								cloud = "20" ;
							else if( tmpCloud.equals( "Thick" ) )
								cloud = "100" ;
							else
								throw ( new NoSuchParameterException( "Clouds does not contain element " + tmpCloud ) ) ;

							item.appendChild( doc.createTextNode( cloud ) ) ;
						}
					}
				}
				else if( next.equalsIgnoreCase( "Atmospheric Conditions" ) )
				{
					item = doc.createElement( next ) ;
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
						logger.debug( "ATMOS: " + ( String )iter.next() ) ;
				}
				else if( next.equalsIgnoreCase( "country" ) )
				{
					item = doc.createElement( next ) ;
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
					{
						abstractButton = ( JRadioButton )( iter.next() ) ;
						if( abstractButton.isSelected() )
						{
							if( !abstractButton.getText().equalsIgnoreCase( "any" ) )
							{
								item.appendChild( doc.createTextNode( abstractButton.getText() ) ) ;
								root.appendChild( item ) ;
							}
						}
					}
				}
				else if( ht.get( next ) instanceof LinkedList )
				{
					item = doc.createElement( next ) ;
					final ListIterator iter = (( LinkedList )ht.get( next )).listIterator( 0 ) ;
					for( ; iter.hasNext() ; iter.nextIndex() )
					{
						obj = iter.next() ;
						if( obj instanceof JComboBox )
						{
							String textField = ( String )( iter.next() ) ;
							obj = ( JComboBox )obj ;
							if( !textField.equals( "" ) )
							{
								sub = doc.createElement( ( String )(( JComboBox )obj).getSelectedItem() ) ;
								sub.appendChild( doc.createTextNode( textField ) ) ;
								item.appendChild( sub ) ;
							}
						}
					}
				}
				else if( ht.get( next ) instanceof LabeledTextField )
				{
					LabeledTextField ltf = ( LabeledTextField )ht.get( next ) ;
					Enumeration<String> n = ltf.getList().elements() ;
					String tmpStr ;

					while( n.hasMoreElements() )
					{
						if( next.equalsIgnoreCase( "pi" ) )
							item = doc.createElement( "name" ) ;
						else if( next.equalsIgnoreCase( "project" ) )
							item = doc.createElement( "projectid" ) ;
						else if( next.equalsIgnoreCase( "seeing" ) )
							item = doc.createElement( "seeing" ) ;
						else if( next.equalsIgnoreCase( "tau" ) )
							item = doc.createElement( "tau" ) ;
						else if( next.equalsIgnoreCase( "airmass" ) )
							item = doc.createElement( "airmass" ) ;
						else if( next.equalsIgnoreCase( "brightness" ) )
							item = doc.createElement( "sky" ) ;
						else
							continue ;

						tmpStr = n.nextElement() ;
						item.appendChild( doc.createTextNode( tmpStr.trim() ) ) ;
						root.appendChild( item ) ;
					} // end of while ()
				}

				else if( ht.get( next ) instanceof LabeledRangeTextField )
				{
					LabeledRangeTextField lrtf = ( LabeledRangeTextField )ht.get( next ) ;
					String tmpStr ;

					// Temporary and very inefficient code fix.
					// Gets over a problem of removing these from the item from the bag.
					if( lrtf.getLowerText().equals( "" ) && lrtf.getUpperText().equals( "" ) )
						continue ;

					if( next.equalsIgnoreCase( "duration" ) )
					{
						item = doc.createElement( "timeest" ) ;
						item.setAttribute( "units" , "minutes" ) ;
					}
					else if( next.equalsIgnoreCase( "observation" ) )
					{
						root = processDate( lrtf , doc , root ) ;
						continue ;
					}
					else
					{
						item = doc.createElement( next ) ;
					}

					if( next.equals( "hour" ) )
						item = doc.createElement( "ha" ) ;

					tmpStr = lrtf.getLowerText() ;
					sub = doc.createElement( "min" ) ;
					sub.appendChild( doc.createTextNode( tmpStr.trim().toLowerCase() ) ) ;
					item.appendChild( sub ) ;

					tmpStr = lrtf.getUpperText() ;
					sub = doc.createElement( "max" ) ;
					sub.appendChild( doc.createTextNode( tmpStr.trim().toLowerCase() ) ) ;
					item.appendChild( sub ) ;

					root.appendChild( item ) ;
				}
				else
				{
					item = null ;
					throw ( new NullPointerException( "A widget in the InputPanel has data, but has not been set!" ) ) ;
				} // end of else

				root.appendChild( item ) ;
			}

			doc.appendChild( root ) ;

			OutputFormat format = new OutputFormat( doc , "UTF-8" , true ) ; // Serialize DOM
			StringWriter stringOut = new StringWriter() ; // Writer will be a String
			XMLSerializer serial = new XMLSerializer( stringOut , format ) ;
			serial.asDOMSerializer() ; // As a DOM Serializer
			serial.serialize( doc.getDocumentElement() ) ;

			_xmlString = stringOut.toString() ;
		}
		catch( Exception ex )
		{
			logger.error( ex.getMessage() , ex ) ;
		}
	}

	/**
	 * The <code>getXML</code> method returns the _xmlString.
	 * 
	 * @return a <code>String</code> value
	 */
	public String getXML()
	{
		buildXML( bag.getHash() ) ;
		return _xmlString ;
	}

	/**
	 * The <code>printXML</code> method is a utility to the current _xmlString.
	 */
	public void printXML()
	{
		logger.debug( getXML() ) ; // Spit out DOM as a String
	}

	/**
	 * The <code>queryMSB</code> method starts the SOAP client. A successful query will write all MSB Summaries to file.
	 */
	public void run()
	{
		MsbClient.queryMSB( getXML() ) ;
	}

	/**
	 * The <code>queryMSB</code> method starts the SOAP client. A successful query will write all MSB Summaries to file and return true.
	 */
	public boolean queryMSB()
	{
		return MsbClient.queryMSB( getXML() ) ;
	}

	/**
	 * The <code>fetchMSB</code> method starts the SOAP client. A successful fetch will start the lower level OMP-OM sequence.
	 * 
	 * @param i
	 *            an <code>Integer</code> value
	 */
	public SpItem fetchMSB( Integer i ) throws NullPointerException
	{
		SpItem spItem = MsbClient.fetchMSB( i ) ;

		if( spItem == null )
			throw ( new NullPointerException() ) ;

		return spItem ;
	}

	/**
	 * <code>XMLisNull</code> is a test for a null _xmlString.
	 * 
	 * @return a <code>boolean</code> value
	 */
	public boolean XMLisNull()
	{
		return( _xmlString == null ) ;
	}

	private Element processDate( LabeledRangeTextField lrtf , Document doc , Element root )
	{
		Element item ;
		String tmpStr ;

		// Make sure the specified time is in a valid format and
		// if not, "make it so number 1"
		String time = lrtf.getUpperText() ;
		StringTokenizer st = new StringTokenizer( time , ":" ) ;
		if( st.countTokens() == 1 )
			time = time + ":00:00" ;
		else if( st.countTokens() == 2 )
			time = time + ":00" ;

		tmpStr = lrtf.getLowerText() + "T" + time ;

		if( !lrtf.timerRunning() )
		{
			if( TimeUtils.isValidDate( tmpStr ) )
			{
				item = doc.createElement( "date" ) ;
				tmpStr = TimeUtils.convertLocalISODatetoUTC( tmpStr ) ;
				item.appendChild( doc.createTextNode( tmpStr.trim() ) ) ;
				root.appendChild( item ) ;
			}
		}
		else
		{
			// We will use the current date, so set execution to true
		}
		// Recalculate the moon if the user has not overridden the default, otherwise leave it as it is
		if( WidgetPanel.getMoonPanel() != null && WidgetPanel.getMoonPanel().getBackground() != Color.red )
		{
			SimpleMoon moon = SimpleMoon.getInstance() ;
			if( lrtf.timerRunning() || !TimeUtils.isValidDate( tmpStr ) )
				moon.reset() ;
			else
				moon.set( tmpStr ) ;
			double moonValue = 0 ;
			if( moon.isUp() )
				moonValue = moon.getIllumination() * 100 ;
			// Delete any existing value and replace with the new
			NodeList list = root.getElementsByTagName( "moon" ) ;
			if( list.getLength() != 0 )
				root.removeChild( list.item( 0 ) ) ;
			item = doc.createElement( "moon" ) ;
			item.appendChild( doc.createTextNode( "" + moonValue ) ) ;
			root.appendChild( item ) ;
		}
		return root ;
	}
}// Querytool
