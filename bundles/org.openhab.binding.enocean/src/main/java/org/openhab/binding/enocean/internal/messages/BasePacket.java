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
package org.openhab.binding.enocean.internal.messages;

import java.security.InvalidParameterException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class BasePacket {

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

        public byte getValue() {
            return value;
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

    protected ESPPacketType packetType;
    protected byte[] data;
    protected byte[] optionalData = new byte[0];

    public BasePacket(int dataLength, int optionalDataLength, ESPPacketType packetType, byte[] payload) {
        this(dataLength, optionalDataLength, packetType.value, payload);
    }

    public BasePacket(int dataLength, int optionalDataLength, byte packetType, byte[] payload) {
        if (!ESPPacketType.hasValue(packetType)) {
            throw new InvalidParameterException("Packet type is unknown");
        }

        if (dataLength + optionalDataLength > payload.length) {
            throw new InvalidParameterException("data length does not match provided lengths");
        }

        this.packetType = ESPPacketType.getPacketType(packetType);

        this.data = new byte[dataLength];
        System.arraycopy(payload, 0, this.data, 0, dataLength);

        if (optionalDataLength > 0) {
            this.optionalData = new byte[optionalDataLength];
            System.arraycopy(payload, dataLength, optionalData, 0, optionalDataLength);
        } else {
            this.optionalData = new byte[0];
        }
    }

    public ESPPacketType getPacketType() {
        return this.packetType;
    }

    public byte[] getPayload(int offset, int length) {
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    public byte[] getPayload() {
        return data;
    }

    public byte[] getOptionalPayload(int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(optionalData, offset, result, 0, length);
        return result;
    }

    public byte[] getOptionalPayload() {
        return optionalData;
    }
}
