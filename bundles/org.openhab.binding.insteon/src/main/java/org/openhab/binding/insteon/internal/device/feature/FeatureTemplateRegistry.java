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
package org.openhab.binding.insteon.internal.device.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link FeatureTemplateRegistry} represents the feature template registry
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class FeatureTemplateRegistry extends InsteonResourceLoader {
    private static final FeatureTemplateRegistry FEATURE_TEMPLATE_REGISTRY = new FeatureTemplateRegistry();
    private static final String RESOURCE_NAME = "/device-features.xml";

    private Map<String, FeatureTemplate> templates = new HashMap<>();

    private FeatureTemplateRegistry() {
        super(RESOURCE_NAME);
    }

    /**
     * Returns feature template for a given type
     *
     * @param type feature type to match
     * @return feature template if found, otherwise null
     */
    public @Nullable FeatureTemplate getTemplate(String type) {
        return templates.get(type);
    }

    /**
     * Returns known feature templates
     *
     * @return currently known feature templates
     */
    public Map<String, FeatureTemplate> getTemplates() {
        return templates;
    }

    /**
     * Initializes feature template registry
     */
    @Override
    protected void initialize() {
        super.initialize();

        logger.debug("loaded {} feature templates", templates.size());
        if (logger.isTraceEnabled()) {
            templates.values().stream().map(String::valueOf).forEach(logger::trace);
        }
    }

    /**
     * Parses feature template document
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
                if ("feature-type".equals(nodeName)) {
                    parseFeatureType(child);
                }
            }
        }
    }

    /**
     * Parses feature type node
     *
     * @param element element to parse
     * @throws SAXException
     */
    private void parseFeatureType(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("feature template in device_features file has no name!");
        }
        if (templates.containsKey(name)) {
            logger.warn("overwriting previous definition of feature template {}", name);
            templates.remove(name);
        }
        Map<String, String> params = getParameters(element, List.of("name"));
        FeatureTemplate template = new FeatureTemplate(name, params);

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("message-handler".equals(nodeName)) {
                    parseMessageHandler(child, template);
                } else if ("command-handler".equals(nodeName)) {
                    parseCommandHandler(child, template);
                } else if ("message-dispatcher".equals(nodeName)) {
                    parseMessageDispatcher(child, template);
                } else if ("poll-handler".equals(nodeName)) {
                    parsePollHandler(child, template);
                }
            }
        }
        templates.put(name, template);
    }

    /**
     * Parses message handler node
     *
     * @param element element to parse
     * @param template feature template to update
     * @throws SAXException
     */
    private void parseMessageHandler(Element element, FeatureTemplate template) throws SAXException {
        HandlerEntry handler = makeHandlerEntry(element);
        if ("true".equals(element.getAttribute("default"))) {
            template.setDefaultMessageHandler(handler);
        } else {
            String command = element.getAttribute("command");
            if (command.isEmpty()) {
                throw new SAXException("undefined command hex for " + element.getNodeName());
            }
            if (!HexUtils.isValidHexString(command)) {
                throw new SAXException("invalid command hex: " + command);
            }
            template.addMessageHandler(handler);
        }
    }

    /**
     * Parses command handler node
     *
     * @param element element to parse
     * @param template feature template to update
     * @throws SAXException
     */
    private void parseCommandHandler(Element element, FeatureTemplate template) throws SAXException {
        HandlerEntry handler = makeHandlerEntry(element);
        if ("true".equals(element.getAttribute("default"))) {
            template.setDefaultCommandHandler(handler);
        } else {
            String command = element.getAttribute("command");
            if (command.isEmpty()) {
                throw new SAXException("undefined command type for " + element.getNodeName());
            }
            if (!CommandHandler.supportsCommandType(command)) {
                throw new SAXException("unsupported command type: " + command);
            }
            template.addCommandHandler(handler);
        }
    }

    /**
     * Parses message dispatcher node
     *
     * @param element element to parse
     * @param template feature template to update
     * @throws SAXException
     */
    private void parseMessageDispatcher(Element element, FeatureTemplate template) throws SAXException {
        HandlerEntry handler = makeHandlerEntry(element);
        template.setMessageDispatcher(handler);
    }

    /**
     * Parses poll handler node
     *
     * @param element element to parse
     * @param template feature template to update
     * @throws SAXException
     */
    private void parsePollHandler(Element element, FeatureTemplate template) throws SAXException {
        HandlerEntry handler = makeHandlerEntry(element);
        template.setPollHandler(handler);
    }

    /**
     * Creates a new HandlerEntry
     *
     * @param element element to parse
     * @return new HandlerEntry
     * @throws SAXException
     */
    private HandlerEntry makeHandlerEntry(Element element) throws SAXException {
        String name = element.getTextContent();
        if (name == null) {
            throw new SAXException("undefined handler name for " + element.getNodeName());
        }
        Map<String, String> params = getParameters(element, List.of("default"));
        return new HandlerEntry(name, params);
    }

    /**
     * Singleton instance function
     *
     * @return FeatureTemplateRegistry singleton reference
     */
    public static synchronized FeatureTemplateRegistry getInstance() {
        if (FEATURE_TEMPLATE_REGISTRY.getTemplates().isEmpty()) {
            FEATURE_TEMPLATE_REGISTRY.initialize();
        }
        return FEATURE_TEMPLATE_REGISTRY;
    }
}
