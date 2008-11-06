package edu.jach.qt.utils ;

/* Gemini imports */
import gemini.sp.SpProg ;
import gemini.sp.SpSurveyContainer ;
import gemini.sp.SpAND ;
import gemini.sp.SpMSB ;
import gemini.sp.SpItem ;
import gemini.sp.SpObs ;

/* Standard imports */
import java.io.File ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.io.StringWriter ;
import java.util.TreeMap ;
import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.DocumentBuilder ;
import javax.xml.parsers.ParserConfigurationException ;
import java.util.Enumeration ;

/* Miscellaneous imports */
import org.apache.xerces.dom.DocumentImpl ;
import org.apache.xml.serialize.XMLSerializer ;
import org.apache.xml.serialize.OutputFormat ;
import org.w3c.dom.Document ;
import org.w3c.dom.Element ;
import org.xml.sax.SAXException ;

import orac.util.SpInputXML ;
import org.apache.log4j.Logger ;
import edu.jach.qt.gui.XmlUtils ;

/**
 * This class returns a <code>Hashtable</code> of calibrations.  Each entry in the
 * hashtable takes the form (String title, Integer id), where 
 * title is the title of the Observation and ID is its unique identifier.
 * Calibration entries are expected to be in the datase and belong to a
 * project called "CAL".  This project must be uniquye and ONLY contain
 * calibration observations.
 *
 * @author   $Author$
 * @version  $Revision$
 */

public class CalibrationList
{
	private static final String ALL_DISABLED = "all" ;
	public static final String ROOT_ELEMENT_TAG = "SpMSBSummary" ;
	static Logger logger = Logger.getLogger( CalibrationList.class ) ;

	/**
	 * Constructor
	 */
	private CalibrationList(){}

	public static OrderedMap<String,SpItem> getCalibrations()
	{
		OrderedMap<String,SpItem> orderedMap = new OrderedMap<String,SpItem>() ;

		try
		{
			String scienceProgramString = MsbClient.fetchCalibrationProgram() ;
			SpItem spItem = ( new SpInputXML() ).xmlToSpItem( scienceProgramString ) ;
			SpProg scienceProgram = null ;
			if( spItem instanceof SpProg )
				scienceProgram = ( SpProg )spItem ;
			if( scienceProgram != null )
				orderedMap = pickApart( orderedMap , scienceProgram ) ;
		}
		catch( Exception e )
		{
			System.out.println( e ) ;
		}
		return orderedMap ;
	}

	private static OrderedMap<String,SpItem> pickApart( OrderedMap<String,SpItem> orderedMap , SpItem spItem )
	{
		Enumeration<SpItem> enumeration = spItem.children() ;
		String telescope = System.getProperty( "telescope" ) ;
		SpItem object ;

		while( enumeration.hasMoreElements() )
		{
			object = enumeration.nextElement() ;
			if( object instanceof SpAND )
			{
				SpAND and = ( SpAND )object ;
				String title = and.getTitle() ;
				orderedMap.add( title , null ) ;
			}
			else if( object instanceof SpObs && "UKIRT".equalsIgnoreCase( telescope ) )
			{
				SpObs obs = ( SpObs )object ;
				String title = obs.getTitleAttr() ;
				orderedMap.add( title , obs ) ;
			}
			else if( object instanceof SpMSB && !( object instanceof SpObs ) )
			{
				SpMSB msb = ( SpMSB )object ;
				String title = msb.getTitleAttr() ;
				orderedMap.add( title , msb ) ;
			}
			else if( object instanceof SpSurveyContainer )
			{
				continue ;
			}
			orderedMap = pickApart( orderedMap , object ) ;
		}
		return orderedMap ;
	}

	/**
	 * Get the list of calibration observations for a specified telescope.
	 * 
	 * @param telescope
	 *            The name os the telescope.
	 * @return A <code>TreeMap</code> of observations. If no observations are found then there will be zero entries in the table.
	 */
	public static TreeMap<String,Integer> getCalibrations( String telescope )
	{
		TreeMap<String,Integer> tree = new TreeMap<String,Integer>() ;
		Document doc = new DocumentImpl() ;
		Element root = doc.createElement( "MSBQuery" ) ;
		Element item ;

		/* Construct the query */
		item = doc.createElement( "disableconstraint" ) ;
		item.appendChild( doc.createTextNode( ALL_DISABLED ) ) ; // Disables all constraints
		root.appendChild( item ) ;

		/*
		 * Add the telescope element - there should be unique a unique CAL project for each telescope
		 */
		item = doc.createElement( "telescope" ) ;
		item.appendChild( doc.createTextNode( System.getProperty( "telescope" ) ) ) ;
		root.appendChild( item ) ;

		/*
		 * The calibration project id MUST be of the form <TELESCOPE>CAL e.g. UKIRTCAL, JCMTCAL, GEMININCAL etc
		 */
		String calibrationProject = telescope.toUpperCase() + "CAL" ;
		item = doc.createElement( "projectid" ) ;
		item.appendChild( doc.createTextNode( calibrationProject ) ) ;
		root.appendChild( item ) ;

		doc.appendChild( root ) ;

		OutputFormat fmt = new OutputFormat( doc , "UTF-8" , true ) ;
		StringWriter writer = new StringWriter() ;
		XMLSerializer serial = new XMLSerializer( writer , fmt ) ;
		try
		{
			serial.asDOMSerializer() ;
			serial.serialize( doc.getDocumentElement() ) ;
		}
		catch( IOException ioe )
		{
			return null ;
		}

		/* Send the query to the database */
		String result = MsbClient.queryCalibration( writer.toString() ) ;
		if( result == null || result.equals( "" ) )
			return null ;

		/*
		 * To allow us to parser the returned XML, create a temporary file to write the XML to, and then build it again using the document model
		 */
		try
		{
			File tmpFile = File.createTempFile( "calibration" , ".xml" ) ;
			FileWriter fw = new FileWriter( tmpFile ) ;
			fw.write( result ) ;
			fw.close() ;

			doc = null ;

			// Build the document factory and try to parse the results
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;

			DocumentBuilder builder = factory.newDocumentBuilder() ;
			doc = builder.parse( tmpFile ) ;

			if( doc != null )
			{
				/*
				 * Since this only contains calibrations, loop through every node and get the title and identifier
				 */
				for( int node = 0 ; node < XmlUtils.getSize( doc , ROOT_ELEMENT_TAG ) ; node++ )
				{
					item = XmlUtils.getElement( doc , ROOT_ELEMENT_TAG , node ) ;
					tree.put( XmlUtils.getValue( item , "title" ) , new Integer( item.getAttribute( "id" ) ) ) ;
				}
			}
			else
			{
				logger.warn( "No Calibration results returned" ) ;
			}
			tmpFile.delete() ;

		}
		catch( SAXException sxe )
		{
			Exception x = sxe ;
			if( sxe.getException() != null )
				x = sxe.getException() ;
			logger.error( "SAX Error generated during parsing" , x ) ;

		}
		catch( ParserConfigurationException pce )
		{
			logger.error( "ParseConfiguration Error generated during parsing" , pce ) ;
		}
		catch( IOException ioe )
		{
			logger.error( "IO Error generated attempting to build Document" , ioe ) ;
		}

		// return the hopefully populated hashtable. 
		// If no entries, we return the hashtable anyway and rely on the caller to realise that it is of zero size
		return tree ;
	}
}
