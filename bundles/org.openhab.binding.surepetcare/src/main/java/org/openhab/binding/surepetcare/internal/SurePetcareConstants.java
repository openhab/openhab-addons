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
package org.openhab.binding.surepetcare.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SurePetcareConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareConstants {

    public static final String BINDING_ID = "surepetcare";

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_HOUSEHOLD = new ThingTypeUID(BINDING_ID, "household");
    public static final ThingTypeUID THING_TYPE_PET = new ThingTypeUID(BINDING_ID, "pet");
    public static final ThingTypeUID THING_TYPE_HUB_DEVICE = new ThingTypeUID(BINDING_ID, "hubDevice");
    public static final ThingTypeUID THING_TYPE_FLAP_DEVICE = new ThingTypeUID(BINDING_ID, "flapDevice");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_HOUSEHOLD, THING_TYPE_PET, THING_TYPE_HUB_DEVICE, THING_TYPE_FLAP_DEVICE));

    // Bridge configuration property names
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_INTERVAL_TOPOLOGY = "refresh_interval_topology";
    public static final String REFRESH_INTERVAL_LOCATION = "refresh_interval_location";

    // Bridge Channel Names
    public static final String BRIDGE_CHANNEL_ONLINE = "online";

    // Household Channel Names
    public static final String HOUSEHOLD_CHANNEL_ID = "id";
    public static final String HOUSEHOLD_CHANNEL_NAME = "name";
    public static final String HOUSEHOLD_CHANNEL_TIMEZONE_ID = "timezoneId";
    // public static final String HOUSEHOLD_CHANNEL_TIMEZONE = "timezone";
    // public static final String HOUSEHOLD_CHANNEL_TIMEZONE_UTC_OFFSET = "timezoneUTCOffset";

    // Device Channel Names
    public static final String DEVICE_CHANNEL_ID = "id";
    public static final String DEVICE_CHANNEL_NAME = "name";
    public static final String DEVICE_CHANNEL_PRODUCT_ID = "productId";
    public static final String DEVICE_CHANNEL_LED_MODE = "ledMode";
    public static final String DEVICE_CHANNEL_PAIRING_MODE = "pairingMode";
    public static final String DEVICE_CHANNEL_HARDWARE_VERSION = "hardwareVersion";
    public static final String DEVICE_CHANNEL_FIRMWARE_VERSION = "firmwareVersion";
    public static final String DEVICE_CHANNEL_ONLINE = "online";
    public static final String DEVICE_CHANNEL_CURFEW_ENABLED = "curfewEnabled";
    public static final String DEVICE_CHANNEL_CURFEW_LOCK_TIME = "curfewLockTime";
    public static final String DEVICE_CHANNEL_CURFEW_UNLOCK_TIME = "curfewUnlockTime";
    public static final String DEVICE_CHANNEL_LOCKING_MODE = "lockingMode";
    public static final String DEVICE_CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String DEVICE_CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String DEVICE_CHANNEL_LOW_BATTERY = "lowBattery";
    public static final String DEVICE_CHANNEL_DEVICE_RSSI = "deviceRSSI";
    public static final String DEVICE_CHANNEL_HUB_RSSI = "hubRSSI";

    // Pet Channel Names
    public static final String PET_CHANNEL_ID = "id";
    public static final String PET_CHANNEL_NAME = "name";
    public static final String PET_CHANNEL_COMMENT = "comment";
    public static final String PET_CHANNEL_GENDER_ID = "genderId";
    public static final String PET_CHANNEL_BREED_ID = "breedId";
    public static final String PET_CHANNEL_SPECIES_ID = "speciesId";
    public static final String PET_CHANNEL_PHOTO_URL = "photoURL";
    public static final String PET_CHANNEL_LOCATION_ID = "locationId";
    public static final String PET_CHANNEL_LOCATION_CHANGED = "locationChanged";

}
