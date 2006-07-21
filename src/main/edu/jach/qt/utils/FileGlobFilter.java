package edu.jach.qt.utils ;

import java.io.FilenameFilter ;
import java.io.File ;

public class FileGlobFilter implements FilenameFilter
{

	String filterExpression ;

	public FileGlobFilter( String expression )
	{
		filterExpression = expression ;
	}

	public boolean accept( File dir , String name )
	{
		if( name.matches( filterExpression ) )
			return true ;
		return false ;
	}
}
