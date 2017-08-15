/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Gräff <david.graeff@tu-dortmund.de>
 */
public class DeviceInformationXML implements DeviceInformation {
    private WeakReference<AbstractConnection> comReference;
    protected DeviceInformationState state;

    public DeviceInformationXML(AbstractConnection xml, DeviceInformationState state) {
        this.comReference = new WeakReference<AbstractConnection>(xml);
        this.state = state;
    }

    /**
     * We need that called only once. Will give us name, id, version and
     * zone information.
     *
     * @throws IOException
     */
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        AbstractConnection com = comReference.get();
        String response = com.sendReceive("<System><Config>GetParam</Config></System>");
        Document doc = XMLUtils.xml(response);
        if (doc == null || doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<System><Config>GetParam failed: " + response);
        }

        state.host = com.getHost();

        Node basicStatus = XMLUtils.getNode(doc.getFirstChild(), "System/Config");

        Node node;
        String value;

        node = XMLUtils.getNode(basicStatus, "Model_Name");
        value = node != null ? node.getTextContent() : "";
        state.name = value;

        node = XMLUtils.getNode(basicStatus, "System_ID");
        value = node != null ? node.getTextContent() : "";
        state.id = value;

        node = XMLUtils.getNode(basicStatus, "Version");
        value = node != null ? node.getTextContent() : "";
        state.version = value;

        state.zones.clear();

        node = XMLUtils.getNode(basicStatus, "Feature_Existence");
        if (node == null) {
            throw new ReceivedMessageParseException("Zone information not provided: " + response);
        }

        Node subnode;
        subnode = XMLUtils.getNode(node, "Main_Zone");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(YamahaReceiverBindingConstants.Zone.Main_Zone);
        }

        subnode = XMLUtils.getNode(node, "Zone_2");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(YamahaReceiverBindingConstants.Zone.Zone_2);
        }
        subnode = XMLUtils.getNode(node, "Zone_3");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(YamahaReceiverBindingConstants.Zone.Zone_3);
        }
        subnode = XMLUtils.getNode(node, "Zone_4");
        value = subnode != null ? subnode.getTextContent() : null;
        if (value != null && (value.equals("1") || value.equals("Available"))) {
            state.zones.add(YamahaReceiverBindingConstants.Zone.Zone_4);
        }
    }
}
