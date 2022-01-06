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

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapConnectableMode;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapDiscoverMode;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapDiscoverableMode;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.SmpIoCapabilities;

/**
 * Abstract base class for all commands. This provides the serialisation methods for converting parameters from Java
 * classes to wire data.
 *
 * @author Chris Jackson - Initial contribution and API
 *
 */
@NonNullByDefault
public abstract class BlueGigaCommand extends BlueGigaPacket {
    protected int[] buffer = new int[131];
    protected int length = 0;

    protected void serializeHeader(int classId, int commandId) {
        // Octet 0 7 1 bit Message Type (MT) 0: Command
        // -------6:3 4 bits Technology Type (TT) 0000: Smart Bluetooth
        // -------2:0 3 bits Length High (LH) Payload length (high bits)
        buffer[length++] = 0;

        // Octet 1 7:0 8 bits Length Low (LL) Payload length (low bits)
        buffer[length++] = 0;

        // Octet 2 7:0 8 bits Class ID (CID) Command class ID
        buffer[length++] = classId;

        // Octet 3 7:0 8 bits Command ID (CMD) Command ID
        buffer[length++] = commandId;
    }

    /**
     * Adds a uint8 into the output stream
     *
     * @param val
     */
    protected void serializeUInt8(int val) {
        buffer[length++] = val & 0xFF;
    }

    protected void serializeBoolean(boolean val) {
        buffer[length++] = val ? 1 : 0;
    }

    /**
     * Adds a uint16 into the output stream
     *
     * @param val
     */
    protected void serializeUInt16(int val) {
        buffer[length++] = val & 0xFF;
        buffer[length++] = (val >> 8) & 0xFF;
    }

    /**
     * Adds a uint32 into the output stream
     *
     * @param passkey
     */
    protected void serializeUInt32(long passkey) {
        buffer[length++] = (int) (passkey & 0xFF);
        buffer[length++] = (int) ((passkey >> 8) & 0xFF);
        buffer[length++] = (int) ((passkey >> 16) & 0xFF);
        buffer[length++] = (int) ((passkey >> 24) & 0xFF);
    }

    protected void serializeUInt8Array(int[] array) {
        serializeUInt8(array.length);

        for (int val : array) {
            serializeUInt8(val);
        }
    }

    protected void serializeUuid(UUID uuid) {
        // TODO this probably needs to support longer UUIDs?
        buffer[length++] = 2;
        long high = uuid.getMostSignificantBits();

        buffer[length++] = (int) ((high >> 32) & 0xff);
        buffer[length++] = (int) ((high >> 40) & 0xff);
    }

    protected void serializeAddress(String address) {
        String[] bytes = address.split(":");
        if (bytes.length == 0) {
            serializeUInt8(0);
            serializeUInt8(0);
            serializeUInt8(0);
            serializeUInt8(0);
            serializeUInt8(0);
            serializeUInt8(0);

            return;
        }

        for (int cnt = 5; cnt >= 0; cnt--) {
            serializeUInt8(Integer.parseInt(bytes[cnt], 16));
        }
    }

    protected void serializeSmpIoCapabilities(SmpIoCapabilities capabilities) {
        serializeUInt8(capabilities.getKey());
    }

    protected void serializeBluetoothAddressType(BluetoothAddressType addrType) {
        serializeUInt8(addrType.getKey());
    }

    protected void serializeGapDiscoverableMode(GapDiscoverableMode mode) {
        serializeUInt8(mode.getKey());
    }

    protected void serializeGapConnectableMode(GapConnectableMode mode) {
        serializeUInt8(mode.getKey());
    }

    protected void serializeGapDiscoverMode(GapDiscoverMode mode) {
        serializeUInt8(mode.getKey());
    }

    protected int[] getPayload() {
        buffer[1] = length - 4;
        return Arrays.copyOfRange(buffer, 0, length);
    }

    public abstract int[] serialize();
}
