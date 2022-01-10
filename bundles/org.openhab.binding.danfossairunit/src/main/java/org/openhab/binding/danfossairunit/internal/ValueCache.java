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
package org.openhab.binding.danfossairunit.internal;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link ValueCache} is responsible for holding the last value of the channels for a
 * certain amount of time {@link ValueCache#durationMs} to prevent unnecessary event bus updates if the value didn't
 * change.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class ValueCache {

    private Map<String, StateWithTimestamp> stateByValue = new HashMap<>();

    private final long durationMs;

    public ValueCache(long durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * Updates or inserts the given value into the value cache. Returns true if there was no value in the cache
     * for the given channelId or if the value has updated to a different value or if the value is older than
     * the cache duration
     */
    public boolean updateValue(String channelId, State newState) {
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();
        StateWithTimestamp oldState = stateByValue.get(channelId);
        boolean writeToCache;
        if (oldState == null) {
            writeToCache = true;
        } else {
            writeToCache = !oldState.state.equals(newState) || oldState.timestamp < (currentTimeMs - durationMs);
        }
        if (writeToCache) {
            stateByValue.put(channelId, new StateWithTimestamp(newState, currentTimeMs));
        }
        return writeToCache;
    }

    private static class StateWithTimestamp {
        State state;
        long timestamp;

        public StateWithTimestamp(State state, long timestamp) {
            this.state = state;
            this.timestamp = timestamp;
        }
    }
}
