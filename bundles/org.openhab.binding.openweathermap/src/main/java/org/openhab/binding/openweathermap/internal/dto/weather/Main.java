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
package org.openhab.binding.openweathermap.internal.dto.weather;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>main</code> object of the JSON response of the Current weather data API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Main {
    private Double temp;
    @SerializedName("feels_like")
    private @Nullable Double feelsLikeTemp;
    private Double pressure;
    private Integer humidity;
    @SerializedName("temp_min")
    private Double tempMin;
    @SerializedName("temp_max")
    private Double tempMax;

    public Double getTemp() {
        return temp;
    }

    public @Nullable Double getFeelsLikeTemp() {
        return feelsLikeTemp;
    }

    public Double getPressure() {
        return pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public Double getTempMax() {
        return tempMax;
    }
}
