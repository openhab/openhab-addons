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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.CHANNEL_AUX;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.MalformedFrameException;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAuxiliary;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAuxiliaryHandler} is responsible for sending Auxiliary (AUX) commands/messages to the bus
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * NOTICE: Support for handling messages from the bus regarding alarm control has to be implemented
 *
 * @author Giovanni Fabiani - Initial contribution
 *
 */
@NonNullByDefault
public class OpenWebNetAuxiliaryHandler extends OpenWebNetThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUX_SUPPORTED_THING_TYPES;
    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAuxiliaryHandler.class);

    public OpenWebNetAuxiliaryHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handles Auxiliary command for a channel
     *
     * @param channel the channel
     * @param command the Command
     */
    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        logger.debug("handleAuxiliaryCommand() (command={} - channel={})", command, channel);
        Where w = deviceWhere;
        if (w != null) {
            if (channel.getId().equals(CHANNEL_AUX)) {
                if (command instanceof StringType) {
                    try {
                        if (command.toString().equals(Auxiliary.WhatAuxiliary.ON.name())) {
                            send(Auxiliary.requestTurnOn(w.value()));
                        } else if (command.toString().equals(Auxiliary.WhatAuxiliary.OFF.name())) {
                            send(Auxiliary.requestTurnOff(w.value()));
                        }
                    } catch (OWNException e) {
                        logger.debug("Exception while processing command {}: {}", command, e.getMessage());
                    }
                } else {
                    logger.debug("Unsupported command {} for channel {}", command, channel);
                }
            } else {
                logger.debug("Unsupported ChannelUID {}", channel);
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        /*
         * NOTICE: It is not possible to get the state of a
         * WHO=9 command. To get state of the Alarm system use WHO=5 instead
         */

        super.requestChannelState(channel);
        Where w = deviceWhere;
        if (w != null) {
            try {
                OpenMessage msg = BaseOpenMessage.parse("*#9##");
                // initializing
                send(msg);
            } catch (MalformedFrameException | UnsupportedFrameException | OWNException e) {
                logger.debug("Exception while processing command: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        /*
         * NOTICE: It is not possible to refresh the state of a
         * WHO=9 command. To refresh the state of the Alarm system use WHO=5 instead
         */

        logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
        requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_AUX));
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereAuxiliary(wStr);
    }

    @Override
    protected String ownIdPrefix() {
        return Who.AUX.value().toString();
    }
}
