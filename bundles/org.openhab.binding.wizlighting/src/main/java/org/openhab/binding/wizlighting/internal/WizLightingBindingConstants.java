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
package org.openhab.binding.wizlighting.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    public static final ThingTypeUID THING_TYPE_WIZ_BULB = new ThingTypeUID(BINDING_ID, "wizBulb");

    /**
     * List of all Channel ids
     */
    public static final String COLOR_CHANNEL = "color";
    public static final String TEMP_CHANNEL = "temperature";
    public static final String SCENE_CHANNEL = "scene";
    public static final String SCENE_SPEED_CHANNEL = "speed";
    public static final String RSSI_CHANNEL = "signalstrength";

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_WIZ_BULB)));

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String BULB_MAC_ADDRESS_ARG = "bulbMacAddress";

    /**
     * Host address configuration argument key.
     */
    public static final String BULB_IP_ADDRESS_ARG = "bulbIpAddress";

    /**
     * Home id configuration argument key.
     */
    public static final String HOME_ID_ARG = "homeId";

    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String UPDATE_INTERVAL_ARG = "updateInterval";

    /**
     * MAC Address of the OpenHAB machine
     */
    public static final String OH_MAC_ADDRESS_ARG = "ohMacAddress";

    /**
     * IP Address of the OpenHAB machine
     */
    public static final String OH_IP_ADDRESS_ARG = "ohIpAddress";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int BULB_DEFAULT_UDP_PORT = 38899;

    /**
     * Default listener socket default UDP port.
     */
    public static final int LISTENER_DEFAULT_UDP_PORT = 38900;

    /**
     * Default listener socket default UDP port.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 4;

    // -------------- Constants Used ----------------

    /**
     * The color temperature range of the WiZ bulbs
     */
    public static final int MIN_COLOR_TEMPERATURE = 2200;
    public static final int MAX_COLOR_TEMPERATURE = 6500;
    public static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;
}
