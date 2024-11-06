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
package org.openhab.binding.icalendar.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A calendar which provides the interface to the calendar implementation for
 * the binding, encapsulating the implementation of the real calendar.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Methods getJustBegunEvents() and getJustEndedEvents()
 * @author Michael Wodniok - Added getFilteredEventsBetween()
 * @author Christian Heinemann - Extension for the time-based filtering strategy
 */
@NonNullByDefault
public abstract class AbstractPresentableCalendar {

    /**
     * Creates an implementing Instance of AbstractPresentableCalendar.
     *
     * @param calendarStream A Stream containing the iCal data.
     * @return The instance.
     * @throws IOException When something while reading stream fails.
     * @throws CalendarException When something while parsing fails.
     */
    public static AbstractPresentableCalendar create(InputStream calendarStream) throws IOException, CalendarException {
        return new BiweeklyPresentableCalendar(calendarStream);
    }

    /**
     * Searches the event currently (at given Instant) present.
     *
     * @param instant The Instant, the event should be returned for.
     * @return The current {@link Event} containing the data of the event or
     *         null if no event is present.
     */
    public abstract @Nullable Event getCurrentEvent(Instant instant);

    /**
     * Return a list of events that have just begun within the time frame
     *
     * @param frameBegin the start of the time frame
     * @param frameEnd the start of the time frame
     * @return list of iCalendar Events that BEGIN within the time frame
     */
    public abstract List<Event> getJustBegunEvents(Instant frameBegin, Instant frameEnd);

    /**
     * Return a list of events that have just ended within the time frame
     *
     * @param frameBegin the start of the time frame
     * @param frameEnd the start of the time frame
     * @return list of iCalendar Events that END within the time frame
     */
    public abstract List<Event> getJustEndedEvents(Instant frameBegin, Instant frameEnd);

    /**
     * The next event after given instant.
     *
     * @param instant The Instant after which the next event should be
     *            searched.
     * @return The next event after the given Instant or null if there is any
     *         further in the calendar.
     */
    public abstract @Nullable Event getNextEvent(Instant instant);

    /**
     * Checks whether an event is present at given Instant.
     *
     * @param instant The Instant, that should be checked.
     * @return True if an event is present.
     */
    public abstract boolean isEventPresent(Instant instant);

    /**
     * Return a filtered List of events with a maximum count, ordered by start.
     *
     * @param begin The begin of the time range where to search for events.
     * @param end The end of the time range where to search for events.
     * @param eventTimeFilter A filter for deciding whether an event falls into the time range.
     * @param eventTextFilter A filter for contents, if set to null, all events will be returned.
     * @param maximumCount The maximum of events returned here.
     * @return A list with the filtered results.
     */
    public abstract List<Event> getFilteredEventsBetween(Instant begin, Instant end, EventTimeFilter eventTimeFilter,
            @Nullable EventTextFilter eventTextFilter, int maximumCount);

    /**
     * Return a filtered List of events with a maximum count, ordered by start. Time based filtering is done by each
     * event's start.
     *
     * @param begin The begin of the time range where to search for events.
     * @param end The end of the time range where to search for events.
     * @param eventTextFilter A filter for contents, if set to null, all events will be returned.
     * @param maximumCount The maximum of events returned here.
     * @return A list with the filtered results.
     */
    public List<Event> getFilteredEventsBetween(Instant begin, Instant end, @Nullable EventTextFilter eventTextFilter,
            int maximumCount) {
        return getFilteredEventsBetween(begin, end, EventTimeFilter.searchByStart(), eventTextFilter, maximumCount);
    }
}
