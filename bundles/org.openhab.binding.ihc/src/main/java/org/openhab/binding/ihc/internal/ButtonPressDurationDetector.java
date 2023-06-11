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
    private Duration duration;
    private long longPressTime;
    private long longPressMaxTime;

    public boolean isShortPress() {
        return shortPress;
    }

    public boolean isLongPress() {
        return longPress;
    }

    public ButtonPressDurationDetector(Duration duration, long longPressTime, long longPressMaxTime) {
        this.duration = duration;
        this.longPressTime = longPressTime;
        this.longPressMaxTime = longPressMaxTime;

        calculate();
    }

    private void calculate() {
        if (duration.toMillis() < 0) {
            logger.debug("Button press duration < 0ms");
        } else if (isBetween(duration.toMillis(), 0, longPressTime)) {
            logger.debug("Button press duration > {}ms and < {}ms", 0, longPressTime);
            shortPress = true;
        } else if (isBetween(duration.toMillis(), longPressTime, longPressMaxTime)) {
            logger.debug("Button press duration > {}ms and < {}ms", longPressTime, longPressMaxTime);
            longPress = true;
        } else {
            logger.debug("Button press duration > {}ms, ignore it", longPressMaxTime);
        }
    }

    private boolean isBetween(long value, long minValue, long maxValueInclusive) {
        return (value > minValue && value <= maxValueInclusive);
    }

    @Override
    public String toString() {
        return String.format("[ duration=%sms, shortPress=%b, longPress=%b ]", duration.toMillis(), shortPress,
                longPress);
    }
}
