/*
 * Copyright (C) 2001-2010 Science and Technology Facilities Council.
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.jach.qt.gui;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.Timer;

import java.util.Hashtable;
import java.util.Vector;
import java.util.ListIterator;
import java.util.LinkedList;
import java.text.DecimalFormat;

import edu.jach.qt.utils.TimeUtils;
import edu.jach.qt.utils.SimpleMoon;
import gemini.util.JACLogger;

/**
 * LabeldRangeTextField
 *
 * Created: Tue Sep 25 15:55:43 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class LabeledRangeTextField extends WidgetPanel implements
        DocumentListener, ActionListener, KeyListener {
    static final JACLogger logger = JACLogger
            .getLogger(LabeledRangeTextField.class);
    private JTextField upperBound;
    private JTextField lowerBound;
    private JLabel widgetLabel;
    private JLabel upperLabel;
    private JLabel lowerLabel;
    private static Timer timer;
    private String name;
    private final String obsFieldName = "Observation Date";

    /**
     * Constructor.
     *
     * Associates the input text with the field name, and add the
     * object to the <code>WidgetDataBag</code> and <code>Hashtable</code>.
     * Constructs the Min and Max text fields.
     *
     * @param ht The <code>Hashtable</code> of widget names and abbreviations.
     * @param wdb The <code>WidgetDataBag</code> containing the widget
     *            information.
     * @param text The label of the current field.
     */
    public LabeledRangeTextField(Hashtable<String, String> ht,
            WidgetDataBag wdb, String text) {
        super(ht, wdb);
        init(text, null);
    }

    public LabeledRangeTextField(Hashtable<String, String> ht,
            WidgetDataBag wdb, String text, String toolTip) {
        super(ht, wdb);
        init(text, toolTip);
    }

    private void init(String text, String toolTip) {
        widgetLabel = new JLabel(text + ": ", JLabel.LEADING);

        if (text.equalsIgnoreCase(obsFieldName)) {
            lowerLabel = new JLabel("Date (yyyy-mm-dd): ", JLabel.TRAILING);
            upperLabel = new JLabel("Time (hh:mm:ss): ", JLabel.TRAILING);
        } else {
            lowerLabel = new JLabel("Min: ", JLabel.TRAILING);
            upperLabel = new JLabel("Max: ", JLabel.TRAILING);
        }

        upperBound = new JTextField();
        lowerBound = new JTextField();

        if (toolTip != null && toolTip.trim().length() != 0) {
            widgetLabel.setToolTipText(toolTip);
            lowerLabel.setToolTipText(toolTip);
            upperLabel.setToolTipText(toolTip);
            upperBound.setToolTipText(toolTip);
            lowerBound.setToolTipText(toolTip);
        }

        if (text.equalsIgnoreCase(obsFieldName)) {
            setLowerText(TimeUtils.getLocalDate());
            setUpperText(TimeUtils.getLocalTime());
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
        GridLayout gl = new GridLayout(0, 5);
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
     *
     * @return The <code>LabeledRangeTextField</code> label.
     */
    public String getName() {
        return abbreviate(name);
    }

    /**
     * Get the value in the Upper Value text field.
     *
     * This will either be the Max. value or the time for a date/time field.
     *
     * @return The value contained in the field.
     */
    public String getUpperText() {
        return upperBound.getText();
    }

    /**
     * Get the value in the Lower Value text field.
     *
     * This will either be the Min. value or the date for a date/time field.
     *
     * @return The value contained in the field.
     */
    public String getLowerText() {
        return lowerBound.getText();
    }

    /**
     * Set the value in the upper text field.
     *
     * Sets a numeric value to two decimal places.
     *
     * @param val A <code>Double</code> object.
     */
    public void setUpperText(Double val) {
        DecimalFormat df = new DecimalFormat("0.00");
        String value = df.format(val);
        upperBound.setText(value);
    }

    /**
     * Set the value in the lower text field.
     *
     * Sets a numeric value to two decimal places.
     *
     * @param val A <code>Double</code> object.
     */
    public void setLowerText(Double val) {
        DecimalFormat df = new DecimalFormat("0.00");
        String value = df.format(val);
        lowerBound.setText(value);
    }

    /**
     * Set the value in the upper text field.
     *
     * Sets the text to that passed in.
     *
     * @param val The text to set.
     */
    public void setUpperText(String val) {
        upperBound.setText(val);
    }

    /**
     * Set the value in the lower text field.
     *
     * Sets the text to that passed in.
     *
     * @param val The text to set.
     */
    public void setLowerText(String val) {
        lowerBound.setText(val);
    }

    /**
     * Return the contents of the (maximum) text field as a
     * <code>Vector&lt;String&gt;</code>.
     *
     * A list of inputs may be specified by separating each entry with a comma.
     *
     * @return The contents of the text field.
     */
    public Vector<String> getUpperList() {
        return getList(getUpperText());
    }

    /**
     * Return the contents of the (minimum) text field as a
     * <code>Vector&lt;String&gt;</code>.
     *
     * A list of inputs may be specified by separating each entry with a comma.
     *
     * @return The contents of the text field.
     */
    public Vector<String> getLowerList() {
        return getList(getLowerText());
    }

    /**
     * Return the contents of the text field as a
     * <code>Vector&lt;String&gt;</code>.
     *
     * A list of inputs may be specified by separating each entry with a comma.
     *
     * @return The contents of the text field.
     */
    private Vector<String> getList(String list) {
        Vector<String> result = new Vector<String>();
        String[] split = list.split(",");
        int index = 0;

        while (index < split.length) {
            result.add(split[index++]);
        }

        return result;
    }

    /**
     * The <code>insertUpdate</code> adds the current text to the WidgetDataBag.
     *
     * All observers are notified.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void insertUpdate(DocumentEvent e) {
        setAttribute(name.substring(0, name.length() - 1), this);
    }

    /**
     * The <code>removeUpdate</code> adds the current text to the WidgetDataBag.
     *
     * All observers are notified.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void removeUpdate(DocumentEvent e) {
        setAttribute(name.substring(0, name.length() - 1), this);
    }

    /**
     * Not implemented.
     *
     * @param e a <code>DocumentEvent</code> value
     */
    public void changedUpdate(DocumentEvent e) {
    }

    /**
     * Implementation of <code>ActionListener</code> interface.
     *
     * Sets the Date/Time fields if present to the current values.
     *
     * @param e An <code>ActionEvent</code> object.
     */
    public void actionPerformed(ActionEvent e) {
        setUpperText(TimeUtils.getLocalTime());
        setLowerText(TimeUtils.getLocalDate());
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     *
     * @param e An <code>ActionEvent</code> object.
     */
    public void keyPressed(KeyEvent evt) {
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     *
     * @param e An <code>ActionEvent</code> object.
     */
    public void keyReleased(KeyEvent evt) {
        String date = lowerBound.getText();
        String time = upperBound.getText();

        // If either date or time is invalid, don't do anything
        String datePattern = "\\d{4}-\\d{2}-\\d{2}";
        String timePattern = "\\d{1,2}(:\\d{1,2})?(:\\d{1,2})?";

        if (!date.matches(datePattern)) {
            return;
        }

        if (!time.matches(timePattern)) {
            return;
        }

        // See if we need to add anything to the time string
        String[] hms = time.split(":");
        switch (hms.length) {
            case 1:
                time = time + ":00:00";
                break;

            case 2:
                time = time + ":00";
                break;

            default:
                // nothing to do
        }

        String dateTime = date + "T" + time;
        dateTime = TimeUtils.convertLocalISODatetoUTC(dateTime);

        // Recalculate moon
        // Try to update the moon Panel
        RadioPanel moonPanel = WidgetPanel.getMoonPanel();
        if (moonPanel == null || moonPanel.getBackground() == Color.red) {
            return;
        }

        double moonValue = 0;
        SimpleMoon moon = SimpleMoon.getInstance();
        moon.set(dateTime);

        if (moon.isUp()) {
            moonValue = moon.getIllumination() * 100;
        }

        moon = null;

        Hashtable<String, Object> ht = widgetBag.getHash();
        JRadioButton b = null;

        for (ListIterator<JRadioButton> iter =
                ((LinkedList<JRadioButton>) ht.get("moon")).listIterator(0);
                iter.hasNext(); iter.nextIndex()) {
            b = iter.next();

            if (moonValue == 0) {
                if (b.getText().equalsIgnoreCase("dark")) {
                    break;
                }
            } else {
                if (moonValue <= 25) {
                    if (b.getText().equalsIgnoreCase("grey")) {
                        break;
                    }
                } else {
                    if (b.getText().equalsIgnoreCase("bright")) {
                        break;
                    }
                }
            }
        }

        if (b != null) {
            b.setSelected(true);
        }
    }

    /**
     * Implementation of <code>KeyListener</code> interface.
     *
     * Stops the Date/Time field from updating if either field is edited.
     *
     * @param e An <code>ActionEvent</code> object.
     */
    public void keyTyped(KeyEvent evt) {
        stopTimer();
    }

    /**
     * Starts the timer running.
     *
     * The timer keeps the Date/Time fields updating.
     */
    public void startTimer() {
        timer.start();
        ProgramTree.setExecutable(true);
    }

    /**
     * Restarts the timer running.
     *
     * The timer keeps the Date/Time fields updating.
     */
    public void restartTimer() {
        timer.restart();
        ProgramTree.setExecutable(true);
    }

    /**
     * Stops the timer running.
     *
     * The timer keeps the Date/Time fields updating.
     */
    public void stopTimer() {
        timer.stop();
        ProgramTree.setExecutable(false);
    }

    /**
     * Check whether the timer used for updating the Date/Time field is
     * running.
     *
     * @return <code>true</code> if the timer is running; <code>false</code>
     *         otherwise.
     */
    public boolean timerRunning() {
        return timer.isRunning();
    }
}
