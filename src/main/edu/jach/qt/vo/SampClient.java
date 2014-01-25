package edu.jach.qt.vo;

import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.gui.GuiHubConnector;

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
        public JMenu buildMenu(Component parent) {
                JMenu menu = new JMenu("Interop");

                menu.add(conn.createRegisterOrHubAction(parent, null));
                menu.add(conn.createShowMonitorAction());

                return menu;
        }
}
