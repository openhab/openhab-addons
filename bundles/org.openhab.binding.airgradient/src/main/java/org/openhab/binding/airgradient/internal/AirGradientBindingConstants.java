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
package org.openhab.binding.airgradient.internal;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirGradientBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientBindingConstants {

    public static final String BINDING_ID = "airgradient";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "airgradient-api");
    public static final ThingTypeUID THING_TYPE_LOCAL = new ThingTypeUID(BINDING_ID, "airgradient-local");
    public static final ThingTypeUID THING_TYPE_LOCATION = new ThingTypeUID(BINDING_ID, "location");

    // List of all Channel ids
    public static final String CHANNEL_PM_01 = "pm01";
    public static final String CHANNEL_PM_02 = "pm02";
    public static final String CHANNEL_PM_10 = "pm10";
    public static final String CHANNEL_PM_003_COUNT = "pm003-count";
    public static final String CHANNEL_ATMP = "atmp";
    public static final String CHANNEL_RHUM = "rhum";
    public static final String CHANNEL_WIFI = "wifi";
    public static final String CHANNEL_RCO2 = "rco2";
    public static final String CHANNEL_TVOC = "tvoc";
    public static final String CHANNEL_LEDS_MODE = "leds";
    public static final String CHANNEL_CALIBRATION = "calibration";
    public static final String CHANNEL_UPLOADS_SINCE_BOOT = "uploads-since-boot";
    public static final String CHANNEL_COUNTRY_CODE = "country-code";
    public static final String CHANNEL_PM_STANDARD = "pm-standard";
    public static final String CHANNEL_ABC_DAYS = "abc-days";
    public static final String CHANNEL_TVOC_LEARNING_OFFSET = "tvoc-learning-offset";
    public static final String CHANNEL_NOX_LEARNING_OFFSET = "nox-learning-offset";
    public static final String CHANNEL_MQTT_BROKER_URL = "mqtt-broker-url";
    public static final String CHANNEL_TEMPERATURE_UNIT = "temperature-unit";
    public static final String CHANNEL_CONFIGURATION_CONTROL = "configuration-control";
    public static final String CHANNEL_POST_TO_CLOUD = "post-to-cloud";
    public static final String CHANNEL_LED_BAR_BRIGHTNESS = "led-bar-brightness";
    public static final String CHANNEL_DISPLAY_BRIGHTNESS = "display-brightness";
    public static final String CHANNEL_MODEL = "model";
    public static final String CHANNEL_LED_BAR_TEST = "led-bar-test";

    // List of all properties
    public static final String PROPERTY_NAME = "name";

    // All configurations
    public static final String CONFIG_LOCATION = "location";
    public static final String CONFIG_API_TOKEN = "token";
    public static final String CONFIG_API_HOST_NAME = "hostname";
    public static final String CONFIG_API_REFRESH_INTERVAL = "refreshInterval";

    // URLs for API
    public static final String CURRENT_MEASURES_PATH = "/public/api/v1/locations/measures/current?token=%s";
    public static final String CURRENT_MEASURES_LOCAL_PATH = "/measures/current";
    public static final String LOCAL_CONFIG_PATH = "/config";
    public static final String LEDS_MODE_PATH = "/public/api/v1/sensors/%s/config/leds/mode?token=%s";
    public static final String CALIBRATE_CO2_PATH = "/public/api/v1/sensors/%s/co2/calibration?token=%s";

    // Discovery
    public static final Duration SEARCH_TIME = Duration.ofSeconds(15);
    public static final boolean BACKGROUND_DISCOVERY = true;
    public static final Duration DEFAULT_POLL_INTERVAL_LOCAL = Duration.ofSeconds(10);

    // Media types
    public static final String CONTENTTYPE_JSON = "application/json";
    public static final String CONTENTTYPE_TEXT = "text/plain";
    public static final String CONTENTTYPE_OPENMETRICS = "application/openmetrics-text";

    // Communication
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
}
