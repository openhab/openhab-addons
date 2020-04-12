/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;

/**
 * The {@link CBusTriggerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusTriggerHandler extends CBusGroupHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusTriggerHandler.class);

    public CBusTriggerHandler(Thing thing) {
        super(thing, CBusBindingConstants.CBUS_APPLICATION_TRIGGER);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Group group = this.group;
        if (group == null) {
            return;
        }
        if (command instanceof RefreshType) {
            /*
             * Cgate cant provide the current value for a trigger group
             */
            logger.debug("Refresh for Trigger group not implemented");
        } else {
            if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_VALUE)) {
                logger.debug("Channel Value command for {}: {}", channelUID, command);
                try {
                    if (command instanceof DecimalType) {
                        group.TriggerEvent(((DecimalType) command).intValue());
                    }
                } catch (CGateException e) {
                    logger.debug("Failed to send trigger command {} to {}", command, group, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication Error");
                }
            }
        }
    }

    public void updateGroup(int updateApplicationId, int updateGroupId, String value) {
        if (updateGroupId == groupId && updateApplicationId == applicationId) {
            Thing thing = getThing();
            Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_VALUE);
            if (channel != null) {
                ChannelUID channelUID = channel.getUID();
                DecimalType val = new DecimalType(value);
                updateState(channelUID, val);
                logger.trace("Updating CBus Trigger Group {} with value {}", thing.getUID(), value);
            }
        }
    }
}
