package edu.jach.qt.utils ;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger ;
import java.util.logging.FileHandler ;
import java.util.logging.SimpleFormatter ;

public class JACLogger
{
	private static JACLogger jacLogger = new JACLogger() ;
	Logger logger = Logger.getAnonymousLogger() ;
	
	private JACLogger()
	{
		String logDir = System.getProperty( "QT_LOG_DIR" ) ;
    	if( logDir != null && !"".equals( logDir ) )
    	{
            try
            {
            	if( !logDir.endsWith( "/" ) )
            		logDir += "/" ;
            	FileHandler handler = new FileHandler( logDir + "QT.log" ) ;
            	handler.setFormatter( new SimpleFormatter() ) ;
            	logger.addHandler( handler ) ;
            }
            catch( SecurityException e ){ e.printStackTrace() ; }
            catch( IOException e ){ e.printStackTrace() ; }
		}
	}
	
	public static JACLogger getLogger()
	{
		return jacLogger ;
	}
	
	public static JACLogger getLogger( Class<?> klass )
	{
		return getLogger() ;
	}
	
	public static JACLogger getRootLogger()
	{
		return getLogger() ;
	}
	
	public void error( String msg )
	{
		logger.severe( msg ) ;
	}
	
	public void error( String msg , Throwable thrown )
	{
		logger.log( Level.SEVERE , msg , thrown ) ;
	}
	
	public void debug( String msg )
	{
		logger.fine( msg ) ;
	}
	
	public void debug( String msg , Throwable thrown )
	{
		logger.log( Level.FINE , msg , thrown ) ;
	}
	
	public void info( String msg )
	{
		logger.info( msg ) ;
	}
	
	public void warn( String msg )
	{
		logger.warning( msg ) ;
	}
	
	public void warn( String msg , Throwable thrown )
	{
		logger.log( Level.WARNING , msg , thrown ) ;
	}
	
	public void fatal( String msg )
	{
		logger.severe( msg ) ;
	}
	
	public void fatal( String msg , Throwable thrown )
	{
		logger.log( Level.SEVERE , msg , thrown ) ;
	}
}
