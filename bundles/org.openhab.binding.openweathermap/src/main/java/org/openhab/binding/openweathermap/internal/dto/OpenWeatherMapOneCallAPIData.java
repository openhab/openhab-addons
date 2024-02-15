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
 * Holds the data from the deserialised JSON response of the One Call APIs.
 * See <a href="https://openweathermap.org/api/one-call-3">One Call API 3.0.</a> and
 * <a href="https://openweathermap.org/api/one-call-api">One Call API 2.5</a>.
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

    public double getLon() {
        return lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getTimezoneOffset() {
        return timezoneOffset;
    }

    public Current getCurrent() {
        return current;
    }

    public List<Minutely> getMinutely() {
        return minutely;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public List<Daily> getDaily() {
        return daily;
    }
}
