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

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

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

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 1, 0, 0);

    private static EventRecord entry(int minute) {
        return new EventRecord(1, 4, START.plusMinutes(minute), new byte[23]);
    }

    /**
     * @return page {@code index} of a log whose newest entry was written at minute 999, newest first
     */
    private static List<EventRecord> page(int index) {
        int newest = 999 - index * EventTracker.PAGE_SIZE;
        return IntStream.range(0, EventTracker.PAGE_SIZE).mapToObj(i -> entry(newest - i)).toList();
    }

    /** Returns the given pages in order, like consecutive requests to the battery, and counts the requests. */
    private static final class Pages implements EventTracker.PageSource {
        private final Deque<List<EventRecord>> pages;
        int requests;

        @SafeVarargs
        Pages(List<EventRecord>... pages) {
            this.pages = new ArrayDeque<>(List.of(pages));
        }

        @Override
        public List<EventRecord> nextPage() {
            requests++;
            List<EventRecord> page = pages.poll();
            return page != null ? page : List.of();
        }
    }

    @Test
    void firstCollectReadsOnePage() throws Exception {
        Pages pages = new Pages(page(0), page(1));

        assertEquals(page(0), tracker.collect(1, pages));
        assertEquals(1, pages.requests);
    }

    @Test
    void collectReadsOlderPagesUntilThePreviouslyNewestEntry() throws Exception {
        tracker.update(1, page(2));
        Pages pages = new Pages(page(0), page(1), page(2), page(3));

        List<EventRecord> fresh = tracker.update(1, tracker.collect(1, pages));

        assertEquals(3, pages.requests);
        // the 40 entries of pages 0 and 1, oldest first
        assertEquals(2 * EventTracker.PAGE_SIZE, fresh.size());
        assertEquals(page(1).getLast(), fresh.getFirst());
        assertEquals(page(0).getFirst(), fresh.getLast());
    }

    @Test
    void collectIgnoresAPageOlderThanThePreviouslyNewestEntry() throws Exception {
        tracker.update(1, page(0));
        Pages pages = new Pages(page(1), page(2));

        assertTrue(tracker.collect(1, pages).isEmpty());
        assertEquals(1, pages.requests);
        // the tracker did not move: the newest page again reports nothing new
        assertTrue(tracker.update(1, page(0)).isEmpty());
    }

    @Test
    void collectStopsAfterTheMaximumNumberOfPages() throws Exception {
        tracker.update(1, List.of(entry(0)));
        Pages pages = new Pages(page(0), page(1), page(2), page(3), page(4), page(5), page(6));

        List<EventRecord> fresh = tracker.update(1, tracker.collect(1, pages));

        assertEquals(EventTracker.MAX_PAGES, pages.requests);
        assertEquals(EventTracker.MAX_PAGES * EventTracker.PAGE_SIZE, fresh.size());
    }

    @Test
    void collectStopsAtTheEndOfTheLog() throws Exception {
        tracker.update(1, List.of(entry(0)));
        Pages pages = new Pages(page(0), page(1).subList(0, 5), page(2));

        assertEquals(EventTracker.PAGE_SIZE + 5, tracker.collect(1, pages).size());
        assertEquals(2, pages.requests);
    }

    @Test
    void collectStopsWhenTheLogStartsOverWithANewerPage() throws Exception {
        tracker.update(1, List.of(entry(0)));
        Pages pages = new Pages(page(1), page(0));

        assertEquals(page(1), tracker.collect(1, pages));
        assertEquals(2, pages.requests);
    }
}
