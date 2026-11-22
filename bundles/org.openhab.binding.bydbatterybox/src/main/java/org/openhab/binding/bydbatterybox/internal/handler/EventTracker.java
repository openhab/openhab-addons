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
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException;

/**
 * Determines which event log entries are new since the previous read of the same unit.
 * <p>
 * The battery returns its event log in pages of 20 entries, newest first. Each request returns the next older page;
 * only after a pause of about two minutes without requests does the battery start at the newest page again. The first
 * read of a unit only remembers the newest entry, so that a restart does not report old entries again. Not
 * thread-safe; used by the polling job only.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class EventTracker {

    static final int PAGE_SIZE = 20;
    static final int MAX_PAGES = 5;

    @FunctionalInterface
    interface PageSource {
        List<EventRecord> nextPage() throws BydCommunicationException, InterruptedException;
    }

    private final Map<Integer, EventRecord> newestSeen = new HashMap<>();

    /**
     * Reads the pages of one unit until they reach the newest entry seen before, so that no entry is lost if more than
     * one page of entries was written since. Does not change the tracker; pass the result to {@link #update}.
     *
     * @return the entries of the pages read, newest first; empty if the first page is older than the newest entry seen
     *         before, i.e. the battery did not start at its newest page
     */
    List<EventRecord> collect(int unit, PageSource source) throws BydCommunicationException, InterruptedException {
        EventRecord previous = newestSeen.get(unit);
        List<EventRecord> page = source.nextPage();
        if (previous != null && !page.isEmpty() && page.getFirst().getTimestamp().isBefore(previous.getTimestamp())) {
            return List.of();
        }
        List<EventRecord> entries = new ArrayList<>(page);
        for (int pages = 1; previous != null && pages < MAX_PAGES && page.size() == PAGE_SIZE
                && !reaches(page, previous); pages++) {
            List<EventRecord> next = source.nextPage();
            // an empty or newer page means the end of the log was passed
            if (next.isEmpty() || next.getFirst().getTimestamp().isAfter(page.getLast().getTimestamp())) {
                break;
            }
            entries.addAll(next);
            page = next;
        }
        return entries;
    }

    private static boolean reaches(List<EventRecord> page, EventRecord previous) {
        return page.contains(previous) || page.getLast().getTimestamp().isBefore(previous.getTimestamp());
    }

    /**
     * @param newestFirst entries as returned by {@link #collect}
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
        // if the previously newest entry was not reached, all entries are new and some were missed
        Collections.reverse(fresh);
        return fresh;
    }
}
