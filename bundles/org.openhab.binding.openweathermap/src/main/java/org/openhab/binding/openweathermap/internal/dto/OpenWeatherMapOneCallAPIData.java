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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.dto.onecall.Alert;
import org.openhab.binding.openweathermap.internal.dto.onecall.Current;
import org.openhab.binding.openweathermap.internal.dto.onecall.Daily;
import org.openhab.binding.openweathermap.internal.dto.onecall.Hourly;
import org.openhab.binding.openweathermap.internal.dto.onecall.Minutely;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the deserialised JSON response. Created using http://www.jsonschema2pojo.org/.
 * Settings:
 * Annotation Style: GSON
 * Use primitive types
 * Use double numbers
 * allow additional properties
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Christoph Weitkamp - Added weather alerts
 */
public class OpenWeatherMapOneCallAPIData {
    private double lat;
    private double lon;
    private String timezone;
    @SerializedName("timezone_offset")
    private int timezoneOffset;
    private Current current;
    private List<Minutely> minutely;
    private List<Hourly> hourly;
    private List<Daily> daily;

    public @Nullable List<Alert> alerts;

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

    public List<Minutely> getMinutely() {
        return minutely;
    }

    public void setMinutely(List<Minutely> minutely) {
        this.minutely = minutely;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }

    public List<Daily> getDaily() {
        return daily;
    }

    public void setDaily(List<Daily> daily) {
        this.daily = daily;
    }
}
