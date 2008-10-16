package edu.jach.qt.utils ;

public class MsbColumns extends OrderedMap<String,MsbColumnInfo>
{
	public void add( MsbColumnInfo msbColumnInfo )
	{
		super.add( msbColumnInfo.getName() , msbColumnInfo ) ;
	}

	public MsbColumnInfo removeIndex( final int index )
	{
		final MsbColumnInfo msbColumnInfo = super.remove( index ) ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo removeName( final String name )
	{
		final MsbColumnInfo msbColumnInfo = super.remove( name ) ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo findName( final String name )
	{
		final MsbColumnInfo msbColumnInfo = super.find( name ) ;
		return msbColumnInfo ;
	}

	public MsbColumnInfo findIndex( final int index )
	{
		final MsbColumnInfo msbColumnInfo = super.find( index ) ;
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
