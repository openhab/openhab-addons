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
package org.openhab.binding.jellyfin.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link JellyfinBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class JellyfinBindingConstants {

    static final String BINDING_ID = "jellyfin";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "client");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SERVER, THING_TYPE_CLIENT);

    // List of all Channel ids
    public static final String SEND_NOTIFICATION_CHANNEL = "send-notification";
    public static final String MEDIA_CONTROL_CHANNEL = "media-control";
    public static final String PLAYING_ITEM_PERCENTAGE_CHANNEL = "playing-item-percentage";
    public static final String PLAYING_ITEM_ID_CHANNEL = "playing-item-id";
    public static final String PLAYING_ITEM_NAME_CHANNEL = "playing-item-name";
    public static final String PLAYING_ITEM_SERIES_NAME_CHANNEL = "playing-item-series-name";
    public static final String PLAYING_ITEM_SEASON_NAME_CHANNEL = "playing-item-season-name";
    public static final String PLAYING_ITEM_SEASON_CHANNEL = "playing-item-season";
    public static final String PLAYING_ITEM_EPISODE_CHANNEL = "playing-item-episode";
    public static final String PLAYING_ITEM_GENRES_CHANNEL = "playing-item-genders";
    public static final String PLAYING_ITEM_TYPE_CHANNEL = "playing-item-type";
    public static final String PLAYING_ITEM_SECOND_CHANNEL = "playing-item-second";
    public static final String PLAYING_ITEM_TOTAL_SECOND_CHANNEL = "playing-item-total-seconds";
    public static final String PLAY_BY_TERMS_CHANNEL = "play-by-terms";
    public static final String PLAY_NEXT_BY_TERMS_CHANNEL = "play-next-by-terms";
    public static final String PLAY_LAST_BY_TERMS_CHANNEL = "play-last-by-terms";
    public static final String BROWSE_ITEM_BY_TERMS_CHANNEL = "browse-by-terms";
    public static final String PLAY_BY_ID_CHANNEL = "play-by-id";
    public static final String PLAY_NEXT_BY_ID_CHANNEL = "play-next-by-id";
    public static final String PLAY_LAST_BY_ID_CHANNEL = "play-last-by-id";
    public static final String BROWSE_ITEM_BY_ID_CHANNEL = "browse-by-id";
    // Discovery
    public static final int DISCOVERY_RESULT_TTL_SEC = 600;
}
