/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - refactoring and extension
 */
@NonNullByDefault
public class BluetoothBindingConstants {

    public static final String BINDING_ID = "bluetooth";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BEACON = new ThingTypeUID(BINDING_ID, "beacon");

    // List of all Channel Type IDs
    public static final String CHANNEL_TYPE_RSSI = "rssi";
    public static final String CHANNEL_TYPE_ADAPTER = "adapterUID";
    public static final String CHANNEL_TYPE_ADAPTER_LOCATION = "adapterLocation";

    public static final String PROPERTY_TXPOWER = "txpower";
    public static final String PROPERTY_MAXCONNECTIONS = "maxconnections";
    public static final String PROPERTY_SOFTWARE_VERSION = "softwareVersion";

    public static final String CONFIGURATION_ADDRESS = "address";
    public static final String CONFIGURATION_DISCOVERY = "backgroundDiscovery";
    public static final String CONFIGURATION_ALWAYS_CONNECTED = "alwaysConnected";
    public static final String CONFIGURATION_IDLE_DISCONNECT_DELAY = "idleDisconnectDelay";

    public static final long BLUETOOTH_BASE_UUID = 0x800000805f9b34fbL;

    public static UUID createBluetoothUUID(long uuid16) {
        return new UUID((uuid16 << 32) | 0x1000, BluetoothBindingConstants.BLUETOOTH_BASE_UUID);
    }

    // Bluetooth profile UUID definitions
    public static final UUID PROFILE_GATT = createBluetoothUUID(0x1801);
    public static final UUID PROFILE_A2DP_SOURCE = createBluetoothUUID(0x110a);
    public static final UUID PROFILE_A2DP_SINK = createBluetoothUUID(0x110b);
    public static final UUID PROFILE_A2DP = createBluetoothUUID(0x110d);
    public static final UUID PROFILE_AVRCP_REMOTE = createBluetoothUUID(0x110c);
    public static final UUID PROFILE_CORDLESS_TELEPHONE = createBluetoothUUID(0x1109);
    public static final UUID PROFILE_DID_PNPINFO = createBluetoothUUID(0x1200);
    public static final UUID PROFILE_HEADSET = createBluetoothUUID(0x1108);
    public static final UUID PROFILE_HFP = createBluetoothUUID(0x111e);
    public static final UUID PROFILE_HFP_AUDIOGATEWAY = createBluetoothUUID(0x111f);

    public static final UUID ATTR_CHARACTERISTIC_DECLARATION = createBluetoothUUID(0x2803);
}
