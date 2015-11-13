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

/* QT imports */
import edu.jach.qt.utils.SimpleMoon;

/* Standard imports */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.JToggleButton;
import javax.swing.JRadioButton;
import javax.swing.ToolTipManager;

import edu.jach.qt.utils.MoonChangeListener;

/* Miscellaneous imports */
import gemini.util.JACLogger;
import gemini.util.ObservingToolUtilities;

/**
 * This is the primary input panel for the QtFrame.
 *
 * It is responsible for configuring the widgets determined in
 * config/qtWidget.conf at run-time. There also exists two Hashtables.
 * One is responsible for assigning abbreviations for widget names,
 * the other is known to the class as the WidgetDataBag.
 *
 * Created: Tue Mar 20 16:41:13 2001
 *
 * @author <a href="mailto:mrippa@jach.hawaii.edu">Mathew Rippa</a>
 */
@SuppressWarnings("serial")
public class WidgetPanel extends JPanel implements MoonChangeListener {
    static final JACLogger logger = JACLogger.getLogger(WidgetPanel.class);
    private int numComponents = 0;
    protected Hashtable<String, String> abbrevTable;
    protected WidgetDataBag widgetBag;
    private int totalNumRadRows = 0;
    private int numRadPanels = 0;
    private static JTextFieldPanel atmospherePanel;
    private static RadioPanel moonPanel;
    private boolean ignoreMoonUpdates = false;

    /**
     * Creates a new <code>WidgetPanel</code> instance.
     *
     * @param ht a <code>Hashtable</code> value
     * @param wdb a <code>WidgetDataBag</code> value
     */
    public WidgetPanel(Hashtable<String, String> ht, WidgetDataBag wdb) {
        abbrevTable = ht;
        widgetBag = wdb;

        SimpleMoon.addChangeListener(this);
    }

    /**
     * The single method responsible for configuring Widgets at runtime.
     *
     * It parses the config/qtWidgets.conf file and sets up the determined
     * widgets as described by the current layout manager.
     *
     * @param file a <code>String</code> value
     * @exception IOException if an error occurs
     */
    public void parseConfig(String file) throws IOException {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        String widget;

        final URL url = ObservingToolUtilities.resourceURL(file);
        BufferedReader in = null;

        if (url != null) {
            InputStream is = url.openStream();
            InputStreamReader reader = new InputStreamReader(is);
            in = new BufferedReader(reader);
        } else {
            in = new BufferedReader(new FileReader(file));
        }

        while (in.ready()) {
            String line = in.readLine().trim();

            // skip over comments
            if ((line.length() == 0) || line.startsWith("#")) {
                continue;
            }

            // which widget?
            String[] words = line.split("\\s+", 2);
            widget = words[0];

            // JLabeldTextField
            if (widget.equals("JTextField")) {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 100;
                gbc.weighty = 0;
                gbc.insets.top = 10;
                gbc.insets.bottom = 5;
                gbc.insets.left = 10;
                gbc.insets.right = 5;
                addTextFields("Labeled", gbc, in);

            } else if (widget.equals("JMinMaxField")) {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.NORTH;
                gbc.weightx = 100;
                gbc.weighty = 0;
                gbc.insets.top = 5;
                gbc.insets.bottom = 5;
                gbc.insets.left = 10;
                gbc.insets.right = 5;
                addTextFields("MinMax", gbc, in);

            } else if (widget.equals("JRangeField")) {
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.NORTH;
                gbc.weightx = 100;
                gbc.weighty = 0;
                gbc.insets.top = 9;
                gbc.insets.bottom = 5;
                gbc.insets.left = 10;
                gbc.insets.right = 15;
                addTextFields("Range", gbc, in);

            } else if (widget.equals("JRadioButtonGroup")
                    || widget.equals("JTextFieldGroup")
                    || widget.equals("JCheckBoxGroup")) {
                CompInfo info = makeList(in);

                // Create the appropriate type of panel.
                WidgetPanel panel;

                if (widget.equals("JRadioButtonGroup")) {
                    panel = new RadioPanel(abbrevTable, widgetBag, info);
                } else if (widget.equals("JTextFieldGroup")) {
                    panel = new JTextFieldPanel(abbrevTable, widgetBag,
                            info);
                } else if (widget.equals("JCheckBoxGroup")) {
                    panel = new ButtonPanel(abbrevTable, widgetBag, info);
                } else {
                    throw new IOException("Unknown widget type: " + widget);
                }

                panel.setName(info.getTitle());
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // "Country" is currently a radio button group at JCMT and a
                // checkbox group at UKIRT, but we still want it to appear
                // as it would if it were a radio button group.  Therefore
                // always treat "Country" as if it's not a checkbox group.
                if (widget.equals("JCheckBoxGroup")
                        && ! info.getTitle().equalsIgnoreCase("Country")) {
                    gbc.weightx = 100;
                    gbc.weighty = 100;
                    gbc.insets.left = 10;

                    if (info.getTitle().equalsIgnoreCase("Instruments")) {
                        add(panel, gbc, 0, 20, 2, 1);
                    } else {
                        add(panel, gbc, 0, 21, 2, 1);
                    }

                } else if (info.getView() != -1) {
                    gbc.insets.top = 0;
                    gbc.insets.bottom = 0;
                    gbc.insets.left = 0;
                    gbc.insets.right = 0;

                    gbc.anchor = GridBagConstraints.NORTH;
                    gbc.weightx = 50;
                    gbc.weighty = 0;

                    if (info.getView() == BoxLayout.Y_AXIS) {
                        if (info.getTitle().equalsIgnoreCase("Clouds")) {
                            add(panel, gbc, 2, 20, 1, info.getSize() + 1);
                        } else if (info.getTitle().equalsIgnoreCase(
                                "Atmosphere")) {
                            add(panel, gbc, 1, 4, 3, info.getSize() + 1);
                            setAtmospherePanel(panel);
                        } else if (info.getTitle().equalsIgnoreCase("Moon")) {
                            add(panel, gbc, 3, 20, 1, info.getSize() + 1);
                            setMoonPanel(panel);
                        } else if (info.getTitle().equalsIgnoreCase("Country")) {
                            add(panel, gbc, 1, 0, 3, info.getSize() + 1);
                        } else {
                            add(panel, gbc, numRadPanels, 20, 1,
                                    info.getSize() + 1);
                        }

                        totalNumRadRows += info.getSize() + 2;

                    } else {
                        add(panel, gbc, 0, numComponents + totalNumRadRows, 1,
                                2);
                    }

                    numRadPanels++;

                } else {
                    logger.error("FAILED to set radio position!");
                    System.exit(1);
                }

            } else if (!widget.equals("[Section]")) {
                break;
            }

        }

        setButtons();
    }

