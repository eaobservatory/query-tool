package edu.jach.qt.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;


public class CalibrationArea extends JPanel {

   public CalibrationArea() {
      String[] data = new String[] {"OBS4:FLAT", "OBS4:ARC", "OBS4:BSG084"};
      JList list = new JList(data);
      Border border=BorderFactory.createMatteBorder(2, 2, 2, 2, Color.white);
      setBorder(new TitledBorder(border, "DEFERED CALIBRATIONS", 0, 0, 
				       new Font("Roman",Font.BOLD,12),Color.black));
      setLayout(new BorderLayout() );
      //list.setSelectedIndex(1);
      add(list);
      
   }

   
}
