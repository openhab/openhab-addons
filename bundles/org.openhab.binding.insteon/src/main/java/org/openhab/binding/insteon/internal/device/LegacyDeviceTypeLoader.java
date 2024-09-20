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
package org.openhab.binding.insteon.internal.device;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.LegacyDeviceType.FeatureGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the device types from an xml file.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class LegacyDeviceTypeLoader {
    private static final LegacyDeviceTypeLoader DEVICE_TYPE_LOADER = new LegacyDeviceTypeLoader();

    private final Logger logger = LoggerFactory.getLogger(LegacyDeviceTypeLoader.class);

    private Map<String, LegacyDeviceType> deviceTypes = new HashMap<>();

    private LegacyDeviceTypeLoader() {
    } // private so nobody can call it

    /**
     * Finds the device type for a given product key
     *
     * @param aProdKey product key to search for
     * @return the device type, or null if not found
     */
    public @Nullable LegacyDeviceType getDeviceType(String aProdKey) {
        return (deviceTypes.get(aProdKey));
    }

    /**
     * Must call loadDeviceTypes() before calling this function!
     *
     * @return currently known device types
     */
    public Map<String, LegacyDeviceType> getDeviceTypes() {
        return (deviceTypes);
    }

    /**
     * Initializes the device types loader
     */
    public void initialize() {
        InputStream input = LegacyDeviceTypeLoader.class.getResourceAsStream("/legacy-device-types.xml");
        if (input != null) {
            loadDeviceTypes(input);
        } else {
            logger.warn("Resource stream is null, cannot read xml file.");
        }
    }

    /**
     * Loads the device types from input stream and stores them in memory for
     * later access.
     *
     * @param in the input stream from which to read
     */
    private void loadDeviceTypes(InputStream in) {
        try {
            parseDeviceTypes(in);
        } catch (ParserConfigurationException e) {
            logger.warn("parser config error when reading device types xml file: ", e);
        } catch (SAXException e) {
            logger.warn("SAX exception when reading device types xml file: ", e);
        } catch (IOException e) {
            logger.warn("I/O exception when reading device types xml file: ", e);
        }
    }

    /**
     * Loads the device types from file and stores them in memory for later access.
     *
     * @param file The name of the file to read from
     */
    public void loadDeviceTypes(String file) {
        try {
            InputStream in = new FileInputStream(file);
            loadDeviceTypes(in);
        } catch (FileNotFoundException e) {
            logger.warn("cannot read device types from file {} ", file, e);
        }
    }

    /**
     * Parses the device types from input stream and stores them in memory for
     * later access.
     *
     * @param in the input stream from which to read
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void parseDeviceTypes(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbFactory.setXIncludeAware(false);
        dbFactory.setExpandEntityReferences(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(in);
        doc.getDocumentElement().normalize();
        Node root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "device".equals(node.getNodeName())) {
                processDevice((Element) node);
            }
        }
    }

    /**
     * Process device node
     *
     * @param e name of the element to process
     * @throws SAXException
     */
    private void processDevice(Element e) throws SAXException {
        String productKey = e.getAttribute("productKey");
        if (productKey.isEmpty()) {
            throw new SAXException("device in device_types file has no product key!");
        }
        if (deviceTypes.containsKey(productKey)) {
            logger.warn("overwriting previous definition of device {}", productKey);
            deviceTypes.remove(productKey);
        }
        LegacyDeviceType devType = new LegacyDeviceType(productKey);

        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element subElement = (Element) node;
            String nodeName = subElement.getNodeName();
            if ("model".equals(nodeName)) {
                devType.setModel(subElement.getTextContent());
            } else if ("description".equals(nodeName)) {
                devType.setDescription(subElement.getTextContent());
            } else if ("feature".equals(nodeName)) {
                processFeature(devType, subElement);
            } else if ("feature_group".equals(nodeName)) {
                processFeatureGroup(devType, subElement);
            }
            deviceTypes.put(productKey, devType);
        }
    }

    private String processFeature(LegacyDeviceType devType, Element e) throws SAXException {
        String name = e.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("feature " + e.getNodeName() + " has feature without name!");
        }
        if (!name.equals(name.toLowerCase())) {
            throw new SAXException("feature name '" + name + "' must be lower case");
        }
        if (!devType.addFeature(name, e.getTextContent())) {
            throw new SAXException("duplicate feature: " + name);
        }
        return (name);
    }

    private String processFeatureGroup(LegacyDeviceType devType, Element e) throws SAXException {
        String name = e.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("feature group " + e.getNodeName() + " has no name attr!");
        }
        String type = e.getAttribute("type");
        if (type.isEmpty()) {
            throw new SAXException("feature group " + e.getNodeName() + " has no type attr!");
        }
        FeatureGroup fg = new FeatureGroup(name, type);
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element subElement = (Element) node;
            String nodeName = subElement.getNodeName();
            if ("feature".equals(nodeName)) {
                fg.addFeature(processFeature(devType, subElement));
            } else if ("feature_group".equals(nodeName)) {
                fg.addFeature(processFeatureGroup(devType, subElement));
            }
        }
        if (!devType.addFeatureGroup(name, fg)) {
            throw new SAXException("duplicate feature group " + name);
        }
        return (name);
    }

    /**
     * Singleton instance function, creates DeviceTypeLoader
     *
     * @return DeviceTypeLoader singleton reference
     */
    @Nullable
    public static synchronized LegacyDeviceTypeLoader instance() {
        if (DEVICE_TYPE_LOADER.getDeviceTypes().isEmpty()) {
            DEVICE_TYPE_LOADER.initialize();
        }
        return DEVICE_TYPE_LOADER;
    }
}
