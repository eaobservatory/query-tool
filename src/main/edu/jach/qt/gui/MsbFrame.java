package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * MsbFrame.java
 *
 *
 * Created: Fri Mar  2 17:20:45 2001
 *
 * @author <a href="mailto: "Matthew Rippa</a>
 * @version
 */

public class MsbFrame extends JDialog
   implements ActionListener {

   private JButton closeButton;
   private JButton testButton;
   private ButtonPanel calibratePanel;

   /**
    * Creates a new <code>MsbFrame</code> instance.
    *
    * @param parent a <code>JFrame</code> value
    */
   public MsbFrame (JFrame parent) {
      super(parent, "Define MSB", true);
      try {
	 initButtons();
  	 compInit();
      }
      catch(Exception e) {
	 e.printStackTrace();
      }
   }

   /**Button initialization*/
   private void initButtons() throws Exception{
      //calibratePanel = new ButtonPanel("CALIBRATE",
      //new String[]
      //{  "ARC",
      //"DARK",
      //"FLAT",
      //    "SKYDIP"
      // });
   }

   /**Component initialization is here*/
   private void compInit() throws Exception  {
      Container contentPane = getContentPane();
      BorderLayout borderLayout1 = new BorderLayout();
      JPanel buttonPanel = new JPanel();
      JPanel mainPanel = new JPanel();
      Color yellow = Color.yellow;
      Color black = Color.black;

      contentPane.setLayout(borderLayout1);
      Border mainBorder = BorderFactory.createEtchedBorder();
      Border buttonBorder = BorderFactory.createEtchedBorder(yellow, black);
      mainPanel.setBorder(mainBorder);
      buttonPanel.setBorder(buttonBorder);

      mainPanel.setLayout(new GridLayout(1,3));
      mainPanel.add(new JList(new String[] {"ARC", "FLAT",
					   "DARK", "FOCUS", "SKYDIP"}) );
      mainPanel.add(calibratePanel);
      mainPanel.add(new JTextField("Input", 20));
      contentPane.add( "Center", mainPanel);

      closeButton = addButton(buttonPanel, "CLOSE");
      contentPane.add(buttonPanel, BorderLayout.SOUTH);
      contentPane.add(mainPanel, BorderLayout.CENTER);
      contentPane.add( "South", buttonPanel);
      setSize(400,320);
   }

   JButton addButton (Container c, String name) {
      JButton button = new JButton(name);
      button.addActionListener(this);
      c.add(button);
      return button;
   }

   /**
    * Describe <code>actionPerformed</code> method here.
    *
    * @param evt an <code>ActionEvent</code> value
    */
   public void actionPerformed(ActionEvent evt) {
      Object source = evt.getSource();
      if(source == closeButton)
	 setVisible(false);
   }
   
}// MsbFrame
