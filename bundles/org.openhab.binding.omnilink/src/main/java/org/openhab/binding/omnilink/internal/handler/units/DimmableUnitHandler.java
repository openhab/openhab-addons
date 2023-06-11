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
package org.openhab.binding.omnilink.internal.handler.units;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.CHANNEL_UNIT_LEVEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.UnitHandler;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;

/**
 * The {@link DimmableUnitHandler} defines some methods that are used to
 * interface with an OmniLink Dimmable Unit. This by extension also defines the
 * Dimmable Unit things that openHAB will be able to pick up and interface with.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class DimmableUnitHandler extends UnitHandler {
    private final Logger logger = LoggerFactory.getLogger(DimmableUnitHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public DimmableUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_UNIT_LEVEL:
                handleUnitLevel(channelUID, command);
                break;
            default:
                logger.debug("Unknown channel for Dimmable Unit thing: {}", channelUID);
                super.handleCommand(channelUID, command);
        }
    }

    private void handleUnitLevel(ChannelUID channelUID, Command command) {
        logger.debug("handleUnitLevel called for channel: {}, command: {}", channelUID, command);
        if (command instanceof PercentType) {
            handlePercent(channelUID, (PercentType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            handleIncreaseDecrease(channelUID, (IncreaseDecreaseType) command);
        } else {
            // Only handle percent or increase/decrease.
            super.handleCommand(channelUID, command);
        }
    }

    private void handlePercent(ChannelUID channelUID, PercentType command) {
        logger.debug("handlePercent called for channel: {}, command: {}", channelUID, command);
        int lightLevel = command.intValue();

        if (lightLevel == 0) {
            super.handleOnOff(channelUID, OnOffType.OFF);
        } else if (lightLevel == 100) {
            super.handleOnOff(channelUID, OnOffType.ON);
        } else {
            sendOmnilinkCommand(CommandMessage.CMD_UNIT_PERCENT, lightLevel, thingID);
        }
    }

    private void handleIncreaseDecrease(ChannelUID channelUID, IncreaseDecreaseType command) {
        logger.debug("handleIncreaseDecrease called for channel: {}, command: {}", channelUID, command);
        sendOmnilinkCommand(IncreaseDecreaseType.INCREASE.equals(command) ? CommandMessage.CMD_UNIT_UPB_BRIGHTEN_STEP_1
                : CommandMessage.CMD_UNIT_UPB_DIM_STEP_1, 0, thingID);
    }
}
