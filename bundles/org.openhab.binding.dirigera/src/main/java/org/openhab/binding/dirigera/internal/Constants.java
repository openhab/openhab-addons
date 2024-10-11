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
package org.openhab.binding.dirigera.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final String BINDING_ID = "dirigera";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "color-light");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_LIGHT = new ThingTypeUID(BINDING_ID, "temperature-light");
    public static final ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motion-sensor");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "light-sensor");
    public static final ThingTypeUID THING_TYPE_CONTACT_SENSOR = new ThingTypeUID(BINDING_ID, "contact-sensor");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG = new ThingTypeUID(BINDING_ID, "smart-plug");
    public static final ThingTypeUID THING_TYPE_UNKNNOWN = new ThingTypeUID(BINDING_ID, "unkown");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY,
            THING_TYPE_COLOR_LIGHT, THING_TYPE_TEMPERATURE_LIGHT, THING_TYPE_MOTION_SENSOR, THING_TYPE_LIGHT_SENSOR,
            THING_TYPE_CONTACT_SENSOR, THING_TYPE_SMART_PLUG);

    public static final String WS_URL = "wss://%s:8443/v1";
    public static final String BASE_URL = "https://%s:8443/v1";
    public static final String OAUTH_URL = BASE_URL + "/oauth/authorize";
    public static final String TOKEN_URL = BASE_URL + "/oauth/token";
    public static final String HOME_URL = BASE_URL + "/home";
    public static final String DEVICE_URL = BASE_URL + "/devices/%s";

    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_DEVICES = "devices";
    public static final String PROPERTY_DEVICE_ID = "id";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_ATTRIBUTES = "attributes";
    public static final String PROPERTY_HTTP_ERROR_STATUS = "http-error-status";
    public static final String PROPERTY_EMPTY = "";

    public static final String ATTRIBUTE_COLOR_MODE = "colorMode";

    public static final String DEVICE_TYPE_GATEWAY = "gateway";
    public static final String DEVICE_TYPE_LIGHT = "light";
    public static final String DEVICE_TYPE_MOTION_SENSOR = "motionSensor";
    public static final String DEVICE_TYPE_LIGHT_SENSOR = "lightSensor";
    public static final String DEVICE_TYPE_CONTACT_SENSOR = "openCloseSensor";
    public static final String DEVICE_TYPE_SMART_PLUG = "outlet";

    // Gateway channels
    public static final String CHANNEL_STATISTICS = "statistics";

    // Generic channels
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";

    // Light channels
    public static final String CHANNEL_LIGHT_HSB = "hsb";
    public static final String CHANNEL_LIGHT_BRIGHTNESS = "brightness";
    public static final String CHANNEL_LIGHT_TEMPERATURE = "temperature";

    // Sensor channels
    public static final String CHANNEL_MOTION_DETECTION = "detection";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";

    // Plug channels
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CURRENT = "ampere";
    public static final String CHANNEL_POTENTIAL = "voltage";
    public static final String CHANNEL_CHILD_LOCK = "child-lock";
    public static final String CHANNEL_STATUS_LIGHT = "status-light";
    public static final String CHANNEL_STATUS_BRIGHTNESS = "status-brightness";

    // Websocket update types
    public static final String EVENT_TYPE_STATE_CHANGE = "deviceStateChanged";

    // Ikea property to openHAB channel mappings
    public static final Map<String, String> SMART_PLUG_MAP = Map.of("isOn", CHANNEL_STATE, "lightLevel",
            CHANNEL_STATUS_BRIGHTNESS, "currentActivePower", CHANNEL_POWER, "currentVoltage", CHANNEL_POTENTIAL,
            "currentAmps", CHANNEL_CURRENT, "statusLight", CHANNEL_STATUS_LIGHT, "childLock", CHANNEL_CHILD_LOCK);
    public static final Map<String, String> TEMPERATURE_LIGHT_MAP = Map.of("isOn", CHANNEL_STATE, "lightLevel",
            CHANNEL_LIGHT_BRIGHTNESS, "colorTemperature", CHANNEL_LIGHT_TEMPERATURE);
    public static final Map<String, String> COLOR_LIGHT_MAP = Map.of("isOn", CHANNEL_STATE, "lightLevel",
            CHANNEL_LIGHT_HSB, "colorHue", CHANNEL_LIGHT_HSB, "colorSaturation", CHANNEL_LIGHT_HSB, "colorTemperature",
            "color-temperature");
    public static final Map<String, String> MOTION_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isDetected", CHANNEL_MOTION_DETECTION);
    public static final Map<String, String> CONTACT_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isOpen", CHANNEL_STATE);
    public static final Map<String, String> LIGHT_SENSOR_MAP = Map.of("illuminance", CHANNEL_ILLUMINANCE);
}
