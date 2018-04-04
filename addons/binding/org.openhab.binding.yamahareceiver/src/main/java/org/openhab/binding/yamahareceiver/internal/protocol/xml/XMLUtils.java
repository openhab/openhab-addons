/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import com.google.common.collect.Sets;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

/**
 * Utility methods for XML handling
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - DAB support, Spotify support, refactoring, input name conversion fix
 *
 */
public class XMLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);

    // We need a lot of xml parsing. Create a document builder beforehand.
    static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    static Node getNode(Node parent, String[] nodePath, int offset) {
        if (parent == null) {
            return null;
        }
        if (offset < nodePath.length - 1) {
            return getNode(((Element) parent).getElementsByTagName(nodePath[offset]).item(0), nodePath, offset + 1);
        } else {
            return ((Element) parent).getElementsByTagName(nodePath[offset]).item(0);
        }
    }

    static Node getNode(Node root, String nodePath) {
        String[] nodePathArr = nodePath.split("/");
        return getNode(root, nodePathArr, 0);
    }

    /**
     * Retrieves the child node according to the xpath expression.
     *
     * @param root
     * @param nodePath
     * @return
     * @throws ReceivedMessageParseException when the child node does not exist throws {@link ReceivedMessageParseException}.
     */
    static Node getNodeOrFail(Node root, String nodePath) throws ReceivedMessageParseException {
        Node node = getNode(root, nodePath);
        if (node == null) {
            throw new ReceivedMessageParseException(nodePath + " child in parent node missing!");
        }
        return node;
    }


    /**
     * Finds the node starting with the root and following the path. If the node is found it's inner text is returned,
     * otherwise the default provided value.
     * @param root
     * @param nodePath
     * @param defaultValue
     * @return
     */
    public static String getNodeContentOrDefault(Node root, String nodePath, String defaultValue) {
        Node node = getNode(root, nodePath);
        if (node != null) {
            return node.getTextContent();
        }
        return defaultValue;
    }

    /**
     * Finds the node starting with the root and following the path. If the node is found it's inner text is returned,
     * otherwise the default provided value.
     * @param root
     * @param nodePath
     * @param defaultValue
     * @return
     */
    public static Integer getNodeContentOrDefault(Node root, String nodePath, Integer defaultValue) {
        Node node = getNode(root, nodePath);
        if (node != null) {
            try {
                return Integer.valueOf(node.getTextContent());
            } catch (NumberFormatException e) {
                LOG.trace("The value '{}' of node with path {} could not been parsed to an integer. Applying default of {}",
                        node.getTextContent(), nodePath, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Parse the given xml message into a xml document node.
     *
     * @param message XML formatted message excluding <?xml> or <YAMAHA_AV> tags.
     * @return Return the response as xml node or throws an exception if response is not xml.
     * @throws IOException
     */
    public static Document xml(String message) throws IOException, ReceivedMessageParseException {
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + message;
        try {
            return XMLUtils.dbf.newDocumentBuilder().parse(new InputSource(new StringReader(response)));
        } catch (SAXException | ParserConfigurationException e) {
            throw new ReceivedMessageParseException(e);
        }
    }

    /**
     * The xml protocol expects HDMI_1, NET_RADIO as xml nodes, while the actual input IDs are
     * HDMI 1, Net Radio. We offer this conversion method therefore.
     *
     * Certain known inputs (e.g. Spotify, Bluetooth) will NOT be transformed (see {@link #INPUTS_NO_CONVERSION}).
     *
     * @param name The inputID like "Net Radio".
     * @return An xml node / xml protocol compatible name like NET_RADIO.
     */
    public static String convertNameToID(String name) {
        // Inputs such as 'Spotify' or 'Bluetooth' should NOT be transformed to upper case
        // as the AVR does not understand them (SPOTIFY != Spotify).
        if (INPUTS_NO_CONVERSION.contains(name)) {
            // return input name without transformation
            return name;
        }

        // Replace whitespace with an underscore. The ID is what is used for xml tags and the AVR doesn't like
        // whitespace in xml tags.
        name = name.replace(" ", "_").toUpperCase();
        // Workaround if the receiver returns "HDMI2" instead of "HDMI_2". We can't really change the input IDs in the
        // thing type description, because we still need to send "HDMI_2" for an input change to the receiver.
        if (name.length() >= 5 && name.startsWith("HDMI") && name.charAt(4) != '_') {
            // Adds the missing underscore.
            name = name.replace("HDMI", "HDMI_");
        }
        return name;
    }

    /**
     * Holds a list of all the inputs names that should NOT be transformed by the {@link #convertNameToID(String)} method.
     */
    private static final Set<String> INPUTS_NO_CONVERSION = Sets.newHashSet(
            YamahaReceiverBindingConstants.INPUT_SPOTIFY,
            YamahaReceiverBindingConstants.INPUT_BLUETOOTH);

    /**
     * Wraps the XML message with the zone tags. Example with zone=Main_Zone:
     * <Main_Zone>message</Main_Zone>.
     *
     * @param message XML message
     * @return
     */
    public static String wrZone(Zone zone, String message) {
        return "<" + zone.name() + ">" + message + "</" + zone.name() + ">";
    }
}
