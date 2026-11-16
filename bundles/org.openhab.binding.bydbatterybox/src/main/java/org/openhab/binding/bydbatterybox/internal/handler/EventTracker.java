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
package org.openhab.binding.bydbatterybox.internal.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;

/**
 * Determines which event log entries are new since the previous read of the same unit.
 * <p>
 * The battery always returns its latest 20 entries, newest first. The first read of a unit only remembers the newest
 * entry, so that a restart does not report old entries again. Not thread-safe; used by the polling job only.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class EventTracker {

    private final Map<Integer, EventRecord> newestSeen = new HashMap<>();

    /**
     * @param newestFirst entries as returned by the battery
     * @return the entries not seen before, oldest first
     */
    List<EventRecord> update(int unit, List<EventRecord> newestFirst) {
        if (newestFirst.isEmpty()) {
            return List.of();
        }
        EventRecord previous = newestSeen.put(unit, newestFirst.getFirst());
        if (previous == null) {
            return List.of();
        }
        List<EventRecord> fresh = new ArrayList<>();
        for (EventRecord record : newestFirst) {
            if (record.equals(previous)) {
                break;
            }
            fresh.add(record);
        }
        // if the previously newest entry is no longer in the window, all entries are new and some were missed
        Collections.reverse(fresh);
        return fresh;
    }
}
