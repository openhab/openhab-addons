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
package org.openhab.binding.emby.internal.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EmbyThrottle} is a Utility to throttle high-frequency events on a per-key basis.
 * Ensures updates for each key are only allowed after a configured interval.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyThrottle {

    private final long intervalMillis;
    private final Map<String, Long> lastExecutionTimes = new ConcurrentHashMap<>();

    public EmbyThrottle(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    /**
     * Returns true if the operation associated with the given key should proceed,
     * based on the configured throttle interval.
     *
     * @param key A unique identifier for the event type (e.g. "updateTitle").
     * @return true if enough time has passed since the last execution for this key.
     */
    public boolean shouldProceed(String key) {
        long now = System.currentTimeMillis();
        return lastExecutionTimes.compute(key, (k, lastTime) -> {
            if (lastTime == null || now - lastTime >= intervalMillis) {
                return now; // Allow update and set new timestamp
            } else {
                return lastTime; // Block update
            }
        }) == now;
    }
}
