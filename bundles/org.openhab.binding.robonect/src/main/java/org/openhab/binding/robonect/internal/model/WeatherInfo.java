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
 * Response to a weather information request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherInfo extends RobonectAnswer {
    private @Nullable Weather weather;
    private @Nullable WeatherService service;

    /**
     * Returns the weather measurements.
     *
     * @return the weather measurements, or {@code null} if not reported
     */
    public @Nullable Weather getWeather() {
        return weather;
    }

    /**
     * Returns the weather service settings.
     *
     * @return the weather service settings, or {@code null} if not reported
     */
    public @Nullable WeatherService getService() {
        return service;
    }
}
