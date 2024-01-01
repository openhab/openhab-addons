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
package org.openhab.binding.lifx.internal.dto;

import java.nio.ByteBuffer;

import org.openhab.binding.lifx.internal.fields.ByteField;
import org.openhab.binding.lifx.internal.fields.Field;
import org.openhab.binding.lifx.internal.fields.MACAddress;
import org.openhab.binding.lifx.internal.fields.MACAddressField;
import org.openhab.binding.lifx.internal.fields.UInt16Field;
import org.openhab.binding.lifx.internal.fields.UInt32Field;
import org.openhab.binding.lifx.internal.fields.UInt8Field;

/**
 * Represents an abstract packet, providing conversion functionality to and from
 * {@link ByteBuffer}s for common packet (preamble) fields. Subtypes of this
 * class can provide conversion functionality for specialized fields.
 *
 * <p>
 * Defining new packet types essentially involves extending this class,
 * defining the fields and implementing {@link #packetType()},
 * {@link #packetLength()}, and {@link #packetBytes()}. By convention, packet
 * type should be stored in a {@code public static final int PACKET_TYPE} field
 * in each subtype, followed by a listing of fields contained in the packet.
 * Field definitions should remain accessible to outside classes in the event
 * they need to worked with directly elsewhere.
 *
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
public abstract class Packet {

    public static final Field<Integer> FIELD_SIZE = new UInt16Field().little();
    public static final Field<Integer> FIELD_PROTOCOL = new UInt16Field().little();
    public static final Field<Long> FIELD_SOURCE = new UInt32Field().little();
    public static final Field<MACAddress> FIELD_TARGET = new MACAddressField();
    public static final Field<ByteBuffer> FIELD_RESERVED_1 = new ByteField(6);
    public static final Field<Integer> FIELD_ACK = new UInt8Field();
    public static final Field<Integer> FIELD_SEQUENCE = new UInt8Field().little();
    public static final Field<ByteBuffer> FIELD_RESERVED_2 = new ByteField(8);
    public static final Field<Integer> FIELD_PACKET_TYPE = new UInt16Field().little();
    public static final Field<ByteBuffer> FIELD_RESERVED_3 = new ByteField(2);

    /**
     * An ordered array of all fields contained in the common packet preamble.
     */
    public static final Field<?>[] PREAMBLE_FIELDS = new Field[] { FIELD_SIZE, FIELD_PROTOCOL, FIELD_SOURCE,
            FIELD_TARGET, FIELD_RESERVED_1, FIELD_ACK, FIELD_SEQUENCE, FIELD_RESERVED_2, FIELD_PACKET_TYPE,
            FIELD_RESERVED_3 };

    protected int size;
    protected int protocol;
    protected long source;
    protected MACAddress target;
    protected ByteBuffer reserved1;
    protected int ackbyte;
    protected int sequence;
    protected ByteBuffer reserved2;
    protected int packetType;
    protected ByteBuffer reserved3;

    protected long timeStamp;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOrigin() {
        return (protocol & 0xC000) >> 14;
    }

    public void setOrigin(int origin) {
        protocol = (protocol & ~(1 << 14)) | (origin << 14);
    }

    public boolean getTagged() {
        return (protocol & 0x2000) >> 13 == 1 ? true : false;
    }

    public void setTagged(boolean flag) {
        protocol = (protocol & ~(1 << 13)) | ((flag ? 1 : 0) << 13);
    }

    public boolean getAddressable() {
        return (protocol & 0x1000) >> 12 == 1 ? true : false;
    }

    public void setAddressable(boolean flag) {
        this.protocol = (protocol & ~(1 << 12)) | ((flag ? 1 : 0) << 12);
    }

    public int getProtocol() {
        return protocol & 0x0FFF;
    }

    public void setProtocol(int protocol) {
        this.protocol = this.protocol | protocol;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

    public MACAddress getTarget() {
        return target;
    }

    public void setTarget(MACAddress lightAddress) {
        this.target = lightAddress != null ? lightAddress : MACAddress.BROADCAST_ADDRESS;
    }

    public ByteBuffer getReserved1() {
        return reserved1;
    }

    public void setReserved1(ByteBuffer reserved2) {
        this.reserved1 = reserved2;
    }

    public boolean getAckRequired() {
        return (ackbyte & 0x02) >> 1 == 1 ? true : false;
    }

    public void setAckRequired(boolean flag) {
        this.ackbyte = (ackbyte & ~(1 << 1)) | ((flag ? 1 : 0) << 1);
    }

    public boolean getResponseRequired() {
        return (ackbyte & 0x01) >> 0 == 1 ? true : false;
    }

    public void setResponseRequired(boolean flag) {
        this.ackbyte = (ackbyte & ~(1 << 0)) | ((flag ? 1 : 0) << 0);
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        if (0 <= sequence && sequence < 256) {
            this.sequence = sequence;
        } else {
            throw new IllegalArgumentException("Sequence number '" + sequence + "' is not in range [0, 255]");
        }
    }

    public ByteBuffer getReserved2() {
        return reserved2;
    }

    public void setReserved2(ByteBuffer reserved3) {
        this.reserved2 = reserved3;
    }

    public int getPacketType() {
        return packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public ByteBuffer getReserved3() {
        return reserved3;
    }

    public void setReserved3(ByteBuffer reserved4) {
        this.reserved3 = reserved4;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Creates an empty packet, setting some default values via
     * {@link #preambleDefaults()}.
     */
    public Packet() {
        preambleDefaults();
        timeStamp = System.currentTimeMillis();
    }

    /**
     * Parses, in order, the defined preamble fields, storing collected values.
     * The buffer's position will be left at the end of the parsed fields and
     * should be equal to the value returned by {@link #preambleLength()}.
     *
     * @param bytes the buffer to read from.
     */
    protected void parsePreamble(ByteBuffer bytes) {
        size = FIELD_SIZE.value(bytes);
        protocol = FIELD_PROTOCOL.value(bytes);
        source = FIELD_SOURCE.value(bytes);
        target = FIELD_TARGET.value(bytes);
        reserved1 = FIELD_RESERVED_1.value(bytes);
        ackbyte = FIELD_ACK.value(bytes);
        sequence = FIELD_SEQUENCE.value(bytes);
        reserved2 = FIELD_RESERVED_2.value(bytes);
        packetType = FIELD_PACKET_TYPE.value(bytes);
        reserved3 = FIELD_RESERVED_3.value(bytes);
    }

    /**
     * Calculates the length of the packet header, defined as the sum of the
     * lengths of all defined fields (see {@link #PREAMBLE_FIELDS}).
     *
     * @return the sum of the length of preamble fields
     */
    protected int preambleLength() {
        int sum = 0;

        for (Field<?> f : PREAMBLE_FIELDS) {
            sum += f.getLength();
        }

        return sum;
    }

    /**
     * Returns a new {@code ByteBuffer} containing the encoded preamble. Note
     * that the returned buffer will have its position set at the end of the
     * buffer and will need to have {@link ByteBuffer#rewind()} called before
     * use.
     *
     * <p>
     * The length of the buffer is the sum of the lengths of the defined
     * preamble fields (see {@link #PREAMBLE_FIELDS} for an ordered list), which
     * may also be accessed via {@link #preambleLength()}.
     *
     * <p>
     * Certain fields are set to default values based on other class methods.
     * For example, the size and packet type fields will be set to the values
     * returned from {@link #length()} and {@link #packetType()}, respectively.
     * Other defaults (such as the protocol, light address, site, and timestamp)
     * may be specified either by directly setting the relevant protected
     * variables or by overriding {@link #preambleDefaults()}.
     *
     * @return a new buffer containing the encoded preamble
     */
    protected ByteBuffer preambleBytes() {
        return ByteBuffer.allocate(preambleLength()).put(FIELD_SIZE.bytes(length())).put(FIELD_PROTOCOL.bytes(protocol))
                .put(FIELD_SOURCE.bytes(source)).put(FIELD_TARGET.bytes(target))
                .put(ByteBuffer.allocate(FIELD_RESERVED_1.getLength())) // empty
                .put(FIELD_ACK.bytes(ackbyte)).put(FIELD_SEQUENCE.bytes(sequence))
                .put(ByteBuffer.allocate(FIELD_RESERVED_2.getLength())) // empty
                .put(FIELD_PACKET_TYPE.bytes(packetType())).put(ByteBuffer.allocate(FIELD_RESERVED_3.getLength())); // empty
    }

    /**
     * Sets default preamble values. If needed, subclasses may override these
     * values by specifically overriding this method, or by setting individual
     * values within the constructor, as this method is called automatically
     * during initialization.
     */
    protected void preambleDefaults() {
        size = 0;
        protocol = 1024;
        target = new MACAddress();
        sequence = 0;
        packetType = packetType();
    }

    /**
     * Returns the packet type. Note that this value is technically distinct
     * from {@code getPacketType()} in that it returns the packet type the
     * current {@code Packet} subtype is designed to parse, while
     * {@code getPacketType()} returns the actual {@code packetType} field of
     * a parsed packet. However, these values should always match.
     *
     * @return the packet type intended to be handled by this Packet subtype
     */
    public abstract int packetType();

    /**
     * Returns the length of the payload specific to this packet subtype. The
     * length of the preamble is specifically excluded.
     *
     * @return the length of this specialized packet payload
     */
    protected abstract int packetLength();

    /**
     * Parses the given {@link ByteBuffer} into class fields. Subtypes may
     * implement {@link #parsePacket(ByteBuffer)} to parse additional fields;
     * the preamble by default is always parsed.
     *
     * @param bytes the buffer to extract data from
     */
    public void parse(ByteBuffer bytes) {
        bytes.rewind();
        parsePreamble(bytes);
        parsePacket(bytes);
    }

    /**
     * Extracts data from the given {@link ByteBuffer} into fields specific to
     * this packet subtype. The preamble will already have been parsed; as such,
     * the buffer will be positioned at the end of the preamble. If needed,
     * {@link #preambleLength()} may be used to restore the position of the
     * buffer.
     *
     * @param bytes the raw bytes to parse
     */
    protected abstract void parsePacket(ByteBuffer bytes);

    /**
     * Returns a {@link ByteBuffer} containing the full payload for this packet,
     * including the populated preamble and any specialized packet payload. The
     * returned buffer will be at position zero.
     *
     * @return the full packet payload
     */
    public ByteBuffer bytes() {
        ByteBuffer preamble = preambleBytes();
        preamble.rewind();

        ByteBuffer packet = packetBytes();
        packet.rewind();

        ByteBuffer ret = ByteBuffer.allocate(length()).put(preamble).put(packet);
        ret.rewind();

        return ret;
    }

    /**
     * Returns a {@link ByteBuffer} containing the payload for this packet. Its
     * length must match the value of {@link #packetLength()}. This specifically
     * excludes preamble fields and should contain only data specific to the
     * packet subtype.
     * <p>
     * Note that returned ByteBuffers will have {@link ByteBuffer#rewind()}
     * called automatically before they are appended to the final packet
     * buffer.
     *
     * @return the packet payload
     */
    protected abstract ByteBuffer packetBytes();

    /**
     * Gets the total length of this packet, in bytes. Specifically, this method
     * is the sum of the preamble ({@link #preambleLength()}) and the payload
     * length ({@link #packetLength()}); subtypes should override methods for
     * those values if desired.
     *
     * @return the total length of this packet
     */
    public int length() {
        return preambleLength() + packetLength();
    }

    /**
     * Returns a list of expected response packet types. An empty array means
     * no responses are expected (suitable for response packet definitions),
     *
     * @return a list of expected responses
     */
    public abstract int[] expectedResponses();

    public boolean isExpectedResponse(int type) {
        for (int a : expectedResponses()) {
            if (a == type) {
                return true;
            }
        }

        return false;
    }

    public boolean isFulfilled(Packet somePacket) {
        return isExpectedResponse(somePacket.getPacketType());
    }
}
