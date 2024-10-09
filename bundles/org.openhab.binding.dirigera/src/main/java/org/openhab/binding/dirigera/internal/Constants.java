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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motion-sensor");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "light-sensor");
    public static final ThingTypeUID THING_TYPE_UNKNNOWN = new ThingTypeUID(BINDING_ID, "unkown");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY,
            THING_TYPE_COLOR_LIGHT, THING_TYPE_MOTION_SENSOR, THING_TYPE_LIGHT_SENSOR);

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_GATEWAY, THING_TYPE_COLOR_LIGHT, THING_TYPE_MOTION_SENSOR, THING_TYPE_LIGHT_SENSOR)
                    .collect(Collectors.toSet()));

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

    // Gateway channels
    public static final String CHANNEL_STATISTICS = "statistics";

    // Generic channels
    public static final String CHANNEL_ON = "on";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";

    // Light channels
    public static final String CHANNEL_LIGHT_HSB = "hsb";

    // Sensor channels
    public static final String CHANNEL_MOTION_DETECTION = "detection";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";

    // Websocket update types
    public static final String EVENT_TYPE_STATE_CHANGE = "deviceStateChanged";

    // Ikea property to openHAB channel mappings
    public static final Map<String, String> COLOR_LIGHT_MAP = Map.of("isOn", CHANNEL_ON, "lightLevel",
            CHANNEL_LIGHT_HSB, "colorHue", CHANNEL_LIGHT_HSB, "colorSaturation", CHANNEL_LIGHT_HSB, "colorTemperature",
            "color-temperature");
    public static final Map<String, String> MOTION_SENSOR_MAP = Map.of("batteryPercentage", CHANNEL_BATTERY_LEVEL,
            "isDetected", CHANNEL_MOTION_DETECTION);
    public static final Map<String, String> LIGHT_SENSOR_MAP = Map.of("illuminance", CHANNEL_ILLUMINANCE);
}
