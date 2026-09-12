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

import org.eclipse.jdt.annotation.Nullable;

/**
 * A match as returned by the Live Tennis API. Singles and doubles share this shape; {@link #draw} states which it
 * is, and for a doubles match {@link #players} holds the two teams.
 *
 * @author Ben Abulafia - Initial contribution
 */
public class Match {

    public @Nullable Long id;
    public @Nullable String tournament;
    public @Nullable String tournamentId;
    public @Nullable String tour;
    public @Nullable String surface;
    public @Nullable String round;
    public @Nullable String roundCode;
    public @Nullable String status;
    public @Nullable String eventStatus;

    /** Whether this is a doubles match. Kept for compatibility and lossy (false also covers "unknown"). */
    public @Nullable Boolean isDoubles;

    /** The honest three-valued draw: {@code "singles"}, {@code "doubles"}, or null when the feed states neither. */
    public @Nullable String draw;

    public @Nullable String scheduledTime;
    public @Nullable MatchPlayers players;
    public @Nullable Score score;
    public @Nullable Integer winner;
}
