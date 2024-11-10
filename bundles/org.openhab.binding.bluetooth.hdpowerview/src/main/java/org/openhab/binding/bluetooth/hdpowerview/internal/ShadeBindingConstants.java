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
package org.openhab.binding.bluetooth.hdpowerview.internal;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ShadeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeBindingConstants {

    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, "shade");

    public static final String CHANNEL_SHADE_PRIMARY = "primary";
    public static final String CHANNEL_SHADE_SECONDARY = "secondary";
    public static final String CHANNEL_SHADE_TILT = "tilt";
    public static final String CHANNEL_SHADE_BATTERY_LEVEL = "battery-level";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SHADE);

    public static final int HUNTER_DOUGLAS_MANUFACTURER_ID = 0x819;

    public static final Map<UUID, String> MAP_UID_PROPERTY_NAMES = Map.of( //
            GattCharacteristic.MANUFACTURER_NAME_STRING.getUUID(), Thing.PROPERTY_VENDOR, //
            GattCharacteristic.HARDWARE_REVISION_STRING.getUUID(), Thing.PROPERTY_HARDWARE_VERSION, //
            GattCharacteristic.FIRMWARE_REVISION_STRING.getUUID(), Thing.PROPERTY_FIRMWARE_VERSION, //
            GattCharacteristic.SERIAL_NUMBER_STRING.getUUID(), Thing.PROPERTY_SERIAL_NUMBER, //
            GattCharacteristic.MODEL_NUMBER_STRING.getUUID(), Thing.PROPERTY_MODEL_ID);

    public static final String HUNTER_DOUGLAS = "Hunter Douglas";
    public static final String SHADE_LABEL = "PowerView Shade";

    public static final String PROPERTY_HOME_ID = "homeId";
    public static final String PROPERTY_ENCRYPTION_KEY = "encryptionKey";

    public static final UUID UUID_SERVICE_SHADE = UUID.fromString("0000FDC1-0000-1000-8000-00805F9B34FB");
    public static final UUID UUID_CHARACTERISTIC_POSITION = UUID.fromString("CAFE1001-C0FF-EE01-8000-A110CA7AB1E0");
    public static final UUID UUID_CHARACTERISTIC_TBD = UUID.fromString("CAFE1002-C0FF-EE01-8000-A110CA7AB1E0");
}
