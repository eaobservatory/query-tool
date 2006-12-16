package edu.jach.qt.gui;


/* QT imports */
import edu.jach.qt.utils.TextReader ;
import edu.jach.qt.utils.SimpleMoon ;

/* Standard imports */
import java.awt.GridBagConstraints ;
import java.awt.GridBagLayout ;
import java.awt.Component ;
import java.awt.Color ;
import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.awt.event.MouseAdapter ;
import java.awt.event.MouseEvent ;
import java.io.IOException ;
import java.util.Hashtable ;
import java.util.LinkedList ;
import java.util.Enumeration ;
import java.util.ListIterator ;
import javax.swing.JPanel ;
import javax.swing.JCheckBox ;
import javax.swing.Box ;
import javax.swing.SwingConstants ;
import javax.swing.BoxLayout ;
import javax.swing.JToggleButton ;
import javax.swing.JRadioButton ;
import javax.swing.ToolTipManager ;

import edu.jach.qt.utils.MoonChangeListener ;

/* Miscellaneous imports */
import org.apache.log4j.Logger;

/**
 * WidgetPanel.java
 * 
 * This is the primary input panel for the QtFrame. It is responsible
 * for configuring the widgets determined in config/qtWidget.conf at
 * run-time.  There also exists two Hashtables.  One is responsible
 * for assigning abbreviations for widget names, the other is known to
 * the class as the WidgetDataBag.
 *
 * Created: Tue Mar 20 16:41:13 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class WidgetPanel extends JPanel implements ActionListener , MoonChangeListener
{

  static Logger logger = Logger.getLogger(WidgetPanel.class);

  private TextReader tr;
  private Box b;
  private LinkedList radioList;
  private int numComponents = 0;
  private JCheckBox[] cb = new JCheckBox[3];
  protected Hashtable abbrevTable;
  protected WidgetDataBag widgetBag;
  private int totalNumRadRows = 0;
  private int numRadPanels = 0;

  private  static JTextFieldPanel atmospherePanel;
  private  static RadioPanel      moonPanel;
  private boolean ignoreMoonUpdates = false;
  /**
   * Describe variable <code>instrumentPanel</code> here.
   *
   */
  public ButtonPanel instrumentPanel;

  /**
	 * Creates a new <code>WidgetPanel</code> instance.
	 * 
	 * @param ht
	 *            a <code>Hashtable</code> value
	 * @param wdb
	 *            a <code>WidgetDataBag</code> value
	 */
	public WidgetPanel( Hashtable ht , WidgetDataBag wdb )
	{
		abbrevTable = ht;
		widgetBag = wdb;

		SimpleMoon.addChangeListener( this ) ;
	}

  /**
	 * The <code>parseConfig</code> method is the single method responsible for configuring Widgets at runtime. It parses the config/qtWidgets.conf file and sets up the determined widgets as described by the current layout manager.
	 * 
	 * @param file
	 *            a <code>String</code> value
	 * @exception IOException
	 *                if an error occurs
	 */
  public void parseConfig(String file) throws IOException {

    GridBagLayout layout = new GridBagLayout();
    setLayout(layout);

    GridBagConstraints gbc = new GridBagConstraints();
    String widget, next, tmp;

    tr = new TextReader(file);
    while (tr.ready()) {

      //skip over comments
      while(tr.peek() == '#') {
	tr.readLine();
      }
	 
      //which widget?
      widget = tr.readWord();
	 
      //JLabeldTextField
      if (widget.equals("JTextField")) {
	//next = tr.readLine();
	tr.readLine();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.top = 10;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 5;
	addTextFields("Labeled", gbc);
      }

      else if (widget.equals("JMinMaxField")) {
	tr.readLine();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.top = 5;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 5;
	addTextFields("MinMax", gbc);
      }

      else if (widget.equals("JRangeField")) {
	tr.readLine();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.weightx = 100;
	gbc.weighty = 0;
	gbc.insets.top = 9;
	gbc.insets.bottom = 5;
	gbc.insets.left = 10;
	gbc.insets.right = 15;
	addTextFields("Range", gbc);
      }

      //JCheckBox
      else if (widget.equals("JCheckBox")) {
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 100;
	gbc.weighty = 0;
	int num =0;
	next = tr.readLine();
	do {
	  next = tr.readLine();
	  if (next.equals("[EndSection]"))
	    break;
	  cb[num] = new JCheckBox(next);
	  cb[num].setHorizontalAlignment(SwingConstants.CENTER);
	  //cb[num].setBackground(java.awt.Color.gray);

	  cb[num].addActionListener(this);
	  add(cb[num], gbc, 1, num, 2, 1);
	  num++;
	  tmp = abbreviate(next);
	  abbrevTable.put(next, tmp);
	  //widgetBag.put(tmp, "false");
	}while (true);
      }

      //JRadioButton Panel
      else if (widget.equals("JRadioButtonGroup") || widget.equals("JTextFieldGroup")) {
	//radioList = new LinkedList();
	CompInfo info = makeList();

	WidgetPanel panel;

	if ( info.getView() != -1) {
	  if (widget.equals("JRadioButtonGroup") ) {
	    panel = new RadioPanel(abbrevTable, widgetBag, info);
	  
	  }

	  else {
	    panel = new JTextFieldPanel(abbrevTable, widgetBag, info);
	  }
	  
	  panel.setName(info.getTitle());
	  gbc.insets.top = 0;
	  gbc.insets.bottom = 0;
	  gbc.insets.left = 0;
	  gbc.insets.right = 0;

	  gbc.fill = GridBagConstraints.HORIZONTAL;
	  gbc.anchor = GridBagConstraints.NORTH;
	  gbc.weightx = 50;
	  gbc.weighty = 0;
	  if ( info.getView() == BoxLayout.Y_AXIS) {
	    
	    if (info.getTitle().equalsIgnoreCase("Clouds") ) {
	      add(panel, gbc, 2, 20, 1, info.getSize()+1);
	    }
	    else if (info.getTitle().equalsIgnoreCase("Atmosphere") ) {
	      add(panel, gbc, 1, 4, 3, info.getSize()+1);
	      setAtmospherePanel(panel);
	    }
	    else if (info.getTitle().equalsIgnoreCase("Moon") ) {
	      add(panel, gbc, 3, 20, 1, info.getSize()+1);
	      setMoonPanel(panel);
	    }
	    else if (info.getTitle().equalsIgnoreCase("Country") ) {
	      add(panel, gbc, 1, 0, 2, info.getSize()+1);
	    }
	    else {
	      add(panel, gbc, numRadPanels, 20,1, info.getSize()+1);
	    }
	    totalNumRadRows += info.getSize()+2;
	  }

	  else {
	    add(panel, gbc, 0, numComponents + totalNumRadRows, 1, 2);
	  }

	  numRadPanels++;
	}
	
	else {
	  logger.error("FAILED to set radio position!");
	  System.exit(1);
	}
      }

      //JCheckBoxGroup
      else if (widget.equals("JCheckBoxGroup")) {
	//LinkedList checkList = new LinkedList();
	CompInfo info = makeList();
        
	    
	instrumentPanel = new ButtonPanel(abbrevTable, widgetBag, info);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	//gbc.anchor = GridBagConstraints.NORTH;
	gbc.weightx = 100;
	gbc.weighty = 100;
	gbc.insets.left = 10;
        if ( info.getTitle().equalsIgnoreCase("Instruments") ) {
            add(instrumentPanel, gbc, 0, 20, 2, 1);
        }
        else {
            add(instrumentPanel, gbc, 0, 21, 2, 1);
        }
      }

      else if(!widget.equals("[Section]")) {
	break;
      }

    }//end while

    validateTree();
    setButtons();

  }//parseConfig

    /**
	 * Special function for setting the value of the Moon buttons. 
	 * Assumes that dark occurs when the moon is set, 
	 * grey when the moon is up but less than 25% illuminated and bright otherwise.
	 */
	public void setButtons()
	{
		if( ignoreMoonUpdates )
			return;

		// Currently sets the moon based on whether it is up and the illuminated fraction
		SimpleMoon moon = SimpleMoon.getInstance() ;
		Hashtable ht = widgetBag.getHash();

		boolean dark = false;
		boolean grey = false;
		boolean bright = false;

		if( moon.isUp() == false )
			dark = true;
		else if( moon.getIllumination() < 0.25 )
			grey = true;
		else
			bright = true;

		for( Enumeration e = ht.keys() ; e.hasMoreElements() ; )
		{
			String next = ( ( String ) e.nextElement() );
			if( next.equalsIgnoreCase( "Moon" ) )
			{
				for( ListIterator iter = ( ( LinkedList ) ( ht.get( next ) ) ).listIterator( 0 ) ; iter.hasNext() ; iter.nextIndex() )
				{
					Object o = iter.next();
					if( o instanceof JRadioButton )
					{
						JToggleButton abstractButton = ( JRadioButton ) o;
						abstractButton.addMouseListener( new MouseAdapter()
						{
							public void mouseClicked( MouseEvent e )
							{
								ignoreMoonUpdates = true;
								ToolTipManager.sharedInstance().registerComponent( moonPanel );
								moonPanel.setToolTipText( "Auto update disabled by user; use \"Set Default\" to enable" );
								moonPanel.setBackground( Color.red );
							}
						} );
						String buttonName = abstractButton.getText();
						if( buttonName.equalsIgnoreCase( "Dark" ) && dark == true )
							abstractButton.setSelected( true );
						else if( buttonName.equalsIgnoreCase( "Grey" ) && grey == true )
							abstractButton.setSelected( true );
						else if( buttonName.equalsIgnoreCase( "Bright" ) && bright == true )
							abstractButton.setSelected( true );
					}
				}
			}
			break;
		}
	}

    /**
	 * Set whether ot not updates should be made for each query. This is turned off by the mouse listener associated with each button on the moon panel
	 */
	public void setMoonUpdatable( boolean flag )
	{
		ignoreMoonUpdates = !flag;
		if( flag == true && moonPanel != null )
		{
			moonPanel.setToolTipText( null );
			moonPanel.setBackground( instrumentPanel.getBackground() );
		}
	}

    /**
	 * Add a nes Text Field to the JTextFieldPanel.
	 * 
	 * @param type
	 *            The type of the textfield. Must one of <italic>Labeled</italic>, <italic>MinMax</italic> or <italic>Range</italic>.
	 * @param gbc
	 *            The GridBatConstraints class for these objets.
	 */
  private void addTextFields(String type, GridBagConstraints gbc) {
    String next, tmp;
    do {
      next = tr.readLine();
      if (next.equals("[EndSection]"))
	break;
      tmp = abbreviate(next);
      abbrevTable.put(next, tmp);
	 
      if(type.equals("Labeled")) {
	add(new LabeledTextField(abbrevTable, widgetBag, next),
	    gbc, 0, numComponents, 1, 1);
      }
      else if(type.equals("MinMax")) {
	add(new LabeledMinMaxTextField(abbrevTable, widgetBag, next),
	    gbc, 0, numComponents, 1, 1);
      }
      else if(type.equals("Range")) {
	add(new LabeledRangeTextField(abbrevTable, widgetBag, next),
	    gbc, 0, numComponents, 1, 1);
      }
    }while (true);
  }

    /**
     * Populate the linked list containing all of the text fields associated with this component.
     */
  private CompInfo makeList () {
    String next, view, tmpTitle = "";
    CompInfo info = new CompInfo();

    next = tr.readLine();
    do{
      next = tr.readLine();
      if (next.equals("GroupTitle")) {
	tmpTitle = tr.readLine();
	info.setTitle(tmpTitle);
	addTableEntry(tmpTitle);
      }
      else if (next.equals("view")) {
	view = tr.readLine();
	
	if (view.trim().equals("X")) {
	  info.setView(BoxLayout.X_AXIS);
	} else if (view.trim().equals("Y")) {
	  info.setView(BoxLayout.Y_AXIS);
	}
      }
      else if (next.equals("[EndSection]"))
	break;
      else {
	info.addElem(next);
	addTableEntry(next);
	//widgetBag.put(tmp, "false");
      }
    }while(true);
    return info;
  }

  /**
   * The <code>addTableEntry</code> method is used to keep track of
   * key:value relationships between the JLabel of a widget(Key) and its
   * abbreviation (value).
   *
   * @param entry a <code>String</code> value
   */
  public void addTableEntry(String entry) {
    String tmp = "";
    tmp = abbreviate(entry);
    abbrevTable.put(entry, tmp);
  }

  /**
	 * The <code>abbreviate</code> method is used to convert the text in a widget JLabel to an abbreviation used in the xml description.
	 * 
	 * @param next
	 *            a <code>String</code> value
	 * @return a <code>String</code> value
	 */
	public String abbreviate( String next )
	{
		String result = "ERROR";
		if( !next.equals( "" ) )
		{
			result = "";
			next.trim();

			String[] st = next.split( "\\p{Space}" ) ;
			result = st[ 0 ].trim() ;
		}
		return result.toLowerCase() ;
	}

  /**
	 * The code>printTable</code> method here gives subJPanels the ability to print the current abbreviation table.
	 * 
	 */
  protected void printTable() {
    logger.debug(abbrevTable.toString());
  }
   
  /**
   * The <code>add</code> method here is a utility for adding widgets
   * or subJPanels to the WdgetPanel.  The layout manager is a
   * GridBag and the current contraints (gbc) are passed to this method.
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
    numComponents++;
  }

    /**
     * Set the atmosphere panel to the current panel.
     * @param panel  The <code>WidgetPanel</code> object corresponding to the
     *               atmosphere panel.
     */
  public void setAtmospherePanel(WidgetPanel panel) {
    atmospherePanel = (JTextFieldPanel)panel;
    if (!TelescopeDataPanel.getCSO().startsWith("-")) {
	atmospherePanel.setTextField("tau:", TelescopeDataPanel.getCSO());
    }
  }

    /**
     * Set the moon panel to the current panel.
     * @param panel  The <code>WidgetPanel</code> object corresponding to the
     *               moon panel.
     */
  public void setMoonPanel(WidgetPanel panel) {
    moonPanel = (RadioPanel)panel;
  }

    /**
     * Return the Atmosphere Panel Object.
     * @return    The atmosphere panel object and its associated components.
     */
  public static JTextFieldPanel getAtmospherePanel() {
    return WidgetPanel.atmospherePanel;
  }

    /**
     * Return the Moon Panel Object.
     * @return    The Moon panel object and its associated components.
     */
    public static RadioPanel getMoonPanel() {
	return WidgetPanel.moonPanel;
    }
  
  /**
   * This <code>actionPerformed</code> method is mandated by
   * ActionListener and is need particularly for the 2 checkbox
   * widgets "Any Instrument" and "Photometric Whether Conditions".
   * All other objects in this JPanel are themselves sub-JPanels and
   * the actionPerformed methods are implemented in their respective
   * classes.  All subJPanels extend WidgetPanel.
   *
   * @param evt an <code>ActionEvent</code> value
   */
  public void actionPerformed( ActionEvent evt) {
    Object source = evt.getSource();
      
    if (source.equals(cb[0])) {
      widgetBag.put(abbrevTable.get(cb[0].getText()), ""+cb[0].isSelected());
    }

    if (source.equals(cb[1])) {
      if (cb[1].isSelected()) {
	instrumentPanel.setSelected(false);
	instrumentPanel.setEnabled(false);
      }
      else {
	instrumentPanel.setEnabled(true);
	instrumentPanel.setSelected(false);
      }
      //widgetBag.put(abbrevTable.get(cb[1].getLabel()), ""+cb[1].isSelected());
    }
  }

  /**
   * Provided for convienince, <code>setAttribute</code> method with
   * this signature is supported but not encouraged.  All classes
   * using this methods should move towards the (String, LinkedList)
   * signature as meand of updateing widget state to the
   * WidgetDataBag object.
   *
   * @param key a <code>String</code> value
   * @param value a <code>String</code> value
   */
  public void setAttribute(String key, String value) {
    widgetBag.put(abbrevTable.get(key), value);
  }

  /**
   * Provided for convienince, <code>setAttribute</code> method with
   * this signature is supported but not encouraged.  All classes
   * using this methods should move towards the (String, LinkedList)
   * signature as meand of updateing widget state to the
   * WidgetDataBag object.
   *
   * @param key a <code>String</code> value
   * @param value a <code>Object</code> value
   */
  public void setAttribute(String key, Object obj) {
    widgetBag.put(abbrevTable.get(key), obj);
  }


  /**
   * The primary means of notifying observers of widget state
   * changes. The <code>setAttribute</code> method triggers all
   * observers' update method.  The update method of respective
   * observers can be implemented in different ways, however the only
   * known observer to date is app/Querytool which rewrites the XML
   * description of this panels state.
   *
   * @param title a <code>String</code> value
   * @param list a <code>LinkedList</code> value
   */
  public void setAttribute(String title, LinkedList list) {
    widgetBag.put(abbrevTable.get(title), list);
  }

    /**
     * Returns The <code>WidgetDataBag</code> associated with this class.
     * @return The <code>WidgetDataBag</code> associated with this class.
     */
    public WidgetDataBag getBag() {
	return widgetBag;
    }

	public void moonChanged()
	{
		setButtons() ;
	}

}// WidgetPanel
