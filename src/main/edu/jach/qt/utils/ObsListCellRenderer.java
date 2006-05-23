package edu.jach.qt.utils;

import java.text.NumberFormat;
import gemini.sp.*;
import java.awt.*;
import java.util.StringTokenizer;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * ObsListCellRenderer.java
 *
 *
 * Created: Mon Mar  4 15:05:01 2002
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @version  $Id$
 */


public class ObsListCellRenderer extends DefaultListCellRenderer {
  final static ImageIcon obsIcon = new ImageIcon(System.getProperty("IMAG_PATH")+"observation.gif");
   
  static Logger logger = Logger.getLogger(ObsListCellRenderer.class);
  // This is the only method defined by ListCellRenderer.
  // We just reconfigure the JLabel each time we're called.
  
  public Component getListCellRendererComponent( JList list , Object value , // value to display
	int index , // cell index
	boolean isSelected , // is the cell selected
	boolean cellHasFocus ) // the list and the cell have the focus
	{
		if( value == null )
		{
			logger.error( "ObsListCellRenderer got a null value - this should not happen" );
			return this;
		}

		if( !( value instanceof SpObs ) )
		{
			logger.error( "ObsListCellRenderer got a value of type " + value.getClass().getName() + "- this should not happen" );
			return this;
		}
		String s = ( ( SpObs ) value ).getTitle();
		// See if this observation is from the program list and has been done
		// This is indicated by a * at the end of the title attribute
		boolean hasBeenObserved = false;
		String titleAttr = ( ( SpObs ) value ).getTitleAttr();
		if( titleAttr != null && !( titleAttr.equals( "" ) ) )
		{
			if( titleAttr.endsWith( "*" ) )
			{
				hasBeenObserved = true;
			}
		}
		String duration = ( String ) ( ( SpObs ) value ).getTable().get( "estimatedDuration" );
		Double d = new Double( duration );
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits( 1 );
		nf.setMinimumFractionDigits( 1 );
		try
		{
			duration = nf.format( d.doubleValue() );
		}
		catch( Exception e )
		{
		}

		s = s + "( " + duration + " seconds )";
		setText( s );
		boolean isDone = false;

		StringTokenizer st = new StringTokenizer( s , "_" );
		while( st.hasMoreTokens() )
		{
			if( st.nextToken().equals( "done" ) )
			{
				isDone = true;
				break;
			}
		}

		if( ( ( SpObs ) value ).isOptional() == false )
		{
			setForeground( list.getForeground() );
		}
		else
		{
			setForeground( Color.green );
		}

		if( value instanceof SpObs )
		{
			if( SpQueuedMap.getSpQueuedMap().containsSpItem( ( SpObs )value ) )
				setForeground( Color.orange );
		}
		
		if( ( ( SpObs ) value ).isMSB() && ( ( SpObs ) value ).isSuspended() )
		{
			setForeground( Color.red );
		}
		else if( !( ( SpObs ) value ).isMSB() )
		{
			// Find the parent MSB and see if it is suspended
			SpItem parent = ( ( SpObs ) value ).parent();
			while( parent != null && !( parent instanceof SpMSB ) )
			{
				parent = parent.parent();
			}
			if( parent != null )
			{
				if( ( ( SpMSB ) parent ).isSuspended() )
				{
					setForeground( Color.red );
				}
			}
		}
		
		// Override the defaults
		if( isDone )
		{
			setForeground( Color.blue ); // Done calibrations appear red
		}
		if( hasBeenObserved )
		{
			setForeground( Color.gray ); // Done Observations appear gray
		}

		setText( s );
		setIcon( obsIcon );

		if( isSelected )
		{
			setBackground( OracColor.blue );
		}

		else
		{
			setBackground( list.getBackground() );
		}

		setEnabled( list.isEnabled() );
		setFont( list.getFont() );
		repaint() ;
		return this;
	}

}// ObsListCellRenderer
