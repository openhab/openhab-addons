/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    private static final String prettifyJSONon = "heos://system/prettify_json_response?enable=on";
    private static final String prettifyJSONoff = "heos://system/prettify_json_response?enable=off";
    private static final String rebootSystem = "heos://system/reboot";
    private static final String signOut = "heos://system/sign_out";
    private static final String heartbeat = "heos://system/heart_beat";

    // Player Commands Control
    private static final String setPlayStatePlay = "heos://player/set_play_state?pid=";
    private static final String setPlayStatePause = "heos://player/set_play_state?pid=";
    private static final String setPlayStateStop = "heos://player/set_play_state?pid=";
    private static final String setVolume = "heos://player/set_volume?pid=";
    private static final String volumeUp = "heos://player/volume_up?pid=";
    private static final String volumeDown = "heos://player/volume_down?pid=";
    private static final String setMute = "heos://player/set_mute?pid=";
    private static final String setMuteToggle = "heos://player/toggle_mute?pid=";
    private static final String playNext = "heos://player/play_next?pid=";
    private static final String playPrevious = "heos://player/play_previous?pid=";
    private static final String playQueueItem = "heos://player/play_queue?pid=";
    private static final String clearQueue = "heos://player/clear_queue?pid=";
    private static final String deleteQueueItem = "heos://player/remove_from_queue?pid=";
    private static final String setPlayMode = "heos://player/set_play_mode?pid=";

    // Group Commands Control
    private static final String getGroups = "heos://group/get_groups";
    private static final String getGroupsInfo = "heos://group/get_group_info?gid=";
    private static final String setGroup = "heos://group/set_group?pid=";
    private static final String getGroupVolume = "heos://group/get_volume?gid=";
    private static final String setGroupVolume = "heos://group/set_volume?gid=";
    private static final String getGroupMute = "heos://group/get_mute?gid=";
    private static final String setGroupMute = "heos://group/set_mute?gid=";
    private static final String toggleGroupMute = "heos://group/toggle_mute?gid=";
    private static final String groupVolumeUp = "heos://group/volume_up?gid=";
    private static final String groupVolumeDown = "heos://group/volume_down?gid=";

    // Player Commands get Information

    private static final String getPlayers = "heos://player/get_players";
    private static final String getPlayerInfo = "heos://player/get_player_info?pid=";
    private static final String getPlayState = "heos://player/get_play_state?pid=";
    private static final String getNowPlayingMedia = "heos://player/get_now_playing_media?pid=";
    private static final String playerGetVolume = "heos://player/get_volume?pid=";
    private static final String playerGetMute = "heos://player/get_mute?pid=";
    private static final String getQueue = "heos://player/get_queue?pid=";
    private static final String getPlayMode = "heos://player/get_play_mode?pid=";

    // Browse Commands
    private static final String getMusicSources = "heos://browse/get_music_sources";
    private static final String browseSource = "heos://browse/browse?sid=";
    private static final String playStream = "heos://browse/play_stream?pid=";
    private static final String addToQueue = "heos://browse/add_to_queue?pid=";
    private static final String playInputSource = "heos://browse/play_input?pid=";
    private static final String playURL = "heos://browse/play_stream?pid=";

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
        return setPlayStatePlay + pid + "&state=play";
    }

    public static String setPlayStatePause(String pid) {
        return setPlayStatePause + pid + "&state=pause";
    }

    public static String setPlayStateStop(String pid) {
        return setPlayStateStop + pid + "&state=stop";
    }

    public static String volumeUp(String pid) {
        return volumeUp + pid + "&step=1";
    }

    public static String volumeDown(String pid) {
        return volumeDown + pid + "&step=1";
    }

    public static String setMuteOn(String pid) {
        return setMute + pid + "&state=on";
    }

    public static String setMuteOff(String pid) {
        return setMute + pid + "&state=off";
    }

    public static String setMuteToggle(String pid) {
        return setMuteToggle + pid + "&state=off";
    }

    public static String setShuffleMode(String pid, String shuffle) {
        return setPlayMode + pid + "&shuffle=" + shuffle;
    }

    public static String setRepeatMode(String pid, String repeat) {
        return setPlayMode + pid + "&repeat=" + repeat;
    }

    public static String getPlayMode(String pid) {
        return getPlayMode + pid;
    }

    public static String playNext(String pid) {
        return playNext + pid;
    }

    public static String playPrevious(String pid) {
        return playPrevious + pid;
    }

    public static String setVolume(String vol, String pid) {
        return setVolume + pid + "&level=" + vol;
    }

    public static String getPlayers() {
        return getPlayers;
    }

    public static String getPlayerInfo(String pid) {
        return getPlayerInfo + pid;
    }

    public static String getPlayState(String pid) {
        return getPlayState + pid;
    }

    public static String getNowPlayingMedia(String pid) {
        return getNowPlayingMedia + pid;
    }

    public static String getVolume(String pid) {
        return playerGetVolume + pid;
    }

    public static String getMusicSources() {
        return getMusicSources;
    }

    public static String prettifyJSONon() {
        return prettifyJSONon;
    }

    public static String prettifyJSONoff() {
        return prettifyJSONoff;
    }

    public static String getMute(String pid) {
        return playerGetMute + pid;
    }

    public static String getQueue(String pid) {
        return getQueue + pid;
    }

    public static String getQueue(String pid, int start, int end) {
        return getQueue(pid) + "&range=" + start + "," + end;
    }

    public static String playQueueItem(String pid, String qid) {
        return playQueueItem + pid + "&qid=" + qid;
    }

    public static String deleteQueueItem(String pid, String qid) {
        return deleteQueueItem + pid + "&qid=" + qid;
    }

    public static String browseSource(String sid) {
        return browseSource + sid;
    }

    public static String playStream(String pid) {
        return playStream + pid;
    }

    public static String addToQueue(String pid) {
        return addToQueue + pid;
    }

    public static String addContainerToQueuePlayNow(String pid, String sid, String cid) {
        return addToQueue + pid + "&sid=" + sid + "&cid=" + cid + "&aid=1";
    }

    public static String clearQueue(String pid) {
        return clearQueue + pid;
    }

    public static String rebootSystem() {
        return rebootSystem;
    }

    public static String playStream(@Nullable String pid, @Nullable String sid, @Nullable String cid,
            @Nullable String mid, @Nullable String name) {
        String newCommand = playStream;
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
        return playStream + pid + "&sid=" + sid + "&mid=" + mid;
    }

    public static String playInputSource(String des_pid, String source_pid, String input) {
        return playInputSource + des_pid + "&spid=" + source_pid + "&input=inputs/" + input;
    }

    public static String playURL(String pid, String url) {
        return playURL + pid + "&url=" + url;
    }

    public static String signIn(String username, String password) {
        String encodedUsername = urlEncode(username);
        String encodedPassword = urlEncode(password);
        return "heos://system/sign_in?un=" + encodedUsername + "&pw=" + encodedPassword;
    }

    public static String signOut() {
        return signOut;
    }

    public static String heartbeat() {
        return heartbeat;
    }

    public static String getGroups() {
        return getGroups;
    }

    public static String getGroupInfo(String gid) {
        return getGroupsInfo + gid;
    }

    public static String setGroup(String[] gid) {
        String players = String.join(",", gid);

        return setGroup + players;
    }

    public static String getGroupVolume(String gid) {
        return getGroupVolume + gid;
    }

    public static String setGroupVolume(String volume, String gid) {
        return setGroupVolume + gid + "&level=" + volume;
    }

    public static String setGroupVolumeUp(String gid) {
        return groupVolumeUp + gid + "&step=1";
    }

    public static String setGroupVolumeDown(String gid) {
        return groupVolumeDown + gid + "&step=1";
    }

    public static String getGroupMute(String gid) {
        return getGroupMute + gid;
    }

    public static String setGroupMuteOn(String gid) {
        return setGroupMute + gid + "&state=on";
    }

    public static String setGroupMuteOff(String gid) {
        return setGroupMute + gid + "&state=off";
    }

    public static String getToggleGroupMute(String gid) {
        return toggleGroupMute + gid;
    }

    private static String urlEncode(String username) {
        String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
        // however it cannot handle escaped @ signs
        return encoded.replace("%40", "@");
    }
}
