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
package org.openhab.binding.bluetooth;

import java.util.UUID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - refactoring and extension
 */
public class BluetoothBindingConstants {

    public static final String BINDING_ID = "bluetooth";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTED = new ThingTypeUID(BINDING_ID, "connected");
    public static final ThingTypeUID THING_TYPE_BEACON = new ThingTypeUID(BINDING_ID, "beacon");

    // List of all Channel Type IDs
    public static final String CHANNEL_TYPE_RSSI = "rssi";

    public static final String PROPERTY_TXPOWER = "txpower";
    public static final String PROPERTY_MAXCONNECTIONS = "maxconnections";

    public static final String CONFIGURATION_ADDRESS = "address";

    public static final long BLUETOOTH_BASE_UUID = 0x800000805f9b34fbL;

    // Bluetooth profile UUID definitions
    public static final UUID PROFILE_GATT = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_A2DP_SOURCE = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_A2DP_SINK = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_A2DP = UUID.fromString("0000110d-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_AVRCP_REMOTE = UUID.fromString("0000110c-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_CORDLESS_TELEPHONE = UUID.fromString("00001109-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_DID_PNPINFO = UUID.fromString("00001200-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_HEADSET = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_HFP = UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb");
    public static final UUID PROFILE_HFP_AUDIOGATEWAY = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");

}
