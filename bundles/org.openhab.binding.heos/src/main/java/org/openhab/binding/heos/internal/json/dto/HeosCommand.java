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
package org.openhab.binding.heos.internal.json.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum for the HEOS commands
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public enum HeosCommand {
    BROWSE,
    CHECK_ACCOUNT,
    CHECK_UPDATE,
    CLEAR_QUEUE,
    DELETE_PLAYLIST,
    GET_GROUPS,
    GET_GROUP_INFO,
    GET_MUSIC_SOURCES,
    GET_NOW_PLAYING_MEDIA,
    GET_PLAYERS,
    GET_PLAYER_INFO,
    GET_PLAY_MODE,
    GET_PLAY_STATE,
    GET_SEARCH_CRITERIA,
    GET_SOURCE_INFO,
    GET_VOLUME,
    HEART_BEAT,
    PLAY_INPUT,
    PLAY_NEXT,
    PLAY_PRESET,
    PLAY_PREVIOUS,

    ADD_TO_QUEUE,
    GET_QUEUE,
    MOVE_QUEUE_ITEM,
    PLAY_QUEUE,
    SAVE_QUEUE,

    GET_QUICKSELECTS,
    PLAY_QUICKSELECT,
    SET_QUICKSELECT,

    PLAY_STREAM,
    PRETTIFY_JSON_RESPONSE,
    REGISTER_FOR_CHANGE_EVENTS,
    REMOVE_FROM_QUEUE,
    RENAME_PLAYLIST,
    RETRIEVE_METADATA,
    SEARCH,
    SET_GROUP,

    SET_MUTE,
    GET_MUTE,
    TOGGLE_MUTE,

    SET_PLAY_MODE,
    SET_PLAY_STATE,
    SIGN_IN,
    SIGN_OUT,

    SET_VOLUME,
    VOLUME_DOWN,
    VOLUME_UP;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
