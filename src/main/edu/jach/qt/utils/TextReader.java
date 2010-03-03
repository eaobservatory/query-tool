package edu.jach.qt.utils ;

import java.io.IOException ;
import java.io.PushbackReader ;
import java.io.InputStreamReader ;
import java.io.FileReader ;

/**
 Class TextReader provides methods for reading character type data an input 
 source, either the keyboard or a text file (characters such as digits and 
 letters only).  TextReader can be constructed either from an InputStream
 such as System.in (the keyboard) and in fact is done automatically with

 TextReader keyboard = new TextReader( ) ;
 
 or by specifying the name of a file as a String as in 
 
 TextReader inputFile = new TextReader( "input.data" ) ;

 Written by Stuart Reges at the Univerity of Arizona 6/11/98 with 
 minor modifications by Rick Mercer.
 */

public class TextReader
{
	//--instance variables
	// PushbackReader used here to avoid bugs in the 1.1 BufferedReader class
	// It also allows for a "peek" method.
	private PushbackReader in ; // the input stream

	// true If from keyboard or false when input is from a file 
	private boolean rePrompting ; // users should be prompted, but not files

	/**
	 Construct an object used to obtain input from the keyboard
	 */
	public TextReader()
	{ 
		// pre : input stream is open for reading
		// post: constructs a TextReader object associated with the keyboard
		in = new PushbackReader( new InputStreamReader( System.in ) ) ;
		rePrompting = true ;
	}

	/**
	 Construct an object used to obtain input from the disk file with 
	 only text data such as letters, digits and other characters like * % $ 
	 *
	 * @param fileName     Name of file dor reading.
	 */
	public TextReader( String fileName )
	{ 
		// pre : fileName is the name of a file that can be opened for reading
		// post: constructs a TextReader tied to the given file
		try
		{
			in = new PushbackReader( new FileReader( fileName ) ) ;
			rePrompting = false ;
		}
		catch( Exception e )
		{
			System.out.println( "Can't open input file '" + fileName + "', program terminated" ) ;
			System.exit( 1 ) ;
		}
	}

	/**
	 * Error handling method.
	 *
	 * @param where    <code>String indicating where error occurred.
	 */
	private void error( String where )
	{ 
		// Have a standard way of displaying error messages
		System.out.println( "\n***Failure in " + where + " message. Program terminated***" ) ;
		System.exit( 1 ) ;
	}

	/** 
	 Allows user to read in a True or fAlSe value into a boolean variable. 
	 The case on input does not matter, but only the strings TRUE or FALSE are accepted
	 *
	 * @return    <code>true</code> ot <code>false</code>
	 */
	public boolean readBoolean()
	{ 
		do
		{
			String torf = readWord() ;
			if( torf.equalsIgnoreCase( "true" ) )
				return true ;
			else if( torf.equalsIgnoreCase( "false" ) )
				return false ;
			else
				System.out.println( torf + " is not 'true' or 'false'. Try again" ) ;
		}
		while( true ) ;
	}

	/**
	 Use this method to read in entire lines of data such as
	 a persons full name or an address.  You can also use it to enter
	 a string that maay or may not have blanks spaces. 
	 Precondition: The input is not at end-of-file of the input stream.
	 @return All characters (including blank spaces) up until the enter
	 key is entered. 
	 */
	public String readLine()
	{
		String result = "" ;
		try
		{
			do
			{
				int next = in.read() ;
				if( next == '\r' ) // skip carriage-return on Windows systems
					continue ;
				if( next == -1 || next == '\n' )
					break ;
				result += ( char )next ;
			}
			while( true ) ;
		}
		catch( Exception e )
		{
			error( "readLine" ) ;
		}
		return result ;
	}

	/**
	 Returns the next character on the input stream. Same as read.
	 Precondition: The input is not at end-of-file of the input stream.
	 @return The next character in the input stream.
	 */
	public char readChar()
	{
		return read() ;
	}

