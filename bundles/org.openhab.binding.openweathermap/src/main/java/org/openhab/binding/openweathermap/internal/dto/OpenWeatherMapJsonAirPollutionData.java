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
package org.openhab.binding.openweathermap.internal.dto;

import org.openhab.binding.openweathermap.internal.dto.base.Coord;

/**
 * The {@link OpenWeatherMapJsonAirPollutionData} is the Java class used to map the JSON response to an OpenWeatherMap
 * request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class OpenWeatherMapJsonAirPollutionData {
    public Coord coord;
    public java.util.List<org.openhab.binding.openweathermap.internal.dto.airpollution.List> list;
}
