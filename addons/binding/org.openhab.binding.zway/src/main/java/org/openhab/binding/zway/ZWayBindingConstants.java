/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ZWayBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayBindingConstants {

    public static final String BINDING_ID = "zway";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "zwayServer");
    public final static ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "zwayDevice");
    public final static ThingTypeUID THING_TYPE_VIRTUAL_DEVICE = new ThingTypeUID(BINDING_ID, "zwayVirtualDevice");

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_DEVICE,
            THING_TYPE_VIRTUAL_DEVICE);

    // List of ignored devices for Discovery
    public final static Set<String> DISCOVERY_IGNORED_DEVICES = ImmutableSet.of("BatteryPolling");

    // List of all Channel IDs
    public final static String BATTERY_CHANNEL = "battery";
    public final static String DOORLOCK_CHANNEL = "doorlock";
    public final static String SENSOR_BINARY_CHANNEL = "sensorBinary";
    public final static String SENSOR_MULTILEVEL_CHANNEL = "sensorMultilevel";
    public final static String SWITCH_BINARY_CHANNEL = "switchBinary";
    public final static String SWITCH_CONTROL_CHANNEL = "switchControl";
    public final static String SWITCH_MULTILEVEL_CHANNEL = "switchMultilevel";
    // switch multilevel (color)
    public final static String SWITCH_COLOR_CHANNEL = "switchColor";
    public final static String SWITCH_COLOR_TEMPERATURE_CHANNEL = "switchColorTemperature";
    // thermostat
    public final static String THERMOSTAT_MODE_CHANNEL = "thermostatMode";
    public final static String THERMOSTAT_SET_POINT_CHANNEL = "thermostatSetPoint";

    public final static String THERMOSTAT_MODE_CC_CHANNEL = "thermostatModeCC";

    // sensor multilevel
    public final static String SENSOR_TEMPERATURE_CHANNEL = "sensorTemperature";
    public final static String SENSOR_LUMINOSITY_CHANNEL = "sensorLuminosity";
    public final static String SENSOR_HUMIDITY_CHANNEL = "sensorHumidity";
    public final static String SENSOR_BAROMETER_CHANNEL = "sensorBarometer";
    public final static String SENSOR_ULTRAVIOLET_CHANNEL = "sensorUltraviolet";
    public final static String SENSOR_CO2_CHANNEL = "sensorCO2";
    public final static String SENSOR_ENERGY_CHANNEL = "sensorEnergy";
    // sensor multilevel (meter)
    public final static String SENSOR_METER_KWH_CHANNEL = "sensorMeterKWh";
    public final static String SENSOR_METER_W_CHANNEL = "sensorMeterW";
    // sensor binary
    public final static String SENSOR_SMOKE_CHANNEL = "sensorSmoke";
    public final static String SENSOR_CO_CHANNEL = "sensorCo";
    public final static String SENSOR_FLOOD_CHANNEL = "sensorFlood";
    public final static String SENSOR_TAMPER_CHANNEL = "sensorTamper";
    public final static String SENSOR_DOOR_WINDOW_CHANNEL = "sensorDoorWindow";
    public final static String SENSOR_MOTION_CHANNEL = "sensorMotion";
    // switch binary
    public final static String SWITCH_POWER_OUTLET_CHANNEL = "switchPowerOutlet";
    // switch multilevel
    public final static String SWITCH_ROLLERSHUTTER_CHANNEL = "switchBlinds";
    // special channels
    public final static String ACTIONS_CHANNEL = "actions";
    public final static String SECURE_INCLUSION_CHANNEL = "secureInclusion";
    public final static String INCLUSION_CHANNEL = "inclusion";
    public final static String EXCLUSION_CHANNEL = "exclusion";

    public final static String ACTIONS_CHANNEL_OPTION_REFRESH = "REFRESH";

    /* Bridge config properties */
    public final static String BRIDGE_CONFIG_OPENHAB_ALIAS = "openHABAlias";
    public final static String BRIDGE_CONFIG_OPENHAB_IP_ADDRESS = "openHABIpAddress";
    public final static String BRIDGE_CONFIG_OPENHAB_PORT = "openHABPort";
    public final static String BRIDGE_CONFIG_OPENHAB_PROTOCOL = "openHABProtocol";
    public final static String BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS = "zwayServerIpAddress";
    public final static String BRIDGE_CONFIG_ZWAY_SERVER_PORT = "zwayServerPort";
    public final static String BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL = "zwayServerProtocol";
    public final static String BRIDGE_CONFIG_ZWAY_SERVER_USERNAME = "zwayServerUsername";
    public final static String BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD = "zwayServerPassword";
    public final static String BRIDGE_CONFIG_POLLING_INTERVAL = "pollingInterval";
    public final static String BRIDGE_CONFIG_OBSERVER_MECHANISM_ENABLED = "observerMechanismEnabled";

    public final static String DEVICE_CONFIG_NODE_ID = "nodeId";
    public final static String DEVICE_CONFIG_VIRTUAL_DEVICE_ID = "deviceId";

    public static final String DEVICE_PROP_LOCATION = "location";
    public static final String DEVICE_PROP_MANUFACTURER_ID = "manufacturerId";
    public static final String DEVICE_PROP_DEVICE_TYPE = "deviceType";
    public static final String DEVICE_PROP_ZDDXMLFILE = "zddxmlfile";
    public static final String DEVICE_PROP_SDK = "SDK";
    public static final String DEVICE_PROP_LAST_UPDATE = "lastUpdate";

    /* Bridge properties */
    public final static String BRIDGE_PROP_SOFTWARE_REVISION_VERSION = "softwareRevisionVersion";
    public final static String BRIDGE_PROP_SOFTWARE_REVISION_DATE = "softwareRevisionDate";
    public final static String BRIDGE_PROP_SDK = "SDK";
    public final static String BRIDGE_PROP_MANUFACTURER_ID = "manufacturerId";
    public final static String BRIDGE_PROP_SECURE_INCLUSION = "secureInclusion";
    public final static String BRIDGE_PROP_FREQUENCY = "frequency";
}
