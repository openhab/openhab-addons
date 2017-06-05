/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.handler;

import static org.openhab.binding.denonmarantz.DenonMarantzBindingConstants.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.denonmarantz.DenonMarantzBindingConstants;
import org.openhab.binding.denonmarantz.internal.DenonMarantzConnector;
import org.openhab.binding.denonmarantz.internal.DenonMarantzStateChangedListener;
import org.openhab.binding.denonmarantz.internal.UnsupportedCommandTypeException;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
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
    private DenonMarantzConfiguration configuration;

    public DenonMarantzHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

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
                    logger.warn("Command for channel {} not supported.", channelUID.getId());
                    break;
            }

        } catch (UnsupportedCommandTypeException e) {
            logger.warn("Unsupported command {} for channel {}", command, channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        // create connection (either Telnet & HTTP or HTTP only)
        // ThingStatus ONLINE/OFFLINE is set when AVR status is known.
        createConnection();
        addZoneChannels();
    }

    private void createConnection() {
        configuration = getConfigAs(DenonMarantzConfiguration.class);
        if (connector != null) {
            connector.dispose();
        }
        connector = new DenonMarantzConnector(configuration, this, scheduler);
        connector.connect();
    }

    private void addZoneChannels() {
        // only add when this is a new Thing instance (and doesn't have the 'zones' property set yet).

        if (this.getThing().getProperties().containsKey("zones")) {
            return;
        }
        if (configuration.getZoneCount() > 1) {
            logger.debug("Adding zone channels");
            // add channels for the secondary zones
            ThingBuilder thingBuilder = editThing();
            CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<Channel>(this.getThing().getChannels());

            for (Entry<ChannelTypeUID, String> entry : DenonMarantzBindingConstants.ZONE2_CHANNEL_TYPES.entrySet()) {
                ChannelType type = TypeResolver.resolve(entry.getKey());
                Channel channel = ChannelBuilder
                        .create(new ChannelUID(this.getThing().getUID(), entry.getValue()), type.getItemType())
                        .withType(entry.getKey()).build();
                channels.add(channel);
            }

            if (configuration.getZoneCount() > 2) {
                // add channels for zone 3 (more zones currently not supported)
                for (Entry<ChannelTypeUID, String> entry : DenonMarantzBindingConstants.ZONE3_CHANNEL_TYPES
                        .entrySet()) {
                    ChannelType type = TypeResolver.resolve(entry.getKey());
                    Channel channel = ChannelBuilder
                            .create(new ChannelUID(this.getThing().getUID(), entry.getValue()), type.getItemType())
                            .withType(entry.getKey()).build();
                    channels.add(channel);
                }
            }

            thingBuilder.withChannels(channels);
            Map<String, String> properties = editProperties();
            properties.put("zones", configuration.getZoneCount().toString());
            thingBuilder.withProperties(properties);
            updateThing(thingBuilder.build());
        }
    }

    @Override
    public void dispose() {
        connector.dispose();
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        String channelID = channelUID.getId();
        if (isLinked(channelID)) {
            updateState(channelID, connector.getState().getStateForChannelID(channelID));
        }
    }

    @Override
    public void stateChanged(String channelID, State state) {
        logger.debug("Recieved state {} for channelID {}", state, channelID);
        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(channelID, state);
    }

    @Override
    public void connectionError(String errorMessage) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
    }
}
