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
                        player.getSpotifySession().transferPlay(getDeviceId());
                        player.getSpotifySession().playActiveTrack(getDeviceId());
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        player.getSpotifySession().pauseActiveTrack(getDeviceId());
                    }
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        player.getSpotifySession().transferPlay(getDeviceId());
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
                if (command instanceof StringType) {
                    String cmd = ((StringType) command).toString();
                    if (cmd.equalsIgnoreCase("play")) {
                        player.getSpotifySession().playActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PLAY);
                    } else if (cmd.equalsIgnoreCase("pause")) {
                        player.getSpotifySession().pauseActiveTrack();
                        setChannelValue(CHANNEL_TRACKPLAYER, PlayPauseType.PAUSE);
                    } else if (cmd.equalsIgnoreCase("next")) {
                        player.getSpotifySession().nextTrack();
                    } else if (cmd.equalsIgnoreCase("prev") || cmd.equalsIgnoreCase("previous")) {
                        player.getSpotifySession().previousTrack();
                    }

                }
                break;
            case CHANNEL_DEVICESHUFFLE:
                logger.debug("CHANNEL_DEVICESHUFFLE {}", command.getClass().getName());

                if (command instanceof OnOffType) {
                    player.getSpotifySession().setShuffleState(getDeviceId(),
                            command.equals(OnOffType.OFF) ? "false" : "true");
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
            case CHANNEL_TRACKPLAY:
                if (command instanceof StringType) {
                    player.getSpotifySession().transferPlay(getDeviceId(), ((StringType) command).toString());
                    player.getSpotifySession().playTrack(getDeviceId(), ((StringType) command).toString());
                }
                break;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Spotify device handler.");

        super.initialize();
        Map<String, String> props = this.thing.getProperties();
        deviceId = props.get("id");
        String isRestricted = props.get("is_restricted");

        if (isRestricted == null || isRestricted.equals("true")) {
            updateStatus(ThingStatus.OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        logger.debug("Initialize Spotify device handler done.");
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
