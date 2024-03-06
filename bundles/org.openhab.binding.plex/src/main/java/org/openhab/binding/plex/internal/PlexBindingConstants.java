/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PlexBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Binding development
 */
@NonNullByDefault
public class PlexBindingConstants {
    private static final String BINDING_ID = "plex";

    // Bridge thing
    public static final String THING_TYPE_SERVER = "server";
    public static final ThingTypeUID UID_SERVER = new ThingTypeUID(BINDING_ID, THING_TYPE_SERVER);
    public static final Set<ThingTypeUID> SUPPORTED_SERVER_THING_TYPES_UIDS = Set.of(UID_SERVER);

    // Monitor things
    public static final String THING_TYPE_PLAYER = "player";
    public static final ThingTypeUID UID_PLAYER = new ThingTypeUID(BINDING_ID, THING_TYPE_PLAYER);

    // Collection of monitor thing types
    public static final Set<ThingTypeUID> SUPPORTED_PLAYER_THING_TYPES_UIDS = Set.of(UID_PLAYER);

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_PLAYER_THING_TYPES_UIDS.stream(), SUPPORTED_SERVER_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));
    // General purpose stuff
    public static final int DEFAULT_REFRESH_PERIOD_SEC = 5;
    // Config parameters
    // Server
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_PORT_NUMBER = "portNumber";
    public static final String CONFIG_TOKEN = "token";
    public static final String CONFIG_REFRESH_RATE = "refreshRate";
    // Player parameters
    public static final String CONFIG_PLAYER_ID = "playerID";
    public static final String CONFIG_PLAYER_NAME = "playerName";

    // List of all Channel ids
    // Server
    public static final String CHANNEL_SERVER_COUNT = "currentPlayers";
    public static final String CHANNEL_SERVER_COUNTACTIVE = "currentPlayersActive";
    // Player
    public static final String CHANNEL_PLAYER_STATE = "state";
    public static final String CHANNEL_PLAYER_TITLE = "title";
    public static final String CHANNEL_PLAYER_RATING_KEY = "ratingKey";
    public static final String CHANNEL_PLAYER_PARENT_RATING_KEY = "parentRatingKey";
    public static final String CHANNEL_PLAYER_GRANDPARENT_RATING_KEY = "grandparentRatingKey";
    public static final String CHANNEL_PLAYER_TYPE = "type";
    public static final String CHANNEL_PLAYER_POWER = "power";
    public static final String CHANNEL_PLAYER_ART = "art";
    public static final String CHANNEL_PLAYER_THUMB = "thumb";
    public static final String CHANNEL_PLAYER_PROGRESS = "progress";
    public static final String CHANNEL_PLAYER_ENDTIME = "endtime";
    public static final String CHANNEL_PLAYER_CONTROL = "player";
    public static final String CHANNEL_PLAYER_USER = "user";
}
