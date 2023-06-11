/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.handler.units.dimmable;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.CHANNEL_UPB_STATUS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.handler.units.DimmableUnitHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;

/**
 * The {@link UpbUnitHandler} defines some methods that are used to
 * interface with an OmniLink UPB Unit. This by extension also defines the
 * UPB Unit thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class UpbUnitHandler extends DimmableUnitHandler {
    private final Logger logger = LoggerFactory.getLogger(UpbUnitHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public UpbUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            updateState(CHANNEL_UPB_STATUS, UnDefType.UNDEF);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_UPB_STATUS:
                if (command instanceof StringType) {
                    sendOmnilinkCommand(CommandMessage.CMD_UNIT_UPB_REQ_STATUS, 0, thingID);
                    updateState(CHANNEL_UPB_STATUS, UnDefType.UNDEF);
                } else {
                    logger.debug("Invalid command: {}, must be StringType", command);
                }
                break;
            default:
                logger.debug("Unknown channel for UPB Unit thing: {}", channelUID);
                super.handleCommand(channelUID, command);
        }
    }
}
