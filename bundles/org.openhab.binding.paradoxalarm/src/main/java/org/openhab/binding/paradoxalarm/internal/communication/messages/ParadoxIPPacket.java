/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openhab.binding.paradoxalarm.internal.communication.crypto.EncryptionHandler;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link ParadoxIPPacket} This class is object representing a full IP request packet. Header and payload together.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(header.getBytes());
            outputStream.write(payload);
            byte[] byteArray = outputStream.toByteArray();

            return byteArray;
        } catch (IOException e) {
            throw new ParadoxRuntimeException("Unable to create byte array stream.", e);
        }
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
         * "IP Encryption 0x08: Disabled 0x09: Enabled"
         */
        private byte encryption = 0x08;
        private byte command = 0;
        private byte subCommand = 0;
        private byte unknown0 = 0x00;
        private byte unknown1 = 0x01;

        public byte[] getBytes() {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                outputStream.write(startOfHeader);
                outputStream.write(ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(payloadLength).array());
                outputStream.write(messageType);
                outputStream.write(encryption);
                outputStream.write(command);
                outputStream.write(subCommand);
                outputStream.write(unknown0);
                outputStream.write(unknown1);
                byte[] byteArray = outputStream.toByteArray();
                return ParadoxUtil.extendArray(byteArray, 16);
            } catch (IOException e) {
                throw new ParadoxRuntimeException("Unable to create byte array stream.", e);
            }
        }

    }
}
