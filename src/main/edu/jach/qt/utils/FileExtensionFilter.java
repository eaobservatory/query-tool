package edu.jach.qt.utils;

import java.io.FilenameFilter;
import java.io.File;

public class FileExtensionFilter implements FilenameFilter
{
	String extension ;

	public FileExtensionFilter( String fileExtension )
	{
		if( fileExtension == null )
			throw new RuntimeException( "You asked for a null file extension." ) ;
		
		fileExtension = fileExtension.trim();
		
		if( fileExtension.equals( "" ) )
			throw new RuntimeException( "You asked an empty file extension." ) ;
		
		if( fileExtension.lastIndexOf( "." ) > 0 )
			throw new RuntimeException( "The file extension you gave contained '.'s other than the initial one." ) ;
		
		if( !fileExtension.startsWith( "." ) )
			extension = "." + fileExtension ;
		else
			extension = fileExtension ;
	}

	public boolean accept( File dir , String name )
	{
		return ( name.endsWith( extension ) ) ;
	}
}
