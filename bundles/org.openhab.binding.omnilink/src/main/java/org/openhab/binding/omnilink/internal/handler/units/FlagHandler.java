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

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.UnitHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedUnitStatus;

/**
 * The {@link FlagHandler} defines some methods that are used to
 * interface with an OmniLink Flag. This by extension also defines the
 * Flag thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class FlagHandler extends UnitHandler {
    private final Logger logger = LoggerFactory.getLogger(FlagHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public FlagHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_FLAG_VALUE:
                if (command instanceof DecimalType) {
                    sendOmnilinkCommand(CommandMessage.CMD_UNIT_SET_COUNTER, ((DecimalType) command).intValue(),
                            thingID);
                } else {
                    logger.debug("Invalid command: {}, must be DecimalType", command);
                }
                break;
            case CHANNEL_FLAG_SWITCH:
                if (command instanceof OnOffType) {
                    handleOnOff(channelUID, (OnOffType) command);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            default:
                logger.debug("Unknown channel for Flag thing: {}", channelUID);
                super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void updateChannels(ExtendedUnitStatus status) {
        logger.debug("updateChannels called for Flag status: {}", status);
        updateState(CHANNEL_FLAG_VALUE, DecimalType.valueOf(Integer.toString(status.getStatus())));
        updateState(CHANNEL_FLAG_SWITCH, OnOffType.from(status.getStatus() == 1));
    }
}
