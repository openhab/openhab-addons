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

    public static final String BINDING_ID = "jellyfin";
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

    /**
     * Thing property key used to store the Jellyfin device name (user-visible device label set in the Jellyfin app).
     * Stored as a Thing property (not a configuration parameter) so it is preserved across device ID regenerations
     * and can be used to re-identify the same physical device when its Jellyfin device ID changes.
     */
    public static final String PROPERTY_DEVICE_NAME = "deviceName";

    /**
     * Lowercase client name substrings used for discovery category filtering.
     * Android TV must be checked before Android to prevent false positive matches.
     */
    public static final String CLIENT_FILTER_WEB = "jellyfin web";
    public static final String CLIENT_FILTER_ANDROID_TV = "android tv";
    public static final String CLIENT_FILTER_ANDROID = "android";
    public static final String CLIENT_FILTER_IOS = "ios";
    public static final String CLIENT_FILTER_SWIFTFIN = "swiftfin";
    public static final String CLIENT_FILTER_INFUSE = "infuse";
    public static final String CLIENT_FILTER_KODI = "kodi";
    public static final String CLIENT_FILTER_JELLYCON = "jellycon";
    public static final String CLIENT_FILTER_ROKU = "roku";

    // Image download channels (dynamic — created when enabled in config)
    public static final String IMAGE_CHANNEL_TYPE_ID = "playing-item-image-channel";
    public static final String IMAGE_PRIMARY_CHANNEL = "playing-item-image-primary";
    public static final String IMAGE_BACKDROP_CHANNEL = "playing-item-image-backdrop";
    public static final String IMAGE_LOGO_CHANNEL = "playing-item-image-logo";
    public static final String IMAGE_THUMB_CHANNEL = "playing-item-image-thumb";
    public static final String IMAGE_DISC_CHANNEL = "playing-item-image-disc";
    public static final String IMAGE_ART_CHANNEL = "playing-item-image-art";
    public static final String IMAGE_BANNER_CHANNEL = "playing-item-image-banner";

    // Image channel config keys
    public static final String CONFIG_IMAGE_PRIMARY_ENABLED = "imagePrimaryEnabled";
    public static final String CONFIG_IMAGE_PRIMARY_WIDTH = "imagePrimaryWidth";
    public static final String CONFIG_IMAGE_BACKDROP_ENABLED = "imageBackdropEnabled";
    public static final String CONFIG_IMAGE_BACKDROP_WIDTH = "imageBackdropWidth";
    public static final String CONFIG_IMAGE_LOGO_ENABLED = "imageLogoEnabled";
    public static final String CONFIG_IMAGE_LOGO_WIDTH = "imageLogoWidth";
    public static final String CONFIG_IMAGE_THUMB_ENABLED = "imageThumbEnabled";
    public static final String CONFIG_IMAGE_THUMB_WIDTH = "imageThumbWidth";
    public static final String CONFIG_IMAGE_DISC_ENABLED = "imageDiscEnabled";
    public static final String CONFIG_IMAGE_DISC_WIDTH = "imageDiscWidth";
    public static final String CONFIG_IMAGE_ART_ENABLED = "imageArtEnabled";
    public static final String CONFIG_IMAGE_ART_WIDTH = "imageArtWidth";
    public static final String CONFIG_IMAGE_BANNER_ENABLED = "imageBannerEnabled";
    public static final String CONFIG_IMAGE_BANNER_WIDTH = "imageBannerWidth";
}
