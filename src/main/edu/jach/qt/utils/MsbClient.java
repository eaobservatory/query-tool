package edu.jach.qt.utils;


// /* Gemini imports */
import gemini.sp.SpItem;

// /* ORAC imports */
import orac.util.SpInputXML ;

/* OMP imports */
import omp.SoapClient ; 

// /* Standard imports */
import java.io.ByteArrayInputStream ;
import java.io.FileWriter ;
import java.io.StringReader ;
import java.net.URL ;
import java.util.zip.GZIPInputStream;

// /* Miscellaneous imports */
import org.apache.log4j.Logger;

/**
 * MsbClient.java
 *
 * This is a utility class used to send SOAP messages to the MsbServer.
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>,
 * modified by Martin Folger

 * $Id$ 
 */
public class MsbClient extends SoapClient {

  static Logger logger = Logger.getLogger(MsbClient.class);

  /**
   * <code>queryMSB</code>
   * Perform a query to the MsbServer with the given query
   * String. Success will return a true value and write the msbSummary
   * xml to file.  A unique file will be written for each user to allow
   * multiple users on th same machine.  This file should not be read by
   * the code.
   *
   * @param xmlQueryString a <code>String</code> value. The xml representing the query.
   * @return a <code>boolean</code> value indicating success.
   */
  public static boolean queryMSB(String xmlQueryString) {
      try {
	
	logger.debug("Sending queryMSB: "+xmlQueryString);
	URL url = new URL(System.getProperty("msbServer"));
	logger.info("Connecting to: "+url.toString());
	flushParameter();
	addParameter("xmlquery", String.class, xmlQueryString);

	String fileName = System.getProperty("msbSummary") + "." +
	    System.getProperty("user.name");

	FileWriter fw = new FileWriter(fileName);
	Object tmp = doCall(url, "urn:OMP::MSBServer", "queryMSB");

	if (tmp != null ) {
	  fw.write( (String)tmp );
	  fw.close();
	}

	else 
	  return false;
	 
      } catch (Exception e) {
	logger.error("queryMSB threw Exception", e);
	e.printStackTrace();
	return false;
      }
      return true;

  }

    /**
     * <code>queryCalibration</code>
     * Perform a query to the MsbServer with the special calibration query
     * String. Success will return a true value and write the msbSummary
     * xml to file.  A unique file will be written for each user to allow
     * multiple users on th same machine.  This file should not be read by
     * the code.
     *
     * @param xmlQueryString a <code>String</code> value. The xml representing the query.
     * @return a <code>String</code> value of the results in XML.
     */
    public static String queryCalibration( String xmlQueryString )
	{
		Object tmp;
		try
		{
			logger.debug( "Sending queryMSB: " + xmlQueryString );
			URL url = new URL( System.getProperty( "msbServer" ) );
			flushParameter();
			addParameter( "xmlquery" , String.class , xmlQueryString );
			addParameter( "maxcount" , Integer.class , new Integer( 2000 ) );
			tmp = doCall( url , "urn:OMP::MSBServer" , "queryMSB" );
		}
		catch( Exception e )
		{
			return null;
		}
		return tmp.toString();
	}   
    
    public static String fetchCalibrationProgram()
	{
		Object tmp ;
		String xml = "" ;
		String telescope = System.getProperty( "telescope" ) ;
		try
		{
			logger.debug( "Sending fetchCalProgram: " + telescope ) ;
			URL url = new URL( System.getProperty( "msbServer" ) ) ;
			flushParameter() ;
			addParameter( "telescope" , String.class , telescope ) ;
			tmp = doCall( url , "urn:OMP::MSBServer" , "fetchCalProgram" ) ;
			if( tmp instanceof byte[] )
				xml = new String( ( byte[] )tmp ) ;
		}
		catch( Exception e )
		{
			return null;
		}
		return xml ;
	}     
    
    /**
	 * <code>fetchMSB</code> Fetch the msb indicated by the msbid. The SpItem will return null on failure. In the future this should throw an exception instead.
	 * 
	 * @param msbid
	 *            an <code>Integer</code> value of the MSB
	 * @return a <code>SpItem</code> value representing the MSB.
	 */
    public static SpItem fetchMSB(Integer msbid) {

        SpItem spItem = null;
        String spXML = null;
        try {

            logger.debug("Sending fetchMSB: "+msbid);
            URL url = new URL(System.getProperty("msbServer"));
            flushParameter();
            addParameter("key", Integer.class, msbid);

            FileWriter fw = new FileWriter(System.getProperty("msbFile")+"."+System.getProperty("user.name"));
            Object o = doCall(url, "urn:OMP::MSBServer", "fetchMSB");
// 	    byte [] input = (byte []) doCall(url, "urn:OMP::MSBServer", "fetchMSB");

            if (o != null ) {
                if ( !(o instanceof String) ) {
//                 if ( (char)input[0] != '<' && (char)input[1] != '?' ) {
                    // File is gzipped
                    byte [] input = (byte [])o;
                    ByteArrayInputStream bis = new ByteArrayInputStream(input);
                    GZIPInputStream gis = new GZIPInputStream(bis);
                    byte [] read = new byte[1024];
                    int len;
                    StringBuffer sb = new StringBuffer();
                    while ( (len = gis.read(read)) > 0) {
                        sb.append(new String(read, 0, len));
                    }
                    gis.close();
                    spXML = sb.toString();
                }
                else {
                    // File is not compressed
                    spXML = (String)o;
                }
                fw.write( spXML );
                fw.close();
                StringReader r = new StringReader(spXML);
                spItem = (SpItem)(new SpInputXML()).xmlToSpItem(spXML);
           }

        } catch (Exception e) {
            logger.error("queryMSB threw Exception", e);
            e.printStackTrace();
            return spItem;
        }

        return spItem;
    }

