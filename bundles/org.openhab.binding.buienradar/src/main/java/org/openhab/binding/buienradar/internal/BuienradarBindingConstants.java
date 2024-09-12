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
package org.openhab.binding.buienradar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BuienradarBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public class BuienradarBindingConstants {

    private static final String BINDING_ID = "buienradar";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RAIN_FORECAST = new ThingTypeUID(BINDING_ID, "rain_forecast");

    public static final String ACTUAL_DATETIME = "actual_datetime";
    public static final String FORECAST_0 = "forecast_0";
    public static final String FORECAST_5 = "forecast_5";
    public static final String FORECAST_10 = "forecast_10";
    public static final String FORECAST_15 = "forecast_15";
    public static final String FORECAST_20 = "forecast_20";
    public static final String FORECAST_25 = "forecast_25";
    public static final String FORECAST_30 = "forecast_30";
    public static final String FORECAST_35 = "forecast_35";
    public static final String FORECAST_40 = "forecast_40";
    public static final String FORECAST_45 = "forecast_45";
    public static final String FORECAST_50 = "forecast_50";
    public static final String FORECAST_55 = "forecast_55";
    public static final String FORECAST_60 = "forecast_60";
    public static final String FORECAST_65 = "forecast_65";
    public static final String FORECAST_70 = "forecast_70";
    public static final String FORECAST_75 = "forecast_75";
    public static final String FORECAST_80 = "forecast_80";
    public static final String FORECAST_85 = "forecast_85";
    public static final String FORECAST_90 = "forecast_90";
    public static final String FORECAST_95 = "forecast_95";
    public static final String FORECAST_100 = "forecast_100";
    public static final String FORECAST_105 = "forecast_105";
    public static final String FORECAST_110 = "forecast_110";
    public static final String FORECAST_115 = "forecast_115";
}
