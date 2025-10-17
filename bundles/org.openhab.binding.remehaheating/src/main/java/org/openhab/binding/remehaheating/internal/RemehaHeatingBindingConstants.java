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
package org.openhab.binding.remehaheating.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RemehaHeatingBindingConstants} class defines common constants used across the binding.
 * 
 * This class contains:
 * - Thing type UIDs for supported devices
 * - Channel identifiers for all supported channels
 * - Configuration parameter names
 * - DHW mode constants
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaHeatingBindingConstants {

    private static final String BINDING_ID = "remehaheating";

    // Thing Type UIDs
    /** Thing type UID for Remeha boiler */
    public static final ThingTypeUID THING_TYPE_BOILER = new ThingTypeUID(BINDING_ID, "boiler");

    // Channel identifiers
    /** Current room temperature channel */
    public static final String CHANNEL_ROOM_TEMPERATURE = "room-temperature";
    /** Target room temperature channel (read/write) */
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    /** Current DHW temperature channel */
    public static final String CHANNEL_DHW_TEMPERATURE = "dhw-temperature";
    /** Target DHW temperature channel */
    public static final String CHANNEL_DHW_TARGET = "dhw-target";
    /** System water pressure channel */
    public static final String CHANNEL_WATER_PRESSURE = "water-pressure";
    /** Outdoor temperature channel */
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    /** Boiler error status channel */
    public static final String CHANNEL_STATUS = "status";
    /** DHW operating mode channel (read/write) */
    public static final String CHANNEL_DHW_MODE = "dhw-mode";
    /** Water pressure OK status channel */
    public static final String CHANNEL_WATER_PRESSURE_OK = "water-pressure-ok";
    /** DHW status channel */
    public static final String CHANNEL_DHW_STATUS = "dhw-status";

    // Configuration parameter names
    /** Email configuration parameter */
    public static final String CONFIG_EMAIL = "email";
    /** Password configuration parameter */
    public static final String CONFIG_PASSWORD = "password";
    /** Refresh interval configuration parameter */
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";

    // DHW operating modes
    /** Anti-frost DHW mode - minimal heating to prevent freezing */
    public static final String DHW_MODE_ANTI_FROST = "anti-frost";
    /** Schedule DHW mode - follows programmed schedule */
    public static final String DHW_MODE_SCHEDULE = "schedule";
    /** Continuous comfort DHW mode - maintains target temperature */
    public static final String DHW_MODE_CONTINUOUS_COMFORT = "continuous-comfort";
}
