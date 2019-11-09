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
package org.openhab.binding.icalpresence.internal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalReader;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.DurationProperty;
import biweekly.property.Summary;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instanciation.
 *
 * @author Michael Wodniok - Initial contribution
 */
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
    private final @NonNull TemporalAmount lookAround;
    private final @NonNull ICalendar bakingCalendar;

    BiweeklyPresentableCalendar(@NonNull InputStream streamed, @NonNull TemporalAmount lookAround)
            throws IOException, CalendarException {
        ICalReader reader = new ICalReader(streamed);
        try {
            ICalendar currentCalendar = reader.readNext();
            if (currentCalendar == null) {
                throw new CalendarException("No calendar was parsed.");
            }
            this.bakingCalendar = currentCalendar;
            this.lookAround = lookAround;
        } finally {
            reader.close();
        }
    }

    @Override
    public boolean isEventPresent(Instant instant) {
        return (this.getCurrentComponentWPeriod(instant) != null);
    }

    @Override
    public @Nullable Event getCurrentEvent(Instant instant) {
        VEventWPeriod currentComponentWPeriod = this.getCurrentComponentWPeriod(instant);
        if (currentComponentWPeriod == null) {
            return null;
        }

        return currentComponentWPeriod.toEvent();
    }

    @Override
    public @Nullable Event getNextEvent(Instant instant) {
        VEventWPeriod earliestNextEvent = null;
        Instant recurrenceCalculationEnd = instant.plus(lookAround);

        for (VEvent currentEvent : bakingCalendar.getEvents()) {
            DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant));
            while (startDates.hasNext()) {
                Instant startInstant = startDates.next().toInstant();
                Instant endInstant = startInstant.plus(duration);
                if (startInstant.isAfter(instant)
                        && (earliestNextEvent == null || earliestNextEvent.start.isAfter(startInstant))) {
                    earliestNextEvent = new VEventWPeriod(currentEvent, startInstant, endInstant);
                }
                if (startInstant.isAfter(recurrenceCalculationEnd)) {
                    break;
                }
            }
        }

        if (earliestNextEvent == null) {
            return null;
        }
        return earliestNextEvent.toEvent();
    }

    private @Nullable VEventWPeriod getCurrentComponentWPeriod(Instant instant) {
        Instant recurrenceCalculationBegin = instant.minus(lookAround);
        Instant recurrenceCalculationEnd = instant.plus(lookAround);
        // new DateTime(instant.plus(lookAround).getEpochSecond() * 1000));
        // DateTime instantDate = new DateTime(instant.getEpochSecond() * 1000);
        for (VEvent currentEvent : bakingCalendar.getEvents()) {
            DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(recurrenceCalculationBegin));
            while (startDates.hasNext()) {
                Instant startInstant = startDates.next().toInstant();
                Instant endInstant = startInstant.plus(duration);
                if (startInstant.isBefore(instant) && endInstant.isAfter(instant)) {
                    return new VEventWPeriod(currentEvent, startInstant, endInstant);
                }
                if (startInstant.isAfter(recurrenceCalculationEnd)) {
                    break;
                }
            }
        }
        return null;
    }

    private @NonNull DateIterator getRecurredEventDateIterator(@NonNull VEvent vEvent) {
        TimezoneInfo tzinfo = this.bakingCalendar.getTimezoneInfo();

        DateStart firstStart = vEvent.getDateStart();
        TimeZone tz;
        if (tzinfo.isFloating(firstStart)) {
            tz = TimeZone.getDefault();
        } else {
            TimezoneAssignment startAssignment = tzinfo.getTimezone(firstStart);
            tz = (startAssignment == null ? TimeZone.getTimeZone("UTC") : startAssignment.getTimeZone());
        }
        return vEvent.getDateIterator(tz);
    }

    private static @Nullable Duration getEventLength(@NonNull VEvent vEvent) {
        DurationProperty duration = vEvent.getDuration();
        if (duration != null) {
            biweekly.util.Duration eventDuration = duration.getValue();
            return Duration.ofMillis(eventDuration.toMillis());
        }
        DateStart start = vEvent.getDateStart();
        DateEnd end = vEvent.getDateEnd();
        if (start == null || end == null) {
            return null;
        }
        return Duration.between(start.getValue().toInstant(), end.getValue().toInstant());
    }

    private static class VEventWPeriod {
        VEvent vEvent;
        Instant start;
        Instant end;

        VEventWPeriod(VEvent vEvent, Instant start, Instant end) {
            this.vEvent = vEvent;
            this.start = start;
            this.end = end;
        }

        @NonNull
        Event toEvent() {
            String title;
            Summary eventSummary = vEvent.getSummary();
            if (eventSummary != null) {
                title = eventSummary.getValue();
            } else {
                title = "-";
            }
            return new Event(title, start, end);
        }
    }
}
