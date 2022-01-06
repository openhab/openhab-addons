/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal.handler;

import static org.openhab.binding.dmx.internal.DmxBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openhab.binding.dmx.internal.DmxBindingConstants.ListenerType;
import org.openhab.binding.dmx.internal.DmxBridgeHandler;
import org.openhab.binding.dmx.internal.DmxThingHandler;
import org.openhab.binding.dmx.internal.ValueSet;
import org.openhab.binding.dmx.internal.action.FadeAction;
import org.openhab.binding.dmx.internal.action.ResumeAction;
import org.openhab.binding.dmx.internal.config.ChaserThingHandlerConfiguration;
import org.openhab.binding.dmx.internal.multiverse.BaseDmxChannel;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChaserThingHandler} is responsible for handling commands, which are
 * sent to the chaser.
 *
 * @author Jan N. Klug - Initial contribution
 */

public class ChaserThingHandler extends DmxThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_CHASER);

    private final Logger logger = LoggerFactory.getLogger(ChaserThingHandler.class);

    private final List<DmxChannel> channels = new ArrayList<>();
    private List<ValueSet> values = new ArrayList<>();

    private boolean resumeAfter = false;
    private OnOffType isRunning = OnOffType.OFF;

    public ChaserThingHandler(Thing dimmerThing) {
        super(dimmerThing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    if (((OnOffType) command).equals(OnOffType.ON)) {
                        Integer channelCounter = 0;
                        for (DmxChannel channel : channels) {
                            if (resumeAfter) {
                                channel.suspendAction();
                            } else {
                                channel.clearAction();
                            }
                            for (ValueSet value : values) {
                                channel.addChannelAction(new FadeAction(value.getFadeTime(),
                                        value.getValue(channelCounter), value.getHoldTime()));
                            }
                            if (resumeAfter) {
                                channel.addChannelAction(new ResumeAction());
                            }
                            channel.addListener(channelUID, this, ListenerType.ACTION);
                            channelCounter++;
                        }
                    } else {
                        for (DmxChannel channel : channels) {
                            if (resumeAfter && channel.isSuspended()) {
                                channel.setChannelAction(new ResumeAction());
                            } else {
                                channel.clearAction();
                            }
                        }
                    }
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, isRunning);
                } else {
                    logger.debug("command {} not supported in channel {}:switch", command.getClass(),
                            this.thing.getUID());
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof StringType) {
                    List<ValueSet> newValues = ValueSet.parseChaseConfig(((StringType) command).toString());
                    if (!newValues.isEmpty()) {
                        values = newValues;
                        logger.debug("updated chase config in {}", this.thing.getUID());
                    } else {
                        logger.debug("could not update chase config in {}, malformed: {}", this.thing.getUID(),
                                command);
                    }
                } else {
                    logger.debug("command {} not supported in channel {}:control", command.getClass(),
                            this.thing.getUID());
                }
                break;
            default:
                logger.debug("Channel {} not supported in thing {}", channelUID.getId(), this.thing.getUID());
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        DmxBridgeHandler bridgeHandler;
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            bridgeHandler = (DmxBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge handler available");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }
        }

        ChaserThingHandlerConfiguration configuration = getConfig().as(ChaserThingHandlerConfiguration.class);

        if (configuration.dmxid.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "DMX channel configuration missing");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        }

        try {
            List<BaseDmxChannel> configChannels = BaseDmxChannel.fromString(configuration.dmxid,
                    bridgeHandler.getUniverseId());
            logger.trace("found {} channels in {}", configChannels.size(), this.thing.getUID());
            for (BaseDmxChannel channel : configChannels) {
                channels.add(bridgeHandler.getDmxChannel(channel, this.thing));
            }
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        }
        if (!configuration.steps.isEmpty()) {
            values = ValueSet.parseChaseConfig(configuration.steps);
            if (!values.isEmpty()) {
                if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                    dmxHandlerStatus = ThingStatusDetail.NONE;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "chaser configuration malformed");
                dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Chase configuration missing");
            dmxHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
        }

        resumeAfter = configuration.resumeafter;
        logger.trace("set resumeAfter to {}", resumeAfter);
    }

    @Override
    public void dispose() {
        if (!channels.isEmpty()) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                DmxBridgeHandler bridgeHandler = (DmxBridgeHandler) bridge.getHandler();
                if (bridgeHandler != null) {
                    bridgeHandler.unregisterDmxChannels(this.thing);
                    logger.debug("removing {} channels from {}", channels.size(), this.thing.getUID());
                }
                ChannelUID switchChannelUID = new ChannelUID(this.thing.getUID(), CHANNEL_SWITCH);
                for (DmxChannel channel : channels) {
                    channel.removeListener(switchChannelUID);
                }
            }
            channels.clear();
        }
    }

    @Override
    public void updateSwitchState(ChannelUID channelUID, State state) {
        logger.trace("received {} for {}", state, channelUID);
        if (channelUID.getId().equals(CHANNEL_SWITCH) && (state instanceof OnOffType)) {
            this.isRunning = (OnOffType) state;
            updateState(channelUID, state);
        } else {
            logger.debug("unknown state received: {} in channel {} thing {}", state, channelUID, this.thing.getUID());
        }
    }
}
