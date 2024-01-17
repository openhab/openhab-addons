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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Class for the weather forecast used by the energy manager to predict the produced power
 * and plan the battery charging.
 *
 * This fields have been identified to exist:
 * com.solarwatt.devices.forecast.Forecast=[
 * CorrectionFactors,
 * DiffuseCorrectionFactors,
 * DateNextYieldTrendScheduler,
 * ModePreventPVForecast,
 * WeatherForecast,
 * ForecastProperties,
 * DateNextConsumptionTrendScheduler,
 * DateNextFactorScheduler,
 * WeatherAPIKey,
 * WeatherHistory
 * ]
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class Forecast extends Device {
    public static final String SOLAR_WATT_CLASSNAME = "com.solarwatt.devices.forecast.Forecast";

    public Forecast(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "Forecast";
    }
}
