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

import org.openhab.binding.openweathermap.internal.dto.airpollution.List;
import org.openhab.binding.openweathermap.internal.dto.base.Coord;

/**
 * Holds the data from the deserialised JSON response of the <a href="https://openweathermap.org/api/air-pollution">Air
 * Pollution API</a>.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class OpenWeatherMapJsonAirPollutionData {
    public Coord coord;
    public java.util.List<List> list;
}
