/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics.internal;

import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.TAG_ITEM_NAME;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Limits the number of distinct string states stored per item.
 * <p>
 * Each distinct string state is stored as a separate series, and every new series costs index space and ingestion
 * time in VictoriaMetrics. Items with free text (messages, JSON, timestamps) would create an unbounded number of
 * series, so new states are dropped once an item has reached the configured maximum.
 * <p>
 * The states already stored are read from the database the first time a series is written, so the limit also applies
 * across restarts. Until they can be read, the string states of the item are deferred.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsStringStateLimiter {
    static final int WARN_AFTER_FAILURES = 5;

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsStringStateLimiter.class);
    private final int maxStates;
    private final BiFunction<String, String, @Nullable Set<String>> storedStatesLoader;
    private final Map<SeriesKey, Set<String>> knownStates = new ConcurrentHashMap<>();
    private final Set<SeriesKey> loggedLimitedSeries = ConcurrentHashMap.newKeySet();
    // consecutive failures to read the stored states, per series
    private final Map<SeriesKey, Integer> failures = new ConcurrentHashMap<>();

    /**
     * @param maxStates the maximum number of distinct string states per item
     * @param storedStatesLoader returns the string states stored for a metric name and item name, or
     *            <code>null</code> if they cannot be read
     */
    public VictoriaMetricsStringStateLimiter(int maxStates,
            BiFunction<String, String, @Nullable Set<String>> storedStatesLoader) {
        this.maxStates = maxStates;
        this.storedStatesLoader = storedStatesLoader;
    }

    /**
     * Remove the string states exceeding the limit from the points to write
     * <p>
     * If the stored states of an item cannot be read, its string states are returned as deferred, so they can be
     * retried later without delaying the states of all other items.
     *
     * @param points the points to write
     * @return the points to write now and the points to retry later
     */
    public Result filter(List<VictoriaMetricsPoint> points) {
        List<VictoriaMetricsPoint> accepted = new ArrayList<>(points.size());
        List<VictoriaMetricsPoint> deferred = new ArrayList<>();
        // the stored states are read at most once per call, a failed read is not repeated for the following points
        Set<SeriesKey> failedSeries = new HashSet<>();
        for (VictoriaMetricsPoint point : points) {
            String stringState = point.getStringState();
            // the empty string state is stored without label, so it does not add a label value
            if (stringState == null || stringState.isEmpty()) {
                accepted.add(point);
                continue;
            }
            SeriesKey key = new SeriesKey(point.getMetricName(),
                    Objects.requireNonNullElse(point.getTags().get(TAG_ITEM_NAME), ""));
            Set<String> states = failedSeries.contains(key) ? null : getStates(key);
            if (states == null) {
                if (failedSeries.add(key)) {
                    countFailure(key);
                }
                deferred.add(point);
            } else if (accept(states, stringState)) {
                accepted.add(point);
            } else if (loggedLimitedSeries.add(key)) {
                logger.warn("Item '{}' has reached the maximum of {} distinct string states, new states are not stored",
                        key.itemName(), maxStates);
            }
        }
        return new Result(accepted, deferred);
    }

    private @Nullable Set<String> getStates(SeriesKey key) {
        Set<String> states = knownStates.get(key);
        if (states != null) {
            return states;
        }
        // read outside of any lock, the database request may take a while
        Set<String> storedStates = storedStatesLoader.apply(key.metricName(), key.itemName());
        if (storedStates == null) {
            return null;
        }
        failures.remove(key);
        Set<String> loadedStates = ConcurrentHashMap.newKeySet();
        loadedStates.addAll(storedStates);
        Set<String> existingStates = knownStates.putIfAbsent(key, loadedStates);
        return existingStates != null ? existingStates : loadedStates;
    }

    private void countFailure(SeriesKey key) {
        int count = failures.merge(key, 1, Integer::sum);
        // a single failure is expected when the database is busy, e.g. while openHAB restores the item states
        if (count == WARN_AFTER_FAILURES) {
            logger.warn("Failed {} times in a row to read the stored string states of item '{}', its string states are "
                    + "kept in the queue and retried. Reading them may exceed '-search.maxLabelsAPIDuration' of "
                    + "VictoriaMetrics.", count, key.itemName());
        } else {
            logger.debug("Failed to read the stored string states of item '{}', its string states are retried",
                    key.itemName());
        }
    }

    private boolean accept(Set<String> states, String stringState) {
        synchronized (states) {
            if (states.contains(stringState)) {
                return true;
            }
            if (states.size() >= maxStates) {
                return false;
            }
            states.add(stringState);
            return true;
        }
    }

    /**
     * The result of {@link VictoriaMetricsStringStateLimiter#filter(List)}
     *
     * @param accepted the points to write now
     * @param deferred the string states of items whose stored states could not be read, to retry later
     */
    public record Result(List<VictoriaMetricsPoint> accepted, List<VictoriaMetricsPoint> deferred) {
    }

    private record SeriesKey(String metricName, String itemName) {
    }
}
