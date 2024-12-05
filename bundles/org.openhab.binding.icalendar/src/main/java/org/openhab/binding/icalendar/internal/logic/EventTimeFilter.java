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

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Strategy for time based filtering.
 *
 * @author Christian Heinemann - Initial contribution
 */
@NonNullByDefault
public abstract class EventTimeFilter {

    /**
     * Creates the strategy to search for events that start in a specific time frame. The exact end of the time frame is
     * exclusive.
     *
     * @return The search strategy.
     */
    public static EventTimeFilter searchByStart() {
        return new SearchByStart();
    }

    /**
     * Creates the strategy to search for events that end in a specific time frame. The exact end of the time frame is
     * inclusive.
     *
     * @return The search strategy.
     */
    public static EventTimeFilter searchByEnd() {
        return new SearchByEnd();
    }

    /**
     * Creates the strategy to search for events that are active in a specific time frame.
     * It finds the same events as {@link #searchByStart()} and {@link #searchByEnd()}, but additionally also events
     * that start before the time frame or end after.
     *
     * @return The search strategy.
     */
    public static EventTimeFilter searchByActive() {
        return new SearchByActive();
    }

    /**
     * Creates the strategy to search for events that end in a specific time frame. The exact end of the time frame is
     * inclusive.
     * <p>
     * This is the strategy applied by {@link BiweeklyPresentableCalendar#getJustEndedEvents(Instant, Instant)}.
     * It is used here for backwards compatibility.
     * There are problems when an event ends exactly at the end of the search period.
     * Then the result is found for both this search period and one that begins immediately after it.
     * However, the usual behavior should be that if there are several non-overlapping search periods, an event will
     * only be found at most once.
     * That's why it is only offered here as non-public for internal use.
     *
     * @return The search strategy.
     */
    static EventTimeFilter searchByJustEnded() {
        return new SearchByJustEnded();
    }

    /**
     * Gives a time to start searching for occurrences of a particular (recurring) event.
     *
     * @param frameStart Start of the frame where to search events.
     * @param eventDuration Duration of the event.
     * @return The time to start searching.
     */
    public abstract Instant searchFrom(Instant frameStart, Duration eventDuration);

    /**
     * Decides whether the relevant characteristic of an event occurrence is after the time frame. With the first hit,
     * no further occurrences of a recurring event are searched for.
     *
     * @param frameEnd End of the frame where to search events.
     * @param eventStart Start of the event occurrence.
     * @param eventDuration Duration of the event.
     * @return {@code true} if an occurrence of the event was found after the time frame, otherwise {@code false}.
     */
    public abstract boolean eventAfterFrame(Instant frameEnd, Instant eventStart, Duration eventDuration);

    /**
     * Decides whether the relevant characteristic of an event occurrence is before the time frame. Such occurrences are
     * ignored.
     *
     * @param frameStart Start of the frame where to search events.
     * @param eventStart Start of the event occurrence.
     * @param eventDuration Duration of the event.
     * @return {@code true} if an occurrence of the event was found before the time frame, otherwise {@code false}.
     */
    public abstract boolean eventBeforeFrame(Instant frameStart, Instant eventStart, Duration eventDuration);

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == null) {
            return false;
        }
        return getClass().equals(other.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static class SearchByStart extends EventTimeFilter {
        @Override
        public Instant searchFrom(Instant frameStart, Duration eventDuration) {
            return frameStart;
        }

        @Override
        public boolean eventAfterFrame(Instant frameEnd, Instant eventStart, Duration eventDuration) {
            return !eventStart.isBefore(frameEnd);
        }

        @Override
        public boolean eventBeforeFrame(Instant frameStart, Instant eventStart, Duration eventDuration) {
            return eventStart.isBefore(frameStart);
        }
    }

    private static class SearchByEnd extends EventTimeFilter {
        @Override
        public Instant searchFrom(Instant frameStart, Duration eventDuration) {
            return frameStart.minus(eventDuration);
        }

        @Override
        public boolean eventAfterFrame(Instant frameEnd, Instant eventStart, Duration eventDuration) {
            return eventStart.plus(eventDuration).isAfter(frameEnd);
        }

        @Override
        public boolean eventBeforeFrame(Instant frameStart, Instant eventStart, Duration eventDuration) {
            return !eventStart.plus(eventDuration).isAfter(frameStart);
        }
    }

    private static class SearchByActive extends EventTimeFilter {
        @Override
        public Instant searchFrom(Instant frameStart, Duration eventDuration) {
            return frameStart.minus(eventDuration);
        }

        @Override
        public boolean eventAfterFrame(Instant frameEnd, Instant eventStart, Duration eventDuration) {
            return !eventStart.isBefore(frameEnd);
        }

        @Override
        public boolean eventBeforeFrame(Instant frameStart, Instant eventStart, Duration eventDuration) {
            return !eventStart.plus(eventDuration).isAfter(frameStart);
        }
    }

    private static class SearchByJustEnded extends EventTimeFilter {
        @Override
        public Instant searchFrom(Instant frameStart, Duration eventDuration) {
            return frameStart.minus(eventDuration);
        }

        @Override
        public boolean eventAfterFrame(Instant frameEnd, Instant eventStart, Duration eventDuration) {
            return eventStart.plus(eventDuration).isAfter(frameEnd);
        }

        @Override
        public boolean eventBeforeFrame(Instant frameStart, Instant eventStart, Duration eventDuration) {
            return eventStart.plus(eventDuration).isBefore(frameStart);
        }
    }
}
