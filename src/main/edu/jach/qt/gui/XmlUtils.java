/*
 * Copyright (c) 1998-99 The Bean Factory, LLC.
 * All Rights Reserved.
 *
 * The Bean Factory, LLC. makes no representations or
 * warranties about the suitability of the software, either express
 * or implied, including but not limited to the implied warranties of
 * merchantableness, fitness for a particular purpose, or
 * non-infringement. The Bean Factory, LLC. shall not be
 * liable for any damages suffered by licensee as a result of using,
 * modifying or distributing this software or its derivatives.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This version modified by the Joint Astronomy Centre.
 */

package edu.jach.qt.gui ;

import java.io.StringReader ;
import java.io.File ;
import java.io.IOException ;
import java.util.StringTokenizer ;
import java.util.Vector ;
import javax.xml.parsers.DocumentBuilderFactory ;
import javax.xml.parsers.DocumentBuilder ;

import org.xml.sax.InputSource ;
import org.xml.sax.Attributes ;
import org.xml.sax.helpers.DefaultHandler ;
import org.apache.xerces.parsers.DOMParser ;

import org.w3c.dom.Element ;
import org.w3c.dom.Document ;
import org.w3c.dom.NodeList ;
import org.w3c.dom.Node ;
import org.xml.sax.SAXException ;

import edu.jach.qt.utils.MSBTableModel ;
import gemini.util.JACLogger ;

import orac.util.OrderedMap ;

import edu.jach.qt.utils.ProjectData ;

/**
 * <B>XmlUtils</B> is a utility method to get information about a 
 * XML document.  It also retrieve data in an XML document. 
 *
 * @author       : Nazmul Idris: BeanFactory LLC,
 * modified by Mathew Rippa

 * @version      : 1.0
 */
public class XmlUtils
{
	static final JACLogger logger = JACLogger.getLogger( XmlUtils.class ) ;

