/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.foxtrot.internal.config.SwitchConfiguration;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_SWITCH;

/**
 * SwitchHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-02-16 23:04
 */
public class SwitchHandler extends FoxtrotBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);

    private SwitchConfiguration conf;

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        super.initialize();

        logger.debug("Initializing Switch handler ...");
        conf = getConfigAs(SwitchConfiguration.class);
        try {
            foxtrotBridgeHandler.register(conf.state, this);

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Enabling variable '" + conf.state + "' failed due error: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Switch handler resources ...");
        foxtrotBridgeHandler.unregister(conf.state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        if (RefreshType.REFRESH.equals(command)) {
            commandExecutor.execGet(conf.state);
        } else if (OnOffType.ON.equals(command)) {
            commandExecutor.execSet(conf.on, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command)) {
            commandExecutor.execSet(conf.off, Boolean.TRUE);
        }
    }

    @Override
    public void refresh(PlcComSReply reply) {
        if (reply.getBool() != null) {
            updateState(CHANNEL_SWITCH, reply.getBool() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        return new StringBuilder("SwitchHandler{'").append(conf != null ? conf.state : null).append("'}").toString();
    }
}
