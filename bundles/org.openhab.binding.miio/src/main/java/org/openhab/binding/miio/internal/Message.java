/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Message} is responsible for creating Xiaomi messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class Message {

    private static final byte[] MAGIC = Utils.hexStringToByteArray("2131");

    private byte[] data;
    private byte[] header;
    private byte[] magic;
    private int length = 0;
    private byte[] unknowns = new byte[4];

    private byte[] deviceId = new byte[4];
    private byte[] tsByte = new byte[4];
    private byte[] checksum;
    private byte[] raw;

    public Message(byte[] raw) {
        this.raw = java.util.Arrays.copyOf(raw, (raw.length < 32) ? 32 : raw.length);
        this.header = java.util.Arrays.copyOf(this.raw, 16);
        this.magic = java.util.Arrays.copyOf(this.raw, 2);
        byte[] msgL = java.util.Arrays.copyOfRange(this.raw, 2, 4);
        this.length = ByteBuffer.wrap(msgL).getShort();
        this.unknowns = java.util.Arrays.copyOfRange(this.raw, 4, 8);
        this.deviceId = java.util.Arrays.copyOfRange(this.raw, 8, 12);
        this.tsByte = java.util.Arrays.copyOfRange(this.raw, 12, 16);
        this.checksum = java.util.Arrays.copyOfRange(this.raw, 16, 32);
        this.data = java.util.Arrays.copyOfRange(this.raw, 32, length);
    }

    public static Message createMsg(byte[] data, byte[] token, byte[] deviceID, int timeStamp)
            throws MiIoCryptoException {
        return new Message(createMsgData(data, token, deviceID, timeStamp));
    }

    public static byte[] createMsgData(byte[] data, byte[] token, byte[] deviceID, int timeStamp)
            throws MiIoCryptoException {
        short msgLength = (short) (data.length + 32);
        ByteBuffer header = ByteBuffer.allocate(16);
        header.put(MAGIC);
        header.putShort(msgLength);
        header.put(new byte[4]);
        header.put(deviceID);
        header.putInt(timeStamp);
        ByteBuffer msg = ByteBuffer.allocate(msgLength);
        msg.put(header.array());
        msg.put(getChecksum(header.array(), token, data));
        msg.put(data);
        return msg.array();
    }

    public static byte[] getChecksum(byte[] header, byte[] token, byte[] data) throws MiIoCryptoException {
        ByteBuffer msg = ByteBuffer.allocate(header.length + token.length + data.length);
        msg.put(header);
        msg.put(token);
        msg.put(data);
        return MiIoCrypto.md5(msg.array());
    }

    public String toSting() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = getTimestamp().format(formatter);
        String s = "Message:\r\nHeader  : " + Utils.getSpacedHex(header) + "\r\nchecksum: "
                + Utils.getSpacedHex(checksum);
        if (getLength() > 32) {
            s += "\r\ncontent : " + Utils.getSpacedHex(data);
        } else {
            s += "\r\ncontent : N/A";
        }
        s += "\r\nHeader Details: Magic:" + Utils.getSpacedHex(magic) + "\r\nLength:   " + Integer.toString(length);
        s += "\r\nSerial:   " + Utils.getSpacedHex(deviceId) + "\r\nTS:" + formattedDate;
        return s;
    }

    /**
     * @return the data block
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the header
     */
    public byte[] getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(byte[] header) {
        this.magic = header;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the unknowns
     */
    public byte[] getUnknowns() {
        return unknowns;
    }

    /**
     * @param unknowns the unknowns to set
     */
    public void setUnknowns(byte[] unknowns) {
        this.unknowns = unknowns;
    }

    /**
     * @return the deviceID
     */
    public byte[] getDeviceId() {
        return deviceId;
    }

    /**
     * @param serialByte - Device Id
     */
    public void setDeviceId(byte[] serialByte) {
        this.deviceId = serialByte;
    }

    /**
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(getTimestampAsInt()), ZoneId.systemDefault());
    }

    /**
     * @return the timestamp
     */
    public int getTimestampAsInt() {
        return ByteBuffer.wrap(tsByte).getInt();
    }

    /**
     * @return the raw message
     */
    public byte[] getRaw() {
        return raw;
    }

    /**
     * Set the message content
     *
     * @param raw byte array containing the message
     */
    public void setRaw(byte[] raw) {
        this.raw = java.util.Arrays.copyOf(raw, (raw.length < 32) ? 32 : raw.length);
    }

    /**
     * @return the checksum
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public boolean isChecksumValid() {
        return Arrays.equals(getChecksum(), checksum);
    }
}
