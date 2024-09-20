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
import org.openhab.binding.insteon.internal.device.feature.LegacyFeatureTemplate.HandlerEntry;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
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
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class LegacyFeatureTemplateLoader {
    private static final LegacyFeatureTemplateLoader FEATURE_TEMPLATE_LOADER = new LegacyFeatureTemplateLoader();

    private final Logger logger = LoggerFactory.getLogger(LegacyFeatureTemplateLoader.class);

    private static Map<String, LegacyFeatureTemplate> features = new HashMap<>();

    public @Nullable LegacyFeatureTemplate getTemplate(String name) {
        return features.get(name);
    }

    public Map<String, LegacyFeatureTemplate> getTemplates() {
        return features;
    }

    public void initialize() {
        InputStream input = LegacyFeatureTemplateLoader.class.getResourceAsStream("/legacy-device-features.xml");
        if (input != null) {
            loadFeatureTemplates(input);
        } else {
            logger.warn("Resource stream is null, cannot read xml file.");
        }
    }

    private void loadFeatureTemplates(InputStream input) {
        try {
            parseTemplates(input);
        } catch (IOException e) {
            logger.warn("IOException while reading device features", e);
        } catch (ParsingException e) {
            logger.warn("Parsing exception while reading device features", e);
        }
    }

    public void loadFeatureTemplates(String file) {
        try {
            InputStream input = new FileInputStream(file);
            loadFeatureTemplates(input);
        } catch (FileNotFoundException e) {
            logger.warn("cannot read feature templates from file {} ", file, e);
        }
    }

    private void parseTemplates(InputStream input) throws IOException, ParsingException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse it!
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    if ("feature".equals(e.getTagName())) {
                        parseFeature(e);
                    }
                }
            }
        } catch (SAXException e) {
            throw new ParsingException("Failed to parse XML!", e);
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Got parser config exception! ", e);
        }
    }

    private void parseFeature(Element e) throws ParsingException {
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

    private HandlerEntry makeHandlerEntry(Element e) throws ParsingException {
        String handler = e.getTextContent();
        if (handler == null) {
            throw new ParsingException("Could not find Handler for: " + e.getTextContent());
        }

        NamedNodeMap attributes = e.getAttributes();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node n = attributes.item(i);
            params.put(n.getNodeName(), n.getNodeValue());
        }
        return new HandlerEntry(handler, params);
    }

    private void parseMessageHandler(Element e, LegacyFeatureTemplate f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if ("true".equals(e.getAttribute("default"))) {
            f.setDefaultMessageHandler(he);
        } else {
            String attr = e.getAttribute("cmd");
            int command = (attr == null) ? 0 : HexUtils.toInteger(attr);
            f.addMessageHandler(command, he);
        }
    }

    private void parseCommandHandler(Element e, LegacyFeatureTemplate f) throws ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if ("true".equals(e.getAttribute("default"))) {
            f.setDefaultCommandHandler(he);
        } else {
            Class<? extends Command> command = parseCommandClass(e.getAttribute("command"));
            f.addCommandHandler(command, he);
        }
    }

    private void parseMessageDispatcher(Element e, LegacyFeatureTemplate f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setMessageDispatcher(he);
    }

    private void parsePollHandler(Element e, LegacyFeatureTemplate f) throws ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setPollHandler(he);
    }

    private Class<? extends Command> parseCommandClass(String c) throws ParsingException {
        if ("OnOffType".equals(c)) {
            return OnOffType.class;
        } else if ("PercentType".equals(c)) {
            return PercentType.class;
        } else if ("DecimalType".equals(c)) {
            return DecimalType.class;
        } else if ("IncreaseDecreaseType".equals(c)) {
            return IncreaseDecreaseType.class;
        } else {
            throw new ParsingException("Unknown Command Type");
        }
    }

    public static synchronized LegacyFeatureTemplateLoader instance() {
        if (FEATURE_TEMPLATE_LOADER.getTemplates().isEmpty()) {
            FEATURE_TEMPLATE_LOADER.initialize();
        }
        return FEATURE_TEMPLATE_LOADER;
    }

    public static class ParsingException extends Exception {
        private static final long serialVersionUID = 3997461423241843949L;

        public ParsingException(String msg) {
            super(msg);
        }

        public ParsingException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
