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
package org.openhab.binding.openweathermap.internal.dto.forecast.daily;

/**
 * Holds the data from the <code>feels_like</code> object of the JSON response of the Daily Forecast 16 Days API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class FeelsLikeTemp {
    private Double day;
    private Double night;
    private Double eve;
    private Double morn;

    public Double getDay() {
        return day;
    }

    public Double getNight() {
        return night;
    }

    public Double getEve() {
        return eve;
    }

    public Double getMorn() {
        return morn;
    }
}
