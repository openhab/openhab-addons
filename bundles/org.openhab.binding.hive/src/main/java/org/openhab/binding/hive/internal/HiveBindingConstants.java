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
package org.openhab.binding.hive.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HiveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class HiveBindingConstants {
    /**
     * The ID of this binding.
     */
    public static final String BINDING_ID = "hive";

    /* ######## Type UIDs ######## */
    /**
     * {@link ThingTypeUID} of a Hive Account.
     */
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    /**
     * {@link ThingTypeUID} of a Hive Boiler Module.
     */
    public static final ThingTypeUID THING_TYPE_BOILER_MODULE = new ThingTypeUID(BINDING_ID, "boiler_module");

    /**
     * {@link ThingTypeUID} of a (virtual) Hive Thermostat Heating Zone.
     */
    public static final ThingTypeUID THING_TYPE_HEATING = new ThingTypeUID(BINDING_ID, "heating");

    /**
     * {@link ThingTypeUID} of a (virtual) Hive Hot Water.
     */
    public static final ThingTypeUID THING_TYPE_HOT_WATER = new ThingTypeUID(BINDING_ID, "hot_water");

    /**
     * {@link ThingTypeUID} of a Hive Hub.
     */
    public static final ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "hub");

    /**
     * {@link ThingTypeUID} of a Hive Thermostat.
     */
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    /**
     * {@link ThingTypeUID} of a Hive Radiator Valve.
     */
    public static final ThingTypeUID THING_TYPE_TRV = new ThingTypeUID(BINDING_ID, "trv");

    /**
     * {@link ThingTypeUID} of a (virtual) Hive Radiator Valve Heating Zone.
     */
    public static final ThingTypeUID THING_TYPE_TRV_GROUP = new ThingTypeUID(BINDING_ID, "trv_group");

    /**
     * The set of {@link ThingTypeUID}s supported by this binding.
     */
    // @formatter:off
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(
            THING_TYPE_ACCOUNT,
            THING_TYPE_BOILER_MODULE,
            THING_TYPE_HEATING,
            THING_TYPE_HOT_WATER,
            THING_TYPE_HUB,
            THING_TYPE_THERMOSTAT,
            THING_TYPE_TRV,
            THING_TYPE_TRV_GROUP
    ).collect(Collectors.toSet()));
    // @formatter:on

    /**
     * The set of {@link ThingTypeUID}s that can be discovered
     * (everything but accounts).
     */
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.unmodifiableSet(
            SUPPORTED_THING_TYPES_UIDS.stream()
                    .filter(it -> it != THING_TYPE_ACCOUNT)
                    .collect(Collectors.toSet())
    );

    /* ######## Channel ids ######## */
    /**
     * Name of the channel that represents how long an auto boost
     * (heat-on-demand boost) should be in minutes.
     */
    public static final String CHANNEL_AUTO_BOOST_DURATION = "auto_boost-duration";

    /**
     * Name of the channel that represents the target heating temperature for a
     * Hive heating zone when auto boost (heating-on-demand) is active.
     */
    public static final String CHANNEL_AUTO_BOOST_TEMPERATURE_TARGET = "auto_boost-temperature-target";

    /**
     * Name of the channel that represents a simplified view of the heating/
     * hot water operating mode that matches the app (e.g. ON / SCHEDULE / OFF)
     */
    public static final String CHANNEL_EASY_MODE_OPERATING = "easy-mode-operating";

    /**
     * Name of the channel that represents a simplified view of if the heating/
     * hot water is being boosted.
     */
    public static final String CHANNEL_EASY_MODE_BOOST = "easy-mode-boost";

    /**
     * Name of the channel that represents if hot water or heating is currently
     * turned on (i.e. is heating water).
     */
    public static final String CHANNEL_EASY_STATE_IS_ON = "easy-state-is_on";

    /**
     * Name of the channel that represents battery level (percent full).
     */
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";

    /**
     * Name of the channel that represents if a battery is low
     * (ON=low battery).
     */
    public static final String CHANNEL_BATTERY_LOW = "battery-low";

    /**
     * Name of the channel that represents battery state
     * (e.g. FULL, NORMAL, LOW).
     */
    public static final String CHANNEL_BATTERY_STATE = "battery-state";

    /**
     * Name of the channel that represents battery voltage.
     */
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";

    /**
     * Name of the channel that represents if a low battery warning has been
     * sent to users of the Hive app.
     */
    public static final String CHANNEL_BATTERY_NOTIFICATION_STATE = "battery-notification_state";

    /**
     * Name of the channel that represents if a Hive device is turned on
     * or off.
     */
    public static final String CHANNEL_MODE_ON_OFF = "mode-on_off";

    /**
     * Name of the channel that represents the operating mode of a Hive device
     * (e.g. SCHEDULE, MANUAL)
     */
    public static final String CHANNEL_MODE_OPERATING = "mode-operating";

    /**
     * Name of the channel that represents the current operating state of a
     * Hive heating related device (e.g. OFF, HEAT).
     */
    public static final String CHANNEL_STATE_OPERATING = "state-operating";

    /**
     * Name of the channel that represents the the temperature currently
     * measured by a Hive device.
     */
    public static final String CHANNEL_TEMPERATURE_CURRENT = "temperature-current";

    /**
     * Name of the channel that represents the target heating temperature for a
     * Hive heating zone.
     */
    public static final String CHANNEL_TEMPERATURE_TARGET = "temperature-target";

    /**
     * Name of the channel that represents the target heating temperature for a
     * Hive heating zone when the transient override (boost) is active.
     */
    public static final String CHANNEL_TEMPERATURE_TARGET_BOOST = "temperature-target-boost";

    /**
     * Name of the channel that represents the current override mode of a Hive
     * device (e.g. NONE, TRANSIENT)
     */
    public static final String CHANNEL_MODE_OPERATING_OVERRIDE = "mode-operating-override";

    /**
     * Name of the channel that represents how long a transient override
     * (boost) should be in minutes.
     */
    public static final String CHANNEL_TRANSIENT_DURATION = "transient-duration";

    /**
     * Name of the channel that represents how long a transient override
     * (boost) has left in minutes.
     */
    public static final String CHANNEL_TRANSIENT_REMAINING = "transient-remaining";

    /**
     * Name of the channel that represents if transient override feature is
     * enabled for a Hive device.
     *
     * <p>
     *     N.B. This is just if the feature is enabled/disabled, not if the
     *     override is currently active.
     * </p>
     */
    public static final String CHANNEL_TRANSIENT_ENABLED = "transient-enabled";

    /**
     * Name of the channel that represents the last time a transient override
     * (boost) was activated.
     */
    public static final String CHANNEL_TRANSIENT_START_TIME = "transient-start_time";

    /**
     * Name of the channel that represents the last time a transient override
     * (boost) was scheduled to end.
     */
    public static final String CHANNEL_TRANSIENT_END_TIME = "transient-end_time";

    /**
     * Name of the channel that represents the average Link Quality Indicator
     * for a wireless Hive device (seems to be from 0-100).
     */
    public static final String CHANNEL_RADIO_LQI_AVERAGE = "radio-lqi-average";

    /**
     * Name of the channel that represents the last known Link Quality Indicator
     * for a wireless Hive device (seems to be from 0-100).
     */
    public static final String CHANNEL_RADIO_LQI_LAST_KNOWN = "radio-lqi-last_known";

    /**
     * Name of the channel that represents the average Received Signal Strength
     * Indicator for a wireless Hive device.
     */
    public static final String CHANNEL_RADIO_RSSI_AVERAGE = "radio-rssi-average";

    /**
     * Name of the channel that represents the last known Received Signal
     * Strength Indicator for a wireless Hive device.
     */
    public static final String CHANNEL_RADIO_RSSI_LAST_KNOWN = "radio-rssi-last_known";

    /**
     * Name of the channel that represents the last time a HiveAccount thing
     * polled the Hive API.
     */
    public static final String CHANNEL_LAST_POLL_TIMESTAMP = "last_poll_timestamp";

    /**
     * Name of the channel used to trigger a dumping all the node information
     * for the linked Hive Account.
     */
    public static final String CHANNEL_DUMP_NODES = "dump_nodes";


    /* ######## Config params ######## */
    /**
     * The configuration key for storing the
     * {@link org.openhab.binding.hive.internal.client.NodeId}
     * of the {@link org.openhab.binding.hive.internal.client.Node} that a
     * {@linkplain org.eclipse.smarthome.core.thing.Thing} is representing.
     */
    public static final String CONFIG_NODE_ID = "nodeId";


    /* ######## Other constants ######## */
    public static final String HEATING_EASY_MODE_OPERATING_MANUAL = "MANUAL";
    public static final String HEATING_EASY_MODE_OPERATING_SCHEDULE = "SCHEDULE";
    public static final String HEATING_EASY_MODE_OPERATING_OFF = "OFF";

    public static final String HOT_WATER_EASY_MODE_OPERATING_ON = "ON";
    public static final String HOT_WATER_EASY_MODE_OPERATING_SCHEDULE = "SCHEDULE";
    public static final String HOT_WATER_EASY_MODE_OPERATING_OFF = "OFF";

    public static final String PROPERTY_EUI64 = "EUI64";

    private HiveBindingConstants() {
        throw new AssertionError();
    }
}
