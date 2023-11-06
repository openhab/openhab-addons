/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.weatherunderground.internal.json;

import java.net.URL;

/**
 * The {@link WeatherUndergroundJsonLocation} is the Java class used
 * to map the entry "location" from the JSON response to a Weather
 * Underground request.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class WeatherUndergroundJsonLocation {

    // Commented members indicate properties returned by the API not used by the binding

    private String type;
    private String country;
    private String country_iso3166;
    private String country_name;
    private String state;
    private String city;
    private String tz_short;
    private String tz_long;
    private String lat;
    private String lon;
    private String zip;
    private String magic;
    private String wmo;
    private String l;
    private String requesturl;
    private String wuiurl;
    // private Object nearby_weather_stations;

    public WeatherUndergroundJsonLocation() {
    }

    public String getType() {
        return type;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryIso3166() {
        return country_iso3166;
    }

    public String getCountryName() {
        return country_name;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getTzShort() {
        return tz_short;
    }

    public String getTzLong() {
        return tz_long;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getZip() {
        return zip;
    }

    public String getMagic() {
        return magic;
    }

    public String getWmo() {
        return wmo;
    }

    public String getL() {
        return l;
    }

    public URL getRequesturl() {
        return WeatherUndergroundJsonUtils.getValidUrl(requesturl);
    }

    public URL getWuiurl() {
        return WeatherUndergroundJsonUtils.getValidUrl(wuiurl);
    }
}
