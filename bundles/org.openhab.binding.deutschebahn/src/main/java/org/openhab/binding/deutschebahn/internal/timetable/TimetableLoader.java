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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.filter.TimetableStopPredicate;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Event;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Timetable;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.library.types.DateTimeType;

/**
 * Helper for loading the required amount of {@link TimetableStop} via a {@link TimetablesV1Api}.
 * This consists of a series of calls.
 *
 * @author Sönke Küper - initial contribution
 */
@NonNullByDefault
public final class TimetableLoader {

    // The api provides at most 18 hours in advance.
    private static final int MAX_ADVANCE_HOUR = 18;

    // The recent changes only contains all changes done within the last 2 minutes.
    private static final int MAX_RECENT_CHANGE_UPDATE = 120;

    // The min. request interval for recent changes is 30 seconds.
    private static final int MIN_RECENT_CHANGE_INTERVAL = 30;

    // Cache containing the TimetableStops per ID
    private final Map<String, TimetableStop> cachedStopsPerId;
    private final Map<String, TimetableStop> cachedChanges;

    private final TimetablesV1Api api;
    private final TimetableStopPredicate stopPredicate;
    private final TimetableStopComparator comparator;
    private final Supplier<Date> currentTimeProvider;
    private int stopCount;

    private final String evaNo;

    @Nullable
    private Date lastRequestedPlan;
    @Nullable
    private Date lastRequestedChanges;

    /**
     * Creates a new {@link TimetableLoader}.
     *
     * @param api {@link TimetablesV1Api} to use.
     * @param stopPredicate Filter for selection of loaded {@link TimetableStop}.
     * @param requestedStopCount Count of stops to be loaded on each call.
     * @param currentTimeProvider {@link Supplier} for the current time.
     */
    public TimetableLoader(final TimetablesV1Api api, final TimetableStopPredicate stopPredicate,
            final EventType eventToSort, final Supplier<Date> currentTimeProvider, final String evaNo,
            final int requestedStopCount) {
        this.api = api;
        this.stopPredicate = stopPredicate;
        this.currentTimeProvider = currentTimeProvider;
        this.evaNo = evaNo;
        this.stopCount = requestedStopCount;
        this.comparator = new TimetableStopComparator(eventToSort);
        this.cachedStopsPerId = new HashMap<>();
        this.cachedChanges = new HashMap<>();
        this.lastRequestedChanges = null;
        this.lastRequestedPlan = null;
    }

    /**
     * Sets the count of needed {@link TimetableStop} that is required at each call of {@link #getTimetableStops()}.
     */
    public void setStopCount(int stopCount) {
        this.stopCount = stopCount;
    }

    /**
     * Updates the cache with current data from plan and changes and returns the {@link TimetableStop}.
     */
    public List<TimetableStop> getTimetableStops() throws IOException {
        this.updateCache();
        final List<TimetableStop> result = new ArrayList<>(this.cachedStopsPerId.values());
        Collections.sort(result, this.comparator);
        return result;
    }

    /**
     * Updates the cached {@link TimetableStop} to ensure that the requested amount of stops is available.
     */
    private void updateCache() throws IOException {
        final Date currentTime = this.currentTimeProvider.get();

        // First update the changes. This will merge them into the existing plan data
        // or cache them, if no corresponding stop is available.
        this.updateChanges(currentTime);

        // Remove all stops that are in the past
        this.removeOldStops(currentTime);

        // Finally fill up plan until required amount of data is available.
        this.updatePlan(currentTime);
    }

