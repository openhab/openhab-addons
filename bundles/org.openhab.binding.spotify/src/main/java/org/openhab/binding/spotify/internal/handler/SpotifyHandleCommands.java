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
package org.openhab.binding.spotify.internal.handler;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.spotify.internal.api.SpotifyApi;
import org.openhab.binding.spotify.internal.api.model.Device;
import org.openhab.binding.spotify.internal.api.model.Playlist;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle {@link Command} actions and call the Spotify Web Api.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to separate class, general refactoring and bug fixes
 */
@NonNullByDefault
class SpotifyHandleCommands {

    private final Logger logger = LoggerFactory.getLogger(SpotifyHandleCommands.class);

    private final SpotifyApi spotifyApi;

    private List<Device> devices = Collections.emptyList();
    private List<Playlist> playlists = Collections.emptyList();

    /**
     * Constructor. For the bridge the deviceId is empty.
     *
     * @param spotifyApi The api class to use to call the spotify api
     */
    public SpotifyHandleCommands(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public void setDevices(final List<Device> devices) {
        this.devices = devices;
    }

    public void setPlaylists(final List<Playlist> playlists) {
        this.playlists = playlists;
    }

    /**
     * Handles commands from the given Channel and calls Spotify Web Api with the given command.
     *
     * @param channelUID Channel the command is from
     * @param command command to run
     * @param active true if current known device is the active device
     * @param deviceId Current known active Spotify device id
     * @return true if the command was done or else if no channel or command matched
     */
    public boolean handleCommand(ChannelUID channelUID, Command command, boolean active, String deviceId) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        boolean commandRun = false;
        final String channel = channelUID.getId();

        switch (channel) {
            case CHANNEL_DEVICENAME:
                if (command instanceof StringType) {
                    final String newName = command.toString();

                    devices.stream().filter(d -> d.getName().equals(newName)).findFirst()
                            .ifPresent(d -> playDeviceId(d.getId(), active, deviceId));
                    commandRun = true;
                }
                break;
            case CHANNEL_DEVICEID:
            case CHANNEL_DEVICES:
                if (command instanceof StringType) {
                    playDeviceId(command.toString(), active, deviceId);
                    commandRun = true;
                }
                break;
            case CHANNEL_DEVICEPLAYER:
            case CHANNEL_TRACKPLAYER:
                commandRun = handleDevicePlay(command, active, deviceId);
                break;
            case CHANNEL_DEVICESHUFFLE:
                if (command instanceof OnOffType onOffCommand) {
                    spotifyApi.setShuffleState(deviceId, onOffCommand);
                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKREPEAT:
                if (command instanceof StringType) {
                    spotifyApi.setRepeatState(deviceId, command.toString());
                    commandRun = true;
                }
            case CHANNEL_DEVICEVOLUME:
                if (command instanceof DecimalType decimalCommand) {
                    final PercentType volume = command instanceof PercentType percentType ? percentType
                            : new PercentType(decimalCommand.intValue());

                    spotifyApi.setVolume(deviceId, volume.intValue());
                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKPLAY:
            case CHANNEL_PLAYLISTS:
                if (command instanceof StringType) {
                    spotifyApi.playTrack(deviceId, command.toString(), 0, 0);
                    commandRun = true;
                }
                break;
            case CHANNEL_PLAYLISTNAME:
                if (command instanceof StringType) {
                    final String newName = command.toString();

                    playlists.stream().filter(pl -> pl.getName().equals(newName)).findFirst()
                            .ifPresent(pl -> spotifyApi.playTrack(deviceId, pl.getUri(), 0, 0));
                    commandRun = true;
                }
                break;
        }
        return commandRun;
    }

    private void playDeviceId(String newDeviceId, boolean active, String currentDeviceId) {
        if (currentDeviceId.equals(newDeviceId) && active) {
            spotifyApi.play(newDeviceId);
        } else {
            spotifyApi.transferPlay(newDeviceId, true);
        }
    }

    /**
     * Helper method to handle device play status.
     *
     * @param command command to run
     * @param active true if the device this command is send to is the active device
     * @param deviceId Spotify device id the command is intended for
     * @return true if the command was done or else if no channel or command matched
     */
    private boolean handleDevicePlay(Command command, boolean active, String deviceId) {
        if (command instanceof PlayPauseType) {
            final boolean play = command == PlayPauseType.PLAY;

            if (active || deviceId.isEmpty()) {
                if (play) {
                    spotifyApi.play(deviceId);
                } else {
                    spotifyApi.pause(deviceId);
                }
            } else {
                spotifyApi.transferPlay(deviceId, play);
            }
            return true;
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                spotifyApi.next(deviceId);
            } else {
                spotifyApi.previous(deviceId);
            }
            return true;
        }
        return false;
    }
}
