package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import ocs.utils.*;
import org.apache.log4j.Logger;
//import edu.jach.qt.utils.LockFile;
import edu.jach.qt.utils.*;


/**
 * A Data Panel on the QT.
 * All information is displayed on this panel, and can be used for seaching, fetching
 * and setting query parameters.
 *
 *
 * Created: Tue Sep 25 18:02:01 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @version $Id$
 */

public class TelescopeDataPanel extends JPanel implements ActionListener {

  static Logger logger = Logger.getLogger(TelescopeDataPanel.class);
   
  public static boolean	DRAMA_ENABLED = "true".equals(System.getProperty("DRAMA_ENABLED"));
  public static String	tauString = "-----";

  private static JLabel csoTauValue;
  private static JLabel airmassValue;
  private JLabel csoTau;
  private JLabel seeing;
  private JLabel seeingValue;
  private JLabel airmass;
  private JButton updateButton;
  private InfoPanel infoPanel;
  private DcHub hub;
  private static String lastCSOValue = "";
  private static boolean acceptUpdates = true;

    /**
     * Constructor.
     * This constructor does the following tasks:
     * <ul>
     * <li> Checks to see if the QT is locked and if so converts to scenario mode.
     * <li> Gets handles for the DRAMA monitired tasks.
     * </ul>
     * @param panel   the <code>InfoPanel</code> Object contructing this panel.
     */
  public TelescopeDataPanel(InfoPanel panel) {
    csoTau		= new JLabel("CSO Tau: ", JLabel.LEADING);
    seeing		= new JLabel("Seeing: ", JLabel.LEADING);
    airmass		= new JLabel("Airmass: ", JLabel.LEADING);
    csoTauValue		= new JLabel(tauString, JLabel.LEADING);
    seeingValue		= new JLabel(tauString, JLabel.LEADING);
    airmassValue	= new JLabel(tauString, JLabel.LEADING);
    updateButton	= new JButton("Set Default");
    this.infoPanel      = panel;
    boolean locked=false;

    /*
     * Check if a lock file exists.  If it doesn't create one, if is does,
     * tell the user who owns the lock and try to start up in scenario mode.
     * If we are already in scenario mode - don't bother
     */

    String lockFileName = File.separator +
	File.separator +
	System.getProperty("telescope") +
	"data" +
	File.separator +
	System.getProperty("deferredDir")+
	File.separator +
	".lock";
    File lockFile = new File (lockFileName.toLowerCase());
    if (lockFile.exists()) {
	locked = true;
    }

    if (TelescopeDataPanel.DRAMA_ENABLED) {
      hub = DcHub.getHandle();
      hub.register("CSOMON");
//       hub.register("TELMON");
    }
    else if (locked) {
      Object[] options = { "CONTINUE", "CANCEL" };
      int n = JOptionPane.
	showOptionDialog(null, 
			 " QT IS LOCKED!.\n\n"+
			 "Continue will allow you to run the QT in Senario Mode.\n"+
			 "Cancel will shutdown the QT\n" +
			 "To run in live, cancel and remove the file "+lockFileName.toLowerCase(), 
			 "Warning", 
			 JOptionPane.OK_CANCEL_OPTION, 
			 JOptionPane.WARNING_MESSAGE, null, options, options[0]);

      if( (n == JOptionPane.NO_OPTION) || (n == JOptionPane.CLOSED_OPTION)) {
	  System.exit(0);
      }
      else {
	  DRAMA_ENABLED = false;
      }
    }
    else  {
      Object[] options = { "CONTINUE", "CANCEL" };
      int n = JOptionPane.
	showOptionDialog(null, 
			 "          NOT A DRAMA ENABLED SYSTEM!.\n\n"+
			 "Continue will allow you to run the QT in Senario Mode.\n"+
			 "Cancel will shutdown the QT", 
			 "Warning", 
			 JOptionPane.OK_CANCEL_OPTION, 
			 JOptionPane.WARNING_MESSAGE, null, options, options[0]);

      if( (n == JOptionPane.NO_OPTION) || (n == JOptionPane.CLOSED_OPTION))
	System.exit(0);
    }
    
    config();
  }
  
