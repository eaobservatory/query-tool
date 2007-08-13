package edu.jach.qt.utils;

import gemini.sp.SpItem ;
import javax.swing.ImageIcon ;
import javax.swing.JTree ;
import javax.swing.tree.DefaultTreeCellRenderer ;
import javax.swing.tree.DefaultMutableTreeNode ;
import java.awt.Component ;
import java.awt.Font ;
import java.awt.Color ;

/** final public class treeCellRenderer renders
 a JTree object in a selection frame

 @version 1.0 1st June 1999
 @author M.Tan@roe.ac.uk
 */
final public class MyTreeCellRenderer extends DefaultTreeCellRenderer
{
	/** public treeCellRenderer() is
	 the constructor. The class has only one constructor so far.
	 one thing is done during the construction. This is
	 about getting a number of icon gif files

	 @param  none
	 @return none
	 @throws none
	 */
	public MyTreeCellRenderer()
	{
		setOpaque( true );

		icon = new ImageIcon[ 6 ];
		icon[ 0 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "note-tiny.gif" );
		icon[ 1 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "observation.gif" );
		icon[ 2 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "iterComp.gif" );
		icon[ 3 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "iterObs.gif" );
		icon[ 4 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "component.gif" );
		icon[ 5 ] = new ImageIcon( System.getProperty( "IMAG_PATH" ) + "archiv_small.gif" );
	}

	/** 
	 public Component getTreeCellRendererComponent( JTree tree,
	 Object value, boolean bSelected, boolean bExpanded,
	 boolean bLeaf, int iRow, boolean bHasFocus )
	 is a public method to render the JTree object. This is mainly about
	 to attach a icon to a related item in the JTree

	 @param many
	 @return  Component
	 @throws none

	 */
	public Component getTreeCellRendererComponent( JTree tree , Object value , boolean bSelected , boolean bExpanded , boolean bLeaf , int iRow , boolean bHasFocus )
	{
		// Find out which node we are rendering and get its text
		DefaultMutableTreeNode node = ( DefaultMutableTreeNode )value;

		SpItem item = ( SpItem )node.getUserObject();

		String text = item.getTitle();
		String type = item.typeStr();

		if( bSelected )
		{
			setFont( new Font( "Courier" , Font.BOLD , 14 ) );
			setForeground( Color.black );
		}
		else
		{
			setFont( new Font( "Courier" , Font.TRUETYPE_FONT , 14 ) );
			setForeground( Color.darkGray );
		}

		if( type.equals( "no" ) )
			setIcon( icon[ 0 ] );
		else if( type.equals( "ob" ) )
			setIcon( icon[ 1 ] );
		else if( type.equals( "if" ) )
			setIcon( icon[ 2 ] );
		else if( type.equals( "ic" ) )
			setIcon( icon[ 3 ] );
		else if( type.equals( "oc" ) )
			setIcon( icon[ 4 ] );
		else if( type.equals( "pr" ) )
		{
			setIcon( icon[ 5 ] );
			setFont( new Font( "Roman" , Font.BOLD , 18 ) );
		}

		setText( text );

		return this;
	}

	private ImageIcon[] icon;
}
