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
package org.openhab.binding.sleepiq.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SleepDataInterval} represents the time periods that can be
 * used for sleep data requests.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum SleepDataInterval {
    DAY("D1"),
    WEEK("W1"),
    MONTH("M1");

    private final String interval;

    SleepDataInterval(final String interval) {
        this.interval = interval;
    }

    public String value() {
        return interval;
    }

    public static SleepDataInterval forValue(String value) {
        for (SleepDataInterval s : SleepDataInterval.values()) {
            if (s.interval.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid sleep data interval: " + value);
    }

    @Override
    public String toString() {
        return interval;
    }
}
