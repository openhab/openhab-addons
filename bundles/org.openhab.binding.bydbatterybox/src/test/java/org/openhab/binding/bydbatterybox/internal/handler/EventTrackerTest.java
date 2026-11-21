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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException;
import org.openhab.binding.bydbatterybox.internal.protocol.BydDecoder;

/**
 * Tests {@link EventTracker}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class EventTrackerTest {

    private final EventTracker tracker = new EventTracker();

    private static List<EventRecord> log() throws BydCommunicationException {
        return BydDecoder.decodeEventLog(1, Fixtures.chunkPayloads("event-bms1", 5));
    }

    @Test
    void firstReadReportsNothing() throws BydCommunicationException {
        assertTrue(tracker.update(1, log()).isEmpty());
    }

    @Test
    void sameLogAgainReportsNothing() throws BydCommunicationException {
        tracker.update(1, log());
        assertTrue(tracker.update(1, log()).isEmpty());
    }

    @Test
    void newEntriesAreReportedOldestFirst() throws BydCommunicationException {
        List<EventRecord> all = log();
        // the previous read ended three entries earlier
        tracker.update(1, all.subList(3, all.size()));

        List<EventRecord> fresh = tracker.update(1, all);

        assertEquals(List.of(all.get(2), all.get(1), all.get(0)), fresh);
    }

    @Test
    void allEntriesAreNewIfThePreviousOneLeftTheWindow() throws BydCommunicationException {
        List<EventRecord> all = log();
        tracker.update(1, all.subList(19, 20));

        assertEquals(19, tracker.update(1, all.subList(0, 19)).size());
    }

    @Test
    void unitsAreTrackedSeparately() throws BydCommunicationException {
        List<EventRecord> all = log();
        tracker.update(1, all.subList(1, all.size()));

        assertTrue(tracker.update(2, all).isEmpty());
        assertEquals(1, tracker.update(1, all).size());
    }

    @Test
    void emptyLogKeepsThePreviousState() throws BydCommunicationException {
        List<EventRecord> all = log();
        tracker.update(1, all.subList(1, all.size()));

        assertTrue(tracker.update(1, List.of()).isEmpty());
        assertEquals(1, tracker.update(1, all).size());
    }
}
