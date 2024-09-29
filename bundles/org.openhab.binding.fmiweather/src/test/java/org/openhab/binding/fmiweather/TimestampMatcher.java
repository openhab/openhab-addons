/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matcher for timestamps
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class TimestampMatcher extends TypeSafeMatcher<long[]> {

    private long start;
    private int intervalMinutes;
    private long steps;

    public TimestampMatcher(long start, int intervalMinutes, long steps) {
        this.start = start;
        this.intervalMinutes = intervalMinutes;
        this.steps = steps;
    }

    @Override
    public void describeTo(@Nullable Description description) {
        if (description == null) {
            return;
        }
        description.appendText(new StringBuilder("start=").append(start).append(", length=").append(steps)
                .append(", interval=").append(intervalMinutes).toString());
    }

    @Override
    protected boolean matchesSafely(long[] timestamps) {
        return verifyLength(timestamps) && verifyStart(timestamps) && verifyStep(timestamps);
    }

    private boolean verifyLength(long[] timestamps) {
        return timestamps.length == steps;
    }

    private boolean verifyStart(long[] timestamps) {
        return timestamps[0] == start;
    }

    private boolean verifyStep(long[] timestamps) {
        for (int i = 1; i < timestamps.length; i++) {
            if (timestamps[i] - timestamps[i - 1] != intervalMinutes * 60) {
                return false;
            }
        }
        return true;
    }
}
