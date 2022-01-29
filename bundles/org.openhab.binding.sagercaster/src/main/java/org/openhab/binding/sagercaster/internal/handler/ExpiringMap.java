/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Optional;
import java.util.SortedMap;
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
    private final SortedMap<Long, T> values = new TreeMap<>();
    private Optional<T> agedValue = Optional.empty();
    private long eldestAge = 0;

    public void setObservationPeriod(long eldestAge) {
        this.eldestAge = eldestAge;
    }

    public void put(T newValue) {
        long now = System.currentTimeMillis();
        values.put(now, newValue);
        values.keySet().stream().filter(key -> key < now - eldestAge).findFirst().ifPresent(eldest -> {
            agedValue = Optional.ofNullable(values.get(eldest));
            values.entrySet().removeIf(map -> map.getKey() <= eldest);
        });
    }

    public Optional<T> getAgedValue() {
        return agedValue;
    }
}
