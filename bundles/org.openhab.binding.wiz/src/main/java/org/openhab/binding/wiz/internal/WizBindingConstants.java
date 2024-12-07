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
package org.openhab.binding.wiz.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WizBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - update version
 */
@NonNullByDefault
public class WizBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "wiz";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_COLOR_BULB = new ThingTypeUID(BINDING_ID, "color-bulb");
    public static final ThingTypeUID THING_TYPE_TUNABLE_BULB = new ThingTypeUID(BINDING_ID, "tunable-bulb");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_BULB = new ThingTypeUID(BINDING_ID, "dimmable-bulb");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG = new ThingTypeUID(BINDING_ID, "plug");
    public static final ThingTypeUID THING_TYPE_FAN = new ThingTypeUID(BINDING_ID, "fan");
    public static final ThingTypeUID THING_TYPE_FAN_WITH_DIMMABLE_BULB = new ThingTypeUID(BINDING_ID,
            "fan-with-dimmable-bulb");

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_COLOR_BULB, THING_TYPE_TUNABLE_BULB,
            THING_TYPE_DIMMABLE_BULB, THING_TYPE_SMART_PLUG, THING_TYPE_FAN, THING_TYPE_FAN_WITH_DIMMABLE_BULB);

    /**
     * List of all Channel ids
     */
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_LAST_UPDATE = "last-update";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_REVERSE = "reverse";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_ABS = "temperature-abs";

    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_GROUP_LIGHT = "light";
    public static final String CHANNEL_GROUP_FAN = "fan";

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String CONFIG_MAC_ADDRESS = "macAddress";

    /**
     * Host address configuration argument key.
     */
    public static final String CONFIG_IP_ADDRESS = "ipAddress";

    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String CONFIG_UPDATE_INTERVAL = "updateInterval";
    public static final long DEFAULT_REFRESH_INTERVAL_SEC = 60;

    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String CONFIG_RECONNECT_INTERVAL = "reconnectInterval";
    public static final long DEFAULT_RECONNECT_INTERVAL_MIN = 15;

    // -------------- Default values ----------------

    /**
     * The number of refresh intervals without a response before a bulb is marked
     * offline
     */
    public static final int MARK_OFFLINE_AFTER_SEC = 5 * 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int DEFAULT_UDP_PORT = 38899;

    /**
     * Default listener socket default UDP port.
     */
    public static final int DEFAULT_LISTENER_UDP_PORT = 38900;

    /**
     * How long before active discovery times out.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 2;

    // -------------- Constants Used ----------------

    /**
     * The color temperature range of the WiZ bulbs
     */
    public static final int MIN_COLOR_TEMPERATURE = 2200;
    public static final int MAX_COLOR_TEMPERATURE = 6500;

    // -------------- Bulb Properties ----------------

    public static final String PROPERTY_IP_ADDRESS = "ipAddress";

    public static final String PROPERTY_HOME_ID = "homeId";
    public static final String PROPERTY_ROOM_ID = "roomId";
    public static final String PROPERTY_HOME_LOCK = "homeLock";
    public static final String PROPERTY_PAIRING_LOCK = "pairingLock";
    public static final String PROPERTY_TYPE_ID = "typeId";
    public static final String PROPERTY_MODULE_NAME = "moduleName";
    public static final String PROPERTY_GROUP_ID = "groupId";

    public static final String EXPECTED_MODULE_NAME = "ESP01_SHRGB1C_31";
    public static final String LAST_KNOWN_FIRMWARE_VERSION = "1.18.0";
    public static final String MODEL_CONFIG_MINIMUM_FIRMWARE_VERSION = "1.22";
}
