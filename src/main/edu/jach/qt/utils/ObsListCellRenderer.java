package edu.jach.qt.utils;

import om.util.OracColor;
import gemini.sp.*;
import java.awt.*;
import java.util.StringTokenizer;
import javax.swing.*;



/**
 * ObsListCellRenderer.java
 *
 *
 * Created: Mon Mar  4 15:05:01 2002
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 */


public class ObsListCellRenderer extends DefaultListCellRenderer {
  final static ImageIcon obsIcon = new ImageIcon(System.getProperty("IMAG_PATH")+"observation.gif");
   
  // This is the only method defined by ListCellRenderer.
  // We just reconfigure the JLabel each time we're called.
  
  public Component getListCellRendererComponent(JList list,
						Object value,            // value to display
						int index,               // cell index
						boolean isSelected,      // is the cell selected
						boolean cellHasFocus)    // the list and the cell have the focus
  {
    String s = ((SpObs)value).getTitle();
    setText(s);
    boolean isDone=false;

    StringTokenizer st = new StringTokenizer(s, "_done_");
    if (st.countTokens() > 0) {
	isDone=true;
    }

    if (((SpObs)value).isOptional() == false) {
	setForeground(list.getForeground());
    }
    else if (isDone) {
	setForeground(Color.red);
    }
    else {
	setForeground(Color.blue);
    }

    setText(s);
    setIcon(obsIcon);

    if (isSelected) {
      setBackground(OracColor.blue);
	//setForeground(OracColor.blue);
      //setBackground(list.getSelectionBackground());
      //setForeground(list.getSelectionForeground());
    }

    else {
      setBackground(list.getBackground());
      //setForeground(list.getForeground());
    }

    setEnabled(list.isEnabled());
    setFont(list.getFont());
    return this;
  }
}// ObsListCellRenderer
