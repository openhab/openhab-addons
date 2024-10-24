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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icalendar.internal.logic.EventTextFilter.Type;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalReader;
import biweekly.parameter.Range;
import biweekly.property.Comment;
import biweekly.property.Contact;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.DurationProperty;
import biweekly.property.Location;
import biweekly.property.RecurrenceId;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.TextProperty;
import biweekly.property.Uid;
import biweekly.util.ICalDate;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instantiation.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Methods getJustBegunEvents() & getJustEndedEvents()
 * @author Michael Wodniok - Extension for filtered events
 * @author Michael Wodniok - Added logic for events moved with "RECURRENCE-ID" (issue 9647)
 * @author Michael Wodniok - Extended logic for defined behavior with parallel current events
 *         (issue 10808)
 * @author Christian Heinemann - Extension for the time-based filtering strategy
 */
@NonNullByDefault
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
    private static final Duration ONE_DAY = Duration.ofDays(1).minusNanos(1);
    private final ICalendar usedCalendar;

    BiweeklyPresentableCalendar(InputStream streamed) throws IOException, CalendarException {
        try (final ICalReader reader = new ICalReader(streamed)) {
            final ICalendar currentCalendar = reader.readNext();
            if (currentCalendar == null) {
                throw new CalendarException("No calendar was parsed.");
            }
            this.usedCalendar = currentCalendar;
        }
    }

    @Override
    public @Nullable Event getCurrentEvent(Instant instant) {
        final VEventWPeriod currentComponentWPeriod = this.getCurrentComponentWPeriod(instant);
        if (currentComponentWPeriod == null) {
            return null;
        }

        return currentComponentWPeriod.toEvent();
    }

    @Override
    public List<Event> getJustBegunEvents(Instant frameBegin, Instant frameEnd) {
        return this.getVEventWPeriodsBetween(frameBegin, frameEnd, 0, EventTimeFilter.searchByStart()).stream()
                .map(VEventWPeriod::toEvent).collect(Collectors.toList());
    }

    @Override
    public List<Event> getJustEndedEvents(Instant frameBegin, Instant frameEnd) {
        return this.getVEventWPeriodsBetween(frameBegin, frameEnd, 0, EventTimeFilter.searchByJustEnded()).stream()
                .map(VEventWPeriod::toEvent).collect(Collectors.toList());
    }

    @Override
    public @Nullable Event getNextEvent(Instant instant) {
        final Collection<VEventWPeriod> candidates = new ArrayList<>();
        final Collection<VEvent> negativeEvents = new ArrayList<>();
        final Collection<VEvent> positiveEvents = new ArrayList<>();
        classifyEvents(positiveEvents, negativeEvents);
        for (final VEvent currentEvent : positiveEvents) {
            final DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            final Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant));
            while (startDates.hasNext()) {
                final Instant startInstant = startDates.next().toInstant();
                if (startInstant.isAfter(instant)) {
                    final Uid currentEventUid = currentEvent.getUid();
                    if (currentEventUid == null || !isCounteredBy(startInstant, currentEventUid, negativeEvents)) {
                        candidates.add(new VEventWPeriod(currentEvent, startInstant, startInstant.plus(duration)));
                        break;
                    }
                }
            }
        }
        VEventWPeriod earliestNextEvent = null;
        for (final VEventWPeriod positiveCandidate : candidates) {
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

    @Override
    public List<Event> getFilteredEventsBetween(Instant begin, Instant end, EventTimeFilter eventTimeFilter,
            @Nullable EventTextFilter eventTextFilter, int maximumCount) {
        List<VEventWPeriod> candidates = this.getVEventWPeriodsBetween(begin, end, maximumCount, eventTimeFilter);
        final List<Event> results = new ArrayList<>(candidates.size());

        if (eventTextFilter != null) {
            Pattern filterPattern;
            if (eventTextFilter.type == Type.TEXT) {
                filterPattern = Pattern.compile(".*" + Pattern.quote(eventTextFilter.value) + ".*",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            } else {
                filterPattern = Pattern.compile(eventTextFilter.value);
            }

            Class<? extends TextProperty> propertyClass;
            switch (eventTextFilter.field) {
                case SUMMARY:
                    propertyClass = Summary.class;
                    break;
                case COMMENT:
                    propertyClass = Comment.class;
                    break;
                case CONTACT:
                    propertyClass = Contact.class;
                    break;
                case DESCRIPTION:
                    propertyClass = Description.class;
                    break;
                case LOCATION:
                    propertyClass = Location.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Property to filter for.");
            }

            List<VEventWPeriod> filteredCandidates = candidates.stream().filter(current -> {
                List<? extends TextProperty> properties = current.vEvent.getProperties(propertyClass);
                for (TextProperty prop : properties) {
                    if (filterPattern.matcher(prop.getValue()).matches()) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            candidates = filteredCandidates;
        }

        for (VEventWPeriod eventWPeriod : candidates) {
            results.add(eventWPeriod.toEvent());
        }

        Collections.sort(results);

        return results.subList(0, (maximumCount > results.size() ? results.size() : maximumCount));
    }

    /**
     * Finds events which begin in the given frame by end time and date
     *
     * @param frameBegin Begin of the frame where to search events.
     * @param frameEnd End of the time frame where to search events.
     * @param maximumPerSeries Limit the results per series. Set to 0 for no limit.
     * @param eventTimeFilter Strategy that decides which events should be considered in the time frame.
     * @return All events which begin in the time frame.
     */
    private List<VEventWPeriod> getVEventWPeriodsBetween(Instant frameBegin, Instant frameEnd, int maximumPerSeries,
            EventTimeFilter eventTimeFilter) {
        final List<VEvent> positiveEvents = new ArrayList<>();
        final List<VEvent> negativeEvents = new ArrayList<>();
        classifyEvents(positiveEvents, negativeEvents);

        final List<VEventWPeriod> eventList = new ArrayList<>();
        for (final VEvent positiveEvent : positiveEvents) {
            final DateIterator positiveBeginDates = getRecurredEventDateIterator(positiveEvent);
            Duration duration = getEventLength(positiveEvent);
            if (duration == null) {
                duration = Duration.ZERO;
            }
            positiveBeginDates.advanceTo(Date.from(eventTimeFilter.searchFrom(frameBegin, duration)));
            int foundInSeries = 0;
            while (positiveBeginDates.hasNext()) {
                final Instant begInst = positiveBeginDates.next().toInstant();
                if (eventTimeFilter.eventAfterFrame(frameEnd, begInst, duration)) {
                    break;
                }
                // biweekly is not as precise as java.time. An exact check is required.
                if (eventTimeFilter.eventBeforeFrame(frameBegin, begInst, duration)) {
                    continue;
                }

                final VEventWPeriod resultingVEWP = new VEventWPeriod(positiveEvent, begInst, begInst.plus(duration));
                final Uid eventUid = positiveEvent.getUid();
                if (eventUid != null) {
                    if (!isCounteredBy(begInst, eventUid, negativeEvents)) {
                        eventList.add(resultingVEWP);
                        foundInSeries++;
                        if (maximumPerSeries != 0 && foundInSeries >= maximumPerSeries) {
                            break;
                        }
                    }
                } else {
                    eventList.add(resultingVEWP);
                    foundInSeries++;
                    if (maximumPerSeries != 0 && foundInSeries >= maximumPerSeries) {
                        break;
                    }
                }
            }
        }

        return eventList;
    }

    /**
     * Classifies events into positive and negative ones.
     *
     * @param positiveEvents A List where to add positive ones.
     * @param negativeEvents A List where to add negative ones.
     */
    private void classifyEvents(Collection<VEvent> positiveEvents, Collection<VEvent> negativeEvents) {
        for (final VEvent currentEvent : usedCalendar.getEvents()) {
            final Status eventStatus = currentEvent.getStatus();
            boolean positive = (eventStatus == null || (eventStatus.isTentative() || eventStatus.isConfirmed()));
            final RecurrenceId eventRecurrenceId = currentEvent.getRecurrenceId();
            if (positive && eventRecurrenceId != null) {
                // RecurrenceId moves an event. This blocks other events of series and creates a new single instance
                positiveEvents.add(currentEvent);
                negativeEvents.add(currentEvent);
            } else {
                final Collection<VEvent> positiveOrNegativeEvents = (positive ? positiveEvents : negativeEvents);
                positiveOrNegativeEvents.add(currentEvent);
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
        final List<VEvent> negativeEvents = new ArrayList<>();
        final List<VEvent> positiveEvents = new ArrayList<>();
        classifyEvents(positiveEvents, negativeEvents);

        VEventWPeriod earliestEndingEvent = null;

        for (final VEvent currentEvent : positiveEvents) {
            final DateIterator startDates = this.getRecurredEventDateIterator(currentEvent);
            final Duration duration = getEventLength(currentEvent);
            if (duration == null) {
                continue;
            }
            startDates.advanceTo(Date.from(instant.minus(duration)));
            while (startDates.hasNext()) {
                final Instant startInstant = startDates.next().toInstant();
                final Instant endInstant = startInstant.plus(duration);
                if (startInstant.isBefore(instant) && endInstant.isAfter(instant)) {
                    final Uid eventUid = currentEvent.getUid();
                    if (eventUid == null || !isCounteredBy(startInstant, eventUid, negativeEvents)) {
                        if (earliestEndingEvent == null || endInstant.isBefore(earliestEndingEvent.end)) {
                            earliestEndingEvent = new VEventWPeriod(currentEvent, startInstant, endInstant);
                        }
                    }
                }
                if (startInstant.isAfter(instant.plus(duration))) {
                    break;
                }
            }
        }

        return earliestEndingEvent;
    }

    /**
     * Finds a duration of the event.
     *
     * @param vEvent The event to find out the duration.
     * @return Either a Duration describing the events length or null, if no information is available.
     */
    private static @Nullable Duration getEventLength(VEvent vEvent) {
        final DurationProperty duration = vEvent.getDuration();
        if (duration != null) {
            final biweekly.util.Duration eventDuration = duration.getValue();
            return Duration.ofMillis(eventDuration.toMillis());
        }
        final DateStart start = vEvent.getDateStart();
        if (start == null) {
            return null;
        }
        final DateEnd end = vEvent.getDateEnd();
        if (end != null) {
            return Duration.between(start.getValue().toInstant(), end.getValue().toInstant());
        }
        return start.getValue().hasTime() ? Duration.ZERO : ONE_DAY;
    }

    /**
     * Retrieves a DateIterator to iterate through the events occurrences.
     *
     * @param vEvent The VEvent to create the iterator for.
     * @return The DateIterator for {@link VEvent}
     */
    private DateIterator getRecurredEventDateIterator(VEvent vEvent) {
        final TimezoneInfo tzinfo = this.usedCalendar.getTimezoneInfo();

        final DateStart firstStart = vEvent.getDateStart();
        TimeZone tz;
        if (tzinfo.isFloating(firstStart)) {
            tz = TimeZone.getDefault();
        } else {
            final TimezoneAssignment startAssignment = tzinfo.getTimezone(firstStart);
            tz = (startAssignment == null ? TimeZone.getTimeZone("UTC") : startAssignment.getTimeZone());
        }
        return vEvent.getDateIterator(tz);
    }

    /**
     * Checks whether a counter event blocks an event with given uid and start.
     *
     * @param startInstant The start of the event.
     * @param eventUid The uid of the event.
     * @param counterEvents Events that may counter.
     * @return True if a counter event exists that matches uid and start, else false.
     */
    private boolean isCounteredBy(Instant startInstant, Uid eventUid, Collection<VEvent> counterEvents) {
        for (final VEvent counterEvent : counterEvents) {
            final Uid counterEventUid = counterEvent.getUid();
            if (counterEventUid != null && eventUid.getValue().contentEquals(counterEventUid.getValue())) {
                final RecurrenceId counterRecurrenceId = counterEvent.getRecurrenceId();
                if (counterRecurrenceId != null) {
                    ICalDate recurrenceDate = counterRecurrenceId.getValue();
                    if (recurrenceDate != null) {
                        Instant recurrenceInstant = Instant.ofEpochMilli(recurrenceDate.getTime());
                        if (recurrenceInstant.equals(startInstant)) {
                            return true;
                        }
                        Range futureOrPast = counterRecurrenceId.getRange();
                        if (futureOrPast != null && futureOrPast.equals(Range.THIS_AND_FUTURE)
                                && startInstant.isAfter(recurrenceInstant)) {
                            return true;
                        }
                        if (futureOrPast != null && futureOrPast.equals(Range.THIS_AND_PRIOR)
                                && startInstant.isBefore(recurrenceInstant)) {
                            return true;
                        }
                    }
                } else {
                    final DateIterator counterStartDates = getRecurredEventDateIterator(counterEvent);
                    counterStartDates.advanceTo(Date.from(startInstant));
                    if (counterStartDates.hasNext()) {
                        final Instant counterStartInstant = counterStartDates.next().toInstant();
                        if (counterStartInstant.equals(startInstant)) {
                            return true;
                        }
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

        public VEventWPeriod(VEvent vEvent, Instant start, Instant end) {
            this.vEvent = vEvent;
            this.start = start;
            this.end = end;
        }

        public Event toEvent() {
            final Summary eventSummary = vEvent.getSummary();
            final String title = eventSummary != null ? eventSummary.getValue() : "-";
            final Description eventDescription = vEvent.getDescription();
            final String description = eventDescription != null ? eventDescription.getValue() : "";
            return new Event(title, start, end, description);
        }
    }
}
