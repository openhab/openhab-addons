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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livetennisapi.internal.api.dto.Match;
import org.openhab.binding.livetennisapi.internal.api.dto.MatchListResponse;

/**
 * Tests the live-match paging: the client must follow {@code meta.has_more} across pages rather than treating the
 * first page as a complete snapshot, and must stop rather than fan out unbounded requests.
 *
 * @author Ben Synapse - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiClientTest {

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
            return offset == 0 ? page(200, true) : page(5, false);
        });

        assertEquals(205, matches.size());
        assertEquals(List.of(0, 200), requestedOffsets);
    }

    @Test
    public void stopsAtThePageCapWhenTheApiKeepsReportingMore() throws LiveTennisApiException {
        List<Integer> requestedOffsets = new ArrayList<>();
        List<Match> matches = client.collectLiveMatches(offset -> {
            requestedOffsets.add(offset);
            return page(200, true);
        });

        // The loop must terminate at the cap rather than spin forever, and never drops the matches it did read.
        assertEquals(200 * requestedOffsets.size(), matches.size());
        assertTrue(requestedOffsets.size() >= 2 && requestedOffsets.size() <= 5,
                "Expected a small, capped number of page fetches but made " + requestedOffsets.size());
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
}
