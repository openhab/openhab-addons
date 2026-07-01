/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.openweathermap.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenWeatherMapCombinedForecastConfiguration} is the class used to match the
 * {@link org.openhab.binding.openweathermap.internal.handler.OpenWeatherMapCombinedForecastHandler}
 * configuration.
 *
 * <p>
 * The {@code forecastResolution} parameter controls which API tiers are fetched and therefore
 * the granularity of the unified forecast TimeSeries:
 * <ul>
 * <li>{@code hourly} — hourly (0–48 h) + 3-hourly (48–120 h) — default</li>
 * <li>{@code 3hourly} — 3-hourly only (0–120 h), free-tier Forecast5 API, no One Call needed</li>
 * </ul>
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class OpenWeatherMapCombinedForecastConfiguration {

    /** Geographic location as {@code lat,lon} string, e.g. {@code "48.1374,11.5755"}. */
    public @NonNullByDefault({}) String location;

    /**
     * Forecast resolution: {@code "hourly"} (default) or {@code "3hourly"}.
     */
    public String forecastResolution = "hourly";
}
