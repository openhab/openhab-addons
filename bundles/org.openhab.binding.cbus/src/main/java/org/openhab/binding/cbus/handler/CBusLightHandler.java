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
package org.openhab.binding.cbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;

/**
 * The {@link CBusLightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusLightHandler extends CBusGroupHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusLightHandler.class);

    public CBusLightHandler(Thing thing) {
        super(thing, CBusBindingConstants.CBUS_APPLICATION_LIGHTING);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Group group = this.group;
        if (group == null) {
            return;
        }
        if (command instanceof RefreshType) {
            try {
                int level = group.getLevel();
                logger.debug("handle RefreshType Command for Chanell {} Group {} level {}", channelUID, groupId, level);
                if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_STATE)) {
                    updateState(channelUID, OnOffType.from(level > 0));
                } else if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_LEVEL)) {
                    updateState(channelUID, new PercentType((int) (level * 100 / 255.0)));
                }
            } catch (CGateException e) {
                logger.debug("Failed to getLevel for group {}", groupId, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication Error");
            }
        } else {
            if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_STATE)) {
                logger.debug("Channel State command for {}: {}", channelUID, command);
                if (command instanceof OnOffType) {
                    try {
                        if (command == OnOffType.ON) {
                            group.on();
                        } else if (command == OnOffType.OFF) {
                            group.off();
                        }
                    } catch (CGateException e) {
                        logger.debug("Failed to send command {} to {}", command, group, e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication Error");
                    }
                }
            } else if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_LEVEL)) {
                logger.debug("Channel Level command for {}: {}", channelUID, command);
                try {
                    if (command instanceof OnOffType) {
                        if (command == OnOffType.ON) {
                            group.on();
                        } else if (command == OnOffType.OFF) {
                            group.off();
                        }
                    } else if (command instanceof PercentType percentCommand) {
                        group.ramp((int) Math.round(percentCommand.doubleValue() / 100 * 255), 0);
                    } else if (command instanceof IncreaseDecreaseType) {
                        int level = group.getLevel();
                        if (command == IncreaseDecreaseType.DECREASE) {
                            level = Math.max(level - 1, 0);
                        } else if (command == IncreaseDecreaseType.INCREASE) {
                            level = Math.min(level + 1, 255);
                        }
                        group.ramp(level, 0);
                        logger.debug("Change group level to {}", level);
                    }
                } catch (CGateException e) {
                    logger.debug("Failed to send command {} to {}", command, group, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication Error");
                }
            }
        }
    }

    @Override
    public void updateGroup(int updateApplicationId, int updateGroupId, String value) {
        if (updateGroupId == groupId && updateApplicationId == applicationId) {
            Thing thing = getThing();
            Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_STATE);
            Channel channelLevel = thing.getChannel(CBusBindingConstants.CHANNEL_LEVEL);
            if (channel != null && channelLevel != null) {
                ChannelUID channelUID = channel.getUID();
                ChannelUID channelLevelUID = channelLevel.getUID();
                logger.debug("channel UID {} level UID {}", channelUID, channelLevelUID);
                if ("on".equalsIgnoreCase(value) || "255".equalsIgnoreCase(value)) {
                    updateState(channelUID, OnOffType.ON);
                    updateState(channelLevelUID, new PercentType(100));
                } else if ("off".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
                    updateState(channelUID, OnOffType.OFF);
                    updateState(channelLevelUID, new PercentType(0));
                } else {
                    try {
                        int v = Integer.parseInt(value);
                        updateState(channelUID, OnOffType.from(v > 0));
                        updateState(channelLevelUID, new PercentType((int) (v * 100 / 255.0)));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid value presented to channel {}. Received {}, expected On/Off", channelUID,
                                value);
                    }
                }
                logger.debug("Updating CBus Lighting Group {} with value {}", thing.getUID(), value);
            } else {
                logger.debug("Failed to Update CBus Lighting Group {} with value {}: No Channel", thing.getUID(),
                        value);
            }
        }
    }
}
