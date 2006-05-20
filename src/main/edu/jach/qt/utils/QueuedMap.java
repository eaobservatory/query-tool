package edu.jach.qt.utils ;

import java.util.TreeMap ;

import java.util.zip.CRC32 ;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;

public class QueuedMap
{

	protected TreeMap treeMap = null ;
	private static QueuedMap queuedMap = null ;
	public String algorythm = "SHA" ;
	private static boolean tryDigest = true ;

	protected QueuedMap()
	{
		treeMap = new TreeMap() ;
	}

	public static synchronized QueuedMap getQueuedMap()
	{
		if( queuedMap == null )
			queuedMap = new QueuedMap() ;
		return queuedMap ;
	}

	public boolean put( String key , Object value )
	{
		String hashed = hash( key ) ;
		boolean replacement = treeMap.containsKey( hashed ) ;
		treeMap.put( hashed , value ) ;
		return replacement ;
	}
	
	public boolean contains( String key )
	{
		String hashed = hash( key ) ;
		return treeMap.containsKey( hashed ) ;
	}
	
	protected String hash( String input )
	{
		String hashed = input ;
		byte[] bytes = input.getBytes() ; 
		if( tryDigest )
		{
			try
			{
				MessageDigest md = MessageDigest.getInstance( algorythm ) ;
				md.update( bytes ) ;
				return new String( md.digest() ) ;
			}
			catch( NoSuchAlgorithmException nsae )
			{
				System.out.println( "Cannot use " + algorythm + " falling back to CRC32" ) ;
				tryDigest = false ;
			}
		}
		else
		{
			CRC32 crc32 = new CRC32() ;
			crc32.update( bytes ) ;
			hashed = String.valueOf( crc32.getValue() ) ;
		}
		return hashed ;
	}
}
