/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zway.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ZWayBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Hecker - Initial contribution
 */
@NonNullByDefault
public class ZWayBindingConstants {

    public static final String BINDING_ID = "zway";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "zwayServer");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "zwayDevice");
    public static final ThingTypeUID THING_TYPE_VIRTUAL_DEVICE = new ThingTypeUID(BINDING_ID, "zwayVirtualDevice");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_DEVICE, THING_TYPE_VIRTUAL_DEVICE).collect(Collectors.toSet()));

    // List of ignored devices for Discovery
    public static final Set<String> DISCOVERY_IGNORED_DEVICES = Collections
            .unmodifiableSet(Collections.singleton(("BatteryPolling")));

    // List of all Channel IDs
    public static final String BATTERY_CHANNEL = "battery";
    public static final String DOORLOCK_CHANNEL = "doorlock";
    public static final String SENSOR_BINARY_CHANNEL = "sensorBinary";
    public static final String SENSOR_MULTILEVEL_CHANNEL = "sensorMultilevel";
    public static final String SENSOR_DISCRETE_CHANNEL = "sensorDiscrete";
    public static final String SWITCH_BINARY_CHANNEL = "switchBinary";
    public static final String SWITCH_CONTROL_CHANNEL = "switchControl";
    public static final String SWITCH_MULTILEVEL_CHANNEL = "switchMultilevel";
    // switch multilevel (color)
    public static final String SWITCH_COLOR_CHANNEL = "switchColor";
    public static final String SWITCH_COLOR_TEMPERATURE_CHANNEL = "switchColorTemperature";
    // thermostat
    public static final String THERMOSTAT_MODE_CHANNEL = "thermostatMode";
    public static final String THERMOSTAT_SET_POINT_CHANNEL = "thermostatSetPoint";

    public static final String THERMOSTAT_MODE_CC_CHANNEL = "thermostatModeCC";

    // sensor multilevel
    public static final String SENSOR_TEMPERATURE_CHANNEL = "sensorTemperature";
    public static final String SENSOR_LUMINOSITY_CHANNEL = "sensorLuminosity";
    public static final String SENSOR_HUMIDITY_CHANNEL = "sensorHumidity";
    public static final String SENSOR_BAROMETER_CHANNEL = "sensorBarometer";
    public static final String SENSOR_ULTRAVIOLET_CHANNEL = "sensorUltraviolet";
    public static final String SENSOR_CO2_CHANNEL = "sensorCO2";
    public static final String SENSOR_ENERGY_CHANNEL = "sensorEnergy";
    // sensor multilevel (meter)
    public static final String SENSOR_METER_KWH_CHANNEL = "sensorMeterKWh";
    public static final String SENSOR_METER_W_CHANNEL = "sensorMeterW";
    // sensor binary
    public static final String SENSOR_SMOKE_CHANNEL = "sensorSmoke";
    public static final String SENSOR_CO_CHANNEL = "sensorCo";
    public static final String SENSOR_FLOOD_CHANNEL = "sensorFlood";
    public static final String SENSOR_TAMPER_CHANNEL = "sensorTamper";
    public static final String SENSOR_DOOR_WINDOW_CHANNEL = "sensorDoorWindow";
    public static final String SENSOR_MOTION_CHANNEL = "sensorMotion";
    // switch binary
    public static final String SWITCH_POWER_OUTLET_CHANNEL = "switchPowerOutlet";
    // switch multilevel
    public static final String SWITCH_ROLLERSHUTTER_CHANNEL = "switchBlinds";
    // special channels
    public static final String ACTIONS_CHANNEL = "actions";
    public static final String SECURE_INCLUSION_CHANNEL = "secureInclusion";
    public static final String INCLUSION_CHANNEL = "inclusion";
    public static final String EXCLUSION_CHANNEL = "exclusion";

    public static final String ACTIONS_CHANNEL_OPTION_REFRESH = "REFRESH";

    /* Bridge config properties */
    public static final String BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS = "zwayServerIpAddress";
    public static final String BRIDGE_CONFIG_ZWAY_SERVER_PORT = "zwayServerPort";
    public static final String BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL = "zwayServerProtocol";
    public static final String BRIDGE_CONFIG_ZWAY_SERVER_USERNAME = "zwayServerUsername";
    public static final String BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD = "zwayServerPassword";
    public static final String BRIDGE_CONFIG_POLLING_INTERVAL = "pollingInterval";

    public static final String DEVICE_CONFIG_NODE_ID = "nodeId";
    public static final String DEVICE_CONFIG_VIRTUAL_DEVICE_ID = "deviceId";

    public static final String DEVICE_PROP_LOCATION = "location";
    public static final String DEVICE_PROP_MANUFACTURER_ID = "manufacturerId";
    public static final String DEVICE_PROP_DEVICE_TYPE = "deviceType";
    public static final String DEVICE_PROP_ZDDXMLFILE = "zddxmlfile";
    public static final String DEVICE_PROP_SDK = "SDK";
    public static final String DEVICE_PROP_LAST_UPDATE = "lastUpdate";

    /* Bridge properties */
    public static final String BRIDGE_PROP_SOFTWARE_REVISION_VERSION = "softwareRevisionVersion";
    public static final String BRIDGE_PROP_SOFTWARE_REVISION_DATE = "softwareRevisionDate";
    public static final String BRIDGE_PROP_SDK = "SDK";
    public static final String BRIDGE_PROP_MANUFACTURER_ID = "manufacturerId";
    public static final String BRIDGE_PROP_SECURE_INCLUSION = "secureInclusion";
    public static final String BRIDGE_PROP_FREQUENCY = "frequency";
}
