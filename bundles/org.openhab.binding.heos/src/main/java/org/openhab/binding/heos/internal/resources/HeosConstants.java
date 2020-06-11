/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeosConstants} provides the constants used within the HEOS
 * network
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosConstants {

    public static final String HEOS = "heos";
    public static final String COMMAND = "command";
    public static final String RESULT = "result";
    public static final String MESSAGE = "message";
    public static final String TEXT = "text";

    public static final String CONNECTION_LOST = "connection_lost";
    public static final String CONNECTION_RESTORED = "connection_restored";

    public static final String PID = "pid";
    public static final String GID = "gid";
    public static final String EID = "eid";
    public static final String IP = "ip";
    public static final String MODEL = "model";
    public static final String VERSION = "version";
    public static final String NETWORK = "network";
    public static final String LINE_OUT = "lineout";
    public static final String CONTROL = "control";
    public static final String SERIAL = "serial";

    public static final String BROWSE = "browse";
    public static final String GET_MUSIC_SOURCES = "get_music_sources";

    // Event Results
    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String ERROR = "Error";
    public static final String COM_UNDER_PROCESS = "command under process";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String REPEAT_ALL = "on_all";
    public static final String REPEAT_ONE = "on_one";

    // Event Types
    public static final String EVENTTYPE_SYSTEM = "system";
    public static final String EVENTTYPE_EVENT = "event";
    public static final String EVENTTYPE_THING = "thing";
    public static final String EVENTTYPE_GROUP = "group";
    public static final String EVENTTYPE_BROWSE = "browse";
    public static final String EVENTTYPE_PLAYER = "player";

    // Event Commands
    public static final String SING_IN = "sign_in";
    public static final String USER_CHANGED = "user_changed";
    public static final String GROUPS_CHANGED = "groups_changed";
    public static final String PLAYER_VOLUME_CHANGED = "player_volume_changed";
    public static final String PLAYER_QUEUE_CHANGED = "player_queue_changed";
    public static final String PLAYER_STATE_CHANGED = "player_state_changed";
    public static final String PLAYER_NOW_PLAYING_CHANGED = "player_now_playing_changed";
    public static final String PLAYERS_CHANGED = "players_changed";
    public static final String PLAYER_NOW_PLAYING_PROGRESS = "player_now_playing_progress";
    public static final String SOURCES_CHANGED = "sources_changed";
    public static final String SHUFFLE_MODE_CHANGED = "shuffle_mode_changed";
    public static final String REPEAT_MODE_CHANGED = "repeat_mode_changed";

    // Browse Command
    public static final String FAVORIT_SID = "1028";
    public static final String PLAYLISTS_SID = "1025";
    public static final String INPUT_SID = "1027";
    public static final String CID = "cid";
    public static final String MID = "mid";
    public static final String SID = "sid";
    public static final String QID = "qid";

    // State commands
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String LEVEL = "level";
    public static final String STATE = "state";
    public static final String CUR_POS = "cur_pos";
    public static final String DURATION = "duration";
    public static final String REPEAT_MODE = "repeat";
    public static final String SHUFFLE = "shuffle";

    // Several command

    public static final String ROLE = "role";
    public static final String LEADER = "leader";
    public static final String NAME = "name";

    // Player commands
    public static final String GET_NOW_PLAYING_MEDIA = "get_now_playing_media";
    public static final String GET_PLAYER_INFO = "get_player_info";
    public static final String GET_PLAY_STATE = "get_play_state";
    public static final String GET_VOLUME = "get_volume";
    public static final String GET_MUTE = "get_mute";
    public static final String GET_PLAY_MODE = "get_play_mode";
    public static final String GET_QUEUE = "get_queue";
    public static final String SET_PLAY_STATE = "set_play_state";
    public static final String SET_VOLUME = "set_volume";
    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    public static final String SONG = "song";
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";
    public static final String STATION = "station";
    public static final String IMAGE_URL = "image_url";
    public static final String RAW_COMMAND = "rawCommand";
    public static final String PLAY_URL = "play_stream";
    public static final String ALBUM_ID = "album_id";
    public static final String TYPE = "type";

    // UI Commands

    public static final String HEOS_UI_ALL = "All";
    public static final String HEOS_UI_ONE = "One";
    public static final String HEOS_UI_OFF = "Off";

}
