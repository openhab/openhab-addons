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
 * Enum PlaybackRequestType.
 */
public enum PlaybackRequestType {

    PLAY("Play"),

    SET_PLAYLIST_ITEM("SetPlaylistItem"),

    REMOVE_FROM_PLAYLIST("RemoveFromPlaylist"),

    MOVE_PLAYLIST_ITEM("MovePlaylistItem"),

    QUEUE("Queue"),

    UNPAUSE("Unpause"),

    PAUSE("Pause"),

    STOP("Stop"),

    SEEK("Seek"),

    BUFFER("Buffer"),

    READY("Ready"),

    NEXT_ITEM("NextItem"),

    PREVIOUS_ITEM("PreviousItem"),

    SET_REPEAT_MODE("SetRepeatMode"),

    SET_SHUFFLE_MODE("SetShuffleMode"),

    PING("Ping"),

    IGNORE_WAIT("IgnoreWait");

    private String value;

    PlaybackRequestType(String value) {
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
    public static PlaybackRequestType fromValue(String value) {
        for (PlaybackRequestType b : PlaybackRequestType.values()) {
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
