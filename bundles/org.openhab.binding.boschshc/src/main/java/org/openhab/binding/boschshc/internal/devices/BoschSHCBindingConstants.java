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
package org.openhab.binding.boschshc.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoschSHCBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - added Shutter Control, ThermostatHandler
 * @author Christian Oeing - Added WallThermostatHandler
 * @author David Pace - Added cameras, intrusion detection system, smart plugs, battery state support and smart bulbs
 * @author Christian Oeing - Added smoke detector
 */
@NonNullByDefault
public class BoschSHCBindingConstants {

    private BoschSHCBindingConstants() {
        // Class containing constants only
    }

    public static final String BINDING_ID = "boschshc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHC = new ThingTypeUID(BINDING_ID, "shc");

    public static final ThingTypeUID THING_TYPE_INWALL_SWITCH = new ThingTypeUID(BINDING_ID, "in-wall-switch");
    public static final ThingTypeUID THING_TYPE_TWINGUARD = new ThingTypeUID(BINDING_ID, "twinguard");
    public static final ThingTypeUID THING_TYPE_WINDOW_CONTACT = new ThingTypeUID(BINDING_ID, "window-contact");
    public static final ThingTypeUID THING_TYPE_WINDOW_CONTACT_2 = new ThingTypeUID(BINDING_ID, "window-contact-2");
    public static final ThingTypeUID THING_TYPE_WINDOW_CONTACT_2_PLUS = new ThingTypeUID(BINDING_ID,
            "window-contact-2-plus");
    public static final ThingTypeUID THING_TYPE_MOTION_DETECTOR = new ThingTypeUID(BINDING_ID, "motion-detector");
    public static final ThingTypeUID THING_TYPE_SHUTTER_CONTROL = new ThingTypeUID(BINDING_ID, "shutter-control");
    public static final ThingTypeUID THING_TYPE_SHUTTER_CONTROL_2 = new ThingTypeUID(BINDING_ID, "shutter-control-2");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_CLIMATE_CONTROL = new ThingTypeUID(BINDING_ID, "climate-control");
    public static final ThingTypeUID THING_TYPE_WALL_THERMOSTAT = new ThingTypeUID(BINDING_ID, "wall-thermostat");
    public static final ThingTypeUID THING_TYPE_CAMERA_360 = new ThingTypeUID(BINDING_ID, "security-camera-360");
    public static final ThingTypeUID THING_TYPE_CAMERA_EYES = new ThingTypeUID(BINDING_ID, "security-camera-eyes");
    public static final ThingTypeUID THING_TYPE_INTRUSION_DETECTION_SYSTEM = new ThingTypeUID(BINDING_ID,
            "intrusion-detection-system");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG_COMPACT = new ThingTypeUID(BINDING_ID, "smart-plug-compact");
    public static final ThingTypeUID THING_TYPE_SMART_BULB = new ThingTypeUID(BINDING_ID, "smart-bulb");
    public static final ThingTypeUID THING_TYPE_SMOKE_DETECTOR = new ThingTypeUID(BINDING_ID, "smoke-detector");
    public static final ThingTypeUID THING_TYPE_UNIVERSAL_SWITCH = new ThingTypeUID(BINDING_ID, "universal-switch");
    public static final ThingTypeUID THING_TYPE_UNIVERSAL_SWITCH_2 = new ThingTypeUID(BINDING_ID, "universal-switch-2");
    public static final ThingTypeUID THING_TYPE_SMOKE_DETECTOR_2 = new ThingTypeUID(BINDING_ID, "smoke-detector-2");
    public static final ThingTypeUID THING_TYPE_LIGHT_CONTROL_2 = new ThingTypeUID(BINDING_ID, "light-control-2");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_WATER_DETECTOR = new ThingTypeUID(BINDING_ID, "water-detector");
    public static final ThingTypeUID THING_TYPE_RELAY = new ThingTypeUID(BINDING_ID, "relay");

    public static final ThingTypeUID THING_TYPE_USER_DEFINED_STATE = new ThingTypeUID(BINDING_ID, "user-defined-state");

