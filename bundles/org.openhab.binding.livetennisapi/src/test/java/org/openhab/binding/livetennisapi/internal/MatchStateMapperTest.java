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
package org.openhab.binding.livetennisapi.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livetennisapi.internal.api.dto.Score;

/**
 * Tests the mapping of {@link Score} snapshots to channel value semantics.
 *
 * @author Ben - Initial contribution
 */
@NonNullByDefault
public class MatchStateMapperTest {

    private static Score score(@Nullable Integer server, @Nullable List<@Nullable String> points, boolean tiebreak) {
        Score score = new Score();
        score.sets = List.of(1, 0);
        score.games = List.of(List.of(6, 3), List.of(4, 2));
        score.points = points;
        score.server = server;
        score.isTiebreak = tiebreak;
        return score;
    }

    /** Builds a points list that may contain nulls, the state the API documents on completed matches. */
    private static List<@Nullable String> points(@Nullable String p1, @Nullable String p2) {
        List<@Nullable String> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        return points;
    }

    @Test
    public void scoreLineListsThePerspectiveSideFirst() {
        Score score = score(1, points("15", "40"), false);
        assertEquals("6-4 3-2", MatchStateMapper.scoreLine(score, 1));
        assertEquals("4-6 2-3", MatchStateMapper.scoreLine(score, 2));
    }

    @Test
    public void scoreLineIsNullOnEmptyGames() {
        Score score = score(1, points("15", "40"), false);
        score.games = List.of();
        assertNull(MatchStateMapper.scoreLine(score, 1));
        score.games = List.of(List.of(), List.of());
        assertNull(MatchStateMapper.scoreLine(score, 1));
        assertNull(MatchStateMapper.scoreLine(null, 1));
    }

    @Test
    public void setsLineListsThePerspectiveSideFirst() {
        Score score = score(1, points("15", "40"), false);
        assertEquals("1-0", MatchStateMapper.setsLine(score, 1));
        assertEquals("0-1", MatchStateMapper.setsLine(score, 2));
        assertNull(MatchStateMapper.setsLine(null, 1));
    }

    @Test
    public void pointsLineListsThePerspectiveSideFirst() {
        Score score = score(1, points("15", "40"), false);
        assertEquals("15-40", MatchStateMapper.pointsLine(score, 1));
        assertEquals("40-15", MatchStateMapper.pointsLine(score, 2));
    }

    @Test
    public void pointsLineIsNullOnNullEntries() {
        // The API documents null point entries, observed live on completed matches
        assertNull(MatchStateMapper.pointsLine(score(1, points(null, null), false), 1));
        assertNull(MatchStateMapper.pointsLine(score(1, points("40", null), false), 1));
        assertNull(MatchStateMapper.pointsLine(score(1, null, false), 1));
    }

    @Test
    public void breakPointWhenReceiverHoldsAd() {
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(1, points("40", "AD"), false)));
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(2, points("AD", "40"), false)));
    }

    @Test
    public void breakPointWhenReceiverHolds40AgainstServerBelow40() {
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(1, points("0", "40"), false)));
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(1, points("15", "40"), false)));
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(1, points("30", "40"), false)));
        assertEquals(Boolean.TRUE, MatchStateMapper.isBreakPoint(score(2, points("40", "30"), false)));
    }

    @Test
    public void noBreakPointAtDeuceOrWithServerAhead() {
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score(1, points("40", "40"), false)));
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score(1, points("AD", "40"), false)));
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score(1, points("40", "30"), false)));
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score(1, points("15", "30"), false)));
    }

    @Test
    public void neverABreakPointInATiebreak() {
        assertEquals(Boolean.FALSE, MatchStateMapper.isBreakPoint(score(2, points("6", "5"), true)));
    }

    @Test
    public void breakPointUnknownWithoutServerOrPoints() {
        assertNull(MatchStateMapper.isBreakPoint(score(null, points("15", "40"), false)));
        assertNull(MatchStateMapper.isBreakPoint(score(1, null, false)));
        assertNull(MatchStateMapper.isBreakPoint(score(1, points(null, null), false)));
        assertNull(MatchStateMapper.isBreakPoint(null));
    }

    @Test
    public void serverPassesThroughOnlyValidSides() {
        assertEquals(1, MatchStateMapper.server(score(1, points("15", "40"), false)));
        assertEquals(2, MatchStateMapper.server(score(2, points("15", "40"), false)));
        assertNull(MatchStateMapper.server(score(null, points("15", "40"), false)));
        assertNull(MatchStateMapper.server(score(3, points("15", "40"), false)));
        assertNull(MatchStateMapper.server(null));
    }
}
