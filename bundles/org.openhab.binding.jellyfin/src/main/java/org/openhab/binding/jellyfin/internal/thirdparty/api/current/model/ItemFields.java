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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Used to control the data that gets attached to DtoBaseItems.
 */
public enum ItemFields {

    AIR_TIME("AirTime"),

    CAN_DELETE("CanDelete"),

    CAN_DOWNLOAD("CanDownload"),

    CHANNEL_INFO("ChannelInfo"),

    CHAPTERS("Chapters"),

    TRICKPLAY("Trickplay"),

    CHILD_COUNT("ChildCount"),

    CUMULATIVE_RUN_TIME_TICKS("CumulativeRunTimeTicks"),

    CUSTOM_RATING("CustomRating"),

    DATE_CREATED("DateCreated"),

    DATE_LAST_MEDIA_ADDED("DateLastMediaAdded"),

    DISPLAY_PREFERENCES_ID("DisplayPreferencesId"),

    ETAG("Etag"),

    EXTERNAL_URLS("ExternalUrls"),

    GENRES("Genres"),

    ITEM_COUNTS("ItemCounts"),

    MEDIA_SOURCE_COUNT("MediaSourceCount"),

    MEDIA_SOURCES("MediaSources"),

    ORIGINAL_TITLE("OriginalTitle"),

    OVERVIEW("Overview"),

    PARENT_ID("ParentId"),

    PATH("Path"),

    PEOPLE("People"),

    PLAY_ACCESS("PlayAccess"),

    PRODUCTION_LOCATIONS("ProductionLocations"),

    PROVIDER_IDS("ProviderIds"),

    PRIMARY_IMAGE_ASPECT_RATIO("PrimaryImageAspectRatio"),

    RECURSIVE_ITEM_COUNT("RecursiveItemCount"),

    SETTINGS("Settings"),

    SERIES_STUDIO("SeriesStudio"),

    SORT_NAME("SortName"),

    SPECIAL_EPISODE_NUMBERS("SpecialEpisodeNumbers"),

    STUDIOS("Studios"),

    TAGLINES("Taglines"),

    TAGS("Tags"),

    REMOTE_TRAILERS("RemoteTrailers"),

    MEDIA_STREAMS("MediaStreams"),

    SEASON_USER_DATA("SeasonUserData"),

    DATE_LAST_REFRESHED("DateLastRefreshed"),

    DATE_LAST_SAVED("DateLastSaved"),

    REFRESH_STATE("RefreshState"),

    CHANNEL_IMAGE("ChannelImage"),

    ENABLE_MEDIA_SOURCE_DISPLAY("EnableMediaSourceDisplay"),

    WIDTH("Width"),

    HEIGHT("Height"),

    EXTRA_IDS("ExtraIds"),

    LOCAL_TRAILER_COUNT("LocalTrailerCount"),

    IS_HD("IsHD"),

    SPECIAL_FEATURE_COUNT("SpecialFeatureCount");

    private String value;

    ItemFields(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ItemFields fromValue(String value) {
        for (ItemFields b : ItemFields.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        return String.format(java.util.Locale.ROOT, "%s=%s", prefix, this.toString());
    }
}
