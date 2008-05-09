package edu.jach.qt.gui;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.BorderLayout;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collections;

/**
 * This class is responsible for allowing users to view the current log file.
 * It is displayed in a new window, and currently no attempt is made to hilight
 * the text.  The user is able to select the number of lines to display and
 * filter on severity level.  The available levels are
 * All, DEBUG, INFO, WARNING, ERROR
 * Messages including and more severe are shown when selecting the filter (i.e.
 * if the user selects INFO, only INFO,WARNING and ERROR messages are displayed)
 *
 * @author   $Author$
 * @version  $Revision$
 */

public class LogViewer
{
	JFrame baseFrame = new JFrame( "QueryTool Log" );
	JTextArea dispArea = new JTextArea( 20 , 50 );
	String[] lineChoices = { "20" , "50" , "100" , "150" };
	String[] levelChoice = { "All" , "DEBUG" , "INFO" , "WARNING" , "ERROR" };
	ArrayList data = new ArrayList();
	int nLines;
	int selectedLevel;

	/**
	 * Constructor
	 */
	LogViewer()
	{
		makeWindow();
	}

	/*
	 * Make the base window and add stuff to it.
	 */
	private void makeWindow()
	{
		baseFrame.setLayout( new BorderLayout() );

		JToolBar tb = makeToolBar();
		baseFrame.add( tb , BorderLayout.NORTH );

		dispArea.setLineWrap( true );
		dispArea.setWrapStyleWord( true );
		dispArea.setEditable( false );
		JScrollPane srollPane = new JScrollPane( dispArea );
		srollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		srollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		baseFrame.add( srollPane , BorderLayout.CENTER );
	}

	/*
	 * Make the toolbar that allows uers to select the number of lines and filter
	 */
	private JToolBar makeToolBar()
	{
		JToolBar tb = new JToolBar();

		// Make  the tool bar components...
		JLabel nLineLabel = new JLabel( "Lines" );
		final JComboBox nLineChooser = new JComboBox();
		for( int i = 0 ; i < lineChoices.length ; i++ )
			nLineChooser.addItem( lineChoices[ i ] );

		nLineChooser.setSelectedIndex( 0 );
		nLines = ( Integer.valueOf( lineChoices[ 0 ] ) ).intValue();
		nLineChooser.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				nLines = ( Integer.valueOf( ( String )nLineChooser.getSelectedItem() ) ).intValue();
				showLog();
			}
		} );

		JLabel filterLabel = new JLabel( "Filter:" );
		final JComboBox filterChooser = new JComboBox();
		for( int i = 0 ; i < levelChoice.length ; i++ )
			filterChooser.addItem( levelChoice[ i ] );

		filterChooser.setSelectedIndex( 0 );
		selectedLevel = 0;
		filterChooser.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				selectedLevel = filterChooser.getSelectedIndex();
				showLog();
			}
		} );

		tb.add( nLineLabel );
		tb.add( nLineChooser );
		tb.addSeparator();
		tb.add( filterLabel );
		tb.add( filterChooser );
		tb.addSeparator();

		tb.setFloatable( false );

		return tb;
	}

	/**
	 * Show the current log message using a default of 20 lines
	 * and a filter of "All".  If the file does not exist or there
	 * is a problem reading the file, then this text pane will
	 * contain a message indicating the problem.
	 * @param  fileName  The filename of the log file.
	 */
	public void showLog( String fileName )
	{
		// Make sure the file exists and we can read it
		File logFile = new File( fileName );
		if( !logFile.exists() || !logFile.canRead() )
		{
			dispArea.append( "Unable to read log file " + fileName );
		}
		else
		{
			readData( logFile );
			showLog();
		}

		baseFrame.pack();
		baseFrame.show();
	}

	/*
	 * Write the log info to the display area
	 */
	private void showLog()
	{
		// Reinitiaise each time we do the display
		dispArea.setText( "" );

		// Convert ArrayList to Array
		String[] dataArray = ( String[] )data.toArray( new String[ 0 ] );

		int count = 0;
		ArrayList displayData = new ArrayList();
		// Start with the oldest data
		for( int i = 0 ; i < dataArray.length ; i++ )
		{
			// See what level the current string starts with
			StringTokenizer st = new StringTokenizer( dataArray[ i ] );
			String firstToken;
			try
			{
				firstToken = st.nextToken();
			}
			catch( Exception e )
			{
				firstToken = "All";
			}
			// See if this token is in the list of choices
			int currentLevel = 0;
			for( int j = 1 ; j < levelChoice.length - 1 ; j++ )
			{
				if( firstToken.equals( levelChoice[ j ] ) )
				{
					currentLevel = j;
					break;
				}
			}
			if( currentLevel >= selectedLevel )
			{
				displayData.add( dataArray[ i ] + "\n" );
				count++ ;
			}
			// If we reach the maximum lines to display, break out
			if( count == nLines )
				break;
		}
		Collections.reverse( displayData );
		for( int i = 0 ; i < displayData.size() ; i++ )
			dispArea.append( ( String )displayData.get( i ) );

		return;
	}

	/*
	 * Read the data from the log file
	 */
	private void readData( File logFile )
	{
		try
		{
			BufferedReader br = new BufferedReader( new FileReader( logFile ) );
			String line;
			while( ( line = br.readLine() ) != null )
				data.add( line );

			br.close();
		}
		catch( Exception e )
		{
			data.add( "Error reading log file" );
			data.add( e.toString() );
			return;
		}
		data.trimToSize();

		// Cut this down to the maximum size we deal with
		int maxSize = ( Integer.valueOf( lineChoices[ lineChoices.length - 1 ] ) ).intValue();
		if( data.size() > maxSize )
		{
			data.subList( 0 , data.size() - maxSize ).clear();
			data.trimToSize();
		}

		// Reverse the ArrayList so that when we display the info, we get things written in the correct order ( oldest first )
		Collections.reverse( data );
	}
}
