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

import org.openhab.binding.openweathermap.internal.dto.base.City;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.List;

/**
 * Holds the data from the deserialised JSON response of the <a href="https://openweathermap.org/forecast16">Daily
 * Forecast 16 Days API</a>.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class OpenWeatherMapJsonDailyForecastData {
    private City city;
    private String cod;
    private Double message;
    private Integer cnt;
    private java.util.List<List> list;

    public City getCity() {
        return city;
    }

    public String getCod() {
        return cod;
    }

    public Double getMessage() {
        return message;
    }

    public Integer getCnt() {
        return cnt;
    }

    public java.util.List<List> getList() {
        return list;
    }
}
