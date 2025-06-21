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
package org.openhab.binding.senseenergy.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link SenseEnergyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyBindingConstants {
    private static final String BINDING_ID = "senseenergy";

    // List of all Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "cloud-connector");
    public static final ThingTypeUID MONITOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "monitor");
    public static final ThingTypeUID PROXY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "proxy-device");

    public static final String PARAM_MONITOR_ID = "id";

    public static final int HEARTBEAT_MINUTES = 5;

    /** Monitor Bridge/Thing ***/
    // Channel group type UIDs
    public static final ChannelGroupTypeUID CHANNEL_GROUP_TYPE_DEVICE_TEMPLATE = new ChannelGroupTypeUID(BINDING_ID,
            "device-template");

    // Channel Groups
    public static final String CHANNEL_GROUP_GENERAL = "general";
    public static final String CHANNEL_GROUP_DISCOVERED_DEVICES = "discovered-devices";
    public static final String CHANNEL_GROUP_SELF_REPORTING_DEVICES = "self-reporting-devices";
    public static final String CHANNEL_GROUP_PROXY_DEVICES = "proxy-devices";

    // Monitor Channel IDs
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_GRID_POWER = "grid-power";
    public static final String CHANNEL_POTENTIAL_1 = "potential-1";
    public static final String CHANNEL_POTENTIAL_2 = "potential-2";
    public static final String CHANNEL_LEG_1_POWER = "leg-1-power";
    public static final String CHANNEL_LEG_2_POWER = "leg-2-power";
    public static final String CHANNEL_MAIN_POWER = "main-power";
    public static final String CHANNEL_SOLAR_POWER = "solar-power";
    public static final String CHANNEL_DEVICES_UPDATED_TRIGGER = "devices-updated-trigger";

    // Discovered Device Channel IDs
    public static final String CHANNEL_DEVICE_POWER = "device-power";
    public static final String CHANNEL_DEVICE_TRIGGER = "device-trigger";

    // Properties
    public static final String PROPERTY_MONITOR_SOLAR_CONFIGURED = "solarConfigured";
    public static final String PROPERTY_MONITOR_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_MONITOR_VERSION = "version";
    public static final String PROPERTY_MONITOR_SERIAL = "serial";
    public static final String PROPERTY_MONITOR_SSID = "ssid";
    public static final String PROPERTY_MONITOR_MAC = "mac";

    /** PROXY DEVICE THING ***/
    // Channel IDs
    public static final String CHANNEL_PROXY_DEVICE_POWER = "proxy-device-power";
    public static final String CHANNEL_PROXY_DEVICE_SWITCH = "proxy-device-switch";
    public static final String CHANNEL_PROXY_DEVICE_DIMMER = "proxy-device-dimmer";
    public static final String CHANNEL_PROXY_DEVICE_STATE = "proxy-device-state";

    public static final String CONFIG_PARAMETER_MAC = "mac";
    public static final String CONFIG_PARAMETER_POWER_LEVELS = "powerLevels";
    public static final String CONFIG_PARAMETER_SENSE_NAME = "senseName";

    public static final String ACTION_OUTPUT_CONSUMPTION = "consumption";
    public static final String ACTION_OUTPUT_PRODUCTION = "production";
    public static final String ACTION_OUTPUT_FROM_GRID = "fromGrid";
    public static final String ACTION_OUTPUT_TO_GRID = "toGrid";
    public static final String ACTION_OUTPUT_NET_PRODUCTION = "netProduction";
    public static final String ACTION_OUTPUT_SOLAR_POWERED = "solarPowered";
    public static final String ACTION_INPUT_SCALE = "scale";
    public static final String ACTION_INPUT_DATETIME = "datetime";
}
