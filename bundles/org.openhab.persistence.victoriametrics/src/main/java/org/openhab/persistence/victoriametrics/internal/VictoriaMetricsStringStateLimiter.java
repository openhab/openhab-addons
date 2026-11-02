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
 * across restarts.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsStringStateLimiter {
    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsStringStateLimiter.class);
    private final int maxStates;
    private final BiFunction<String, String, @Nullable Set<String>> storedStatesLoader;
    private final Map<SeriesKey, Set<String>> knownStates = new ConcurrentHashMap<>();
    private final Set<SeriesKey> loggedLimitedSeries = ConcurrentHashMap.newKeySet();

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
     *
     * @param points the points to write
     * @return the points to write, or <code>null</code> if the stored states could not be read and the points should
     *         be retried later
     */
    public @Nullable List<VictoriaMetricsPoint> filter(List<VictoriaMetricsPoint> points) {
        List<VictoriaMetricsPoint> result = new ArrayList<>(points.size());
        for (VictoriaMetricsPoint point : points) {
            String stringState = point.getStringState();
            if (stringState == null) {
                result.add(point);
                continue;
            }
            SeriesKey key = new SeriesKey(point.getMetricName(),
                    Objects.requireNonNullElse(point.getTags().get(TAG_ITEM_NAME), ""));
            Set<String> states = knownStates.get(key);
            if (states == null) {
                // read outside of any lock, the database request may take a while
                Set<String> storedStates = storedStatesLoader.apply(key.metricName(), key.itemName());
                if (storedStates == null) {
                    return null;
                }
                Set<String> loadedStates = ConcurrentHashMap.newKeySet();
                loadedStates.addAll(storedStates);
                Set<String> existingStates = knownStates.putIfAbsent(key, loadedStates);
                states = existingStates != null ? existingStates : loadedStates;
            }
            if (accept(states, stringState)) {
                result.add(point);
            } else if (loggedLimitedSeries.add(key)) {
                logger.warn("Item '{}' has reached the maximum of {} distinct string states, new states are not stored",
                        key.itemName(), maxStates);
            }
        }
        return result;
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
     * Forget the states of an item, e.g. after its data was removed
     *
     * @param itemName the item name or alias
     */
    public void forget(String itemName) {
        knownStates.keySet().removeIf(key -> key.itemName().equals(itemName));
        loggedLimitedSeries.removeIf(key -> key.itemName().equals(itemName));
    }

    private record SeriesKey(String metricName, String itemName) {
    }
}
