package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.ImageIcon;
import java.net.URL;
import java.awt.Toolkit;
import edu.jach.qt.app.Querytool;
import java.io.FileNotFoundException;
import java.io.File;
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

   private static final String LOGO_IMAGE
      = "/home/mrippa/netroot/src/QT/icons/omp_logo1.gif";

   private MyQueryTableModel local_mqtm;
   private MsbFrame calibrateDialog = null;
   private TimePanel timePanel ;
   private JFrame myParent;

   JLabel hstLabel = new JLabel("HST");
   JButton searchButton = new JButton();
   JButton xmlPrintButton = new JButton();
   JButton exitButton = new JButton();
   ImagePanel imagePanel;


   /**
    * Creates a new <code>InfoPanel</code> instance.
    *
    * @param parent a <code>JFrame</code> value
    * @param mqtm a <code>MyQueryTableModel</code> value
    */
   public InfoPanel (JFrame parent, MyQueryTableModel mqtm){
      myParent = parent;
      local_mqtm = mqtm;
      //setSize(new Dimension(300, 550));
      setBackground(Color.gray);
      MatteBorder matte = new MatteBorder(2,2,2,2,Color.black);
      setBorder(matte);
      GridBagLayout gbl = new GridBagLayout();
      setLayout(gbl);

      try {
	 /* Set the OMP logo*/
	 Image image1 = setLogo();
	 imagePanel = new ImagePanel(image1);
      } catch(FileNotFoundException fnfe) {
	 System.out.println("ERROR: "+fnfe.getMessage());
      }
      compInit();
   }

   public Image setLogo() throws FileNotFoundException {
      Image theImage;
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      if(new File(LOGO_IMAGE).exists() )
	 theImage = toolkit.getImage(LOGO_IMAGE);
      else throw new FileNotFoundException("File: "+LOGO_IMAGE+" was not found");
      return theImage;
   }

   /**
    * Describe <code>compInit</code> method here.
    *
    */
   public void compInit() {
      GridBagConstraints gbc = new GridBagConstraints();
      timePanel = new TimePanel();
      
      searchButton.setText("Search");
      searchButton.setName("Search");
      searchButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	       local_mqtm.setQuery();
	    }
	 } );
      xmlPrintButton.setText("Execute");
      xmlPrintButton.setName("Execute");
      xmlPrintButton.addActionListener(this);

      exitButton.setText("Exit");
      exitButton.setName("Exit");
      exitButton.addActionListener(this);


      gbc.fill = GridBagConstraints.BOTH;
      //gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets.bottom = 5;
      gbc.insets.left = 5;
      gbc.insets.right = 5;
      gbc.weightx = 100;
      gbc.weighty = 100;
      add(imagePanel, gbc, 0, 0, 1, 1);

      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.insets.bottom = 5;
      gbc.insets.left = 5;
      gbc.insets.right = 5;
      gbc.weightx = 100;
      gbc.weighty = 100;
      add(searchButton, gbc, 0, 1, 1, 1);
    
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.weightx = 100;
      gbc.weighty = 100;
      add(xmlPrintButton, gbc, 0, 2, 1, 1);

      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.weightx = 100;
      gbc.weighty = 100;
      add(exitButton, gbc, 0, 3, 1, 1);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 100;
      gbc.weighty = 0;
      hstLabel.setForeground(Color.black);
      hstLabel.setHorizontalAlignment(SwingConstants.CENTER);
      gbc.insets.bottom = 0;
      gbc.insets.top = 0;
      add(hstLabel, gbc, 0, 4, 1, 1);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 100;
      gbc.weighty = 0;
      gbc.insets.left = 0;
      gbc.insets.right = 0;
      add(timePanel, gbc, 0, 5, 1, 1);


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
	 Querytool.printXML();
      }
      //      else if (source == calibrateButton) {
      // if (calibrateDialog == null)
      //    calibrateDialog = new MsbFrame(myParent);
      // calibrateDialog.show();
      //}
      
   }



}// InfoPanel

class ImagePanel extends JPanel {
    Image image;

    public ImagePanel(Image image) {
        this.image = image;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background

        //Draw image at its natural size first.
        //g.drawImage(image, 0, 0, this); //85x62 image

        //Now draw the image scaled.
        g.drawImage(image, 0, 0, this);
    }
}
