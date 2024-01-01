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
 * The {@link OpenWeatherMapJsonDailyForecastData} is the Java class used to map the JSON response to an OpenWeatherMap
 * request.
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

    public void setCity(City city) {
        this.city = city;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public Double getMessage() {
        return message;
    }

    public void setMessage(Double message) {
        this.message = message;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public java.util.List<List> getList() {
        return list;
    }

    public void setList(java.util.List<List> list) {
        this.list = list;
    }
}
