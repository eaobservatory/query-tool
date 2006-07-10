package edu.jach.qt.utils ;

public class MsbColumns extends OrderedMap
{	

	public void add( MsbColumnInfo msbColumnInfo )
	{
		super.add( msbColumnInfo.getName() , msbColumnInfo ) ;
	}
	
	public MsbColumnInfo removeIndex( final int index )
	{
		MsbColumnInfo msbColumnInfo = null ;
		final Object object = super.remove( index ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo removeName( final String name )
	{
		MsbColumnInfo msbColumnInfo = null ;
		final Object object = super.remove( name ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
        return msbColumnInfo ;
	}

	public MsbColumnInfo findName( final String name )
	{
		MsbColumnInfo msbColumnInfo = null ;
		final Object object = super.find( name ) ;
		if( object instanceof MsbColumnInfo )
			msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo findIndex( final int index )
    {
		MsbColumnInfo msbColumnInfo = null ;
		final Object object = super.find( index ) ;
		if( object instanceof MsbColumnInfo )
		        msbColumnInfo = ( MsbColumnInfo )object ;
		return msbColumnInfo ;
    }

	public void setVisibility( String name , boolean visible )
	{
		final MsbColumnInfo msbColumnInfo = findName( name ) ;
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
