package edu.jach.qt.utils ;

import gemini.sp.SpRootItem ;
import gemini.sp.SpItem ;
import gemini.sp.SpObs ;
import gemini.sp.SpTreeMan ;

import gemini.sp.obsComp.SpInstObsComp ;

import jsky.app.ot.OtFileIO ;
import jsky.app.ot.OtCfg ;

import java.util.Enumeration ;

import java.io.File ;
import java.io.BufferedReader ;
import java.io.FileReader ;

public class UkirtTranslator
{
	SpRootItem _root ;
	static boolean _useClassic = false ;
	static File _inputFile = null ;
	static String _outDir = null ;

	public UkirtTranslator() throws Exception
	{
		System.setProperty( "EXEC_PATH" , _outDir ) ;
		System.setProperty( "CONF_PATH" , _outDir ) ;
		OtCfg.init() ;
		BufferedReader rdr = new BufferedReader( new FileReader( _inputFile ) ) ;
		try
		{
			_root = OtFileIO.fetchSp( rdr ) ;
		}
		catch( Exception e )
		{
			System.out.println( "Can not access file " + _inputFile.getName() ) ;
			System.exit( 1 ) ;
		}

		getObservations( _root ) ;
	}

	private void getObservations( SpItem root )
	{
		Enumeration children = root.children() ;
		String rootTitle = root.getTitleAttr() ;
		if( rootTitle == null || rootTitle.equals( "" ) )
			rootTitle = root.typeStr() ;

		while( children.hasMoreElements() )
		{
			SpItem child = ( SpItem )children.nextElement() ;
			if( child.getClass().getName().endsWith( "SpObs" ) )
				doTranslate( rootTitle , ( SpObs )child ) ;
			else if( child.getClass().getName().endsWith( "SpMSB" ) )
				getObservations( child ) ;
		}
	}

	private void doTranslate( String parentName , SpObs obs )
	{
		String obsName = obs.getTitleAttr() ;
		if( obsName == null || obsName.equals( "" ) )
			obsName = obs.typeStr() ;

		String tname ;

		try
		{
			SpInstObsComp inst = SpTreeMan.findInstrument( obs ) ;
			if( inst == null )
				throw new Exception( "No instrument selected" ) ;
			String instName = inst.type().getReadable() ;
			tname = QtTools.translate( obs , instName ) ;
		}
		catch( Exception e )
		{
			System.out.println( "Error translating " + parentName + ":" + obsName ) ;
			return ;
		}
		System.out.println( "Translation for \"" + parentName + ":" + obsName + "\" stored in " + tname ) ;
	}

	public static void main( String[] args )
	{
		// Should take one argument - the name of the input XML file
		try
		{
			for( int i = 0 ; i < args.length ; i++ )
			{
				if( args[ i ].equals( "-classic" ) )
					_useClassic = true ;
				else if( args[ i ].equals( "-i" ) || args[ i ].equals( "--inputFile" ) )
					_inputFile = new File( args[ ++i ] ) ;
				else if( args[ i ].equals( "-o" ) || args[ i ].equals( "--outDir" ) )
					_outDir = args[ ++i ] ;
				else
					System.out.println( "Unknown option " + args[ i ] + " ignored" ) ;
			}
		}
		catch( Exception e )
		{
			System.out.println( "Incorrect usage ; ukirtTranslator (-classic) (-i inputFile) (-o outputDir)" ) ;
			System.exit( 1 ) ;
		}

		if( _inputFile == null )
		{
			File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) ) ;
			File[] files = tmpDir.listFiles() ;
			for( int i = 0 ; i < files.length ; i++ )
			{
				if( files[ i ].getName().startsWith( "msb.xml" ) )
				{
					if( _inputFile == null )
					{
						_inputFile = files[ i ] ;
					}
					else
					{
						if( _inputFile.lastModified() < files[ i ].lastModified() )
							_inputFile = files[ i ] ;
					}
				}
			}
		}

		if( _outDir == null )
			_outDir = System.getProperty( "user.dir" ) ;

		try
		{
			UkirtTranslator r = new UkirtTranslator() ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}
	}
}
