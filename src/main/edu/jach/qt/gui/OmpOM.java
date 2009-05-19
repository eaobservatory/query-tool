package edu.jach.qt.gui ;

/* Gemini imports */
import gemini.sp.SpItem ;
import gemini.sp.SpObs;
import gemini.sp.SpTreeMan ;
import gemini.sp.SpMSB ;
import gemini.sp.iter.SpIterChop ;
import gemini.sp.iter.SpIterRepeat ;
import gemini.sp.iter.SpIterOffset ;
import gemini.sp.obsComp.SpSiteQualityObsComp ;
import gemini.sp.obsComp.SpSchedConstObsComp ;

/* JSKY imports */
import jsky.app.ot.OtFileIO ;

/* ORAC imports */
import orac.ukirt.inst.SpInstUFTI ;
import orac.ukirt.inst.SpInstCGS4 ;
import orac.ukirt.inst.SpInstIRCAM3 ;
import orac.ukirt.inst.SpInstUIST ;
import orac.ukirt.inst.SpInstWFCAM ;
import orac.ukirt.iter.SpIterBiasObs ;
import orac.ukirt.iter.SpIterCGS4 ;
import orac.ukirt.iter.SpIterCGS4CalUnit ;
import orac.ukirt.iter.SpIterCGS4CalObs ;
import orac.ukirt.iter.SpIterCalUnit ;
import orac.ukirt.iter.SpIterDarkObs ;
import orac.ukirt.iter.SpIterFP ;
import orac.ukirt.iter.SpIterIRCAM3 ;
import orac.ukirt.iter.SpIterIRPOL ;
import orac.ukirt.iter.SpIterNod ;
import orac.ukirt.iter.SpIterUFTI ;
import orac.jcmt.inst.SpInstHeterodyne ;
import orac.jcmt.iter.SpIterFocusObs ;
import orac.jcmt.iter.SpIterFrequency ;
import orac.jcmt.iter.SpIterJiggleObs ;
import orac.jcmt.iter.SpIterNoiseObs ;
import orac.jcmt.iter.SpIterPOL ;
import orac.jcmt.iter.SpIterPointingObs ;
import orac.jcmt.iter.SpIterRasterObs ;
import orac.jcmt.iter.SpIterSkydipObs ;
import orac.jcmt.iter.SpIterStareObs ;

/* QT imports */
import edu.jach.qt.utils.DragTreeCellRenderer ;
import edu.jach.qt.utils.MyTreeCellRenderer ;
import edu.jach.qt.utils.QtTools ;

/* Standard imports */
import java.awt.Dimension ;
import java.io.File ;
import java.util.Hashtable ;
import java.util.NoSuchElementException ;
import java.util.Vector ;
import java.util.Enumeration ;
import javax.swing.JPanel ;
import javax.swing.JSplitPane ;
import javax.swing.JFrame ;

/* Miscellaneous imports */
import org.apache.log4j.Logger ;

