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
package org.openhab.binding.insteon.internal;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSceneButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.RemoteSwitchButtonConfig;
import org.openhab.binding.insteon.internal.device.feature.FeatureEnums.VenstarSystemMode;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link InsteonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonBindingConstants {
    public static final String BINDING_ID = "insteon";
    public static final Path BINDING_DATA_DIR = Path.of(OpenHAB.getUserDataFolder(), BINDING_ID);

    // List of all thing type uids
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_HUB1 = new ThingTypeUID(BINDING_ID, "hub1");
    public static final ThingTypeUID THING_TYPE_HUB2 = new ThingTypeUID(BINDING_ID, "hub2");
    public static final ThingTypeUID THING_TYPE_PLM = new ThingTypeUID(BINDING_ID, "plm");
    public static final ThingTypeUID THING_TYPE_SCENE = new ThingTypeUID(BINDING_ID, "scene");
    public static final ThingTypeUID THING_TYPE_X10 = new ThingTypeUID(BINDING_ID, "x10");
    public static final ThingTypeUID THING_TYPE_LEGACY_DEVICE = new ThingTypeUID(BINDING_ID, "legacy-device");
    public static final ThingTypeUID THING_TYPE_LEGACY_NETWORK = new ThingTypeUID(BINDING_ID, "network");

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE, THING_TYPE_SCENE);
    public static final Set<ThingTypeUID> DISCOVERABLE_LEGACY_THING_TYPES_UIDS = Set.of(THING_TYPE_LEGACY_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE, THING_TYPE_HUB1,
            THING_TYPE_HUB2, THING_TYPE_PLM, THING_TYPE_SCENE, THING_TYPE_X10, THING_TYPE_LEGACY_DEVICE,
            THING_TYPE_LEGACY_NETWORK);

    // List of all thing properties
    public static final String PROPERTY_DEVICE_ADDRESS = "address";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_ENGINE_VERSION = "engineVersion";
    public static final String PROPERTY_PRODUCT_ID = "productId";
    public static final String PROPERTY_SCENE_GROUP = "group";

    // List of all channel parameters
    public static final String PARAMETER_GROUP = "group";
    public static final String PARAMETER_ON_LEVEL = "onLevel";
    public static final String PARAMETER_RAMP_RATE = "rampRate";

    // List of specific device feature names
    public static final String FEATURE_DATABASE_DELTA = "databaseDelta";
    public static final String FEATURE_HEARTBEAT = "heartbeat";
    public static final String FEATURE_HEARTBEAT_INTERVAL = "heartbeatInterval";
    public static final String FEATURE_HEARTBEAT_ON_OFF = "heartbeatOnOff";
    public static final String FEATURE_INSTEON_ENGINE = "insteonEngine";
    public static final String FEATURE_LED_CONTROL = "ledControl";
    public static final String FEATURE_LED_ON_OFF = "ledOnOff";
    public static final String FEATURE_LINK_FF_GROUP = "linkFFGroup";
    public static final String FEATURE_LOW_BATTERY_THRESHOLD = "lowBatteryThreshold";
    public static final String FEATURE_MOMENTARY_DURATION = "momentaryDuration";
    public static final String FEATURE_MONITOR_MODE = "monitorMode";
    public static final String FEATURE_ON_LEVEL = "onLevel";
    public static final String FEATURE_PING = "ping";
    public static final String FEATURE_RAMP_RATE = "rampRate";
    public static final String FEATURE_RELAY_MODE = "relayMode";
    public static final String FEATURE_RELAY_SENSOR_FOLLOW = "relaySensorFollow";
    public static final String FEATURE_SCENE = "scene";
    public static final String FEATURE_STAY_AWAKE = "stayAwake";
    public static final String FEATURE_TEMPERATURE_SCALE = "temperatureScale";
    public static final String FEATURE_TWO_GROUPS = "2Groups";

    // List of specific device feature types
    public static final String FEATURE_TYPE_FANLINC_FAN = "FanLincFan";
    public static final String FEATURE_TYPE_GENERIC_DIMMER = "GenericDimmer";
    public static final String FEATURE_TYPE_GENERIC_SWITCH = "GenericSwitch";
    public static final String FEATURE_TYPE_KEYPAD_BUTTON = "KeypadButton";
    public static final String FEATURE_TYPE_KEYPAD_BUTTON_OFF_MASK = "KeypadButtonOffMask";
    public static final String FEATURE_TYPE_KEYPAD_BUTTON_ON_MASK = "KeypadButtonOnMask";
    public static final String FEATURE_TYPE_KEYPAD_BUTTON_TOGGLE_MODE = "KeypadButtonToggleMode";
    public static final String FEATURE_TYPE_OUTLET_SWITCH = "OutletSwitch";
    public static final String FEATURE_TYPE_REMOTE_SCENE_BUTTON_CONFIG = "RemoteSceneButtonConfig";
    public static final String FEATURE_TYPE_REMOTE_SWITCH_BUTTON_CONFIG = "RemoteSwitchButtonConfig";
    public static final String FEATURE_TYPE_THERMOSTAT_FAN_MODE = "ThermostatFanMode";
    public static final String FEATURE_TYPE_THERMOSTAT_SYSTEM_MODE = "ThermostatSystemMode";
    public static final String FEATURE_TYPE_THERMOSTAT_COOL_SETPOINT = "ThermostatCoolSetpoint";
    public static final String FEATURE_TYPE_THERMOSTAT_HEAT_SETPOINT = "ThermostatHeatSetpoint";
    public static final String FEATURE_TYPE_VENSTAR_FAN_MODE = "VenstarFanMode";
    public static final String FEATURE_TYPE_VENSTAR_SYSTEM_MODE = "VenstarSystemMode";
    public static final String FEATURE_TYPE_VENSTAR_COOL_SETPOINT = "VenstarCoolSetpoint";
    public static final String FEATURE_TYPE_VENSTAR_HEAT_SETPOINT = "VenstarHeatSetpoint";

    // Map of custom state description options
    public static final Map<String, List<String>> CUSTOM_STATE_DESCRIPTION_OPTIONS = Map.ofEntries(
            Map.entry(FEATURE_TYPE_REMOTE_SCENE_BUTTON_CONFIG, RemoteSceneButtonConfig.names()),
            Map.entry(FEATURE_TYPE_REMOTE_SWITCH_BUTTON_CONFIG, RemoteSwitchButtonConfig.names()),
            Map.entry(FEATURE_TYPE_VENSTAR_SYSTEM_MODE, VenstarSystemMode.names()));
}
