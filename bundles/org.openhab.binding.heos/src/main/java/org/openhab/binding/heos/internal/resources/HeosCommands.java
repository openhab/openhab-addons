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
package org.openhab.binding.heos.internal.resources;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HeosCommands} provides the available commands for the HEOS network.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosCommands {

    // System Commands
    private static final String REGISTER_CHANGE_EVENT_ON = "heos://system/register_for_change_events?enable=on";
    private static final String REGISTER_CHANGE_EVENT_OFF = "heos://system/register_for_change_events?enable=off";
    private static final String HEOS_ACCOUNT_CHECK = "heos://system/check_account";
    private static final String PRETTIFY_JSON_ON = "heos://system/prettify_json_response?enable=on";
    private static final String PRETTIFY_JSON_OFF = "heos://system/prettify_json_response?enable=off";
    private static final String REBOOT_SYSTEM = "heos://system/reboot";
    private static final String SIGN_OUT = "heos://system/sign_out";
    private static final String HEARTBEAT = "heos://system/heart_beat";

    // Player Commands Control
    private static final String SET_PLAY_STATE_PLAY = "heos://player/set_play_state?pid=";
    private static final String SET_PLAY_STATE_PAUSE = "heos://player/set_play_state?pid=";
    private static final String SET_PLAY_STATE_STOP = "heos://player/set_play_state?pid=";
    private static final String SET_VOLUME = "heos://player/set_volume?pid=";
    private static final String VOLUME_UP = "heos://player/volume_up?pid=";
    private static final String VOLUME_DOWN = "heos://player/volume_down?pid=";
    private static final String SET_MUTE = "heos://player/set_mute?pid=";
    private static final String SET_MUTE_TOGGLE = "heos://player/toggle_mute?pid=";
    private static final String PLAY_NEXT = "heos://player/play_next?pid=";
    private static final String PLAY_PREVIOUS = "heos://player/play_previous?pid=";
    private static final String PLAY_QUEUE_ITEM = "heos://player/play_queue?pid=";
    private static final String CLEAR_QUEUE = "heos://player/clear_queue?pid=";
    private static final String DELETE_QUEUE_ITEM = "heos://player/remove_from_queue?pid=";
    private static final String SET_PLAY_MODE = "heos://player/set_play_mode?pid=";

    // Group Commands Control
    private static final String GET_GROUPS = "heos://group/get_groups";
    private static final String GET_GROUPS_INFO = "heos://group/get_group_info?gid=";
    private static final String SET_GROUP = "heos://group/set_group?pid=";
    private static final String GET_GROUP_VOLUME = "heos://group/get_volume?gid=";
    private static final String SET_GROUP_VOLUME = "heos://group/set_volume?gid=";
    private static final String GET_GROUP_MUTE = "heos://group/get_mute?gid=";
    private static final String SET_GROUP_MUTE = "heos://group/set_mute?gid=";
    private static final String TOGGLE_GROUP_MUTE = "heos://group/toggle_mute?gid=";
    private static final String GROUP_VOLUME_UP = "heos://group/volume_up?gid=";
    private static final String GROUP_VOLUME_DOWN = "heos://group/volume_down?gid=";

    // Player Commands get Information

    private static final String GET_PLAYERS = "heos://player/get_players";
    private static final String GET_PLAYER_INFO = "heos://player/get_player_info?pid=";
    private static final String GET_PLAY_STATE = "heos://player/get_play_state?pid=";
    private static final String GET_NOW_PLAYING_MEDIA = "heos://player/get_now_playing_media?pid=";
    private static final String PLAYER_GET_VOLUME = "heos://player/get_volume?pid=";
    private static final String PLAYER_GET_MUTE = "heos://player/get_mute?pid=";
    private static final String GET_QUEUE = "heos://player/get_queue?pid=";
    private static final String GET_PLAY_MODE = "heos://player/get_play_mode?pid=";

    // Browse Commands
    private static final String GET_MUSIC_SOURCES = "heos://browse/get_music_sources";
    private static final String BROWSE_SOURCE = "heos://browse/browse?sid=";
    private static final String PLAY_STREAM = "heos://browse/play_stream?pid=";
    private static final String ADD_TO_QUEUE = "heos://browse/add_to_queue?pid=";
    private static final String PLAY_INPUT_SOURCE = "heos://browse/play_input?pid=";
    private static final String PLAY_URL = "heos://browse/play_stream?pid=";

    public static String registerChangeEventOn() {
        return REGISTER_CHANGE_EVENT_ON;
    }

    public static String registerChangeEventOff() {
        return REGISTER_CHANGE_EVENT_OFF;
    }

    public static String heosAccountCheck() {
        return HEOS_ACCOUNT_CHECK;
    }

    public static String setPlayStatePlay(String pid) {
        return SET_PLAY_STATE_PLAY + pid + "&state=play";
    }

    public static String setPlayStatePause(String pid) {
        return SET_PLAY_STATE_PAUSE + pid + "&state=pause";
    }

    public static String setPlayStateStop(String pid) {
        return SET_PLAY_STATE_STOP + pid + "&state=stop";
    }

    public static String volumeUp(String pid) {
        return VOLUME_UP + pid + "&step=1";
    }

    public static String volumeDown(String pid) {
        return VOLUME_DOWN + pid + "&step=1";
    }

    public static String setMuteOn(String pid) {
        return SET_MUTE + pid + "&state=on";
    }

    public static String setMuteOff(String pid) {
        return SET_MUTE + pid + "&state=off";
    }

    public static String setMuteToggle(String pid) {
        return SET_MUTE_TOGGLE + pid + "&state=off";
    }

    public static String setShuffleMode(String pid, String shuffle) {
        return SET_PLAY_MODE + pid + "&shuffle=" + shuffle;
    }

    public static String setRepeatMode(String pid, String repeat) {
        return SET_PLAY_MODE + pid + "&repeat=" + repeat;
    }

    public static String getPlayMode(String pid) {
        return GET_PLAY_MODE + pid;
    }

    public static String playNext(String pid) {
        return PLAY_NEXT + pid;
    }

    public static String playPrevious(String pid) {
        return PLAY_PREVIOUS + pid;
    }

    public static String setVolume(String vol, String pid) {
        return SET_VOLUME + pid + "&level=" + vol;
    }

    public static String getPlayers() {
        return GET_PLAYERS;
    }

    public static String getPlayerInfo(String pid) {
        return GET_PLAYER_INFO + pid;
    }

    public static String getPlayState(String pid) {
        return GET_PLAY_STATE + pid;
    }

    public static String getNowPlayingMedia(String pid) {
        return GET_NOW_PLAYING_MEDIA + pid;
    }

    public static String getVolume(String pid) {
        return PLAYER_GET_VOLUME + pid;
    }

    public static String getMusicSources() {
        return GET_MUSIC_SOURCES;
    }

    public static String prettifyJSONon() {
        return PRETTIFY_JSON_ON;
    }

    public static String prettifyJSONoff() {
        return PRETTIFY_JSON_OFF;
    }

    public static String getMute(String pid) {
        return PLAYER_GET_MUTE + pid;
    }

    public static String getQueue(String pid) {
        return GET_QUEUE + pid;
    }

    public static String getQueue(String pid, int start, int end) {
        return getQueue(pid) + "&range=" + start + "," + end;
    }

    public static String playQueueItem(String pid, String qid) {
        return PLAY_QUEUE_ITEM + pid + "&qid=" + qid;
    }

    public static String deleteQueueItem(String pid, String qid) {
        return DELETE_QUEUE_ITEM + pid + "&qid=" + qid;
    }

    public static String browseSource(String sid) {
        return BROWSE_SOURCE + sid;
    }

    public static String playStream(String pid) {
        return PLAY_STREAM + pid;
    }

    public static String addToQueue(String pid) {
        return ADD_TO_QUEUE + pid;
    }

    public static String addContainerToQueuePlayNow(String pid, String sid, String cid) {
        return ADD_TO_QUEUE + pid + "&sid=" + sid + "&cid=" + cid + "&aid=1";
    }

    public static String clearQueue(String pid) {
        return CLEAR_QUEUE + pid;
    }

    public static String rebootSystem() {
        return REBOOT_SYSTEM;
    }

    public static String playStream(@Nullable String pid, @Nullable String sid, @Nullable String cid,
            @Nullable String mid, @Nullable String name) {
        String newCommand = PLAY_STREAM;
        if (pid != null) {
            newCommand = newCommand + pid;
        }
        if (sid != null) {
            newCommand = newCommand + "&sid=" + sid;
        }
        if (cid != null) {
            newCommand = newCommand + "&cid=" + cid;
        }
        if (mid != null) {
            newCommand = newCommand + "&mid=" + mid;
        }
        if (name != null) {
            newCommand = newCommand + "&name=" + name;
        }
        return newCommand;
    }

    public static String playStream(String pid, String sid, String mid) {
        return PLAY_STREAM + pid + "&sid=" + sid + "&mid=" + mid;
    }

    public static String playInputSource(String des_pid, String source_pid, String input) {
        return PLAY_INPUT_SOURCE + des_pid + "&spid=" + source_pid + "&input=inputs/" + input;
    }

    public static String playURL(String pid, String url) {
        return PLAY_URL + pid + "&url=" + url;
    }

    public static String signIn(String username, String password) {
        String encodedUsername = urlEncode(username);
        String encodedPassword = urlEncode(password);
        return "heos://system/sign_in?un=" + encodedUsername + "&pw=" + encodedPassword;
    }

    public static String signOut() {
        return SIGN_OUT;
    }

    public static String heartbeat() {
        return HEARTBEAT;
    }

    public static String getGroups() {
        return GET_GROUPS;
    }

    public static String getGroupInfo(String gid) {
        return GET_GROUPS_INFO + gid;
    }

    public static String setGroup(String[] gid) {
        String players = String.join(",", gid);

        return SET_GROUP + players;
    }

    public static String getGroupVolume(String gid) {
        return GET_GROUP_VOLUME + gid;
    }

    public static String setGroupVolume(String volume, String gid) {
        return SET_GROUP_VOLUME + gid + "&level=" + volume;
    }

    public static String setGroupVolumeUp(String gid) {
        return GROUP_VOLUME_UP + gid + "&step=1";
    }

    public static String setGroupVolumeDown(String gid) {
        return GROUP_VOLUME_DOWN + gid + "&step=1";
    }

    public static String getGroupMute(String gid) {
        return GET_GROUP_MUTE + gid;
    }

    public static String setGroupMuteOn(String gid) {
        return SET_GROUP_MUTE + gid + "&state=on";
    }

    public static String setGroupMuteOff(String gid) {
        return SET_GROUP_MUTE + gid + "&state=off";
    }

    public static String getToggleGroupMute(String gid) {
        return TOGGLE_GROUP_MUTE + gid;
    }

    private static String urlEncode(String username) {
        String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
        // however it cannot handle escaped @ signs
        return encoded.replace("%40", "@");
    }
}
