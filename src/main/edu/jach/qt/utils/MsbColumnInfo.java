package edu.jach.qt.utils ;

public class MsbColumnInfo
{

	String name ; 
	Class klass ;
	boolean visible = true ;

	public MsbColumnInfo( String name , String klassType )
	{
		this.name = name ;
		if( klassType.equalsIgnoreCase( "Integer" ) )
			klass = Integer.class ;
		else if( klassType.equalsIgnoreCase( "Float" ) ) 
			klass = Number.class ;
		else 
			klass = String.class ;
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
}
