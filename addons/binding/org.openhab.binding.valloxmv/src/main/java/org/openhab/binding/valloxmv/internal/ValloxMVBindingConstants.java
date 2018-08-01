/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ValloxMVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Björn Brings - Initial contribution
 */

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
     * arget temperature in home state.
     */
    public static final String CHANNEL_HOME_AIR_TEMP_TARGET = "homeairtemptarget";

    // Thing configuration
    /**
     * Name of the configuration parameters
     */
    public static final String CONFIG_UPDATE_INTERVAL = "updateinterval";
    public static final String CONFIG_IP = "ip";
}
