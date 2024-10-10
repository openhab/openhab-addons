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
package org.openhab.binding.insteon.internal.transport.message;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link MsgDefinitionRegistry} represents the message definition registry
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class MsgDefinitionRegistry extends InsteonResourceLoader {
    private static final MsgDefinitionRegistry MSG_DEFINITION_REGISTRY = new MsgDefinitionRegistry();
    private static final String RESOURCE_NAME = "/msg-definitions.xml";

    private Map<String, MsgDefinition> definitions = new LinkedHashMap<>();

    private MsgDefinitionRegistry() {
        super(RESOURCE_NAME);
    }

    /**
     * Returns message definition for a given type
     *
     * @param type message type to match
     * @return message definition if found, otherwise null
     */
    public @Nullable MsgDefinition getDefinition(String type) {
        return definitions.get(type);
    }

    /**
     * Returns message definition for a given command and direction
     *
     * @param cmd message command to match
     * @param direction message direction to match
     * @return message definition if found, otherwise null
     */
    public @Nullable MsgDefinition getDefinition(byte cmd, Direction direction) {
        return getDefinition(cmd, null, direction);
    }

    /**
     * Returns message definition for a given command, extended flag and direction
     *
     * @param cmd message command to match
     * @param isExtended if message is extended
     * @param direction message direction to match
     * @return message definition if found, otherwise null
     */
    public @Nullable MsgDefinition getDefinition(byte cmd, @Nullable Boolean isExtended, Direction direction) {
        return definitions.values().stream()
                .filter(definition -> definition.getCommand() == cmd && definition.getDirection() == direction
                        && (isExtended == null || definition.isExtended() == isExtended))
                .findFirst().orElse(null);
    }

    /**
     * Returns known message definitions
     *
     * @return currently known message definitions
     */
    public Map<String, MsgDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * Initializes message definition registry
     */
    @Override
    protected void initialize() {
        super.initialize();

        logger.debug("loaded {} message definitions", definitions.size());
        if (logger.isTraceEnabled()) {
            definitions.entrySet().stream()
                    .map(definition -> String.format("%s->%s", definition.getKey(), definition.getValue()))
                    .forEach(logger::trace);
        }
    }

    /**
     * Parses message definition document
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
                if ("msg".equals(nodeName)) {
                    parseMsgDefinition(child);
                }
            }
        }
    }

    /**
     * Parses message definition node
     *
     * @param element element to parse
     * @throws SAXException
     */
    private void parseMsgDefinition(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.isEmpty()) {
            throw new SAXException("undefined message definition name");
        }
        Direction direction = Direction.get(element.getAttribute("direction"));
        if (direction == null) {
            throw new SAXException("invalid direction for message definition " + name);
        }
        int length = getAttributeAsInteger(element, "length", 0);
        if (length == 0) {
            throw new SAXException("undefined length for message definition " + name);
        }
        int headerLength = 0;
        int offset = 0;
        byte[] data = new byte[length];
        Map<String, Field> fields = new LinkedHashMap<>();

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if (!"header".equals(nodeName)) {
                    // Increment the offset by the field data type length
                    offset += parseField(child, offset, data, fields);
                } else if (offset == 0) {
                    headerLength = parseHeader(child, data, fields);
                    // Set the offset to the header length
                    offset = headerLength;
                }
            }
        }
        if (headerLength == 0) {
            throw new SAXException("undefined header for message definition " + name);
        }
        if (offset != length) {
            throw new SAXException("actual msg length " + offset + " differs from given length " + length);
        }

        MsgDefinition definition = new MsgDefinition(data, headerLength, direction, fields);
        if (definitions.put(name, definition) != null) {
            logger.warn("duplicate message definition {}", name);
        }
    }

    /**
     * Parses header node
     *
     * @param element element to parse
     * @param data msg data to update
     * @param fields fields map to update
     * @return header length
     * @throws SAXException
     */
    private int parseHeader(Element element, byte[] data, Map<String, Field> fields) throws SAXException {
        int length = getAttributeAsInteger(element, "length", 0);
        if (length == 0) {
            throw new SAXException("undefined header length");
        }
        int offset = 0;

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                // Increment the offset by the field data type length
                offset += parseField(child, offset, data, fields);
            }
        }
        if (offset != length) {
            throw new SAXException("actual header length " + offset + " differs from given length " + length);
        }
        return length;
    }

    /**
     * Parses field node
     *
     * @param element element to parse
     * @param offset msg offset
     * @param data msg data to update
     * @param fields fields map to update
     * @return field data type length
     * @throws SAXException
     */
    private int parseField(Element element, int offset, byte[] data, Map<String, Field> fields) throws SAXException {
        String name = element.getAttribute("name");
        DataType dataType = DataType.get(element.getNodeName());
        if (dataType == null) {
            throw new SAXException("invalid field data type");
        }
        Field field = new Field(name, offset, dataType);
        try {
            field.set(data, element.getTextContent());
        } catch (FieldException | IllegalArgumentException e) {
            throw new SAXException("failed to set field data:", e);
        }
        if (!name.isEmpty()) {
            fields.put(name, field);
        }
        return dataType.getSize();
    }

    /**
     * Singleton instance function
     *
     * @return MsgDefinitionRegistry singleton reference
     */
    public static synchronized MsgDefinitionRegistry getInstance() {
        if (MSG_DEFINITION_REGISTRY.getDefinitions().isEmpty()) {
            MSG_DEFINITION_REGISTRY.initialize();
        }
        return MSG_DEFINITION_REGISTRY;
    }
}