    /**
     * Set the Tau value on the appropriate <code>JLabel</code>
     * @param val   the value to set as a decimal number.
     */
  public static void setTau(double val) {
  
    //The arg in the following constructor is a pattern for formating
    DecimalFormat myFormatter = new DecimalFormat("0.000");
    String output = myFormatter.format(val);

    if (acceptUpdates && !output.equals(tauString)) {
	if (lastCSOValue.equals("")) {
	    lastCSOValue = output;
	    if (WidgetPanel.getAtmospherePanel() != null) {
		WidgetPanel.getAtmospherePanel().setTextField("tau:",
							      output);
		
	    }
	}
	else if (WidgetPanel.getAtmospherePanel() != null &&
		 lastCSOValue.equals(WidgetPanel.getAtmospherePanel().getText("tau:"))) {
	    lastCSOValue = output;
	    WidgetPanel.getAtmospherePanel().setTextField("tau:",
							  output);
	}
	else if (!(lastCSOValue.equals(WidgetPanel.getAtmospherePanel().getText("tau:")))) {
	    acceptUpdates = false;
	}
    }
    TelescopeDataPanel.csoTauValue.setText(""+output);
  }

    /**
     * Set the Airmass value on the appropriate <code>JLabel</code>
     * @param val   the value to set as a decimal number.
     */
  public static void setAirmass(double val) {
  
    //The arg in the following constructor is a pattern for formating
    DecimalFormat myFormatter = new DecimalFormat("0.000");
    String output = myFormatter.format(val);

    TelescopeDataPanel.airmassValue.setText(""+output);
  }

