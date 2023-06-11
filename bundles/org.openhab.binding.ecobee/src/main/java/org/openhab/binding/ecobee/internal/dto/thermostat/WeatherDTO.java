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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.util.Date;
import java.util.List;

/**
 * The {@link WeatherDTO} contains the weather and forecast information for the thermostat's location.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class WeatherDTO {

    /*
     * The time stamp in UTC of the weather forecast
     */
    public Date timestamp;

    /*
     * The weather station identifier
     */
    public String weatherStation;

    /*
     * The list of latest weather station forecasts
     */
    public List<WeatherForecastDTO> forecasts;
}
