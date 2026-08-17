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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.livetennisapi.internal.api.dto.Score;

/**
 * Maps a {@link Score} snapshot to the channel value semantics of this binding.
 *
 * All methods return null when the underlying data does not allow an honest answer — the API documents that
 * {@code points} entries and {@code server} can be null and that {@code games} can be empty, and this binding maps
 * those states to UNDEF rather than guessing.
 *
 * @author Ben - Initial contribution
 */
@NonNullByDefault
public final class MatchStateMapper {

    private static final int SIDE_P1 = 1;
    private static final int SIDE_P2 = 2;

    private MatchStateMapper() {
    }

    /**
     * Formats the per-set games as one line, e.g. "6-4 3-2".
     *
     * @param perspective side (1 or 2) whose games are listed first
     */
    public static @Nullable String scoreLine(@Nullable Score score, int perspective) {
        List<@Nullable List<@Nullable Integer>> games = score == null ? null : score.games;
        if (games == null || games.size() < 2) {
            return null;
        }
        List<@Nullable Integer> own = games.get(perspective == SIDE_P2 ? 1 : 0);
        List<@Nullable Integer> other = games.get(perspective == SIDE_P2 ? 0 : 1);
        if (own == null || other == null) {
            return null;
        }
        int sets = Math.min(own.size(), other.size());
        if (sets == 0) {
            return null;
        }
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < sets; i++) {
            Integer ownGames = own.get(i);
            Integer otherGames = other.get(i);
            if (ownGames == null || otherGames == null) {
                return null;
            }
            if (i > 0) {
                line.append(' ');
            }
            line.append(ownGames).append('-').append(otherGames);
        }
        return line.toString();
    }

    /**
     * Formats the sets won as one line, e.g. "1-0".
     *
     * @param perspective side (1 or 2) whose sets are listed first
     */
    public static @Nullable String setsLine(@Nullable Score score, int perspective) {
        List<@Nullable Integer> sets = score == null ? null : score.sets;
        if (sets == null || sets.size() < 2) {
            return null;
        }
        Integer own = sets.get(perspective == SIDE_P2 ? 1 : 0);
        Integer other = sets.get(perspective == SIDE_P2 ? 0 : 1);
        if (own == null || other == null) {
            return null;
        }
        return own + "-" + other;
    }

    /**
     * Formats the in-game points as one line, e.g. "40-15" or "AD-40". The API documents that entries can be null
     * (observed on completed matches); in that case null is returned.
     *
     * @param perspective side (1 or 2) whose points are listed first
     */
    public static @Nullable String pointsLine(@Nullable Score score, int perspective) {
        List<@Nullable String> points = score == null ? null : score.points;
        if (points == null || points.size() < 2) {
            return null;
        }
        String own = points.get(perspective == SIDE_P2 ? 1 : 0);
        String other = points.get(perspective == SIDE_P2 ? 0 : 1);
        if (own == null || other == null) {
            return null;
        }
        return own + "-" + other;
    }

    /**
     * Derives whether the current game stands at break point: the receiver holds AD, or the receiver holds 40 while
     * the server holds 0, 15 or 30. There are no break points in a tiebreak. Returns null (unknown) when the server
     * or the points are not stated.
     */
    public static @Nullable Boolean isBreakPoint(@Nullable Score score) {
        if (score == null) {
            return null;
        }
        if (Boolean.TRUE.equals(score.isTiebreak)) {
            return Boolean.FALSE;
        }
        Integer server = score.server;
        List<@Nullable String> points = score.points;
        if (server == null || (server != SIDE_P1 && server != SIDE_P2) || points == null || points.size() < 2) {
            return null;
        }
        String serverPoints = points.get(server - 1);
        String receiverPoints = points.get(server == SIDE_P1 ? 1 : 0);
        if (serverPoints == null || receiverPoints == null) {
            return null;
        }
        return "AD".equals(receiverPoints) || ("40".equals(receiverPoints)
                && ("0".equals(serverPoints) || "15".equals(serverPoints) || "30".equals(serverPoints)));
    }

    /** Returns the serving side (1 or 2), or null when no game is in progress or the side is not stated. */
    public static @Nullable Integer server(@Nullable Score score) {
        Integer server = score == null ? null : score.server;
        return server == null || (server != SIDE_P1 && server != SIDE_P2) ? null : server;
    }
}
