/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergydbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VictronEnergyDBusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
@NonNullByDefault
public class VictronEnergyDBusBindingConstants {

    private static final String BINDING_ID = "victronenergydbus";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOLARCHARGER = new ThingTypeUID(BINDING_ID, "sc");

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_STATE_STRING = "stateStr";
    public static final String CHANNEL_DC_VOLTAGE = "DcV";
    public static final String CHANNEL_DC_CURRENT = "DcI";
    public static final String CHANNEL_PV_VOLTAGE = "PvV";
    public static final String CHANNEL_PV_CURRENT = "PvI";
    public static final String CHANNEL_YIELD_POWER = "YP";
    public static final String CHANNEL_YIELD_USER = "YU";
    public static final String CHANNEL_YIELD_SYSTEM = "YS";
    public static final String CHANNEL_YIELD_TODAY = "YT";
    public static final String CHANNEL_MAXIMUM_POWER_TODAY = "MPT";
    public static final String CHANNEL_TIME_IN_FLOAT_TODAY = "TIFT";
    public static final String CHANNEL_TIME_IN_ABSORPTION_TODAY = "TIAT";
    public static final String CHANNEL_TIME_IN_BULK_TODAY = "TIBT";
    public static final String CHANNEL_MAXIMUM_PV_VOLTAGE_TODAY = "MPVT";
    public static final String CHANNEL_MAXIMUM_BATTERY_CURRENT_TODAY = "MBCT";
    public static final String CHANNEL_MINIMUM_BATTERY_VOLTAGE_TODAY = "MinBVT";
    public static final String CHANNEL_MAXIMUM_BATTERY_VOLTAGE_TODAY = "MaxBVT";
    public static final String CHANNEL_YIELD_YESTERDAY = "YY";
    public static final String CHANNEL_MAXIMUM_POWER_YESTERDAY = "MPY";
    public static final String CHANNEL_TIME_IN_FLOAT_YESTERDAY = "TIFY";
    public static final String CHANNEL_TIME_IN_ABSORPTION_YESTERDAY = "TIAY";
    public static final String CHANNEL_TIME_IN_BULK_YESTERDAY = "TIBY";
    public static final String CHANNEL_MAXIMUM_PV_VOLTAGE_YESTERDAY = "MPVY";
    public static final String CHANNEL_MAXIMUM_BATTERY_CURRENT_YESTERDAY = "MBCY";
    public static final String CHANNEL_MINIMUM_BATTERY_VOLTAGE_YESTERDAY = "MinBVY";
    public static final String CHANNEL_MAXIMUM_BATTERY_VOLTAGE_YESTERDAY = "MaxBVY";
    public static final String CHANNEL_SERIAL = "Serial";
    public static final String CHANNEL_FIRMWARE_VERSION = "FwV";
    public static final String CHANNEL_PRODUCT_ID = "PId";
    public static final String CHANNEL_DEVICE_INSTANCE = "DI";
    public static final String CHANNEL_PRODUCTNAME = "Pn";
    public static final String CHANNEL_ERROR = "Err";
    public static final String CHANNEL_ERROR_STRING = "ErrStr";

    // Config Params
    public static final String PORT = "port";
}
