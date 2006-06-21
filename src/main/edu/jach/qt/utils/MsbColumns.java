package edu.jach.qt.utils ;

public class MsbColumns extends OrderedMap
{	
	public MsbColumns()
	{
		super() ;
	}

	public void add( MsbColumnInfo msbColumnInfo )
	{
		super.add( msbColumnInfo.getName() , msbColumnInfo ) ;
	}
	
	public MsbColumnInfo removeIndex( int index )
	{
		MsbColumnInfo msbColumnInfo = null ;
		Object object = super.remove( index ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo removeName( String name )
	{
		MsbColumnInfo msbColumnInfo = null ;
		Object object = super.remove( name ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
        return msbColumnInfo ;
	}

	public MsbColumnInfo findName( String name )
	{
		MsbColumnInfo msbColumnInfo = null ;
		Object object = super.find( name ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo findIndex( int index )
    {
		MsbColumnInfo msbColumnInfo = null ;
		Object object = super.find( index ) ;
		if( object instanceof MsbColumnInfo )
		        msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
    }

	public void setVisibility( String name , boolean visible )
	{
		MsbColumnInfo msbColumnInfo = findName( name ) ;
		if( msbColumnInfo != null )
			msbColumnInfo.setVisible( visible ) ;
	}

	public void setVisibility( int index , boolean visible )
	{
		MsbColumnInfo msbColumnInfo = findIndex( index ) ;
		if( msbColumnInfo != null )
                	msbColumnInfo.setVisible( visible ) ;
	}
	
	public boolean getVisibility( String name )
	{
		MsbColumnInfo msbColumnInfo = findName( name ) ;
		if( msbColumnInfo != null )
			return msbColumnInfo.getVisible() ;
		return false ;
	}

	public boolean getVisibility( int index )
	{
		MsbColumnInfo msbColumnInfo = findIndex( index ) ;
		if( msbColumnInfo != null )
			return msbColumnInfo.getVisible() ;
		return false ;
	}	
}
