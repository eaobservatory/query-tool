package edu.jach.qt.utils ;

import javax.swing.JWindow ;
import javax.swing.JFrame ;
import javax.swing.SwingUtilities;
import java.awt.Dimension ;

import javax.swing.JProgressBar ;

public class Splash
{
	private JWindow window ;
	private int width = 200 ;
	private int height = 30 ;

	public Splash(final String string)
	{
            try {
                SwingUtilities.invokeAndWait(new Runnable() {public void run() {
                    window = new JWindow();
                    window.setSize( width , height ) ;
                    Dimension screenSize = window.getToolkit().getScreenSize() ;
                    Dimension windowSize = window.getSize() ;
                    window.setLocation( ( ( screenSize.width >> 1 ) - ( windowSize.width >> 1 ) ) , ( ( screenSize.height >> 1 ) - ( windowSize.height >> 1 ) ) ) ;

                    JProgressBar progress = new JProgressBar( 0 , windowSize.width ) ;
                    progress.setIndeterminate( true ) ;
                    progress.setDoubleBuffered( true ) ;
                    if( string != null )
                    {
                        progress.setString( string ) ;
                        progress.setStringPainted( true ) ;
                    }

                    window.add( progress ) ;
                    window.setVisible( true ) ;
                }});
            }
            catch (Exception e) {
                e.printStackTrace();
            }
	}

	public Splash()
	{
		this(null);
	}

	public void done()
	{
		if( window != null )
		{
                    SwingUtilities.invokeLater(new Runnable() {public void run() {
			window.setVisible( false ) ;
			window.dispose() ;
			window = null ;
                    }});
		}
	}
}
