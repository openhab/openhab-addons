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
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.BrowseResult;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link HeosFacade} is the interface for handling commands, which are
 * sent to the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosFacade {
    private static final int MAX_QUEUE_PAGES = 25;
    private final Logger logger = LoggerFactory.getLogger(HeosFacade.class);

    private final HeosSystem heosSystem;
    private final HeosEventController eventController;

    public HeosFacade(HeosSystem heosSystem, HeosEventController eventController) {
        this.heosSystem = heosSystem;
        this.eventController = eventController;
    }

    public synchronized List<BrowseResult> getFavorites() throws IOException, ReadException {
        return getBrowseResults(FAVORITE_SID);
    }

    public List<BrowseResult> getInputs() throws IOException, ReadException {
        return getBrowseResults(String.valueOf(INPUT_SID));
    }

    public List<BrowseResult> getPlaylists() throws IOException, ReadException {
        return getBrowseResults(PLAYLISTS_SID);
    }

    private List<BrowseResult> getBrowseResults(String sourceIdentifier) throws IOException, ReadException {
        HeosResponseObject<BrowseResult[]> response = browseSource(sourceIdentifier);
        logger.debug("Response: {}", response);

        if (response.payload == null) {
            return Collections.emptyList();
        }
        logger.debug("Received results: {}", Arrays.asList(response.payload));

        return Arrays.asList(response.payload);
    }

    public List<Media> getQueue(String pid) throws IOException, ReadException {
        List<Media> media = new ArrayList<>();
        for (int page = 0; page < MAX_QUEUE_PAGES; page++) {
            HeosResponseObject<Media[]> response = fetchQueue(pid, page);
            if (!response.result || response.payload == null) {
                break;
            }

            media.addAll(Arrays.asList(response.payload));

            if (response.payload.length < 100) {
                break;
            }

            if (page == MAX_QUEUE_PAGES - 1) {
                logger.info("Currently only a maximum of {} pages is fetched for every queue", MAX_QUEUE_PAGES);
            }
        }

        return media;
    }

    HeosResponseObject<Media[]> fetchQueue(String pid, int page) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getQueue(pid, page * 100, (page + 1) * 100), Media[].class);
    }

    public HeosResponseObject<Player> getPlayerInfo(String pid) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getPlayerInfo(pid), Player.class);
    }

    public HeosResponseObject<Group> getGroupInfo(String gid) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getGroupInfo(gid), Group.class);
    }

    /**
     * Pauses the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void pause(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setPlayStatePause(pid));
    }

    /**
     * Starts the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void play(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setPlayStatePlay(pid));
    }

    /**
     * Stops the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void stop(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setPlayStateStop(pid));
    }

    /**
     * Jumps to the next song on the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void next(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playNext(pid));
    }

    /**
     * Jumps to the previous song on the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void previous(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playPrevious(pid));
    }

    /**
     * Toggles the mute state the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void mute(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setMuteToggle(pid));
    }

    /**
     * Mutes the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void muteON(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setMuteOn(pid));
    }

    /**
     * Un-mutes the HEOS player
     *
     * @param pid The PID of the dedicated player
     */
    public void muteOFF(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setMuteOff(pid));
    }

    /**
     * Set the play mode of the player or group
     *
     * @param pid The PID of the dedicated player or group
     * @param mode The shuffle mode: Allowed commands: on; off
     */
    public void setShuffleMode(String pid, String mode) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setShuffleMode(pid, mode));
    }

    /**
     * Sets the repeat mode of the player or group
     *
     * @param pid The ID of the dedicated player or group
     * @param mode The repeat mode. Allowed commands: on_all; on_one; off
     */
    public void setRepeatMode(String pid, String mode) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setRepeatMode(pid, mode));
    }

    /**
     * Set the HEOS player to a dedicated volume
     *
     * @param vol The volume the player shall be set to (value between 0 -100)
     * @param pid The ID of the dedicated player or group
     */
    public void setVolume(String vol, String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setVolume(vol, pid));
    }

    /**
     * Increases the HEOS player volume 1 Step
     *
     * @param pid The ID of the dedicated player or group
     */
    public void increaseVolume(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.volumeUp(pid));
    }

    /**
     * Decreases the HEOS player volume 1 Step
     *
     * @param pid The ID of the dedicated player or group
     */
    public void decreaseVolume(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.volumeDown(pid));
    }

    /**
     * Toggles mute state of the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroup(String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setMuteToggle(gid));
    }

    /**
     * Mutes the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroupON(String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setGroupMuteOn(gid));
    }

    /**
     * Un-mutes the HEOS group
     *
     * @param gid The GID of the group
     */
    public void muteGroupOFF(String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setGroupMuteOff(gid));
    }

    /**
     * Set the volume of the group to a specific level
     *
     * @param vol The volume the group shall be set to (value between 0-100)
     * @param gid The GID of the group
     */
    public void volumeGroup(String vol, String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setGroupVolume(vol, gid));
    }

    /**
     * Increases the HEOS group volume 1 Step
     *
     * @param gid The ID of the dedicated player or group
     */
    public void increaseGroupVolume(String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setGroupVolumeUp(gid));
    }

    /**
     * Decreases the HEOS group volume 1 Step
     *
     * @param gid The ID of the dedicated player or group
     */
    public void decreaseGroupVolume(String gid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.setGroupVolumeDown(gid));
    }

    /**
     * Un-Group the HEOS group to single player
     *
     * @param gid The GID of the group
     */
    public void ungroupGroup(String gid) throws IOException, ReadException {
        String[] pid = new String[] { gid };
        heosSystem.send(HeosCommands.setGroup(pid));
    }

    /**
     * Builds a group from single players
     *
     * @param pids The single player IDs of the player which shall be grouped
     * @return
     */
    public boolean groupPlayer(String[] pids) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.setGroup(pids)).result;
    }

    /**
     * Browses through a HEOS source. Currently no response
     *
     * @param sid The source sid which shall be browsed
     * @return
     */
    public HeosResponseObject<BrowseResult[]> browseSource(String sid) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.browseSource(sid), BrowseResult[].class);
    }

    /**
     * Adds a media container to the queue and plays the media directly
     * Information of the sid and cid has to be obtained via the browse function
     *
     * @param pid The player ID where the media object shall be played
     * @param sid The source ID where the media is located
     * @param cid The container ID of the media
     */
    public void addContainerToQueuePlayNow(String pid, String sid, String cid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.addContainerToQueuePlayNow(pid, sid, cid));
    }

    /**
     * Reboot the bridge to which the connection is established
     */
    public void reboot() throws IOException, ReadException {
        heosSystem.send(HeosCommands.rebootSystem());
    }

    /**
     * Login in via the bridge to the HEOS account
     *
     * @param name The username
     * @param password The password of the user
     * @return
     */
    public HeosResponseObject<Void> logIn(String name, String password) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.signIn(name, password));
    }

    /**
     * Get all the players known by HEOS
     *
     * @return
     */
    public HeosResponseObject<Player[]> getPlayers() throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getPlayers(), Player[].class);
    }

    /**
     * Get all the groups known by HEOS
     *
     * @return
     */
    public HeosResponseObject<Group[]> getGroups() throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getGroups(), Group[].class);
    }

    /**
     * Plays a specific station on the HEOS player
     *
     * @param pid The player ID
     * @param sid The source ID where the media is located
     * @param cid The container ID of the media
     * @param mid The media ID of the media
     * @param name Station name returned by 'browse' command.
     */
    public void playStream(@Nullable String pid, @Nullable String sid, @Nullable String cid, @Nullable String mid,
            @Nullable String name) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playStream(pid, sid, cid, mid, name));
    }

    /**
     * Plays a specific station on the HEOS player
     *
     * @param pid The player ID
     * @param sid The source ID where the media is located
     * @param mid The media ID of the media
     */
    public void playStream(String pid, String sid, String mid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playStream(pid, sid, mid));
    }

    /**
     * Plays a specified local input source on the player.
     * Input name as per specified in HEOS CLI Protocol
     *
     * @param pid
     * @param input
     */
    public void playInputSource(String pid, String input) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playInputSource(pid, pid, input));
    }

    /**
     * Plays a specified input source from another player on the selected player.
     * Input name as per specified in HEOS CLI Protocol
     *
     * @param destinationPid the PID where the source shall be played
     * @param sourcePid the PID where the source is located.
     * @param input the input name
     */
    public void playInputSource(String destinationPid, String sourcePid, String input)
            throws IOException, ReadException {
        heosSystem.send(HeosCommands.playInputSource(destinationPid, sourcePid, input));
    }

    /**
     * Plays a file from a URL
     *
     * @param pid the PID where the file shall be played
     * @param url the complete URL the file is located
     */
    public void playURL(String pid, URL url) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playURL(pid, url.toString()));
    }

    /**
     * clear the queue
     *
     * @param pid The player ID the media is playing on
     */
    public void clearQueue(String pid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.clearQueue(pid));
    }

    /**
     * Deletes a media from the queue
     *
     * @param pid The player ID the media is playing on
     * @param qid The queue ID of the media. (starts by 1)
     */
    public void deleteMediaFromQueue(String pid, String qid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.deleteQueueItem(pid, qid));
    }

    /**
     * Plays a specific media file from the queue
     *
     * @param pid The player ID the media shall be played on
     * @param qid The queue ID of the media. (starts by 1)
     */
    public void playMediaFromQueue(String pid, String qid) throws IOException, ReadException {
        heosSystem.send(HeosCommands.playQueueItem(pid, qid));
    }

    /**
     * Asks for the actual state of the player. The result has
     * to be handled by the event controller. The system returns {@link HeosConstants#PLAY},
     * {@link HeosConstants#PAUSE} or {@link HeosConstants#STOP}.
     *
     * @param id The player ID the state shall get for
     * @return
     */
    public HeosResponseObject<Void> getPlayState(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getPlayState(id));
    }

    /**
     * Ask for the actual mute state of the player. The result has
     * to be handled by the event controller. The HEOS system returns {@link HeosConstants#ON}
     * or {@link HeosConstants#OFF}.
     *
     * @param id The player id the mute state shall get for
     * @return
     */
    public HeosResponseObject<Void> getPlayerMuteState(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getMute(id));
    }

    /**
     * Ask for the actual volume the player. The result has
     * to be handled by the event controller. The HEOS system returns
     * a value between 0 and 100
     *
     * @param id The player id the volume shall get for
     * @return
     */
    public HeosResponseObject<Void> getPlayerVolume(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getVolume(id));
    }

    /**
     * Ask for the actual shuffle mode of the player. The result has
     * to be handled by the event controller. The HEOS system returns {@link HeosConstants#ON},
     * {@link HeosConstants#REPEAT_ALL} or {@link HeosConstants#REPEAT_ONE}
     *
     * @param id The player id the shuffle mode shall get for
     * @return
     */
    public HeosResponseObject<Void> getPlayMode(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getPlayMode(id));
    }

    public HeosResponseObject<Void> getGroupMuteState(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getGroupMute(id));
    }

    public HeosResponseObject<Void> getGroupVolume(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getGroupVolume(id));
    }

    public HeosResponseObject<Media> getNowPlayingMedia(String id) throws IOException, ReadException {
        return heosSystem.send(HeosCommands.getNowPlayingMedia(id), Media.class);
    }

    /**
     * Sends a RAW command to the HEOS bridge. The command has to be
     * in accordance with the HEOS CLI specification
     *
     * @param command to send
     * @return
     */
    public HeosResponseObject<JsonElement> sendRawCommand(String command) throws IOException, ReadException {
        return heosSystem.send(command, JsonElement.class);
    }

    /**
     * Register an {@link HeosEventListener} to get notification of system events
     *
     * @param listener The HeosEventListener
     */
    public void registerForChangeEvents(HeosEventListener listener) {
        eventController.addListener(listener);
    }

    /**
     * Unregister an {@link HeosEventListener} to get notification of system events
     *
     * @param listener The HeosEventListener
     */
    public void unregisterForChangeEvents(HeosEventListener listener) {
        eventController.removeListener(listener);
    }

    public boolean isConnected() {
        return heosSystem.isConnected();
    }

    public void closeConnection() {
        heosSystem.closeConnection();
    }
}
