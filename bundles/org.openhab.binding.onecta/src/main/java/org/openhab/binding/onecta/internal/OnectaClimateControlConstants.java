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
package org.openhab.binding.onecta.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OnectaClimateControlConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaClimateControlConstants {

    // List of all Channel ids
    public static final String CHANNEL_AC_TEMP = "basic#setpoint-temp";
    public static final String CHANNEL_AC_TEMPMIN = "basic#setpoint-temp-min";
    public static final String CHANNEL_AC_TEMPMAX = "basic#setpoint-temp-max";
    public static final String CHANNEL_AC_TEMPSTEP = "basic#setpoint-temp-step";
    public static final String CHANNEL_INDOOR_TEMP = "basic#indoor-temp";
    public static final String CHANNEL_LEAVINGWATER_TEMP = "basic#leaving-water-temp";
    public static final String CHANNEL_OUTDOOR_TEMP = "basic#outdoor-temp";
    public static final String CHANNEL_INDOOR_HUMIDITY = "basic#humidity";
    public static final String CHANNEL_AC_POWER = "basic#power";
    public static final String CHANNEL_AC_POWERFULMODE = "basic#powerful-mode";
    public static final String CHANNEL_AC_OPERATIONMODE = "basic#operation-mode";
    public static final String CHANNEL_AC_NAME = "basic#name";
    public static final String CHANNEL_AC_FANSPEED = "basic#fan-speed";
    public static final String CHANNEL_AC_FANMOVEMENT_HOR = "basic#fan-dir-hor";
    public static final String CHANNEL_AC_FANMOVEMENT_VER = "basic#fan-dir-ver";
    public static final String CHANNEL_AC_FANMOVEMENT = "basic#fan-dir";
    public static final String CHANNEL_AC_ECONOMODE = "basic#econo-mode";
    public static final String CHANNEL_AC_STREAMER = "basic#streamer";
    public static final String CHANNEL_AC_HOLIDAYMODE = "basic#holiday-mode";
    public static final String CHANNEL_AC_SETPOINT_LEAVINGWATER_OFFSET = "basic#set-leaving-water-offset";
    public static final String CHANNEL_AC_SETPOINT_LEAVINGWATER_TEMP = "basic#set-leaving-water-temp";
    public static final String CHANNEL_AC_TIMESTAMP = "basic#timestamp";
    public static final String CHANNEL_AC_ENERGY_COOLING_DAY = "consumption-data-cooling#energy-cooling-day-%s";
    public static final String CHANNEL_AC_ENERGY_COOLING_WEEK = "consumption-data-cooling#energy-cooling-week-%s";
    public static final String CHANNEL_AC_ENERGY_COOLING_MONTH = "consumption-data-cooling#energy-cooling-month-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_DAY = "consumption-data-heating#energy-heating-day-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_WEEK = "consumption-data-heating#energy-heating-week-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_MONTH = "consumption-data-heating#energy-heating-month-%s";
    public static final String CHANNEL_AC_ENERGY_HEATING_CURRENT_DAY = "consumption-data-heating#energy-heating-current-day";
    public static final String CHANNEL_AC_ENERGY_HEATING_CURRENT_YEAR = "consumption-data-heating#energy-heating-current-year";
    public static final String CHANNEL_AC_ENERGY_COOLING_CURRENT_DAY = "consumption-data-cooling#energy-cooling-current-day";
    public static final String CHANNEL_AC_ENERGY_COOLING_CURRENT_YEAR = "consumption-data-cooling#energy-cooling-current-year";
    public static final String PROPERTY_AC_NAME = "name";
}
