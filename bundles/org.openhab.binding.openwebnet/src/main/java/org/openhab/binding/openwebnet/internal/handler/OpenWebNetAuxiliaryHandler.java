/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.What;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAuxiliary;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAuxiliaryHandler} is responsible for handling Auxiliary (AUX) commands/messages
 * for an OpenWebNet device.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 * @author Giovanni Fabiani - Auxiliary message support
 */
@NonNullByDefault
public class OpenWebNetAuxiliaryHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAuxiliaryHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUX_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = -1; // timestamp when the last request for all device refresh was sent
    // for this handler

    protected static final int ALL_DEVICES_REFRESH_INTERVAL_MSEC = 60000; // interval in msec before sending another

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
        logger.debug("handleSwitchCommand() (command={} - channel={})", command, channel);
        Where w = deviceWhere;
        if (w != null) {
            if (channel.getId().equals(CHANNEL_AUX)) {
                if (command instanceof StringType) {
                    updateStatus(ThingStatus.ONLINE);
                    try {
                        if (command.toString().equals(Auxiliary.WhatAuxiliary.ON.name())) {
                            send(Auxiliary.requestTurnOn(w.value()));
                            // TODO: Update state
                        } else if (command.toString().equals(Auxiliary.WhatAuxiliary.OFF.name())) {
                            send(Auxiliary.requestTurnOff(w.value()));
                        }
                    } catch (OWNException e) {
                        logger.warn("Exception while processing command {}: {}", command, e.getMessage());
                    }
                } else {
                    logger.warn("Unsupported Command {}", channel);
                }
            } else {
                logger.warn("Unsupported ChannelUID {}", channel);
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() thingUID={} channel={}", thing.getUID(), channel.getId());
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        super.handleMessage(msg);
        Where w = deviceWhere;
        try {
            if (w != null) {
                Response res = send(Auxiliary.requestStatus(w.value()));
                if (res != null) {
                    msg = (BaseOpenMessage) res.getResponseMessages().get(0);
                    logger.warn("OWN message:  {}", msg.toString());
                }
                What what = msg.getWhat();
                updateState(CHANNEL_AUX, new StringType(what.toString()));
            }
        } catch (OWNException e) {
            logger.warn("Exception while processing command: {}", e.getMessage());
        }
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
