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
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputState;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The zone protocol class is used to control one zone of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link ZoneControlState} instead.
 *
 * @author David Gräff - Initial contribution
 */
public class ZoneAvailableInputsXML implements ZoneAvailableInputs {
    private AvailableInputStateListener observer;
    private final WeakReference<AbstractConnection> comReference;
    private final YamahaReceiverBindingConstants.Zone zone;

    public ZoneAvailableInputsXML(AbstractConnection xml, YamahaReceiverBindingConstants.Zone zone,
            AvailableInputStateListener observer) {
        this.comReference = new WeakReference<AbstractConnection>(xml);
        this.zone = zone;
        this.observer = observer;
    }

    /**
     * Return the zone
     */
    public YamahaReceiverBindingConstants.Zone getZone() {
        return zone;
    }

    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection com = comReference.get();
        String response = com
                .sendReceive(XMLUtils.wrZone(zone, "<Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input>"));
        Document doc = XMLUtils.xml(response);
        if (doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("<Input><Input_Sel_Item>GetParam failed: " + response);
        }
        Node inputSelItem = XMLUtils.getNode(doc.getFirstChild(), zone + "/Input/Input_Sel_Item");
        NodeList items = inputSelItem.getChildNodes();

        AvailableInputState state = new AvailableInputState();

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String name = item.getElementsByTagName("Param").item(0).getTextContent();
            boolean writable = item.getElementsByTagName("RW").item(0).getTextContent().contains("W");
            if (writable) {
                state.availableInputs.put(XMLUtils.convertNameToID(name), name);
            }
        }

        observer.availableInputsChanged(state);
    }
}
