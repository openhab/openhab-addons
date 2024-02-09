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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.openhab.binding.insteon.internal.utils.Utils.ParsingException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains an Insteon Message consisting of the raw data, and the message definition.
 * For more info, see the public Insteon Developer's Guide, 2nd edition,
 * and the Insteon Modem Developer's Guide.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class Msg {
    private static final Logger logger = LoggerFactory.getLogger(Msg.class);

    /**
     * Represents the direction of the message from the host's view.
     * The host is the machine to which the modem is attached.
     */
    public enum Direction {
        TO_MODEM("TO_MODEM"),
        FROM_MODEM("FROM_MODEM");

        private static Map<String, Direction> map = new HashMap<>();

        private String directionString;

        static {
            map.put(TO_MODEM.getDirectionString(), TO_MODEM);
            map.put(FROM_MODEM.getDirectionString(), FROM_MODEM);
        }

        Direction(String dirString) {
            this.directionString = dirString;
        }

        public String getDirectionString() {
            return directionString;
        }

        public static Direction getDirectionFromString(String dir) {
            Direction direction = map.get(dir);
            if (direction != null) {
                return direction;
            } else {
                throw new IllegalArgumentException("Unable to find direction for " + dir);
            }
        }
    }

    // has the structure of all known messages
    private static final Map<String, Msg> MSG_MAP = new HashMap<>();
    // maps between command number and the length of the header
    private static final Map<Integer, Integer> HEADER_MAP = new HashMap<>();
    // has templates for all message from modem to host
    private static final Map<Integer, Msg> REPLY_MAP = new HashMap<>();

    private int headerLength = -1;
    private byte[] data;
    private MsgDefinition definition = new MsgDefinition();
    private Direction direction = Direction.TO_MODEM;
    private long quietTime = 0;

    /**
     * Constructor
     *
     * @param headerLength length of message header (in bytes)
     * @param data byte array with message
     * @param dataLength length of byte array data (in bytes)
     * @param dir direction of the message (from/to modem)
     */
    public Msg(int headerLength, byte[] data, int dataLength, Direction dir) {
        this.headerLength = headerLength;
        this.direction = dir;
        this.data = new byte[dataLength];
        System.arraycopy(data, 0, this.data, 0, dataLength);
    }

    /**
     * Copy constructor, needed to make a copy of the templates when
     * generating messages from them.
     *
     * @param m the message to make a copy of
     */
    public Msg(Msg m) {
        headerLength = m.headerLength;
        data = m.data.clone();
        // the message definition usually doesn't change, but just to be sure...
        definition = new MsgDefinition(m.definition);
        direction = m.direction;
    }

    static {
        // Use xml msg loader to load configs
        try {
            InputStream stream = FrameworkUtil.getBundle(Msg.class).getResource("/msg_definitions.xml").openStream();
            if (stream != null) {
                Map<String, Msg> msgs = XMLMessageReader.readMessageDefinitions(stream);
                MSG_MAP.putAll(msgs);
            } else {
                logger.warn("could not get message definition resource!");
            }
        } catch (IOException e) {
            logger.warn("i/o error parsing xml insteon message definitions", e);
        } catch (ParsingException e) {
            logger.warn("parse error parsing xml insteon message definitions", e);
        } catch (FieldException e) {
            logger.warn("got field exception while parsing xml insteon message definitions", e);
        }
        buildHeaderMap();
        buildLengthMap();
    }

    //
    // ------------------ simple getters and setters -----------------
    //

    /**
     * Experience has shown that if Insteon messages are sent in close succession,
     * only the first one will make it. The quiet time parameter says how long to
     * wait after a message before the next one can be sent.
     *
     * @return the time (in milliseconds) to pause after message has been sent
     */
    public long getQuietTime() {
        return quietTime;
    }

    public byte @Nullable [] getData() {
        return data;
    }

    public int getLength() {
        return data.length;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public Direction getDirection() {
        return direction;
    }

    public MsgDefinition getDefinition() {
        return definition;
    }

    public byte getCommandNumber() {
        return data.length < 2 ? -1 : data[1];
    }

    public boolean isPureNack() {
        return data.length == 2 && data[1] == 0x15;
    }

    public boolean isExtended() {
        if (getLength() < 2) {
            return false;
        }
        if (!definition.containsField("messageFlags")) {
            return (false);
        }
        try {
            byte flags = getByte("messageFlags");
            return ((flags & 0x10) == 0x10);
        } catch (FieldException e) {
            // do nothing
        }
        return false;
    }

    public boolean isUnsolicited() {
        // if the message has an ACK/NACK, it is in response to our message,
        // otherwise it is out-of-band, i.e. unsolicited
        return !definition.containsField("ACK/NACK");
    }

    public boolean isEcho() {
        return isPureNack() || !isUnsolicited();
    }

    public boolean isOfType(MsgType mt) {
        try {
            MsgType t = MsgType.fromValue(getByte("messageFlags"));
            return (t == mt);
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isBroadcast() {
        return isOfType(MsgType.ALL_LINK_BROADCAST) || isOfType(MsgType.BROADCAST);
    }

    public boolean isCleanup() {
        return isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isAllLink() {
        return isOfType(MsgType.ALL_LINK_BROADCAST) || isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isAckOfDirect() {
        return isOfType(MsgType.ACK_OF_DIRECT);
    }

    public boolean isAllLinkCleanupAckOrNack() {
        return isOfType(MsgType.ALL_LINK_CLEANUP_ACK) || isOfType(MsgType.ALL_LINK_CLEANUP_NACK);
    }

    public boolean isX10() {
        try {
            int cmd = getByte("Cmd") & 0xff;
            if (cmd == 0x63 || cmd == 0x52) {
                return true;
            }
        } catch (FieldException e) {
        }
        return false;
    }

    public void setDefinition(MsgDefinition d) {
        definition = d;
    }

    public void setQuietTime(long t) {
        quietTime = t;
    }

    public void addField(Field f) {
        definition.addField(f);
    }

    public @Nullable InsteonAddress getAddr(String name) {
        @Nullable
        InsteonAddress a = null;
        try {
            a = definition.getField(name).getAddress(data);
        } catch (FieldException e) {
            // do nothing, we'll return null
        }
        return a;
    }

    public int getHopsLeft() throws FieldException {
        int hops = (getByte("messageFlags") & 0x0c) >> 2;
        return hops;
    }

    /**
     * Will put a byte at the specified key
     *
     * @param key the string key in the message definition
     * @param value the byte to put
     */
    public void setByte(@Nullable String key, byte value) throws FieldException {
        Field f = definition.getField(key);
        f.setByte(data, value);
    }

    /**
     * Will put an int at the specified field key
     *
     * @param key the name of the field
     * @param value the int to put
     */
    public void setInt(String key, int value) throws FieldException {
        Field f = definition.getField(key);
        f.setInt(data, value);
    }

    /**
     * Will put address bytes at the field
     *
     * @param key the name of the field
     * @param adr the address to put
     */
    public void setAddress(String key, InsteonAddress adr) throws FieldException {
        Field f = definition.getField(key);
        f.setAddress(data, adr);
    }

    /**
     * Will fetch a byte
     *
     * @param key the name of the field
     * @return the byte
     */
    public byte getByte(String key) throws FieldException {
        return (definition.getField(key).getByte(data));
    }

    /**
     * Will fetch a byte array starting at a certain field
     *
     * @param key the name of the first field
     * @param numBytes of bytes to get
     * @return the byte array
     */
    public byte[] getBytes(String key, int numBytes) throws FieldException {
        int offset = definition.getField(key).getOffset();
        if (offset < 0 || offset + numBytes > data.length) {
            throw new FieldException("data index out of bounds!");
        }
        byte[] section = new byte[numBytes];
        byte[] data = this.data;
        System.arraycopy(data, offset, section, 0, numBytes);
        return section;
    }

    /**
     * Will fetch address from field
     *
     * @param field the filed name to fetch
     * @return the address
     */
    public InsteonAddress getAddress(String field) throws FieldException {
        return (definition.getField(field).getAddress(data));
    }

    /**
     * Fetch 3-byte (24bit) from message
     *
     * @param key1 the key of the msb
     * @param key2 the key of the second msb
     * @param key3 the key of the lsb
     * @return the integer
     */
    public int getInt24(String key1, String key2, String key3) throws FieldException {
        int i = (definition.getField(key1).getByte(data) << 16) & (definition.getField(key2).getByte(data) << 8)
                & definition.getField(key3).getByte(data);
        return i;
    }

    public String toHexString() {
        return Utils.getHexString(data);
    }

    /**
     * Sets the userData fields from a byte array
     *
     * @param arg
     */
    public void setUserData(byte[] arg) {
        byte[] data = Arrays.copyOf(arg, 14); // appends zeros if short
        try {
            setByte("userData1", data[0]);
            setByte("userData2", data[1]);
            setByte("userData3", data[2]);
            setByte("userData4", data[3]);
            setByte("userData5", data[4]);
            setByte("userData6", data[5]);
            setByte("userData7", data[6]);
            setByte("userData8", data[7]);
            setByte("userData9", data[8]);
            setByte("userData10", data[9]);
            setByte("userData11", data[10]);
            setByte("userData12", data[11]);
            setByte("userData13", data[12]);
            setByte("userData14", data[13]);
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", e.getMessage());
        }
    }

    /**
     * Calculate and set the CRC with the older 1-byte method
     *
     * @return the calculated crc
     */
    public int setCRC() {
        int crc;
        try {
            crc = getByte("command1") + getByte("command2");
            byte[] bytes = getBytes("userData1", 13); // skip userData14!
            for (byte b : bytes) {
                crc += b;
            }
            crc = ((~crc) + 1) & 0xFF;
            setByte("userData14", (byte) (crc & 0xFF));
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", this, e);
            crc = 0;
        }
        return crc;
    }

    /**
     * Calculate and set the CRC with the newer 2-byte method
     *
     * @return the calculated crc
     */
    public int setCRC2() {
        int crc = 0;
        try {
            byte[] bytes = getBytes("command1", 14);
            for (int loop = 0; loop < bytes.length; loop++) {
                int b = bytes[loop] & 0xFF;
                for (int bit = 0; bit < 8; bit++) {
                    int fb = b & 0x01;
                    if ((crc & 0x8000) == 0) {
                        fb = fb ^ 0x01;
                    }
                    if ((crc & 0x4000) == 0) {
                        fb = fb ^ 0x01;
                    }
                    if ((crc & 0x1000) == 0) {
                        fb = fb ^ 0x01;
                    }
                    if ((crc & 0x0008) == 0) {
                        fb = fb ^ 0x01;
                    }
                    crc = ((crc << 1) | fb) & 0xFFFF;
                    b = b >> 1;
                }
            }
            setByte("userData13", (byte) ((crc >> 8) & 0xFF));
            setByte("userData14", (byte) (crc & 0xFF));
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", this, e);
            crc = 0;
        }
        return crc;
    }

    @Override
    public String toString() {
        String s = (direction == Direction.TO_MODEM) ? "OUT:" : "IN:";
        // need to first sort the fields by offset
        Comparator<Field> cmp = new Comparator<>() {
            @Override
            public int compare(Field f1, Field f2) {
                return f1.getOffset() - f2.getOffset();
            }
        };
        TreeSet<Field> fields = new TreeSet<>(cmp);
        for (Field f : definition.getFields().values()) {
            fields.add(f);
        }
        for (Field f : fields) {
            if (f.getName().equals("messageFlags")) {
                byte b;
                try {
                    b = f.getByte(data);
                    MsgType t = MsgType.fromValue(b);
                    s += f.toString(data) + "=" + t.toString() + ":" + (b & 0x03) + ":" + ((b & 0x0c) >> 2) + "|";
                } catch (FieldException e) {
                    logger.warn("toString error: ", e);
                } catch (IllegalArgumentException e) {
                    logger.warn("toString msg type error: ", e);
                }
            } else {
                s += f.toString(data) + "|";
            }
        }
        return s;
    }

    /**
     * Factory method to create Msg from raw byte stream received from the
     * serial port.
     *
     * @param buf the raw received bytes
     * @param msgLen length of received buffer
     * @param isExtended whether it is an extended message or not
     * @return message, or null if the Msg cannot be created
     */
    public static @Nullable Msg createMessage(byte[] buf, int msgLen, boolean isExtended) {
        if (buf.length < 2) {
            return null;
        }
        Msg template = REPLY_MAP.get(cmdToKey(buf[1], isExtended));
        if (template == null) {
            return null; // cannot find lookup map
        }
        if (msgLen != template.getLength()) {
            logger.warn("expected msg {} len {}, got {}", template.getCommandNumber(), template.getLength(), msgLen);
            return null;
        }
        Msg msg = new Msg(template.getHeaderLength(), buf, msgLen, Direction.FROM_MODEM);
        msg.setDefinition(template.getDefinition());
        return (msg);
    }

    /**
     * Finds the header length from the insteon command in the received message
     *
     * @param cmd the insteon command received in the message
     * @return the length of the header to expect
     */
    public static int getHeaderLength(byte cmd) {
        Integer len = HEADER_MAP.get((int) cmd);
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
    public static int getMessageLength(byte b, boolean isExtended) {
        int key = cmdToKey(b, isExtended);
        Msg msg = REPLY_MAP.get(key);
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
    public static boolean isExtended(byte[] buf, int len, int headerLength) {
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

    /**
     * Creates Insteon message (for sending) of a given type
     *
     * @param type the type of message to create, as defined in the xml file
     * @return reference to message created
     * @throws InvalidMessageTypeException if there is no such message type known
     */
    public static Msg makeMessage(String type) throws InvalidMessageTypeException {
        Msg m = MSG_MAP.get(type);
        if (m == null) {
            throw new InvalidMessageTypeException("unknown message type: " + type);
        }
        return new Msg(m);
    }

    private static int cmdToKey(byte cmd, boolean isExtended) {
        return (cmd + (isExtended ? 256 : 0));
    }

    private static void buildHeaderMap() {
        for (Msg m : MSG_MAP.values()) {
            if (m.getDirection() == Direction.FROM_MODEM) {
                HEADER_MAP.put((int) m.getCommandNumber(), m.getHeaderLength());
            }
        }
    }

    private static void buildLengthMap() {
        for (Msg m : MSG_MAP.values()) {
            if (m.getDirection() == Direction.FROM_MODEM) {
                int key = cmdToKey(m.getCommandNumber(), m.isExtended());
                REPLY_MAP.put(key, m);
            }
        }
    }
}
