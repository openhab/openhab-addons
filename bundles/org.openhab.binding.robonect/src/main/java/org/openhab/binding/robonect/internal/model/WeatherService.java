/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/** Weather-service configuration returned by the Robonect API. */
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
