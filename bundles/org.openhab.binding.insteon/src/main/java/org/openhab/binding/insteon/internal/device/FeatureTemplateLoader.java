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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.FeatureTemplate.HandlerEntry;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class that loads the device feature templates from an xml stream
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class FeatureTemplateLoader {
    private static final Logger logger = LoggerFactory.getLogger(FeatureTemplateLoader.class);
    private static FeatureTemplateLoader featureTemplateLoader = new FeatureTemplateLoader();
    private Map<String, FeatureTemplate> templates = new HashMap<>();

    /**
     * Finds feature template for a given type
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
     * Reads the feature templates from input stream and stores them in memory for
     * later access.
     *
     * @param stream the input stream from which to read
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadFeatureTemplatesXML(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {
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
                if ("feature-type".equals(nodeName)) {
                    parseFeature(child);
                }
            }
        }
    }

    /**
     * Parses feature node
     *
     * @param e element to parse
     * @throws SAXException
     */
    private void parseFeature(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if ("".equals(name)) {
            throw new SAXException("feature template in device_features file has no name!");
        }
        if (templates.containsKey(name)) {
            logger.warn("overwriting previous definition of feature template {}", name);
            templates.remove(name);
        }
        Map<String, String> params = getParameters(element);
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
            int command = getCommandHexValue(element.getAttribute("command"));
            template.addMessageHandler(command, handler);
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
            Class<? extends Command> classRef = getCommandClass(element.getAttribute("command"));
            template.addCommandHandler(classRef, handler);
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
        return new HandlerEntry(name, getParameters(element));
    }

    /**
     * Returns command class for a given class name
     *
     * @param name command class name
     * @return command class
     * @throws SAXException
     */
    private Class<? extends Command> getCommandClass(String name) throws SAXException {
        if ("OnOffType".equals(name)) {
            return OnOffType.class;
        } else if ("PercentType".equals(name)) {
            return PercentType.class;
        } else if ("DecimalType".equals(name)) {
            return DecimalType.class;
        } else if ("IncreaseDecreaseType".equals(name)) {
            return IncreaseDecreaseType.class;
        } else if ("QuantityType".equals(name)) {
            return QuantityType.class;
        } else if ("StringType".equals(name)) {
            return StringType.class;
        } else if ("UpDownType".equals(name)) {
            return UpDownType.class;
        } else if ("StopMoveType".equals(name)) {
            return StopMoveType.class;
        } else if ("RefreshType".equals(name)) {
            return RefreshType.class;
        } else {
            throw new SAXException("unknown command class: " + name);
        }
    }

    /**
     * Returns command hex value for a given hex string
     *
     * @param hex command hex string
     * @return command hex value
     * @throws SAXException
     */
    private int getCommandHexValue(String hex) throws SAXException {
        try {
            return ByteUtils.hexStringToInteger(hex);
        } catch (NumberFormatException e) {
            throw new SAXException("invalid command hex value: " + hex);
        }
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
        List<String> excludeList = Arrays.asList("name", "command", "default");
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
    private void logTemplates() {
        templates.values().stream().map(String::valueOf).forEach(logger::debug);
    }

    /**
     * Singleton instance function
     *
     * @return FeatureTemplateLoader singleton reference
     */
    public static synchronized FeatureTemplateLoader instance() {
        if (featureTemplateLoader.getTemplates().isEmpty()) {
            InputStream input = FeatureTemplateLoader.class.getResourceAsStream("/device_features.xml");
            try {
                if (input != null) {
                    featureTemplateLoader.loadFeatureTemplatesXML(input);
                } else {
                    logger.warn("Resource stream is null, cannot read xml file.");
                }
            } catch (ParserConfigurationException e) {
                logger.warn("parser config error when reading device features xml file: ", e);
            } catch (SAXException e) {
                logger.warn("SAX exception when reading device features xml file: ", e);
            } catch (IOException e) {
                logger.warn("I/O exception when reading device features xml file: ", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("loaded {} feature templates: ", featureTemplateLoader.getTemplates().size());
                featureTemplateLoader.logTemplates();
            }
        }
        return featureTemplateLoader;
    }
}
