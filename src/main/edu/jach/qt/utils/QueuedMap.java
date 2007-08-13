package edu.jach.qt.utils;

import java.util.TreeMap;

import java.util.zip.CRC32;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * QueuedMap is a map that hashes the key independantly of the maps own implementation.
 * As to why you would want to do this, you probably don't.
 */

public class QueuedMap
{
	protected TreeMap treeMap = null;
	private static QueuedMap queuedMap = null;
	public String algorythm = "SHA";
	private static boolean tryDigest = true;

	protected QueuedMap()
	{
		treeMap = new TreeMap();
	}

	public static synchronized QueuedMap getQueuedMap()
	{
		if( queuedMap == null )
			queuedMap = new QueuedMap();
		return queuedMap;
	}

	public boolean put( String key , Object value )
	{
		if( key == null )
			return false;
		String hashed = hash( key );
		boolean replacement = treeMap.containsKey( hashed );
		treeMap.put( hashed , value );
		return replacement;
	}

	public boolean contains( String key )
	{
		if( key == null )
			return false;
		String hashed = hash( key );
		return treeMap.containsKey( hashed );
	}

	protected String hash( String input )
	{
		if( input == null )
			return "";
		String hashed = input;
		byte[] bytes = input.getBytes();
		if( tryDigest )
		{
			try
			{
				MessageDigest md = MessageDigest.getInstance( algorythm );
				md.update( bytes );
				return new String( md.digest() );
			}
			catch( NoSuchAlgorithmException nsae )
			{
				System.out.println( "Cannot use " + algorythm + " falling back to CRC32" );
				tryDigest = false;
			}
		}
		else
		{
			CRC32 crc32 = new CRC32();
			crc32.update( bytes );
			hashed = String.valueOf( crc32.getValue() );
		}
		return hashed;
	}
}
