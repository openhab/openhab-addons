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
package org.openhab.binding.livetennisapi.internal.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livetennisapi.internal.MatchStateMapper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests that the API's documented JSON shapes — including the documented null states and doubles matches —
 * deserialize into the DTOs and map to the expected channel values.
 *
 * @author Ben Abulafia - Initial contribution
 */
@NonNullByDefault
public class DeserializationTest {

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private <T> T fromResource(String filename, Class<T> type) throws IOException {
        try (InputStream inputStream = DeserializationTest.class.getResourceAsStream("/" + filename)) {
            assertNotNull(inputStream, "Missing test resource " + filename);
            @Nullable
            T result = gson.fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), type);
            assertNotNull(result);
            return Objects.requireNonNull(result);
        }
    }

    private List<Match> matches() throws IOException {
        MatchListResponse response = fromResource("matches-live.json", MatchListResponse.class);
        return Objects.requireNonNull(response.data);
    }

    @Test
    public void deserializesListPaginationMeta() throws IOException {
        MatchListResponse response = fromResource("matches-live.json", MatchListResponse.class);

        MatchListResponse.Meta meta = Objects.requireNonNull(response.meta);
        assertEquals(200, meta.limit);
        assertEquals(0, meta.offset);
        assertEquals(4, meta.count);
        assertEquals(4, meta.total);
        // has_more is the flag the client pages on; it must map from snake_case
        assertEquals(Boolean.FALSE, meta.hasMore);
    }

    @Test
    public void deserializesLiveMatchWithScore() throws IOException {
        List<Match> matches = matches();
        assertEquals(4, matches.size());

        Match match = matches.get(0);
        assertEquals(910001L, match.id);
        assertEquals("Cincinnati Open", match.tournament);
        assertEquals("atp-cincinnati", match.tournamentId);
        assertEquals("QF", match.roundCode);
        assertEquals("live", match.status);
        assertNull(match.eventStatus);

        MatchPlayers players = Objects.requireNonNull(match.players);
        Player p1 = Objects.requireNonNull(players.p1);
        assertEquals(3333L, p1.id);
        assertEquals("Player One", p1.name);
        assertEquals(5, p1.ranking);
        assertEquals(4980, p1.rankingPoints);
        assertEquals("Player Two", Objects.requireNonNull(players.p2).name);

        Score score = Objects.requireNonNull(match.score);
        assertEquals(List.of(1, 0), score.sets);
        assertEquals(1, score.server);
        assertEquals(Boolean.FALSE, score.isTiebreak);
        assertEquals("6-4 3-2", MatchStateMapper.scoreLine(score, 1));
        assertEquals("1-0", MatchStateMapper.setsLine(score, 1));
        assertEquals("15-40", MatchStateMapper.pointsLine(score, 1));
        // Server at 15, receiver at 40
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score));
    }

    @Test
    public void deserializesTiebreakScore() throws IOException {
        Match match = matches().get(1);

        Score score = Objects.requireNonNull(match.score);
        assertEquals(Boolean.TRUE, score.isTiebreak);
        assertEquals(2, score.server);
        assertEquals("6-6", MatchStateMapper.scoreLine(score, 1));
        assertEquals("6-5", MatchStateMapper.pointsLine(score, 1));
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score));
    }

    @Test
    public void deserializesDocumentedNullStates() throws IOException {
        Match match = matches().get(2);

        assertNull(match.tournamentId);
        assertNull(match.scheduledTime);
        assertEquals("Interrupted", match.eventStatus);
        MatchPlayers players = Objects.requireNonNull(match.players);
        assertNull(Objects.requireNonNull(players.p1).ranking);

        Score score = Objects.requireNonNull(match.score);
        assertNull(score.server);
        List<@Nullable String> points = Objects.requireNonNull(score.points);
        assertEquals(2, points.size());
        assertNull(points.get(0));
        assertNull(points.get(1));
        assertNull(MatchStateMapper.scoreLine(score, 1));
        assertNull(MatchStateMapper.pointsLine(score, 1));
        assertNull(MatchStateMapper.isBreakPoint(score));
        assertNull(MatchStateMapper.server(score));
        assertEquals("1-1", MatchStateMapper.setsLine(score, 1));
    }

    @Test
    public void deserializesDoublesMatch() throws IOException {
        Match match = matches().get(3);

        assertEquals(Boolean.TRUE, match.isDoubles);
        assertEquals("doubles", match.draw);
        assertEquals("doubles", MatchStateMapper.discipline(match));

        MatchPlayers players = Objects.requireNonNull(match.players);
        Player team1 = Objects.requireNonNull(players.p1);
        // A doubles team is one participant: the pairing is the name, and per-individual biography is not applicable
        assertEquals("Bopanna / Ebden", team1.name);
        assertEquals(Boolean.TRUE, team1.isDoublesTeam);
        assertNull(team1.country);

        Score score = Objects.requireNonNull(match.score);
        assertEquals("0-1", MatchStateMapper.setsLine(score, 1));
    }

    @Test
    public void deserializesUsage() throws IOException {
        Usage usage = fromResource("usage.json", Usage.class);

        assertEquals("free", usage.tier);
        Usage.Limits limits = Objects.requireNonNull(usage.limits);
        assertEquals(30, limits.perMinute);
        assertEquals(100, limits.perDay);
        Usage.Today today = Objects.requireNonNull(usage.today);
        assertEquals(42, today.calls);
        assertEquals(58, today.remainingDay);
        assertEquals("2026-08-15T20:02:11Z", usage.asOf);
    }
}
