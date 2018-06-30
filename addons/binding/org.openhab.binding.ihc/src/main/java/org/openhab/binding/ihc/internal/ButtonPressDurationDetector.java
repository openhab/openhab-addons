/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle button press durations
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ButtonPressDurationDetector {
    private final Logger logger = LoggerFactory.getLogger(ButtonPressDurationDetector.class);

    private boolean shortPress;
    private boolean longPress;
    private boolean extraLongPress;
    private Duration duration;
    private long short_press_max_time;
    private long long_press_max_time;
    private long extra_long_press_max_time;

    public boolean isShortPress() {
        return shortPress;
    }

    public boolean isLongPress() {
        return longPress;
    }

    public boolean isExtraLongPress() {
        return extraLongPress;
    }

    public ButtonPressDurationDetector(Duration duration, long short_press_max_time, long long_press_max_time,
            long extra_long_press_max_time) {
        this.duration = duration;
        this.short_press_max_time = short_press_max_time;
        this.long_press_max_time = long_press_max_time;
        this.extra_long_press_max_time = extra_long_press_max_time;

        calculate();
    }

    private void calculate() {
        if (duration.toMillis() < 0) {
            logger.debug("Button press duration < 0ms");
        } else if (isBetween(duration.toMillis(), 0, short_press_max_time)) {
            logger.debug("Button press duration > {}ms and <= {}ms", 0, short_press_max_time);
            shortPress = true;
        } else if (isBetween(duration.toMillis(), short_press_max_time, long_press_max_time)) {
            logger.debug("Button press duration > {}ms and <= {}ms", short_press_max_time, long_press_max_time);
            longPress = true;
        } else if (isBetween(duration.toMillis(), long_press_max_time, extra_long_press_max_time)) {
            logger.debug("Button press duration > {}ms and <= {}ms", long_press_max_time, extra_long_press_max_time);
            extraLongPress = true;
        } else {
            logger.debug("Button press duration > {}ms, ignore it", extra_long_press_max_time);
        }
    }

    private boolean isBetween(long value, long minValue, long maxValueInclusive) {
        return (value > minValue && value <= maxValueInclusive);
    }

    @Override
    public String toString() {
        return String.format("duration=%sms, shortPress=%b, longPress=%b, extraLongPress=%b", duration.toMillis(),
                shortPress, longPress, extraLongPress);
    }
}