    /**
     * Builds the components of the interface.
     * Adds buttons, labels and other panels.
     */
  public void config() {
      
    setBackground(Color.black);
    setBorder(BorderFactory.createTitledBorder
	      (BorderFactory.createEtchedBorder(new Color(51, 134, 206), Color.black), "Current Info",
	       TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    csoTau.setForeground(java.awt.Color.white);
    seeing.setForeground(java.awt.Color.white);
    airmass.setForeground(java.awt.Color.white);
    csoTauValue.setForeground(java.awt.Color.green);
    seeingValue.setForeground(java.awt.Color.green);
    airmassValue.setForeground(java.awt.Color.green);

    updateButton.setBackground(java.awt.Color.gray);
    updateButton.addActionListener(this);

    gbc.fill = GridBagConstraints.NONE;
    //gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets.bottom = 5;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;

    add(csoTau, gbc, 0, 0, 1, 1);
    add(csoTauValue, gbc, 1, 0, 1, 1);

    add(seeing, gbc, 0, 1, 1, 1);
    add(seeingValue, gbc, 1, 1, 1, 1);

    add(airmass, gbc, 0, 2, 1, 1);
    add(airmassValue, gbc, 1, 2, 1, 1);

    add(updateButton, gbc, 0, 3, 2, 1);
            
  }

    /**
     * Returns the current DRAMA hub.
     * @return  The current DRAMA hub created by the constructor.
     */
  public DcHub getHub() {
    return hub;
  }
    
    /**
     * Gets the tau value currently displayed on this panel.
     * @return   The current CSO tau value.
     */
    public static String getCSO() {
	return TelescopeDataPanel.csoTauValue.getText();
    }
   
  /**
   * Describe <code>add</code> method here.
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

  // implementation of java.awt.event.ActionListener interface

  /**
   * Implementation of java.awt.event.ActionListener interface.
   * This will update the following fields:
   * <ul>
   * <li> The Tau value
   * <li> The airmass value
   * <li> The Date and Time fields
   * <li> The information about the moon.
   *</ul>
   * @param param1 ActionEvent created by the "Set Current" button.
   */
    public void actionPerformed(ActionEvent param1) {
	logger.debug("New tau: "+ TelescopeDataPanel.csoTauValue.getText());
        
	// Ignore case where the tau value is set to the default
	if (!TelescopeDataPanel.csoTauValue.getText().equals(tauString))
	    {
		acceptUpdates = true;
		lastCSOValue = "";
		WidgetPanel.getAtmospherePanel().setTextField("tau:",
							      TelescopeDataPanel.csoTauValue.getText());
		//WidgetPanel.getAtmospherePanel().setTau(TelescopeDataPanel.csoTauValue.getText());
	    }
	//WidgetPanel.getAtmospherePanel().setSeeing(TelescopeDataPanel.seeingValue.getText());
	//WidgetPanel.getAtmospherePanel().setAirmass(TelescopeDataPanel.airmassValue.getText());


	SimpleMoon moon = new SimpleMoon();
	boolean dark = false;
	boolean grey = false;
	boolean bright = false;
	
	if (moon.isUp() == false ) {
	    dark = true;
	}
	else if (moon.getIllumination() < 0.25) {
	    grey = true;
	}
	else {
	    bright = true;
	}
	RadioPanel moonPanel = WidgetPanel.getMoonPanel();
	if (moonPanel != null) {
	    ListIterator iter = moonPanel.radioElems.listIterator(0);
	    while (iter.hasNext()) {
		JToggleButton abstractButton = (JRadioButton)iter.next();
		if (abstractButton.getText().equalsIgnoreCase("dark") && dark == true) {
		    abstractButton.setSelected(true);
		    abstractButton.doClick();
		}
		else if (abstractButton.getText().equalsIgnoreCase("Grey") && grey == true) {
		    abstractButton.setSelected(true);
		    abstractButton.doClick();
		}
		else if (abstractButton.getText().equalsIgnoreCase("Bright") && bright == true) {
		    abstractButton.setSelected(true);
		    abstractButton.doClick();
		}
	    }
	}

	infoPanel.getFrame().setMenuDefault();

	WidgetPanel widgetPanel = infoPanel.getFrame().getWidgets();
	Component [] components = widgetPanel.getComponents();
	for (int i=0; i<widgetPanel.getComponentCount(); i++ ) {
	    if ( components[i] instanceof LabeledTextField ) {
		((LabeledTextField)components[i]).setText("");
	    }
	    else if ( components[i] instanceof LabeledRangeTextField ) {
		LabeledRangeTextField lrtf = (LabeledRangeTextField) components[i];
		if ( components[i].getName().equalsIgnoreCase("airmass") ) {
		    if (!TelescopeDataPanel.airmassValue.getText().equals(tauString)) {
			String zCurrentAirmass = TelescopeDataPanel.airmassValue.getText();
			Double currentAirmass;
			try {
			    currentAirmass = new Double(zCurrentAirmass);
			    double upperLimit = currentAirmass.doubleValue();
			    upperLimit = upperLimit - 20.*upperLimit/100.;
			    if (upperLimit < 1.0 ) upperLimit = 1.0;

			    double lowerLimit = currentAirmass.doubleValue();
			    lowerLimit = lowerLimit + 20.*lowerLimit/100.;
			    if (lowerLimit > 3.0) lowerLimit = 3.0;

			    lrtf.setLowerText (new Double(upperLimit));
			    lrtf.setUpperText (new Double(lowerLimit));
			}
			catch ( NumberFormatException nfe ) {
			}
		    }
		    else {
			lrtf.setLowerText("");
			lrtf.setUpperText("");
		    }
		}
		else if (  components[i].getName().equalsIgnoreCase("observation") ) {
		    TimeUtils tu = new TimeUtils();
		    lrtf.startTimer();
		    infoPanel.getQuery().setExecutable(true);
		}
		else {
		    ((LabeledRangeTextField)components[i]).setLowerText("");
		    ((LabeledRangeTextField)components[i]).setUpperText("");
		}
	    }
	}
    }
}
