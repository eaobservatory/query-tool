package edu.jach.qt.utils ;

import java.util.Vector ;

public class MsbColumnInfo
{

	String name ; 
	Class klass ;
	boolean visible = true ;
	Vector vector ;

	public MsbColumnInfo( String name , String klassType )
	{
		this.name = name ;
		if( klassType.equalsIgnoreCase( "Integer" ) )
			klass = Integer.class ;
		else if( klassType.equalsIgnoreCase( "Float" ) ) 
			klass = Number.class ;
		else 
			klass = String.class ;
		vector = new Vector() ;
	}

	public String getName()
	{
		return name ;
	}

	public Class getClassType()
	{
		return klass ;
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