/**
 * This is the top most class of the OMP-OM.  This 
 * starts off all subsequent OMP-OM specific classes.
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class OmpOM extends JPanel
{
	static Logger logger = Logger.getLogger( OmpOM.class ) ;
	private ProgramTree ptree ;
	private File file ;
	private SpItem spItem ;
	private Hashtable<Integer,ProgramTree> ptreeHashtable ;
	private DeferredProgramList deferredList ;
	public NotePanel notes ;

	/**
	 * Creates a new <code>OmpOM</code> instance.
	 *
	 */
	public OmpOM()
	{
		ptreeHashtable = new Hashtable<Integer,ProgramTree>() ;

		/* 
		 * Need to construct UKIRT-specific items so that their SpTypes are
		 * statically initialised.  Otherwise the sp classes won't know about 
		 * their types.  AB 19-Apr-2000
		 */
		this.initSpItems( System.getProperty( "telescope" ) ) ;
		logger.info( "SpItems initialized" ) ;
		ptree = new ProgramTree() ;
	}

	private void initSpItems( String telescope )
	{
		if( telescope.equalsIgnoreCase( "ukirt" ) )
		{
			/* Init UKIRT Instruments */
			new SpInstUFTI() ;
			new SpInstCGS4() ;
			new SpInstIRCAM3() ;
			new SpInstUIST() ;
			new SpInstWFCAM() ;
			new orac.ukirt.inst.SpDRRecipe() ;

			/* OMP Specific */
			new SpIterChop() ;

			/* Init UKIRT SpTypes */
			new SpIterBiasObs() ;
			new SpIterBiasObs() ;
			new SpIterCGS4() ;
			new SpIterCGS4CalUnit() ;
			new SpIterCGS4CalObs() ;
			new SpIterCalUnit() ;
			new SpIterDarkObs() ;
			new SpIterFP() ;
			new SpIterIRCAM3() ;
			new SpIterIRPOL() ;
			new SpIterNod() ;
			new SpIterUFTI() ;

			/* Init GEMINI Miscellaneous */
			new SpIterRepeat() ;
			new SpIterOffset() ;
			new orac.ukirt.iter.SpIterObserve() ;
			new orac.ukirt.iter.SpIterSky() ;
			new SpSchedConstObsComp() ;
			new SpSiteQualityObsComp() ;
		}
		else if( telescope.equalsIgnoreCase( "jcmt" ) )
		{
			/* Init JCMT Instruments */
			new SpInstHeterodyne() ;

			/* Init JCMT SpTypes */
			new SpIterChop() ;
			new orac.jcmt.inst.SpDRRecipe() ;
			new SpIterFocusObs() ;
			new SpIterFrequency() ;
			new SpIterJiggleObs() ;
			new SpIterNoiseObs() ;
			new SpIterPOL() ;
			new SpIterPointingObs() ;
			new SpIterRasterObs() ;
			new SpIterSkydipObs() ;
			new SpIterStareObs() ;
			/* Init JCMT Miscellaneous */
			new orac.jcmt.obsComp.SpSiteQualityObsComp() ;
		}
	}

	/**
	 * Set the SpItem.
	 * Set the SpItem to that passed in.
	 * @param item   The value of the SpItem to set.
	 */
	public void setSpItem( SpItem item )
	{
		spItem = item ;
	}

	/** 
	 * Set whether the current SpItems can be executed.
	 * @param flag   <code>true</code> if items can be executed.
	 */
	public void setExecutable( boolean flag )
	{
		ptree.setExecutable( flag ) ;
	}

	/**
	 * Get the name of the program.
	 * Gets the name of the program from Title field in the first observation.
	 * @return The program name, <italic>'No Observations'</italic> if no program, or <italic>'Title Not Found'</italic> on error.
	 */
	public String getProgramName()
	{
		String returnString = "Title Not Found" ;
		SpItem currentItem = ProgramTree.getCurrentItem() ;
		if( currentItem != null )
		{
			Vector<SpItem> progVector = SpTreeMan.findAllItems( currentItem , SpMSB.class.getName() ) ;
			if( progVector == null || progVector.size() == 0 )
				progVector = SpTreeMan.findAllItems( currentItem , SpObs.class.getName() ) ;
			try
			{
				if( progVector != null && progVector.size() > 0 )
				{
					SpMSB spMsb = ( SpMSB )progVector.firstElement() ;
					returnString = spMsb.getTitle() ;
				}
			}
			catch( NoSuchElementException nse )
			{
				logger.warn( returnString ) ;
				nse.printStackTrace() ;
			}
		}
		else
		{
			returnString = "No Observations" ;
		}
		return returnString ;
	}

	/**
	 * This adds a
	 * <code>ProgramTree</code>, referrenced by the msbID, to the list
	 * of trees.
	 *
	 * @param msbID an <code>int</code> value
	 */
	public void addNewTree( Integer msbID )
	{
		if( msbID == null )
			ptree.addList( null ) ;
		else
			ptree.addList( spItem ) ;

		ptree.setMinimumSize( new Dimension( 400 , 550 ) ) ;
	}

	/**
	 * 
	 * The method used for debugging.  It loads in a hard-wired MSB file
	 * to use as the ProgramTree object.
	 */
	public void addNewTree()
	{
		file = new File( System.getProperty( "arrayTests" , "/home/mrippa/install/omp/QT/config/array_tests.xml" ) ) ;
		spItem = OtFileIO.fetchSp( file.getParent() , file.getName() ) ;

		ptree.addList( spItem ) ;
		ptree.setMinimumSize( new Dimension( 400 , 550 ) ) ;

		ptreeHashtable.put( new Integer( 41 ) , ptree ) ;
	}

	/**
	 * Reloads the current program.
	 */
	public void resetTree()
	{
		spItem = OtFileIO.fetchSp( file.getParent() , file.getName() ) ;
		ptree.addList( spItem ) ;
	}

	/**
	 * Construct the Staging Panel. Builds the deferred list and Observer Notes panel and displaays as a tabbed pane.
	 * 
	 * @return The <code>JSplitPanel</code> staging area.
	 */
	public JSplitPane getTreePanel()
	{
		deferredList = new DeferredProgramList() ;
		notes = new NotePanel() ;
		JSplitPane dsp = new JSplitPane( JSplitPane.VERTICAL_SPLIT , deferredList , notes ) ;
		dsp.setDividerLocation( 150 ) ;
		JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , ptree , dsp ) ;
		if( ProgramTree.getCurrentItem() != null )
			NotePanel.setNote( ProgramTree.getCurrentItem() ) ;
		else if( DeferredProgramList.getCurrentItem() != null )
			NotePanel.setNote( DeferredProgramList.getCurrentItem() ) ;
		else
			NotePanel.setNote( spItem ) ;
		return splitPane ;
	}

	/**
	 * Constructs a drag tree panel.
	 * 
	 * @return The <code>JSplitPanel</code> staging area.
	 * @deprected Not Replaced.
	 */
	public JSplitPane getDragTreePanel()
	{
		DragDropObject ddo = new DragDropObject( spItem ) ;
		MsbNode root = new MsbNode( ddo ) ;
		getItems( spItem , root ) ;

		DnDJTree ddt = new DnDJTree( root ) ;
		ddt.setCellRenderer( new DragTreeCellRenderer() ) ;
		JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , ptree , ddt ) ;

		return splitPane ;
	}

	/**
	 * See if we can shutdown the QT.  Checks whether the program tree still has
	 * an MSB which requires actioning.
	 */
	public boolean checkProgramTree()
	{
		boolean safeToExit = true ;
		if( ptree != null )
			safeToExit = ptree.shutDownRequest() ;

		return safeToExit ;
	}

	/**
	 * Tests the display.
	 */
	public void test()
	{
		JFrame f = new JFrame() ;
		f.setSize( 400 , 300 ) ;
		DragDropObject ddo = new DragDropObject( spItem ) ;
		MsbNode root = new MsbNode( ddo ) ;
		getItems( spItem , root ) ;
		DnDJTree ddt = new DnDJTree( root ) ;
		ddt.setCellRenderer( new MyTreeCellRenderer() ) ;
		f.add( ddt ) ;
		f.show() ;
	}

	/** public void getItems (SpItem spItem,DefaultMutableTreeNode node)
	 is a public method to add ALL the items of a sp object into the
	 JTree *recursively*.
	 
	 @param SpItem spItem,DefaultMutableTreeNode node
	 @return  none
	 @throws none
	 
	 */
	private void getItems( SpItem spItem , MsbNode node )
	{
		Enumeration<SpItem> children = spItem.children() ;
		while( children.hasMoreElements() )
		{
			SpItem childNode = children.nextElement() ;
			DragDropObject ddo = new DragDropObject( childNode ) ;
			MsbNode temp = new MsbNode( ddo ) ;
			node.add( temp ) ;
			getItems( childNode , temp ) ;
		}
	}

	public void updateDeferredList()
	{
		deferredList.reload() ;
	}

	public void enableList( boolean flag )
	{
		ptree.enableList( flag ) ;
	}

	public static void main( String[] args )
	{
		QtTools.loadConfig( System.getProperty( "qtConfig" ) ) ;
		QtTools.loadConfig( System.getProperty( "omConfig" ) ) ;

		JFrame f = new JFrame() ;
		OmpOM om = new OmpOM() ;
		om.addNewTree() ;
		f.add( om.getTreePanel() ) ;
		f.setSize( 400 , 300 ) ;
		f.setVisible( true ) ;
	}
}// OmpOM
