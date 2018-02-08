/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.AutomationState;
import org.openhab.binding.openwebnet.internal.LightState;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetLightingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Antoine Laydier
 *
 */
public class OpenWebNetLightingHandler extends OpenWebNetZigBeeThingHandler {

    private Logger logger = LoggerFactory.getLogger(OpenWebNetLightingHandler.class);

    private boolean[] isChannelADimmer;
    private @Nullable PercentType[] lastPercentOfChannel;

    @SuppressWarnings("null")
    public static final @NonNull Set<@NonNull ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(OpenWebNetBindingConstants.THING_TYPE_LIGHTING, OpenWebNetBindingConstants.THING_TYPE_DUAL_LIGHTING)
            .collect(Collectors.toSet());

    public OpenWebNetLightingHandler(@NonNull Thing thing) {
        super(thing);
        isChannelADimmer = new boolean[2];
        isChannelADimmer[0] = false;
        isChannelADimmer[1] = false;
        lastPercentOfChannel = new @Nullable PercentType[2];
        lastPercentOfChannel[0] = null;
        lastPercentOfChannel[1] = null;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        @Nullable
        Bridge bridge = getBridge();
        @Nullable
        OpenWebNetBridgeHandler bridgeHandler = (bridge == null) ? null : (OpenWebNetBridgeHandler) bridge.getHandler();
        int channelNumber = 1;
        switch (channelUID.getId()) {
            case OpenWebNetBindingConstants.CHANNEL_DIMMER01:
            case OpenWebNetBindingConstants.CHANNEL_SWITCH01:
                channelNumber = 0;
                // no break on purpose
            case OpenWebNetBindingConstants.CHANNEL_DIMMER02:
            case OpenWebNetBindingConstants.CHANNEL_SWITCH02:
                channelNumber++;
                final int currentChannel = channelNumber;
                if (LightState.toOpenWebNet(command) != LightState.ERROR) {
                    if (bridgeHandler != null) {
                        if (command instanceof PercentType) {
                            lastPercentOfChannel[currentChannel - 1] = (PercentType) command;
                        }
                        if (OnOffType.ON.equals(command)) {
                            @Nullable
                            PercentType value = lastPercentOfChannel[currentChannel - 1];
                            if (value != null) {
                                logger.debug("{} -> Set last known value of channel {} : {}", this, currentChannel,
                                        value);
                                updateState(thing.getChannels().get(currentChannel - 1).getUID(), value);
                            }
                        }
                        bridgeHandler.setLight(getWhere(currentChannel), command, false);
                    }
                } else {
                    logger.warn("{} -> Invalid command {} received on channel {}", this, command, channelUID.getId());
                }
                break;
            default:
                logger.warn("{} -> Command {} received for the unknown channel {}", this, command, channelUID.getId());
        }
    }

    @Override
    public void onStatusChange(@NonNull LightState state) {
        @Nullable
        Bridge bridge = getBridge();
        @Nullable
        OpenWebNetBridgeHandler bridgeHandler = (bridge == null) ? null : (OpenWebNetBridgeHandler) bridge.getHandler();

        switch (state.channel) {
            case "01":
                if (isChannelADimmer[0] && OnOffType.ON.equals(state.state)) {
                    @Nullable
                    PercentType value = lastPercentOfChannel[0];
                    if (value != null) {
                        logger.info("{} -> Set last known value of channel 1 : {}", this, value);
                        updateState(thing.getChannels().get(0).getUID(), value);
                    } else {
                        // as the last state is known, request to get it
                        logger.info("{} -> Request Reflesh on channel 1", this);
                        if (bridgeHandler != null) {
                            bridgeHandler.getLight(getWhere(1), false);
                        }
                    }
                } else {
                    logger.info("{} -> Set channel 1 to {}", this, state.state);
                    updateState(thing.getChannels().get(0).getUID(), state.state);
                    if (state.state instanceof PercentType) {
                        lastPercentOfChannel[0] = (PercentType) state.state;
                    }
                }
                break;
            case "02":
                if (isChannelADimmer[1] && OnOffType.ON.equals(state.state)) {
                    @Nullable
                    PercentType value = lastPercentOfChannel[1];
                    if (value != null) {
                        logger.info("{} -> Set last known value of channel 2 : {}", this, value);
                        updateState(thing.getChannels().get(0).getUID(), value);
                    } else {
                        // as the last state is known, request to get it
                        logger.info("{} -> Request Reflesh on channel 2", this);
                        if (bridgeHandler != null) {
                            bridgeHandler.getLight(getWhere(2), false);
                        }
                    }
                } else {
                    logger.info("{} -> Set channel 2 to {}", this, state.state);
                    updateState(thing.getChannels().get(1).getUID(), state.state);
                    if (state.state instanceof PercentType) {
                        lastPercentOfChannel[1] = (PercentType) state.state;
                    }
                }
                break;
            default:
                logger.warn("{} -> Unknown channel {} receives change {}", this, state.channel, state.state);
        }

    }

