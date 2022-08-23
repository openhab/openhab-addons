/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceType.FeatureEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the device types from an xml file.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class DeviceTypeLoader {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTypeLoader.class);
    private static DeviceTypeLoader deviceTypeLoader = new DeviceTypeLoader();
    private Map<String, DeviceType> deviceTypes = new LinkedHashMap<>();
    private Map<String, FeatureEntry> baseFeatures = new LinkedHashMap<>();

    /**
     * Finds the device type for a given name
     *
     * @param name device type name to search for
     * @return the device type, or null if not found
     */
    public @Nullable DeviceType getDeviceType(@Nullable String name) {
        return deviceTypes.get(name);
    }

    /**
     * Must call loadDeviceTypesXML() before calling this function!
     *
     * @return currently known device types
     */
    public Map<String, DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    /**
     * Reads the device types from input stream and stores them in memory for
     * later access.
     *
     * @param stream the input stream from which to read
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadDeviceTypesXML(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
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
        Node root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("device-type".equals(nodeName)) {
                    parseDeviceType(child);
                } else if ("base-features".equals(nodeName)) {
                    parseBaseFeatures(child);
                }
            }
        }
    }

    /**
     * Parses device type node
     *
     * @param element element to parse
     * @throws SAXException
     */
    private void parseDeviceType(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if ("".equals(name)) {
            throw new SAXException("device type in device_types file has no name!");
        }
        if (deviceTypes.containsKey(name)) {
            logger.warn("overwriting previous definition of device type {}", name);
            deviceTypes.remove(name);
        }
        Map<String, Boolean> flags = getFlags(element);
        Map<String, FeatureEntry> features = new LinkedHashMap<>();

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("feature".equals(nodeName)) {
                    parseFeature(child, features);
                } else if ("feature-group".equals(nodeName)) {
                    parseFeatureGroup(child, features);
                }
            }
        }
        // add base features if device type not network brige or x10 categories
        if (!name.startsWith("NetworkBridge") && !name.startsWith("X10")) {
            baseFeatures.forEach((key, feature) -> features.putIfAbsent(key, feature));
        }
        deviceTypes.put(name, new DeviceType(name, flags, features));
    }

    /**
     * Parses base features node
     *
     * @param element element to parse
     * @throws SAXException
     */
    private void parseBaseFeatures(Element element) throws SAXException {
        if (!baseFeatures.isEmpty()) {
            throw new SAXException("base features have already been loaded");
        }

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("feature".equals(nodeName)) {
                    parseFeature(child, baseFeatures);
                }
            }
        }
    }

    /**
     * Parses feature node
     *
     * @param element element to parse
     * @param features features map to update
     * @throws SAXException
     */
    private String parseFeature(Element element, Map<String, FeatureEntry> features) throws SAXException {
        String name = element.getAttribute("name");
        if ("".equals(name)) {
            throw new SAXException("undefined feature name");
        }
        String type = element.getTextContent();
        if (type == null) {
            throw new SAXException("undefined feature type");
        }
        Map<String, String> params = getParameters(element);
        FeatureEntry feature = new FeatureEntry(name, type, params);
        if (features.putIfAbsent(name, feature) != null) {
            throw new SAXException("duplicate feature: " + name);
        }
        return name;
    }

    /**
     * Parses feature group node
     *
     * @param element element to parse
     * @param features features map to update
     * @throws SAXException
     */
    private String parseFeatureGroup(Element element, Map<String, FeatureEntry> features) throws SAXException {
        String name = element.getAttribute("name");
        if ("".equals(name)) {
            throw new SAXException("undefined feature group name");
        }
        String type = element.getAttribute("type");
        if ("".equals(type)) {
            throw new SAXException("undefined feature group type");
        }
        Map<String, String> params = getParameters(element);
        FeatureEntry feature = new FeatureEntry(name, type, params);
        if (features.putIfAbsent(name, feature) != null) {
            throw new SAXException("duplicate feature group: " + name);
        }

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("feature".equals(nodeName)) {
                    feature.addConnectedFeature(parseFeature(child, features));
                }
            }
        }
        return name;
    }

    /**
     * Returns flags based on a given element attributes
     *
     * @param element element to parse
     * @return flags map
     * @throws SAXException
     */
    private Map<String, Boolean> getFlags(Element element) throws SAXException {
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

    /**
     * Returns parameters based on a given element attributes
     *
     * @param element element to parse
     * @return parameters map
     * @throws SAXException
     */
    private Map<String, String> getParameters(Element element) throws SAXException {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> params = new HashMap<>();
        List<String> excludeList = Arrays.asList("name", "type");
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String nodeName = attribute.getNodeName();
            String nodeValue = attribute.getNodeValue();
            if (!excludeList.contains(nodeName)) {
                params.put(nodeName, nodeValue);
            }
        }
        return params;
    }

    /**
     * Helper function for debugging
     */
    private void logDeviceTypes() {
        deviceTypes.values().stream().map(String::valueOf).forEach(logger::debug);
    }

    /**
     * Singleton instance function
     *
     * @return DeviceTypeLoader singleton reference
     */
    public static synchronized DeviceTypeLoader instance() {
        if (deviceTypeLoader.getDeviceTypes().isEmpty()) {
            InputStream input = DeviceTypeLoader.class.getResourceAsStream("/device_types.xml");
            try {
                if (input != null) {
                    deviceTypeLoader.loadDeviceTypesXML(input);
                } else {
                    logger.warn("Resource stream is null, cannot read xml file.");
                }
            } catch (ParserConfigurationException e) {
                logger.warn("parser config error when reading device types xml file: ", e);
            } catch (SAXException e) {
                logger.warn("SAX exception when reading device types xml file: ", e);
            } catch (IOException e) {
                logger.warn("I/O exception when reading device types xml file: ", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("loaded {} device types: ", deviceTypeLoader.getDeviceTypes().size());
                deviceTypeLoader.logDeviceTypes();
            }
        }
        return deviceTypeLoader;
    }
}
