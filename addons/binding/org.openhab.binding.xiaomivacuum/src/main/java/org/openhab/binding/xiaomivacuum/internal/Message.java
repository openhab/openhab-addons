/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * The {@link Message} is responsible for creating Xiaomi messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class Message {

    private static final byte[] MAGIC = Utils.hexStringToByteArray("2131");

    private byte[] data;
    private byte[] header;
    private byte[] magic;
    private int length = 0;
    private byte[] unknowns = new byte[4];

    private byte[] serialByte = new byte[4];
    private LocalDateTime timeStamp;
    private byte[] tsByte = new byte[4];
    private byte[] checksum;
    private byte[] raw;

    public Message(byte[] raw) {
        this.setRaw(raw);
        this.header = java.util.Arrays.copyOf(raw, 16);
        this.magic = java.util.Arrays.copyOf(raw, 2);
        byte[] msgL = java.util.Arrays.copyOfRange(raw, 2, 4);
        this.length = ByteBuffer.wrap(msgL).getShort();
        this.unknowns = java.util.Arrays.copyOfRange(raw, 4, 8);
        this.serialByte = java.util.Arrays.copyOfRange(raw, 8, 12);
        this.tsByte = java.util.Arrays.copyOfRange(raw, 12, 16);
        this.timeStamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(ByteBuffer.wrap(tsByte).getInt()),
                ZoneId.systemDefault());
        this.checksum = java.util.Arrays.copyOfRange(raw, 16, 32);
        this.data = java.util.Arrays.copyOfRange(raw, 32, length);
    }

    public static Message createMsg(byte[] data, byte[] token, byte[] serial) throws RoboCryptoException {
        return new Message(createMsgData(data, token, serial));
    }

    public static byte[] createMsgData(byte[] data, byte[] token, byte[] serial) throws RoboCryptoException {
        short msgLength = (short) (data.length + 32);
        ByteBuffer header = ByteBuffer.allocate(16);
        header.put(MAGIC);
        header.putShort(msgLength);
        header.put(new byte[4]);
        header.put(serial);
        header.putInt(nowtimestamp());

        ByteBuffer msg = ByteBuffer.allocate(msgLength);
        msg.put(header.array());
        msg.put(getChecksum(header.array(), token, data));
        msg.put(data);
        return msg.array();
    }

    public static byte[] getChecksum(byte[] header, byte[] token, byte[] data) throws RoboCryptoException {
        ByteBuffer msg = ByteBuffer.allocate(header.length + token.length + data.length);
        msg.put(header);
        msg.put(token);
        msg.put(data);
        return RoboCrypto.md5(msg.array());
    }

    private static int nowtimestamp() {
        Long longTime = TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
        return longTime.intValue();
    }

    public String toSting() {

        long ts = ByteBuffer.wrap(tsByte).getInt();
        Date date = new Date(TimeUnit.SECONDS.toMillis(ts));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getDefault());
        String formattedDate = sdf.format(date);

        String s = "Message:\r\nHeader  : " + Utils.getSpacedHex(header) + "\r\nchecksum: "
                + Utils.getSpacedHex(checksum);
        if (getLength() > 32) {
            s += "\r\ncontent : " + Utils.getSpacedHex(data);
        } else {
            s += "\r\ncontent : N/A";
        }
        s += "\r\nHeader Details: Magic:" + Utils.getSpacedHex(magic) + "\r\nLength:   " + Integer.toString(length);
        s += "\r\nSerial:   " + Utils.getSpacedHex(serialByte) + "\r\nTS:" + formattedDate;
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
     * @return the serialByte
     */
    public byte[] getSerialByte() {
        return serialByte;
    }

    /**
     * @param serialByte the serialByte to set
     */
    public void setSerialByte(byte[] serialByte) {
        this.serialByte = serialByte;
    }

    /**
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timeStamp;
    }

    /**
     * @return the raw message
     */
    public byte[] getRaw() {
        return raw;
    }

    /**
     * @param bytearray message
     */
    public void setRaw(byte[] raw) {
        this.raw = raw;
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
}
