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
package org.openhab.binding.insteon2.internal.transport.message;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon2.internal.device.InsteonAddress;
import org.openhab.binding.insteon2.internal.transport.message.Msg.Direction;
import org.openhab.binding.insteon2.internal.utils.HexUtils;
import org.openhab.binding.insteon2.internal.utils.ResourceLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link MsgDefinitionRegistry} represents message definition registry
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class MsgDefinitionRegistry extends ResourceLoader {
    private static final MsgDefinitionRegistry MSG_DEFINITION_REGISTRY = new MsgDefinitionRegistry();
    private static final String RESOURCE_NAME = "/msg_definitions.xml";

    private Map<String, Msg> definitions = new LinkedHashMap<>();

    /**
     * Returns message template for a given type
     *
     * @param type message type to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(String type) {
        return definitions.get(type);
    }

    /**
     * Returns message template for a given command and direction
     *
     * @param cmd message command to match
     * @param direction message direction to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(byte cmd, Direction direction) {
        return getTemplate(cmd, null, direction);
    }

    /**
     * Returns message template for a given command, extended flag and direction
     *
     * @param cmd message command to match
     * @param isExtended if message is extended
     * @param direction message direction to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(byte cmd, @Nullable Boolean isExtended, Direction direction) {
        return definitions.values().stream().filter(msg -> msg.getCommand() == cmd && msg.getDirection() == direction
                && (isExtended == null || msg.isExtended() == isExtended)).findFirst().orElse(null);
    }

    /**
     * Returns known message definitions
     *
     * @return currently known message definitions
     */
    public Map<String, Msg> getDefinitions() {
        return definitions;
    }

    /**
     * Initializes message definition registry
     */
    @Override
    protected void initialize() {
        super.initialize();

        if (logger.isDebugEnabled()) {
            logger.debug("loaded {} message definitions", definitions.size());
            if (logger.isTraceEnabled()) {
                definitions.entrySet().stream()
                        .map(definition -> String.format("%s->%s", definition.getKey(), definition.getValue()))
                        .forEach(logger::trace);
            }
        }
    }

    /**
     * Returns message definition resource name
     */
    @Override
    protected String getResourceName() {
        return RESOURCE_NAME;
    }

    /**
     * Parses message definition document
     *
     * @param element element to parse
     * @throws SAXException
     * @throws IOException
     */
    @Override
    protected void parseDocument(Element element) throws SAXException, IOException {
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
        LinkedHashMap<Field, Object> fields = new LinkedHashMap<>();
        String name = element.getAttribute("name");
        Direction direction = Direction.valueOf(element.getAttribute("direction"));
        int length = element.hasAttribute("length") ? Integer.parseInt(element.getAttribute("length")) : 0;
        int headerLength = 0;
        int offset = 0;

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("header".equals(nodeName)) {
                    headerLength = parseHeader(child, fields);
                    // Increment the offset by the header length
                    offset += headerLength;
                } else {
                    // Increment the offset by the field data type length
                    offset += parseField(child, offset, fields);
                }
            }
        }
        if (length == 0) {
            length = offset;
        } else if (offset != length) {
            throw new SAXException("actual msg length " + offset + " differs from given msg length " + length);
        }

        try {
            Msg msg = makeMsgTemplate(fields, headerLength, length, direction);
            definitions.put(name, msg);
        } catch (FieldException e) {
            throw new SAXException("failed to create message definition " + name + ":", e);
        }
    }

    /**
     * Parses header node
     *
     * @param element element to parse
     * @param fields fields map to update
     * @return header length
     * @throws SAXException
     */
    private int parseHeader(Element element, LinkedHashMap<Field, Object> fields) throws SAXException {
        int length = Integer.parseInt(element.getAttribute("length"));
        int offset = 0;

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                // Increment the offset by the field data type length
                offset += parseField(child, offset, fields);
            }
        }
        if (length != offset) {
            throw new SAXException("actual header length " + offset + " differs from given length " + length);
        }
        return length;
    }

    /**
     * Parses field node
     *
     * @param element element to parse
     * @param offset msg offset
     * @param fields fields map to update
     * @return field data type length
     * @throws SAXException
     */
    private int parseField(Element element, int offset, LinkedHashMap<Field, Object> fields) throws SAXException {
        String name = element.getAttribute("name");
        if (name == null) {
            throw new SAXException("undefined field name");
        }
        DataType dataType = DataType.get(element.getNodeName());
        Field field = new Field(name, dataType, offset);
        Object value = getFieldValue(dataType, element.getTextContent().trim());
        fields.put(field, value);
        return dataType.getSize();
    }

    /**
     * Returns field value
     *
     * @param dataType field data type
     * @param value value to convert
     * @return field value
     * @throws SAXException
     */
    private Object getFieldValue(DataType dataType, String value) throws SAXException {
        switch (dataType) {
            case BYTE:
                return getByteValue(value);
            case ADDRESS:
                return getAddressValue(value);
            default:
                throw new SAXException("invalid field data type");
        }
    }

    /**
     * Returns field value as a byte
     *
     * @param value value to convert
     * @return byte
     * @throws SAXException
     */
    private byte getByteValue(String value) throws SAXException {
        try {
            return "".equals(value) ? 0x00 : (byte) HexUtils.toInteger(value);
        } catch (NumberFormatException e) {
            throw new SAXException("invalid field byte value: " + value);
        }
    }

    /**
     * Returns field value as an insteon address
     *
     * @param value value to convert
     * @return insteon address
     * @throws SAXException
     */
    private InsteonAddress getAddressValue(String value) throws SAXException {
        try {
            return "".equals(value) ? InsteonAddress.UNKNOWN : new InsteonAddress(value);
        } catch (IllegalArgumentException e) {
            throw new SAXException("invalid field address value: " + value);
        }
    }

    /**
     * Returns new message template
     *
     * @param fields msg fields
     * @param length msg length
     * @param headerLength header length
     * @param direction msg direction
     * @return new msg template
     * @throws FieldException
     */
    private Msg makeMsgTemplate(Map<Field, Object> fields, int headerLength, int length, Direction direction)
            throws FieldException {
        Msg msg = new Msg(headerLength, length, direction);
        for (Entry<Field, Object> entry : fields.entrySet()) {
            Field field = entry.getKey();
            byte[] data = msg.getData();
            field.set(data, entry.getValue());
            if (!"".equals(field.getName())) {
                msg.addField(field);
            }
        }
        return msg;
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