	/**
	 Returns the next character on the input stream. Same as readChar.
	 Precondition: The input is not at end-of-file of the input stream.
	 @return The next character in the input stream.
	 */
	public char read()
	{ 
		// pre : not at end-of-file of the input stream
		// post: reads the next character of input and returns it

		char result = ' ' ;
		try
		{
			result = ( char )in.read() ;
			if( result == '\r' ) // skip carriage-return on Windows systems
				result = ( char )in.read() ;
		}
		catch( Exception e )
		{
			System.out.println( "Failure in call on read method, program terminated." ) ;
			System.exit( 1 ) ;
		}
		return result ;
	}

	/**
	 Puts the given character back into the input stream to be read again
	 * @param ch  Character to put back into stream
	 */
	public void unread( char ch )
	{
		try
		{
			in.unread( ( byte )ch ) ;
		}
		catch( Exception e )
		{
			error( "unread" ) ;
		}
	}

	/**
	 Use this to find out what the next character is when what you do depends
	 on it. Especially useful when processing complex file input with unknown
	 amounts of input data. The peek method allows for multiple sentinels
	 @return the next character in the input stream without actually reading it.
	 */
	public char peek()
	{
		int next = 0 ;
		try
		{
			next = in.read() ;
		}
		catch( Exception e )
		{
			error( "peek" ) ;
		}
		if( next != -1 )
			unread( ( char )next ) ;
		return ( char )next ;
	}

	/**
	 Reads one string (terminated by end-of-file or whitespace). This method
	 will skip any leading whitespace. 
	 Precondition stream contains at least one nonwhitespace character
	 @returns The next string in the input disk file or 
	 that is typed at the keyboard.
	 */
	public String readWord()
	{
		String result = "" ;
		try
		{
			int next ;
			do
			{
				next = in.read() ;
			}
			while( ( next != -1 || next != 10 ) && Character.isWhitespace( ( char )next ) ) ;

			while( ( next != -1 || next != 10 ) && !Character.isWhitespace( ( char )next ) )
			{
				result += ( char )next ;
				next = in.read() ;
			}

			while( next != -1 && next != 10 && next != '\n' && Character.isWhitespace( ( char )next ) )
				next = in.read() ;

			unread( ( char )next ) ;

		} // end try
		catch( Exception e )
		{
			error( "readWord" ) ;
		} // end catch

		return result ;
	}

	/** 
	 Reads an int and skips any trailing whitespace on current line.
	 Keeps trying if floating point number is invalid.
	 Precondition: next token in input stream is an int.
	 @returns The next integer in the input disk file or that is typed 
	 at the keyboard.
	 */
	public int readInt()
	{
		int result = 0 ;
		do // keep on trying until a valid double is entered
		{
			try
			{
				result = Integer.parseInt( readWord() ) ;
				break ; // result is good, jump out of loop down to return result ; 
			}
			catch( Exception e )
			{
				if( rePrompting )
					System.out.println( "Invalid integer. Try again." ) ;
				else
				{
					error( "readInt" ) ;
					break ;
				}
			}
		}
		while( true ) ;
		return result ;
	}

	/** 
	 Reads an floating point numnber and skips any trailing whitespace on 
	 current line. Keeps trying if floating point number is invalid.
	 Precondition: next token in input stream is an valid number (int or double).
	 @returns The next floating point number in the input disk file or 
	 that is typed at the keyboard.
	 */
	public double readDouble()
	{ 
		// pre : next token in input stream is a double
		// post: reads double and skips any trailing whitespace on current line
		//       Keeps trying if floating point number is invalid
		double result = 0. ;

		do // keep on trying until a valid double is entered
		{
			try
			{
				result = new Double( readWord() ) ;
				break ; // result is good, jump out of loop down to return result ;
			}
			catch( Exception e )
			{
				if( rePrompting )
				{
					System.out.println( "Invalid floating-point number. Try again." ) ;
				}
				else
				{
					error( "readDouble" ) ;
					break ;
				}
			}
		}
		while( true ) ;
		return result ;
	}

	/**
	 Find out if the input stream has more data that can be read. 
	 This is especially useful when processing file data where the 
	 size of the file is not determined (or changes a lot).
	 @returns true if input stream is ready for reading otherwise returns false
	 */
	public boolean ready()
	{
		boolean result = false ;
		try
		{
			result = in.ready() ;
		}
		catch( IOException e )
		{
			error( "ready" ) ;
		}
		return result ;
	}
}
