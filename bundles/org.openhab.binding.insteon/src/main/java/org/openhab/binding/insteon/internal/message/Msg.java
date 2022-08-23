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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.BitwiseUtils;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
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
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
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
            if (direction == null) {
                throw new IllegalArgumentException("direction " + dir + " not found");
            }
            return direction;
        }
    }

    private int headerLength = -1;
    private byte[] data;
    private MsgDefinition definition = new MsgDefinition();
    private Direction direction = Direction.TO_MODEM;
    private long quietTime = 0;
    private boolean replayed = false;
    private long timestamp = System.currentTimeMillis();

    /**
     * Constructor
     *
     * @param headerLength length of message header (in bytes)
     * @param dataLength length of byte array data (in bytes)
     * @param direction direction of the message (from/to modem)
     */
    public Msg(int headerLength, int dataLength, Direction direction) {
        this.headerLength = headerLength;
        this.direction = direction;
        this.data = new byte[dataLength];
    }

    /**
     * Copy constructor, needed to make a copy of the templates when
     * generating messages from them.
     *
     * @param m the message to make a copy of
     */
    public Msg(Msg msg) {
        this.headerLength = msg.headerLength;
        this.data = msg.data.clone();
        // the message definition usually doesn't change, but just to be sure...
        this.definition = new MsgDefinition(msg.definition);
        this.direction = msg.direction;
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

    public byte[] getData() {
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

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPureNack() {
        return data.length == 2 && data[1] == 0x15;
    }

    public boolean isExtended() {
        try {
            return BitwiseUtils.isBitFlagSet(getInt("messageFlags"), 4);
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isFromAddress(InsteonAddress address) {
        try {
            return getAddress("fromAddress").equals(address);
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isInbound() {
        return direction == Direction.FROM_MODEM;
    }

    public boolean isOutbound() {
        return direction == Direction.TO_MODEM;
    }

    public boolean isEcho() {
        return isPureNack() || isReply();
    }

    public boolean isReply() {
        return containsField("ACK/NACK");
    }

    public boolean isReplyAck() {
        try {
            return getByte("ACK/NACK") == 0x06;
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isReplyNack() {
        try {
            return getByte("ACK/NACK") == 0x15;
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isOfType(MsgType t) {
        return t == getType();
    }

    public boolean isBroadcast() {
        return isOfType(MsgType.ALL_LINK_BROADCAST) || isOfType(MsgType.BROADCAST);
    }

    public boolean isAllLinkBroadcast() {
        return isOfType(MsgType.ALL_LINK_BROADCAST);
    }

    public boolean isCleanup() {
        return isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isAllLink() {
        return isOfType(MsgType.ALL_LINK_BROADCAST) || isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isDirect() {
        return isOfType(MsgType.DIRECT);
    }

    public boolean isAckOfDirect() {
        return isOfType(MsgType.ACK_OF_DIRECT);
    }

    public boolean isNackOfDirect() {
        return isOfType(MsgType.NACK_OF_DIRECT);
    }

    public boolean isAckOrNackOfDirect() {
        return isOfType(MsgType.ACK_OF_DIRECT) || isOfType(MsgType.NACK_OF_DIRECT);
    }

    public boolean isAllLinkCleanupAckOrNack() {
        return isOfType(MsgType.ALL_LINK_CLEANUP_ACK) || isOfType(MsgType.ALL_LINK_CLEANUP_NACK);
    }

    public boolean isInsteonMessage() {
        return containsField("messageFlags");
    }

    public boolean isX10() {
        return containsField("X10Flag");
    }

    public boolean isReplayed() {
        return replayed;
    }

    public void setData(byte[] data, int dataLength) {
        this.data = new byte[dataLength];
        System.arraycopy(data, 0, this.data, 0, dataLength);
    }

    public void setDefinition(MsgDefinition definition) {
        this.definition = definition;
    }

    public void setQuietTime(long quietTime) {
        this.quietTime = quietTime;
    }

    public void setIsReplayed(boolean replayed) {
        this.replayed = replayed;
    }

    public void addField(Field f) {
        definition.addField(f);
    }

    public boolean containsField(String key) {
        return definition.containsField(key);
    }

    public int getHopsLeft() {
        try {
            return (getByte("messageFlags") & 0x0C) >> 2;
        } catch (FieldException e) {
            return -1;
        }
    }

    public int getMaxHops() {
        try {
            return getByte("messageFlags") & 0x03;
        } catch (FieldException e) {
            return -1;
        }
    }

    /**
     * Sets a byte in specific field
     *
     * @param key the string key in the message definition
     * @param value the byte to put
     */
    public void setByte(String key, byte value) throws FieldException {
        Field field = definition.getField(key);
        field.setByte(data, value);
    }

    /**
     * Sets an int in specific field
     *
     * @param key the name of the field
     * @param value the int to put
     */
    public void setInt(String key, int value) throws FieldException {
        Field field = definition.getField(key);
        field.setInt(data, value);
    }

    /**
     * Sets address bytes in specific field
     *
     * @param key the name of the field
     * @param address the address to put
     */
    public void setAddress(String key, InsteonAddress address) throws FieldException {
        Field field = definition.getField(key);
        field.setAddress(data, address);
    }

    /**
     * Returns a byte
     *
     * @param key the name of the field
     * @return the byte
     */
    public byte getByte(String key) throws FieldException {
        return definition.getField(key).getByte(data);
    }

    /**
     * Returns a byte array starting at a certain field
     *
     * @param key the name of the first field
     * @param numBytes number of bytes to get
     * @return the byte array
     */
    public byte[] getBytes(String key, int numBytes) throws FieldException {
        int offset = definition.getField(key).getOffset();
        if (offset < 0 || offset + numBytes > data.length) {
            throw new FieldException("data index out of bounds!");
        }
        byte[] section = new byte[numBytes];
        System.arraycopy(data, offset, section, 0, numBytes);
        return section;
    }

    /**
     * Returns the address from a field
     *
     * @param key the name of the field
     * @return the address
     */
    public InsteonAddress getAddress(String key) throws FieldException {
        return definition.getField(key).getAddress(data);
    }

    /**
     * Returns a byte array starting at a certain field as an up to 32-bit integer
     *
     * @param key the name of the first field
     * @param numBytes number of bytes to use for conversion
     * @return the integer
     */
    public int getBytesAsInt(String key, int numBytes) throws FieldException {
        if (numBytes < 1 || numBytes > 4) {
            throw new FieldException("number of bytes out of bounds!");
        }
        int i = 0;
        int shift = 8 * (numBytes - 1);
        for (byte b : getBytes(key, numBytes)) {
            i |= (b & 0xFF) << shift;
            shift -= 8;
        }
        return i;
    }

    /**
     * Returns a byte as a 8-bit integer
     *
     * @param key the name of the field
     * @return the integer
     */
    public int getInt(String key) throws FieldException {
        return getByte(key) & 0xFF;
    }

    /**
     * Returns a 2-byte array starting at a certain field as a 16-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt16(String key) throws FieldException {
        return getBytesAsInt(key, 2);
    }

    /**
     * Returns a 3-byte array starting at a certain field as a 24-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt24(String key) throws FieldException {
        return getBytesAsInt(key, 3);
    }

    /**
     * Returns a 4-byte array starting at a certain field as a 32-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt32(String key) throws FieldException {
        return getBytesAsInt(key, 4);
    }

    /**
     * Returns a byte as a hex string
     *
     * @param key the name of the field
     * @return the hex string
     */
    public String getHexString(String key) throws FieldException {
        return ByteUtils.getHexString(getByte(key));
    }

    /**
     * Returns a byte array starting at a certain field as a hex string
     *
     * @param key the name of the field
     * @param numBytes number of bytes to get
     * @return the hex string
     */
    public String getHexString(String key, int numBytes) throws FieldException {
        return ByteUtils.getHexString(getBytes(key, numBytes), numBytes);
    }

    /**
     * Returns the address from a field or null if not found
     *
     * @param key the name of the field
     * @return the address if available, otherwise null
     */
    public @Nullable InsteonAddress getAddressOrNull(String key) {
        try {
            return getAddress(key);
        } catch (FieldException e) {
            return null;
        }
    }

    /**
     * Returns a byte as a 8-bit integer or default value
     *
     * @param key the name of the field
     * @param def the default value to use if field not available
     * @return the integer
     */
    public int getIntOrDefault(String key, int def) {
        try {
            return getInt(key);
        } catch (FieldException e) {
            return def;
        }
    }

    /**
     * Returns group based on specific message characteristics
     *
     * @return group number if available, otherwise -1
     */
    public int getGroup() {
        try {
            if (isAllLinkBroadcast()) {
                return getAddress("toAddress").getLowByte() & 0xFF;
            }
            if (isCleanup()) {
                return getInt("command2");
            }
            if (isExtended()) {
                byte cmd1 = getByte("command1");
                byte cmd2 = getByte("command2");
                // group number for specific extended msg located in userData1 byte
                if (cmd1 == 0x2E && cmd2 == 0x00) {
                    return getInt("userData1");
                }
            }
        } catch (FieldException e) {
            logger.warn("got field exception on msg: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Returns msg type based on message flags
     *
     * @return msg type
     */
    public MsgType getType() {
        try {
            return MsgType.fromValue(getByte("messageFlags"));
        } catch (FieldException | IllegalArgumentException e) {
            return MsgType.INVALID;
        }
    }

    /**
     * Sets the userData fields from a byte array
     *
     * @param data
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
     * Calculates the CRC using the older 1-byte method
     *
     * @return the calculated crc
     * @throws FieldException
     */
    public int calculateCRC() throws FieldException {
        int crc = 0;
        byte[] bytes = getBytes("command1", 15); // skip userData14
        for (byte b : bytes) {
            crc += b;
        }
        return (~crc + 1) & 0xFF;
    }

    /**
     * Calculates the CRC using the newer 2-byte method
     *
     * @return the calculated crc
     * @throws FieldException
     */
    public int calculateCRC2() throws FieldException {
        int crc = 0;
        byte[] bytes = getBytes("command1", 14); // skip userData13/14
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
                crc = (crc << 1) | fb;
                b = b >> 1;
            }
        }
        return crc & 0xFFFF;
    }

    /**
     * Checks if message has a valid CRC using the older 1-byte method
     *
     * @return true if valid
     */
    public boolean hasValidCRC() {
        try {
            return getInt("userData14") == calculateCRC();
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", e.getMessage());
        }
        return false;
    }

    /**
     * Checks if message has a valid CRC using the newer 2-byte method is valid
     *
     * @return true if valid
     */
    public boolean hasValidCRC2() {
        try {
            return getInt16("userData13") == calculateCRC2();
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", e.getMessage());
        }
        return false;
    }

    /**
     * Sets the calculated CRC using the older 1-byte method
     */
    public void setCRC() {
        try {
            int crc = calculateCRC();
            setByte("userData14", (byte) crc);
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", e.getMessage());
        }
    }

    /**
     * Sets the calculated CRC using the newer 2-byte method
     */
    public void setCRC2() {
        try {
            int crc = calculateCRC2();
            setByte("userData13", (byte) ((crc >> 8) & 0xFF));
            setByte("userData14", (byte) (crc & 0xFF));
        } catch (FieldException e) {
            logger.warn("got field exception on msg {}:", e.getMessage());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Msg other = (Msg) obj;
        return Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        String s = (direction == Direction.TO_MODEM) ? "OUT:" : "IN:";
        for (Field field : definition.getFields()) {
            if ("messageFlags".equals(field.getName())) {
                s += field.toString(data) + "=" + getType() + ":" + getHopsLeft() + ":" + getMaxHops() + "|";
            } else {
                s += field.toString(data) + "|";
            }
        }
        return s;
    }

    /**
     * Factory method to create Msg from raw byte stream received from the serial port.
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
        Msg template = MsgDefinitionLoader.instance().getTemplate(buf[1], isExtended, Direction.FROM_MODEM);
        if (template == null) {
            return null;
        }
        if (msgLen != template.getLength()) {
            logger.warn("expected msg {} len {}, got {}", template.getCommandNumber(), template.getLength(), msgLen);
            return null;
        }
        Msg msg = new Msg(template);
        msg.setData(buf, msgLen);
        return msg;
    }

    /**
     * Factory method to determine the header length of a received message
     *
     * @param cmd the message command received
     * @return the length of the header to expect
     */
    public static int getHeaderLength(byte cmd) {
        Msg msg = MsgDefinitionLoader.instance().getTemplate(cmd, Direction.FROM_MODEM);
        return msg != null ? msg.getHeaderLength() : -1;
    }

    /**
     * Factory method to determine the length of a received message
     *
     * @param cmd the message command received
     * @param isExtended if is an extended message
     * @return message length, or -1 if length cannot be determined
     */
    public static int getMessageLength(byte cmd, boolean isExtended) {
        Msg msg = MsgDefinitionLoader.instance().getTemplate(cmd, isExtended, Direction.FROM_MODEM);
        return msg != null ? msg.getLength() : -1;
    }

    /**
     * Factory method to determine if a message is extended
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
        boolean isExtended = BitwiseUtils.isBitFlagSet(flags & 0xFF, 4);
        return isExtended;
    }

    /**
     * Factory method to create a message to send for a given cmd
     *
     * @param cmd the message cmd to create, as defined in the xml file
     * @return the insteon message
     * @throws InvalidMessageTypeException
     */
    public static Msg makeMessage(byte cmd) throws InvalidMessageTypeException {
        Msg msg = MsgDefinitionLoader.instance().getTemplate(cmd, Direction.TO_MODEM);
        if (msg == null) {
            throw new InvalidMessageTypeException("unknown message command: " + ByteUtils.getHexString(cmd));
        }
        return new Msg(msg);
    }

    /**
     * Factory method to create an Insteon message to send for a given type
     *
     * @param type the message type to create, as defined in the xml file
     * @return the insteon message
     * @throws InvalidMessageTypeException
     */
    public static Msg makeMessage(String type) throws InvalidMessageTypeException {
        Msg msg = MsgDefinitionLoader.instance().getTemplate(type);
        if (msg == null) {
            throw new InvalidMessageTypeException("unknown message type: " + type);
        }
        return new Msg(msg);
    }

    /**
     * Factory method to create a broadcast message to send
     *
     * @param group the broadcast group to send the message to
     * @param cmd1 the message command 1 field
     * @param cmd2 the message command 2 field
     * @return the broadcast message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeBroadcastMessage(int group, byte cmd1, byte cmd2)
            throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendStandardMessage");
        msg.setAddress("toAddress", new InsteonAddress((byte) 0, (byte) 0, (byte) (group & 0xFF)));
        msg.setByte("messageFlags", (byte) 0xCF);
        msg.setByte("command1", cmd1);
        msg.setByte("command2", cmd2);
        msg.setQuietTime(0L);
        return msg;
    }

    /**
     * Factory method to create a standard message to send
     *
     * @param address the address to send the message to
     * @param cmd1 the message command 1 field
     * @param cmd2 the message command 2 field
     * @return the standard message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeStandardMessage(InsteonAddress address, byte cmd1, byte cmd2)
            throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendStandardMessage");
        msg.setAddress("toAddress", address);
        msg.setByte("messageFlags", (byte) 0x0F);
        msg.setByte("command1", cmd1);
        msg.setByte("command2", cmd2);
        // set default quiet time accounting for ack response
        msg.setQuietTime(1000L);
        return msg;
    }

    /**
     * Factory method to create an extended message to send with optional CRC
     *
     * @param address the address to send the message to
     * @param cmd1 the message command 1 field
     * @param cmd2 the message command 2 field
     * @param setCRC if the CRC should be set
     * @return extended message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeExtendedMessage(InsteonAddress address, byte cmd1, byte cmd2, boolean setCRC)
            throws FieldException, InvalidMessageTypeException {
        return makeExtendedMessage(address, cmd1, cmd2, new byte[] {}, setCRC);
    }

    /**
     * Factory method to create an extended message to send with specific user data and optional CRC
     *
     * @param address the address to send the message to
     * @param cmd1 the message command 1 field
     * @param cmd2 the message command 2 field
     * @param data the message user data fields
     * @param setCRC if the CRC should be set
     * @return extended message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeExtendedMessage(InsteonAddress address, byte cmd1, byte cmd2, byte[] data, boolean setCRC)
            throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendExtendedMessage");
        msg.setAddress("toAddress", address);
        msg.setByte("messageFlags", (byte) 0x1F);
        msg.setByte("command1", cmd1);
        msg.setByte("command2", cmd2);
        msg.setUserData(data);
        if (setCRC) {
            msg.setCRC();
        }
        // set default quiet time accounting for ack followed by direct response messages
        msg.setQuietTime(2000L);
        return msg;
    }

    /**
     * Factory method to create an extended message to send with specific user data and CRC2
     *
     * @param address the address to send the message to
     * @param cmd1 the message command 1 field
     * @param cmd2 the message command 2 field
     * @param data the message user data fields
     * @return extended message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeExtendedMessageCRC2(InsteonAddress address, byte cmd1, byte cmd2, byte[] data)
            throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeExtendedMessage(address, cmd1, cmd2, data, false);
        msg.setCRC2();
        return msg;
    }

    /**
     * Factory method to create an X10 message to send
     *
     * @param rawX10 the X10 raw field
     * @param X10Flag the X10 flag field
     * @return the X10 message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeX10Message(byte rawX10, byte X10Flag) throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendX10Message");
        msg.setByte("rawX10", rawX10);
        msg.setByte("X10Flag", X10Flag);
        msg.setQuietTime(300L);
        return msg;
    }
}
