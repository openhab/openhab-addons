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
 * A participant as returned by the Live Tennis API: either an individual player or, when
 * {@link #isDoublesTeam} is {@code true}, a doubles team.
 *
 * A doubles team is a single participant, not two merged players: {@link #name} is the pairing
 * (for example {@code "Bopanna / Ebden"}) and the per-individual biography fields ({@link #country},
 * {@link #ranking}, {@link #rankingPoints}, {@link #rankingMovement}) carry the team's values where the
 * feed states them and are {@code null} otherwise — the API does not attempt to reconcile the two
 * individuals' countries or rankings into one record.
 *
 * @author Ben Abulafia - Initial contribution
 */
public class Player {

    public @Nullable Long id;
    public @Nullable String name;
    public @Nullable String country;
    public @Nullable Integer ranking;
    public @Nullable Integer rankingPoints;
    public @Nullable String rankingMovement;
    public @Nullable Boolean isDoublesTeam;
}
