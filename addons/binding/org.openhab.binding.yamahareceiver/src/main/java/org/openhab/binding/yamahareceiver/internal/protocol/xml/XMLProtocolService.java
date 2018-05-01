/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Provides services for XML protocol
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class XMLProtocolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLProtocolService.class);

    private final WeakReference<AbstractConnection> comReference;

    public XMLProtocolService(AbstractConnection com) {
        this.comReference = new WeakReference<>(com);
    }

    /**
     * Sends a command to the specified zone.
     * @param con
     * @param zone
     * @param cmd
     * @return The response XML node (specific to the command sent).
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public static Node getZoneResponse(AbstractConnection con, Zone zone, String cmd) throws IOException, ReceivedMessageParseException {
        return getResponse(con, XMLUtils.wrZone(zone, cmd), zone.toString());
    }

    /**
     * Sends a command to the specified zone.
     * @param con
     * @param zone
     * @param cmd
     * @param path XML tree path to extract from the response
     * @return The response XML node (specific to the command sent).
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public static Node getZoneResponse(AbstractConnection con, Zone zone, String cmd, String path) throws IOException, ReceivedMessageParseException {
        return getResponse(con, XMLUtils.wrZone(zone, cmd), zone + "/" + path);
    }

    /**
     * Send the command and retrieve the node at the specified element path.
     * @param cmd
     * @param path
     * @return
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public static Node getResponse(AbstractConnection con, String cmd, String path) throws IOException, ReceivedMessageParseException {
        String response = con.sendReceive(cmd);
        Document doc = XMLUtils.xml(response);
        if (doc.getFirstChild() == null) {
            throw new ReceivedMessageParseException("The command '" + cmd + "' failed: " + response);
        }
        Node content = XMLUtils.getNode(doc.getFirstChild(), path);
        return content;
    }

    /**
     * Sends a request to retrieve the input values available for the zone.
     * @param con
     * @param zone
     * @return
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public static Collection<InputDto> getInputs(AbstractConnection con, Zone zone)
            throws IOException, ReceivedMessageParseException {

        Node inputSelItem = getZoneResponse(con, zone, "<Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input>", "Input/Input_Sel_Item");
        NodeList items = inputSelItem.getChildNodes();

        List<InputDto> inputs = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String param = item.getElementsByTagName("Param").item(0).getTextContent();
            boolean writable = item.getElementsByTagName("RW").item(0).getTextContent().contains("W");
            inputs.add(new InputDto(param, writable));
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Zone {} - inputs: {}", zone, inputs.stream().map(InputDto::toString).collect(joining(", ")));
        }

        return inputs;
    }

    /**
     * Represents an input source
     */
    public static class InputDto {

        private final String param;
        private final boolean writable;

        public InputDto(String param, boolean writable) {
            this.param = param;
            this.writable = writable;
        }

        public String getParam() {
            return param;
        }

        public boolean isWritable() {
            return writable;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", param, writable ? "RW" : "R");
        }
    }
}
