/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A calendar-class which provides everything needed for the binding,
 * encapsulating the implementation of the real calendar.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractPresentableCalendar {

    /**
     * Creates an implementing Instance of AbstractPresentableCalendar. Please
     * use {@link #setLookAround(Duration)} before to ensure the presentable
     * calendar is useful.
     *
     * @param calendarStream A Stream containing the iCal-data.
     * @param lookAround The time window to search for events around a certain
     *            point in time.
     * @return The instance.
     * @throws IOException When something while reading stream fails.
     * @throws CalendarException When something while parsing fails.
     */
    public static AbstractPresentableCalendar create(InputStream calendarStream, Duration lookAround)
            throws IOException, CalendarException {
        return new BiweeklyPresentableCalendar(calendarStream, lookAround);
    }

    /**
     * Checks whether an event is present at given Instant.
     *
     * @param instant The Instant, that should be checked.
     * @return Whether an event is present.
     */
    public abstract boolean isEventPresent(Instant instant);

    /**
     * Searches the event currently (at given Instant) present.
     *
     * @param instant The Instant, the event should be returned for.
     * @return The current {@link Event} containing the data of the event or
     *         null if no event is present.
     */
    public abstract @Nullable Event getCurrentEvent(Instant instant);

    /**
     * The next event after given instant.
     *
     * @param instant The Instant after which the next event should be
     *            searched.
     * @return The next event after the given Instant or null if there is any
     *         further in the calendar.
     */
    public abstract @Nullable Event getNextEvent(Instant instant);
}
