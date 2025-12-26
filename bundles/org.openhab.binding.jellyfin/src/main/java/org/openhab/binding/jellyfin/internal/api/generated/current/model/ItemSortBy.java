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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * These represent sort orders.
 */
public enum ItemSortBy {

    DEFAULT("Default"),

    AIRED_EPISODE_ORDER("AiredEpisodeOrder"),

    ALBUM("Album"),

    ALBUM_ARTIST("AlbumArtist"),

    ARTIST("Artist"),

    DATE_CREATED("DateCreated"),

    OFFICIAL_RATING("OfficialRating"),

    DATE_PLAYED("DatePlayed"),

    PREMIERE_DATE("PremiereDate"),

    START_DATE("StartDate"),

    SORT_NAME("SortName"),

    NAME("Name"),

    RANDOM("Random"),

    RUNTIME("Runtime"),

    COMMUNITY_RATING("CommunityRating"),

    PRODUCTION_YEAR("ProductionYear"),

    PLAY_COUNT("PlayCount"),

    CRITIC_RATING("CriticRating"),

    IS_FOLDER("IsFolder"),

    IS_UNPLAYED("IsUnplayed"),

    IS_PLAYED("IsPlayed"),

    SERIES_SORT_NAME("SeriesSortName"),

    VIDEO_BIT_RATE("VideoBitRate"),

    AIR_TIME("AirTime"),

    STUDIO("Studio"),

    IS_FAVORITE_OR_LIKED("IsFavoriteOrLiked"),

    DATE_LAST_CONTENT_ADDED("DateLastContentAdded"),

    SERIES_DATE_PLAYED("SeriesDatePlayed"),

    PARENT_INDEX_NUMBER("ParentIndexNumber"),

    INDEX_NUMBER("IndexNumber");

    private String value;

    ItemSortBy(String value) {
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
    public static ItemSortBy fromValue(String value) {
        for (ItemSortBy b : ItemSortBy.values()) {
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

        return String.format(Locale.ROOT, "%s=%s", prefix, this.toString());
    }
}
