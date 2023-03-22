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
package org.openhab.binding.bluetooth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link BluetoothDescriptor} class defines the Bluetooth descriptor.
 * <p>
 * Descriptors are defined attributes that describe a characteristic value.
 * <p>
 * https://www.bluetooth.com/specifications/gatt/descriptors
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - added constructor and fixed setValue method
 */
@NonNullByDefault
public class BluetoothDescriptor {

    protected final BluetoothCharacteristic characteristic;
    protected final UUID uuid;

    protected final int handle;

    /**
     * The main constructor
     *
     * @param characteristic the characteristic that this class describes
     * @param uuid the uuid of the descriptor
     */
    public BluetoothDescriptor(BluetoothCharacteristic characteristic, UUID uuid, int handle) {
        this.characteristic = characteristic;
        this.uuid = uuid;
        this.handle = handle;
    }

    /**
     * Returns the characteristic this descriptor belongs to.
     *
     * @return
     */
    BluetoothCharacteristic getCharacteristic() {
        return characteristic;
    }

    /**
     * Returns the permissions for this descriptor.
     *
     * @return the permissions
     */
    public int getPermissions() {
        return 0;
    }

    /**
     * Returns the UUID of this descriptor.
     *
     * @return the UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the handle for this descriptor
     *
     * @return the handle for the descriptor
     */
    public int getHandle() {
        return handle;
    }

    public @Nullable GattDescriptor getDescriptor() {
        return GattDescriptor.getDescriptor(uuid);
    }

    public enum GattDescriptor {
        // Descriptors
        CHARACTERISTIC_EXTENDED_PROPERTIES(0x2900),
        CHARACTERISTIC_USER_DESCRIPTION(0x2901),
        CLIENT_CHARACTERISTIC_CONFIGURATION(0x2902),
        SERVER_CHARACTERISTIC_CONFIGURATION(0x2903),
        CHARACTERISTIC_PRESENTATION_FORMAT(0x2904),
        CHARACTERISTIC_AGGREGATE_FORMAT(0x2905),
        VALID_RANGE(0x2906),
        EXTERNAL_REPORT_REFERENCE(0x2907),
        REPORT_REFERENCE(0x2908),
        NUMBER_OF_DIGITALS(0x2909),
        TRIGGER_SETTING(0x290A);

        private static @Nullable Map<UUID, GattDescriptor> uuidToServiceMapping;

        private final UUID uuid;

        private GattDescriptor(long key) {
            this.uuid = BluetoothBindingConstants.createBluetoothUUID(key);
        }

        public static @Nullable GattDescriptor getDescriptor(UUID uuid) {
            Map<UUID, GattDescriptor> localServiceMapping = uuidToServiceMapping;
            if (localServiceMapping == null) {
                localServiceMapping = new HashMap<>();
                for (GattDescriptor s : values()) {
                    localServiceMapping.put(s.uuid, s);
                }
                uuidToServiceMapping = localServiceMapping;
            }
            return localServiceMapping.get(uuid);
        }

        /**
         * @return the key
         */
        public UUID getUUID() {
            return uuid;
        }
    }
}
