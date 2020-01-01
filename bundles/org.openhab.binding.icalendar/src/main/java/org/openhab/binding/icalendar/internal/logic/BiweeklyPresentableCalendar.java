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
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalReader;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.DurationProperty;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Uid;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instanciation.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
    private final TemporalAmount lookAround;
    private final ICalendar bakingCalendar;

    BiweeklyPresentableCalendar(InputStream streamed, TemporalAmount lookAround) throws IOException, CalendarException {
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

        Instant recurrenceCalculationEnd = instant.plus(lookAround);

        List<VEventWPeriod> positiveCandidates = new LinkedList<VEventWPeriod>();
        List<VEventWPeriod> negativeCandidates = new LinkedList<VEventWPeriod>();
        for (VEvent currentEvent : bakingCalendar.getEvents()) {
            @Nullable
            Status eventStatus = currentEvent.getStatus();
            boolean additive = (eventStatus == null
                    || (eventStatus != null && (eventStatus.isTentative() || eventStatus.isConfirmed())));

            DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant));
            while (startDates.hasNext()) {
                Instant startInstant = startDates.next().toInstant();
                Instant endInstant = startInstant.plus(duration);
                if (startInstant.isAfter(instant)) {
                    List<VEventWPeriod> candidateList = (additive ? positiveCandidates : negativeCandidates);
                    candidateList.add(new VEventWPeriod(currentEvent, startInstant, endInstant));
                }
                if (startInstant.isAfter(recurrenceCalculationEnd)) {
                    break;
                }
            }
        }
        VEventWPeriod earliestNextEvent = null;
        for (VEventWPeriod positiveCandidate : positiveCandidates) {
            @Nullable
            Uid pcUid = positiveCandidate.vEvent.getUid();
            boolean cancel = false;
            if (pcUid != null) {
                for (VEventWPeriod negativeCandidate : negativeCandidates) {
                    @Nullable
                    Uid ncUid = negativeCandidate.vEvent.getUid();
                    if (ncUid != null && ncUid.getValue().contentEquals(pcUid.getValue())
                            && positiveCandidate.start.equals(negativeCandidate.start)
                            && positiveCandidate.end.equals(negativeCandidate.end)) {
                        cancel = true;
                        break;
                    }
                }
            }
            if (!cancel && (earliestNextEvent == null || earliestNextEvent.start.isAfter(positiveCandidate.start))) {
                earliestNextEvent = positiveCandidate;
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
        List<VEventWPeriod> positiveCandidates = new LinkedList<VEventWPeriod>();
        List<VEventWPeriod> negativeCandidates = new LinkedList<VEventWPeriod>();
        for (VEvent currentEvent : bakingCalendar.getEvents()) {
            @Nullable
            Status eventStatus = currentEvent.getStatus();
            boolean additive = (eventStatus == null
                    || (eventStatus != null && (eventStatus.isTentative() || eventStatus.isConfirmed())));
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
                    List<VEventWPeriod> candidateList = (additive ? positiveCandidates : negativeCandidates);
                    candidateList.add(new VEventWPeriod(currentEvent, startInstant, endInstant));
                    break;
                }
                if (startInstant.isAfter(recurrenceCalculationEnd)) {
                    break;
                }
            }
        }

        for (VEventWPeriod possibleCandidate : positiveCandidates) {
            boolean cancel = false;
            @Nullable
            Uid candidateUid = possibleCandidate.vEvent.getUid();
            if (candidateUid != null) {
                for (VEventWPeriod possibleCancellation : negativeCandidates) {
                    @Nullable
                    Uid cancellationUid = possibleCancellation.vEvent.getUid();
                    if (cancellationUid != null && candidateUid.getValue().contentEquals(cancellationUid.getValue())) {
                        cancel = true;
                        break;
                    }
                }
            }
            if (!cancel) {
                return possibleCandidate;
            }
        }
        return null;
    }

    private DateIterator getRecurredEventDateIterator(VEvent vEvent) {
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

    private static @Nullable Duration getEventLength(VEvent vEvent) {
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
