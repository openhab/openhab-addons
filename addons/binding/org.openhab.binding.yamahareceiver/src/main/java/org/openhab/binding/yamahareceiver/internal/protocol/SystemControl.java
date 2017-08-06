/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.Zone;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControl.State} instead.
 *
 * @author David Gräff <david.graeff@tu-dortmund.de>
 */
public class SystemControl {
    /**
     * Receiver state
     *
     */
    public static class State {
        public String host = null;
        // Some AVR information
        public String name = "N/A";
        public String id = "";
        public String version = "0.0";
        public List<Zone> zones = new ArrayList<>();
        public boolean power = false;

        // If we lost the connection, invalidate the state.
        public void invalidate() {
            power = false;
            zones.clear();
            version = "0.0";
            name = "N/A";
            id = "";
        }
    }

    /**
     * We need that called only once. Will give us name, id, version and
     * zone information.
     *
     * @throws IOException
     */
    public void fetchDeviceInformation(HttpXMLSendReceive com, State state)
            throws IOException, ParserConfigurationException, SAXException {
        String response = com.post("<System><Config>GetParam</Config></System>");
        Document doc = com.xml(response);
        if (doc == null || doc.getFirstChild() == null) {
            throw new SAXException("<System><Config>GetParam failed: " + response);
        }

        state.host = com.getHost();

        Node basicStatus = HttpXMLSendReceive.getNode(doc.getFirstChild(), "System/Config");

        Node node;
        String value;

        node = HttpXMLSendReceive.getNode(basicStatus, "Model_Name");
        value = node != null ? node.getTextContent() : "";
        state.name = value;

        node = HttpXMLSendReceive.getNode(basicStatus, "System_ID");
        value = node != null ? node.getTextContent() : "";
        state.id = value;

        node = HttpXMLSendReceive.getNode(basicStatus, "Version");
        value = node != null ? node.getTextContent() : "";
        state.version = value;

        state.zones.clear();

        node = HttpXMLSendReceive.getNode(basicStatus, "Feature_Existence");
        if (node == null) {
            throw new SAXException("Zone information not provided: " + response);
        }

        Node subnode;
        subnode = HttpXMLSendReceive.getNode(node, "Main_Zone");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(Zone.Main_Zone);
        }

        subnode = HttpXMLSendReceive.getNode(node, "Zone_2");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(Zone.Zone_2);
        }
        subnode = HttpXMLSendReceive.getNode(node, "Zone_3");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(Zone.Zone_3);
        }
        subnode = HttpXMLSendReceive.getNode(node, "Zone_4");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(Zone.Zone_4);
        }
    }

    public void fetchPowerInformation(HttpXMLSendReceive xml, State state)
            throws IOException, ParserConfigurationException, SAXException {
        String response = xml.post("<System><Power_Control>GetParam</Power_Control></System>");
        Document doc = xml.xml(response);
        if (doc == null || doc.getFirstChild() == null) {
            throw new SAXException("<System><Power_Control>GetParam failed: " + response);
        }

        Node basicStatus = HttpXMLSendReceive.getNode(doc.getFirstChild(), "System/Power_Control");

        Node node;
        String value;

        node = HttpXMLSendReceive.getNode(basicStatus, "Power");
        value = node != null ? node.getTextContent() : "";
        state.power = (value.equals("On"));
    }

    /**
     * Switches the AVR on/off (off equals network standby here).
     *
     * @param xml The communication object
     * @param power The new power state
     * @param state The current state object. Will be updated with the new power state after sending the command.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void setPower(HttpXMLSendReceive xml, boolean power, State state)
            throws IOException, ParserConfigurationException, SAXException {
        String str = power ? "On" : "Standby";
        xml.postPut("<System><Power_Control><Power>" + str + "</Power></Power_Control></System>");
        fetchPowerInformation(xml, state);
    }
}
