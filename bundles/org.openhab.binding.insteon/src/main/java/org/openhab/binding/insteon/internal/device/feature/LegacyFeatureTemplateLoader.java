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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
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
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class LegacyFeatureTemplateLoader extends InsteonResourceLoader {
    private static final LegacyFeatureTemplateLoader FEATURE_TEMPLATE_LOADER = new LegacyFeatureTemplateLoader();
    private static final String RESOURCE_NAME = "/legacy-device-features.xml";

    private static Map<String, LegacyFeatureTemplate> features = new HashMap<>();

    private LegacyFeatureTemplateLoader() {
        super(RESOURCE_NAME);
    }

    public @Nullable LegacyFeatureTemplate getTemplate(String name) {
        return features.get(name);
    }

    public Map<String, LegacyFeatureTemplate> getTemplates() {
        return features;
    }

    @Override
    protected void parseDocument(Element element) throws SAXException {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("feature".equals(child.getTagName())) {
                    parseFeature(child);
                }
            }
        }
    }

    private void parseFeature(Element element) throws SAXException {
        String name = element.getAttribute("name");
        boolean statusFeature = "true".equals(element.getAttribute("statusFeature"));
        LegacyFeatureTemplate feature = new LegacyFeatureTemplate(name, statusFeature, element.getAttribute("timeout"));

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("message-handler".equals(child.getTagName())) {
                    parseMessageHandler(child, feature);
                } else if ("command-handler".equals(child.getTagName())) {
                    parseCommandHandler(child, feature);
                } else if ("message-dispatcher".equals(child.getTagName())) {
                    parseMessageDispatcher(child, feature);
                } else if ("poll-handler".equals(child.getTagName())) {
                    parsePollHandler(child, feature);
                }
            }
        }

        features.put(name, feature);
    }

    private HandlerEntry makeHandlerEntry(Element element) throws SAXException {
        String handler = element.getTextContent();
        if (handler == null) {
            throw new SAXException("Could not find Handler for: " + element.getTextContent());
        }

        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            params.put(node.getNodeName(), node.getNodeValue());
        }
        return new HandlerEntry(handler, params);
    }

    private void parseMessageHandler(Element element, LegacyFeatureTemplate template) throws SAXException {
        HandlerEntry he = makeHandlerEntry(element);
        if ("true".equals(element.getAttribute("default"))) {
            template.setDefaultMessageHandler(he);
        } else {
            String attr = element.getAttribute("cmd");
            int command = (attr == null) ? 0 : HexUtils.toInteger(attr);
            template.addMessageHandler(command, he);
        }
    }

    private void parseCommandHandler(Element element, LegacyFeatureTemplate template) throws SAXException {
        HandlerEntry he = makeHandlerEntry(element);
        if ("true".equals(element.getAttribute("default"))) {
            template.setDefaultCommandHandler(he);
        } else {
            Class<? extends Command> command = parseCommandClass(element.getAttribute("command"));
            template.addCommandHandler(command, he);
        }
    }

    private void parseMessageDispatcher(Element element, LegacyFeatureTemplate template) throws SAXException {
        HandlerEntry he = makeHandlerEntry(element);
        template.setMessageDispatcher(he);
    }

    private void parsePollHandler(Element element, LegacyFeatureTemplate template) throws SAXException {
        HandlerEntry he = makeHandlerEntry(element);
        template.setPollHandler(he);
    }

    private Class<? extends Command> parseCommandClass(String command) throws SAXException {
        if ("OnOffType".equals(command)) {
            return OnOffType.class;
        } else if ("PercentType".equals(command)) {
            return PercentType.class;
        } else if ("DecimalType".equals(command)) {
            return DecimalType.class;
        } else if ("IncreaseDecreaseType".equals(command)) {
            return IncreaseDecreaseType.class;
        } else {
            throw new SAXException("Unknown Command Type");
        }
    }

    public static synchronized LegacyFeatureTemplateLoader instance() {
        if (FEATURE_TEMPLATE_LOADER.getTemplates().isEmpty()) {
            FEATURE_TEMPLATE_LOADER.initialize();
        }
        return FEATURE_TEMPLATE_LOADER;
    }
}
