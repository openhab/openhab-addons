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
package org.openhab.binding.insteon.internal.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.message.Msg.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the message definitions from an xml file.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class MsgDefinitionLoader {
    private static final Logger logger = LoggerFactory.getLogger(MsgDefinitionLoader.class);
    private static MsgDefinitionLoader msgDefinitionLoader = new MsgDefinitionLoader();
    private Map<String, Msg> definitions = new LinkedHashMap<>();

    /**
     * Finds message template for a given type
     *
     * @param type message type to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(String type) {
        return definitions.get(type);
    }

    /**
     * Finds message template for a given command and direction
     *
     * @param cmd message command to match
     * @param direction message direction to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(byte cmd, Direction direction) {
        return getTemplate(cmd, null, direction);
    }

    /**
     * Finds message template for a given command, extended flag and direction
     *
     * @param cmd message command to match
     * @param isExtended if message is extended
     * @param direction message direction to match
     * @return message template if found, otherwise null
     */
    public @Nullable Msg getTemplate(byte cmd, @Nullable Boolean isExtended, Direction direction) {
        return definitions.values().stream().filter(msg -> msg.getCommandNumber() == cmd
                && msg.getDirection() == direction && (isExtended == null || msg.isExtended() == isExtended))
                .findFirst().orElse(null);
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
     * Reads the device products from input stream and stores them in memory for
     * later access.
     *
     * @param stream the input stream from which to read
     * @throws FieldException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadMsgDefinitionsXML(InputStream stream)
            throws FieldException, ParserConfigurationException, SAXException, IOException {
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
                if ("msg".equals(nodeName)) {
                    parseMsgDefinition(child);
                }
            }
        }
    }

    /**
     * Reads the device products from file and stores them in memory for later access.
     *
     * @param filename the name of the file to read from
     * @throws FieldException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadMsgDefinitionsXML(String filename)
            throws FieldException, ParserConfigurationException, SAXException, IOException {
        File file = new File(filename);
        InputStream stream = new FileInputStream(file);
        loadMsgDefinitionsXML(stream);
    }

    /**
     * Parses message definition node
     *
     * @param element element to parse
     * @throws FieldException
     * @throws SAXException
     */
    private void parseMsgDefinition(Element element) throws FieldException, SAXException {
        LinkedHashMap<Field, Object> fields = new LinkedHashMap<>();
        String name = element.getAttribute("name");
        Direction direction = Direction.getDirectionFromString(element.getAttribute("direction"));
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
            throw new SAXException("Actual msg length " + offset + " differs from given msg length " + length);
        }

        Msg msg = makeMsgTemplate(fields, headerLength, length, direction);
        definitions.put(name, msg);
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
            throw new SAXException("Actual header length " + offset + " differs from given length " + length);
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
        DataType dataType = DataType.getDataType(element.getNodeName());
        Field field = new Field(name, dataType, offset);
        Object value = DataTypeParser.parseDataType(dataType, element.getTextContent());
        fields.put(field, value);
        return dataType.getSize();
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
     * Helper function for debugging
     */
    private void logDefinitions() {
        definitions.entrySet().stream()
                .map(definition -> String.format("%s->%s", definition.getKey(), definition.getValue()))
                .forEach(logger::debug);
    }

    /**
     * Singleton instance function
     *
     * @return MsgDefinitionLoader singleton reference
     */
    public static synchronized MsgDefinitionLoader instance() {
        if (msgDefinitionLoader.getDefinitions().isEmpty()) {
            InputStream input = MsgDefinitionLoader.class.getResourceAsStream("/msg_definitions.xml");
            try {
                if (input != null) {
                    msgDefinitionLoader.loadMsgDefinitionsXML(input);
                } else {
                    logger.warn("Resource stream is null, cannot read xml file.");
                }
            } catch (FieldException | ParserConfigurationException e) {
                logger.warn("parser config error when reading message definitions xml file: ", e);
            } catch (SAXException e) {
                logger.warn("SAX exception when reading message definitions xml file: ", e);
            } catch (IOException e) {
                logger.warn("I/O exception when reading message definitions xml file: ", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("loaded {} message definitions: ", msgDefinitionLoader.getDefinitions().size());
                msgDefinitionLoader.logDefinitions();
            }
        }
        return msgDefinitionLoader;
    }
}