    /**
     * Special function for setting the value of the Moon buttons.
     *
     * Assumes that dark occurs when the moon is set, grey when the moon is up
     * but less than 25% illuminated and bright otherwise.
     */
    public void setButtons() {
        if (!ignoreMoonUpdates) {
            // Currently sets the moon based on whether it is up and the
            // illuminated fraction
            SimpleMoon moon = SimpleMoon.getInstance();
            Hashtable<String, Object> ht = widgetBag.getHash();

            boolean dark = false;
            boolean grey = false;
            boolean bright = false;

            if (moon.isUp() == false) {
                dark = true;
            } else if (moon.getIllumination() < 0.25) {
                grey = true;
            } else {
                bright = true;
            }

            Object tmp = ht.get("moon");

            if (tmp != null && tmp instanceof LinkedList) {
                ListIterator iter = ((LinkedList) tmp).listIterator(0);

                while (iter.hasNext()) {
                    Object o = iter.next();

                    if (o instanceof JRadioButton) {
                        JToggleButton abstractButton = (JRadioButton) o;

                        abstractButton.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                ignoreMoonUpdates = true;
                                ToolTipManager.sharedInstance()
                                        .registerComponent(moonPanel);
                                moonPanel.setToolTipText(
                                        "Auto update disabled by user;"
                                        + " use \"Set Default\" to enable");
                                moonPanel.setBackground(Color.red);
                            }
                        });

                        String buttonName = abstractButton.getText();

                        if (buttonName.equalsIgnoreCase("Dark")) {
                            abstractButton.setSelected(dark);
                        } else if (buttonName.equalsIgnoreCase("Grey")) {
                            abstractButton.setSelected(grey);
                        } else if (buttonName.equalsIgnoreCase("Bright")) {
                            abstractButton.setSelected(bright);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set whether or not updates should be made for each query.
     *
     * This is turned off by the mouse listener associated with each button
     * on the moon panel
     */
    public void setMoonUpdatable(boolean flag) {
        ignoreMoonUpdates = !flag;

        if (flag == true && moonPanel != null) {
            moonPanel.setToolTipText(null);
            moonPanel.setBackground(getBackground());
        }
    }

    /**
     * Add a nes Text Field to the JTextFieldPanel.
     *
     * @param type The type of the textfield. Must one of
     *            <i>Labeled</i>, <i>MinMax</i> or
     *            <i>Range</i>.
     * @param gbc The GridBatConstraints class for these objects.
     */
    private void addTextFields(String type, GridBagConstraints gbc,
            BufferedReader in) throws IOException {
        String next, tmp;

        do {
            next = in.readLine().trim();

            if (next.equals("[EndSection]")) {
                break;
            }

            String toolTip = null;

            if (next.matches(".*-.*")) {
                String[] split = next.split("-");
                next = split[0].trim();
                toolTip = split[1].trim();
            }

            tmp = abbreviate(next);
            abbrevTable.put(next, tmp);

            if (type.equals("Labeled")) {
                add(new LabeledTextField(abbrevTable, widgetBag, next, toolTip),
                        gbc, 0, numComponents, 1, 1);
            } else if (type.equals("MinMax")) {
                add(new LabeledMinMaxTextField(abbrevTable, widgetBag, next,
                        toolTip), gbc, 0, numComponents, 1, 1);
            } else if (type.equals("Range")) {
                add(new LabeledRangeTextField(abbrevTable, widgetBag, next,
                        toolTip), gbc, 0, numComponents, 1, 1);
            }

        } while (true);
    }

    /**
     * Populate the linked list containing all of the text fields associated
     * with this component.
     */
    private CompInfo makeList(BufferedReader in) throws IOException {
        String next, view, tmpTitle = "";
        CompInfo info = new CompInfo();

        do {
            next = in.readLine().trim();

            if (next.equals("GroupTitle")) {
                tmpTitle = in.readLine().trim();
                info.setTitle(tmpTitle);
                addTableEntry(tmpTitle);

            } else if (next.equals("view")) {
                view = in.readLine().trim();

                if (view.trim().equals("X")) {
                    info.setView(BoxLayout.X_AXIS);
                } else if (view.trim().equals("Y")) {
                    info.setView(BoxLayout.Y_AXIS);
                }

            } else if (next.equals("[EndSection]")) {
                break;

            } else {
                info.addElem(next);
                addTableEntry(next);
            }

        } while (true);

        return info;
    }

    /**
     * Used to keep track of key:value relationships between the JLabel of a
     * widget (key) and its abbreviation (value).
     *
     * @param entry a <code>String</code> value
     */
    public void addTableEntry(String entry) {
        String tmp = "";
        tmp = abbreviate(entry);
        abbrevTable.put(entry, tmp);
    }

    /**
     * Used to convert the text in a widget JLabel to an abbreviation used
     * in the xml description.
     *
     * @param next a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String abbreviate(String next) {
        String result = "ERROR";

        if (!next.equals("")) {
            result = "";
            next.trim();

            String[] st = next.split("\\p{Space}");
            result = st[0].trim();
        }

        return result.toLowerCase();
    }

    /**
     * Gives subJPanels the ability to print the current abbreviation table.
     */
    protected void printTable() {
        logger.debug(abbrevTable.toString());
    }

    /**
     * A utility for adding widgets or subJPanels to the WdgetPanel.
     *
     * The layout manager is a GridBag and the current constraints (gbc)
     * are passed to this method.
     *
     * @param c a <code>Component</code> value
     * @param gbc a <code>GridBagConstraints</code> value
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param w an <code>int</code> value
     * @param h an <code>int</code> value
     */
    public void add(Component c, GridBagConstraints gbc, int x, int y, int w,
            int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        add(c, gbc);
        numComponents++;
    }

    /**
     * Set the atmosphere panel to the current panel.
     *
     * @param panel The <code>WidgetPanel</code> object corresponding to the
     *            atmosphere panel.
     */
    public void setAtmospherePanel(WidgetPanel panel) {
        atmospherePanel = (JTextFieldPanel) panel;

        if (!TelescopeDataPanel.getCSO().startsWith("-")) {
            atmospherePanel.setTextField("tau:", TelescopeDataPanel.getCSO());
        }
    }

    /**
     * Set the moon panel to the current panel.
     *
     * @param panel The <code>WidgetPanel</code> object corresponding to the
     *            moon panel.
     */
    public void setMoonPanel(WidgetPanel panel) {
        moonPanel = (RadioPanel) panel;
    }

    /**
     * Return the Atmosphere Panel Object.
     *
     * @return The atmosphere panel object and its associated components.
     */
    public static JTextFieldPanel getAtmospherePanel() {
        return WidgetPanel.atmospherePanel;
    }

    /**
     * Return the Moon Panel Object.
     *
     * @return The Moon panel object and its associated components.
     */
    public static RadioPanel getMoonPanel() {
        return WidgetPanel.moonPanel;
    }

    /**
     * Provided for convenience this signature is supported but not encouraged.
     *
     * All classes using this methods should move towards
     * the (String, LinkedList) signature as means of
     * updating widget state to the WidgetDataBag object.
     *
     * @param key a <code>String</code> value
     * @param value a <code>String</code> value
     */
    public void setAttribute(String key, String value) {
        widgetBag.put(abbrevTable.get(key), value);
    }

    /**
     * Provided for convenience this signature is supported but not encouraged.
     *
     * All classes using this methods should move towards
     * the (String, LinkedList) signature as means of
     * updating widget state to the WidgetDataBag object.
     *
     * @param key a <code>String</code> value
     * @param value a <code>Object</code> value
     */
    public void setAttribute(String key, Object obj) {
        String object = abbrevTable.get(key);
        widgetBag.put(object, obj);
    }

    /**
     * Triggers all observers' update method.
     *
     * The primary means of notifying observers of widget state changes.
     *
     * The update method of respective observers can be implemented in
     * different ways, however the only known observer to date is app/Querytool
     * which rewrites the XML description of this panels state.
     *
     * @param title a <code>String</code> value
     * @param list a <code>LinkedList</code> value
     */
    public void setAttribute(String title, LinkedList list) {
        widgetBag.put(abbrevTable.get(title), list);
    }

    /**
     * Return The <code>WidgetDataBag</code> associated with this class.
     *
     * @return The <code>WidgetDataBag</code> associated with this class.
     */
    public WidgetDataBag getBag() {
        return widgetBag;
    }

    public void moonChanged() {
        setButtons();
    }
}
