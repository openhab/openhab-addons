/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
