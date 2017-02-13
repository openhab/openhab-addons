/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openhab.binding.insteonplm.internal.message.Message.Direction;
import org.openhab.binding.insteonplm.internal.utils.Pair;
import org.openhab.binding.insteonplm.internal.utils.Utils.DataTypeParser;
import org.openhab.binding.insteonplm.internal.utils.Utils.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the Msg definitions from an XML file
 *
 * @author Daniel Pfrommer
 * @since 1.5.0
 */
public class XMLMessageReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLMessageReader.class);
    // has the structure of all known messages
    private final HashMap<String, Message> messageMap;
    // maps between command number and the length of the header
    private final HashMap<Integer, Integer> headerMap = new HashMap<Integer, Integer>();
    // has templates for all message from modem to host
    private final HashMap<Integer, Message> replyMap = new HashMap<Integer, Message>();

    /**
     * Reads the message definitions from an xml file
     *
     * @param input input stream from which to read
     * @return what was read from file: the map between clear text string and Msg objects
     * @throws IOException couldn't read file etc
     * @throws ParsingException something wrong with the file format
     * @throws FieldException something wrong with the field definition
     */
    public XMLMessageReader(InputStream input) throws IOException, ParsingException, FieldException {
        messageMap = new HashMap<String, Message>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse it!
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            Node root = doc.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().equals("msg")) {
                        Pair<String, Message> msgDef = readMessageDefinition((Element) node);
                        messageMap.put(msgDef.getKey(), msgDef.getValue());
                    }
                }
            }
        } catch (SAXException e) {
            throw new ParsingException("Failed to parse XML!", e);
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Got parser config exception! ", e);
        }
        buildHeaderMap();
        buildLengthMap();
    }

    /**
     * Lookup the message based on the id.
     */
    public Message getMessage(String id) {
        return messageMap.get(id);
    }

    private Pair<String, Message> readMessageDefinition(Element msg) throws FieldException, ParsingException {
        int length = 0;
        int hlength = 0;
        LinkedHashMap<Field, Object> fieldMap = new LinkedHashMap<Field, Object>();
        String dir = msg.getAttribute("direction");
        String name = msg.getAttribute("name");
        Message.Direction direction = Message.Direction.s_getDirectionFromString(dir);

        if (msg.hasAttribute("length")) {
            length = Integer.parseInt(msg.getAttribute("length"));
        }

        NodeList nodes = msg.getChildNodes();

        int offset = 0;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("header")) {
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

        return new Pair<String, Message>(name, createMsg(fieldMap, length, hlength, direction));
    }

    private int readHeaderElement(Element header, LinkedHashMap<Field, Object> fields) throws ParsingException {
        int offset = 0;
        int headerLen = Integer.parseInt(header.getAttribute("length"));

        NodeList nodes = header.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Pair<Field, Object> definition = readField((Element) node, offset);
                if (definition != null) {
                    offset += definition.getKey().getType().getSize();
                    fields.put(definition.getKey(), definition.getValue());
                }
            }
        }
        if (headerLen != offset) {
            throw new ParsingException(
                    "Actual header length " + offset + " differs from given length " + headerLen + "!");
        }
        return headerLen;
    }

    private Pair<Field, Object> readField(Element field, int offset) {
        DataType dType = DataType.s_getDataType(field.getTagName());
        // Will return blank if no name attribute
        String name = field.getAttribute("name");
        Field f = new Field(name, dType, offset);
        // Now we have field, only need value
        String sVal = field.getTextContent();
        Object val = DataTypeParser.s_parseDataType(dType, sVal);
        Pair<Field, Object> pair = new Pair<Field, Object>(f, val);
        return pair;
    }

    private Message createMsg(HashMap<Field, Object> values, int length, int headerLength, Message.Direction dir)
            throws FieldException {
        Message msg = new Message(headerLength, new byte[length], length, dir);
        for (Entry<Field, Object> e : values.entrySet()) {
            Field f = e.getKey();
            f.set(msg.getData(), e.getValue());
            if (f.getName() != null && !f.getName().equals("")) {
                msg.addField(f);
            }
        }
        return msg;
    }

    private void buildHeaderMap() {
        for (Message m : messageMap.values()) {
            if (m.getDirection() == Direction.FROM_MODEM) {
                headerMap.put(new Integer(m.getCommandNumber()), m.getHeaderLength());
            }
        }
    }

    private void buildLengthMap() {
        for (Message m : messageMap.values()) {
            if (m.getDirection() == Direction.FROM_MODEM) {
                Integer key = new Integer(cmdToKey(m.getCommandNumber(), m.isExtended()));
                replyMap.put(key, m);
            }
        }
    }

    private int cmdToKey(byte cmd, boolean isExtended) {
        return (cmd + (isExtended ? 256 : 0));
    }

    /**
     * Factory method to create Msg from raw byte stream received from the
     * serial port.
     *
     * @param m_buf the raw received bytes
     * @param msgLen length of received buffer
     * @param isExtended whether it is an extended message or not
     * @return message, or null if the Msg cannot be created
     */
    public Message createMessage(byte[] m_buf, int msgLen, boolean isExtended) {
        if (m_buf == null || m_buf.length < 2) {
            return null;
        }
        Message template = replyMap.get(cmdToKey(m_buf[1], isExtended));
        if (template == null) {
            return null; // cannot find lookup map
        }
        if (msgLen != template.getLength()) {
            logger.error("expected msg {} len {}, got {}", template.getCommandNumber(), template.getLength(), msgLen);
            return null;
        }
        Message msg = new Message(template.getHeaderLength(), m_buf, msgLen, Direction.FROM_MODEM);
        msg.setDefinition(template.getDefinition());
        return (msg);
    }

    /**
     * Finds the header length from the insteon command in the received message
     *
     * @param cmd the insteon command received in the message
     * @return the length of the header to expect
     */
    public int getHeaderLength(byte cmd) {
        Integer len = headerMap.get(new Integer(cmd));
        if (len == null) {
            return (-1); // not found
        }
        return len;
    }

    /**
     * Tries to determine the length of a received Insteon message.
     *
     * @param b Insteon message command received
     * @param isExtended flag indicating if it is an extended message
     * @return message length, or -1 if length cannot be determined
     */
    public int getMessageLength(byte b, boolean isExtended) {
        int key = cmdToKey(b, isExtended);
        Message msg = replyMap.get(key);
        if (msg == null) {
            return -1;
        }
        return msg.getLength();
    }

    /**
     * From bytes received thus far, tries to determine if an Insteon
     * message is extended or standard.
     *
     * @param buf the received bytes
     * @param len the number of bytes received so far
     * @param headerLength the known length of the header
     * @return true if it is definitely extended, false if cannot be
     *         determined or if it is a standard message
     */
    public boolean isExtended(byte[] buf, int len, int headerLength) {
        if (headerLength <= 2) {
            return false;
        } // extended messages are longer
        if (len < headerLength) {
            return false;
        } // not enough data to tell if extended
        byte flags = buf[headerLength - 1]; // last byte says flags
        boolean isExtended = (flags & 0x10) == 0x10; // bit 4 is the message
        return (isExtended);
    }

}
