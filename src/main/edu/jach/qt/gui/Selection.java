package edu.jach.qt.gui ;

import gemini.sp.SpItem ;
import gemini.sp.SpProg ;

public class Selection
{
	private static final Selection self = new Selection() ;
	private static SpItem selectedItem = null ;
	private static final Class<SpProg> spProg = SpProg.class ;

	public static final int DEFERRED = 1 ;
	public static final int NOTDEFERRED = 0 ;
	public static final int NOTSET = -1 ;
	
	private static int ISDEFERRED = NOTSET ;
	
	private Selection(){}

	public static synchronized SpItem selection()
	{
		return selectedItem ;
	}
	
	public static synchronized int deferred()
	{
		return ISDEFERRED ;
	}

	public static synchronized void setSelection( SpItem item , boolean deferred )
	{
		if( item != null )
		{		
			if( !deferred && !spProg.isInstance( item ) )
				selectedItem = item.getRootItem() ;
			else
				selectedItem = item ;
			
			if( deferred )
				ISDEFERRED = DEFERRED ;
			else
				ISDEFERRED = NOTDEFERRED ;
		}
		else
		{
			clear() ;
		}
	}
	
	public static synchronized void clear()
	{
		selectedItem = null ;
		ISDEFERRED = NOTSET ;
	}
	
	private static synchronized void squeek()
	{
		if( selectedItem != null  )
			System.out.println( "Currently selected item is " + selectedItem.getTitle() ) ;
		else
			System.out.println( "Currently selected item is null" ) ;
	}
}
