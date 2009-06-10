package edu.jach.qt.utils ;

import java.net.Socket ;
import java.net.ServerSocket ;
import java.net.URL;

import java.io.InputStream ;
import java.io.OutputStream ;

import java.io.BufferedOutputStream ;
import java.io.IOException ;

import edu.jach.qt.utils.JACLogger ;

public class CuteProxy
{	
	protected String targetHost = "127.0.0.1" ;
	protected int targetPort = 80 ;
	protected Integer localPort = 8080 ;
	private static boolean active = false ;
	
	private static final JACLogger logger = JACLogger.getLogger( CuteProxy.class ) ;
	
	public static void main( String args[] )
	{
		new CuteProxy( 8080 ) ;
	}

	public CuteProxy( int portNumber )
	{
		if( active )
			throw new RuntimeException( "Already running." ) ;
		localPort = portNumber ;
		active = true ;
		new CuteServer() ;
	}
	
	public boolean setUpLoopback()
	{
		boolean changed = false ;
		String proxyHost = System.getProperty( "http.proxyHost" ) ;
		if( proxyHost == null || "".equals( proxyHost.trim() ) )
		{
			System.setProperty( "http.proxyHost" , "127.0.0.1" ) ;
			System.setProperty( "http.proxyPort" , localPort.toString() ) ;
			changed = true ;
		}
		
		return changed ;
	}
	
	public boolean disableLoopback()
	{
		System.clearProperty( "http.proxyHost" ) ;
		System.clearProperty( "http.proxyPort" ) ;
		return true ;
	}
	
	private boolean setTarget()
	{
		boolean changed = false ;
		try
		{
			URL url = new URL( System.getProperty( "msbServer" ) ) ;
			targetHost = url.getHost() ;
			changed = true ;
		}
		catch( java.net.MalformedURLException mue )
		{
			mue.printStackTrace() ;
		}
		return changed ;
	}
	
	private class CuteServer implements Runnable
	{		
		public CuteServer()
		{
			if( setUpLoopback() && setTarget() )
			{
				Thread thread = new Thread( this ) ;
				thread.start() ;
			}
			else
			{
				logger.error( "Proxy host already set or target URL malformed, aborting proxy setup." ) ;
			}
		}
		
		public void run()
		{	
			int connections = 0 ;
			ServerSocket serverSocket = null ;
			try
			{
				serverSocket = new ServerSocket( localPort ) ;
			}
			catch( IOException ioe )
			{
				System.out.println( ioe ) ;
				System.exit( -1 ) ;
			}
	
			while( true )
			{
				Socket connection = null ;
				Socket remote = null ;
				System.out.println( "Hello, this is Dr.Frasier Crane. I'm listening ..." ) ;
				
				synchronized( this )
				{
					try
					{
							connection = serverSocket.accept() ;
							remote = new Socket( targetHost , targetPort ) ;
							connections++ ;
					}
					catch( IOException ioe )
					{
						System.out.println( ioe.toString() ) ;
					}
		
					if( connection != null && remote != null )
					{
						CutePair in = new CutePair( connection , remote ) ;
						in.setName( "Sender:" + connections ) ;
						
						CutePair out = new CutePair( remote , connection ) ;
						out.setName( "Receiver:" + connections ) ;
						
						in.start() ;
						out.start() ;
					}
				}
			}
		}
	}

	private class CutePair extends Thread
	{
		Socket connectingFrom = null ;
		Socket connectingTo = null ;
		
		public CutePair( Socket connectionFrom , Socket connectingTo )
		{
			this.connectingFrom = connectionFrom ;
			this.connectingTo = connectingTo ;
		}
		
		public void run()
		{
			InputStream in = null ;
			OutputStream out = null ;
			
			try
			{
				in = connectingFrom.getInputStream() ;
				out = new BufferedOutputStream( connectingTo.getOutputStream() ) ;
			}
			catch( IOException ioe )
			{
				System.out.println( ioe.toString() ) ;
				System.exit( -1 ) ;
			}
			
			int readLength = -1 ;
			int totalBytes = -1 ;
			boolean active = true ;
			int bufferSize = 512 ;
			byte[] bytes = new byte[ bufferSize ] ;
			
			try
			{
				while( active )
				{
					readLength = in.read( bytes ) ;
					if( readLength != -1 )
					{
						totalBytes += readLength ;
						System.out.print( getName() + " -> " + totalBytes + "\r" ) ;
						out.write( bytes , 0 , readLength ) ;
					}
					else
					{
						active = false ;
					}
					out.flush() ;
				}
			}
			catch( IOException ioe )
			{
				System.out.println( getName() + " ! " + ioe ) ;
			}
			finally
			{
				System.out.print( getName() + " is attempting to close()" + "\r" ) ;
				if( out != null )
				{
					try
					{
						out.close() ;
					}
					catch( IOException ioe )
					{
						System.out.println( getName() + " had a problem closing " + out + " " + ioe ) ;
					}
				}
			}
			logger.info( getName() + " -> " + totalBytes ) ;
		}
	}
}
