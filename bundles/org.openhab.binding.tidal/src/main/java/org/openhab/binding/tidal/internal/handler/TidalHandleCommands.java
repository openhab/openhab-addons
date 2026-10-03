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
package org.openhab.binding.tidal.internal.handler;

import static org.openhab.binding.tidal.internal.TidalBindingConstants.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tidal.internal.api.TidalApi;
import org.openhab.binding.tidal.internal.api.model.Playlist;
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
 * Class to handle {@link Command} actions and call the Tidal Web Api.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
class TidalHandleCommands {

    private final Logger logger = LoggerFactory.getLogger(TidalHandleCommands.class);

    private final TidalApi tidalApi;

    private List<Playlist> playlists = Collections.emptyList();

    /**
     * Constructor. For the bridge the deviceId is empty.
     *
     * @param tidalApi The api class to use to call the tidal api
     */
    public TidalHandleCommands(TidalApi tidalApi) {
        this.tidalApi = tidalApi;
    }

    public void setDevices() {
    }

    public void setPlaylists(final List<Playlist> playlists) {
        this.playlists = playlists;
    }

    /**
     * Handles commands from the given Channel and calls Tidal Web Api with the given command.
     *
     * @param channelUID Channel the command is from
     * @param command command to run
     * @param active true if current known device is the active device
     * @param deviceId Current known active Tidal device id
     * @return true if the command was done or else if no channel or command matched
     */
    public boolean handleCommand(ChannelUID channelUID, Command command, boolean active, String deviceId) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        boolean commandRun = false;
        final String channel = channelUID.getId();

        switch (channel) {
            case CHANNEL_DEVICENAME:
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
                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKREPEAT:
                if (command instanceof StringType) {
                    commandRun = true;
                }
                break;
            case CHANNEL_DEVICEVOLUME:
                if (command instanceof DecimalType decimalCommand) {
                    final PercentType volume = command instanceof PercentType percentType ? percentType
                            : new PercentType(decimalCommand.intValue());

                    commandRun = true;
                }
                break;
            case CHANNEL_TRACKPLAY:
            case CHANNEL_PLAYLISTS:
                if (command instanceof StringType) {
                    commandRun = true;
                }
                break;
            case CHANNEL_PLAYLISTNAME:
                if (command instanceof StringType) {
                    final String newName = command.toString();

                    commandRun = true;
                }
                break;
        }
        return commandRun;
    }

    private void playDeviceId(String newDeviceId, boolean active, String currentDeviceId) {
        if (currentDeviceId.equals(newDeviceId) && active) {
        } else {
        }
    }

    /**
     * Helper method to handle device play status.
     *
     * @param command command to run
     * @param active true if the device this command is send to is the active device
     * @param deviceId Tidal device id the command is intended for
     * @return true if the command was done or else if no channel or command matched
     */
    private boolean handleDevicePlay(Command command, boolean active, String deviceId) {
        if (command instanceof PlayPauseType) {
            final boolean play = command == PlayPauseType.PLAY;

            if (active || deviceId.isEmpty()) {
                if (play) {
                } else {
                }
            } else {
            }
            return true;
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
            } else {
            }
            return true;
        }
        return false;
    }
}
