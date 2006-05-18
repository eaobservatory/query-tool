package edu.jach.qt.utils;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.apache.soap.*;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.literalxml.XMLParameterSerializer;
import org.apache.soap.rpc.*;
import org.apache.soap.util.xml.QName;


/**
 * <code>SoapClient.java</code>
 *
 *
 * Created: Mon Aug 27 18:30:31 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>,
 * modified by Martin Folger
 *
 * $Id$ 
 */
public class SoapClient {

  private static Header header = null;
  private static Vector params = new Vector();

    public static final String FAULT_CODE_INVALID_USER = "SOAP-ENV:Client.InvalidUser";
   
  /**
   * <code>addParameter</code>. Add a Parameter to the next Call that
   * is to take place.
   *
   * @param name a <code>String</code> value. The name of the
   * parameter to be added to next soap Call.

   * @param type a <code>Class</code> value. The explicit Class of
   * this parameter.

   * @param val an <code>Object</code> value. The object to register this Parameter with.
   */
  protected static void addParameter(String name, Class type, Object val) {
    params.add(new Parameter(name, type, val, null));
  }
    
  /**
   * <code>flushParameter</code> Clear the Vector of
   * <code>Parameters</code> Objects to be sent in the next Call.
   * 
   */
  protected static void flushParameter() {
    params.clear();
  }
    
  /**
	 * <code>doCall</code> Send the Call with various configurations.
	 *
	 * @param url an <code>URL</code> val indicating the soap server to
	 * connect to.
	 * @param soapAction a <code>String</code>. The URN like
	 * "urn:OMP::MSBServer"
	 * @param methodName a <code>String</code> The name of the method to
	 * call in the server.
	 * @return an <code>Object</code> returned by the method called in
	 * the server .
	 */
	protected static Object doCall( URL url , String soapAction , String methodName ) throws Exception
	{
		Object obj = new Object();

		try
		{
			Call call = new Call();

			call.setTargetObjectURI( soapAction );
			call.setEncodingStyleURI( Constants.NS_URI_SOAP_ENC );
			call.setMethodName( methodName );
			call.setParams( params );
			if( header != null )
				call.setHeader( header );
			soapAction += "#" + methodName;

			Response resp = call.invoke( url , soapAction );

			// check response 
			if( !resp.generatedFault() )
			{
				Parameter ret = resp.getReturnValue();
				if( ret == null )
					obj = null;
				else
					obj = ret.getValue();

				//Reset the params vector.
				params.clear();

				//return result;
				return obj;
			}
			else
			{
				Fault fault = resp.getFault();

				if( fault.getFaultCode().equals( FAULT_CODE_INVALID_USER ) )
				{
					throw new InvalidUserException( fault.getFaultString() );
				}
				JOptionPane.showMessageDialog( null , "Code:    " + fault.getFaultCode() + "\n" + "Problem: " + fault.getFaultString() , "Error Message" , JOptionPane.ERROR_MESSAGE );
			}

		}
		catch( InvalidUserException e )
		{
			throw e;
		}
		catch( SOAPException se )
		{
			JOptionPane.showMessageDialog( null , se.getMessage() , "SOAP Exception" , JOptionPane.ERROR_MESSAGE ) ;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
}

