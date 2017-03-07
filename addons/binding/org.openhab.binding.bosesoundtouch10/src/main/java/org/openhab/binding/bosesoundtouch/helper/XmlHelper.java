/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link XmlHelper} class parses messages coming from the speaker through the websocket.
 *
 * @author syracom - Initial contribution
 */
public class XmlHelper {
    private Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    public static final String WS_UPDATE_VOLUME = "/updates/volumeUpdated/volume/actualvolume";
    public static final String WS_UPDATE_BASS = "/updates/bassUpdated";
    public static final String WS_UPDATE_NOWPLAYING = "/updates/nowPlayingUpdated/nowPlaying/ContentItem/itemName";
    public static final String WS_UPDATE_SOURCE = "/updates/nowPlayingUpdated/nowPlaying/ContentItem"; // source

    public static final String WS_UPDATE_SELECTION = "/updates/nowSelectionUpdated/preset"; // id

    public static final String REST_CURRENT_VOLUME = "/volume/actualvolume";
    public static final String REST_CURRENT_BASS = "/bass/actualbass";
    public static final String REST_NOWPLAYING = "/nowPlaying";
    public static final String REST_NOWPLAYING_ITEMNAME = "/nowPlaying/ContentItem/itemName";
    public static final String REST_CURRENT_PRESET = "/presets/preset";

    public static final String NODE_ATTRIBUTE_ID = "id";
    public static final String NODE_ATTRIBUTE_SOURCE = "source";

    private static final Set<String> SUPPORTED_MESSAGES_TYPE0 = new HashSet<>();
    static {
        SUPPORTED_MESSAGES_TYPE0.add(WS_UPDATE_BASS);
    }

    private static final Set<String> SUPPORTED_MESSAGES_TYPE1 = new HashSet<>();
    static {
        SUPPORTED_MESSAGES_TYPE1.add(WS_UPDATE_VOLUME);
        SUPPORTED_MESSAGES_TYPE1.add(WS_UPDATE_NOWPLAYING);
    }

    private static final Set<String> SUPPORTED_MESSAGES_TYPE2 = new HashSet<>();
    static {
        SUPPORTED_MESSAGES_TYPE2.add(WS_UPDATE_SOURCE);
        SUPPORTED_MESSAGES_TYPE2.add(WS_UPDATE_SELECTION);
    }

    private synchronized Document createXmlDocument(String message) {
        Document xmlDocument = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            xmlDocument = builder.parse(new ByteArrayInputStream(message.getBytes()));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.debug("Exception on creating xml document");
        }
        return xmlDocument;
    }

    public synchronized Set<XmlResult> parseMessage(String message) {
        Set<XmlResult> resultSet = new HashSet<XmlResult>();
        if (message == null || message.isEmpty()) {
            return resultSet;
        }
        String result = "";
        try {
            Document xmlDocument = createXmlDocument(message);
            if (xmlDocument == null) {
                return resultSet;
            }
            XPath xPath = XPathFactory.newInstance().newXPath();
            for (String expression : SUPPORTED_MESSAGES_TYPE0) {
                // XPATH does not work for this
                if (message.contains("bassUpdated")) {
                    resultSet.add(new XmlResult(expression, ""));
                }
            }
            for (String expression : SUPPORTED_MESSAGES_TYPE1) {
                result = xPath.compile(expression).evaluate(xmlDocument);
                if (!result.isEmpty()) {
                    resultSet.add(new XmlResult(expression, result));
                }
            }
            NodeList nodeList;
            Node node;
            Element element;
            for (String expression : SUPPORTED_MESSAGES_TYPE2) {
                nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        element = (Element) node;
                        if (!element.getAttribute(NODE_ATTRIBUTE_SOURCE).isEmpty()) {
                            result = element.getAttribute(NODE_ATTRIBUTE_SOURCE);
                            resultSet.add(new XmlResult(expression, result));
                        }
                        if (!element.getAttribute(NODE_ATTRIBUTE_ID).isEmpty()) {
                            result = element.getAttribute(NODE_ATTRIBUTE_ID);
                            resultSet.add(new XmlResult(expression, result));
                        }
                    }
                }
            }
        } catch (XPathExpressionException e) {
            logger.debug("Exception on XPath");
        }
        return resultSet;
    }

    public synchronized String parsePath(String expression, String message) {
        String result = "";
        if (message == null || message.isEmpty()) {
            return "";
        }
        try {
            Document xmlDocument = createXmlDocument(message);
            XPath xPath = XPathFactory.newInstance().newXPath();
            result = xPath.compile(expression).evaluate(xmlDocument);
        } catch (XPathExpressionException e) {
            logger.debug("Exception on XMLHelper");
        }
        return result;
    }

    public synchronized String parsePresets(String expression, String message, String sourceName) {
        String result = "";
        if (message == null || message.isEmpty()) {
            return "";
        }
        try {
            Document xmlDocument = createXmlDocument(message);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList presets = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            String id = "";
            for (int i = 0; i < presets.getLength(); i++) {
                Node nNode = presets.item(i);
                Element eElement = (Element) nNode;
                id = eElement.getAttribute(NODE_ATTRIBUTE_ID);
                NodeList childNodes = nNode.getChildNodes(); // ContentItems
                Node node = childNodes.item(0).getChildNodes().item(0);// itemName
                String value = node.getTextContent();
                if (value.equals(sourceName)) {
                    return id;
                }
            }
        } catch (XPathExpressionException e) {
            logger.debug("Exception on XMLHelper");
        }
        return result;
    }

    public synchronized String parseSource(String expression, String message, String attributeName) {
        String result = "";
        if (message == null || message.isEmpty()) {
            return "";
        }
        try {
            Document xmlDocument = createXmlDocument(message);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nowplaying = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            Element e = (Element) nowplaying.item(0);
            return e.getAttribute(attributeName);
        } catch (XPathExpressionException e) {
            logger.debug("Exception on XMLHelper");
        }
        return result;
    }
}
