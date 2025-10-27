/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sagercaster.internal.handler;

import java.time.Duration;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ExpiringMap} is responsible for storing a list of values of class T
 * Values older than eldestAge are discarded at each insert of a new one
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class ExpiringMap<T> {
    private final NavigableMap<Long, T> values = new TreeMap<>();
    private final long windowMillis;

    public ExpiringMap(Duration duration) {
        this.windowMillis = duration.toMillis();
    }

    /**
     * @param newValue - added to the Map
     * @return the eldest value if it existed before insertion
     */
    public Optional<T> put(T newValue) {
        long now = System.currentTimeMillis();

        // removes all entries > 6h
        long cutoff = now - windowMillis;
        while (!values.isEmpty() && values.firstKey() < cutoff) {
            values.pollFirstEntry();
        }

        values.put(now, newValue);
        return Optional.ofNullable(values.size() < 2 ? null : values.firstEntry().getValue());
    }

    /**
     * @return age of the eldest element in minutes
     */
    public long getDataAgeInMin() {
        long now = System.currentTimeMillis();
        return Duration.ofMillis(values.isEmpty() ? 0 : now - values.firstKey()).toMinutes();
    }
}
