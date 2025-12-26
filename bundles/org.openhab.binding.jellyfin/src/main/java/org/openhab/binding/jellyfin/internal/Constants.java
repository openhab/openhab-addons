/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * The Constants class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK and respective runtime
 */
@NonNullByDefault
public class Constants {

    static final String BINDING_ID = "jellyfin";
    static final String BINDING_PID = "binding.jellyfin";

    public static class ServerProperties {
        public static final String API_VERSION = "apiVersion";
        public static final String SERVER_URI = "uri";
        public static final String SERVER_VERSION = "Server Version";
    }

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_JELLYFIN_CLIENT = new ThingTypeUID(BINDING_ID, "client");
    // Only server handler is implemented - client discovery exists but no client handler yet
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SERVER, THING_TYPE_JELLYFIN_CLIENT);
    public static final Set<ThingTypeUID> DISCOVERABLE_CLIENT_THING_TYPES = Set.of(THING_TYPE_JELLYFIN_CLIENT);

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
    public static final String PLAYING_ITEM_GENRES_CHANNEL = "playing-item-genres";
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
    // Stop channel (Switch type) - complements system.media-control Player channel
    public static final String MEDIA_STOP_CHANNEL = "media-stop";
    // Phase 11: Extended media controls (shuffle, repeat, quality, audio, subtitle)
    public static final String MEDIA_SHUFFLE_CHANNEL = "media-shuffle";
    public static final String MEDIA_REPEAT_CHANNEL = "media-repeat";
    public static final String MEDIA_QUALITY_CHANNEL = "media-quality";
    public static final String MEDIA_AUDIO_TRACK_CHANNEL = "media-audio-track";
    public static final String MEDIA_SUBTITLE_CHANNEL = "media-subtitle";
    // Discovery
    public static final int DISCOVERY_RESULT_TTL_SEC = 600;
}
