package edu.jach.qt.utils ;

/**
 * This exception is thrown when the user specifies an invalid
 * user name on the MSB Done dialog box.
 *
 * @author $Author$
 * @version $Header$
 */

public class InvalidUserException extends Exception
{
	InvalidUserException( String message )
	{
		super( message ) ;
	}
}
