package edu.jach.qt.utils;

//import om.util.OracColor;
import java.text.NumberFormat;
import java.text.ParsePosition;
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
 * @version  $Id$
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
    // See if this observation is from the program list and has been done
    // This is indicated by a * at the end of the title attribute
    boolean hasBeenObserved = false;
    String titleAttr = ((SpObs)value).getTitleAttr();
    if (titleAttr != null && !(titleAttr.equals(""))) {
	if (titleAttr.endsWith("*")) {
	    hasBeenObserved = true;
	}
    }
    String duration = (String)((SpObs)value).getTable().get("estimatedDuration");
    Double d = new Double(duration);
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(1);
    nf.setMinimumFractionDigits(1);
    try {
	duration = nf.format(d.doubleValue());
    }
    catch (Exception e) {
    }
    
    s = s + "( "+duration+" seconds )";
    setText(s);
    boolean isDone=false;

    StringTokenizer st = new StringTokenizer(s, "_");
    while (st.hasMoreTokens()) {
	if (st.nextToken().equals("done")) {
	    isDone=true;
	    break;
	}
    }

    if (((SpObs)value).isOptional() == false) {
	setForeground(list.getForeground());
    }
    else {
	setForeground(Color.blue);
    }

    // Override the defaults
    if (isDone) {
	setForeground(Color.red);   // Done calibrations appear red
    }
    if (hasBeenObserved) {
	setForeground(Color.gray);  // Done Observations appear gray
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
