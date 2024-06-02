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
package org.openhab.binding.valloxmv.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ValloxMVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class ValloxMVBindingConstants {

    private static final String BINDING_ID = "valloxmv";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VALLOXMV = new ThingTypeUID(BINDING_ID, "valloxmv");

    // 4 states of ventilation unit (Fireplace = 1, Away = 2, At home = 3, Boost = 4)
    public static final int STATE_FIREPLACE = 1;
    public static final int STATE_AWAY = 2;
    public static final int STATE_ATHOME = 3;
    public static final int STATE_BOOST = 4;

    // List of all Channel ids
    /**
     * Ventilation unit powered on
     */
    public static final String CHANNEL_ONOFF = "onoff";

    /**
     * Current state ventilation unit (Fireplace = 1, Away = 2, At home = 3, Boost = 4)
     */
    public static final String CHANNEL_STATE = "state";

    /**
     * Current fan speed (0 - 100)
     */
    public static final String CHANNEL_FAN_SPEED = "fanspeed";

    /**
     * Current fan speed of extracting fan (1/min)
     */
    public static final String CHANNEL_FAN_SPEED_EXTRACT = "fanspeedextract";

    /**
     * Current fan speed of supplying fan (1/min)
     */
    public static final String CHANNEL_FAN_SPEED_SUPPLY = "fanspeedsupply";

    /**
     * Current temperature inside the building
     */
    public static final String CHANNEL_TEMPERATURE_INSIDE = "tempinside";

    /**
     * Current temperature outside the building
     */
    public static final String CHANNEL_TEMPERATURE_OUTSIDE = "tempoutside";

    /**
     * Current temperature of the air flow exhausting the building.
     */
    public static final String CHANNEL_TEMPERATURE_EXHAUST = "tempexhaust";

    /**
     * Current temperature of the air flow incoming to the building before heating (if optional heating module included
     * in ventilation unit).
     */
    public static final String CHANNEL_TEMPERATURE_INCOMING_BEFORE_HEATING = "tempincomingbeforeheating";

    /**
     * Current temperature of the air flow incoming to the building.
     */
    public static final String CHANNEL_TEMPERATURE_INCOMING = "tempincoming";

    /**
     * Current humidity of the air flow exhausting the building.
     */
    public static final String CHANNEL_HUMIDITY = "humidity";

    /**
     * Current CO2 of the air flow exhausting the building.
     */
    public static final String CHANNEL_CO2 = "co2";

    /**
     * Current cell state (0=heat recovery, 1=cool recovery, 2=bypass, 3=defrosting).
     */
    public static final String CHANNEL_CELLSTATE = "cellstate";

    /**
     * Total uptime in years (+ uptime in hours = total uptime).
     */
    public static final String CHANNEL_UPTIME_YEARS = "uptimeyears";

    /**
     * Total uptime in hours (+ uptime in years = total uptime).
     */
    public static final String CHANNEL_UPTIME_HOURS = "uptimehours";

    /**
     * Current uptime in hours.
     */
    public static final String CHANNEL_UPTIME_HOURS_CURRENT = "uptimehourscurrent";

    /**
     * Date filter was changed last time.
     */
    public static final String CHANNEL_FILTER_CHANGED_DATE = "filterchangeddate";

    /**
     * Days until filter has to be changed.
     */
    public static final String CHANNEL_REMAINING_FILTER_DAYS = "remainingfilterdays";

    /**
     * Extract fan base speed in % (0-100).
     */
    public static final String CHANNEL_EXTR_FAN_BALANCE_BASE = "extrfanbalancebase";

    /**
     * Supply fan base speed in % (0-100).
     */
    public static final String CHANNEL_SUPP_FAN_BALANCE_BASE = "suppfanbalancebase";

    /**
     * Home fan speed in % (0-100).
     */
    public static final String CHANNEL_HOME_SPEED_SETTING = "homespeedsetting";

    /**
     * Away fan speed in % (0-100).
     */
    public static final String CHANNEL_AWAY_SPEED_SETTING = "awayspeedsetting";

    /**
     * Boost fan speed in % (0-100).
     */
    public static final String CHANNEL_BOOST_SPEED_SETTING = "boostspeedsetting";

    /**
     * Target temperature in home state.
     */
    public static final String CHANNEL_HOME_AIR_TEMP_TARGET = "homeairtemptarget";

    /**
     * Target temperature in away state.
     */
    public static final String CHANNEL_AWAY_AIR_TEMP_TARGET = "awayairtemptarget";

    /**
     * Target temperature in boost state.
     */
    public static final String CHANNEL_BOOST_AIR_TEMP_TARGET = "boostairtemptarget";

    /**
     * Timer value setting in minutes of boost profile (1-65535).
     */
    public static final String CHANNEL_BOOST_TIME = "boosttime";

    /**
     * Timer enabled setting in boost profile (Enabled = 1, Disabled = 0).
     */
    public static final String CHANNEL_BOOST_TIMER_ENABLED = "boosttimerenabled";

    /**
     * Fireplace profile extract fan speed setting in % (0-100).
     */
    public static final String CHANNEL_FIREPLACE_EXTR_FAN = "fireplaceextrfan";

    /**
     * Fireplace profile supply fan speed setting in % (0-100).
     */
    public static final String CHANNEL_FIREPLACE_SUPP_FAN = "fireplacesuppfan";

    /**
     * Timer value setting in minutes of fireplace profile (1-65535).
     */
    public static final String CHANNEL_FIREPLACE_TIME = "fireplacetime";

    /**
     * Timer enabled setting in fireplace profile (Enabled = 1, Disabled = 0).
     */
    public static final String CHANNEL_FIREPLACE_TIMER_ENABLED = "fireplacetimerenabled";

    /**
     * Programmable profile enabled
     * Not sure if this is needed at all, Vallox modbus document does not list this.
     */
    // public static final String CHANNEL_EXTRA_ENABLED = "extraenabled";

    /**
     * Target temperature in programmable profile.
     */
    public static final String CHANNEL_EXTRA_AIR_TEMP_TARGET = "extraairtemptarget";

    /**
     * Programmable profile extract fan speed setting in % (0-100).
     */
    public static final String CHANNEL_EXTRA_EXTR_FAN = "extraextrfan";

    /**
     * Programmable profile supply fan speed setting in % (0-100).
     */
    public static final String CHANNEL_EXTRA_SUPP_FAN = "extrasuppfan";

    /**
     * Timer value setting in minutes of programmable profile (1-65535).
     */
    public static final String CHANNEL_EXTRA_TIME = "extratime";

    /**
     * Timer enabled setting in programmable profile (Enabled = 1, Disabled = 0).
     */
    public static final String CHANNEL_EXTRA_TIMER_ENABLED = "extratimerenabled";

    /**
     * Weekly Timer enabled setting (Enabled = 1, Disabled = 0).
     */
    public static final String CHANNEL_WEEKLY_TIMER_ENABLED = "weeklytimerenabled";

    /**
     * Set of writable channels that are Switches
     */
    public static final Set<String> WRITABLE_CHANNELS_SWITCHES = Set.of(CHANNEL_ONOFF, CHANNEL_BOOST_TIMER_ENABLED,
            CHANNEL_FIREPLACE_TIMER_ENABLED, CHANNEL_EXTRA_TIMER_ENABLED, CHANNEL_WEEKLY_TIMER_ENABLED);

    /**
     *
     * Set of writable channels that are dimensionless
     */
    public static final Set<String> WRITABLE_CHANNELS_DIMENSIONLESS = Set.of(CHANNEL_EXTR_FAN_BALANCE_BASE,
            CHANNEL_SUPP_FAN_BALANCE_BASE, CHANNEL_HOME_SPEED_SETTING, CHANNEL_AWAY_SPEED_SETTING,
            CHANNEL_BOOST_SPEED_SETTING, CHANNEL_BOOST_TIME, CHANNEL_BOOST_TIMER_ENABLED, CHANNEL_FIREPLACE_EXTR_FAN,
            CHANNEL_FIREPLACE_SUPP_FAN, CHANNEL_FIREPLACE_TIME, CHANNEL_FIREPLACE_TIMER_ENABLED, CHANNEL_EXTRA_EXTR_FAN,
            CHANNEL_EXTRA_SUPP_FAN, CHANNEL_EXTRA_TIME, CHANNEL_EXTRA_TIMER_ENABLED, CHANNEL_WEEKLY_TIMER_ENABLED);

    /**
     * Set of writable channels that are temperatures
     */
    public static final Set<String> WRITABLE_CHANNELS_TEMPERATURE = Set.of(CHANNEL_HOME_AIR_TEMP_TARGET,
            CHANNEL_AWAY_AIR_TEMP_TARGET, CHANNEL_BOOST_AIR_TEMP_TARGET, CHANNEL_EXTRA_AIR_TEMP_TARGET);

    // Thing configuration
    /**
     * Name of the configuration parameters
     */
    public static final String CONFIG_UPDATE_INTERVAL = "updateinterval";
    public static final String CONFIG_IP = "ip";
}