    @Override
    public void initialize() {
        // Get channel(s) type
        String prop;
        int channelType;
        @NonNull
        ThingBuilder thingBuilder = editThing();
        @NonNull
        List<Channel> list = thing.getChannels();
        @NonNull
        List<@NonNull Channel> newList = new ArrayList<@NonNull Channel>();
        boolean modified = false;

        for (int port = 1; port <= getNumberOfPorts(); port++) {

            prop = editProperties()
                    .get(port == 1 ? OpenWebNetBindingConstants.CHANNEL1 : OpenWebNetBindingConstants.CHANNEL2);
            try {
                channelType = Integer.valueOf(prop);
                boolean isDimmer = OpenWebNetChannelType.getType(channelType).isDimmer();
                for (int i = 0; i < list.size(); i++) {
                    Channel channel = list.get(i);
                    switch (channel.getUID().getId()) {
                        case OpenWebNetBindingConstants.CHANNEL_DIMMER01:
                            if (port == 1) {
                                isChannelADimmer[0] = isDimmer;
                                if (isDimmer) {
                                    // no modification, actual Channel is added in list in case the other channel is
                                    // modified
                                    newList.add(channel);
                                    logger.debug("{} -> Channel {} of type DIMMER kept", this, port);
                                } else {
                                    // do nothing --> Remove the channel
                                    modified = true;
                                    logger.debug("{} -> Channel {} of type SWITCH removed", this, port);
                                }
                            } else {
                                continue;
                            }
                            break;
                        case OpenWebNetBindingConstants.CHANNEL_SWITCH01:
                            if (port == 1) {
                                isChannelADimmer[0] = isDimmer;
                                if (!isDimmer) {
                                    // no modification, actual Channel is added in list in case the other channel is
                                    // modified
                                    newList.add(channel);
                                    logger.debug("{} -> Channel {} of type SWITCH kept", this, port);
                                } else {
                                    // do nothing --> Remove the channel
                                    modified = true;
                                    logger.debug("{} -> Channel {} of type DIMMER removed", this, port);
                                }
                            } else {
                                continue;
                            }
                            break;
                        case OpenWebNetBindingConstants.CHANNEL_DIMMER02:
                            if (port == 2) {
                                isChannelADimmer[1] = isDimmer;
                                if (isDimmer) {
                                    // no modification, actual Channel is added in list in case the other channel is
                                    // modified
                                    newList.add(channel);
                                    logger.debug("{} -> Channel {} of type DIMMER kept", this, port);
                                } else {
                                    // do nothing --> Remove the channel
                                    modified = true;
                                    logger.debug("{} -> Channel {} of type SWITCH removed", this, port);
                                }
                            } else {
                                continue;
                            }
                            break;
                        case OpenWebNetBindingConstants.CHANNEL_SWITCH02:
                            if (port == 2) {
                                isChannelADimmer[1] = isDimmer;
                                if (!isDimmer) {
                                    // no modification, actual Channel is added in list in case the other channel is
                                    // modified
                                    newList.add(channel);
                                    logger.debug("{} -> Channel {} of type SWITCH kept", this, port);
                                } else {
                                    // do nothing --> Remove the channel
                                    modified = true;
                                    logger.debug("{} -> Channel {} of type DIMMER removed", this, port);
                                }
                            } else {
                                continue;
                            }
                            break;
                    }
                }

            } catch (NumberFormatException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Channel " + port + " type invalid (not a number: " + prop + ")");
                return;
            }
        }

        if (modified) {
            thingBuilder.withChannels(newList);
            updateThing(thingBuilder.build());
        }

        super.initialize();
    }

    @Override
    public void onStatusChange(@NonNull AutomationState state) {
        logger.warn("{} -> should never be called with an Automation state ({})", this, state.toString());
    }

    @Override
    public String toString() {
        return "Lighting Handler for MAC=" + getMacAddress();
    }
}
