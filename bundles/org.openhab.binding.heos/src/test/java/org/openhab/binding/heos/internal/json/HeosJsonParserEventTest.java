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
package org.openhab.binding.heos.internal.json;

import static java.lang.Long.valueOf;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;

/**
 * Tests to validate the functioning of the HeosJsonParser specifically for event objects
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class HeosJsonParserEventTest {

    private final HeosJsonParser subject = new HeosJsonParser();

    @Test
    public void event_now_playing_changed() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/player_now_playing_changed\", \"message\": \"pid=1679855527\"}}");

        assertEquals(HeosEvent.PLAYER_NOW_PLAYING_CHANGED, event.command);
        assertEquals("event/player_now_playing_changed", event.rawCommand);
        assertEquals(valueOf(1679855527), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
    }

    @Test
    public void event_now_playing_progress() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/player_now_playing_progress\", \"message\": \"pid=1679855527&cur_pos=224848000&duration=0\"}}");

        assertEquals(HeosEvent.PLAYER_NOW_PLAYING_PROGRESS, event.command);
        assertEquals("event/player_now_playing_progress", event.rawCommand);
        assertEquals(valueOf(1679855527), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
        assertEquals(valueOf(224848000), event.getNumericAttribute(HeosCommunicationAttribute.CURRENT_POSITION));
        assertEquals(valueOf(0), event.getNumericAttribute(HeosCommunicationAttribute.DURATION));
    }

    @Test
    public void event_state_changed() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/player_state_changed\", \"message\": \"pid=1679855527&state=play\"}}");

        assertEquals(HeosEvent.PLAYER_STATE_CHANGED, event.command);
        assertEquals("event/player_state_changed", event.rawCommand);
        assertEquals(valueOf(1679855527), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
        assertEquals("play", event.getAttribute(HeosCommunicationAttribute.STATE));
    }

    @Test
    public void event_playback_error() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/player_playback_error\", \"message\": \"pid=1679855527&error=Could Not Download\"}}");

        assertEquals(HeosEvent.PLAYER_PLAYBACK_ERROR, event.command);
        assertEquals("event/player_playback_error", event.rawCommand);
        assertEquals(valueOf(1679855527), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
        assertEquals("Could Not Download", event.getAttribute(HeosCommunicationAttribute.ERROR));
    }

    @Test
    public void event_volume_changed() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/player_volume_changed\", \"message\": \"pid=1958912779&level=23&mute=off\"}}");

        assertEquals(HeosEvent.PLAYER_VOLUME_CHANGED, event.command);
        assertEquals("event/player_volume_changed", event.rawCommand);
        assertEquals(valueOf(1958912779), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
        assertEquals(valueOf(23), event.getNumericAttribute(HeosCommunicationAttribute.LEVEL));
        assertFalse(event.getBooleanAttribute(HeosCommunicationAttribute.MUTE));
    }

    @Test
    public void event_shuffle_mode_changed() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/shuffle_mode_changed\", \"message\": \"pid=-831584083&shuffle=on\"}}");

        assertEquals(HeosEvent.SHUFFLE_MODE_CHANGED, event.command);
        assertEquals("event/shuffle_mode_changed", event.rawCommand);
        assertEquals(valueOf(-831584083), event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
        assertTrue(event.getBooleanAttribute(HeosCommunicationAttribute.SHUFFLE));
    }

    @Test
    public void event_sources_changed() {
        HeosEventObject event = subject.parseEvent("{\"heos\": {\"command\": \"event/sources_changed\"}}");

        assertEquals(HeosEvent.SOURCES_CHANGED, event.command);
        assertEquals("event/sources_changed", event.rawCommand);
    }

    @Test
    public void event_user_changed() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/user_changed\", \"message\": \"signed_in&un=martinvw@mtin.nl\"}}");

        assertEquals(HeosEvent.USER_CHANGED, event.command);
        assertEquals("event/user_changed", event.rawCommand);
        assertTrue(event.hasAttribute(HeosCommunicationAttribute.SIGNED_IN));
        assertEquals("martinvw@mtin.nl", event.getAttribute(HeosCommunicationAttribute.USERNAME));
    }

    @Test
    public void event_unknown_event() {
        HeosEventObject event = subject.parseEvent("{\"heos\": {\"command\": \"event/does_not_exist\"}}");

        assertNull(event.command);
        assertEquals("event/does_not_exist", event.rawCommand);
    }

    @Test
    public void event_duplicate_attributes() {
        HeosEventObject event = subject.parseEvent(
                "{\"heos\": {\"command\": \"event/does_not_exist\", \"message\": \"signed_in&un=test1&un=test2\"}}");

        // the first one is ignored but it does not crash
        assertEquals("test2", event.getAttribute(HeosCommunicationAttribute.USERNAME));
    }

    @Test
    public void event_non_numeric() {
        HeosEventObject event = subject
                .parseEvent("{\"heos\": {\"command\": \"event/does_not_exist\", \"message\": \"pid=test\"}}");

        // the first one is ignored but it does not crash
        assertNull(event.getNumericAttribute(HeosCommunicationAttribute.PLAYER_ID));
    }

    @Test
    public void event_numeric_missing() {
        HeosEventObject event = subject.parseEvent("{\"heos\": {\"command\": \"event/does_not_exist\"}}");

        // the first one is ignored but it does not crash
        assertNull(event.getAttribute(HeosCommunicationAttribute.PLAYER_ID));
    }

    /*
     *
     * {"heos": {"command": "browse/browse", "result": "success", "message": "command under process&sid=1025"}}
     * {"heos": {"command": "browse/browse", "result": "success", "message": "command under process&sid=1028"}}
     * {"heos": {"command": "browse/browse", "result": "success", "message": "sid=1025&returned=6&count=6"}, "payload":
     * [{"container": "yes", "type": "playlist", "cid": "132562", "playable": "yes", "name":
     * "Maaike Ouboter - En hoe het dan ook weer dag wordt", "image_url": ""}, {"container": "yes", "type": "playlist",
     * "cid": "132563", "playable": "yes", "name": "Maaike Ouboter - Vanaf nu is het van jou", "image_url": ""},
     * {"container": "yes", "type": "playlist", "cid": "162887", "playable": "yes", "name": "Easy listening",
     * "image_url": ""}, {"container": "yes", "type": "playlist", "cid": "174461", "playable": "yes", "name":
     * "Nieuwe muziek 5-2019", "image_url": ""}, {"container": "yes", "type": "playlist", "cid": "194000", "playable":
     * "yes", "name": "Nieuwe muziek 2019-05", "image_url": ""}, {"container": "yes", "type": "playlist", "cid":
     * "194001", "playable": "yes", "name": "Clean Bandit", "image_url": ""}]}
     * {"heos": {"command": "browse/browse", "result": "success", "message": "sid=1028&returned=3&count=3"}, "payload":
     * [{"container": "no", "mid": "s6707", "type": "station", "playable": "yes", "name":
     * "NPO 3FM 96.8 (Top 40 %26 Pop Music)", "image_url":
     * "http://cdn-profiles.tunein.com/s6707/images/logoq.png?t=636268"}, {"container": "no", "mid": "s2967", "type":
     * "station", "playable": "yes", "name": "Classic FM Nederland (Classical Music)", "image_url":
     * "http://cdn-radiotime-logos.tunein.com/s2967q.png"}, {"container": "no", "mid": "s1993", "type": "station",
     * "playable": "yes", "name": "BNR Nieuwsradio", "image_url": "http://cdn-radiotime-logos.tunein.com/s1993q.png"}],
     * "options": [{"browse": [{"id": 20, "name": "Remove from HEOS Favorites"}]}]}
     * {"heos": {"command": "event/user_changed", "message": "signed_in&un=martinvw@mtin.nl"}}
     * {"heos": {"command": "group/get_groups", "result": "success", "message": ""}, "payload": []}
     * {"heos": {"command": "player/get_mute", "result": "fail", "message": "eid=2&text=ID Not Valid&pid=null"}}
     * {"heos": {"command": "player/get_mute", "result": "success", "message": "pid=1958912779&state=off"}}
     * {"heos": {"command": "player/get_mute", "result": "success", "message": "pid=1958912779&state=on"}}
     * {"heos": {"command": "player/get_mute", "result": "success", "message": "pid=-831584083&state=off"}}
     * {"heos": {"command": "player/get_mute", "result": "success", "message": "pid=-831584083&state=on"}}
     * {"heos": {"command": "player/get_now_playing_media", "result": "fail", "message":
     * "eid=2&text=ID Not Valid&pid=null"}}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=1679855527"},
     * "payload": {"type": "song", "song": "", "album": "", "artist": "", "image_url": "", "album_id": "1", "mid": "1",
     * "qid": 1, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=1958912779"},
     * "payload": {"type": "song", "song": "Solo (feat. Demi Lovato)", "album": "What Is Love? (Deluxe)", "artist":
     * "Clean Bandit", "image_url":
     * "http://192.168.1.230:8015//m-browsableMediaUri/getImageFromTag/mnt/326C72A3E307501E47DE2B0F47D90EB8/Clean%20Bandit/What%20Is%20Love_%20(Deluxe)/03%20Solo%20(feat.%20Demi%20Lovato).m4a",
     * "album_id": "", "mid":
     * "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/Clean+Bandit/What+Is+Love_+(Deluxe)/03+Solo+(feat.+Demi+Lovato).m4a",
     * "qid": 1, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=1958912779"},
     * "payload": {"type": "station", "song": "HEOS Bar - HDMI 2", "station": "HEOS Bar - HDMI 2", "album": "",
     * "artist": "", "image_url": "", "album_id": "inputs", "mid": "inputs/hdmi_in_2", "qid": 1, "sid": 1027},
     * "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=1958912779"},
     * "payload": {"type": "station", "song": "HEOS Bar - HDMI 3", "station": "HEOS Bar - HDMI 3", "album": "",
     * "artist": "", "image_url": "", "album_id": "inputs", "mid": "inputs/hdmi_in_3", "qid": 1, "sid": 1027},
     * "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=-831584083"},
     * "payload": {"type": "song", "song": "Applejack", "album":
     * "The Real... Dolly Parton: The Ultimate Dolly Parton Collection", "artist": "Dolly Parton", "image_url":
     * "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/getImageFromTag/Dolly%20Parton/The%20Real%20Dolly%20Parton%20%5bDisc%202%5d/2-07%20Applejack.m4a",
     * "album_id": "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-418", "mid":
     * "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-418/t-4150", "qid": 43, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=-831584083"},
     * "payload": {"type": "song", "song": "Dancing Queen", "album": "ABBA Gold: Greatest Hits", "artist": "ABBA",
     * "image_url":
     * "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/getImageFromTag/ABBA/ABBA%20Gold_%20Greatest%20Hits/01%20Dancing%20Queen%201.m4a",
     * "album_id": "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-398", "mid":
     * "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-398/t-4237", "qid": 1, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=-831584083"},
     * "payload": {"type": "song", "song": "D.I.V.O.R.C.E.", "album":
     * "The Real... Dolly Parton: The Ultimate Dolly Parton Collection", "artist": "Dolly Parton", "image_url":
     * "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/getImageFromTag/Dolly%20Parton/The%20Real%20Dolly%20Parton%20%5bDisc%201%5d/1-03%20D.I.V.O.R.C.E.m4a",
     * "album_id": "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-417", "mid":
     * "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-417/t-4138", "qid": 22, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=-831584083"},
     * "payload": {"type": "song", "song": "Homeward Bound", "album": "The Very Best Of Art Garfunkel: Across America",
     * "artist": "Art Garfunkel", "image_url":
     * "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/getImageFromTag/Art%20Garfunkel/The%20Very%20Best%20Of%20Art%20Garfunkel_%20Across%20A/06%20-%20Art%20Garfunkel%20-%20Homeward%20Bound.mp3",
     * "album_id": "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-127", "mid":
     * "m-1c176905-f6c7-d168-dc35-86b4735c5976/alb/a-127/t-1385", "qid": 80, "sid": 1024}, "options": []}
     * {"heos": {"command": "player/get_player_info", "result": "success", "message": "pid=1958912779"}, "payload":
     * {"name": "HEOS Bar", "pid": 1958912779, "model": "HEOS Bar", "version": "1.520.200", "ip": "192.168.1.195",
     * "network": "wired", "lineout": 0, "serial": "ADAG9180917029"}}
     * {"heos": {"command": "player/get_player_info", "result": "success", "message": "pid=-831584083"}, "payload":
     * {"name": "Kantoor HEOS 3", "pid": -831584083, "model": "HEOS 3", "version": "1.520.200", "ip": "192.168.1.230",
     * "network": "wired", "lineout": 0, "serial": "ACNG9180110887"}}
     * {"heos": {"command": "player/get_players", "result": "success", "message": ""}, "payload": [{"name": "HEOS Bar",
     * "pid": 1958912779, "model": "HEOS Bar", "version": "1.520.200", "ip": "192.168.1.195", "network": "wired",
     * "lineout": 0, "serial": "ADAG9180917029"}, {"name": "Kantoor HEOS 3", "pid": -831584083, "model": "HEOS 3",
     * "version": "1.520.200", "ip": "192.168.1.230", "network": "wired", "lineout": 0, "serial": "ACNG9180110887"}]}
     * {"heos": {"command": "player/get_players", "result": "success", "message": ""}, "payload": [{"name":
     * "Kantoor HEOS 3", "pid": -831584083, "model": "HEOS 3", "version": "1.520.200", "ip": "192.168.1.230", "network":
     * "wired", "lineout": 0, "serial": "ACNG9180110887"}, {"name": "HEOS Bar", "pid": 1958912779, "model": "HEOS Bar",
     * "version": "1.520.200", "ip": "192.168.1.195", "network": "wired", "lineout": 0, "serial": "ADAG9180917029"}]}
     * {"heos": {"command": "player/get_play_mode", "result": "fail", "message": "eid=2&text=ID Not Valid&pid=null"}}
     * {"heos": {"command": "player/get_play_mode", "result": "success", "message":
     * "pid=1958912779&repeat=off&shuffle=off"}}
     * {"heos": {"command": "player/get_play_mode", "result": "success", "message":
     * "pid=-831584083&repeat=off&shuffle=on"}}
     * {"heos": {"command": "player/get_play_state", "result": "fail", "message": "eid=2&text=ID Not Valid&pid=null"}}
     * {"heos": {"command": "player/get_play_state", "result": "success", "message": "pid=1958912779&state=stop"}}
     * {"heos": {"command": "player/get_play_state", "result": "success", "message": "pid=-831584083&state=pause"}}
     * {"heos": {"command": "player/get_play_state", "result": "success", "message": "pid=-831584083&state=play"}}
     * {"heos": {"command": "player/get_play_state", "result": "success", "message": "pid=-831584083&state=stop"}}
     * {"heos": {"command": "player/get_volume", "result": "fail", "message": "eid=2&text=ID Not Valid&pid=null"}}
     * {"heos": {"command": "player/get_volume", "result": "success", "message": "pid=1958912779&level=14"}}
     * {"heos": {"command": "player/get_volume", "result": "success", "message": "pid=1958912779&level=21"}}
     * {"heos": {"command": "player/get_volume", "result": "success", "message": "pid=1958912779&level=23"}}
     * {"heos": {"command": "player/get_volume", "result": "success", "message": "pid=-831584083&level=12"}}
     * {"heos": {"command": "player/get_volume", "result": "success", "message": "pid=-831584083&level=15"}}
     * {"heos": {"command": "player/play_next", "result": "success", "message": "pid=-831584083"}}
     * {"heos": {"command": "player/play_previous", "result": "success", "message": "pid=-831584083"}}
     * {"heos": {"command": "player/set_mute", "result": "success", "message": "pid=1958912779&state=off"}}
     * {"heos": {"command": "player/set_mute", "result": "success", "message": "pid=1958912779&state=on"}}
     * {"heos": {"command": "player/set_mute", "result": "success", "message": "pid=-831584083&state=off"}}
     * {"heos": {"command": "player/set_mute", "result": "success", "message": "pid=-831584083&state=on"}}
     * {"heos": {"command": "player/set_play_mode", "result": "success", "message": "pid=-831584083&shuffle=off"}}
     * {"heos": {"command": "player/set_play_mode", "result": "success", "message": "pid=-831584083&shuffle=on"}}
     * {"heos": {"command": "player/set_play_state", "result": "success", "message": "pid=-831584083&state=pause"}}
     * {"heos": {"command": "player/set_play_state", "result": "success", "message": "pid=-831584083&state=play"}}
     * {"heos": {"command": "player/set_volume", "result": "fail", "message":
     * "eid=9&text=Out of range&pid=-831584083&level=OFF"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=1958912779&level=14"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=1958912779&level=17"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=10"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=12"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=14"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=15"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=16"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=18"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=21"}}
     * {"heos": {"command": "player/set_volume", "result": "success", "message": "pid=-831584083&level=4"}}
     * {"heos": {"command": "player/volume_down", "result": "success", "message": "pid=-831584083&step=1"}}
     * {"heos": {"command": "system/heart_beat", "result": "success", "message": ""}}
     * {"heos": {"command": "system/register_for_change_events", "result": "success", "message": "enable=off"}}
     * {"heos": {"command": "system/register_for_change_events", "result": "success", "message": "enable=on"}}
     * {"heos": {"command": "system/register_for_change_events", "reult": "success", "message": "enable=on"}}
     * {"heos": {"command": "system/sign_in", "result": "success", "message":
     * "command under process&un=martinvw@mtin.nl&pw=Pl7WUFC61Q7zdQD5"}}
     * {"heos": {"command": "system/sign_in", "result": "success", "message": "signed_in&un=martinvw@mtin.nl"}}
     *
     */
}
