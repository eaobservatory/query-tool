package edu.jach.qt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.Timer;

import java.util.*;
import java.lang.*;
import java.text.*;

import edu.jach.qt.utils.*;
import org.apache.log4j.Logger;

/**
 * LabeldRangeTextField.java
 *
 *
 * Created: Tue Sep 25 15:55:43 2001
 *
 * @author <a href="mailto: mrippa@jach.hawaii.edu"Mathew Rippa</a>
 * @version $Id$
 */

public class LabeledRangeTextField extends WidgetPanel 
  implements DocumentListener,ActionListener, KeyListener {

    static Logger logger = Logger.getLogger(LabeledRangeTextField.class);

  private JTextField upperBound;
  private JTextField lowerBound;

  private JLabel     widgetLabel;
  private JLabel     upperLabel;
  private JLabel     lowerLabel;

  private static Timer      timer;

  private String name;
  private final String obsFieldName = "Observation Date";

    /**
     * Contructor.
     * Associates the input text with the field name, and add the object to
     * the <code>WidgetDataBag</code> and <code>Hashtable</code>.  Constructs
     * the Min and Max text fields.
     * @param ht  The <code>Hashtable</code> of widget names and abbreviations.
     * @param wdb The <code>WidgetDataBag</code> containing the widget information.
     * @param text The label of the current field.
     */
  public LabeledRangeTextField(Hashtable ht, WidgetDataBag wdb, String text) {
    super(ht, wdb);

    widgetLabel = new JLabel(text + ": ", JLabel.LEADING);
    if (text.equalsIgnoreCase(obsFieldName)) {
	lowerLabel = new JLabel("Date (yyyy-mm-dd): ",JLabel.TRAILING);
	upperLabel = new JLabel("Time (hh:mm:ss): ",JLabel.TRAILING);
    }
    else {
	lowerLabel = new JLabel("Min: ",JLabel.TRAILING);
	upperLabel = new JLabel("Max: ",JLabel.TRAILING);
    }


    upperBound = new JTextField();
    lowerBound = new JTextField();
    if ( text.equalsIgnoreCase(obsFieldName)) {
	TimeUtils time = new TimeUtils();
	setLowerText (time.getLocalDate());
	setUpperText (time.getLocalTime());
// 	upperBound.addActionListener(this);
// 	lowerBound.addActionListener(this);
	upperBound.addKeyListener(this);
	lowerBound.addKeyListener(this);
	timer = new Timer(0, this);
        timer.setDelay(1000);
	timer.addActionListener(this);
	startTimer();
    }
    setup();
  }

  private void setup() {
    name = widgetLabel.getText().trim();
    GridLayout gl = new GridLayout(0,5);
    gl.setHgap(0);
    setForeground(Color.white);
    widgetLabel.setForeground(Color.black);

    setLayout(gl);
    add(widgetLabel);

    add(lowerLabel);
    add(lowerBound);
    lowerBound.getDocument().addDocumentListener(this);
    
    add(upperLabel);
    add(upperBound);
    upperBound.getDocument().addDocumentListener(this);
  }

    /**
     * Get the name associated with the current object.
     * @return The <code>LabeledRangeTextField</code> label.
     */
  public String getName() {
    return abbreviate(name);
  }

    /**
     * Get the value in the Upper Value text field.
     * this will either be the Max. value or the time for a date/time field.
     * @return The value contained in the field.
     */
  public String getUpperText() {
    return upperBound.getText();
  }

    /**
     * Get the value in the Lower Value text field.
     * this will either be the Min. value or the date for a date/time field.
     * @return The value contained in the field.
     */
  public String getLowerText() {
    return lowerBound.getText();
  }

    /** 
     * Set the value in the upper text field.
     * Sets a numeric value to two decimal places.
     * @param val  A <code>Double</code> object.
     */
  public void setUpperText(Double val) {
      DecimalFormat df = new DecimalFormat("0.00");
      String value = df.format(val.doubleValue());
      upperBound.setText(value);
  }

    /** 
     * Set the value in the lower text field.
     * Sets a numeric value to two decimal places.
     * @param val  A <code>Double</code> object.
     */
  public void setLowerText(Double val) {
      DecimalFormat df = new DecimalFormat("0.00");
      String value = df.format(val.doubleValue());
      lowerBound.setText(value);
  }

    /** 
     * Set the value in the upper text field.
     * Sets the text to that passed in..
     * @param val  The text to set.
     */
  public void setUpperText(String val) {
      upperBound.setText(val);
  }

    /** 
     * Set the value in the lower text field.
     * Sets the text to that passed in..
     * @param val  The text to set.
     */
  public void setLowerText(String val) {
      lowerBound.setText(val);
  }

    /** Return the contents of the (maximum) text field as a <code>Vector</code>.
     * A list of inputs may be specified by separating each entry with a comma.
     * @return The contents of the text field.
     */
  public Vector getUpperList() {
    String tmpStr = getUpperText();
    Vector result = new Vector();
    StringTokenizer st = new StringTokenizer(tmpStr, ",");

    while (st.hasMoreTokens()) {
      String temp1 = st.nextToken();
      result.add(temp1);
    }
    return result;
  }
 
    /** Return the contents of the (minimum) text field as a <code>Vector</code>.
     * A list of inputs may be specified by separating each entry with a comma.
     * @return The contents of the text field.
     */
  public Vector getLowerList() {
    String tmpStr = getLowerText();
    Vector result = new Vector();
    StringTokenizer st = new StringTokenizer(tmpStr, ",");

    while (st.hasMoreTokens()) {
      String temp1 = st.nextToken();
      result.add(temp1);
    }
    return result;
  }
 
  /**
   * The <code>insertUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void insertUpdate(DocumentEvent e) {

    setAttribute(name.substring(0,name.length()-1), this);
  }

  /**
   * The <code>removeUpdate</code> adds the current text to the
   * WidgetDataBag.  All observers are notified.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void removeUpdate(DocumentEvent e) {
    setAttribute(name.substring(0,name.length()-1), this);
  }

  /**
   * The <code>changedUpdate</code> method is not implemented.
   *
   * @param e a <code>DocumentEvent</code> value
   */
  public void changedUpdate(DocumentEvent e) {
      
  }

    /**
     * Implementation of <code>ActionListener</code> interface.
     * Sets the Date/Time fields if present to the current values.
     * @param e  An <code>ActionEvent</code> object.
     */
    public void actionPerformed( ActionEvent e) {
	TimeUtils tu = new TimeUtils();
	setUpperText(tu.getLocalTime());
	setLowerText(tu.getLocalDate());
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     * @param e  An <code>ActionEvent</code> object.
     */
    public void keyPressed(KeyEvent evt) {
    }

    /**
	 * Implementation of <code>KeyListener</code> interface.
	 * 
	 * @param e
	 *            An <code>ActionEvent</code> object.
	 */
	public void keyReleased( KeyEvent evt )
	{
		String date = lowerBound.getText();
		String time = upperBound.getText();
		// If either date or time is invalid, son't do anything
		String datePattern = "\\d{4}-\\d{2}-\\d{2}";
		String timePattern = "\\d{1,2}(:\\d{1,2})?(:\\d{1,2})?";
		if( !date.matches( datePattern ) )
			return;
		if( !time.matches( timePattern ) )
			return;

		// See if we need to add anything to the time string
		String[] hms = time.split( ":" );
		switch( hms.length )
		{
			case 1 :
				time = time + ":00:00";
				break;
			case 2 :
				time = time + ":00";
				break;
			default :
		// nothing to do
		}

		String dateTime = date + "T" + time;
		TimeUtils tu = new TimeUtils();
		dateTime = tu.convertLocalISODatetoUTC( dateTime );

		// Recalculate moon
		// Try to update the moon Panel
		RadioPanel moonPanel = WidgetPanel.getMoonPanel();
		if( moonPanel == null || moonPanel.getBackground() == Color.darkGray )
			return;
		double moonValue = 0;
		SimpleMoon moon = new SimpleMoon( dateTime );
		if( moon.isUp() )
			moonValue = moon.getIllumination() * 100;

		Hashtable ht = widgetBag.getHash();
		JRadioButton b = null ;
		for( ListIterator iter = ( ( LinkedList )ht.get( "moon" ) ).listIterator( 0 ) ; iter.hasNext() ; iter.nextIndex() )
		{
			b = ( JRadioButton )iter.next() ;
			
			if( moonValue == 0 )
			{
				if( b.getText().equalsIgnoreCase( "dark" ) )
					break;					
			}
			else
			{
				if( moonValue <= 25 )
				{
					if( b.getText().equalsIgnoreCase( "grey" ) )
						break ;					
				}
				else
				{
					if( b.getText().equalsIgnoreCase( "bright" ) )
						break ;						
				}
			}
		}
		if( b != null )
			b.setSelected( true ) ;
	}

    /**
	 * Implementation of <code>KeyListener</code> interface. Stops the Date/Time field from updating if either field is edited.
	 * 
	 * @param e
	 *            An <code>ActionEvent</code> object.
	 */
    public void keyTyped( KeyEvent evt) {
	stopTimer();
    }

    /**
	 * Starts the timer running. The timer keeps the Date/Time fields updating.
	 */
    public void startTimer() {
	timer.start();
	ProgramTree.setExecutable(true);
    }

    /**
	 * ReStarts the timer running. The timer keeps the Date/Time fields updating.
	 */
    public void restartTimer() {
	timer.restart();
	
	ProgramTree.setExecutable(true);
    }

    /**
     * Stops the timer running.
     * The timer keeps the Date/Time fields updating.
     */
    public void stopTimer() {
	timer.stop();
	ProgramTree.setExecutable(false);
    }


    /**
     * Check whether the timer used for updating the Date/Time field is running..
     * @return <code>true</code> if the timer is running; <code>flase</code> otherwise.
     */
    public boolean timerRunning() {
	return timer.isRunning();
    }

  
}
