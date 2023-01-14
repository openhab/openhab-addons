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
package org.openhab.binding.openweathermap.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OpenWeatherMapJsonUVIndexData} is the Java class used to map the JSON response to an OpenWeatherMap
 * request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class OpenWeatherMapJsonUVIndexData {
    private Double lat;
    private Double lon;
    @SerializedName("date_iso")
    private String dateIso;
    private Integer date;
    private Double value;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getDateIso() {
        return dateIso;
    }

    public void setDateIso(String dateIso) {
        this.dateIso = dateIso;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
