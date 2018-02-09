/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.handler;

import static org.openhab.binding.denonmarantz.DenonMarantzBindingConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.denonmarantz.DenonMarantzBindingConstants;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.DenonMarantzStateChangedListener;
import org.openhab.binding.denonmarantz.internal.UnsupportedCommandTypeException;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnectorFactory;
import org.openhab.binding.denonmarantz.internal.connector.http.DenonMarantzHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DenonMarantzHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
public class DenonMarantzHandler extends BaseThingHandler implements DenonMarantzStateChangedListener {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzHandler.class);
    private DenonMarantzConnector connector;
    private DenonMarantzConfiguration config;
    private DenonMarantzConnectorFactory connectorFactory = new DenonMarantzConnectorFactory();
    private DenonMarantzState denonMarantzState;

    public DenonMarantzHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connector == null) {
            return;
        }

        if (connector instanceof DenonMarantzHttpConnector && command instanceof RefreshType) {
            // Refreshing individual channels isn't supported by the Http connector.
            // The connector refreshes all channels together at the configured polling interval.
            return;
        }

        try {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    connector.sendPowerCommand(command, 0);
                    break;
                case CHANNEL_MAIN_ZONE_POWER:
                    connector.sendPowerCommand(command, 1);
                    break;
                case CHANNEL_MUTE:
                    connector.sendMuteCommand(command, 1);
                    break;
                case CHANNEL_MAIN_VOLUME:
                    connector.sendVolumeCommand(command, 1);
                    break;
                case CHANNEL_MAIN_VOLUME_DB:
                    connector.sendVolumeDbCommand(command, 1);
                    break;
                case CHANNEL_INPUT:
                    connector.sendInputCommand(command, 1);
                    break;
                case CHANNEL_SURROUND_PROGRAM:
                    connector.sendSurroundProgramCommand(command);
                    break;
                case CHANNEL_COMMAND:
                    connector.sendCustomCommand(command);
                    break;

                case CHANNEL_ZONE2_POWER:
                    connector.sendPowerCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_MUTE:
                    connector.sendMuteCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_VOLUME:
                    connector.sendVolumeCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_VOLUME_DB:
                    connector.sendVolumeCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_INPUT:
                    connector.sendInputCommand(command, 2);
                    break;

                case CHANNEL_ZONE3_POWER:
                    connector.sendPowerCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_MUTE:
                    connector.sendMuteCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_VOLUME:
                    connector.sendVolumeCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_VOLUME_DB:
                    connector.sendVolumeCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_INPUT:
                    connector.sendInputCommand(command, 3);
                    break;

                default:
                    throw new UnsupportedCommandTypeException();
            }
        } catch (UnsupportedCommandTypeException e) {
            logger.debug("Unsupported command {} for channel {}", command, channelUID.getId());
        }
    }

    public boolean checkConfiguration() {
        // prevent too low values for polling interval
        if (config.httpPollingInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The polling interval should be at least 5 seconds!");
            return false;
        }
        // Check zone count is within supported range
        if (config.getZoneCount() < 1 || config.getZoneCount() > 3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This binding supports 1 to 3 zones. Please update the zone count.");
            return false;
        }
        return true;
    }

    @Override
    public void initialize() {
        config = getConfigAs(DenonMarantzConfiguration.class);

        if (!checkConfiguration()) {
            return;
        }

        denonMarantzState = new DenonMarantzState(this);
        configureZoneChannels();
        // create connection (either Telnet or HTTP)
        // ThingStatus ONLINE/OFFLINE is set when AVR status is known.
        createConnection();
    }

    private void createConnection() {
        connector = connectorFactory.getConnector(config, denonMarantzState, scheduler);
        connector.connect();
    }

    private void configureZoneChannels() {
        logger.debug("Configuring zone channels");
        Integer zoneCount = config.getZoneCount();
        ArrayList<Channel> channels = new ArrayList<Channel>(this.getThing().getChannels());
        boolean channelsUpdated = false;

        // construct a set with the existing channel type UIDs, to quickly check
        HashSet<ChannelTypeUID> currentChannelTypeUIDs = new HashSet<ChannelTypeUID>();
        channels.forEach(channel -> currentChannelTypeUIDs.add(channel.getChannelTypeUID()));

        HashSet<Entry<ChannelTypeUID, String>> channelsToRemove = new HashSet<Entry<ChannelTypeUID, String>>();

        if (zoneCount > 1) {
            ArrayList<Entry<ChannelTypeUID, String>> channelsToAdd = new ArrayList<Entry<ChannelTypeUID, String>>(
                    DenonMarantzBindingConstants.ZONE2_CHANNEL_TYPES.entrySet());

            if (zoneCount > 2) {
                // add channels for zone 3 (more zones currently not supported)
                channelsToAdd.addAll(DenonMarantzBindingConstants.ZONE3_CHANNEL_TYPES.entrySet());
            } else {
                channelsToRemove.addAll(DenonMarantzBindingConstants.ZONE3_CHANNEL_TYPES.entrySet());
            }

            // filter out the already existing channels
            channelsToAdd.removeIf(c -> currentChannelTypeUIDs.contains(c.getKey()));

            // add the channels that were not yet added
            if (!channelsToAdd.isEmpty()) {
                for (Entry<ChannelTypeUID, String> entry : channelsToAdd) {
                    String itemType = DenonMarantzBindingConstants.CHANNEL_ITEM_TYPES.get(entry.getValue());
                    Channel channel = ChannelBuilder
                            .create(new ChannelUID(this.getThing().getUID(), entry.getValue()), itemType)
                            .withType(entry.getKey()).build();
                    channels.add(channel);
                }
                channelsUpdated = true;
            } else {
                logger.debug("No zone channels have been added");
            }
        } else {
            channelsToRemove.addAll(DenonMarantzBindingConstants.ZONE2_CHANNEL_TYPES.entrySet());
            channelsToRemove.addAll(DenonMarantzBindingConstants.ZONE3_CHANNEL_TYPES.entrySet());
        }

        // filter out the non-existing channels
        channelsToRemove.removeIf(c -> !currentChannelTypeUIDs.contains(c.getKey()));

        // remove the channels that were not yet added
        if (!channelsToRemove.isEmpty()) {
            for (Entry<ChannelTypeUID, String> entry : channelsToRemove) {
                if (channels.removeIf(c -> (entry.getKey()).equals(c.getChannelTypeUID()))) {
                    logger.trace("Removed channel {}", entry.getKey().getAsString());
                } else {
                    logger.trace("Could NOT remove channel {}", entry.getKey().getAsString());
                }
            }
            channelsUpdated = true;
        } else {
            logger.debug("No zone channels have been removed");
        }

        // update Thing if channels changed
        if (channelsUpdated) {
            updateThing(editThing().withChannels(channels).build());
        }
    }

    @Override
    public void dispose() {
        if (connector != null) {
            connector.dispose();
            connector = null;
        }
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        String channelID = channelUID.getId();
        if (isLinked(channelID)) {
            updateState(channelID, denonMarantzState.getStateForChannelID(channelID));
        }
    }

    @Override
    public void stateChanged(String channelID, State state) {
        logger.debug("Received state {} for channelID {}", state, channelID);

        // Don't flood the log with thing 'updated: ONLINE' each time a single channel changed
        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(channelID, state);
    }

    @Override
    public void connectionError(String errorMessage) {
        if (this.getThing().getStatus() != ThingStatus.OFFLINE) {
            // Don't flood the log with thing 'updated: OFFLINE' when already offline
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }
}
