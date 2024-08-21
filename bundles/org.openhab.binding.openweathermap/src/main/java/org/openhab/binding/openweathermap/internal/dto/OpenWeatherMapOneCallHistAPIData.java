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
package org.openhab.binding.openweathermap.internal.dto;

import java.util.List;

import org.openhab.binding.openweathermap.internal.dto.onecall.Current;
import org.openhab.binding.openweathermap.internal.dto.onecallhist.Hourly;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the historical data from the deserialised JSON response of the One Call APIs.
 * See <a href="https://openweathermap.org/api/one-call-3">One Call API 3.0.</a> and
 * <a href="https://openweathermap.org/api/one-call-api">One Call API 2.5</a>.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class OpenWeatherMapOneCallHistAPIData {
    private double lat;
    private double lon;
    private String timezone;
    @SerializedName("timezone_offset")
    private int timezoneOffset;
    private Current current;
    private List<Hourly> hourly = null;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(int timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }
}
