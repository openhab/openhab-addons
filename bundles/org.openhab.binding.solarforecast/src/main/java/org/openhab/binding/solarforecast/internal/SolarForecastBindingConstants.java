/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.solarforecast.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarForecastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastBindingConstants {

    private static final String BINDING_ID = "solarforecast";

    public static final ThingTypeUID FORECAST_SOLAR_MULTI_STRING = new ThingTypeUID(BINDING_ID, "fs-site");
    public static final ThingTypeUID FORECAST_SOLAR_PART_STRING = new ThingTypeUID(BINDING_ID, "fs-plane");
    public static final ThingTypeUID SOLCAST_BRIDGE_STRING = new ThingTypeUID(BINDING_ID, "sc-site");
    public static final ThingTypeUID SOLCAST_PART_STRING = new ThingTypeUID(BINDING_ID, "sc-plane");
    public static final Set<ThingTypeUID> SUPPORTED_THING_SET = Set.of(FORECAST_SOLAR_MULTI_STRING,
            FORECAST_SOLAR_PART_STRING, SOLCAST_BRIDGE_STRING, SOLCAST_PART_STRING);

    public static final String CHANNEL_TODAY = "today";
    public static final String CHANNEL_ACTUAL = "actual";
    public static final String CHANNEL_REMAINING = "remaining";
    public static final String CHANNEL_DAY1 = "day1";
    public static final String CHANNEL_DAY1_LOW = "day1-low";
    public static final String CHANNEL_DAY1_HIGH = "day1-high";
    public static final String CHANNEL_DAY2 = "day2";
    public static final String CHANNEL_DAY2_LOW = "day2-low";
    public static final String CHANNEL_DAY2_HIGH = "day2-high";
    public static final String CHANNEL_DAY3 = "day3";
    public static final String CHANNEL_DAY3_LOW = "day3-low";
    public static final String CHANNEL_DAY3_HIGH = "day3-high";
    public static final String CHANNEL_DAY4 = "day4";
    public static final String CHANNEL_DAY4_LOW = "day4-low";
    public static final String CHANNEL_DAY4_HIGH = "day4-high";
    public static final String CHANNEL_DAY5 = "day5";
    public static final String CHANNEL_DAY5_LOW = "day5-low";
    public static final String CHANNEL_DAY5_HIGH = "day5-high";
    public static final String CHANNEL_DAY6 = "day6";
    public static final String CHANNEL_DAY6_LOW = "day6-low";
    public static final String CHANNEL_DAY6_HIGH = "day6-high";

    public static final String CHANNEL_RAW = "raw";
    public static final String CHANNEL_RAW_TUNING = "raw-tuning";

    public static final String AUTODETECT = "auto-detect";
    public static final String SLASH = "/";
    public static final String EMPTY = "";
}
