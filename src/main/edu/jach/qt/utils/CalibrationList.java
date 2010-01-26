package edu.jach.qt.utils ;

/* Gemini imports */
import gemini.sp.SpProg ;
import gemini.sp.SpSurveyContainer ;
import gemini.sp.SpAND ;
import gemini.sp.SpMSB ;
import gemini.sp.SpItem ;
import gemini.sp.SpObs ;
import gemini.util.JACLogger ;

/* Standard imports */
import java.util.Enumeration ;

import orac.util.SpInputXML ;
import orac.util.OrderedMap ;;

/**
 * This class returns a <code>OrderedMap</code> of calibrations.  Each entry in the
 * OrderedMap takes the form (String title, SpItem cal), where
 * title is the title of the Observation and cal is calibration observation.
 * Calibration entries are expected to be in the database and belong to a
 * project called "CAL".  This project must be unique and ONLY contain
 * calibration observations.
 *
 * @author   $Author$
 * @version  $Revision$
 */

public class CalibrationList
{
	public static final String ROOT_ELEMENT_TAG = "SpMSBSummary" ;
	static final JACLogger logger = JACLogger.getLogger( CalibrationList.class ) ;

	/**
	 * Constructor
	 */
	private CalibrationList(){}

	public static OrderedMap<String,OrderedMap<String,SpItem>> getCalibrations()
	{
		OrderedMap<String,OrderedMap<String,SpItem>> orderedMap = new OrderedMap<String,OrderedMap<String,SpItem>>() ;

		try
		{
			String scienceProgramString = MsbClient.fetchCalibrationProgram() ;
			SpItem spItem = new SpInputXML().xmlToSpItem( scienceProgramString ) ;			
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

	private static OrderedMap<String,SpItem> folder = null ;

	private static OrderedMap<String,OrderedMap<String,SpItem>> pickApart( OrderedMap<String,OrderedMap<String,SpItem>> orderedMap , SpItem spItem )
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
				folder = new OrderedMap<String,SpItem>() ;
				orderedMap.add( title , folder ) ;
			}
			else if( object instanceof SpObs && "UKIRT".equalsIgnoreCase( telescope ) )
			{
				SpObs obs = ( SpObs )object ;
				String title = obs.getTitleAttr() ;
				if( folder != null )
					folder.add( title , obs ) ;
			}
			else if( object instanceof SpMSB && !( object instanceof SpObs ) )
			{
				SpMSB msb = ( SpMSB )object ;
				String title = msb.getTitleAttr() ;
				if( folder != null )
					folder.add( title , msb ) ;
			}
			else if( object instanceof SpSurveyContainer )
			{
				continue ;
			}
			orderedMap = pickApart( orderedMap , object ) ;
		}
		return orderedMap ;
	}
}
