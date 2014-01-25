package edu.jach.qt.vo;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.table.TableModel;

import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.gui.GuiHubConnector;

import edu.jach.qt.utils.MsbClient;
import edu.jach.qt.utils.MsbColumns;

/**
 * A SAMP client for the QT.
 *
 * This class is to handle interaction between the
 * QT and the JSAMP library.
 */
public class SampClient {
        private static SampClient instance = null;

        private GuiHubConnector conn;

        private SampClient() {
                conn = new GuiHubConnector(DefaultClientProfile.getProfile());

                Metadata meta = new Metadata();
                meta.setName("QT");
                meta.setDescriptionText("OMP Query Tool");
                conn.declareMetadata(meta);
        }

        /**
         * Retrieve the SAMP client instance.
         *
         * This is a singleton class, so this method can be used
         * to construct or retrieve the existing instance of it.
         */
        public static SampClient getInstance() {
                if (instance == null) {
                        instance = new SampClient();
                }

                return instance;
        }

        /**
         * Create a menu for controlling the SAMP client.
         */
        public JMenu buildMenu(final Component parent, final TableModel tableModel) {
                JMenu menu = new JMenu("Interop");

                menu.add(conn.createRegisterOrHubAction(parent, null));
                menu.add(conn.createShowMonitorAction());

                JMenuItem item = new JMenuItem("Broadcast query result coordinates");
                item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            broadcastTableCoordinates(tableModel);
                        }
                });
                menu.add(item);

                return menu;
        }

        private void broadcastTableCoordinates(TableModel tableModel) {
                MsbColumns columns = MsbClient.getColumnInfo();
                int projectColumn = columns.getIndexForKey("projectid");
                int targetColumn = columns.getIndexForKey("target");
                int raColumn = columns.getIndexForKey("ra");
                int decColumn = columns.getIndexForKey("dec");

                int rows = tableModel.getRowCount();

                StringBuilder table = new StringBuilder("<?xml version=\"1.0\"?>\n" +
                        "<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\">\n" +
                        "    <RESOURCE name=\"QT Query Results\">\n" +
                        "        <TABLE name=\"Results\">\n" +
                        "            <FIELD name=\"Name\" ID=\"col1\" ucd=\"meta.id;meta.main\"\n" +
                        "                   datatype=\"char\" arraysize=\"*\"/>\n" +
                        "            <FIELD name=\"RA\" ID=\"col2\" ucd=\"pos.eq.ra;meta.main\"\n" +
                        "                   datatype=\"float\" width=\"5\" precision=\"1\" unit=\"deg\"/>\n" +
                        "            <FIELD name=\"Dec\" ID=\"col3\" ucd=\"pos.eq.dec;meta.main\"\n" +
                        "                   datatype=\"float\" width=\"5\" precision=\"1\" unit=\"deg\"/>\n" +
                        "            <DATA>\n" +
                        "                <TABLEDATA>\n");

                for (int i = 0; i < rows; i ++) {
                        String project = (String) tableModel.getValueAt(i, projectColumn);
                        String target = (String) tableModel.getValueAt(i, targetColumn);
                        String raStr = (String) tableModel.getValueAt(i, raColumn);
                        String decStr = (String) tableModel.getValueAt(i, decColumn);

                        String[] raArray = raStr.split("\\/");
                        String[] decArray = decStr.split("\\/");

                        if (raArray.length != decArray.length) {
                                System.err.println("Mismatching number of RA and Dec coordinates");
                        }
                        else {
                                for (int j = 0; j < raArray.length; j ++) {
                                        try {
                                                String raDeg = String.format("%.1f", Double.parseDouble(raArray[j]) * 15.0);
                                                String decDeg = String.format("%.1f", Double.parseDouble(decArray[j]));

                                                String name = project + ": " + target;

                                                if (name.length() > 30) {
                                                        name = name.substring(0, 30);
                                                }

                                                table.append("                    <TR>" +
                                                        "<TD>");

                                                for (int k = 0; k < name.length(); k ++ ) {
                                                        char c = name.charAt(k);
                                                        if ((c >= 'A' && c <= 'Z') ||
                                                            (c >= 'a' && c <= 'a') ||
                                                            (c >= '0' && c <= '9') ||
                                                            (c == ' ')) {
                                                                table.append(c);
                                                        }
                                                        else {
                                                                table.append(String.format("&#%d;", (int) c));
                                                        }
                                                }

                                                table.append("</TD>" +
                                                        "<TD>" + raDeg + "</TD>" +
                                                        "<TD>" + decDeg + "</TD>" +
                                                        "</TR>\n");
                                        }
                                        catch (NumberFormatException e) {
                                                System.err.println("Malformed RA or Dec coordinate");
                                        }
                                }
                        }
                }

                table.append(
                        "                </TABLEDATA>\n" +
                        "            </DATA>\n" +
                        "        </TABLE>\n" +
                        "    </RESOURCE>\n" +
                        "</VOTABLE>\n");
        }
}
