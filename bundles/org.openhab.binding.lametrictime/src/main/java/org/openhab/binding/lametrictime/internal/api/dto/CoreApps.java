/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.dto;

/**
 * Class for managing the core apps.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class CoreApps {
    private static final ClockApp CLOCK = new ClockApp();
    private static final CountdownApp COUNTDOWN = new CountdownApp();
    private static final RadioApp RADIO = new RadioApp();
    private static final StopwatchApp STOPWATCH = new StopwatchApp();
    private static final WeatherApp WEATHER = new WeatherApp();

    public static ClockApp clock() {
        return CLOCK;
    }

    public static CountdownApp countdown() {
        return COUNTDOWN;
    }

    public static RadioApp radio() {
        return RADIO;
    }

    public static StopwatchApp stopwatch() {
        return STOPWATCH;
    }

    public static WeatherApp weather() {
        return WEATHER;
    }

    // @formatter:off
    private CoreApps() {}
    // @formatter:on
}
