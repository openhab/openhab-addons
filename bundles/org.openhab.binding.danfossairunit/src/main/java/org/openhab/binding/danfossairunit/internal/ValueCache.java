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
package org.openhab.binding.danfossairunit.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link ValueCache} is responsible for holding the last value of the channels for a
 * certain amount of time {@link ValueCache#durationMillis} to prevent unnecessary event bus updates if the value didn't
 * change.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ValueCache {

    private final Map<String, StateWithTimestamp> stateByValue = new HashMap<>();

    private final long durationMillis;

    public ValueCache(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    /**
     * Updates or inserts the given value into the value cache. Returns true if there was no value in the cache
     * for the given channelId or if the value has updated to a different value or if the value is older than
     * the cache duration.
     *
     * @param channelId the channel's id
     * @param state new state
     */
    public boolean updateValue(String channelId, State state) {
        Instant now = Instant.now();
        StateWithTimestamp cachedValue = stateByValue.get(channelId);
        if (cachedValue == null || !state.equals(cachedValue.state)
                || cachedValue.timestamp.isBefore(now.minus(durationMillis, ChronoUnit.MILLIS))) {
            stateByValue.put(channelId, new StateWithTimestamp(state, now));
            return true;
        }
        return false;
    }

    private static class StateWithTimestamp {
        State state;
        Instant timestamp;

        public StateWithTimestamp(State state, Instant timestamp) {
            this.state = state;
            this.timestamp = timestamp;
        }
    }
}