    private static MsbColumns columns ;
    public synchronized static MsbColumns getColumnInfo()
    {
    	if( columns == null )
    		columns = new MsbColumns() ;
    	else
    		return columns ;
    		
    	String[] names = getColumnNames() ;
    	String[] types = getColumnClasses() ;

    	String hiddenColumns = System.getProperty( "hiddenColumns" ) ;
    	String[] hidden = hiddenColumns.split( "%" ) ;

    	if( names != null && types != null )
    	{
	    	if( names.length == types.length )
	    	{
	    		MsbColumnInfo columnInfo ;
	    		for( int index = 0 ; index < names.length ; index++ )
	    		{
	    			String name = names[ index ] ;
	    			String type = types[ index ] ;
	    			columnInfo = new MsbColumnInfo( name , type ) ;
	    			for( int i = 0 ; i < hidden.length ; i++ )
	    			{
	    				if( hidden[ i ].equalsIgnoreCase( name ) )
	    					columnInfo.setVisible( false ) ;
	    			}
	    			columns.add( columnInfo ) ;
	    		}
	    	}   
    	}
    	else
    	{
    		columns = new MsbColumns() ;
    	}
    	return columns ;
    }
    
    /**
	 * Method to get the list of columns in an MSB Summary. Requires the <code>telescope</code> system parameter to be set.
	 * 
	 * @return A string array of column names.
	 */
    private static String[] columnNames ;
    private static String[] getColumnNames()
	{
		if( columnNames != null )
			return columnNames ;
		try
		{
			URL url = new URL( System.getProperty( "msbServer" ) );
			flushParameter();
			addParameter( "telescope" , String.class , System.getProperty( "telescope" ) );
			Object o = doCall( url , "urn:OMP::MSBServer" , "getResultColumns" );
			columnNames = ( String[] ) o;
		}
		catch( Exception e )
		{
			logger.error( "getColumnNames threw exception" , e );
		}
		return columnNames ;
	}

	/**
	 * Method to get the list of column types in an MSB Summary. Requires the <code>telescope</code> system parameter to be set.
	 * 
	 * @return A string array of column types (eg Integer, String, etc).
	 */
	private static String[] columnClasses ;
	private static String[] getColumnClasses()
	{
		if( columnClasses != null )
			return columnClasses ;
		try
		{
			URL url = new URL( System.getProperty( "msbServer" ) );
			flushParameter();
			addParameter( "telescope" , String.class , System.getProperty( "telescope" ) );
			Object o = doCall( url , "urn:OMP::MSBServer" , "getTypeColumns" );
			columnClasses = ( String[] ) o;
		}
		catch( Exception e )
		{
			logger.error( "getColumnNames threw exception" , e );
		}
		return columnClasses ;
	}


  /**
	 * <code>doneMSB</code> Mark the given project ID as done in the database.
	 * 
	 * @param projID
	 *            a <code>String</code> the project ID.
	 * @param checksum
	 *            a <code>String</code> the checksum for this project.
	 */
  public static void doneMSB(String projID, String checksum) {
    try {

      logger.debug("Sending doneMSB "+projID+ " "+checksum);

      URL url = new URL(System.getProperty("msbServer"));
      flushParameter();
      addParameter("projID", String.class, projID);
      addParameter("checksum", String.class, checksum);

      Object tmp = doCall(url, "urn:OMP::MSBServer", "doneMSB");

      if (tmp != null ) {
	// tmp has something with success
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }


  /**
   * <code>doneMSB</code> Mark the given project ID as done in the database.
   *
   * @param projID a <code>String</code> the project ID.
   * @param checksum a <code>String</code> the checksum for this project.
   * @param user the ID of the user ("observer" should never be used)
   * @param comment textual information associated with the project
   */
  public static void doneMSB(String projID, String checksum,
			     String user, String comment) throws Exception {
    try {

      logger.debug("Sending doneMSB "+projID+ " "+checksum);

      URL url = new URL(System.getProperty("msbServer"));
      flushParameter();
      addParameter("projID", String.class, projID);
      addParameter("checksum", String.class, checksum);
      addParameter("userID", String.class, user);
      addParameter("reason", String.class, comment);

      Object tmp = doCall(url, "urn:OMP::MSBServer", "doneMSB");

      if (tmp != null ) {
	// tmp has something with success
      }

    } 
    catch (InvalidUserException e) {
	throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * <code>rejectMSB</code> Does Not Mark the given project ID as done in the database.
   *
   * @param projID a <code>String</code> the project ID.
   * @param checksum a <code>String</code> the checksum for this project.
   * @param user the ID of the user ("observer" should never be used)
   * @param comment textual information associated with the project
   */
  public static void rejectMSB(String projID, String checksum,
			     String user, String comment) throws Exception {
    try {

      logger.debug("Sending rejectMSB "+projID+ " "+checksum);

      URL url = new URL(System.getProperty("msbServer"));
      System.out.println("Sending request to "+url.toString());
      flushParameter();
      addParameter("projID", String.class, projID);
      addParameter("checksum", String.class, checksum);
      addParameter("userID", String.class, user);
      addParameter("reason", String.class, comment);

      Object tmp = doCall(url, "urn:OMP::MSBServer", "rejectMSB");

      if (tmp != null ) {
	// tmp has something with success
      }

    }
    catch (InvalidUserException e) {
	throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * Describe <code>main</code> method here.
   *
   * @param args a <code>String[]</code> value
   */
  public static void main(String[] args) {
    MsbClient.queryMSB("<Query><Moon>Dark</Moon></Query>");
    MsbClient.fetchMSB(new Integer(96));
  }

}
