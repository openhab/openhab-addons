/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.handler;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.spotify.internal.api.SpotifyApi;
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

    private final Logger logger = LoggerFactory.getLogger(SpotifyDeviceHandler.class);

    private SpotifyApi spotifyApi;
    private String deviceId;

    /**
     * Constructor. For the bridge the deviceId is empty.
     *
     * @param spotifyApi The api class to use to call the spotify api
     * @param deviceId spotify device id or empty for bridge
     */
    public SpotifyHandleCommands(SpotifyApi spotifyApi, String deviceId) {
        this.spotifyApi = spotifyApi;
        this.deviceId = deviceId;
    }

    /**
     * Handles commands from the given Channel and calls Spotify Web Api with the given command.
     *
     * @param channelUID Channel the command is from
     * @param command command to run
     * @param active true if the device this command is send to is the active device
     * @return true if the command was done or else if no channel or command matched
     */
    public boolean handleCommand(ChannelUID channelUID, Command command, boolean active, String activeDeviceId) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        boolean commandRun = false;
        String channel = channelUID.getId();

        switch (channel) {
            case CHANNEL_DEVICENAME:
                if (command instanceof StringType) {
                    String newDeviceId = command.toString();

                    if (activeDeviceId.isEmpty() || activeDeviceId.equals(newDeviceId)) {
                        spotifyApi.play(newDeviceId);
                    } else {
                        spotifyApi.transferPlay(newDeviceId, true);
                    }
                    commandRun = true;
                }
                break;
            case CHANNEL_DEVICEPLAYER:
            case CHANNEL_TRACKPLAYER:
                commandRun = handleDevicePlay(command, active);
                break;
            case CHANNEL_DEVICESHUFFLE:
                if (command instanceof OnOffType) {
                    spotifyApi.setShuffleState(deviceId, (OnOffType) command);
                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKREPEAT:
                if (command instanceof StringType) {
                    spotifyApi.setRepeatState(deviceId, command.toString());
                    commandRun = true;
                }
            case CHANNEL_DEVICEVOLUME:
                if (command instanceof DecimalType) {
                    PercentType volume = new PercentType(((DecimalType) command).intValue());

                    spotifyApi.setVolume(deviceId, volume.intValue());
                    commandRun = true;
                } else if (command instanceof PercentType) {
                    PercentType volume = (PercentType) command;

                    spotifyApi.setVolume(deviceId, volume.intValue());
                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKPLAY:
            case CHANNEL_PLAYLIST:
                if (command instanceof StringType) {
                    spotifyApi.playTrack(deviceId, command.toString());
                    commandRun = true;
                }
                break;
        }
        return commandRun;
    }

    /**
     * Helper method to handle device play status.
     *
     * @param command command to run
     * @param active true if the device this command is send to is the active device
     * @return true if the command was done or else if no channel or command matched
     */
    private boolean handleDevicePlay(Command command, boolean active) {
        if (command instanceof PlayPauseType) {
            boolean play = command == PlayPauseType.PLAY;
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