    // List of all Channel IDs
    // Auto-generated from thing-types.xml via script, don't modify
    public static final String CHANNEL_SCENARIO_TRIGGERED = "scenario-triggered";
    public static final String CHANNEL_TRIGGER_SCENARIO = "trigger-scenario";
    public static final String CHANNEL_POWER_SWITCH = "power-switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_RATING = "temperature-rating";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY_RATING = "humidity-rating";
    public static final String CHANNEL_ENERGY_CONSUMPTION = "energy-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION = "power-consumption";
    public static final String CHANNEL_PURITY = "purity";
    public static final String CHANNEL_AIR_DESCRIPTION = "air-description";
    public static final String CHANNEL_PURITY_RATING = "purity-rating";
    public static final String CHANNEL_COMBINED_RATING = "combined-rating";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_LATEST_MOTION = "latest-motion";
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_VALVE_TAPPET_POSITION = "valve-tappet-position";
    public static final String CHANNEL_SETPOINT_TEMPERATURE = "setpoint-temperature";
    public static final String CHANNEL_CHILD_LOCK = "child-lock";
    public static final String CHANNEL_CHILD_PROTECTION = "child-protection";
    public static final String CHANNEL_PRIVACY_MODE = "privacy-mode";
    public static final String CHANNEL_CAMERA_NOTIFICATION = "camera-notification";
    public static final String CHANNEL_SYSTEM_AVAILABILITY = "system-availability";
    public static final String CHANNEL_ARMING_STATE = "arming-state";
    public static final String CHANNEL_ALARM_STATE = "alarm-state";
    public static final String CHANNEL_ACTIVE_CONFIGURATION_PROFILE = "active-configuration-profile";
    public static final String CHANNEL_ARM_ACTION = "arm-action";
    public static final String CHANNEL_DISARM_ACTION = "disarm-action";
    public static final String CHANNEL_MUTE_ACTION = "mute-action";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_SMOKE_CHECK = "smoke-check";
    public static final String CHANNEL_SILENT_MODE = "silent-mode";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_BYPASS_STATE = "bypass-state";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public static final String CHANNEL_KEY_CODE = "key-code";
    public static final String CHANNEL_KEY_NAME = "key-name";
    public static final String CHANNEL_KEY_EVENT_TYPE = "key-event-type";
    public static final String CHANNEL_KEY_EVENT_TIMESTAMP = "key-event-timestamp";
    public static final String CHANNEL_WATER_LEAKAGE = "water-leakage";
    public static final String CHANNEL_PUSH_NOTIFICATIONS_ON_MOVE = "push-notifications-on-move";
    public static final String CHANNEL_ACOUSTIC_SIGNALS_ON_MOVE = "acoustic-signals-on-move";
    public static final String CHANNEL_WATER_LEAKAGE_SENSOR_CHECK = "water-leakage-sensor-check";
    public static final String CHANNEL_SENSOR_MOVED = "sensor-moved";
    public static final String CHANNEL_IMPULSE_SWITCH = "impulse-switch";
    public static final String CHANNEL_IMPULSE_LENGTH = "impulse-length";
    public static final String CHANNEL_INSTANT_OF_LAST_IMPULSE = "instant-of-last-impulse";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_VIBRATION_SENSOR_ENABLED = "vibration-sensor-enabled";
    public static final String CHANNEL_VIBRATION_SENSOR_SENSITIVITY = "vibration-sensor-sensitivity";
    public static final String CHANNEL_VIBRATION_SENSOR_STATE = "vibration-sensor-state";
    // numbered channels
    // the rationale for introducing numbered channels was discussed in
    // https://github.com/openhab/openhab-addons/pull/16400
    public static final String CHANNEL_POWER_SWITCH_1 = "power-switch-1";
    public static final String CHANNEL_POWER_SWITCH_2 = "power-switch-2";
    public static final String CHANNEL_CHILD_PROTECTION_1 = "child-protection-1";
    public static final String CHANNEL_CHILD_PROTECTION_2 = "child-protection-2";

    public static final String CHANNEL_USER_DEFINED_STATE = "user-state";

    // static device/service names
    public static final String SERVICE_INTRUSION_DETECTION = "intrusionDetectionSystem";

    // thing properties
    public static final String PROPERTY_LOCATION_LEGACY = "Location";
    public static final String PROPERTY_LOCATION = "location";
}
