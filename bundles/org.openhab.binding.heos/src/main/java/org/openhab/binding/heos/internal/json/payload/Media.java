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
package org.openhab.binding.heos.internal.json.payload;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for response payloads from now_playing commands
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class Media {
    public static final int SOURCE_PANDORA = 1;
    public static final int SOURCE_RHAPSODY = 2;
    public static final int SOURCE_TUNE_IN = 3;
    public static final int SOURCE_SPOTIFY = 4;
    public static final int SOURCE_DEEZER = 5;
    public static final int SOURCE_NAPSTER = 6;
    public static final int SOURCE_I_HEART_RADIO = 7;
    public static final int SOURCE_SIRIUS_XM = 8;
    public static final int SOURCE_SOUNDCLOUD = 9;
    public static final int SOURCE_TIDAL = 10;
    // public static final int SOURCE_FUTURE_SERVICE = 11;
    public static final int SOURCE_RDIO = 12;
    public static final int SOURCE_AMAZON_MUSIC = 13;
    // public static final int SOURCE_FUTURE_SERVICE = 14;
    public static final int SOURCE_MOODMIX = 15;
    public static final int SOURCE_JUKE = 16;
    // public static final int SOURCE_FUTURE_SERVICE = 17;
    public static final int SOURCE_Q_Q_MUSIC = 18;

    public static final int SOURCE_LOCAL = 1024;
    public static final int SOURCE_PLAYLIST = 1025;
    public static final int SOURCE_HISTORY = 1026;
    public static final int SOURCE_AUX = 1027;
    public static final int SOURCE_FAVORITES = 1028;

    public @Nullable String type;
    public @Nullable String song;
    public @Nullable String station;
    public @Nullable String album;
    public @Nullable String artist;
    public @Nullable String imageUrl;
    public @Nullable String albumId;
    @SerializedName("mid")
    public @Nullable String mediaId;
    @SerializedName("qid")
    public int queueId;
    @SerializedName("sid")
    public int sourceId;

    public String combinedSongArtist() {
        return String.format("%s - %s", artist, song);
    }

    @Override
    public String toString() {
        return "Media{" + "type='" + type + '\'' + ", song='" + song + '\'' + ", station='" + station + '\''
                + ", album='" + album + '\'' + ", artist='" + artist + '\'' + ", imageUrl='" + imageUrl + '\''
                + ", albumId='" + albumId + '\'' + ", mediaId='" + mediaId + '\'' + ", queueId=" + queueId
                + ", sourceId=" + sourceId + '}';
    }
}
