package edu.jach.qt.gui;

import java.util.EventListener ;
import java.awt.event.KeyListener ;
import javax.swing.JFrame ;
import javax.swing.JScrollPane ;

/* Gemini imports */
import gemini.sp.SpItem ;
import gemini.sp.SpType ;
import gemini.sp.SpRootItem ;
import gemini.sp.SpFactory ;

/* OT imports */
import jsky.app.ot.OtTreeWidget ;


/**
 * Class to display an Observation as a tree in a new frame.
 */
class TreeViewer
{
	private JFrame frame;

	/**
	 * Constructor.
	 * Creates a tree view of the input.
	 * @param item  An observation (SpItem class)
	 */
	public TreeViewer( SpItem item )
	{
		// Construct a new tree
		OtTreeWidget otTree = makeTree( item );

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add( otTree );

		frame = new JFrame();
		frame.setSize( 400 , 200 );
		frame.getContentPane().add( scrollPane );
		frame.setTitle( item.getTitle() );
		frame.setVisible( true );
	}

	public void update( SpItem item )
	{
		// Construct a new tree
		OtTreeWidget otTree = makeTree( item );

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add( otTree );

		frame.getContentPane().removeAll();
		frame.getContentPane().add( scrollPane );
		frame.setTitle( item.getTitle() );
		frame.show();
		if( !frame.isVisible() )
			frame.setVisible( true );

		frame.requestFocus();
		frame.repaint();
	}

	private OtTreeWidget makeTree( SpItem item )
	{
		// Construct a new tree
		OtTreeWidget otTree = new OtTreeWidget();
		SpItem[] itemArray = { item };

		// Create a science program to insert this into.
		SpItem root = SpFactory.create( SpType.SCIENCE_PROGRAM );
		otTree.resetProg( ( SpRootItem )root );
		otTree.spItemsAdded( root , itemArray , ( SpItem )null );
		EventListener[] listeners = otTree.getTree().getListeners( KeyListener.class );
		for( int i = 0 ; i < listeners.length ; i++ )
			otTree.getTree().removeKeyListener( ( KeyListener )listeners[ i ] );

		return otTree;
	}
}
