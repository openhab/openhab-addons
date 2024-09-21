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
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.binding.insteon.internal.utils.ResourceLoader;
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
public class LegacyFeatureTemplateLoader extends ResourceLoader {
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
    protected void parseDocument(Element e) throws SAXException {
        NodeList nodes = e.getChildNodes();
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

    private void parseFeature(Element e) throws SAXException {
        String name = e.getAttribute("name");
        boolean statusFeature = "true".equals(e.getAttribute("statusFeature"));
        LegacyFeatureTemplate feature = new LegacyFeatureTemplate(name, statusFeature, e.getAttribute("timeout"));

        NodeList nodes = e.getChildNodes();
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

    private HandlerEntry makeHandlerEntry(Element e) throws SAXException {
        String handler = e.getTextContent();
        if (handler == null) {
            throw new SAXException("Could not find Handler for: " + e.getTextContent());
        }

        NamedNodeMap attributes = e.getAttributes();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node n = attributes.item(i);
            params.put(n.getNodeName(), n.getNodeValue());
        }
        return new HandlerEntry(handler, params);
    }

    private void parseMessageHandler(Element e, LegacyFeatureTemplate f) throws SAXException {
        HandlerEntry he = makeHandlerEntry(e);
        if ("true".equals(e.getAttribute("default"))) {
            f.setDefaultMessageHandler(he);
        } else {
            String attr = e.getAttribute("cmd");
            int command = (attr == null) ? 0 : HexUtils.toInteger(attr);
            f.addMessageHandler(command, he);
        }
    }

    private void parseCommandHandler(Element e, LegacyFeatureTemplate f) throws SAXException {
        HandlerEntry he = makeHandlerEntry(e);
        if ("true".equals(e.getAttribute("default"))) {
            f.setDefaultCommandHandler(he);
        } else {
            Class<? extends Command> command = parseCommandClass(e.getAttribute("command"));
            f.addCommandHandler(command, he);
        }
    }

    private void parseMessageDispatcher(Element e, LegacyFeatureTemplate f) throws SAXException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setMessageDispatcher(he);
    }

    private void parsePollHandler(Element e, LegacyFeatureTemplate f) throws SAXException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setPollHandler(he);
    }

    private Class<? extends Command> parseCommandClass(String c) throws SAXException {
        if ("OnOffType".equals(c)) {
            return OnOffType.class;
        } else if ("PercentType".equals(c)) {
            return PercentType.class;
        } else if ("DecimalType".equals(c)) {
            return DecimalType.class;
        } else if ("IncreaseDecreaseType".equals(c)) {
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
