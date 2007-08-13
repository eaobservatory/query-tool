package edu.jach.qt.gui;

import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import java.net.URL;
import java.util.Vector;
import javax.swing.JLabel ;
import javax.swing.SwingConstants ;
import javax.swing.ImageIcon ;
import javax.swing.JFrame ;
import javax.swing.JButton ;
import javax.swing.Timer ;

/**
 * LogoPanel.java
 *
 *
 * Created: Mon Apr  8 09:43:18 2002
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class LogoPanel extends JLabel implements Runnable , ActionListener
{
	static int frameNumber = -1;
	int delay;
	Thread animatorThread;
	static boolean frozen = false;
	Timer timer;
	Vector images;

	/**
	 * Constructor.
	 * Builds the QT logo and user interface.
	 */
	public LogoPanel()
	{
		setHorizontalAlignment( SwingConstants.CENTER );

		images = new Vector();
		try
		{
			for( int i = 1 ; i <= 10 ; i++ )
				images.add( i - 1 , new ImageIcon( new URL( "file:///jac_sw/omp/QT/icons/QtLogo" + i + ".png" ) ) );

			buildUI();

			setIcon( new ImageIcon( new URL( "file:///jac_sw/omp/QT/icons/QtLogo.png" ) ) );
		}
		catch( Exception e ){}
	}

	//Note: Container must use BorderLayout, which is the default layout manager for content panes.
	/**
	 * Sets up animation of the QT interface.
	 */
	void buildUI()
	{
		int fps = 40;

		//How many milliseconds between frames?
		delay = ( fps > 0 ) ? ( 1000 / fps ) : 100;

		//Set up a timer that calls this object's action handler
		timer = new javax.swing.Timer( delay , this );
		timer.setInitialDelay( 0 );
		timer.setCoalesce( true );
	}

	/**
	 * Starts the QT logo animation sequence.
	 */
	public void start()
	{
		startAnimation();
	}

	/**
	 * Stops the QT logo animation sequence.
	 */
	public void stop()
	{
		stopAnimation();
		try
		{
			setIcon( new ImageIcon( new URL( "file:///jac_sw/omp/QT/icons/QtLogo.png" ) ) );
			frameNumber = -1;
		}
		catch( Exception e ){}
	}

	/**
	 * Starts the QT logo animation sequence.
	 */
	public synchronized void startAnimation()
	{
		if( frozen )
		{
			//Do nothing.  The user has requested that we stop changing the image.
		}
		else
		{
			//Start animating!
			if( !timer.isRunning() )
				timer.start();
		}
	}

	/**
	 * Stops the QT logo animation sequence.
	 */
	public synchronized void stopAnimation()
	{
		//Stop the animating thread.
		if( timer.isRunning() )
			timer.stop();
	}

	/**
	 * Implementation of <code>ActionListener</code> interface.
	 * Stops the interface scrolling.
	 * @param e   An <code>ActionEvent</code>.
	 */
	public void actionPerformed( ActionEvent e )
	{
		frameNumber++ ;
		setIcon( ( ( ImageIcon )images.elementAt( LogoPanel.frameNumber % 10 ) ) );
	}

	/** 
	 * Implementation of the <code>Runnable</code> interface.
	 */
	public void run(){}

	//Invoked only when this is run as an application.
	public static void main( String[] args )
	{
		JFrame f = new JFrame( "ImageSequenceTimer" );
		JButton b = new JButton( "Start" );
		final LogoPanel logoPanel = new LogoPanel();

		f.addWindowListener( new WindowAdapter()
		{
			public void windowClosing( WindowEvent e )
			{
				System.exit( 0 );
			}
		} );

		f.getContentPane().add( logoPanel );
		f.setVisible( true );
		f.getContentPane().add( b , "South" );

		b.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				logoPanel.start();
			}
		} );
	}
}// LogoPanel
