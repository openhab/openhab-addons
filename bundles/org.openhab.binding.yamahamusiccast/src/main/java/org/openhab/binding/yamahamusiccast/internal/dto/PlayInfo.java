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
package org.openhab.binding.yamahamusiccast.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the PlayInfo request requested from the Yamaha model/device via the API.
 *
 * @author Lennert Coopman - Initial contribution
 */

public class PlayInfo {

    @SerializedName("response_code")
    private String responseCode;

    @SerializedName("playback")
    private String playback;

    @SerializedName("artist")
    private String artist;

    @SerializedName("track")
    private String track;

    @SerializedName("album")
    private String album;

    @SerializedName("albumart_url")
    private String albumarturl;

    @SerializedName("repeat")
    private String repeat;

    @SerializedName("shuffle")
    private String shuffle;

    @SerializedName("play_time")
    private int playTime = 0;

    @SerializedName("total_time")
    private int totalTime = 0;

    public String getResponseCode() {
        if (responseCode == null) {
            responseCode = "";
        }
        return responseCode;
    }

    public String getPlayback() {
        if (playback == null) {
            playback = "";
        }
        return playback;
    }

    public String getArtist() {
        if (artist == null) {
            artist = "";
        }
        return artist;
    }

    public String getTrack() {
        if (track == null) {
            track = "";
        }
        return track;
    }

    public String getAlbum() {
        if (album == null) {
            album = "";
        }
        return album;
    }

    public String getAlbumArtUrl() {
        if (albumarturl == null) {
            albumarturl = "";
        }
        return albumarturl;
    }

    public String getRepeat() {
        if (repeat == null) {
            repeat = "";
        }
        return repeat;
    }

    public String getShuffle() {
        if (shuffle == null) {
            shuffle = "";
        }
        return shuffle;
    }

    public int getPlayTime() {
        return playTime;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
