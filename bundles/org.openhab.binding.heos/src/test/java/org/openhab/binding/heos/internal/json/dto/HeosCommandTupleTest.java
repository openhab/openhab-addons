/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.heos.internal.json.dto.HeosCommand.*;
import static org.openhab.binding.heos.internal.json.dto.HeosCommandGroup.SYSTEM;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests to validate the functioning of the HeosCommandTuple
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class HeosCommandTupleTest {
    @Test
    public void system() {
        assertMatches("system/check_account", SYSTEM, CHECK_ACCOUNT);
        assertMatches("system/sign_in", SYSTEM, SIGN_IN);
        assertMatches("system/sign_out", SYSTEM, SIGN_OUT);
        assertMatches("system/register_for_change_events", SYSTEM, REGISTER_FOR_CHANGE_EVENTS);
        assertMatches("system/heart_beat", SYSTEM, HEART_BEAT);
        assertMatches("system/prettify_json_response", SYSTEM, PRETTIFY_JSON_RESPONSE);
    }

    @Test
    public void browse() {
        assertMatches("browse/browse", HeosCommandGroup.BROWSE, HeosCommand.BROWSE);
        assertMatches("browse/get_music_sources", HeosCommandGroup.BROWSE, HeosCommand.GET_MUSIC_SOURCES);
        assertMatches("browse/get_source_info", HeosCommandGroup.BROWSE, HeosCommand.GET_SOURCE_INFO);
        assertMatches("browse/get_search_criteria", HeosCommandGroup.BROWSE, HeosCommand.GET_SEARCH_CRITERIA);
        assertMatches("browse/search", HeosCommandGroup.BROWSE, HeosCommand.SEARCH);
        assertMatches("browse/play_stream", HeosCommandGroup.BROWSE, HeosCommand.PLAY_STREAM);
        assertMatches("browse/play_preset", HeosCommandGroup.BROWSE, HeosCommand.PLAY_PRESET);
        assertMatches("browse/play_input", HeosCommandGroup.BROWSE, HeosCommand.PLAY_INPUT);
        assertMatches("browse/play_stream", HeosCommandGroup.BROWSE, HeosCommand.PLAY_STREAM);
        assertMatches("browse/add_to_queue", HeosCommandGroup.BROWSE, HeosCommand.ADD_TO_QUEUE);
        assertMatches("browse/rename_playlist", HeosCommandGroup.BROWSE, HeosCommand.RENAME_PLAYLIST);
        assertMatches("browse/delete_playlist", HeosCommandGroup.BROWSE, HeosCommand.DELETE_PLAYLIST);
        assertMatches("browse/retrieve_metadata", HeosCommandGroup.BROWSE, HeosCommand.RETRIEVE_METADATA);
    }

    @Test
    public void player() {
        assertMatches("player/get_players", HeosCommandGroup.PLAYER, HeosCommand.GET_PLAYERS);
        assertMatches("player/get_player_info", HeosCommandGroup.PLAYER, HeosCommand.GET_PLAYER_INFO);
        assertMatches("player/get_play_state", HeosCommandGroup.PLAYER, HeosCommand.GET_PLAY_STATE);
        assertMatches("player/set_play_state", HeosCommandGroup.PLAYER, HeosCommand.SET_PLAY_STATE);
        assertMatches("player/get_now_playing_media", HeosCommandGroup.PLAYER, HeosCommand.GET_NOW_PLAYING_MEDIA);
        assertMatches("player/get_volume", HeosCommandGroup.PLAYER, HeosCommand.GET_VOLUME);
        assertMatches("player/set_volume", HeosCommandGroup.PLAYER, HeosCommand.SET_VOLUME);
        assertMatches("player/volume_up", HeosCommandGroup.PLAYER, HeosCommand.VOLUME_UP);
        assertMatches("player/volume_down", HeosCommandGroup.PLAYER, HeosCommand.VOLUME_DOWN);
        assertMatches("player/get_mute", HeosCommandGroup.PLAYER, HeosCommand.GET_MUTE);
        assertMatches("player/set_mute", HeosCommandGroup.PLAYER, HeosCommand.SET_MUTE);
        assertMatches("player/toggle_mute", HeosCommandGroup.PLAYER, HeosCommand.TOGGLE_MUTE);
        assertMatches("player/get_play_mode", HeosCommandGroup.PLAYER, HeosCommand.GET_PLAY_MODE);
        assertMatches("player/set_play_mode", HeosCommandGroup.PLAYER, HeosCommand.SET_PLAY_MODE);
        assertMatches("player/get_queue", HeosCommandGroup.PLAYER, HeosCommand.GET_QUEUE);
        assertMatches("player/play_queue", HeosCommandGroup.PLAYER, HeosCommand.PLAY_QUEUE);
        assertMatches("player/remove_from_queue", HeosCommandGroup.PLAYER, HeosCommand.REMOVE_FROM_QUEUE);
        assertMatches("player/save_queue", HeosCommandGroup.PLAYER, HeosCommand.SAVE_QUEUE);
        assertMatches("player/clear_queue", HeosCommandGroup.PLAYER, HeosCommand.CLEAR_QUEUE);
        assertMatches("player/move_queue_item", HeosCommandGroup.PLAYER, HeosCommand.MOVE_QUEUE_ITEM);
        assertMatches("player/play_next", HeosCommandGroup.PLAYER, HeosCommand.PLAY_NEXT);
        assertMatches("player/play_previous", HeosCommandGroup.PLAYER, HeosCommand.PLAY_PREVIOUS);
        assertMatches("player/set_quickselect", HeosCommandGroup.PLAYER, HeosCommand.SET_QUICKSELECT);
        assertMatches("player/play_quickselect", HeosCommandGroup.PLAYER, HeosCommand.PLAY_QUICKSELECT);
        assertMatches("player/get_quickselects", HeosCommandGroup.PLAYER, HeosCommand.GET_QUICKSELECTS);
        assertMatches("player/check_update", HeosCommandGroup.PLAYER, HeosCommand.CHECK_UPDATE);
    }

    @Test
    public void group() {
        assertMatches("group/get_groups", HeosCommandGroup.GROUP, HeosCommand.GET_GROUPS);
        assertMatches("group/get_group_info", HeosCommandGroup.GROUP, HeosCommand.GET_GROUP_INFO);
        assertMatches("group/set_group", HeosCommandGroup.GROUP, HeosCommand.SET_GROUP);
        assertMatches("group/get_volume", HeosCommandGroup.GROUP, HeosCommand.GET_VOLUME);
        assertMatches("group/set_volume", HeosCommandGroup.GROUP, HeosCommand.SET_VOLUME);
        assertMatches("group/volume_up", HeosCommandGroup.GROUP, HeosCommand.VOLUME_UP);
        assertMatches("group/volume_down", HeosCommandGroup.GROUP, HeosCommand.VOLUME_DOWN);
        assertMatches("group/get_mute", HeosCommandGroup.GROUP, HeosCommand.GET_MUTE);
        assertMatches("group/set_mute", HeosCommandGroup.GROUP, HeosCommand.SET_MUTE);
        assertMatches("group/toggle_mute", HeosCommandGroup.GROUP, HeosCommand.TOGGLE_MUTE);
    }

    private void assertMatches(String command, HeosCommandGroup commandGroup, HeosCommand heosCommand) {
        HeosCommandTuple tuple = HeosCommandTuple.valueOf(command);

        assertNotNull(tuple);
        assertEquals(commandGroup, tuple.commandGroup);
        assertEquals(heosCommand, tuple.command);
    }

    /**
     * "browse/browse"
     * "group/get_groups"
     * "player/get_mute"
     * "player/get_now_playing_media"
     * "player/get_player_info"
     * "player/get_players"
     * "player/get_play_mode"
     * "player/get_play_state"
     * "player/get_volume"
     * "player/play_next"
     * "player/play_previous"
     * "player/set_mute"
     * "player/set_play_mode"
     * "player/set_play_state"
     * "player/set_volume"
     * "player/volume_down"
     * "system/heart_beat"
     * "system/register_for_change_events"
     * "system/sign_in"
     */
}
