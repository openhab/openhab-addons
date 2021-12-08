/*
 * Copyright 2022 Mark Hilbush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SleepDataInterval} represents the possible sides of the bed (i.e. left and right).
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
        throw new IllegalArgumentException("Invalid side: " + value);
    }

    @Override
    public String toString() {
        return interval;
    }
}
