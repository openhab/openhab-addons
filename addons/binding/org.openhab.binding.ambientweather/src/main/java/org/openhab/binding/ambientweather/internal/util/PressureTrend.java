/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link PressureTrend} is responsible for determining the 3 hour
 * barometric pressure trend. All calculations are in inches of Mercury.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class PressureTrend extends SlidingTimeWindow<Double> {
    // Pressure trend is established after 3 hours
    private static final long PRESSURE_TREND_PERIOD = 1000 * 60 * 60 * 3;

    // Thresholds used to determine pressure trends
    private static final double RAPIDLY_THRESHOLD = 0.06;
    private static final double STEADY_THRESHOLD = 0.02;

    // Pressure trends
    private static final StringType RISING_RAPIDLY = new StringType("RISING RAPIDLY");
    private static final StringType RISING = new StringType("RISING");
    private static final StringType FALLING_RAPIDLY = new StringType("FALLING RAPIDLY");
    private static final StringType FALLING = new StringType("FALLING");
    private static final StringType STEADY = new StringType("STEADY");
    private static final StringType UNKNOWN = new StringType("UNKNOWN");

    public PressureTrend() {
        super(PRESSURE_TREND_PERIOD);
    }

    public StringType getPressureTrend() {
        long firstTime;
        long lastTime;
        double firstValue;
        double lastValue;

        synchronized (storage) {
            firstTime = storage.firstKey();
            lastTime = storage.lastKey();
            firstValue = storage.get(storage.firstKey()).doubleValue();
            lastValue = storage.get(storage.lastKey()).doubleValue();
        }
        if (lastTime - firstTime < period * 0.99) {
            // Not with 1% of time period
            return UNKNOWN;
        }

        double pressureDifference = lastValue - firstValue;
        StringType pressureTrend;
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
