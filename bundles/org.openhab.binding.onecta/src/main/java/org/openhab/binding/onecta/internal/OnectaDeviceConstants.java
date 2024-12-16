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
package org.openhab.binding.onecta.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaDeviceConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaDeviceConstants {

    // List of all Channel ids
    public static final String CHANNEL_AC_TEMP = "basic#settemp";
    public static final String CHANNEL_AC_TEMPMIN = "basic#settempmin";
    public static final String CHANNEL_AC_TEMPMAX = "basic#settempmax";
    public static final String CHANNEL_AC_TEMPSTEP = "basic#settempstep";
    public static final String CHANNEL_AC_TARGETTEMP = "basic#targettemp";
    public static final String CHANNEL_AC_TARGETTEMPMIN = "basic#targettempmin";
    public static final String CHANNEL_AC_TARGETTEMPMAX = "basic#targettempmax";
    public static final String CHANNEL_AC_TARGETTEMPSTEP = "basic#targettempstep";
    public static final String CHANNEL_INDOOR_TEMP = "basic#indoortemp";
    public static final String CHANNEL_LEAVINGWATER_TEMP = "basic#leavingwatertemp";
    public static final String CHANNEL_OUTDOOR_TEMP = "basic#outdoortemp";
    public static final String CHANNEL_INDOOR_HUMIDITY = "basic#humidity";
    public static final String CHANNEL_AC_POWER = "basic#power";
    public static final String CHANNEL_AC_POWERFULMODE = "basic#powerfulmode";
    public static final String CHANNEL_AC_RAWDATA = "extra#rawdata";
    public static final String CHANNEL_AC_OPERATIONMODE = "basic#operationmode";
    public static final String CHANNEL_AC_NAME = "basic#name";
    public static final String CHANNEL_AC_FANSPEED = "basic#fanspeed";
    public static final String CHANNEL_AC_FANMOVEMENT_HOR = "basic#fandirhor";
    public static final String CHANNEL_AC_FANMOVEMENT_VER = "basic#fandirver";
    public static final String CHANNEL_AC_FANMOVEMENT = "basic#fandir";
    public static final String CHANNEL_AC_ECONOMODE = "basic#economode";
    public static final String CHANNEL_AC_STREAMER = "basic#streamer";
    public static final String CHANNEL_AC_HOLIDAYMODE = "basic#holidaymode";
    public static final String CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET = "basic#setleavingwateroffset";
    public static final String CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP = "basic#setleavingwatertemp";
    public static final String CHANNEL_AC_TIMESTAMP = "basic#timestamp";
    public static final String CHANNEL_AC_DEMANDCONTROL = "demandcontrol#demandcontrol";
    public static final String CHANNEL_AC_DEMANDCONTROLFIXEDVALUE = "demandcontrol#demandcontrolfixedvalue";
    public static final String CHANNEL_AC_DEMANDCONTROLFIXEDSTEPVALUE = "demandcontrol#demandcontrolfixedstepvalue";
    public static final String CHANNEL_AC_DEMANDCONTROLFIXEDMINVALUE = "demandcontrol#demandcontrolfixedminvalue";
    public static final String CHANNEL_AC_DEMANDCONTROLFIXEDMAXVALUE = "demandcontrol#demandcontrolfixedmaxvalue";
    public static final String CHANNEL_AC_ENERGY_COOLING_DAY = "consumption-data-cooling#energycoolingday-%s";
    public static final String CHANNEL_AC_ENERGY_COOLING_WEEK = "consumption-data-cooling#energycoolingweek-%s";
    public static final String CHANNEL_AC_ENERGY_COOLING_MONTH = "consumption-data-cooling#energycoolingmonth-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_DAY = "consumption-data-heating#energyheatingday-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_WEEK = "consumption-data-heating#energyheatingweek-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_MONTH = "consumption-data-heating#energyheatingmonth-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_CURRENT_DAY = "consumption-data-heating#energyheatingcurrentday";
    public static final String CHANNEL_AC_ENERGY_HEATING_CURRENT_YEAR = "consumption-data-heating#energyheatingcurrentyear";
    public static final String CHANNEL_AC_ENERGY_COOLING_CURRENT_DAY = "consumption-data-cooling#energycoolingcurrentday";
    public static final String CHANNEL_AC_ENERGY_COOLING_CURRENT_YEAR = "consumption-data-cooling#energycoolingcurrentyear";
    public static final String PROPERTY_AC_NAME = "name";
}
