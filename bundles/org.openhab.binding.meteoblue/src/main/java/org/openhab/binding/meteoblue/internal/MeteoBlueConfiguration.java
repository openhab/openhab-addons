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
package org.openhab.binding.meteoblue.internal;

/**
 * Model for the meteoblue binding configuration.
 *
 * @author Chris Carman - Initial contribution
 */
public class MeteoBlueConfiguration {

    // default values
    public static final int DEFAULT_REFRESH = 240;

    // constants
    public static final String SERVICETYPE_COMM = "Commercial";
    public static final String SERVICETYPE_NONCOMM = "NonCommercial";
    public static final String COMM_BASE_URL = "http://my.meteoblue.com/dataApi/dispatch.pl?type=json_7day_3h_firstday&";
    public static final String NONCOMM_BASE_URL = "http://my.meteoblue.com/packages/basic-day?";
    public static final String URL_MINIMAL_PARAMS = "apikey=#API_KEY#&lat=#LATITUDE#&lon=#LONGITUDE#";

    // required parameters
    // servicetype - either Commercial or NonCommercial
    public String serviceType;
    // location - lat., long., and alt. in a single string
    public String location;

    // optional parameters
    // refresh - time period in minutes between pulls
    public Integer refresh;
    // latitude - the latitude of this location in degrees (-90 to 90)
    public Double latitude;
    // longitude - the longitude of this location in degrees (-180 to 180)
    public Double longitude;
    // altitude - the height above sea level of the location, in meters
    public Double altitude;
    // timeZone - the timezone of the location (see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)
    public String timeZone;

    // returns the URL for the specified serviceType
    public static String getURL(String serviceType) {
        if (SERVICETYPE_COMM.equals(serviceType)) {
            return COMM_BASE_URL + URL_MINIMAL_PARAMS + "#FORMAT_PARAMS#";
        } else {
            return NONCOMM_BASE_URL + URL_MINIMAL_PARAMS + "#FORMAT_PARAMS#";
        }
    }

    public void parseLocation() {
        String[] split = location.split(",");
        String a1 = split.length > 0 ? split[0] : null;
        String a2 = split.length > 1 ? split[1] : null;
        String a3 = split.length > 2 ? split[2] : null;

        if (a1 != null && !a1.isBlank()) {
            latitude = tryGetDouble(a1);
        }

        if (a2 != null && !a2.isBlank()) {
            longitude = tryGetDouble(a2);
        }

        if (a3 != null && !a3.isBlank()) {
            altitude = tryGetDouble(a3);
        }
    }

    private Double tryGetDouble(String toParse) {
        try {
            return Double.parseDouble(toParse);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