	/**
	 Return an Element given an XML document, tag name, and index
	 
	 @param     doc     XML docuemnt
	 @param     tagName a tag name
	 @param     index   a index
	 @return    an Element
	 */
	public static Element getElement( Document doc , String tagName , int index )
	{
		//given an XML document and a tag return an Element at a given index
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tagName ) ;
		return ( Element )rows.item( index ) ;
	}

	/**
	 Return an Element given an XML document, tag name, and index
	 
	 @param     doc     XML docuemnt
	 @param     tagName a tag name
	 @param     index   a index
	 @return    an Element
	 */
	public static Element getElement( Document doc , String tagName , int index , String selection )
	{
		//given an XML document and a tag return an Element at a given index
		int selectionCount = -1 ;
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tagName ) ;
		int count ;
		for( count = 0 ; count < rows.getLength() ; count++ )
		{
			Element e = getElement( doc , tagName , count ) ;
			String projectId = getValue( e , "projectid" ) ;
			if( projectId.trim().equals( selection.trim() ) )
			{
				selectionCount++ ;
				if( selectionCount == index )
					break ;
			}
		}
		return ( Element )rows.item( count ) ;
	}

	/**
	 Return the number of person in an XML document
	 
	 @param     doc     XML document
	 @param     tagName a tag name
	 @return    the number of person in an XML document
	 */
	public static int getSize( Document doc , String tagName )
	{
		//given an XML document and a tag name return the number of ocurances 
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tagName ) ;
		return rows.getLength() ;
	}

	/**
	 Return the number of person in an XML document
	 
	 @param     doc     XML document
	 @param     tagName a tag name
	 @return    the number of person in an XML document
	 */
	public static int getSize( Document doc , String tagName , String includeThis )
	{
		//given an XML document and a tag name return the number of ocurances 
		int rowCount = 0 ;
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tagName ) ;
		for( int i = 0 ; i < rows.getLength() ; i++ )
		{
			Element e = getElement( doc , tagName , i ) ;
			String projectId = getValue( e , "projectid" ) ;
			if( projectId.trim().equals( includeThis.trim() ) )
				rowCount++ ;
		}
		return rowCount ;
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
	public static String getValue( Element e , String tagName )
	{
		try
		{
			//get node lists of a tag name from a Element
			NodeList elements = e.getElementsByTagName( tagName ) ;

			Node node = elements.item( 0 ) ;
			NodeList nodes = node.getChildNodes() ;

			//find a value whose value is non-whitespace
			String s ;
			for( int i = 0 ; i < nodes.getLength() ; i++ )
			{
				s = nodes.item( i ).getNodeValue().trim() ;
				if( s.equals( "" ) || s.equals( "\r" ) )
					continue ;
				else
					return s ;
			}
		}
		catch( Exception ex )
		{
			logger.error( "XmlUtils getValueAt() threw exception" , ex ) ;
		}

		return null ;
	}

	public static String getChecksum( String xmlString )
	{
		// Conver string to document
		Document doc ;
		String checksum = null ;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;
			DocumentBuilder builder = factory.newDocumentBuilder() ;
			doc = builder.parse( new InputSource( new StringReader( xmlString ) ) ) ;
		}
		catch( Exception e )
		{
			return checksum ;
		}
		// Get the MSB element
		Element msb = getElement( doc , "SpMSB" , 0 ) ;
		// Get the attribute checksum
		if( msb != null )
		{
			checksum = msb.getAttribute( "checksum" ) ;
		}
		else
		{
			// See if this is an obs which is a MSB
			Element obs = getElement( doc , "SpObs" , 0 ) ;
			if( obs != null )
				checksum = obs.getAttribute( "checksum" ) ;
		}
		if( checksum == null )
			logger.error( "Unable to determine checksum" ) ;

		return checksum ;
	}

	/**
	 For testing purpose, it print out Node list
	 
	 @param     rows    a Nodelist
	 */
	public static void printNodeTypes( NodeList rows )
	{
		logger.debug( "\tenumerating NodeList (of Elements):" ) ;
		logger.debug( "\tClass\tNT\tNV" ) ;
		//iterate a given Node list
		for( int ri = 0 ; ri < rows.getLength() ; ri++ )
		{
			Node n = rows.item( ri ) ;
			if( n instanceof Element )
				System.out.print( "\tElement" ) ;
			else
				System.out.print( "\tNode" ) ;

			//print out Node type and Node value
			logger.debug( "\t" + n.getNodeType() + "\t" + n.getNodeValue() ) ;
		}
		logger.debug( "" ) ;
	}

	private static String convertStringToTime( String oldTime )
	{
		if( oldTime == null || oldTime.length() == 0 )
			return "00h00m00s" ;

		boolean hasHours = ( oldTime.indexOf( 'h' ) != -1 ) ;
		boolean hasMinutes = ( oldTime.indexOf( 'm' ) != -1 ) ;
		StringBuffer rtn = new StringBuffer( oldTime ) ;
		// Delete the seconds fields and replace other chars with colons
		rtn.deleteCharAt( oldTime.indexOf( 's' ) ) ;
		if( hasMinutes )
			rtn.setCharAt( oldTime.indexOf( 'm' ) , ':' ) ;
		else
			rtn.insert( 0 , "00:" ) ;

		if( hasHours )
			rtn.setCharAt( oldTime.indexOf( 'h' ) , ':' ) ;
		else
			rtn.insert( 0 , "00:" ) ;

		StringTokenizer st = new StringTokenizer( rtn.toString() , ":" ) ;
		int index = 1 ;
		String hh = "00" ;
		String mm = "00" ;
		String ss = "00" ;
		while( st.hasMoreTokens() )
		{
			String toke = st.nextToken() ;
			switch( index )
			{
				case 1 :
					if( toke.length() < 2 )
						hh = "0" + toke ;
					else
						hh = toke ;
					break ;
				case 2 :
					if( toke.length() < 2 )
						mm = "0" + toke ;
					else
						mm = toke ;
					break ;
				case 3 :
					if( toke.length() < 2 )
						ss = "0" + toke ;
					else
						ss = toke ;
					break ;
			}
			index++ ;
		}
		return hh + "h" + mm + "m" + ss + "s" ;

	}

	public static OrderedMap<String,MSBTableModel> getNewModel( Document doc , String tag )
	{
		if( doc == null )
			return null ;

		// Get all of the summary nodes
		NodeList rows = doc.getDocumentElement().getElementsByTagName( tag ) ;
		if( rows.getLength() == 0 )
			return null ;

		NodeList children ;
		// Create an OrderedMap containing the data for each project
		OrderedMap<String,MSBTableModel> projectData = new OrderedMap<String,MSBTableModel>() ;

		// Loop through all the elements in the document and start populating the new model.
		int rowsLength = rows.getLength() ;
		Element e ;
		String currentProject ;
		MSBTableModel currentModel ;
		for( int i = 0 ; i < rowsLength ; i++ )
		{
			e = getElement( doc , tag , i ) ;
			currentProject = getValue( e , "projectid" ) ;
			// Loop through all the models to see if there is one we can use
			if( projectData.find( currentProject ) == null )
			{
				currentModel = new MSBTableModel( currentProject ) ;
				projectData.add( currentProject , currentModel ) ;
			}
			else
			{
				currentModel = projectData.find( currentProject ) ;
			}

			currentModel.bumpIndex() ;

			// Now we have the model, loop through the elements and add these to the model
			children = rows.item( i ).getChildNodes() ;
			int numberOfChildren = children.getLength() ;
			String name ;
			String value ;
			for( int j = 0 ; j < numberOfChildren ; j++ )
			{
				name = children.item( j ).getNodeName().trim() ;
				if( name.charAt( 0 ) == '#' )
					continue ;
				value = getValue( e , name ) ;
				if( "timeest".equals( name ) )
					value = convertStringToTime( value ) ;
				currentModel.insertData( name , value ) ;
			}
		}
		return projectData ;
	}

	public static Vector<ProjectData> getProjectData()
	{
		return getProjectData( System.getProperty( "msbSummary" ) + "." + System.getProperty( "user.name" ) ) ;
	}

	public static Vector<ProjectData> getProjectData( String xmlFileName )
	{
		Document projectDoc = null ;
		DOMParser parser = new DOMParser() ;
		try
		{
			parser.parse( xmlFileName ) ;
			projectDoc = parser.getDocument() ;
		}
		catch( IOException ioe )
		{
			System.err.println( "WARNING: IOException Reading File " + xmlFileName + "." ) ;
			return null ;
		}
		catch( SAXException sxe )
		{
			System.err.println( "SAXException Reading File " + xmlFileName + "." ) ;
			sxe.printStackTrace() ;
			return null ;
		}
		Vector<String> projectIds = new Vector<String>() ;
		Vector<ProjectData> projectData = new Vector<ProjectData>() ;
		if( projectDoc != null )
		{
			int nElements = getSize( projectDoc , "SpMSBSummary" ) ;
			for( int i = 0 ; i < nElements ; i++ )
			{
				String value = getValue( getElement( projectDoc , "SpMSBSummary" , i ) , "projectid" ) ;
				if( !( projectIds.contains( value ) ) )
				{
					String priority = getValue( getElement( projectDoc , "SpMSBSummary" , i ) , "priority" ) ;
					StringTokenizer st = new StringTokenizer( priority , "." ) ;
					priority = st.nextToken() ;
					Integer iPriority = new Integer( priority ) ;
					projectData.add( new ProjectData( value , iPriority ) ) ;
					projectIds.add( value ) ;
				}
			}
		}
		projectData.add( 0 , new ProjectData( "All" , new Integer( 0 ) ) ) ;
		return projectData ;
	}

	public static void clearProjectData()
	{
		File pdf = new File( System.getProperty( "msbSummary" ) + "." + System.getProperty( "user.name" ) ) ;
		if( pdf.exists() && pdf.canWrite() )
			pdf.delete() ;
	}

	public static int getProjectCount()
	{
		return getProjectData().size() ;
	}

	public static String getProjectAt( int index )
	{
		Vector<ProjectData> data = getProjectData() ;
		ProjectData projectData = data.elementAt( index ) ;
		return projectData.projectID ;
	}

	public static String getPriorityAt( int index )
	{
		// Get the data
		Vector<ProjectData> data = getProjectData() ;
		ProjectData projectData = data.elementAt( index ) ;
		return projectData.priority.toString() ;
	}

	public static class SAXHandler extends DefaultHandler
	{
		private String _currentName ;
		private final String _titleElement = "projectid" ;
		private final String _priority = "priority" ;
		private boolean getNextPriority = false ;
		private Vector<String> ids = new Vector<String>() ;
		private Vector<Integer> priorities = new Vector<Integer>() ;
		int idCount = 0 ;

		public void startDocument()
		{
			ids.clear() ;
			priorities.clear() ;
		}

		public void startElement( String namespaceURI , String localName , String qName , Attributes attr )
		{
			_currentName = qName ;
		}

		public void characters( char[] ch , int start , int length )
		{
			if( _currentName.equals( _titleElement ) )
			{
				idCount++ ;
				String s = new String( ch ) ;
				s = s.substring( start , start + length ) ;
				s = s.trim() ;
				// This was added to trap some random problems with the SAX parser.  
				// I don't really know why it happens, but this trap should get rid of them
				if( !s.equals( "" ) && !ids.contains( s ) && start != 0 && length != 1 )
				{
					ids.add( s ) ;
					getNextPriority = true ;
				}
			}
			else if( _currentName.equals( _priority ) && getNextPriority == true )
			{
				String s = new String( ch ) ;
				s = s.substring( start , start + length ) ;
				StringTokenizer st = new StringTokenizer( s , "." ) ;
				s = st.nextToken() ;
				priorities.add( new Integer( s ) ) ;
				getNextPriority = false ;
			}
		}

		public Vector<String> getProjectIds()
		{
			return ids ;
		}

		public Vector<Integer> getPriorities()
		{
			return priorities ;
		}
	}

}//end class

