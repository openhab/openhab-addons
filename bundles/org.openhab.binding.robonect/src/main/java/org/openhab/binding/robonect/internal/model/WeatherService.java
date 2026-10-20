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

    public @Nullable Boolean getEnable() {
        return enable;
    }

    public @Nullable Integer getInterval() {
        return interval;
    }

    public @Nullable WeatherConfig getConfig() {
        return config;
    }

    public @Nullable WeatherLocation getLocation() {
        return location;
    }
}
