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

/**
 * A match as returned by the Live Tennis API.
 *
 * @author Ben - Initial contribution
 */
public class Match {

    public Long id;
    public String tournament;
    public String tournamentId;
    public String tour;
    public String surface;
    public String round;
    public String roundCode;
    public String status;
    public String eventStatus;
    public Boolean isDoubles;
    public String scheduledTime;
    public MatchPlayers players;
    public Score score;
    public Integer winner;
}
