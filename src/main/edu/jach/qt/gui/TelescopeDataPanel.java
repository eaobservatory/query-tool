package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import ocs.utils.*;


/**
 * TelescopeDataPanel.java
 *
 *
 * Created: Tue Sep 25 18:02:01 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */

public class TelescopeDataPanel extends JPanel implements ActionListener {
   
  public final static boolean	DRAMA_ENABLED = "true".equals(System.getProperty("DRAMA_ENABLED"));

  public static String	tauString = "-----";

  private static JLabel csoTauValue;
  private JLabel csoTau;
  private JLabel seeing;
  private JLabel seeingValue;
  private JLabel airmass;
  private JLabel airmassValue;
  private JButton updateButton;
  private DcHub hub;

  public TelescopeDataPanel() {
    csoTau		= new JLabel("CSO Tau: ", JLabel.LEADING);
    seeing		= new JLabel("Seeing: ", JLabel.LEADING);
    airmass		= new JLabel("Airmass: ", JLabel.LEADING);
    csoTauValue		= new JLabel(tauString, JLabel.LEADING);
    seeingValue		= new JLabel("0.25\"", JLabel.LEADING);
    airmassValue	= new JLabel("1.2", JLabel.LEADING);
    updateButton	= new JButton("Set Current");

    if (TelescopeDataPanel.DRAMA_ENABLED) {
      hub = DcHub.getHandle();
      hub.register("CSOMON");
    }

    else {
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
    System.out.println("The tau: "+ TelescopeDataPanel.csoTauValue.getText());
        
    WidgetPanel.getAtmospherePanel().setTau(TelescopeDataPanel.csoTauValue.getText());
    //WidgetPanel.getAtmospherePanel().setSeeing(TelescopeDataPanel.seeingValue.getText());
    //WidgetPanel.getAtmospherePanel().setAirmass(TelescopeDataPanel.airmassValue.getText());
  }
   
}
