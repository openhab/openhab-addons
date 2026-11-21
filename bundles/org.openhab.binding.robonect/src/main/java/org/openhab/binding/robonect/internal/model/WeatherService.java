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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Weather-service configuration returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherService {
    private @Nullable Boolean enable;
    private @Nullable Integer interval;
    private @Nullable WeatherConfig config;
    private @Nullable WeatherLocation location;

    /**
     * Reports whether the weather service is enabled.
     *
     * @return whether the weather service is enabled, or {@code null} if not reported
     */
    public @Nullable Boolean getEnable() {
        return enable;
    }

    /**
     * Returns the weather update interval in minutes.
     *
     * @return the weather update interval in minutes, or {@code null} if not reported
     */
    public @Nullable Integer getInterval() {
        return interval;
    }

    /**
     * Returns the weather restriction settings.
     *
     * @return the weather restriction settings, or {@code null} if not reported
     */
    public @Nullable WeatherConfig getConfig() {
        return config;
    }

    /**
     * Returns the configured weather location.
     *
     * @return the configured weather location, or {@code null} if not reported
     */
    public @Nullable WeatherLocation getLocation() {
        return location;
    }
}
