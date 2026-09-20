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
package org.openhab.binding.livetennisapi.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.MatchListResponse;

/**
 * Tests the live-match paging: the client must follow {@code meta.has_more} across pages until the snapshot is
 * complete, and must fail rather than return a partial snapshot when the page bound is reached.
 *
 * @author Ben Abulafia - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiClientTest {

    private static final int PAGE_SIZE = LiveTennisApiClient.LIVE_MATCH_PAGE_SIZE;

    private final LiveTennisApiClient client = new LiveTennisApiClient(new HttpClient(), "test-key");

    private static MatchListResponse page(int count, boolean hasMore) {
        MatchListResponse response = new MatchListResponse();
        List<Match> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add(new Match());
        }
        response.data = data;
        MatchListResponse.Meta meta = new MatchListResponse.Meta();
        meta.hasMore = hasMore;
        response.meta = meta;
        return response;
    }

    @Test
    public void singlePageSnapshotReturnsEverythingInOneFetch() throws LiveTennisApiException {
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            return page(3, false);
        });
        assertEquals(3, matches.size());
        assertEquals(List.of(0), requestedOffsets);
    }

    @Test
    public void pagesForwardWhileHasMoreIsTrue() throws LiveTennisApiException {
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            // First page full and flags more; second page is the tail.
            return offset == 0 ? page(PAGE_SIZE, true) : page(5, false);
        });
        assertEquals(PAGE_SIZE + 5, matches.size());
        assertEquals(List.of(0, PAGE_SIZE), requestedOffsets);
    }

    @Test
    public void pagesThroughManyPagesUntilTheSnapshotIsComplete() throws LiveTennisApiException {
        int fullPages = 7;
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            return offset < fullPages * PAGE_SIZE ? page(PAGE_SIZE, true) : page(1, false);
        });
        assertEquals(fullPages * PAGE_SIZE + 1, matches.size());
        assertEquals(fullPages + 1, requestedOffsets.size());
        for (int i = 0; i < requestedOffsets.size(); i++) {
            assertEquals(i * PAGE_SIZE, requestedOffsets.get(i));
        }
    }

    @Test
    public void failsInsteadOfReturningAPartialSnapshotAtThePageBound() {
        List<Integer> requestedOffsets = new ArrayList<>();
        assertThrows(LiveTennisApiException.class, () -> client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            return page(PAGE_SIZE, true);
        }));
        // The loop must terminate at the bound rather than spin forever, and must not hand back what it read so far.
        assertEquals(LiveTennisApiClient.MAX_LIVE_PAGES, requestedOffsets.size());
    }

    @Test
    public void fallsBackToPageFillWhenMetaIsAbsent() throws LiveTennisApiException {
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            MatchListResponse response = new MatchListResponse();
            List<Match> data = new ArrayList<>();
            data.add(new Match());
            response.data = data; // no meta: a short page is treated as the final one
            return response;
        });
        assertEquals(1, matches.size());
        assertEquals(List.of(0), requestedOffsets);
    }

    @Test
    public void fallsBackToPageFillAndStopsOnTheFirstEmptyPageWhenMetaIsAbsent() throws LiveTennisApiException {
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            MatchListResponse response = new MatchListResponse();
            List<Match> data = new ArrayList<>();
            if (offset == 0) {
                for (int i = 0; i < PAGE_SIZE; i++) {
                    data.add(new Match());
                }
            }
            response.data = data; // no meta: a full page is followed up, an empty page ends the snapshot
            return response;
        });
        assertEquals(PAGE_SIZE, matches.size());
        assertEquals(List.of(0, PAGE_SIZE), requestedOffsets);
    }
}
