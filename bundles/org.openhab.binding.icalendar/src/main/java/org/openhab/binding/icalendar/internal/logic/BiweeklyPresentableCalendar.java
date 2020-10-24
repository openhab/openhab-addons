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
import biweekly.property.Comment;
import biweekly.property.Contact;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.DurationProperty;
import biweekly.property.Location;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.TextProperty;
import biweekly.property.Uid;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Implementation of {@link AbstractPresentableCalendar} with ical4j. Please
 * use {@link AbstractPresentableCalendar#create(InputStream)} for productive
 * instantiation.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Methods getJustBegunEvents() & getJustEndedEvents()
 * @author Michael Wodniok - Extension for filtered events
 */
@NonNullByDefault
class BiweeklyPresentableCalendar extends AbstractPresentableCalendar {
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
        final List<Event> eventList = new ArrayList<>();
        // process all the events in the iCalendar
        for (final VEvent event : usedCalendar.getEvents()) {
            // iterate over all begin dates
            final DateIterator begDates = getRecurredEventDateIterator(event);
            while (begDates.hasNext()) {
                final Instant begInst = begDates.next().toInstant();
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
                eventList.add(new VEventWPeriod(event, begInst, begInst.plus(duration)).toEvent());
                break;
            }
        }
        return eventList;
    }

    @Override
    public List<Event> getJustEndedEvents(Instant frameBegin, Instant frameEnd) {
        final List<Event> eventList = new ArrayList<>();
        // process all the events in the iCalendar
        for (final VEvent event : usedCalendar.getEvents()) {
            final Duration duration = getEventLength(event);
            if (duration == null) {
                continue;
            }
            // iterate over all begin dates
            final DateIterator begDates = getRecurredEventDateIterator(event);
            while (begDates.hasNext()) {
                final Instant begInst = begDates.next().toInstant();
                final Instant endInst = begInst.plus(duration);
                if (endInst.isBefore(frameBegin)) {
                    continue;
                } else if (endInst.isAfter(frameEnd)) {
                    break;
                }
                // fall through => means we are within the time frame
                eventList.add(new VEventWPeriod(event, begInst, endInst).toEvent());
                break;
            }
        }
        return eventList;
    }

    @Override
    public @Nullable Event getNextEvent(Instant instant) {
        final Collection<VEventWPeriod> candidates = new ArrayList<VEventWPeriod>();
        final Collection<VEvent> negativeEvents = new ArrayList<VEvent>();
        final Collection<VEvent> positiveEvents = new ArrayList<VEvent>();
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
    public List<Event> getFilteredEventsBetween(Instant begin, Instant end, @Nullable EventTextFilter filter,
            int maximumCount) {
        List<VEventWPeriod> candidates = this.getVEventWPeriodsBetween(begin, end);
        final List<Event> results = new ArrayList<>(candidates.size());

        if (filter != null) {
            Pattern filterPattern;
            if (filter.type == Type.TEXT) {
                filterPattern = Pattern.compile(".*" + Pattern.quote(filter.value) + ".*",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            } else {
                filterPattern = Pattern.compile(filter.value);
            }

            Class<? extends TextProperty> propertyClass;
            switch (filter.field) {
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
     * Finds events which begin in the given frame.
     *
     * @param frameBegin Begin of the frame where to search events.
     * @param frameEnd End of the time frame where to search events.
     * @return All events which begin in the time frame.
     */
    private List<VEventWPeriod> getVEventWPeriodsBetween(Instant frameBegin, Instant frameEnd) {
        final List<VEvent> positiveEvents = new ArrayList<>();
        final List<VEvent> negativeEvents = new ArrayList<>();
        classifyEvents(positiveEvents, negativeEvents);

        final List<VEventWPeriod> eventList = new ArrayList<>();
        for (final VEvent positiveEvent : positiveEvents) {
            final DateIterator positiveBeginDates = getRecurredEventDateIterator(positiveEvent);
            positiveBeginDates.advanceTo(Date.from(frameBegin));
            while (positiveBeginDates.hasNext()) {
                final Instant begInst = positiveBeginDates.next().toInstant();
                if (begInst.isAfter(frameEnd)) {
                    break;
                }
                Duration duration = getEventLength(positiveEvent);
                if (duration == null) {
                    duration = Duration.ZERO;
                }

                final VEventWPeriod resultingVEWP = new VEventWPeriod(positiveEvent, begInst, begInst.plus(duration));
                final Uid eventUid = positiveEvent.getUid();
                if (eventUid != null) {
                    if (!isCounteredBy(begInst, eventUid, negativeEvents)) {
                        eventList.add(resultingVEWP);
                    }
                } else {
                    eventList.add(resultingVEWP);
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
            final Collection<VEvent> positiveOrNegativeEvents = (positive ? positiveEvents : negativeEvents);
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
        final List<VEvent> negativeEvents = new ArrayList<VEvent>();
        final List<VEvent> positiveEvents = new ArrayList<VEvent>();
        classifyEvents(positiveEvents, negativeEvents);

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
        final DurationProperty duration = vEvent.getDuration();
        if (duration != null) {
            final biweekly.util.Duration eventDuration = duration.getValue();
            return Duration.ofMillis(eventDuration.toMillis());
        }
        final DateStart start = vEvent.getDateStart();
        final DateEnd end = vEvent.getDateEnd();
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
     * Checks whether an counter event blocks an event with given uid and start.
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
