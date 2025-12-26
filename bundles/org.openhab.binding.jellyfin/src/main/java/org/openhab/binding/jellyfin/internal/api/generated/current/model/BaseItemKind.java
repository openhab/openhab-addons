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
 * The base item kind.
 */
public enum BaseItemKind {

    AGGREGATE_FOLDER("AggregateFolder"),

    AUDIO("Audio"),

    AUDIO_BOOK("AudioBook"),

    BASE_PLUGIN_FOLDER("BasePluginFolder"),

    BOOK("Book"),

    BOX_SET("BoxSet"),

    CHANNEL("Channel"),

    CHANNEL_FOLDER_ITEM("ChannelFolderItem"),

    COLLECTION_FOLDER("CollectionFolder"),

    EPISODE("Episode"),

    FOLDER("Folder"),

    GENRE("Genre"),

    MANUAL_PLAYLISTS_FOLDER("ManualPlaylistsFolder"),

    MOVIE("Movie"),

    LIVE_TV_CHANNEL("LiveTvChannel"),

    LIVE_TV_PROGRAM("LiveTvProgram"),

    MUSIC_ALBUM("MusicAlbum"),

    MUSIC_ARTIST("MusicArtist"),

    MUSIC_GENRE("MusicGenre"),

    MUSIC_VIDEO("MusicVideo"),

    PERSON("Person"),

    PHOTO("Photo"),

    PHOTO_ALBUM("PhotoAlbum"),

    PLAYLIST("Playlist"),

    PLAYLISTS_FOLDER("PlaylistsFolder"),

    PROGRAM("Program"),

    RECORDING("Recording"),

    SEASON("Season"),

    SERIES("Series"),

    STUDIO("Studio"),

    TRAILER("Trailer"),

    TV_CHANNEL("TvChannel"),

    TV_PROGRAM("TvProgram"),

    USER_ROOT_FOLDER("UserRootFolder"),

    USER_VIEW("UserView"),

    VIDEO("Video"),

    YEAR("Year");

    private String value;

    BaseItemKind(String value) {
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
    public static BaseItemKind fromValue(String value) {
        for (BaseItemKind b : BaseItemKind.values()) {
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
