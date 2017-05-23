/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.handler;

import static org.openhab.binding.spotify.SpotifyBindingConstants.*;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andreas Stenlund - Initial contribution
 */
public class SpotifyDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SpotifyDeviceHandler.class);
    private SpotifyHandler player = null;
    private String deviceId;

    public SpotifyDeviceHandler(Thing thing) {
        super(thing);
        if (getBridge() != null) {
            player = (SpotifyHandler) getBridge().getHandler();
        }
    }

    public void changeStatus(ThingStatus status) {
        updateStatus(status);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (player == null && getBridge() != null) {
            player = (SpotifyHandler) getBridge().getHandler();
        }

        if (player == null) {
            return;
        }

        String channel = channelUID.getId();

        switch (channel) {
            case CHANNEL_DEVICEPLAY:
                logger.debug("CHANNEL_DEVICEPLAY {}", command.getClass().getName());

                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        player.getSpotifySession().playActiveTrack(getDeviceId());
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        player.getSpotifySession().pauseActiveTrack(getDeviceId());
                    }
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        player.getSpotifySession().playActiveTrack(getDeviceId());
                    } else if (command.equals(OnOffType.OFF)) {
                        player.getSpotifySession().pauseActiveTrack(getDeviceId());
                    }
                }
                if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        player.getSpotifySession().playActiveTrack(getDeviceId());
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        player.getSpotifySession().previousTrack(getDeviceId());
                    }

                }
                break;
            case CHANNEL_DEVICESHUFFLE:
                logger.debug("CHANNEL_DEVICESHUFFLE {}", command.getClass().getName());

                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        player.getSpotifySession().setShuffleState(deviceId, "true");
                    } else if (command.equals(OnOffType.OFF)) {
                        player.getSpotifySession().setShuffleState(deviceId, "false");
                    }
                }
                break;
            case CHANNEL_DEVICEVOLUME:
                if (command instanceof DecimalType) {
                    PercentType volume = new PercentType(((DecimalType) command).intValue());
                    player.getSpotifySession().setVolume(volume.intValue());
                    setChannelValue(CHANNEL_DEVICEVOLUME, volume);
                } else if (command instanceof PercentType) {
                    PercentType volume = (PercentType) command;
                    player.getSpotifySession().setVolume(volume.intValue());
                    setChannelValue(CHANNEL_DEVICEVOLUME, volume);
                }
                break;

        }
        if (channelUID.getId().equals(CHANNEL_TRACKID)) {
            if (command instanceof StringType) {
                player.getSpotifySession().playTrack(getDeviceId(), ((StringType) command).toString());
            }

        }

        if (channelUID.getId().equals(CHANNEL_DEVICESHUFFLE)) {
            if (command instanceof OnOffType) {
                player.getSpotifySession().setShuffleState(getDeviceId(),
                        command.equals(OnOffType.OFF) ? "false" : "true");
            }
        }

    }

    @Override
    public void initialize() {
        logger.debug("Initialize SpotifyConnect handler.");
        // this.spotifyDevice = new SpotifyDevice();

        super.initialize();
        Map<String, String> props = this.thing.getProperties();
        deviceId = props.get("id");
        String isRestricted = props.get("is_restricted");

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        if (isRestricted.equals("true")) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        logger.debug("Initialize SpotifyConnect handler done.");
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("SpotifyDevice {}: SpotifyBridge status changed to {}.", getDeviceId(),
                bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Spotify Bridge Offline"); // SpotifyConnectBindingConstants.getI18nConstant(SpotifyConnectBindingConstants.OFFLINE_CTLR_OFFLINE)
            logger.debug("SpotifyDevice {}: SpotifyBridge is not online.", getDeviceId(), bridgeStatusInfo.getStatus());
            return;
        }

        /*
         * for (Channel channel : getThing().getChannels()) {
         * // Process the channel properties and configuration
         * Map<String, String> properties = channel.getProperties();
         * Configuration configuration = channel.getConfiguration();
         * }
         */
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParmeters) {
        // can be overridden by subclasses
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParmeters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        // reinitialize with new configuration and persist changes
        dispose();
        updateConfiguration(configuration);
        initialize();
    }

    public void setChannelValue(String CHANNEL, State state) {
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            Channel channel = getThing().getChannel(CHANNEL);
            updateState(channel.getUID(), state);
            // logger.debug("Updating status of spotify device {} channel {}.", getThing().getLabel(),
            // channel.getUID());
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public SpotifyHandler getController() {
        return player;
    }

    public void setController(SpotifyHandler controller) {
        this.player = controller;
    }
}
