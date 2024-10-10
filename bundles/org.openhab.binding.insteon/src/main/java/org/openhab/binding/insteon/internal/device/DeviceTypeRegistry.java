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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.openhab.binding.insteon.internal.device.DeviceType.CommandEntry;
import org.openhab.binding.insteon.internal.device.DeviceType.DefaultLinkEntry;
import org.openhab.binding.insteon.internal.device.DeviceType.FeatureEntry;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link DeviceTypeRegistry} represents the device type registry
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class DeviceTypeRegistry extends InsteonResourceLoader {
    private static final DeviceTypeRegistry DEVICE_TYPE_REGISTRY = new DeviceTypeRegistry();
    private static final String RESOURCE_NAME = "/device-types.xml";

    private Map<String, DeviceType> deviceTypes = new LinkedHashMap<>();
    private Map<String, FeatureEntry> baseFeatures = new LinkedHashMap<>();

    private DeviceTypeRegistry() {
        super(RESOURCE_NAME);
    }

    /**
     * Returns the device type for a given name
     *
     * @param name device type name to search for
     * @return the device type, or null if not found
     */
    public @Nullable DeviceType getDeviceType(@Nullable String name) {
        return deviceTypes.get(name);
    }

    /**
     * Returns known device types
     *
     * @return currently known device types
     */
    public Map<String, DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    /**
     * Initializes device type registry
     */
    @Override
    protected void initialize() {
        super.initialize();

        logger.debug("loaded {} device types", deviceTypes.size());
        if (logger.isTraceEnabled()) {
            deviceTypes.values().stream().map(String::valueOf).forEach(logger::trace);
        }
    }

    /**
     * Parses device type document
     *
     * @param element element to parse
     * @throws SAXException
     */
    @Override
    protected void parseDocument(Element element) throws SAXException {
        NodeList nodes = element.getChildNodes();
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
        if (name.isEmpty()) {
            throw new SAXException("device type in device_types file has no name!");
        }
        if (deviceTypes.containsKey(name)) {
            logger.warn("overwriting previous definition of device type {}", name);
            deviceTypes.remove(name);
        }
        Map<String, Boolean> flags = getFlags(element);
        Map<String, FeatureEntry> features = new LinkedHashMap<>();
        Map<String, DefaultLinkEntry> links = new LinkedHashMap<>();

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
                } else if ("default-link".equals(nodeName)) {
                    parseDefaultLink(child, links);
                }
            }
        }
        // add base features if device type not network brige or x10 categories
        if (!name.startsWith("NetworkBridge") && !name.startsWith("X10")) {
            baseFeatures.forEach(features::putIfAbsent);
        }
        deviceTypes.put(name, new DeviceType(name, flags, features, links));
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
     * @return the parsed feature name
     * @throws SAXException
     */
    private String parseFeature(Element element, Map<String, FeatureEntry> features) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("undefined feature name");
        }
        String type = element.getTextContent();
        if (type == null) {
            throw new SAXException("undefined feature type");
        }
        Map<String, String> params = getParameters(element, List.of("name"));
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
    private void parseFeatureGroup(Element element, Map<String, FeatureEntry> features) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("undefined feature group name");
        }
        String type = element.getAttribute("type");
        if (type.isEmpty()) {
            throw new SAXException("undefined feature group type");
        }
        Map<String, String> params = getParameters(element, List.of("name", "type"));
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
    }

    /**
     * Parses default link
     *
     * @param element element to parse
     * @param links links map to update
     * @throws SAXException
     */
    private void parseDefaultLink(Element element, Map<String, DefaultLinkEntry> links) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("undefined default link name");
        }
        boolean isController = "controller".equals(element.getAttribute("type"));
        int group = getAttributeAsInteger(element, "group");
        if (group <= 0 || group >= 255) {
            throw new SAXException("out of bound default link group: " + group);
        }
        byte[] data = { getHexAttributeAsByte(element, "data1"), getHexAttributeAsByte(element, "data2"),
                getHexAttributeAsByte(element, "data3") };

        DefaultLinkEntry link = new DefaultLinkEntry(name, isController, group, data);

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("command".equals(nodeName)) {
                    link.addCommand(getDefaultLinkCommand(child));
                }
            }
        }

        if (links.putIfAbsent(name, link) != null) {
            throw new SAXException("duplicate default link: " + name);
        }
    }

    /**
     * Returns a default link command
     *
     * @param element element to parse
     * @return default link command
     * @throws SAXException
     */
    private CommandEntry getDefaultLinkCommand(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("undefined default link command name");
        }
        int ext = getAttributeAsInteger(element, "ext");
        if (ext < 0 || ext > 2) {
            throw new SAXException("out of bound default link command ext argument: " + ext);
        }
        byte cmd1 = getHexAttributeAsByte(element, "cmd1");
        if (cmd1 == 0) {
            throw new SAXException("invalid default link command cmd1 argument: " + HexUtils.getHexString(cmd1));
        }
        byte cmd2 = getHexAttributeAsByte(element, "cmd2", (byte) 0x00);
        byte[] data = { getHexAttributeAsByte(element, "data1", (byte) 0x00),
                getHexAttributeAsByte(element, "data2", (byte) 0x00),
                getHexAttributeAsByte(element, "data3", (byte) 0x00) };

        return new CommandEntry(name, ext, cmd1, cmd2, data);
    }

    /**
     * Singleton instance function
     *
     * @return DeviceTypeRegistry singleton reference
     */
    public static synchronized DeviceTypeRegistry getInstance() {
        if (DEVICE_TYPE_REGISTRY.getDeviceTypes().isEmpty()) {
            DEVICE_TYPE_REGISTRY.initialize();
        }
        return DEVICE_TYPE_REGISTRY;
    }
}
