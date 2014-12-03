/*
 * Copyright (C) 2014 Science and Technology Facilities Council.
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

package edu.jach.qt.vo;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.table.TableModel;

import org.astrogrid.samp.Client;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.Subscriptions;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.gui.GuiHubConnector;
import org.astrogrid.samp.httpd.HttpServer;
import org.astrogrid.samp.httpd.UtilServer;

import edu.jach.qt.utils.ErrorBox;
import edu.jach.qt.utils.MsbClient;
import edu.jach.qt.utils.MsbColumns;

/**
 * A SAMP client for the QT.
 *
 * This class is to handle interaction between the QT and the JSAMP library.
 */
public class SampClient implements HttpServer.Handler {
    private static SampClient instance = null;

    private GuiHubConnector conn;
    private String basePath = null;
    private HashMap<String, String> tableCoords = new HashMap<String, String>();
    private int tableCoordsNum = 0;

    private SampClient() {
        conn = new GuiHubConnector(DefaultClientProfile.getProfile());
        conn.declareSubscriptions(conn.computeSubscriptions());

        Metadata meta = new Metadata();
        meta.setName("QT");
        meta.setDescriptionText("OMP Query Tool");
        conn.declareMetadata(meta);

    }

    /**
     * Retrieve the SAMP client instance.
     *
     * This is a singleton class, so this method can be used to construct or
     * retrieve the existing instance of it.
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
        menu.add(new broadcastTableAction(tableModel));

        final JMenu sendMenu = new JMenu("Send query result coordinates to...");
        sendMenu.setEnabled(conn.isConnected());
        menu.add(sendMenu);
        conn.addConnectionListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sendMenu.setEnabled(conn.isConnected());
            }
        });

        conn.getClientListModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                buildSendToMenu(sendMenu, tableModel);
            }

            public void intervalAdded(ListDataEvent e) {
                buildSendToMenu(sendMenu, tableModel);
            }

            public void intervalRemoved(ListDataEvent e) {
                buildSendToMenu(sendMenu, tableModel);
            }
        });

        buildSendToMenu(sendMenu, tableModel);
        return menu;
    }

    /**
     * Create the "send to" menu listing all connect clients which are
     * subscribed to VO table load events.
     */
    private void buildSendToMenu(JMenu sendMenu, final TableModel tableModel) {
        sendMenu.removeAll();
        if (!conn.isConnected()) {
            return;
        }

        String selfId = "";
        Map<String, Client> clients = (Map<String, Client>) conn.getClientMap();

        try {
            selfId = conn.getConnection().getRegInfo().getSelfId();
        } catch (IOException e) {
        }

        for (final Map.Entry<String, Client> client : clients.entrySet()) {
            Subscriptions subscriptions = client.getValue().getSubscriptions();

            if (client.getKey().equals(selfId) || (subscriptions == null)
                    || !subscriptions.isSubscribed("table.load.votable")) {
                continue;
            }

            JMenuItem menuItem =
                    new JMenuItem(client.getValue().getMetadata().getName());
            sendMenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    notifyTableCoordinates(tableModel, client.getKey());
                }
            });
        }
    }

    /**
     * Action for broadcasting the table to all SAMP clients.
     *
     * This enables/disables itself based on whether we are connected to a hub.
     */
    private class broadcastTableAction extends AbstractAction implements
            ChangeListener {
        private TableModel tableModel;

        public broadcastTableAction(TableModel tableModel) {
            super("Broadcast query result coordinates");
            this.tableModel = tableModel;
            conn.addConnectionListener(this);
            setEnabled(conn.isConnected());
        }

        public void actionPerformed(ActionEvent e) {
            notifyTableCoordinates(tableModel, null);
        }

        public void stateChanged(ChangeEvent e) {
            setEnabled(conn.isConnected());
        }
    };

    /**
     * Notify SAMP clients of VO table of the query coordinates to load.
     *
     * Can specify a particular client ID, or if this is set to null, the table
     * is broadcast to all clients.
     *
     * This places the table in the tableCoords map and issues an URL by which
     * the clients can retrieve it.
     */
    private void notifyTableCoordinates(TableModel tableModel, String clientId) {
        MsbColumns columns = MsbClient.getColumnInfo();
        int projectColumn = columns.getIndexForKey("projectid");
        int targetColumn = columns.getIndexForKey("target");
        int raColumn = columns.getIndexForKey("ra");
        int decColumn = columns.getIndexForKey("dec");

        int rows = tableModel.getRowCount();

        StringBuilder table = new StringBuilder(
                "<?xml version=\"1.0\"?>\n"
                        + "<VOTABLE version=\"1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                        + "         xmlns=\"http://www.ivoa.net/xml/VOTable/v1.3\">\n"
                        + "    <RESOURCE name=\"QT Query Results\">\n"
                        + "        <TABLE name=\"Results\">\n"
                        + "            <FIELD name=\"Name\" ID=\"col1\" ucd=\"meta.id;meta.main\"\n"
                        + "                   datatype=\"char\" arraysize=\"*\"/>\n"
                        + "            <FIELD name=\"RA\" ID=\"col2\" ucd=\"pos.eq.ra;meta.main\"\n"
                        + "                   datatype=\"float\" width=\"5\" precision=\"1\" unit=\"deg\"/>\n"
                        + "            <FIELD name=\"Dec\" ID=\"col3\" ucd=\"pos.eq.dec;meta.main\"\n"
                        + "                   datatype=\"float\" width=\"5\" precision=\"1\" unit=\"deg\"/>\n"
                        + "            <DATA>\n"
                        + "                <TABLEDATA>\n");

        for (int i = 0; i < rows; i++) {
            String project = (String) tableModel.getValueAt(i, projectColumn);
            String target = (String) tableModel.getValueAt(i, targetColumn);
            String raStr = (String) tableModel.getValueAt(i, raColumn);
            String decStr = (String) tableModel.getValueAt(i, decColumn);

            if (raStr.equals("CAL") || decStr.equals("CAL")) {
                continue;
            }

            String[] raArray = raStr.split("\\/");
            String[] decArray = decStr.split("\\/");

            if (raArray.length != decArray.length) {
                System.err.println(
                        "Mismatching number of RA and Dec coordinates");
            } else {
                for (int j = 0; j < raArray.length; j++) {
                    try {
                        String raDeg = String.format("%.1f",
                                Double.parseDouble(raArray[j]) * 15.0);
                        String decDeg = String.format("%.1f",
                                Double.parseDouble(decArray[j]));

                        String name = project + ": " + target;

                        if (name.length() > 30) {
                            name = name.substring(0, 30);
                        }

                        table.append("                    <TR>" + "<TD>");

                        for (int k = 0; k < name.length(); k++) {
                            char c = name.charAt(k);
                            if ((c >= 'A' && c <= 'Z')
                                    || (c >= 'a' && c <= 'a')
                                    || (c >= '0' && c <= '9') || (c == ' ')) {
                                table.append(c);
                            } else {
                                table.append(String.format("&#%d;", (int) c));
                            }
                        }

                        table.append("</TD>" + "<TD>" + raDeg + "</TD>"
                                + "<TD>" + decDeg + "</TD>" + "</TR>\n");
                    } catch (NumberFormatException e) {
                        System.err.println("Malformed RA or Dec coordinate");
                    }
                }
            }
        }

        table.append("                </TABLEDATA>\n" + "            </DATA>\n"
                + "        </TABLE>\n" + "    </RESOURCE>\n" + "</VOTABLE>\n");

        try {
            UtilServer server = UtilServer.getInstance();
            if (basePath == null) {
                basePath = "/" + server.getBasePath("tablecoords");
                server.getServer().addHandler(this);
            }

            String path = basePath + "/" + Integer.toString(++tableCoordsNum)
                    + ".xml";

            String url = server.getServer().getBaseUrl().toString() + path;

            tableCoords.put(path, table.toString());

            Map msg = new HashMap();
            Map params = new HashMap();

            msg.put("samp.mtype", "table.load.votable");
            msg.put("samp.params", params);
            params.put("url", url);
            params.put("name", "QT_query_" + Integer.toString(tableCoordsNum));

            if (clientId == null) {
                conn.getConnection().notifyAll(msg);
            } else {
                conn.getConnection().notify(clientId, msg);
            }

        } catch (IOException e) {
            final String message = "Error sending table of coordinates:\n"
                    + e.toString();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new ErrorBox(null, message);
                }
            });
        }
    }

    /**
     * Handle HTTP requests from SAMP clients wishing to retrieve the VO tables.
     */
    public HttpServer.Response serveRequest(HttpServer.Request request) {
        String path = request.getUrl();
        String method = request.getMethod();

        if (!path.startsWith(basePath)) {
            return null;
        }

        final String table = (String) tableCoords.get(path);

        if (table == null) {
            return HttpServer.createErrorResponse(404, "Not found");
        }

        Map header = new HashMap();
        header.put("Content-Type", "application/x-votable+xml");
        header.put("Content-Length", table.length());

        if (method.equals("GET")) {
            return new HttpServer.Response(200, "OK", header) {
                public void writeBody(OutputStream out) {
                    PrintWriter pw = new PrintWriter(out);
                    pw.write(table);
                    pw.close();
                }
            };
        } else if (method.equals("HEAD")) {
            return new HttpServer.Response(200, "OK", header) {
                public void writeBody(OutputStream out) {
                }
            };
        }

        return HttpServer.create405Response(new String[]{"GET", "HEAD"});
    }
}
