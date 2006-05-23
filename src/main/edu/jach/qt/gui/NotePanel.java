package edu.jach.qt.gui;


import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.Document;

import gemini.sp.*;


/**
 * Constructs a scrollable text panel.
 * @author $Author$
 * @version $Id$
 */
final public class NotePanel extends JPanel {

    private GridBagConstraints		gbc;
    private static JTextPane            textPanel;

    /**
     * Constructs a scrollable non-editable text panel.
     * Sets the label of "Observer Notes", and the line wrapping convention.
     */
    public NotePanel() {
	Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
	setBorder(new TitledBorder(border, "Observer Notes", 
				   0, 0, new Font("Roman",Font.BOLD,12),Color.black));
	setLayout(new BorderLayout() );
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	gbc = new GridBagConstraints();
	
	textPanel = new JTextPane();
	textPanel.setEditable(false);

	JScrollPane scrollPane = new JScrollPane(textPanel);
	scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	
	gbc.fill = GridBagConstraints.BOTH;
	//gbc.anchor = GridBagConstraints.EAST;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 5;
	gbc.weightx = 100;
	gbc.weighty = 100;
	add(scrollPane, gbc, 0, 0, 2, 1);
    }
    
  /**
   * Add a compnent to the <code>GridBagConstraints</code>
   *
   * @param c a <code>Component</code> value
   * @param gbc a <code>GridBagConstraints</code> value
   * @param x an <code>int</code> value
   * @param y an <code>int</code> value
   * @param w an <code>int</code> value
   * @param h an <code>int</code> value
   */
    public void add(Component c, GridBagConstraints gbc, 
		    int x, int y, int w, int h) {
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = w;
	gbc.gridheight = h;
	add(c, gbc);      
    }

    /**
	 * Sets the text in the panel. Uses <code>SpNote.isObserveInstruction() </code> to locate Observer Note.
	 * 
	 * @param sp
	 *            the SpItem tree which may contain an Observer Note.
	 */
	public static void setNote( SpItem sp )
	{
		if( sp == null )
		{
			return;
		}

		// StringBuffer notes = new StringBuffer();
		ArrayList notes = new ArrayList();
		ArrayList styles = new ArrayList();

		Vector noteVector = SpTreeMan.findAllItems( sp , "gemini.sp.SpNote" );
		Enumeration e = noteVector.elements();
		while( e.hasMoreElements() )
		{
			SpNote thisNote = ( SpNote ) e.nextElement();
			if( thisNote.isObserveInstruction() )
			{
				String[] instructions = thisNote.getInstructions();
				if( instructions != null )
				{
					for( int i = 0 ; i < instructions.length ; i++ )
					{
						notes.add( instructions[ i ] + "\n" );
						styles.add( "bold" );
					}
				}
				notes.add( "\n" + thisNote.getNote() + "\n" );
				styles.add( "regular" );
			}
		}
		initStyles();

		Document doc = textPanel.getDocument();
		try
		{
			doc.remove( 0 , doc.getLength() ) ;
			for( int i = 0 ; i < notes.size() ; i++ )
			{
				doc.insertString( doc.getLength() , ( String ) notes.get( i ) , textPanel.getStyle( ( String ) styles.get( i ) ) );
			}
		}
		catch( Exception ex )
		{
			System.out.println( "Could not insert observer notes" );
		}
		textPanel.setCaretPosition( 0 );
		textPanel.repaint() ;
	}

    private static void initStyles() {
	Style def = StyleContext.getDefaultStyleContext().
	    getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = textPanel.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = textPanel.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
    }


}
