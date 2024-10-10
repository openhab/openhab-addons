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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.openhab.binding.insteon.internal.device.LegacyDeviceType.FeatureGroup;
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
public class LegacyDeviceTypeLoader extends InsteonResourceLoader {
    private static final LegacyDeviceTypeLoader DEVICE_TYPE_LOADER = new LegacyDeviceTypeLoader();
    private static final String RESOURCE_NAME = "/legacy-device-types.xml";

    private Map<String, LegacyDeviceType> deviceTypes = new HashMap<>();

    private LegacyDeviceTypeLoader() {
        super(RESOURCE_NAME);
    }

    /**
     * Finds the device type for a given product key
     *
     * @param productKey product key to search for
     * @return the device type, or null if not found
     */
    public @Nullable LegacyDeviceType getDeviceType(String productKey) {
        return deviceTypes.get(productKey);
    }

    /**
     * Returns known device types
     *
     * @return currently known device types
     */
    public Map<String, LegacyDeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    /**
     * Parses the device types document
     *
     * @param element element to parse
     * @throws SAXException
     */
    @Override
    protected void parseDocument(Element element) throws SAXException {
        NodeList nodes = element.getChildNodes();
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
     * @param element name of the element to process
     * @throws SAXException
     */
    private void processDevice(Element element) throws SAXException {
        String productKey = element.getAttribute("productKey");
        if (productKey.isEmpty()) {
            throw new SAXException("device in device_types file has no product key!");
        }
        if (deviceTypes.containsKey(productKey)) {
            logger.warn("overwriting previous definition of device {}", productKey);
            deviceTypes.remove(productKey);
        }
        LegacyDeviceType devType = new LegacyDeviceType(productKey);

        NodeList nodes = element.getChildNodes();
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

    private String processFeature(LegacyDeviceType devType, Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("feature " + element.getNodeName() + " has feature without name!");
        }
        if (!name.equals(name.toLowerCase())) {
            throw new SAXException("feature name '" + name + "' must be lower case");
        }
        if (!devType.addFeature(name, element.getTextContent())) {
            throw new SAXException("duplicate feature: " + name);
        }
        return name;
    }

    private String processFeatureGroup(LegacyDeviceType devType, Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("feature group " + element.getNodeName() + " has no name attr!");
        }
        String type = element.getAttribute("type");
        if (type.isEmpty()) {
            throw new SAXException("feature group " + element.getNodeName() + " has no type attr!");
        }
        FeatureGroup fg = new FeatureGroup(name, type);
        NodeList nodes = element.getChildNodes();
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
        return name;
    }

    /**
     * Singleton instance function, creates DeviceTypeLoader
     *
     * @return DeviceTypeLoader singleton reference
     */
    public static synchronized LegacyDeviceTypeLoader instance() {
        if (DEVICE_TYPE_LOADER.getDeviceTypes().isEmpty()) {
            DEVICE_TYPE_LOADER.initialize();
        }
        return DEVICE_TYPE_LOADER;
    }
}
