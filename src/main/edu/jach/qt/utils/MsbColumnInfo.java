package edu.jach.qt.utils ;

import java.util.Vector ;

public class MsbColumnInfo
{

	String name , klass ;
	boolean visible = true ;
	Vector vector ;

	public MsbColumnInfo( String name , String klass )
	{
		this.name = name ;
		this.klass = klass ;
		vector = new Vector() ;
	}

	public String getName()
	{
		return name ;
	}

	public Class getClassType()
	{
		if( klass.equalsIgnoreCase( "Integer" ) || klass.equalsIgnoreCase( "Integer" ) )
			return Number.class ;
		return String.class ;
	}

	public void setVisible( boolean visible )
	{
		this.visible = visible ;
	}

	public boolean getVisible()
	{
		return visible ;
	}

	public Vector getVector()
	{
		return vector ;
	}
	
	public void clearVector()
	{
		vector.clear() ;
	}
	
	public void addToVector( Object object )
	{
		vector.add( object ) ;
	}
}
