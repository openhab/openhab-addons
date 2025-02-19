/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.grundfosalpha.internal;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.grundfosalpha.internal.protocol.MessageType;
import org.openhab.core.util.HexUtils;

/**
 * This represents a request for writing characteristic to a Bluetooth device.
 *
 * This can be used for adding such requests to a queue.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class CharacteristicRequest {
    private UUID uuid;
    private byte[] value;

    /**
     * Creates a new request object.
     *
     * @param uuid The UUID of the characteristic
     * @param messageType The {@link MessageType} containing the data to write
     */
    public CharacteristicRequest(UUID uuid, MessageType messageType) {
        this.uuid = uuid;
        this.value = messageType.request();
    }

    /**
     * Writes the characteristic to the provided {@link BluetoothDevice}.
     *
     * @param device The Bluetooth device
     * @return true if written, false if the characteristic is not found in the device
     */
    public boolean send(BluetoothDevice device) {
        BluetoothCharacteristic characteristic = device.getCharacteristic(uuid);
        if (characteristic != null) {
            device.writeCharacteristic(characteristic, value);
            return true;
        }
        return false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CharacteristicRequest other)) {
            return false;
        }

        return uuid.equals(other.uuid) && Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, value);
    }

    @Override
    public String toString() {
        return uuid + ": " + HexUtils.bytesToHex(value);
    }
}
