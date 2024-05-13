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

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon2.internal.device.DeviceAddress;
import org.openhab.binding.insteon2.internal.device.InsteonAddress;
import org.openhab.binding.insteon2.internal.device.X10Address;
import org.openhab.binding.insteon2.internal.device.X10Flag;
import org.openhab.binding.insteon2.internal.utils.BinaryUtils;
import org.openhab.binding.insteon2.internal.utils.HexUtils;
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
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class Msg {

    public static enum Direction {
        TO_MODEM,
        FROM_MODEM
    }

    private final Logger logger = LoggerFactory.getLogger(Msg.class);

    private byte[] data;
    private int headerLength;
    private Direction direction;
    private MsgDefinition definition = new MsgDefinition();
    private long quietTime = 0;
    private boolean replayed = false;
    private long timestamp = System.currentTimeMillis();

    public Msg(int headerLength, int dataLength, Direction direction) {
        this.data = new byte[dataLength];
        this.headerLength = headerLength;
        this.direction = direction;
    }

    public Msg(Msg msg, byte[] data, int dataLength) {
        this.data = Arrays.copyOf(data, dataLength);
        this.headerLength = msg.headerLength;
        this.direction = msg.direction;
        // the message definition usually doesn't change, but just to be sure...
        this.definition = new MsgDefinition(msg.definition);
    }

    public Msg(Msg msg) {
        this(msg, msg.data, msg.data.length);
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

    public long getQuietTime() {
        return quietTime;
    }

    public byte getCommand() {
        try {
            return getByte("Cmd");
        } catch (FieldException e) {
            return (byte) 0xFF;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPureNack() {
        return data.length == 2 && data[1] == 0x15;
    }

    public boolean isExtended() {
        try {
            return BinaryUtils.isBitSet(getInt("messageFlags"), 4);
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isFromAddress(@Nullable InsteonAddress address) {
        try {
            return getInsteonAddress("fromAddress").equals(address);
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

    public boolean isReplyOf(Msg msg) {
        return isReply() && Arrays.equals(msg.getData(), Arrays.copyOf(data, msg.getLength()));
    }

    public boolean isFailureReport() {
        return getCommand() == 0x5C;
    }

    public boolean isOfType(MsgType type) {
        return type == getType();
    }

    public boolean isBroadcast() {
        return isOfType(MsgType.BROADCAST);
    }

    public boolean isAllLinkBroadcast() {
        return isOfType(MsgType.ALL_LINK_BROADCAST);
    }

    public boolean isAllLinkCleanup() {
        return isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isAllLinkBroadcastOrCleanup() {
        return isOfType(MsgType.ALL_LINK_BROADCAST) || isOfType(MsgType.ALL_LINK_CLEANUP);
    }

    public boolean isAllLinkCleanupAckOrNack() {
        return isOfType(MsgType.ALL_LINK_CLEANUP_ACK) || isOfType(MsgType.ALL_LINK_CLEANUP_NACK);
    }

    public boolean isAllLinkSuccessReport() {
        try {
            return isOfType(MsgType.ALL_LINK_BROADCAST) && getByte("command1") == 0x06;
        } catch (FieldException e) {
            return false;
        }
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

    public boolean isInsteon() {
        return containsField("messageFlags");
    }

    public boolean isX10() {
        return containsField("X10Flag");
    }

    public boolean isX10Address() {
        try {
            return getByte("X10Flag") == X10Flag.ADDRESS.code();
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isX10Command() {
        try {
            return getByte("X10Flag") == X10Flag.COMMAND.code();
        } catch (FieldException e) {
            return false;
        }
    }

    public boolean isReplayed() {
        return replayed;
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
     * Sets a byte at a specific field
     *
     * @param key the string key in the message definition
     * @param value the byte to put
     */
    public void setByte(String key, byte value) throws FieldException {
        Field field = definition.getField(key);
        field.setByte(data, value);
    }

    /**
     * Sets address bytes at a specific field
     *
     * @param key the name of the field
     * @param address the address to put
     */
    public void setAddress(String key, DeviceAddress address) throws FieldException {
        Field field = definition.getField(key);
        if (address instanceof InsteonAddress insteonAddress) {
            field.setAddress(data, insteonAddress);
        } else if (address instanceof X10Address x10Address) {
            field.setByte(data, x10Address.getCode());
        }
    }

    /**
     * Sets a byte array starting at a specific field
     *
     * @param key the name of the first field
     */
    public void setBytes(String key, byte[] bytes) throws FieldException {
        int offset = definition.getField(key).getOffset();
        if (offset < 0 || offset + bytes.length > data.length) {
            throw new FieldException("data index out of bounds!");
        }
        System.arraycopy(bytes, 0, data, offset, bytes.length);
    }

    /**
     * Sets a byte array starting at a specific field as an up to 32-bit integer
     *
     * @param key the name of the first field
     * @param value the integer to put
     * @param numBytes number of bytes to put
     */
    public void setInt(String key, int value, int numBytes) throws FieldException {
        if (numBytes < 1 || numBytes > 4) {
            throw new FieldException("number of bytes out of bounds!");
        }
        byte[] bytes = new byte[numBytes];
        int shift = 8 * (numBytes - 1);
        for (int i = 0; i < numBytes; i++) {
            bytes[i] = (byte) (value >> shift);
            shift -= 8;
        }
        setBytes(key, bytes);
    }

    /**
     * Returns a byte from a specific field
     *
     * @param key the name of the field
     * @return the byte
     */
    public byte getByte(String key) throws FieldException {
        return definition.getField(key).getByte(data);
    }

    /**
     * Returns the insteon address from a specific field
     *
     * @param key the name of the field
     * @return the insteon address
     */
    public InsteonAddress getInsteonAddress(String key) throws FieldException {
        return definition.getField(key).getAddress(data);
    }

    /**
     * Returns the x10 address
     *
     * @return the x10 address
     */
    public @Nullable X10Address getX10Address() throws FieldException {
        if (isX10Address()) {
            return new X10Address(getByte("rawX10"));
        }
        return null;
    }

    /**
     * Returns a byte array starting from a specific field
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
        return Arrays.copyOfRange(data, offset, offset + numBytes);
    }

    /**
     * Returns a byte array starting from a specific field as an up to 32-bit integer
     *
     * @param key the name of the first field
     * @param numBytes number of bytes to use for conversion
     * @return the integer
     */
    public int getInt(String key, int numBytes) throws FieldException {
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
     * Returns a byte from a specific field as a 8-bit integer
     *
     * @param key the name of the field
     * @return the integer
     */
    public int getInt(String key) throws FieldException {
        return getByte(key) & 0xFF;
    }

    /**
     * Returns a 2-byte array starting from a specific field as a 16-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt16(String key) throws FieldException {
        return getInt(key, 2);
    }

    /**
     * Returns a 3-byte array starting from a specific field as a 24-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt24(String key) throws FieldException {
        return getInt(key, 3);
    }

    /**
     * Returns a 4-byte array starting from a specific field as a 32-bit integer
     *
     * @param key the name of the first field
     * @return the integer
     */
    public int getInt32(String key) throws FieldException {
        return getInt(key, 4);
    }

    /**
     * Returns a byte as a hex string
     *
     * @param key the name of the field
     * @return the hex string
     */
    public String getHexString(String key) throws FieldException {
        return HexUtils.getHexString(getByte(key));
    }

    /**
     * Returns a byte array starting from a certain field as a hex string
     *
     * @param key the name of the field
     * @param numBytes number of bytes to get
     * @return the hex string
     */
    public String getHexString(String key, int numBytes) throws FieldException {
        return HexUtils.getHexString(getBytes(key, numBytes), numBytes);
    }

    /**
     * Returns group based on specific message characteristics
     *
     * @return group number if available, otherwise -1
     */
    public int getGroup() {
        try {
            if (isAllLinkBroadcast()) {
                return getInsteonAddress("toAddress").getLowByte() & 0xFF;
            }
            if (isAllLinkCleanup()) {
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
            return MsgType.valueOf(getInt("messageFlags"));
        } catch (FieldException | IllegalArgumentException e) {
            return MsgType.INVALID;
        }
    }

    /**
     * Sets the userData fields from a byte array
     *
     * @param args list of user data arguments
     */
    public void setUserData(byte[] args) {
        try {
            for (int i = 0; i < 14; i++) {
                setByte("userData" + (i + 1), args.length > i ? args[i] : (byte) 0x00);
            }
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
        return Optional
                .ofNullable(MsgDefinitionRegistry.getInstance().getTemplate(buf[1], isExtended, Direction.FROM_MODEM))
                .filter(template -> template.getLength() == msgLen).map(template -> new Msg(template, buf, msgLen))
                .orElse(null);
    }

    /**
     * Factory method to determine the header length of a received message
     *
     * @param cmd the message command received
     * @return the length of the header to expect
     */
    public static int getHeaderLength(byte cmd) {
        return Optional.ofNullable(MsgDefinitionRegistry.getInstance().getTemplate(cmd, Direction.FROM_MODEM))
                .map(Msg::getHeaderLength).orElse(-1);
    }

    /**
     * Factory method to determine the length of a received message
     *
     * @param cmd the message command received
     * @param isExtended if is an extended message
     * @return message length, or -1 if length cannot be determined
     */
    public static int getMessageLength(byte cmd, boolean isExtended) {
        return Optional
                .ofNullable(MsgDefinitionRegistry.getInstance().getTemplate(cmd, isExtended, Direction.FROM_MODEM))
                .map(Msg::getLength).orElse(-1);
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
        boolean isExtended = BinaryUtils.isBitSet(flags & 0xFF, 4);
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
        return Optional.ofNullable(MsgDefinitionRegistry.getInstance().getTemplate(cmd, Direction.TO_MODEM))
                .map(Msg::new).orElseThrow(() -> new InvalidMessageTypeException(
                        "unknown message command: " + HexUtils.getHexString(cmd)));
    }

    /**
     * Factory method to create an Insteon message to send for a given type
     *
     * @param type the message type to create, as defined in the xml file
     * @return the insteon message
     * @throws InvalidMessageTypeException
     */
    public static Msg makeMessage(String type) throws InvalidMessageTypeException {
        return Optional.ofNullable(MsgDefinitionRegistry.getInstance().getTemplate(type)).map(Msg::new)
                .orElseThrow(() -> new InvalidMessageTypeException("unknown message type: " + type));
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
     * Factory method to create an X10 address message to send
     *
     * @param address the X10 address
     * @return the X10 address message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeX10AddressMessage(X10Address address) throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendX10Message");
        msg.setByte("rawX10", address.getCode());
        msg.setByte("X10Flag", X10Flag.ADDRESS.code());
        msg.setQuietTime(300L);
        return msg;
    }

    /**
     * Factory method to create an X10 command message to send
     *
     * @param cmd the X10 command
     * @return the X10 command message
     * @throws FieldException
     * @throws InvalidMessageTypeException
     */
    public static Msg makeX10CommandMessage(byte cmd) throws FieldException, InvalidMessageTypeException {
        Msg msg = Msg.makeMessage("SendX10Message");
        msg.setByte("rawX10", cmd);
        msg.setByte("X10Flag", X10Flag.COMMAND.code());
        msg.setQuietTime(300L);
        return msg;
    }
}
