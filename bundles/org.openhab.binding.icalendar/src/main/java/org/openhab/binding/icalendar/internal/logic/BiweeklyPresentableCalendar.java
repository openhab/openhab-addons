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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
 * @author Andrew Fiddian-Green - Methods getJustBegunEvents() & getJustEndedEvents()
 */
@NonNullByDefault
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
    private final ICalendar usedCalendar;

    BiweeklyPresentableCalendar(InputStream streamed) throws IOException, CalendarException {
        try (ICalReader reader = new ICalReader(streamed)) {
            ICalendar currentCalendar = reader.readNext();
            if (currentCalendar == null) {
                throw new CalendarException("No calendar was parsed.");
            }
            this.usedCalendar = currentCalendar;
        }
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
                    eventList = new ArrayList<Event>();
                }
                eventList.add(new VEventWPeriod(event, begInst, begInst.plus(duration)).toEvent());
                break;
            }
        }
        return eventList;
    }

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
                    eventList = new ArrayList<Event>();
                }
                eventList.add(new VEventWPeriod(event, begInst, endInst).toEvent());
                break;
            }
        }
        return eventList;
    }

    @Override
    public @Nullable Event getNextEvent(Instant instant) {
        Collection<VEventWPeriod> candidates = new ArrayList<VEventWPeriod>();
        Collection<VEvent> negativeEvents = new ArrayList<VEvent>();
        Collection<VEvent> positiveEvents = new ArrayList<VEvent>();
        classifyEvents(positiveEvents, negativeEvents);
        for (VEvent currentEvent : positiveEvents) {
            DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant));
            while (startDates.hasNext()) {
                Instant startInstant = startDates.next().toInstant();
                if (startInstant.isAfter(instant)) {
                    @Nullable
                    Uid currentEventUid = currentEvent.getUid();
                    if (currentEventUid == null || !isCounteredBy(startInstant, currentEventUid, negativeEvents)) {
                        candidates.add(new VEventWPeriod(currentEvent, startInstant, startInstant.plus(duration)));
                        break;
                    }
                }
            }
        }
        VEventWPeriod earliestNextEvent = null;
        for (VEventWPeriod positiveCandidate : candidates) {
            if (earliestNextEvent == null || earliestNextEvent.start.isAfter(positiveCandidate.start)) {
                earliestNextEvent = positiveCandidate;
            }
        }

        if (earliestNextEvent == null) {
            return null;
        }
        return earliestNextEvent.toEvent();
    }

    @Override
    public boolean isEventPresent(Instant instant) {
        return (this.getCurrentComponentWPeriod(instant) != null);
    }

    /**
     * Classifies events into positive and negative ones.
     *
     * @param positiveEvents A List where to add positive ones.
     * @param negativeEvents A List where to add negative ones.
     */
    private void classifyEvents(Collection<VEvent> positiveEvents, Collection<VEvent> negativeEvents) {
        for (VEvent currentEvent : usedCalendar.getEvents()) {
            @Nullable
            Status eventStatus = currentEvent.getStatus();
            boolean positive = (eventStatus == null || (eventStatus.isTentative() || eventStatus.isConfirmed()));
            Collection<VEvent> positiveOrNegativeEvents = (positive ? positiveEvents : negativeEvents);
            positiveOrNegativeEvents.add(currentEvent);
        }
    }

    /**
     * Searches for a current event at given Instant.
     *
     * @param instant The Instant to use for finding events.
     * @return A VEventWPeriod describing the event or null if there is none.
     */
    private @Nullable VEventWPeriod getCurrentComponentWPeriod(Instant instant) {
        List<VEvent> negativeEvents = new ArrayList<VEvent>();
        List<VEvent> positiveEvents = new ArrayList<VEvent>();
        classifyEvents(positiveEvents, negativeEvents);

        for (VEvent currentEvent : positiveEvents) {
            DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant.minus(duration)));
            while (startDates.hasNext()) {
                Instant startInstant = startDates.next().toInstant();
                Instant endInstant = startInstant.plus(duration);
                if (startInstant.isBefore(instant) && endInstant.isAfter(instant)) {
                    @Nullable
                    Uid eventUid = currentEvent.getUid();
                    if (eventUid == null || !isCounteredBy(startInstant, eventUid, negativeEvents)) {
                        return new VEventWPeriod(currentEvent, startInstant, endInstant);
                    }
                }
                if (startInstant.isAfter(instant.plus(duration))) {
                    break;
                }
            }
        }

        return null;
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
     * Checks whether an counter event blocks an event with given uid and start.
     *
     * @param startInstant The start of the event.
     * @param eventUid The uid of the event.
     * @param counterEvents Events that may counter.
     * @return True if a counter event exists that matches uid and start, else false.
     */
    private boolean isCounteredBy(Instant startInstant, Uid eventUid, Collection<VEvent> counterEvents) {
        for (VEvent counterEvent : counterEvents) {
            @Nullable
            Uid counterEventUid = counterEvent.getUid();
            if (counterEventUid != null && eventUid.getValue().contentEquals(counterEventUid.getValue())) {
                DateIterator counterStartDates = getRecurredEventDateIterator(counterEvent);
                counterStartDates.advanceTo(Date.from(startInstant));
                if (counterStartDates.hasNext()) {
                    Instant counterStartInstant = counterStartDates.next().toInstant();
                    if (counterStartInstant.equals(startInstant)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * A Class describing an event together with a start and end instant.
     *
     * @author Michael Wodniok - Initial contribution.
     */
    private static class VEventWPeriod {
        final VEvent vEvent;
        final Instant start;
        final Instant end;

        VEventWPeriod(VEvent vEvent, Instant start, Instant end) {
            this.vEvent = vEvent;
            this.start = start;
            this.end = end;
        }

        Event toEvent() {
            String title;
            Summary eventSummary = vEvent.getSummary();
            title = eventSummary != null ? eventSummary.getValue() : "-";
            String description;
            Description eventDescription = vEvent.getDescription();
            description = eventDescription != null ? eventDescription.getValue() : "";
            return new Event(title, start, end, description);
        }
    }
}
