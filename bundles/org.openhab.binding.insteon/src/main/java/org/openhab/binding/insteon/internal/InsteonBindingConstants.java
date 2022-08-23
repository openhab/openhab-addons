/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link InsteonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonBindingConstants {
    public static final String BINDING_ID = "insteon";
    public static final String BINDING_DATA_DIR = OpenHAB.getUserDataFolder() + File.separator + BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_HUB1 = new ThingTypeUID(BINDING_ID, "hub1");
    public static final ThingTypeUID THING_TYPE_HUB2 = new ThingTypeUID(BINDING_ID, "hub2");
    public static final ThingTypeUID THING_TYPE_PLM = new ThingTypeUID(BINDING_ID, "plm");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");
    public static final ThingTypeUID THING_TYPE_X10 = new ThingTypeUID(BINDING_ID, "x10");

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE, THING_TYPE_SCENE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE, THING_TYPE_HUB1,
            THING_TYPE_HUB2, THING_TYPE_PLM, THING_TYPE_SCENE, THING_TYPE_X10);

    // List of all Thing properties
    public static final String PROPERTY_DEVICE_ADDRESS = "address";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_ENGINE_VERSION = "engineVersion";
    public static final String PROPERTY_PRODUCT_ID = "productId";
    public static final String PROPERTY_SCENE_GROUP = "group";

    // List of all channel parameters
    public static final String PARAMETER_GROUP = "group";
    public static final String PARAMETER_ON_LEVEL = "onLevel";
    public static final String PARAMETER_RAMP_RATE = "rampRate";

    // List of specific device features
    public static final String FEATURE_DATABASE_DELTA = "databaseDelta";
    public static final String FEATURE_HEARTBEAT_MONITOR = "heartbeatMonitor";
    public static final String FEATURE_INSTEON_ENGINE = "insteonEngine";
    public static final String FEATURE_LED_CONTROL = "ledControl";
    public static final String FEATURE_LED_ON_OFF = "ledOnOff";
    public static final String FEATURE_OFF_MASK = "offMask";
    public static final String FEATURE_ON_LEVEL = "onLevel";
    public static final String FEATURE_ON_MASK = "onMask";
    public static final String FEATURE_PING = "ping";
    public static final String FEATURE_RAMP_RATE = "rampRate";
    public static final String FEATURE_STAY_AWAKE = "stayAwake";
    public static final String FEATURE_SYSTEM_MODE = "systemMode";
    public static final String FEATURE_TEMPERATURE_FORMAT = "temperatureFormat";
    public static final String FEATURE_TOGGLE_MODE = "toggleMode";

    // List of specific device types
    public static final String CLIMATE_CONTROL_VENSTAR_THERMOSTAT = "ClimateControl_VenstarThermostat";

    // Map of custom state description options
    public static final Map<String, String[]> CUSTOM_STATE_DESCRIPTION_OPTIONS = Map.of(
            // Venstar Thermostat System Mode
            CLIMATE_CONTROL_VENSTAR_THERMOSTAT + ":" + FEATURE_SYSTEM_MODE,
            new String[] { "OFF", "HEAT", "COOL", "AUTO", "PROGRAM_HEAT", "PROGRAM_COOL", "PROGRAM_AUTO" });
}
