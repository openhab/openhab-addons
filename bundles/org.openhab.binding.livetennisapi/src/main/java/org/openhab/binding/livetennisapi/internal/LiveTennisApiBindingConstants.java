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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LiveTennisApiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ben Abulafia - Initial contribution
 */
@NonNullByDefault
public class LiveTennisApiBindingConstants {

    public static final String BINDING_ID = "livetennisapi";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_TOURNAMENT = new ThingTypeUID(BINDING_ID, "tournament");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_PLAYER,
            THING_TYPE_TOURNAMENT);

    // Account (bridge) channels
    public static final String CHANNEL_TIER = "usage#tier";
    public static final String CHANNEL_CALLS_TODAY = "usage#calls-today";
    public static final String CHANNEL_REMAINING_TODAY = "usage#remaining-today";

    // Live match channels, shared by the player and tournament things
    public static final String CHANNEL_LIVE_STATUS = "live#status";
    public static final String CHANNEL_LIVE_DISCIPLINE = "live#discipline";
    public static final String CHANNEL_LIVE_SCORE_LINE = "live#score-line";
    public static final String CHANNEL_LIVE_SETS = "live#sets";
    public static final String CHANNEL_LIVE_POINTS = "live#points";
    public static final String CHANNEL_LIVE_BREAK_POINT = "live#break-point";
    public static final String CHANNEL_LIVE_TIEBREAK = "live#tiebreak";

    // Player thing channels
    public static final String CHANNEL_LIVE_TOURNAMENT = "live#tournament";
    public static final String CHANNEL_LIVE_ROUND = "live#round";
    public static final String CHANNEL_LIVE_OPPONENT = "live#opponent";
    public static final String CHANNEL_LIVE_SERVING = "live#serving";

    public static final String CHANNEL_NEXT_OPPONENT = "next-match#opponent";
    public static final String CHANNEL_NEXT_START_TIME = "next-match#start-time";
    public static final String CHANNEL_NEXT_TOURNAMENT = "next-match#tournament";
    public static final String CHANNEL_NEXT_ROUND = "next-match#round";

    public static final String CHANNEL_PROFILE_RANKING = "profile#ranking";
    public static final String CHANNEL_PROFILE_RANKING_POINTS = "profile#ranking-points";

    // Tournament thing channels
    public static final String CHANNEL_INFO_NAME = "info#name";
    public static final String CHANNEL_INFO_SURFACE = "info#surface";
    public static final String CHANNEL_INFO_CATEGORY = "info#category";

    public static final String CHANNEL_LIVE_MATCH_COUNT = "live#match-count";
    public static final String CHANNEL_LIVE_PLAYERS = "live#players";
    public static final String CHANNEL_LIVE_SERVER = "live#server";
}
