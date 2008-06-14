package edu.jach.qt.utils ;

/**
 * Bench.java
 *
 *
 * Created: Wed Feb 21 15:09:06 2001
 *
 * @author <a href="mailto: "Matthew Rippa</a>
 * @version
 */

public class Bench
{
	public static void main( String[] args )
	{
		long startTime = 0 ;
		long stopTime = 0 ;
		long[] elapsedTime = new long[ 20 ] ;
		int count = 0 ;
		double total = 0. ;
		double average = 0. ;

		for( int j = 0 ; j < 20 ; j++ )
		{
			startTime = System.currentTimeMillis() ;

			for( int i = 1 ; i <= 1000000 ; i++ )
				count++ ;

			stopTime = System.currentTimeMillis() ;

			elapsedTime[ j ] = stopTime - startTime ;
			System.out.println( "Elapsed time is: " + elapsedTime[ j ] + " milliseconds." ) ;

			total += elapsedTime[ j ] ;
		}

		average = total / 20 ;
		System.out.println( "Average time: " + average + " milliseconds." ) ;

	}
} // Bench
