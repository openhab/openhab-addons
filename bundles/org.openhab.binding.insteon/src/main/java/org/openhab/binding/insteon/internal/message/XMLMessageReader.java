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
package org.openhab.binding.insteon.internal.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.utils.Pair;
import org.openhab.binding.insteon.internal.utils.Utils.DataTypeParser;
import org.openhab.binding.insteon.internal.utils.Utils.ParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the Msg definitions from an XML file
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class XMLMessageReader {
    /**
     * Reads the message definitions from an xml file
     *
     * @param input input stream from which to read
     * @return what was read from file: the map between clear text string and Msg objects
     * @throws IOException couldn't read file etc
     * @throws ParsingException something wrong with the file format
     * @throws FieldException something wrong with the field definition
     */
    public static Map<String, Msg> readMessageDefinitions(InputStream input)
            throws IOException, ParsingException, FieldException {
        Map<String, Msg> messageMap = new HashMap<>();
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

            Node root = doc.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if ("msg".equals(node.getNodeName())) {
                        Pair<String, Msg> msgDef = readMessageDefinition((Element) node);
                        messageMap.put(msgDef.getKey(), msgDef.getValue());
                    }
                }
            }
        } catch (SAXException e) {
            throw new ParsingException("Failed to parse XML!", e);
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Got parser config exception! ", e);
        }
        return messageMap;
    }

    private static Pair<String, Msg> readMessageDefinition(Element msg) throws FieldException, ParsingException {
        int length = 0;
        int hlength = 0;
        LinkedHashMap<Field, Object> fieldMap = new LinkedHashMap<>();
        String dir = msg.getAttribute("direction");
        String name = msg.getAttribute("name");
        Msg.Direction direction = Msg.Direction.getDirectionFromString(dir);

        if (msg.hasAttribute("length")) {
            length = Integer.parseInt(msg.getAttribute("length"));
        }

        NodeList nodes = msg.getChildNodes();

        int offset = 0;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("header".equals(node.getNodeName())) {
                    int o = readHeaderElement((Element) node, fieldMap);
                    hlength = o;
                    // Increment the offset by the header length
                    offset += o;
                } else {
                    Pair<Field, Object> field = readField((Element) node, offset);
                    fieldMap.put(field.getKey(), field.getValue());
                    // Increment the offset
                    offset += field.getKey().getType().getSize();
                }
            }
        }
        if (offset != length) {
            throw new ParsingException(
                    "Actual msg length " + offset + " differs from given msg length " + length + "!");
        }
        if (length == 0) {
            length = offset;
        }

        return new Pair<>(name, createMsg(fieldMap, length, hlength, direction));
    }

    private static int readHeaderElement(Element header, LinkedHashMap<Field, Object> fields) throws ParsingException {
        int offset = 0;
        int headerLen = Integer.parseInt(header.getAttribute("length"));

        NodeList nodes = header.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Pair<Field, Object> definition = readField((Element) node, offset);
                offset += definition.getKey().getType().getSize();
                fields.put(definition.getKey(), definition.getValue());
            }
        }
        if (headerLen != offset) {
            throw new ParsingException(
                    "Actual header length " + offset + " differs from given length " + headerLen + "!");
        }
        return headerLen;
    }

    private static Pair<Field, Object> readField(Element field, int offset) {
        DataType dType = DataType.getDataType(field.getTagName());
        // Will return blank if no name attribute
        String name = field.getAttribute("name");
        Field f = new Field(name, dType, offset);
        // Now we have field, only need value
        String sVal = field.getTextContent();
        Object val = DataTypeParser.parseDataType(dType, sVal);
        return new Pair<>(f, val);
    }

    private static Msg createMsg(HashMap<Field, Object> values, int length, int headerLength, Msg.Direction dir)
            throws FieldException {
        Msg msg = new Msg(headerLength, new byte[length], length, dir);
        for (Entry<Field, Object> e : values.entrySet()) {
            Field f = e.getKey();
            byte[] data = msg.getData();
            if (data != null) {
                f.set(data, e.getValue());
            } else {
                throw new FieldException("data is null");
            }
            if (!"".equals(f.getName())) {
                msg.addField(f);
            }
        }
        return msg;
    }
}
