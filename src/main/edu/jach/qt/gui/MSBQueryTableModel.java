package edu.jach.qt.gui;

import java.io.File ;
import java.io.IOException ;
import java.util.Vector ;
import java.util.ArrayList ;
import java.util.List ;
import javax.swing.table.AbstractTableModel ;
import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.DocumentBuilder ;
import javax.xml.parsers.ParserConfigurationException ;
import org.apache.log4j.Logger;
import org.w3c.dom.Document ;
import org.w3c.dom.Element ;
import org.xml.sax.SAXException;  

import edu.jach.qt.utils.MsbClient;

import edu.jach.qt.utils.MsbColumnInfo ;
import edu.jach.qt.utils.MsbColumns ;
import edu.jach.qt.utils.MSBTableModel ;

import edu.jach.qt.utils.OrderedMap ;

/**
 * MSBQueryTableModel.java
 *
 *
 * Created: Tue Aug 28 16:49:16 2001
 *
 */

public class MSBQueryTableModel extends AbstractTableModel implements Runnable {

  static Logger logger = Logger.getLogger(MSBQueryTableModel.class);
  private String _projectId = null;
  public static final String ROOT_ELEMENT_TAG = "SpMSBSummary";

  public static final String MSB_SUMMARY = System.getProperty("msbSummary")+"."+System.getProperty("user.name");
  public static final String MSB_SUMMARY_TEST = System.getProperty("msbSummaryTest");

    private int            colCount;           // The number of columns TO DISPLAY
                                               // This may be less than the actual number of columns
    public static int      PROJECTID;          // Index of column containing the project ID
    public static int      CHECKSUM;           // Index of column containing the project checksum
    public static int      MSBID;              // Index of column containing the MSB Id

    private OrderedMap         model;
    private Vector         modelIndex = new Vector();
    
    public static String [] colClassNames ;
      
  //DATA
  //DOM object to hold XML document contents
  protected Document doc;
  protected Element msbIndex;
  public Integer[] projectIds;
  boolean docIsNull;

  //used to hold a list of TableModelListeners
  protected List tableModelListeners = new ArrayList();        

    /**
	 * Constructor. Constructs a tabe model with 200 possible entries.
	 */
	public MSBQueryTableModel() throws Exception
	{
		updateColumns();
		adjustColumnData();

		docIsNull = true;
		projectIds = new Integer[ 200 ];
	}


    /**
     * Set the current project id.
     * @param project   The name of the current project
     */
    public void setProjectId(String project) {
	_projectId = project;
	fireTableChanged(null);
    }

    /**
	 * Impelmentation of <code>Runnable</code> interface. Creates a DOM document for populating the table.
	 */
	public void run()
	{
		// Clear the current model
		if( model != null )
		{
			for( int i = 0 ; i < model.size() ; i++ )
			{
				Object temp =  model.remove( i ) ;
				if( temp instanceof MSBTableModel )
				{
					MSBTableModel msbTableModel = ( MSBTableModel )temp ;
					msbTableModel.clear() ;
				}
			}
			model.clear();
		}
		modelIndex.clear();

		// Parse the MSB summary which should have already been generated from the query.
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse( new File( MSB_SUMMARY ) );
			docIsNull = false;

		}
		catch( SAXException sxe )
		{
			Exception x = sxe;
			if( sxe.getException() != null )
				x = sxe.getException();
			logger.error( "SAX Error generated during parsing" , x );

		}
		catch( ParserConfigurationException pce )
		{
			logger.error( "ParseConfiguration Error generated during parsing" , pce );
		}
		catch( IOException ioe )
		{
			logger.error( "IO Error generated attempting to build Document" , ioe );
		}

