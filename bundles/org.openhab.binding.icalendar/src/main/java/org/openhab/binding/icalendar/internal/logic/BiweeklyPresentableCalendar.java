/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import biweekly.property.Description;
import biweekly.property.DurationProperty;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Uid;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instantiation.
 *
 * @author Michael Wodniok - Initial contribution
 *
 * @author Andrew Fiddian-Green - Methods getJustBegunEvents() & getJustEndedEvents()
 *
 */
@NonNullByDefault
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
    private final TemporalAmount lookAround;
    private final ICalendar usedCalendar;

    BiweeklyPresentableCalendar(InputStream streamed, TemporalAmount lookAround) throws IOException, CalendarException {
        ICalReader reader = new ICalReader(streamed);
        try {
            ICalendar currentCalendar = reader.readNext();
            if (currentCalendar == null) {
                throw new CalendarException("No calendar was parsed.");
            }
            this.usedCalendar = currentCalendar;
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
        fillNextEventCandidateLists(instant, positiveCandidates, negativeCandidates, recurrenceCalculationEnd);
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

    /**
     * Fills candidate lists for the next event after an Instant.
     *
     * @param instant The Instant to search after.
     * @param positiveCandidates A target List of positive candidates.
     * @param negativeCandidates A target List of negative candidates.
     * @param recurrenceCalculationEnd The end point of recurrence calculation.
     */
    private void fillNextEventCandidateLists(Instant instant, List<VEventWPeriod> positiveCandidates,
            List<VEventWPeriod> negativeCandidates, Instant recurrenceCalculationEnd) {
        for (VEvent currentEvent : usedCalendar.getEvents()) {
            @Nullable
            Status eventStatus = currentEvent.getStatus();
            boolean additive = (eventStatus == null || (eventStatus.isTentative() || eventStatus.isConfirmed()));

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
    }

    /**
     * Searches for a current event at given Instant.
     *
     * @param instant The Instant to use for finding events.
     * @return A VEventWPeriod describing the event or null if there is none.
     */
    private @Nullable VEventWPeriod getCurrentComponentWPeriod(Instant instant) {
        Instant recurrenceCalculationBegin = instant.minus(lookAround);
        Instant recurrenceCalculationEnd = instant.plus(lookAround);
        List<VEventWPeriod> positiveCandidates = new LinkedList<VEventWPeriod>();
        List<VEventWPeriod> negativeCandidates = new LinkedList<VEventWPeriod>();
        fillCurrentEventCandidateLists(instant, positiveCandidates, negativeCandidates, recurrenceCalculationBegin,
                recurrenceCalculationEnd);

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

    /**
     * Finds event candidates at given instant.
     *
     * @param instant The Instant to find event candidates around.
     * @param positiveCandidates The List for the positive Candidates.
     * @param negativeCandidates The List for the negative Candidates.
     * @param recurrenceCalculationBegin The starting point in calculation.
     * @param recurrenceCalculationEnd The end point in calculation.
     */
    private void fillCurrentEventCandidateLists(Instant instant, List<VEventWPeriod> positiveCandidates,
            List<VEventWPeriod> negativeCandidates, Instant recurrenceCalculationBegin,
            Instant recurrenceCalculationEnd) {
        for (VEvent currentEvent : usedCalendar.getEvents()) {
            @Nullable
            Status eventStatus = currentEvent.getStatus();
            boolean additive = (eventStatus == null || (eventStatus.isTentative() || eventStatus.isConfirmed()));
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
    }

    /**
     * Retrieves a DateIterator to iterate through the events occurrences.
     *
     * @param vEvent The VEvent to create the iterator for.
     * @return The DateIterator for {@link VEvent}
     */
    private DateIterator getRecurredEventDateIterator(VEvent vEvent) {
        TimezoneInfo tzinfo = this.usedCalendar.getTimezoneInfo();

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

    /**
     * Finds a duration of the event.
     *
     * @param vEvent The event to find out the duration.
     * @return Either a Duration describing the events length or null, if no information is available.
     */
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

    /**
     * A Class describing an event together with a start and end instant.
     *
     * @author Michael Wodniok - Initial contribution.
     */
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
            String description;
            Description eventDescription = vEvent.getDescription();
            if (eventDescription != null) {
                description = eventDescription.getValue();
            } else {
                description = "";
            }
            return new Event(title, start, end, description);
        }
    }

    /**
     * Return a list of events that have just begun within the time frame
     *
     * @param frameBegin the start of the time frame
     * @param frameEnd the start of the time frame
     * @return list of iCalendar Events that BEGIN within the time frame
     *
     */
    @Override
    public @Nullable List<Event> getJustBegunEvents(Instant frameBegin, Instant frameEnd) {
        List<Event> eventList = null;
        // process all the events in the iCalendar
        for (VEvent event : usedCalendar.getEvents()) {
            // iterate over all begin dates
            DateIterator begDates = getRecurredEventDateIterator(event);
            while (begDates.hasNext()) {
                Instant begInst = begDates.next().toInstant();
                if (begInst.isBefore(frameBegin)) {
                    continue;
                } else if (begInst.isAfter(frameEnd)) {
                    break;
                }
                // fall through => means we are within the time frame
                Duration duration = getEventLength(event);
                if (duration == null) {
                    duration = Duration.ofMinutes(1);
                }
                if (eventList == null) {
                    eventList = new LinkedList<Event>();
                }
                eventList.add(new VEventWPeriod(event, begInst, begInst.plus(duration)).toEvent());
                break;
            }
        }
        return eventList;
    }

    /**
     * Return a list of events that have just ended within the time frame
     *
     * @param frameBegin the start of the time frame
     * @param frameEnd the start of the time frame
     * @return list of iCalendar Events that END within the time frame
     *
     */
    @Override
    public @Nullable List<Event> getJustEndedEvents(Instant frameBegin, Instant frameEnd) {
        List<Event> eventList = null;
        // process all the events in the iCalendar
        for (VEvent event : usedCalendar.getEvents()) {
            Duration duration = getEventLength(event);
            if (duration == null) {
                continue;
            }
            // iterate over all begin dates
            DateIterator begDates = getRecurredEventDateIterator(event);
            while (begDates.hasNext()) {
                Instant begInst = begDates.next().toInstant();
                Instant endInst = begInst.plus(duration);
                if (endInst.isBefore(frameBegin)) {
                    continue;
                } else if (endInst.isAfter(frameEnd)) {
                    break;
                }
                // fall through => means we are within the time frame
                if (eventList == null) {
                    eventList = new LinkedList<Event>();
                }
                eventList.add(new VEventWPeriod(event, begInst, endInst).toEvent());
                break;
            }
        }
        return eventList;
    }

}
