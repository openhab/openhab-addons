/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility methods for XML handling
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - DAB support, Spotify support, refactoring, input name conversion fix, Input mapping fix
 */
public class XMLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);

    // We need a lot of xml parsing. Create a document builder beforehand.
    static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();

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

    static Stream<Element> getChildElements(Node node) {
        if (node == null) {
            return Stream.empty();
        }
        return toStream(node.getChildNodes()).filter(x -> x.getNodeType() == Node.ELEMENT_NODE).map(x -> (Element) x);
    }

    static Stream<Node> toStream(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }

    /**
     * Retrieves the child node according to the xpath expression.
     *
     * @param root
     * @param nodePath
     * @return
     * @throws ReceivedMessageParseException when the child node does not exist throws
     *             {@link ReceivedMessageParseException}.
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
     *
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
     * Finds the node starting with the root and following the path.
     * If the node is found it's inner text is returned, otherwise the default provided value.
     * The first path that exists is returned.
     *
     * @param root
     * @param nodePaths
     * @param defaultValue
     * @return
     */
    public static String getAnyNodeContentOrDefault(Node root, String defaultValue, String... nodePaths) {
        for (String nodePath : nodePaths) {
            String value = getNodeContentOrDefault(root, nodePath, (String) null);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * Finds the node starting with the root and following the path. If the node is found it's inner text is returned,
     * otherwise the default provided value.
     *
     * @param root
     * @param nodePath
     * @return
     */
    public static String getNodeContentOrEmpty(Node root, String nodePath) {
        return getNodeContentOrDefault(root, nodePath, "");
    }

    /**
     * Finds the node starting with the root and following the path. If the node is found it's inner text is returned,
     * otherwise the default provided value.
     *
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
                LOG.trace(
                        "The value '{}' of node with path {} could not been parsed to an integer. Applying default of {}",
                        node.getTextContent(), nodePath, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Parse the given xml message into a xml document node.
     *
     * @param message XML formatted message.
     * @return Return the response as xml node or throws an exception if response is not xml.
     * @throws IOException
     */
    public static Document xml(String message) throws IOException, ReceivedMessageParseException {
        // Ensure the message contains XML declaration
        String response = message.startsWith("<?xml") ? message
                : "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + message;

        try {
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            DBF.setFeature("http://xml.org/sax/features/external-general-entities", false);
            DBF.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DBF.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DBF.setXIncludeAware(false);
            DBF.setExpandEntityReferences(false);
            return DBF.newDocumentBuilder().parse(new InputSource(new StringReader(response)));
        } catch (SAXException | ParserConfigurationException e) {
            throw new ReceivedMessageParseException(e);
        }
    }

    /**
     * Wraps the XML message with the zone tags. Example with zone=Main_Zone:
     * {@code
     * <Main_Zone>message</Main_Zone>
     * }.
     *
     * @param message XML message
     * @return
     */
    public static String wrZone(Zone zone, String message) {
        return "<" + zone.name() + ">" + message + "</" + zone.name() + ">";
    }
}