		// If the document exists, build a new model so we don't need to keep
		// going back to the XML.
		if( doc != null )
		{
			logger.info( "Building new model" );
			model = XmlUtils.getNewModel( doc , ROOT_ELEMENT_TAG );
			// Move the columns around to the current bitset.
			adjustColumnData();
			if( model != null )
			{
				// Create an internal map of projects to MSBs
				for( int i = 0 ; i < model.size() ; i++ )
				{
					modelIndex.add( ( ( MSBTableModel )model.find( i ) ).getProjectId() );
				}
			}
			_projectId = "all";
			logger.info( "Result contained " + getRowCount() + " MSBs in " + modelIndex.size() + " Projects" );
		}
	}
    
    /**
	 * Return the current DOM document.
	 */
  public Document getDoc() {
    return doc;
  }

  //
  // TableModel implementation
  //

  /**
     Return the number of columns for the model.

     @return    the number of columns in the model
  */
  public int getColumnCount() {
    return colCount;
  }

    /**
	 * Get the real number of columns in the model. This may be less than the number of columns displayed on the associated table.
	 * 
	 * @return The number of columns in the model.
	 */
	public int getRealColumnCount()
	{
		return MsbClient.getColumnInfo().size() ;
	}


  /**
	 * Return the number of persons in an XML document
	 * 
	 * @return the number or rows in the model
	 */
	public int getRowCount()
	{
		int rowCount = 0;
		if( model == null || model.size() == 0 || _projectId == null )
			return rowCount;
		rowCount = ( ( MSBTableModel )model.find( 0 ) ).getColumn( 0 ).size();
		return rowCount;
	}

  /**
	 * Return an XML data given its location
	 * 
	 * @param r
	 *            the row whose value is to be looked up
	 * @param c
	 *            the column whose value is to be looked up
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt( int r , int c )
	{
		if( _projectId.equalsIgnoreCase( "all" ) )
		{
			// Need to get data for all the MSBs returned...
			int rowCount = 0;
			for( int index = 0 ; index < model.size() ; index++ )
			{
				// Get the number of rows in the current model
				rowCount = ( ( MSBTableModel )model.find( index ) ).getColumn( 0 ).size();
				if( rowCount <= r )
				{
					// We have the right model, so get the data
					r = r - rowCount;
					continue;
				}
				return ( ( MSBTableModel )model.find( index ) ).getData( r , c );
			}
		}
		else
		{
			int index = modelIndex.indexOf( _projectId );
			if( index != -1 )
			{
				return ( ( MSBTableModel )model.find( index ) ).getData( r , c );
			}
		}
		return null;
	}
	
    /**
     * Get the Summary Identifier of the current row.
     * @param row  The selected row of the table.
     * @return The SpSummaryId from the selected row.
     */
    public Integer getSpSummaryId(int row) {
	return projectIds[row];
    }
    
    /**
	 * Return the name of column for the table.
	 * 
	 * @param c
	 *            the index of column
	 * @return the name of the column
	 */
	public String getColumnName( int c )
	{
		MsbColumnInfo columnInfo = ( MsbColumnInfo )MsbClient.getColumnInfo().find( c ) ;
		return columnInfo.getName() ;
	}

	/**
	 * Return column class
	 * 
	 * @parm c the index of column
	 * @return the common ancestor class of the object values in the model.
	 */
	public Class getColumnClass( int c )
	{
		MsbColumnInfo columnInfo = ( MsbColumnInfo )MsbClient.getColumnInfo().find( c ) ;
		return columnInfo.getClassType() ;
	}

    /**
	 * Return false - table is not editable
	 * 
	 * @param r
	 *            the row whose value is to be looked up
	 * @param c
	 *            the column whose value is to be looked up
	 * @return <code>false</code> always..
	 */
    public boolean isCellEditable(int r, int c) {
	return false;
    }
    
    /**
       This method is not implemented, because the table is not editable.
       
     @param	    value		 the new value
     @param	    r	 the row whose value is to be changed
     @param	    c 	 the column whose value is to be changed
    */
    public void setValueAt(Object value, int r, int c) {
    }



    /**
	 * Method to select a subset of columns in the model to display on the associated table. The <code>BitSet</code> input must be in the same order as that returned from a <code>getColumnNames</code> query. If a bit is set, it is assumed that column should be displayed.
	 * 
	 * @see edu.jach.qt.utils.MsbClient#getColumnNames()
	 * @param colSet
	 *            The set of columns to display.
	 */
	public void updateColumns()
	{
		int nHidden = 0;
		
		MsbColumns columns = MsbClient.getColumnInfo() ;
		for( int i = columns.size() - 1 ; i >= 0 ; i-- )
		{
			if( !columns.getVisibility( i ) )
			{
				nHidden++;
				Object object = columns.remove( i ) ;
				columns.add( ( MsbColumnInfo )object ) ;
			}
		}
		// Set the column count
		colCount = columns.size() - nHidden;
		
		// these really should be replaced
		MSBID = columns.findIndex( "msbid" ) ;
		PROJECTID = columns.findIndex( "projectid" ) ;
		CHECKSUM = columns.findIndex( "checksum" ) ;
		
		fireTableChanged( null );
	}

    public void adjustColumnData()
	{
		if( model == null )
			return ;
		// Loop through each submodel
		for( int i = 0 ; i < model.size() ; i++ )
		{
			MSBTableModel current = ( MSBTableModel )model.find( i );
			for( int j = current.getWidth() - 1 ; j >= 0 ; j-- )
			{
				if( !current.isVisible( j ) )
				{
					// Move the column to the end to hide it.
					current.moveColumnToEnd( j );
				}
			}
		}
	}

    public void clear()
	{
		if( model != null )
		{
			model.clear();
			modelIndex.removeAllElements();
		}
		updateColumns();
	}

}// MSBQueryTableModel
