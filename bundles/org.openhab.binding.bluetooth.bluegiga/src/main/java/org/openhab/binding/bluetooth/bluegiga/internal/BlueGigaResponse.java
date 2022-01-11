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
package org.openhab.binding.bluetooth.bluegiga.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.AttributeChangeReason;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.AttributeValueType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ConnectionStatusFlag;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.ScanResponseType;

/**
 * Abstract class for response and event packets. This provides the deserialization methods to convert wire data to Java
 * classes.
 *
 * @author Chris Jackson - Initial contribution and API
 *
 */
@NonNullByDefault
public abstract class BlueGigaResponse extends BlueGigaPacket {
    private int[] buffer = new int[131];
    private int position = 0;
    protected boolean event = false;

    protected BlueGigaResponse(int[] inputBuffer) {
        // TODO Auto-generated constructor stub
        buffer = inputBuffer;
        position = 4;
    }

    /**
     * Returns true if this response is an event, or false if it is a response to a command
     *
     * @return true if this is an event
     */
    public boolean isEvent() {
        return event;
    }

    /**
     * Reads a int8 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeInt8() {
        if (buffer[position] >= 128) {
            return buffer[position++] - 256;
        } else {
            return buffer[position++];
        }
    }

    /**
     * Reads a uint8 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeUInt8() {
        return buffer[position++];
    }

    protected boolean deserializeBoolean() {
        return buffer[position++] != 0;
    }

    /**
     * Reads a uint16 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeUInt16() {
        return buffer[position++] + (buffer[position++] << 8);
    }

    protected UUID deserializeUuid() {
        long low;
        long high;

        // This is a uint8array type so first byte is the length
        int length = buffer[position++];
        switch (length) {
            case 2:
                // 0000xxxx-0000-1000-8000-00805F9B34FB
                low = 0x800000805f9b34fbL;
                high = ((long) buffer[position++] << 32) + ((long) buffer[position++] << 40) + 0x00001000L;
                break;
            case 4:
                // xxxxxxxx-0000-1000-8000-00805F9B34FB
                low = 0x800000805f9b34fbL;
                high = ((long) buffer[position++] << 32) + ((long) buffer[position++] << 40)
                        + ((long) buffer[position++] << 48) + ((long) buffer[position++] << 56) + 0x00001000L;
                break;
            case 16:
                low = (buffer[position++]) + ((long) buffer[position++] << 8) + ((long) buffer[position++] << 16)
                        + ((long) buffer[position++] << 24) + ((long) buffer[position++] << 32)
                        + ((long) buffer[position++] << 40) + ((long) buffer[position++] << 48)
                        + ((long) buffer[position++] << 56);
                high = (buffer[position++]) + ((long) buffer[position++] << 8) + ((long) buffer[position++] << 16)
                        + ((long) buffer[position++] << 24) + ((long) buffer[position++] << 32)
                        + ((long) buffer[position++] << 40) + ((long) buffer[position++] << 48)
                        + ((long) buffer[position++] << 56);
                break;
            default:
                low = 0;
                high = 0;
                position += length;
                break;
        }
        return new UUID(high, low);
    }

    protected BgApiResponse deserializeBgApiResponse() {
        return BgApiResponse.getBgApiResponse(deserializeUInt16());
    }

    public Set<ConnectionStatusFlag> deserializeConnectionStatusFlag() {
        int val = deserializeUInt8();
        Set<ConnectionStatusFlag> options = new HashSet<>();
        for (ConnectionStatusFlag option : ConnectionStatusFlag.values()) {
            if (option == ConnectionStatusFlag.UNKNOWN) {
                continue;
            }
            if ((option.getKey() & val) != 0) {
                options.add(option);
            }
        }
        return options;
    }

    protected AttributeValueType deserializeAttributeValueType() {
        return AttributeValueType.getAttributeValueType(deserializeUInt8());
    }

    protected BluetoothAddressType deserializeBluetoothAddressType() {
        return BluetoothAddressType.getBluetoothAddressType(deserializeUInt8());
    }

    protected AttributeChangeReason deserializeAttributeChangeReason() {
        return AttributeChangeReason.getAttributeChangeReason(deserializeUInt8());
    }

    protected ScanResponseType deserializeScanResponseType() {
        return ScanResponseType.getScanResponseType(deserializeUInt8());
    }

    protected long deserializeUInt32() {
        return buffer[position++] + (buffer[position++] << 8) + (buffer[position++] << 16) + (buffer[position++] << 24);
    }

    protected int[] deserializeUInt8Array() {
        int length = buffer[position++];
        int[] val = new int[length];

        for (int cnt = 0; cnt < length; cnt++) {
            val[cnt] = deserializeUInt8();
        }

        return val;
    }

    protected String deserializeAddress() {
        StringBuilder builder = new StringBuilder();

        for (int cnt = 5; cnt >= 0; cnt--) {
            if (cnt < 5) {
                builder.append(':');
            }
            builder.append(String.format("%02X", buffer[position + cnt]));
        }
        position += 6;

        return builder.toString();
    }
}
