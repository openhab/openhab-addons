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
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.communication.crypto.EncryptionHandler;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link ParadoxIPPacket} This class is object representing a full IP request packet. Header and payload together.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxIPPacket implements IPPacket {

    public static final byte[] EMPTY_PAYLOAD = new byte[0];

    private PacketHeader header;
    private byte[] payload;

    public ParadoxIPPacket(byte[] bytes) {
        this(bytes, true);
    }

    @SuppressWarnings("null")
    public ParadoxIPPacket(byte[] payload, boolean isChecksumRequired) {
        this.payload = payload != null ? payload : new byte[0];
        if (isChecksumRequired) {
            payload[payload.length - 1] = ParadoxUtil.calculateChecksum(payload);
        }
        short payloadLength = (short) (payload != null ? payload.length : 0);
        header = new PacketHeader(payloadLength);
    }

    @Override
    public byte[] getBytes() {
        final byte[] headerBytes = header.getBytes();
        int bufferLength = headerBytes.length + payload.length;

        byte[] bufferArray = new byte[bufferLength];
        ByteBuffer buf = ByteBuffer.wrap(bufferArray);
        buf.put(headerBytes);
        buf.put(payload);

        return bufferArray;
    }

    public ParadoxIPPacket setCommand(HeaderCommand command) {
        header.command = command.getValue();
        return this;
    }

    public ParadoxIPPacket setMessageType(HeaderMessageType messageType) {
        header.messageType = messageType.getValue();
        return this;
    }

    public ParadoxIPPacket setUnknown0(byte unknownByteValue) {
        header.unknown0 = unknownByteValue;
        return this;
    }

    @Override
    public PacketHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public void encrypt() {
        EncryptionHandler encryptionHandler = EncryptionHandler.getInstance();
        payload = encryptionHandler.encrypt(payload);
        header.encryption = 0x09;
    }

    @Override
    public String toString() {
        return "ParadoxIPPacket [" + ParadoxUtil.byteArrayToString(getBytes()) + "]";
    }

    public class PacketHeader {

        public PacketHeader(short payloadLength) {
            this.payloadLength = payloadLength;
        }

        private static final int BYTES_LENGTH = 9;

        /**
         * Start of header - always 0xAA
         */
        private byte startOfHeader = (byte) 0xAA;

        /**
         * Payload length - 2 bytes (LL HH)
         */
        private short payloadLength = 0;

        /**
         * "Message Type: 0x01: IP responses 0x02: Serial/pass through cmd response
         * 0x03: IP requests 0x04: Serial/pass through cmd requests"
         */
        private byte messageType = 0x03;

        /**
         * "IP Encryption Disabled=0x08, Enabled=0x09"
         */
        private byte encryption = 0x08;
        private byte command = 0;
        private byte subCommand = 0;
        private byte unknown0 = 0x00;
        private byte unknown1 = 0x01;

        public byte[] getBytes() {
            byte[] bufferArray = new byte[BYTES_LENGTH];
            ByteBuffer buf = ByteBuffer.wrap(bufferArray);
            buf.put(startOfHeader);
            buf.order(ByteOrder.LITTLE_ENDIAN).putShort(payloadLength);
            buf.put(messageType);
            buf.put(encryption);
            buf.put(command);
            buf.put(subCommand);
            buf.put(unknown0);
            buf.put(unknown1);
            return ParadoxUtil.extendArray(bufferArray, 16);
        }
    }
}
