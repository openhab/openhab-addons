/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.heos.internal.json.dto.HeosCommand;
import org.openhab.binding.heos.internal.json.dto.HeosCommandGroup;
import org.openhab.binding.heos.internal.json.dto.HeosCommandTuple;
import org.openhab.binding.heos.internal.json.dto.HeosError;
import org.openhab.binding.heos.internal.json.dto.HeosErrorCode;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.BrowseResult;
import org.openhab.binding.heos.internal.json.payload.BrowseResultType;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.GroupPlayerRole;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.json.payload.YesNoEnum;

/**
 * Tests to validate the functioning of the HeosJsonParser specifically for response objects
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class HeosJsonParserResponseTest {

    private final HeosJsonParser subject = new HeosJsonParser();

    @Test
    public void signIn() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"system/sign_in\", \"result\": \"success\", \"message\": \"signed_in&un=test@example.org\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.SYSTEM, heosCommand.commandGroup);
        assertEquals(HeosCommand.SIGN_IN, heosCommand.command);
        assertTrue(response.result);

        assertEquals("test@example.org", response.getAttribute(USERNAME));
        assertTrue(response.hasAttribute(SIGNED_IN));
    }

    @Test
    public void signInUnderProcess() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"system/sign_in\", \"message\": \"command under process\"}}", Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.SYSTEM, heosCommand.commandGroup);
        assertEquals(HeosCommand.SIGN_IN, heosCommand.command);
        assertFalse(response.result);
        assertFalse(response.isFinished());
    }

    @Test
    public void signInFailed() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"system/sign_in\", \"message\": \"eid=10&text=User not found\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.SYSTEM, heosCommand.commandGroup);
        assertEquals(HeosCommand.SIGN_IN, heosCommand.command);
        assertFalse(response.result);
        assertTrue(response.isFinished());

        HeosError error = response.getError();
        assertNotNull(error);
        assertEquals(HeosErrorCode.USER_NOT_FOUND, error.code);
    }

    @Test
    public void getMute() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"player/get_mute\", \"result\": \"success\", \"message\": \"pid=1958912779&state=on\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_MUTE, heosCommand.command);
        assertTrue(response.result);

        assertEquals(Long.valueOf(1958912779), response.getNumericAttribute(PLAYER_ID));
        assertTrue(response.getBooleanAttribute(STATE));
    }

    @Test
    public void getMuteError() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"player/get_mute\", \"result\": \"fail\", \"message\": \"eid=2&text=ID Not Valid&pid=null\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_MUTE, heosCommand.command);
        assertFalse(response.result);

        HeosError error = response.getError();
        assertNotNull(error);
        assertEquals(HeosErrorCode.INVALID_ID, error.code);
    }

    @Test
    public void browseBrowseUnderProcess() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"browse/browse\", \"result\": \"success\", \"message\": \"command under process&sid=1025\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.BROWSE, heosCommand.commandGroup);
        assertEquals(HeosCommand.BROWSE, heosCommand.command);
        assertTrue(response.result);

        assertEquals(Long.valueOf(1025), response.getNumericAttribute(SOURCE_ID));
        assertFalse(response.isFinished());
    }

    @Test
    public void incorrectLevel() {
        HeosResponseObject<Void> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"player/set_volume\", \"result\": \"fail\", \"message\": \"eid=9&text=Parameter out of range&pid=-831584083&level=OFF\"}}",
                Void.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.SET_VOLUME, heosCommand.command);
        assertFalse(response.result);

        HeosError error = response.getError();
        assertNotNull(error);
        assertEquals(HeosErrorCode.PARAMETER_OUT_OF_RANGE, error.code);
        assertEquals("#9: Parameter out of range", error.code.toString());
    }

    @Test
    public void getPlayers() {
        HeosResponseObject<Player[]> response = subject.parseResponse(
                """
                        {"heos": {"command": "player/get_players", "result": "success", "message": ""}, "payload": [\
                        {"name": "Kantoor HEOS 3", "pid": -831584083, "model": "HEOS 3", "version": "1.520.200", "ip": "192.168.1.230", "network": "wired", "lineout": 0, "serial": "ACNG9180110887"}, \
                        {"name": "HEOS Bar", "pid": 1958912779, "model": "HEOS Bar", "version": "1.520.200", "ip": "192.168.1.195", "network": "wired", "lineout": 0, "serial": "ADAG9180917029"}]}\
                        """,
                Player[].class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_PLAYERS, heosCommand.command);
        assertTrue(response.result);

        Player[] payload = response.payload;
        assertNotNull(payload);
        assertEquals(2, payload.length);

        Player player0 = payload[0];

        assertEquals("Kantoor HEOS 3", player0.name);
        assertEquals(-831584083, player0.playerId);
        assertEquals("HEOS 3", player0.model);
        assertEquals("1.520.200", player0.version);
        assertEquals("192.168.1.230", player0.ip);
        assertEquals("wired", player0.network);
        assertEquals(0, player0.lineout);
        assertEquals("ACNG9180110887", player0.serial);

        Player player1 = payload[1];

        assertEquals("HEOS Bar", player1.name);
        assertEquals(1958912779, player1.playerId);
        assertEquals("HEOS Bar", player1.model);
        assertEquals("1.520.200", player1.version);
        assertEquals("192.168.1.195", player1.ip);
        assertEquals("wired", player1.network);
        assertEquals(0, player1.lineout);
        assertEquals("ADAG9180917029", player1.serial);
    }

    @Test
    public void getPlayerInfo() {
        HeosResponseObject<Player> response = subject.parseResponse(
                "{\"heos\": {\"command\": \"player/get_player_info\", \"result\": \"success\", \"message\": \"pid=1958912779\"}, \"payload\": {\"name\": \"HEOS Bar\", \"pid\": 1958912779, \"model\": \"HEOS Bar\", \"version\": \"1.520.200\", \"ip\": \"192.168.1.195\", \"network\": \"wired\", \"lineout\": 0, \"serial\": \"ADAG9180917029\"}}",
                Player.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_PLAYER_INFO, heosCommand.command);
        assertTrue(response.result);

        Player payload = response.payload;
        assertNotNull(payload);
        assertEquals("HEOS Bar", payload.name);
        assertEquals(1958912779, payload.playerId);
        assertEquals("HEOS Bar", payload.model);
        assertEquals("1.520.200", payload.version);
        assertEquals("192.168.1.195", payload.ip);
        assertEquals("wired", payload.network);
        assertEquals(0, payload.lineout);
        assertEquals("ADAG9180917029", payload.serial);
    }

    @Test
    public void getNowPlayingMedia() {
        HeosResponseObject<Media> response = subject.parseResponse(
                """
                        {"heos": {"command": "player/get_now_playing_media", "result": "success", "message": "pid=1958912779"}, "payload": \
                        {"type": "song", "song": "Solo (feat. Demi Lovato)", "album": "What Is Love? (Deluxe)", "artist": "Clean Bandit", "image_url": "http://192.168.1.230:8015//m-browsableMediaUri/getImageFromTag/mnt/326C72A3E307501E47DE2B0F47D90EB8/Clean%20Bandit/What%20Is%20Love_%20(Deluxe)/03%20Solo%20(feat.%20Demi%20Lovato).m4a", "album_id": "", "mid": "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/Clean+Bandit/What+Is+Love_+(Deluxe)/03+Solo+(feat.+Demi+Lovato).m4a", "qid": 1, "sid": 1024}, "options": []}
                        """,
                Media.class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.PLAYER, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_NOW_PLAYING_MEDIA, heosCommand.command);
        assertTrue(response.result);

        assertEquals(Long.valueOf(1958912779), response.getNumericAttribute(PLAYER_ID));

        Media payload = response.payload;
        assertNotNull(payload);
        assertEquals("song", payload.type);
        assertEquals("Solo (feat. Demi Lovato)", payload.song);
        assertEquals("What Is Love? (Deluxe)", payload.album);
        assertEquals("Clean Bandit", payload.artist);
        assertEquals(
                "http://192.168.1.230:8015//m-browsableMediaUri/getImageFromTag/mnt/326C72A3E307501E47DE2B0F47D90EB8/Clean%20Bandit/What%20Is%20Love_%20(Deluxe)/03%20Solo%20(feat.%20Demi%20Lovato).m4a",
                payload.imageUrl);
        assertEquals("", payload.albumId);
        assertEquals(
                "http://192.168.1.230:8015/m-1c176905-f6c7-d168-dc35-86b4735c5976/Clean+Bandit/What+Is+Love_+(Deluxe)/03+Solo+(feat.+Demi+Lovato).m4a",
                payload.mediaId);
        assertEquals(1, payload.queueId);
        assertEquals(1024, payload.sourceId);
    }

    @Test
    public void browsePlaylist() {
        HeosResponseObject<BrowseResult[]> response = subject.parseResponse(
                """
                        {"heos": {"command": "browse/browse", "result": "success", "message": "sid=1025&returned=6&count=6"}, "payload": [\
                        {"container": "yes", "type": "playlist", "cid": "132562", "playable": "yes", "name": "Maaike Ouboter - En hoe het dan ook weer dag wordt", "image_url": ""}, \
                        {"container": "yes", "type": "playlist", "cid": "132563", "playable": "yes", "name": "Maaike Ouboter - Vanaf nu is het van jou", "image_url": ""}, \
                        {"container": "yes", "type": "playlist", "cid": "162887", "playable": "yes", "name": "Easy listening", "image_url": ""}, \
                        {"container": "yes", "type": "playlist", "cid": "174461", "playable": "yes", "name": "Nieuwe muziek 5-2019", "image_url": ""}, \
                        {"container": "yes", "type": "playlist", "cid": "194000", "playable": "yes", "name": "Nieuwe muziek 2019-05", "image_url": ""}, \
                        {"container": "yes", "type": "playlist", "cid": "194001", "playable": "yes", "name": "Clean Bandit", "image_url": ""}]}\
                        """,
                BrowseResult[].class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.BROWSE, heosCommand.commandGroup);
        assertEquals(HeosCommand.BROWSE, heosCommand.command);
        assertTrue(response.result);

        assertEquals(Long.valueOf(1025), response.getNumericAttribute(SOURCE_ID));
        assertEquals(Long.valueOf(6), response.getNumericAttribute(RETURNED));
        assertEquals(Long.valueOf(6), response.getNumericAttribute(COUNT));

        BrowseResult[] payload = response.payload;
        assertNotNull(payload);
        BrowseResult result = payload[5];

        assertEquals(YesNoEnum.YES, result.container);
        assertEquals(BrowseResultType.PLAYLIST, result.type);
        assertEquals(YesNoEnum.YES, result.playable);
        assertEquals("194001", result.containerId);
        assertEquals("Clean Bandit", result.name);
        assertEquals("", result.imageUrl);
    }

    @Test
    public void browseFavorites() {
        HeosResponseObject<BrowseResult[]> response = subject.parseResponse(
                """
                        {"heos": {"command": "browse/browse", "result": "success", "message": "sid=1028&returned=3&count=3"}, "payload": [\
                        {"container": "no", "mid": "s6707", "type": "station", "playable": "yes", "name": "NPO 3FM 96.8 (Top 40 %26 Pop Music)", "image_url": "http://cdn-profiles.tunein.com/s6707/images/logoq.png?t=636268"}, \
                        {"container": "no", "mid": "s2967", "type": "station", "playable": "yes", "name": "Classic FM Nederland (Classical Music)", "image_url": "http://cdn-radiotime-logos.tunein.com/s2967q.png"}, \
                        {"container": "no", "mid": "s1993", "type": "station", "playable": "yes", "name": "BNR Nieuwsradio", "image_url": "http://cdn-radiotime-logos.tunein.com/s1993q.png"}], \
                        "options": [{"browse": [{"id": 20, "name": "Remove from HEOS Favorites"}]}]}\
                        """,
                BrowseResult[].class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.BROWSE, heosCommand.commandGroup);
        assertEquals(HeosCommand.BROWSE, heosCommand.command);
        assertTrue(response.result);

        assertEquals(Long.valueOf(1028), response.getNumericAttribute(SOURCE_ID));
        assertEquals(Long.valueOf(3), response.getNumericAttribute(RETURNED));
        assertEquals(Long.valueOf(3), response.getNumericAttribute(COUNT));

        BrowseResult[] payload = response.payload;
        assertNotNull(payload);
        BrowseResult result = payload[0];

        assertEquals(YesNoEnum.NO, result.container);
        assertEquals("s6707", result.mediaId);
        assertEquals(BrowseResultType.STATION, result.type);
        assertEquals(YesNoEnum.YES, result.playable);
        assertEquals("NPO 3FM 96.8 (Top 40 %26 Pop Music)", result.name);
        assertEquals("http://cdn-profiles.tunein.com/s6707/images/logoq.png?t=636268", result.imageUrl);

        // TODO validate options
    }

    @Test
    public void getGroups() {
        HeosResponseObject<Group[]> response = subject.parseResponse(
                """
                        {"heos": {"command": "group/get_groups", "result": "success", "message": ""}, "payload": [ \
                        {"name": "Group 1", "gid": "214243242", "players": [ {"name": "HEOS 1", "pid": "2142443242", "role": "leader"}, {"name": "HEOS 3", "pid": "32432423432", "role": "member"}, {"name": "HEOS 5", "pid": "342423564", "role": "member"}]}, \
                        {"name": "Group 2", "gid": "2142432342", "players": [ {"name": "HEOS 3", "pid": "32432423432", "role": "member"}, {"name": "HEOS 5", "pid": "342423564", "role": "member"}]}]}\
                        """,
                Group[].class);

        HeosCommandTuple heosCommand = response.heosCommand;
        assertNotNull(heosCommand);
        assertEquals(HeosCommandGroup.GROUP, heosCommand.commandGroup);
        assertEquals(HeosCommand.GET_GROUPS, heosCommand.command);
        assertTrue(response.result);

        Group[] payload = response.payload;
        assertNotNull(payload);
        Group group = payload[0];

        assertEquals("Group 1", group.name);
        assertEquals("214243242", group.id);

        List<Group.Player> players = group.players;

        Group.Player player0 = players.get(0);
        assertEquals("HEOS 1", player0.name);
        assertEquals("2142443242", player0.id);
        assertEquals(GroupPlayerRole.LEADER, player0.role);

        Group.Player player1 = players.get(1);
        assertEquals("HEOS 3", player1.name);
        assertEquals("32432423432", player1.id);
        assertEquals(GroupPlayerRole.MEMBER, player1.role);
    }
}
