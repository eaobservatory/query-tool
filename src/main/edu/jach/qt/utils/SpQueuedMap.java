package edu.jach.qt.utils ;

import gemini.sp.SpItem;
import gemini.sp.SpMSB;
import gemini.sp.SpObs ;

import java.io.File ;
import java.io.IOException ;
import java.util.Calendar;
import java.util.TimeZone ;

public class SpQueuedMap extends QueuedMap
{
	private static SpQueuedMap queuedMap = null ;
	
	private SpQueuedMap() 
	{
		super() ;
	}

	public static synchronized SpQueuedMap getSpQueuedMap()
	{
		if( queuedMap == null )
			queuedMap = new SpQueuedMap() ;
		return queuedMap ;
	}
	
	public boolean putSpItem( final SpItem item )
	{
		if( item == null )
			return false ;
		boolean replacement = false ;
		final String checksum = msbChecksum( item ) ;
		if( !checksum.equals( "" ) )
		{
			replacement = treeMap.containsKey( checksum ) ;
			if( (( SpMSB )item).getNumberRemaining() < 2 )
				treeMap.put( checksum , "" + System.currentTimeMillis() ) ;
			writeChecksumToDisk( ( SpMSB )item ) ;
		}
		return replacement ;
	}
	
	public boolean containsSpItem( SpItem item )
	{
		if( item == null )
			return false ;
		item = getCorrectItem( item ) ;
		String checksum = msbChecksum( item ) ;
		return treeMap.containsKey( checksum ) ;
	}
	
	public String containsMsbChecksum( String checksum )
	{
		if( checksum == null )
			return null ;
		if( treeMap.containsKey( checksum ) )
			return convertTimeStamp( ( String )treeMap.get( checksum ) ) ;
		String onDisk = isChecksumOnDisk( checksum ) ;
		if( onDisk != null )
			treeMap.put( checksum , onDisk ) ;
		return convertTimeStamp( onDisk ) ;
	}
	
	private String msbChecksum( final SpItem item )
	{
		if( item instanceof SpMSB ) 
		{
			final SpMSB msb = ( SpMSB )item ;
			return msb.getChecksum() ;
		}		
		return "" ;
	}

	private SpItem getCorrectItem( SpItem item )
	{
		SpItem spitem = ( SpItem )item ;
		if( item instanceof SpObs )
		{
			final SpItem parent = spitem.parent() ;
			if( parent instanceof SpMSB )
				spitem = parent ;
		}		
		return spitem ;
	}

	private boolean writeChecksumToDisk( SpMSB item )
	{
		boolean success = false ;
		String path = getChecksumCachePath() ;
		String checksum = msbChecksum( item ) ;
		int remaining = item.getNumberRemaining() ;
		File file = new File( path + checksum + "_" + remaining + "_" + System.currentTimeMillis() ) ;
		try
		{
			 success = file.createNewFile() ;
		}
		catch( IOException ioe ){}
		return success ;
	}

	private String isChecksumOnDisk( String checksum )
	{
		String success = null ;
		String path = getChecksumCachePath() ;
		File filePath = new File( path ) ;
		FileGlobFilter filter = new FileGlobFilter( "^" + checksum + "_-?\\d+_\\d*" ) ;
		String[] directoryContents = filePath.list( filter ) ;
		int highestRepeatCount = 0 ;
		long nearestDate = 0 ;
		for( int index = 0 ; index < directoryContents.length ; index++ )
		{
			String[] split = directoryContents[ index ].split( "_" ) ;
			try
			{
				int repeatCount = Integer.parseInt( split[ 1 ] ) ;
				if( repeatCount > highestRepeatCount )
					highestRepeatCount = repeatCount ;
				long date = Long.parseLong( split[ 2 ] ) ;
				if( date > nearestDate )
					nearestDate = date ;
			}
			catch( NumberFormatException nfe )
			{
				highestRepeatCount = 0 ;
			}
		}
		if( directoryContents.length != 0 && highestRepeatCount <= directoryContents.length )
			success = "" + nearestDate ;
		
 		return success ;
	}

	String cachePath = null ;
	
	private String getChecksumCachePath()
	{
		if( cachePath == null )
		{
			Calendar calendar = Calendar.getInstance() ;
			TimeZone timeZone = TimeZone.getTimeZone( "UTC" ) ;
			calendar.setTimeZone( timeZone ) ;
			String date = calendar.get( Calendar.YEAR ) + "" + calendar.get( Calendar.MONTH ) + "" + calendar.get( Calendar.DATE ) + File.separator ;

			String cacheFiles = File.separator + System.getProperty( "telescope" ).toLowerCase() + "data" + File.separator ;
			cacheFiles += System.getProperty( "cacheFiles" ) ;
			String home = System.getProperty( "user.home" ) ;
			if( cacheFiles == null || cacheFiles.equals( "" ) )
				cacheFiles = home ;
			if( !cacheFiles.endsWith( File.separator ) )
				cacheFiles += File.separator ;
			
			String QTCacheFiles = cacheFiles + "QT" + File.separator ;

			cachePath = QTCacheFiles + date ;
			File directory = new File( cachePath ) ;
			if( !directory.exists() )
			{
				File QTHomeDirectory = new File( QTCacheFiles ) ;
				deleteDirectory( QTHomeDirectory ) ;
				
				if( !directory.mkdirs() )
				{
					System.out.println( "Unable to create " + cachePath + " using " + home ) ;
					cachePath = home ;
					if( !cachePath.endsWith( File.separator ) )
						cachePath += File.separator ;
				}
			}
		}
		return cachePath ;
	}
	
	private void deleteDirectory( File directory )
	{
		if( directory.exists() && directory.canWrite() && directory.isDirectory() )
		{
			File[] contents = directory.listFiles() ;
			File temp ;
			for( int i = 0 ; i < contents.length ; i++ )
			{
				temp = contents[ i ] ;
				if( temp.isDirectory() )
					deleteDirectory( temp ) ;
				temp.delete() ;
			}
			directory.delete() ;
		}		
	}
	
	private String convertTimeStamp( String time )
	{
		String returnString = null ;
		try
		{
			long parsedTime = Long.parseLong( time ) ;
			returnString = "" ;
			long currentTime = System.currentTimeMillis() ;
			long difference = currentTime - parsedTime ;
			long seconds = difference / 1000 ;
			long minutes = seconds / 60 ;
			long hours = minutes / 60 ;
			minutes %= 60 ;
			if( hours != 0 )
			{
				returnString += hours + " hour" ;
				if( hours > 1 )
					returnString += "s" ;
				returnString += " " ;
			}
			if( minutes != 0 )
			{
				returnString += minutes + " minute" ;
				if( minutes > 1 )
					returnString += "s" ;
				returnString += " " ;
			}
			if( returnString.equals( "" ) )
				returnString += seconds + " seconds " ;
			returnString += "ago" ;
		}
		catch( NumberFormatException nfe ){}
		return returnString ;
	}
	
	public void fillCache()
	{
		String path = getChecksumCachePath() ;
		File filePath = new File( path ) ;
		FileGlobFilter filter = new FileGlobFilter( "^\\w+_-?\\d+_\\d*" ) ;
		String[] directoryContents = filePath.list( filter ) ;
		for( int index = 0 ; index < directoryContents.length ; index++ )
		{
			String[] split = directoryContents[ index ].split( "_" ) ;
			String checksum =  split[ 0 ] ;
			String timestamp = isChecksumOnDisk( split[ 0 ] ) ;
			if( timestamp != null )
				treeMap.put( checksum , timestamp ) ;
		}
	}
}
