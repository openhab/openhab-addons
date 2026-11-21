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
 * Mowing restrictions configured for the Robonect weather service.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherConfig {
    private @Nullable Integer maxrain;
    private @Nullable Integer mintemp;
    private @Nullable Integer maxtemp;
    private @Nullable Integer minhumidity;
    private @Nullable Integer maxhumidity;
    private @Nullable Boolean dontmowduringday;
    private @Nullable Boolean dontmowduringnight;

    /**
     * Returns the maximum rain intensity in percent.
     *
     * @return the maximum rain intensity in percent, or {@code null} if not reported
     */
    public @Nullable Integer getMaxrain() {
        return maxrain;
    }

    /**
     * Returns the minimum temperature in degrees Celsius.
     *
     * @return the minimum temperature in degrees Celsius, or {@code null} if not reported
     */
    public @Nullable Integer getMintemp() {
        return mintemp;
    }

    /**
     * Returns the maximum temperature in degrees Celsius.
     *
     * @return the maximum temperature in degrees Celsius, or {@code null} if not reported
     */
    public @Nullable Integer getMaxtemp() {
        return maxtemp;
    }

    /**
     * Returns the minimum humidity in percent.
     *
     * @return the minimum humidity in percent, or {@code null} if not reported
     */
    public @Nullable Integer getMinhumidity() {
        return minhumidity;
    }

    /**
     * Returns the maximum humidity in percent.
     *
     * @return the maximum humidity in percent, or {@code null} if not reported
     */
    public @Nullable Integer getMaxhumidity() {
        return maxhumidity;
    }

    /**
     * Reports whether mowing is prevented during the day.
     *
     * @return whether mowing is prevented during the day, or {@code null} if not reported
     */
    public @Nullable Boolean getDontmowduringday() {
        return dontmowduringday;
    }

    /**
     * Reports whether mowing is prevented during the night.
     *
     * @return whether mowing is prevented during the night, or {@code null} if not reported
     */
    public @Nullable Boolean getDontmowduringnight() {
        return dontmowduringnight;
    }
}
