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
package org.openhab.binding.fmiweather.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Request for weather observations
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ObservationRequest extends Request {

    public static final String STORED_QUERY_ID = "fmi::observations::weather::multipointcoverage";

    // For description of variables, see http://opendata.fmi.fi/meta?observableProperty=observation
    public static final String PARAM_TEMPERATURE = "t2m";
    public static final String PARAM_HUMIDITY = "rh";
    public static final String PARAM_WIND_DIRECTION = "wd_10min";
    public static final String PARAM_WIND_SPEED = "ws_10min";
    public static final String PARAM_WIND_GUST = "wg_10min";
    public static final String PARAM_PRESSURE = "p_sea";
    public static final String PARAM_PRECIPITATION_AMOUNT = "r_1h";
    public static final String PARAM_SNOW_DEPTH = "snow_aws";
    public static final String PARAM_VISIBILITY = "vis";
    public static final String PARAM_CLOUDS = "n_man";
    public static final String PARAM_PRESENT_WEATHER = "wawa";

    public static final String[] PARAMETERS = new String[] { PARAM_TEMPERATURE, PARAM_HUMIDITY, PARAM_WIND_DIRECTION,
            PARAM_WIND_SPEED, PARAM_WIND_GUST, PARAM_PRESSURE, PARAM_PRECIPITATION_AMOUNT, PARAM_SNOW_DEPTH,
            PARAM_VISIBILITY, PARAM_CLOUDS, PARAM_PRESENT_WEATHER };

    public ObservationRequest(QueryParameter location, long startEpoch, long endEpoch, long timestepMinutes) {
        super(STORED_QUERY_ID, location, startEpoch, endEpoch, timestepMinutes, PARAMETERS);
    }
}
