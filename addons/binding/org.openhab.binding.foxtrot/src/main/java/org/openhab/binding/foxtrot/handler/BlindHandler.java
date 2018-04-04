/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.foxtrot.internal.config.BlindConfiguration;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_BLIND;

/**
 * BlindHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 16:57
 */
public class BlindHandler extends FoxtrotBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    private BlindConfiguration conf;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        super.initialize();

        logger.debug("Initializing Blind handler ...");
        conf = getConfigAs(BlindConfiguration.class);

        try {
            foxtrotBridgeHandler.register(conf.state, this, conf.delta);

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Enabling variable '" + conf.state + "' failed due error: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Blind handler resources ...");
        foxtrotBridgeHandler.unregister(conf.state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            commandExecutor.execGet(conf.state);
        } else if (UpDownType.UP.equals(command)) {
            commandExecutor.execSet(conf.up, Boolean.TRUE);
        } else if (UpDownType.DOWN.equals(command)) {
            commandExecutor.execSet(conf.down, Boolean.TRUE);
        } else if (StopMoveType.STOP.equals(command)) {
            commandExecutor.execSet(conf.stop, Boolean.TRUE);
        }
    }

    @Override
    public void refresh(PlcComSReply reply) {
        if (reply.getNumber() != null) {
            updateState(CHANNEL_BLIND, new PercentType(reply.getNumber()));
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        return new StringBuilder("BlindHandler{'").append(conf != null ? conf.state : null).append("'}").toString();
    }
}