    /**
     * Removes all stops from the cache with planned and changed time after the current time.
     */
    private void removeOldStops(final Date currentTime) {
        final Iterator<Entry<String, TimetableStop>> it = this.cachedStopsPerId.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, TimetableStop> currentEntry = it.next();
            final TimetableStop stop = currentEntry.getValue();

            // Remove entry if planned and changed time are in the past
            if (isInPast(stop, currentTime)) {
                it.remove();
            }
        }
    }

    /**
     * Returns <code>true</code> if the planned and changed time from arrival and departure are in the past.
     */
    private static boolean isInPast(TimetableStop stop, Date currentTime) {
        return isBefore(EventAttribute.PT, stop.getAr(), currentTime) //
                && isBefore(EventAttribute.CT, stop.getAr(), currentTime) //
                && isBefore(EventAttribute.PT, stop.getDp(), currentTime) //
                && isBefore(EventAttribute.PT, stop.getDp(), currentTime);
    }

    /**
     * Checks if the value of the given {@link EventAttribute} is either <code>null</code> or before
     * the given compareTime.
     * If the {@link Event} is <code>null</code> it will return <code>true</code>.
     */
    private static boolean isBefore( //
            final EventAttribute<Date, DateTimeType> attribute, //
            final @Nullable Event event, //
            final Date toCompare) {
        if (event == null) {
            return true;
        }
        final Date value = attribute.getValue(event);
        if (value == null) {
            return true;
        } else {
            return value.before(toCompare);
        }
    }

    /**
     * Checks if enough plan entries are available and loads them from the backing {@link TimetablesV1Api} if required.
     */
    private void updatePlan(final Date currentTime) throws IOException {
        // If enough stops are available in cache do nothing.
        if (this.cachedStopsPerId.size() >= this.stopCount) {
            return;
        }

        // start requesting at last request time.
        final GregorianCalendar requestTime = new GregorianCalendar();
        if (this.lastRequestedPlan != null) {
            requestTime.setTime(this.lastRequestedPlan);
            requestTime.set(Calendar.HOUR_OF_DAY, requestTime.get(Calendar.HOUR_OF_DAY) + 1);
        } else {
            requestTime.setTime(currentTime);
        }

        // Determine the max. time for which a plan is available
        final GregorianCalendar maxRequestTime = new GregorianCalendar();
        maxRequestTime.setTime(currentTime);
        maxRequestTime.set(Calendar.HOUR_OF_DAY, maxRequestTime.get(Calendar.HOUR_OF_DAY) + MAX_ADVANCE_HOUR);

        // load until required amount of stops is present or no more data is available.
        while ((this.cachedStopsPerId.size() < this.stopCount) && requestTime.before(maxRequestTime)) {
            final Timetable timetable = this.api.getPlan(this.evaNo, requestTime.getTime());
            this.lastRequestedPlan = requestTime.getTime();

            // Filter only stops that are selected by given filter
            final List<TimetableStop> stops = timetable //
                    .getS() //
                    .stream() //
                    .filter(this.stopPredicate) //
                    .collect(Collectors.toList());

            // Merge the loaded stops with the cached changes and put them into the plan cache.
            this.processLoadedPlan(stops, currentTime);

            // Move request time one hour ahead.
            requestTime.set(Calendar.HOUR_OF_DAY, requestTime.get(Calendar.HOUR_OF_DAY) + 1);
        }
    }

    /**
     * Merges the loaded plan stops with the previously cached changes.
     * The result will be cached as plan data, if not in the past.
     */
    private void processLoadedPlan(List<TimetableStop> stops, Date currentTime) {
        for (final TimetableStop stop : stops) {

            // Check if a change for the stop was cached and apply it
            final TimetableStop change = this.cachedChanges.remove(stop.getId());
            if (change != null) {
                TimetableStopMerger.merge(stop, change);
            }

            // Check if stop is in past after applying changes and put
            // into cached plan if not.
            if (!isInPast(stop, currentTime)) {
                this.cachedStopsPerId.put(stop.getId(), stop);
            }
        }
    }

    /**
     * Loads the changes from the api and merges them into the cached plan entries.
     */
    private void updateChanges(final Date currentTime) throws IOException {
        final List<TimetableStop> changes = this.loadChanges(currentTime);
        this.processChanges(changes);
    }

    /**
     * Merges the given {@link TimetableStop} into the cached plan.
     * If no stop in the plan for the change exist it will be put into the changes cache.
     */
    private void processChanges(final List<TimetableStop> changes) {
        for (final TimetableStop change : changes) {

            final TimetableStop existingEntry = this.cachedStopsPerId.get(change.getId());
            if (existingEntry != null) {
                TimetableStopMerger.merge(existingEntry, change);
            } else {
                this.cachedChanges.put(change.getId(), change);
            }
        }
    }

    /**
     * Loads the full or recent changes depending on last request time.
     */
    private List<TimetableStop> loadChanges(final Date currentTime) throws IOException {
        boolean fullChanges = false;
        final long secondsSinceLastUpdate = this.getSecondsSinceLastRequestedChanges(currentTime);

        // The recent changes are updated every 30 seconds, so if last update is less than 30 seconds do nothing.
        if (secondsSinceLastUpdate < MIN_RECENT_CHANGE_INTERVAL) {
            return Collections.emptyList();
        }

        // The recent changes are only available for 120 seconds, so if last update is older perform a full update.
        if (secondsSinceLastUpdate >= MAX_RECENT_CHANGE_UPDATE) {
            fullChanges = true;
        }

        Timetable changes;
        if (fullChanges) {
            changes = this.api.getFullChanges(this.evaNo);
        } else {
            changes = this.api.getRecentChanges(this.evaNo);
        }
        this.lastRequestedChanges = currentTime;
        return changes.getS();
    }

    @SuppressWarnings("null")
    private long getSecondsSinceLastRequestedChanges(final Date currentTime) {
        if (this.lastRequestedChanges == null) {
            return Long.MAX_VALUE;
        } else {
            return ChronoUnit.SECONDS.between(this.lastRequestedChanges.toInstant(), currentTime.toInstant());
        }
    }
}
