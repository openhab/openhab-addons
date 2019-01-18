/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.messages;

import java.security.InvalidParameterException;
import java.util.Arrays;

import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.transceiver.Helper;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class ESP3Packet {

    private static final int ENOCEAN_HEADER_LENGTH = 4;
    private static final int ENOCEAN_SYNC_BYTE_LENGTH = 1;
    private static final int ENOCEAN_CRC3_HEADER_LENGTH = 1;
    private static final int ENOCEAN_CRC8_DATA_LENGTH = 1;

    protected ESPPacketType packetType;
    protected byte[] payload;
    protected byte[] optionalPayload = null;

    public enum ESPPacketType {
        RADIO_ERP1((byte) 0x01),
        RESPONSE((byte) 0x02),
        RADIO_SUB_TEL((byte) 0x03),
        EVENT((byte) 0x04),
        COMMON_COMMAND((byte) 0x05),
        SMART_ACK_COMMAND((byte) 0x06),
        REMOTE_MAN_COMMAND((byte) 0x07),
        RADIO_MESSAGE((byte) 0x09),
        RADIO_ERP2((byte) 0x0A);

        private byte value;

        private ESPPacketType(byte value) {
            this.value = value;
        }

        public static boolean hasValue(byte value) {
            for (ESPPacketType p : ESPPacketType.values()) {
                if (p.value == value) {
                    return true;
                }
            }

            return false;
        }

        public static ESPPacketType getPacketType(byte packetType) {
            for (ESPPacketType p : ESPPacketType.values()) {
                if (p.value == packetType) {
                    return p;
                }
            }

            throw new InvalidParameterException("Unknown packetType value");
        }

    }

    public ESP3Packet() {

    }

    public ESP3Packet(int dataLength, int optionalDataLength, byte packetType, byte[] payload) {

        if (!ESPPacketType.hasValue(packetType)) {
            throw new InvalidParameterException("Packet type is unknown");
        }

        if (dataLength + optionalDataLength > payload.length) {
            throw new InvalidParameterException("data length does not match provided lengths");
        }

        setPacketType(ESPPacketType.getPacketType(packetType));

        this.payload = new byte[dataLength];
        System.arraycopy(payload, 0, this.payload, 0, dataLength);

        if (optionalDataLength > 0) {
            this.optionalPayload = new byte[optionalDataLength];
            System.arraycopy(payload, dataLength, optionalPayload, 0, optionalDataLength);
        } else {
            this.optionalPayload = new byte[0];
        }
    }

    public ESP3Packet(int dataLength, int optionalDataLength, ESPPacketType packetType, byte[] payload) {
        this(dataLength, optionalDataLength, packetType.value, payload);
    }

    public void setPacketType(ESPPacketType packetTye) {
        this.packetType = packetTye;
    }

    public ESPPacketType getPacketType() {
        return this.packetType;
    }

    public void setPayload(byte[] data) {
        this.payload = Arrays.copyOf(data, data.length);
    }

    public byte[] getPayload(int offset, int length) {
        return Arrays.copyOfRange(payload, offset, offset + length);
    }

    public final byte[] getPayload() {
        return payload;
    }

    public void setOptionalPayload(byte[] optionalData) {
        if (optionalData == null) {
            this.optionalPayload = null;
        } else {
            this.optionalPayload = Arrays.copyOf(optionalData, optionalData.length);
        }
    }

    public byte[] getOptionalPayload(int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(optionalPayload, offset, result, 0, length);
        return result;
    }

    public final byte[] getOptionalPayload() {
        return optionalPayload;
    }

    public byte[] serialize() throws EnOceanException {
        try {
            int optionalLength = optionalPayload != null ? optionalPayload.length : 0;

            byte[] result = new byte[ENOCEAN_SYNC_BYTE_LENGTH + ENOCEAN_HEADER_LENGTH + ENOCEAN_CRC3_HEADER_LENGTH
                    + payload.length + optionalLength + ENOCEAN_CRC8_DATA_LENGTH];

            result[0] = Helper.ENOCEAN_SYNC_BYTE;
            result[1] = (byte) ((payload.length >> 8) & 0xff);
            result[2] = (byte) (payload.length & 0xff);
            result[3] = (byte) (optionalLength & 0xff);
            result[4] = packetType.value;
            result[5] = Helper.calcCRC8(result, ENOCEAN_SYNC_BYTE_LENGTH, ENOCEAN_HEADER_LENGTH);
            for (int i = 0; i < payload.length; i++) {
                result[6 + i] = payload[i];
            }
            if (optionalPayload != null) {
                for (int i = 0; i < optionalPayload.length; i++) {
                    result[6 + payload.length + i] = (byte) (optionalPayload[i] & 0xff);
                }
            }
            result[6 + payload.length + optionalLength] = Helper.calcCRC8(result, 6, payload.length + optionalLength);

            return result;
        } catch (Exception e) {
            throw new EnOceanException(e.getMessage());
        }
    }
}
