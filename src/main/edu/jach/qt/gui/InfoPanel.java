package edu.jach.qt.gui;

/* QT imports */
import edu.jach.qt.app.Querytool;
import edu.jach.qt.utils.*;

/* Standard imports */
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Boolean;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ocs.utils.DcHub;
import ocs.utils.ObeyNotRegisteredException;
import org.apache.log4j.Logger;

/**
 * InfoPanel.java
 *
 *
 * Created: Tue Apr 24 16:28:12 2001
 *
 * @author <a href="mailto: "Mathew Rippa</a>
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 * $Id$
 */
public class InfoPanel extends JPanel implements ActionListener {

  static Logger logger = Logger.getLogger(InfoPanel.class);

  /**
   * The constant <code>LOGO_IMAGE</code> specifies the String
   * location for the QT logo image.
   * 
   */
  public static final String LOGO_IMAGE = System.getProperty("qtLogo");

  /**
   * The constant <code>SAT_WEBPAGE</code> specifies the webpage
   * containing the String of the latest image to show.
   * 
   */
  public static final String SAT_WEBPAGE = System.getProperty("satellitePage");

  /**
   * The constant <code>IMG_PREFIX</code> is the static portion of the
   * image source URL.
   * 
   */
  public static final String IMG_PREFIX = System.getProperty("imagePrefix");

  /**
   * The variable <code>searchButton</code> is the button clicked to
   * start a query.
   * 
   */
  public static JButton searchButton   = new JButton();

  /**
   * The variable <code>logoPanel</code> is a reference to the easily get the LogoPanel.
   *
   */
  public static LogoPanel logoPanel    = new LogoPanel();

  private TelescopeDataPanel telescopeInfoPanel;
  private MSBQueryTableModel msb_qtm;
  private TimePanel timePanel;
  private UTPanel   utPanel;
  private SatPanel satPanel;
  private Querytool localQuerytool;
  private QtFrame qtf;
  //private JButton xmlPrintButton;
  private JButton exitButton;
  private JButton fetchMSB;


  /**
   * Creates a new <code>InfoPanel</code> instance.
   *
   * @param msbQTM a <code>MSBQueryTableModel</code> value
   * @param qt a <code>Querytool</code> value
   * @param qtFrame a <code>QtFrame</code> value
   */
  public InfoPanel (MSBQueryTableModel msbQTM, Querytool qt, QtFrame qtFrame) {
    localQuerytool = qt;
    msb_qtm = msbQTM;
    qtf = qtFrame;

    MatteBorder matte = new MatteBorder(3,3,3,3, Color.green);
    GridBagLayout gbl = new GridBagLayout();

    setBackground(Color.black);
    setBorder(matte);
    setLayout(gbl);
    setMinimumSize(new Dimension(174, 550));
    setPreferredSize(new Dimension(288, 550));

    compInit();

  }

  private void compInit() {
    GridBagConstraints gbc = new GridBagConstraints();

    //xmlPrintButton	= new JButton();
    exitButton		= new JButton();
    fetchMSB		= new JButton();
    timePanel		= new TimePanel();
    utPanel		= new UTPanel();
    satPanel		= new SatPanel();
    telescopeInfoPanel	= new TelescopeDataPanel(this);

    /*Setup the SEARCH button*/
    InfoPanel.searchButton.setText("Search");
    InfoPanel.searchButton.setName("Search");
    InfoPanel.searchButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  final SwingWorker worker = new SwingWorker() {
	      Boolean isStatusOK;

	      public Object construct() {
		  if (qtf.getSelectedTab() != 0) {
		      qtf.setSelectedTab(0);
		  }
		isStatusOK = new Boolean(localQuerytool.queryMSB());
		return isStatusOK;  //not used yet
	      }

	      //Runs on the event-dispatching thread.
	      public void finished() { 
		logoPanel.stop();
		if ( isStatusOK.booleanValue()) {
		  Thread tableFill = new Thread(msb_qtm);
		  tableFill.start();
		  
		}
	      }
	    };
	  logger.info("Query Sent");

	  localQuerytool.printXML();
      
	  logoPanel.start();
	  worker.start();  //required for SwingWorker 3
	}
      } );

    InfoPanel.searchButton.setBackground(java.awt.Color.gray);

    /*
    xmlPrintButton.setText("Fetch MSB");
    xmlPrintButton.setName("Fetch MSB");
    xmlPrintButton.setBackground(java.awt.Color.gray);
    xmlPrintButton.addActionListener(this);
    */
    fetchMSB.setText("Fetch MSB");
    fetchMSB.setName("Fetch MSB");
    fetchMSB.setBackground(java.awt.Color.gray);
    fetchMSB.addActionListener(this);

    /*Setup the EXIT button*/
    exitButton.setText("Exit");
    exitButton.setName("Exit");
    exitButton.setBackground(java.awt.Color.gray);
    exitButton.addActionListener(this);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weighty = 0.2;
    add(logoPanel, gbc, 0, 0, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(5,15,5,15);
    gbc.weighty = 0.0;
        
    /*Add all the buttons*/
    add(InfoPanel.searchButton, gbc, 0, 5, 1, 1);
    add(fetchMSB, gbc, 0, 10, 1, 1);
    add(exitButton, gbc, 0, 15, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 100;
    gbc.insets = new Insets(0,2,0,2);
    gbc.weighty = 0.6;
    add(telescopeInfoPanel, gbc, 0, 20, 1, 1);

    add(satPanel, gbc, 0, 30, 1, 1);


    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 0;
    gbc.insets.left = 0;
    gbc.insets.right = 0;
    add(timePanel, gbc, 0, 40, 1, 1);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 100;
    gbc.weighty = 0;
    gbc.insets.left = 0;
    gbc.insets.right = 0;
    add(utPanel, gbc, 0, 60, 1, 1);
  }

    /**
     * Get the parent frame.
     * @return The parent QT Frame object.
     */
    public QtFrame getFrame() {
	return qtf;
    }

    /**
     * Get the current query.
     * @return The current <code>QueryTool</code> object.
     */
    public Querytool getQuery() {
	return localQuerytool;
    }

  private void add(Component c, GridBagConstraints gbc, 
		  int x, int y, int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    add(c, gbc);      
  }

  /**
   * <code>getXMLquery</code> will get the String that contains the
   * current XML defining the query.
   *
   * @return a <code>String</code> representing the query. 
   */
  public String getXMLquery() {
    return localQuerytool.getXML();
  }

    /**
     * Get the <code>TelescopeDataPanel</code>
     * @return  the current telescope panel.
     */
    public TelescopeDataPanel getTelescopeDataPanel()
    {
	return telescopeInfoPanel;
    }

    public SatPanel getSatPanel() {
	return satPanel;
    }

  /* -- No longer use-- now a static const
    public String getImageFile() {
    return LOGO_IMAGE;
    }
  */

  /*
   * <code>getCSODcHub</code> method here.
   *
   * @return a <code>DcHub</code> value
   
   public DcHub getCSODcHub() {
   return telescopeInfoPanel.getHub();
   }
  */

  /**
   * <code>actionPerformed</code> satisfies the ActionListener
   * interface.  This is called when any ActionEvents are triggered by
   * registered ActionListeners. In this case it's either the exit
   * button or the fetchMSB button.
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
    else if (source == fetchMSB) {
      qtf.sendToStagingArea();
    }
  }
}// InfoPanel


