package edu.jach.qt.gui;

import edu.jach.qt.app.Querytool;
import edu.jach.qt.utils.*;
import java.awt.*;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Boolean;
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.border.*;
import javax.swing.event.*;
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

  //reconsider icons dir 'An absolute path!'
  private static final String LOGO_IMAGE = System.getProperty("qtLogo");

  private MSBQueryTableModel msb_qtm;
  private TimePanel timePanel ;
  private Querytool localQuerytool;

  JLabel hstLabel = new JLabel("HST");
  JButton searchButton = new JButton();
  JButton xmlPrintButton = new JButton();
  JButton exitButton = new JButton();
  JButton fetchMSB   = new JButton();
  QtFrame qtf;
  JLabel logoImage;

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
    //setSize(new Dimension(300, 550));
    setMinimumSize(new Dimension(175, 550));
    setPreferredSize(new Dimension(288, 550));

    logoImage = new JLabel();
    setBackground(Color.black);
    MatteBorder matte = new MatteBorder(4,4,4,4,Color.green);
    setBorder(matte);
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);

    try {
      setImage();
    } catch(Exception fnfe) {
      System.out.println("ERROR: "+fnfe.getMessage());
    }
    compInit();

  }

  public void setImage() throws Exception {
    URL url = new URL("file://"+LOGO_IMAGE);
    if(url != null) {
      logoImage.setIcon(new ImageIcon(url));
    }
    else {
      logoImage.setIcon(new ImageIcon(InfoPanel.class.getResource("file://"+LOGO_IMAGE)));
    }
  }

  /**
   * Describe <code>compInit</code> method here.
   *
   */
  public void compInit() {
    GridBagConstraints gbc = new GridBagConstraints();
    timePanel = new TimePanel();

    TelescopeDataPanel telescopeInfoPanel = new TelescopeDataPanel();
    telescopeInfoPanel.config();

    final Led blinker = new Led();

    searchButton.setText("Search");
    searchButton.setName("Search");
    searchButton.setBackground(java.awt.Color.gray);
    searchButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  final SwingWorker worker = new SwingWorker() {
	      Boolean isStatusOK;

	      public Object construct() {
		isStatusOK = new Boolean(localQuerytool.queryMSB());
		return isStatusOK;  //not used yet
	      }

	      //Runs on the event-dispatching thread.
	      public void finished() { 
		blinker.blinkLed(false);
		if ( isStatusOK.booleanValue()) {
		  Thread tableFill = new Thread(msb_qtm);
		  tableFill.start();
		  
		} // end of if ()
	      }
	    };

	  blinker.blinkLed(true);
	  //blinkThread.start();
	  worker.start();  //required for SwingWorker 3
	}
      } );
      
    searchButton.setBackground(java.awt.Color.gray);

    xmlPrintButton.setText("Execute");
    xmlPrintButton.setName("Execute");
    xmlPrintButton.setBackground(java.awt.Color.gray);
    xmlPrintButton.addActionListener(this);

    exitButton.setText("Exit");
    exitButton.setName("Exit");
    exitButton.setBackground(java.awt.Color.gray);
    exitButton.addActionListener(this);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets.bottom = 5;
    gbc.insets.left = 10;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(logoImage, gbc, 0, 0, 1, 1);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets.bottom = 5;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(blinker, gbc, 0, 1, 1, 1);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets.bottom = 5;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(searchButton, gbc, 0, 2, 1, 1);
    
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(xmlPrintButton, gbc, 0, 3, 1, 1);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(exitButton, gbc, 0, 4, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.weighty = 100;
    add(telescopeInfoPanel, gbc, 0, 5, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 0;
    hstLabel.setForeground(Color.black);
    hstLabel.setHorizontalAlignment(SwingConstants.CENTER);
    gbc.insets.bottom = 0;
    gbc.insets.top = 0;
    add(hstLabel, gbc, 0, 6, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 0;
    gbc.insets.left = 0;
    gbc.insets.right = 0;
    add(timePanel, gbc, 0, 7, 1, 1);
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

  /**
   * Describe <code>actionPerformed</code> method here.
   *
   * @param e an <code>ActionEvent</code> value
   */
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    Color color = getBackground();
    if (source == exitButton) {
      System.exit(0);
    }
    if (source == searchButton) {
      System.exit(0);
    }
    else if (source == xmlPrintButton) {
      localQuerytool.printXML();
    }
  }



}// InfoPanel
