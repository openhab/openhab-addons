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
 * This exists simply to identify a set of known commands.
 */
public enum GeneralCommandType {

    MOVE_UP("MoveUp"),

    MOVE_DOWN("MoveDown"),

    MOVE_LEFT("MoveLeft"),

    MOVE_RIGHT("MoveRight"),

    PAGE_UP("PageUp"),

    PAGE_DOWN("PageDown"),

    PREVIOUS_LETTER("PreviousLetter"),

    NEXT_LETTER("NextLetter"),

    TOGGLE_OSD("ToggleOsd"),

    TOGGLE_CONTEXT_MENU("ToggleContextMenu"),

    SELECT("Select"),

    BACK("Back"),

    TAKE_SCREENSHOT("TakeScreenshot"),

    SEND_KEY("SendKey"),

    SEND_STRING("SendString"),

    GO_HOME("GoHome"),

    GO_TO_SETTINGS("GoToSettings"),

    VOLUME_UP("VolumeUp"),

    VOLUME_DOWN("VolumeDown"),

    MUTE("Mute"),

    UNMUTE("Unmute"),

    TOGGLE_MUTE("ToggleMute"),

    SET_VOLUME("SetVolume"),

    SET_AUDIO_STREAM_INDEX("SetAudioStreamIndex"),

    SET_SUBTITLE_STREAM_INDEX("SetSubtitleStreamIndex"),

    TOGGLE_FULLSCREEN("ToggleFullscreen"),

    DISPLAY_CONTENT("DisplayContent"),

    GO_TO_SEARCH("GoToSearch"),

    DISPLAY_MESSAGE("DisplayMessage"),

    SET_REPEAT_MODE("SetRepeatMode"),

    CHANNEL_UP("ChannelUp"),

    CHANNEL_DOWN("ChannelDown"),

    GUIDE("Guide"),

    TOGGLE_STATS("ToggleStats"),

    PLAY_MEDIA_SOURCE("PlayMediaSource"),

    PLAY_TRAILERS("PlayTrailers"),

    SET_SHUFFLE_QUEUE("SetShuffleQueue"),

    PLAY_STATE("PlayState"),

    PLAY_NEXT("PlayNext"),

    TOGGLE_OSD_MENU("ToggleOsdMenu"),

    PLAY("Play"),

    SET_MAX_STREAMING_BITRATE("SetMaxStreamingBitrate"),

    SET_PLAYBACK_ORDER("SetPlaybackOrder");

    private String value;

    GeneralCommandType(String value) {
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
    public static GeneralCommandType fromValue(String value) {
        for (GeneralCommandType b : GeneralCommandType.values()) {
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
