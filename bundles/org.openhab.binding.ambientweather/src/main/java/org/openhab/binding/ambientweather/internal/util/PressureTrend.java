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
package org.openhab.binding.ambientweather.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PressureTrend} is responsible for determining the 3 hour
 * barometric pressure trend. All calculations are in inches of Mercury.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class PressureTrend extends SlidingTimeWindow<Double> {
    private final Logger logger = LoggerFactory.getLogger(PressureTrend.class);

    // Pressure trend is established after 3 hours
    private static final long PRESSURE_TREND_PERIOD = 1000 * 60 * 60 * 3;

    // Thresholds used to determine pressure trends
    private static final double RAPIDLY_THRESHOLD = 0.06;
    private static final double STEADY_THRESHOLD = 0.02;

    // Pressure trends
    private static final String RISING_RAPIDLY = "RISING RAPIDLY";
    private static final String RISING = "RISING";
    private static final String FALLING_RAPIDLY = "FALLING RAPIDLY";
    private static final String FALLING = "FALLING";
    private static final String STEADY = "STEADY";
    private static final String UNKNOWN = "UNKNOWN";

    public PressureTrend() {
        super(PRESSURE_TREND_PERIOD);
    }

    public String getPressureTrend() {
        long firstTime;
        long lastTime;
        double firstValue;
        double lastValue;

        if (storage.isEmpty()) {
            return UNKNOWN;
        }

        synchronized (storage) {
            firstTime = storage.firstKey();
            lastTime = storage.lastKey();
            firstValue = storage.get(storage.firstKey()).doubleValue();
            lastValue = storage.get(storage.lastKey()).doubleValue();
        }
        if (lastTime - firstTime < period * 0.99) {
            // Not within 1% of time period
            return UNKNOWN;
        }

        double pressureDifference = lastValue - firstValue;
        String pressureTrend;
        if (pressureDifference > RAPIDLY_THRESHOLD) {
            pressureTrend = RISING_RAPIDLY;
        } else if (pressureDifference > STEADY_THRESHOLD && pressureDifference <= RAPIDLY_THRESHOLD) {
            pressureTrend = RISING;
        } else if (pressureDifference > -STEADY_THRESHOLD && pressureDifference <= STEADY_THRESHOLD) {
            pressureTrend = STEADY;
        } else if (pressureDifference > -RAPIDLY_THRESHOLD && pressureDifference <= -STEADY_THRESHOLD) {
            pressureTrend = FALLING;
        } else {
            pressureTrend = FALLING_RAPIDLY;
        }
        removeOldEntries();
        return pressureTrend;
    }
}
