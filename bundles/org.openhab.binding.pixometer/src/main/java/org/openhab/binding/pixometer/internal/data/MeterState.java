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
package org.openhab.binding.pixometer.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pixometer.internal.config.ReadingInstance;
import org.openhab.core.library.types.DateTimeType;

/**
 * Abstract data class to store shared/common meter state values
 *
 * @author Jerome Luckenbach - Initial contribution
 *
 */
@NonNullByDefault
public class MeterState {

    private final DateTimeType lastReadingDate;
    private final DateTimeType lastRefreshTime;
    private final double readingValue;

    /**
     * Initialize times from the given timestamps
     *
     * @param lastReadingDate time of last reading as ZonedDateTime
     * @param lastRefreshTime time of last refresh as ZonedDateTime
     */
    public MeterState(ReadingInstance reading) {
        this.readingValue = reading.getValue();
        this.lastReadingDate = new DateTimeType(reading.getReadingDate());
        this.lastRefreshTime = new DateTimeType();
    }

    /**
     * @return returns the current reading value
     */
    public double getReadingValue() {
        return readingValue;
    }

    /**
     * @return returns the last time that the meter has been read into pixometer
     */
    public DateTimeType getLastReadingDate() {
        return lastReadingDate;
    }

    /**
     * @return returns the last time, the item has been refreshed
     */
    public DateTimeType getLastRefreshTime() {
        return lastRefreshTime;
    }
}
