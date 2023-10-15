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
package org.openhab.binding.nuki.internal.constants;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NukiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Katter - Initial contribution
 * @author Christian Hoefler - Door sensor integration
 * @author Jan Vybíral - Opener integration
 */
@NonNullByDefault
public class NukiBindingConstants {

    public static final String BINDING_ID = "nuki";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SMARTLOCK = new ThingTypeUID(BINDING_ID, "smartlock");
    public static final ThingTypeUID THING_TYPE_OPENER = new ThingTypeUID(BINDING_ID, "opener");

    public static final Set<ThingTypeUID> THING_TYPE_BRIDGE_UIDS = Set.of(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> THING_TYPE_SMARTLOCK_UIDS = Set.of(THING_TYPE_SMARTLOCK);
    public static final Set<ThingTypeUID> THING_TYPE_OPENER_UIDS = Set.of(THING_TYPE_OPENER);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_BRIDGE_UIDS, THING_TYPE_SMARTLOCK_UIDS, THING_TYPE_OPENER_UIDS).flatMap(Set::stream)
            .collect(Collectors.toSet());

    // Device Types
    public static final int DEVICE_SMART_LOCK = 0;
    public static final int DEVICE_OPENER = 2;
    public static final int DEVICE_SMART_DOOR = 3;
    public static final int DEVICE_SMART_LOCK_3 = 4;

    // Properties
    public static final String PROPERTY_WIFI_FIRMWARE_VERSION = "wifiFirmwareVersion";
    public static final String PROPERTY_HARDWARE_ID = "hardwareId";
    public static final String PROPERTY_SERVER_ID = "serverId";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_NUKI_ID = "nukiId";
    public static final String PROPERTY_BRIDGE_ID = "bridgeId";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";

    // List of all Smart Lock Channel ids
    public static final String CHANNEL_SMARTLOCK_LOCK = "lock";
    public static final String CHANNEL_SMARTLOCK_STATE = "lockState";
    public static final String CHANNEL_SMARTLOCK_LOW_BATTERY = "lowBattery";
    public static final String CHANNEL_SMARTLOCK_KEYPAD_LOW_BATTERY = "keypadLowBattery";
    public static final String CHANNEL_SMARTLOCK_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_SMARTLOCK_BATTERY_CHARGING = "batteryCharging";
    public static final String CHANNEL_SMARTLOCK_DOOR_STATE = "doorsensorState";

    // List of all Opener Channel ids
    public static final String CHANNEL_OPENER_STATE = "openerState";
    public static final String CHANNEL_OPENER_MODE = "openerMode";
    public static final String CHANNEL_OPENER_LOW_BATTERY = "openerLowBattery";
    public static final String CHANNEL_OPENER_RING_ACTION_STATE = "ringActionState";
    public static final String CHANNEL_OPENER_RING_ACTION_TIMESTAMP = "ringActionTimestamp";

    // List of all config-description parameters
    public static final String CONFIG_IP = "ip";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_MANAGECB = "manageCallbacks";
    public static final String CONFIG_API_TOKEN = "apiToken";
    public static final String CONFIG_UNLATCH = "unlatch";
    public static final String CONFIG_SECURE_TOKEN = "secureToken";

    // Nuki Bridge API Lock Actions
    public static final int LOCK_ACTIONS_UNLOCK = 1;
    public static final int LOCK_ACTIONS_LOCK = 2;
    public static final int LOCK_ACTIONS_UNLATCH = 3;
    public static final int LOCK_ACTIONS_LOCKNGO_UNLOCK = 4;
    public static final int LOCK_ACTIONS_LOCKNGO_UNLATCH = 5;

    // Nuki Bridge API Lock States
    public static final int LOCK_STATES_UNCALIBRATED = 0;
    public static final int LOCK_STATES_LOCKED = 1;
    public static final int LOCK_STATES_UNLOCKING = 2;
    public static final int LOCK_STATES_UNLOCKED = 3;
    public static final int LOCK_STATES_LOCKING = 4;
    public static final int LOCK_STATES_UNLATCHED = 5;
    public static final int LOCK_STATES_UNLOCKED_LOCKNGO = 6;
    public static final int LOCK_STATES_UNLATCHING = 7;
    public static final int LOCK_STATES_MOTOR_BLOCKED = 254;
    public static final int LOCK_STATES_UNDEFINED = 255;

    // Nuki Binding additional Lock States
    public static final int LOCK_STATES_UNLOCKING_LOCKNGO = 1002;
    public static final int LOCK_STATES_UNLATCHING_LOCKNGO = 1007;

    // Nuki Binding Door States
    public static final int DOORSENSOR_STATES_UNAVAILABLE = 0;
    public static final int DOORSENSOR_STATES_DEACTIVATED = 1;
    public static final int DOORSENSOR_STATES_CLOSED = 2;
    public static final int DOORSENSOR_STATES_OPEN = 3;
    public static final int DOORSENSOR_STATES_UNKNOWN = 4;
    public static final int DOORSENSOR_STATES_CALIBRATING = 5;

    // trigger channel events
    public static final String EVENT_RINGING = "RINGING";
}
