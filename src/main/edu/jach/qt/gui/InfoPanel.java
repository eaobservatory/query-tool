package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Boolean;
import java.net.URL;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.jach.qt.app.Querytool;
import edu.jach.qt.utils.*;
import ocs.utils.ObeyNotRegisteredException;
import ocs.utils.DcHub;
/**
 * InfoPanel.java
 *
 *
 * Created: Tue Apr 24 16:28:12 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @version
 */

public class InfoPanel extends JPanel
  implements ActionListener {

  public static final String LOGO_IMAGE = System.getProperty("qtLogo");
  public static final String SAT_WEBPAGE = System.getProperty("satellitePage");
  public static final String IMG_PREFIX = System.getProperty("imagePrefix");

  public static JButton searchButton   = new JButton();
  public static LogoPanel logoPanel    = new LogoPanel();

  private MSBQueryTableModel msb_qtm;
  private TimePanel timePanel;
  private SatPanel satPanel;
  private Querytool localQuerytool;
  private Led blinker;
  private TelescopeDataPanel telescopeInfoPanel;

  JLabel hstLabel		= new JLabel("HST");

  JButton xmlPrintButton	= new JButton();
  JButton exitButton		= new JButton();
  JButton fetchMSB		= new JButton();
  QtFrame qtf;

  /**
   * Creates a new <code>InfoPanel</code> instance.
   *
   * @param parent a <code>JFrame</code> value
   * @param mqtm a <code>MSBQueryTableModel</code> value
   */
  public InfoPanel (MSBQueryTableModel msbQTM, Querytool qt, QtFrame qtf) {
    msb_qtm = msbQTM;
    localQuerytool = qt;
    this.qtf=qtf;
    setMinimumSize(new Dimension(174, 550));
    setPreferredSize(new Dimension(288, 550));

    setBackground(Color.black);
    MatteBorder matte = new MatteBorder(3,3,3,3,Color.green);
    setBorder(matte);
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);

    compInit();

  }

  /**
   * Describe <code>compInit</code> method here.
   *
   */
  public void compInit() {
    GridBagConstraints gbc = new GridBagConstraints();
    timePanel = new TimePanel();
    satPanel = new SatPanel();

    telescopeInfoPanel = new TelescopeDataPanel();
    telescopeInfoPanel.config();

    //blinker = new Led();

    InfoPanel.searchButton.setText("Search");
    InfoPanel.searchButton.setName("Search");
    InfoPanel.searchButton.setBackground(java.awt.Color.gray);
    InfoPanel.searchButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  final SwingWorker worker = new SwingWorker() {
	      Boolean isStatusOK;

	      public Object construct() {
		isStatusOK = new Boolean(localQuerytool.queryMSB());
		return isStatusOK;  //not used yet
	      }

	      //Runs on the event-dispatching thread.
	      public void finished() { 
		logoPanel.stop();
		//blinker.blinkLed(false);
		if ( isStatusOK.booleanValue()) {
		  Thread tableFill = new Thread(msb_qtm);
		  tableFill.start();
		  
		} // end of if ()
	      }
	    };

	  //blinker.blinkLed(true);
	  //blinkThread.start();
	  logoPanel.start();
	  worker.start();  //required for SwingWorker 3
	}
      } );
      
    InfoPanel.searchButton.setBackground(java.awt.Color.gray);

    xmlPrintButton.setText("Execute");
    xmlPrintButton.setName("Execute");
    xmlPrintButton.setBackground(java.awt.Color.gray);
    xmlPrintButton.addActionListener(this);

    exitButton.setText("Exit");
    exitButton.setName("Exit");
    exitButton.setBackground(java.awt.Color.gray);
    exitButton.addActionListener(this);

    gbc.fill = GridBagConstraints.BOTH;
    //gbc.anchor = GridBagConstraints.CENTER;
    //gbc.insets.bottom = 0;
    //gbc.insets.left = 10;
    //gbc.insets.right = 5;
    //gbc.weightx = 1.0;
    gbc.weighty = 0.2;
    add(logoPanel, gbc, 0, 0, 1, 1);

    //      gbc.fill = GridBagConstraints.NONE;
    //      gbc.anchor = GridBagConstraints.CENTER;
    //      gbc.insets.bottom = 0;
    //      gbc.insets.left = 5;
    //      gbc.insets.right = 5;
    //      gbc.weightx = 100;
    //      gbc.weighty = 100;
    //      add(blinker, gbc, 0, 1, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    //gbc.weighty = 1.0;
    add(satPanel, gbc, 0, 2, 1, 1);

    //gbc.fill = GridBagConstraints.BOTH;
    //gbc.anchor = GridBagConstraints.CENTER;
    //      gbc.insets.bottom = 0;
    //      gbc.insets.left = 5;
    //      gbc.insets.right = 5;
    //gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    add(InfoPanel.searchButton, gbc, 0, 5, 1, 1);
    
    //gbc.fill = GridBagConstraints.NONE;
    //gbc.anchor = GridBagConstraints.CENTER;
    //gbc.weightx = 100;
    //gbc.weighty = 100;
    add(xmlPrintButton, gbc, 0, 10, 1, 1);

    //gbc.fill = GridBagConstraints.NONE;
    //gbc.anchor = GridBagConstraints.CENTER;
    //gbc.weightx = 100;
    //gbc.weighty = 100;
    add(exitButton, gbc, 0, 15, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    //gbc.ipady = 30; 
    gbc.weighty = 0.6;
    add(telescopeInfoPanel, gbc, 0, 20, 1, 1);

    //      gbc.fill = GridBagConstraints.BOTH;
    //      gbc.weightx = 100;
    //      gbc.weighty = 0;
    //      hstLabel.setForeground(Color.blue);
    //      hstLabel.setHorizontalAlignment(SwingConstants.CENTER);
    //      gbc.insets.bottom = 0;
    //      gbc.insets.top = 0;
    //      add(hstLabel, gbc, 0, 6, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 0;
    gbc.insets.left = 0;
    gbc.insets.right = 0;
    add(timePanel, gbc, 0, 25, 1, 1);

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

  public String getXMLquery() {
    return localQuerytool.getXML();
  }

  public String getImageFile() {
    return LOGO_IMAGE;
  }

  public Led getBlinker() {
    return blinker;
  }

  public DcHub getCSODcHub() {
    return telescopeInfoPanel.getHub();
  }

  /**
   * Describe <code>actionPerformed</code> method here.
   *
   * @param e an <code>ActionEvent</code> value
   */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    Color color = getBackground();
    if (source == exitButton) {

      if (TelescopeDataPanel.DRAMA_ENABLED) {
	try {
	  telescopeInfoPanel.getHub().closeDcHub();
	} catch ( ObeyNotRegisteredException onr) {
	  
	}
      }

      qtf.exitQT();
    }
    else if (source == xmlPrintButton) {
      localQuerytool.printXML();
    }
  }
}// InfoPanel


