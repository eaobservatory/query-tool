package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import ocs.utils.*;
import org.apache.log4j.Logger;
import edu.jach.qt.utils.LockFile;


/**
 * TelescopeDataPanel.java
 *
 *
 * Created: Tue Sep 25 18:02:01 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
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
  private DcHub hub;

  public TelescopeDataPanel() {
    csoTau		= new JLabel("CSO Tau: ", JLabel.LEADING);
    seeing		= new JLabel("Seeing: ", JLabel.LEADING);
    airmass		= new JLabel("Airmass: ", JLabel.LEADING);
    csoTauValue		= new JLabel(tauString, JLabel.LEADING);
    seeingValue		= new JLabel(tauString, JLabel.LEADING);
    airmassValue	= new JLabel(tauString, JLabel.LEADING);
    updateButton	= new JButton("Set Current");
    boolean locked=false;

    /*
     * Check if a lock file exists.  If it doesn't create one, if is does,
     * tell the user who owns the lock and try to start up in scenario mode.
     * If we are already in scenario mode - don't bother
     */
    if (DRAMA_ENABLED) {
	if (LockFile.exists()) {
	    if (! (LockFile.owner().equals(System.getProperty("user.name")))) {
		// The current lock belongs to someone else
		String message = "QT locked by user "+LockFile.owner();
		JOptionPane.showMessageDialog(null, "Starting is scenario mode", message, JOptionPane.WARNING_MESSAGE);
		TelescopeDataPanel.DRAMA_ENABLED = false;
		locked = true;
	    }
	}
	else {
	    LockFile.createLock();
	}
    }

    if (TelescopeDataPanel.DRAMA_ENABLED) {
      hub = DcHub.getHandle();
      hub.register("CSOMON");
      //hub.register("TELMON");
    }

    else if (!locked) {
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
  
  public static void setTau(double val) {
  
    //The arg in the following constructor is a pattern for formating
    DecimalFormat myFormatter = new DecimalFormat("0.000");
    String output = myFormatter.format(val);

    TelescopeDataPanel.csoTauValue.setText(""+output);
  }

  public static void setAirmass(double val) {
  
    //The arg in the following constructor is a pattern for formating
    DecimalFormat myFormatter = new DecimalFormat("0.000");
    String output = myFormatter.format(val);

    TelescopeDataPanel.airmassValue.setText(""+output);
  }

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

  public DcHub getHub() {
    return hub;
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
   *
   * @param param1 <description>
   */
  public void actionPerformed(ActionEvent param1) {
    logger.debug("New tau: "+ TelescopeDataPanel.csoTauValue.getText());
        
    // Ignore case where the tau value is set to the default
    if (!TelescopeDataPanel.csoTauValue.getText().equals(tauString))
	{
	    WidgetPanel.getAtmospherePanel().setTextField("tau:",
						  TelescopeDataPanel.csoTauValue.getText());
	    //WidgetPanel.getAtmospherePanel().setTau(TelescopeDataPanel.csoTauValue.getText());
	}
    //WidgetPanel.getAtmospherePanel().setSeeing(TelescopeDataPanel.seeingValue.getText());
    //WidgetPanel.getAtmospherePanel().setAirmass(TelescopeDataPanel.airmassValue.getText());
  }
   
}
