/*
 * Copyright (C) 2006-2008 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.utils ;

import java.util.TreeMap ;

import java.util.zip.CRC32 ;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;

/**
 * 
 * QueuedMap is a map that hashes the key independently of the maps own implementation.
 * As to why you would want to do this, you probably don't.
 */

public class QueuedMap
{
	protected TreeMap<String,Object> treeMap = null ;
	private static QueuedMap queuedMap = null ;
	public String algorythm = "SHA" ;
	private static boolean tryDigest = true ;

	protected QueuedMap()
	{
		treeMap = new TreeMap<String,Object>() ;
	}

	public static synchronized QueuedMap getQueuedMap()
	{
		if( queuedMap == null )
			queuedMap = new QueuedMap() ;
		return queuedMap ;
	}

	public boolean put( String key , Object value )
	{
		if( key == null )
			return false ;
		String hashed = hash( key ) ;
		boolean replacement = treeMap.containsKey( hashed ) ;
		treeMap.put( hashed , value ) ;
		return replacement ;
	}

	public boolean contains( String key )
	{
		if( key == null )
			return false ;
		String hashed = hash( key ) ;
		return treeMap.containsKey( hashed ) ;
	}

	protected String hash( String input )
	{
		if( input == null )
			return "" ;
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
