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

import java.util.List;

/**
 * The current score snapshot of a match as returned by the Live Tennis API.
 *
 * All fields are nullable: the API documents that {@code points} entries can be null, {@code games} can be empty
 * (observed on completed matches) and {@code server} is null when no game is in progress.
 *
 * @author Ben - Initial contribution
 */
public class Score {

    /** Sets won so far: {@code [sets_p1, sets_p2]}. */
    public List<Integer> sets;

    /** Games per set: {@code [games_p1, games_p2]}, each a per-set list. */
    public List<List<Integer>> games;

    /** In-game points as tennis strings ("0", "15", "30", "40", "AD"); entries can be null. */
    public List<String> points;

    /** Which side serves (1 or 2), null when no game is in progress. */
    public Integer server;

    public Boolean isTiebreak;
    public String timestamp;
}
