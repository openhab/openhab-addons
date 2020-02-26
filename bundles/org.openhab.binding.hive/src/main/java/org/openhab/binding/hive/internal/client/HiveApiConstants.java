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
package org.openhab.binding.hive.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.net.URI;

/**
 * Constants related to the Hive API.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveApiConstants {
    private HiveApiConstants() {
        throw new AssertionError();
    }

    /**
     * The location of the real Hive API.
     */
    public static final URI DEFAULT_BASE_PATH = URI.create("https://api.prod.bgchprod.info/omnia/");

    /* Endpoints */
    public static final URI ENDPOINT_ACCESS_TOKENS = URI.create("accessTokens");
    public static final URI ENDPOINT_CALLBACK_TOKENS = URI.create("callbackTokens");
    public static final URI ENDPOINT_CHANNELS = URI.create("channels");
    public static final URI ENDPOINT_CONTACTS = URI.create("contacts");
    public static final URI ENDPOINT_DEVICE_TOKENS = URI.create("deviceTokens");
    public static final URI ENDPOINT_EVENTS = URI.create("events");
    public static final URI ENDPOINT_MEDIA = URI.create("media");
    public static final URI ENDPOINT_MIGRATION = URI.create("migrations");
    public static final URI ENDPOINT_NODES = URI.create("nodes");
    public static final URI ENDPOINT_NODE = URI.create("nodes/");
    public static final URI ENDPOINT_PASSWORDS = URI.create("auth/passwordReset");
    public static final URI ENDPOINT_RESERVATIONS = URI.create("nodes/reservation");
    public static final URI ENDPOINT_RULES = URI.create("rules");
    public static final URI ENDPOINT_SESSIONS = URI.create("auth/sessions");
    public static final URI ENDPOINT_SESSION = URI.create("auth/sessions/");
    public static final URI ENDPOINT_TOPOLOGY = URI.create("topology");
    public static final URI ENDPOINT_USERS = URI.create("users");

    /* Media Types */
    public static final String MEDIA_TYPE_JSON = "application/json";
    public static final String MEDIA_TYPE_API_V6_0_0_JSON = "application/vnd.alertme.zoo-6.0.0+json";
    public static final String MEDIA_TYPE_API_V6_1_0_JSON = "application/vnd.alertme.zoo-6.1.0+json";
    public static final String MEDIA_TYPE_API_V6_2_0_JSON = "application/vnd.alertme.zoo-6.2.0+json";
    public static final String MEDIA_TYPE_API_V6_3_0_JSON = "application/vnd.alertme.zoo-6.3.0+json";
    public static final String MEDIA_TYPE_API_V6_4_0_JSON = "application/vnd.alertme.zoo-6.4.0+json";
    public static final String MEDIA_TYPE_API_V6_5_0_JSON = "application/vnd.alertme.zoo-6.5.0+json";
    public static final String MEDIA_TYPE_API_V6_6_0_JSON = "application/vnd.alertme.zoo-6.6.0+json";

    /* Status Codes */
    public static final int STATUS_CODE_200_OK = 200;
    public static final int STATUS_CODE_400_BAD_REQUEST = 400;
    public static final int STATUS_CODE_401_UNAUTHORIZED = 401;
    public static final int STATUS_CODE_403_FORBIDDEN = 403;

    /* Action Types */
    public static final String ACTION_TYPE_GENERIC = "http://alertme.com/schema/json/configuration/configuration.device.action.generic.v1.json#";
    
    /* Attribute Names */
    public static final String ATTRIBUTE_NAME_AUTO_BOOST_V1_AUTO_BOOST_DURATION = "autoBoostDuration";
    public static final String ATTRIBUTE_NAME_AUTO_BOOST_V1_AUTO_BOOST_TARGET_HEAT_TEMPERATURE = "autoBoostTargetHeatTemperature";
    public static final String ATTRIBUTE_NAME_BATTERY_DEVICE_V1_BATTERY_LEVEL = "batteryLevel";
    public static final String ATTRIBUTE_NAME_BATTERY_DEVICE_V1_BATTERY_STATE = "batteryState";
    public static final String ATTRIBUTE_NAME_BATTERY_DEVICE_V1_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String ATTRIBUTE_NAME_BATTERY_DEVICE_V1_NOTIFICATION_STATE = "notificationState";
    public static final String ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_OPERATING_MODE = "operatingMode";
    public static final String ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_OPERATING_STATE = "operatingState";
    public static final String ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_TEMPORARY_OPERATING_MODE_OVERRIDE = "temporaryOperatingModeOverride";
    public static final String ATTRIBUTE_NAME_HEATING_THERMOSTAT_V1_TARGET_HEAT_TEMPERATURE = "targetHeatTemperature";
    public static final String ATTRIBUTE_NAME_ON_OFF_DEVICE_V1_MODE = "mode";
    public static final String ATTRIBUTE_NAME_TEMPERATURE_SENSOR_V1_TEMPERATURE = "temperature";
    public static final String ATTRIBUTE_NAME_TRANSIENT_MODE_V1_ACTIONS = "actions";
    public static final String ATTRIBUTE_NAME_TRANSIENT_MODE_V1_DURATION = "duration";
    public static final String ATTRIBUTE_NAME_TRANSIENT_MODE_V1_IS_ENABLED = "isEnabled";
    public static final String ATTRIBUTE_NAME_TRANSIENT_MODE_V1_START_DATETIME = "startDatetime";
    public static final String ATTRIBUTE_NAME_TRANSIENT_MODE_V1_END_DATETIME = "endDatetime";
    public static final String ATTRIBUTE_NAME_WATER_HEATER_V1_OPERATING_MODE = "operatingMode";
    public static final String ATTRIBUTE_NAME_WATER_HEATER_V1_IS_ON = "isOn";
    public static final String ATTRIBUTE_NAME_WATER_HEATER_V1_TEMPORARY_OPERATING_MODE_OVERRIDE = "temporaryOperatingModeOverride";
    public static final String ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_EUI64 = "eui64";
    public static final String ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_AVERAGE_LQI = "averageLQI";
    public static final String ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_LAST_KNOWN_LQI = "lastKnownLQI";
    public static final String ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_AVERAGE_RSSI = "averageRSSI";
    public static final String ATTRIBUTE_NAME_ZIGBEE_DEVICE_V1_LAST_KNOWN_RSSI = "lastKnownRSSI";
    
    /* Group IDs */
    public static final String GROUP_ID_TRVS = "trvs";
    public static final String GROUP_ID_TRVBM = "trvbm";
    
    /* Feature Types */
    public static final String FEATURE_TYPE_AUTOBOOST_V1 = "http://alertme.com/schema/json/feature/node.feature.autoboost.v1.json#";
    public static final String FEATURE_TYPE_BATTERY_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.battery_device.v1.json#";
    public static final String FEATURE_TYPE_CHILD_LOCK_V1 = "http://alertme.com/schema/json/feature/node.feature.child_lock.v1.json#";
    public static final String FEATURE_TYPE_DEVICE_MANAGEMENT_V1 = "http://alertme.com/schema/json/feature/node.feature.device_management.v1.json#";
    public static final String FEATURE_TYPE_DISPLAY_ORIENTATION_V1 = "http://alertme.com/schema/json/feature/node.feature.display_orientation.v1.json#";
    public static final String FEATURE_TYPE_ETHERNET_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.ethernet_device.v1.json#";
    public static final String FEATURE_TYPE_FROST_PROTECT_V1 = "http://alertme.com/schema/json/feature/node.feature.frost_protect.v1.json#";
    public static final String FEATURE_TYPE_GROUP_V1 = "http://alertme.com/schema/json/feature/node.feature.group.v1.json#";
    public static final String FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.heating_temperature_control_device.v1.json#";
    public static final String FEATURE_TYPE_HEATING_TEMPERATURE_CONTROL_V1 = "http://alertme.com/schema/json/feature/node.feature.heating_temperature_control.v1.json#";
    public static final String FEATURE_TYPE_HEATING_THERMOSTAT_V1 = "http://alertme.com/schema/json/feature/node.feature.heating_thermostat.v1.json#";
    public static final String FEATURE_TYPE_HIVE_HUB_V1 = "http://alertme.com/schema/json/feature/node.feature.hive_hub.v1.json#";
    public static final String FEATURE_TYPE_LIFECYCLE_STATE_V1 = "http://alertme.com/schema/json/feature/node.feature.lifecycle_state.v1.json#";
    public static final String FEATURE_TYPE_LINKS_V1 = "http://alertme.com/schema/json/feature/node.feature.links.v1.json#";
    public static final String FEATURE_TYPE_MOUNTING_MODE_V1 = "http://alertme.com/schema/json/feature/node.feature.mounting_mode.v1.json#";
    public static final String FEATURE_TYPE_ON_OFF_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.on_off_device.v1.json#";
    public static final String FEATURE_TYPE_PI_HEATING_DEMAND_V1 = "http://alertme.com/schema/json/feature/node.feature.pi_heating_demand.v1.json#";
    public static final String FEATURE_TYPE_PHYSICAL_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.physical_device.v1.json#";
    public static final String FEATURE_TYPE_RADIO_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.radio_device.v1.json#";
    public static final String FEATURE_TYPE_STANDBY_V1 = "http://alertme.com/schema/json/feature/node.feature.standby.v1.json#";
    public static final String FEATURE_TYPE_TEMPERATURE_SENSOR_V1 = "http://alertme.com/schema/json/feature/node.feature.temperature_sensor.v1.json#";
    public static final String FEATURE_TYPE_TRANSIENT_MODE_V1 = "http://alertme.com/schema/json/feature/node.feature.transient_mode.v1.json#";
    public static final String FEATURE_TYPE_TRV_CALIBRATION_V1 = "http://alertme.com/schema/json/feature/node.feature.trv_calibration.v1.json#";
    public static final String FEATURE_TYPE_TRV_ERROR_DIAGNOSTICS_V1 = "http://alertme.com/schema/json/feature/node.feature.trv_error_diagnostics.v1.json#";
    public static final String FEATURE_TYPE_WATER_HEATER_V1 = "http://alertme.com/schema/json/feature/node.feature.water_heater.v1.json#";
    public static final String FEATURE_TYPE_ZIGBEE_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.zigbee_device.v1.json#";
    public static final String FEATURE_TYPE_ZIGBEE_ROUTING_DEVICE_V1 = "http://alertme.com/schema/json/feature/node.feature.zigbee_routing_device.v1.json#";
    
    /* Node Types */
    public static final String NODE_TYPE_HUB = "http://alertme.com/schema/json/node.class.hub.json#";
    public static final String NODE_TYPE_THERMOSTAT = "http://alertme.com/schema/json/node.class.thermostat.json#";
    public static final String NODE_TYPE_THERMOSTAT_UI = "http://alertme.com/schema/json/node.class.thermostatui.json#";
    public static final String NODE_TYPE_RADIATOR_VALVE = "http://alertme.com/schema/json/node.class.trv.json#";
    public static final String NODE_TYPE_SYNTHETIC_DAYLIGHT = "http://alertme.com/schema/json/node.class.synthetic.daylight.json#";
    public static final String NODE_TYPE_SYNTHETIC_HOME_STATE = "http://alertme.com/schema/json/node.class.synthetic.home.state.json#";
    public static final String NODE_TYPE_SYNTHETIC_RULE = "http://alertme.com/schema/json/node.class.synthetic.rule.json#";
    
    /* Product Types */
    public static final String PRODUCT_TYPE_ACTIONS = "ACTIONS";
    public static final String PRODUCT_TYPE_BOILER_MODULE = "BOILER_MODULE";
    public static final String PRODUCT_TYPE_DAYLIGHT_SD = "DAYLIGHT_SD";
    public static final String PRODUCT_TYPE_HEATING = "HEATING";
    public static final String PRODUCT_TYPE_HOT_WATER = "HOT_WATER";
    public static final String PRODUCT_TYPE_HUB = "HUB";
    public static final String PRODUCT_TYPE_THERMOSTAT_UI = "THERMOSTAT_UI";
    public static final String PRODUCT_TYPE_TRV = "TRV";
    public static final String PRODUCT_TYPE_TRV_GROUP = "TRV_GROUP";
    public static final String PRODUCT_TYPE_UNKNOWN = "UNKNOWN";

    /* Protocols */
    public static final String PROTOCOL_SYNTHETIC = "SYNTHETIC";
    public static final String PROTOCOL_ZIGBEE = "ZIGBEE";
    public static final String PROTOCOL_PROXIED = "PROXIED";
    public static final String PROTOCOL_MQTT = "MQTT";
    public static final String PROTOCOL_XMPP = "XMPP";
    public static final String PROTOCOL_VIRTUAL = "VIRTUAL";

    /* Schedule Types */
    public static final String SCHEDULE_TYPE_WEEKLY = "http://alertme.com/schema/json/configuration/configuration.device.schedule.weekly.v1.json#";

    public static boolean isServerError(final int statusCode) {
        return statusCode >= 500;
    }
}
