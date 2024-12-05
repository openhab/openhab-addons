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
package org.openhab.binding.insteon.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The {@link InsteonResourceLoader} represents an abstract Insteon resource loader
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonResourceLoader {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String name;

    protected InsteonResourceLoader(String name) {
        this.name = name;
    }

    protected void initialize() {
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream != null) {
            loadDocument(stream);
        } else {
            logger.warn("Resource stream {} cannot be found.", name);
        }
    }

    public void loadDocument(String filename) {
        try {
            InputStream stream = new FileInputStream(filename);
            loadDocument(stream);
        } catch (FileNotFoundException e) {
            logger.warn("xml document {} not found", filename);
        }
    }

    protected void loadDocument(InputStream stream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();

            parseDocument(doc.getDocumentElement());
        } catch (ParserConfigurationException e) {
            logger.warn("parser config error when loading xml document:", e);
        } catch (SAXException e) {
            logger.warn("SAX exception when loading xml document:", e);
        } catch (IOException e) {
            logger.warn("I/O exception when loading xml document:", e);
        }
    }

    protected abstract void parseDocument(Element element) throws SAXException;

    protected Map<String, Boolean> getFlags(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, Boolean> flags = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if ("true".equals(nodeValue) || "false".equals(nodeValue)) {
                flags.put(nodeName, "true".equals(nodeValue));
            }
        }
        return flags;
    }

    protected Map<String, String> getParameters(Element element, List<String> excludedAttrs) {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if (!excludedAttrs.contains(nodeName)) {
                params.put(nodeName, nodeValue);
            }
        }
        return params;
    }

    protected int getAttributeAsInteger(Element element, String name) throws SAXException {
        try {
            return Integer.parseInt(element.getAttribute(name));
        } catch (NumberFormatException e) {
            throw new SAXException("invalid integer attribute " + name);
        }
    }

    protected int getAttributeAsInteger(Element element, String name, int defaultValue) throws SAXException {
        return element.hasAttribute(name) ? getAttributeAsInteger(element, name) : defaultValue;
    }

    protected int getHexAttributeAsInteger(Element element, String name) throws SAXException {
        try {
            return HexUtils.toInteger(element.getAttribute(name));
        } catch (NumberFormatException e) {
            throw new SAXException("invalid hex attribute " + name);
        }
    }

    protected int getHexAttributeAsInteger(Element element, String name, int defaultValue) throws SAXException {
        return element.hasAttribute(name) ? getHexAttributeAsInteger(element, name) : defaultValue;
    }

    protected byte getHexAttributeAsByte(Element element, String name) throws SAXException {
        return (byte) (getHexAttributeAsInteger(element, name) & 0xFF);
    }

    protected byte getHexAttributeAsByte(Element element, String name, byte defaultValue) throws SAXException {
        return element.hasAttribute(name) ? getHexAttributeAsByte(element, name) : defaultValue;
    }
}
