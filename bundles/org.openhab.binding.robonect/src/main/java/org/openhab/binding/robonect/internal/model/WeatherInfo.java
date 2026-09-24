/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.*;

/**
 * Response to a weather information request.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class WeatherInfo extends RobonectAnswer {
    private @Nullable Weather weather;

    public @Nullable Weather getWeather() {
        return weather;
    }
}
