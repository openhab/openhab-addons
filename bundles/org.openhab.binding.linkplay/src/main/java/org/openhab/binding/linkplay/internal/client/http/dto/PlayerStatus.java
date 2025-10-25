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
package org.openhab.binding.linkplay.internal.client.http.dto;

import org.openhab.binding.linkplay.internal.client.upnp.TransportState;

import com.google.gson.annotations.SerializedName;

/**
 * Player status.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class PlayerStatus {

    /**
     * Playback status reported by the device.
     */
    public enum PlaybackStatus {
        @SerializedName("play")
        PLAYING,
        @SerializedName("pause")
        PAUSED,
        @SerializedName("stop")
        STOPPED,
        @SerializedName("buffering")
        BUFFERING
    }

    public PlaybackStatus status;

    /** Type of stream currently playing. */
    public String type;

    /** Channel (Left/Right/Stereo). */
    @SerializedName("ch")
    public String channel;

    /** Source input mode. */
    public String mode;

    /** Repeat / shuffle loop mode. */
    public String loop;

    /** Name of EQ preset applied. */
    public String eq;

    /** Vendor string reported by firmware. */
    public String vendor;

    /** Current track position (seconds). */
    @SerializedName("curpos")
    public String currentPosition;

    /** Offset PTS (used for video lip-sync). */
    public String offsetPts;

    /** Total track length (seconds). */
    @SerializedName("totlen")
    public String totalLength;

    /** Track title (hex encoded). */
    @SerializedName("Title")
    public String title;

    /** Track artist (hex encoded). */
    @SerializedName("Artist")
    public String artist;

    /** Track album (hex encoded). */
    @SerializedName("Album")
    public String album;

    /** Volume level (0-100). */
    @SerializedName("vol")
    public String volume;

    /** Mute flag (1 / 0). */
    public String mute;

    /** Alarm clock active flag. */
    @SerializedName("alarmflag")
    public String alarmFlag;

    /** Playlist item count. */
    @SerializedName("plicount")
    public String playlistCount;

    /** Current playlist index. */
    @SerializedName("plicurr")
    public String playlistCurrent;

    // Decoding now handled by PlayerStatusAdapter during JSON deserialization.

    @Override
    public String toString() {
        return "PlayerStatus{" + "status=" + status + ", type='" + type + '\'' + ", channel='" + channel + '\''
                + ", mode='" + mode + '\'' + ", loop='" + loop + '\'' + ", eq='" + eq + '\'' + ", vendor='" + vendor
                + '\'' + ", currentPosition='" + currentPosition + '\'' + ", offsetPts='" + offsetPts + '\''
                + ", totalLength='" + totalLength + '\'' + ", title='" + title + '\'' + ", artist='" + artist + '\''
                + ", album='" + album + '\'' + ", volume='" + volume + '\'' + ", mute='" + mute + '\'' + ", alarmFlag='"
                + alarmFlag + '\'' + ", playlistCount='" + playlistCount + '\'' + ", playlistCurrent='"
                + playlistCurrent + '\'' + '}';
    }

    public static PlaybackStatus fromTransportState(TransportState transportState) {
        return switch (transportState) {
            case PLAYING -> PlaybackStatus.PLAYING;
            case PAUSED_PLAYBACK -> PlaybackStatus.PAUSED;
            case STOPPED -> PlaybackStatus.STOPPED;
            case TRANSITIONING -> PlaybackStatus.BUFFERING;
            default -> PlaybackStatus.STOPPED;
        };
    }
}
