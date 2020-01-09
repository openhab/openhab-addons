/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WizLightingBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
@NonNullByDefault
public class WizLightingBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "wizlighting";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_WIZ_COLOR_BULB = new ThingTypeUID(BINDING_ID, "wizColorBulb");
    public static final ThingTypeUID THING_TYPE_WIZ_TUNABLE_BULB = new ThingTypeUID(BINDING_ID, "wizTunableBulb");
    public static final ThingTypeUID THING_TYPE_WIZ_DIMMABLE_BULB = new ThingTypeUID(BINDING_ID, "wizDimmableBulb");
    public static final ThingTypeUID THING_TYPE_WIZ_SMART_PLUG = new ThingTypeUID(BINDING_ID, "wizPlug");

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_WIZ_COLOR_BULB,
            THING_TYPE_WIZ_TUNABLE_BULB, THING_TYPE_WIZ_DIMMABLE_BULB, THING_TYPE_WIZ_SMART_PLUG)
            .collect(Collectors.toSet());

    /**
     * List of all Channel ids
     */
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_DIMMING = "dimming";
    public static final String CHANNEL_SWITCH_STATE = "state";
    public static final String CHANNEL_LIGHT_MODE = "lightMode";
    public static final String CHANNEL_DYNAMIC_SPEED = "speed";
    public static final String CHANNEL_RSSI = "signalstrength";
    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String CONFIG_MAC_ADDRESS = "bulbMacAddress";

    /**
     * Host address configuration argument key.
     */
    public static final String CONFIG_IP_ADDRESS = "bulbIpAddress";

    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String CONFIG_UPDATE_INTERVAL = "updateInterval";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int DEFAULT_BULB_UDP_PORT = 38899;

    /**
     * Default listener socket default UDP port.
     */
    public static final int DEFAULT_LISTENER_UDP_PORT = 38900;

    /**
     * How long before active discovery times out.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 2;

    /**
     * The number of refresh intervals without a response before a bulb is marked
     * offline
     */
    public static final int INTERVALS_BEFORE_OFFLINE = 4;

    // -------------- Constants Used ----------------

    /**
     * The color temperature range of the WiZ bulbs
     */
    public static final int MIN_COLOR_TEMPERATURE = 2200;
    public static final int MAX_COLOR_TEMPERATURE = 6500;
    public static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;

    // -------------- Bulb Properties ----------------

    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_HOME_ID = "homeId";
    public static final String PROPERTY_ROOM_ID = "roomId";
    public static final String PROPERTY_HOME_LOCK = "homeLock";
    public static final String PROPERTY_PAIRING_LOCK = "pairingLock";
    public static final String PROPERTY_TYPE_ID = "typeId";
    public static final String PROPERTY_MODULE_NAME = "moduleName";
    public static final String PROPERTY_GROUP_ID = "groupId";
}
