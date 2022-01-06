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
package org.openhab.binding.cbus.handler;

import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link CBusTemperatureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusTemperatureHandler extends CBusGroupHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusTemperatureHandler.class);

    public CBusTemperatureHandler(Thing thing) {
        super(thing, CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only thing - no commands to handle
        if (command instanceof RefreshType) {
            try {
                Group group = this.group;
                if (group != null) {
                    int level = group.getLevel();
                    logger.debug("handle RefreshType Command for Chanell {} Group {} level {}", channelUID, groupId,
                            level);
                    if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_TEMP)) {
                        updateState(channelUID, new QuantityType<>(level, CELSIUS));
                    }
                }
            } catch (CGateException e) {
                logger.debug("Failed to getLevel for group {}", groupId, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication Error");
            }
        }
    }

    public void updateGroup(int updateApplicationId, int updateGroupId, String value) {
        if (updateGroupId == groupId && updateApplicationId == applicationId) {
            Thing thing = getThing();
            Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_TEMP);

            if (channel != null) {
                ChannelUID channelUID = channel.getUID();
                updateState(channelUID, new QuantityType<>(Double.parseDouble(value), CELSIUS));
                logger.trace("Updating CBus Temperature Group {} with value {}", thing.getUID(), value);
            } else {
                logger.trace("Failed to Update CBus Temperature Group {} with value {}: No Channel", thing.getUID(),
                        value);
            }
        }
    }
}
