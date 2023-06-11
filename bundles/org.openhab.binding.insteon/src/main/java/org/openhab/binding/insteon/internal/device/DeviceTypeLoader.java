/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceType.FeatureGroup;
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
 */
@NonNullByDefault
public class DeviceTypeLoader {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTypeLoader.class);
    private Map<String, DeviceType> deviceTypes = new HashMap<>();
    private static DeviceTypeLoader deviceTypeLoader = new DeviceTypeLoader();

    private DeviceTypeLoader() {
    } // private so nobody can call it

    /**
     * Finds the device type for a given product key
     *
     * @param aProdKey product key to search for
     * @return the device type, or null if not found
     */
    public @Nullable DeviceType getDeviceType(String aProdKey) {
        return (deviceTypes.get(aProdKey));
    }

    /**
     * Must call loadDeviceTypesXML() before calling this function!
     *
     * @return currently known device types
     */
    public Map<String, DeviceType> getDeviceTypes() {
        return (deviceTypes);
    }

    /**
     * Reads the device types from input stream and stores them in memory for
     * later access.
     *
     * @param in the input stream from which to read
     */
    public void loadDeviceTypesXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
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
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("device")) {
                processDevice((Element) node);
            }
        }
    }

    /**
     * Reads the device types from file and stores them in memory for later access.
     *
     * @param aFileName The name of the file to read from
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadDeviceTypesXML(String aFileName) throws ParserConfigurationException, SAXException, IOException {
        File file = new File(aFileName);
        InputStream in = new FileInputStream(file);
        loadDeviceTypesXML(in);
    }

    /**
     * Process device node
     *
     * @param e name of the element to process
     * @throws SAXException
     */
    private void processDevice(Element e) throws SAXException {
        String productKey = e.getAttribute("productKey");
        if ("".equals(productKey)) {
            throw new SAXException("device in device_types file has no product key!");
        }
        if (deviceTypes.containsKey(productKey)) {
            logger.warn("overwriting previous definition of device {}", productKey);
            deviceTypes.remove(productKey);
        }
        DeviceType devType = new DeviceType(productKey);

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

    private String processFeature(DeviceType devType, Element e) throws SAXException {
        String name = e.getAttribute("name");
        if ("".equals(name)) {
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

    private String processFeatureGroup(DeviceType devType, Element e) throws SAXException {
        String name = e.getAttribute("name");
        if ("".equals(name)) {
            throw new SAXException("feature group " + e.getNodeName() + " has no name attr!");
        }
        String type = e.getAttribute("type");
        if ("".equals(type)) {
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
        }
        return deviceTypeLoader;
    }
}
