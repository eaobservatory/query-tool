package edu.jach.qt.utils ;

import java.awt.Window ;
import java.awt.Frame ;
import java.awt.Dimension ;

import javax.swing.JProgressBar ;

public class Splash
{

	private Window window ;
	private int width = 160 ;
	private int height = 24 ;

	public Splash( Frame parent , String string )
	{
		window = new Window( parent ) ;
		window.setSize( width, height ) ;
		Dimension screenSize = window.getToolkit().getScreenSize(); 
		Dimension windowSize = window.getSize() ;
		window.setLocation
		( 
			( ( screenSize.width >> 1 ) - ( windowSize.width >> 1 )  ) , 			
			( ( screenSize.height >> 1 ) - ( windowSize.height >> 1 )  ) 
		) ;
		
		JProgressBar progress = new JProgressBar( 0 , windowSize.width ) ;
		progress.setIndeterminate( true ) ;
		progress.setDoubleBuffered( true ) ;
		if( string != null )
		{
			progress.setString( string ) ;
			progress.setStringPainted( true ) ;
		}
		
		window.add( progress ) ;
		window.show() ;
	}

	public Splash()
	{
		this( new Frame() , null ) ;
	}

	public void done()
	{
		if( window != null )
		{
			window.hide() ;
			window.dispose() ;
			window = null ;
		}
	}

}
