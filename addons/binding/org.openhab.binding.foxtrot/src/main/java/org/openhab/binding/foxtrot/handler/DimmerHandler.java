/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.foxtrot.internal.config.DimmerConfiguration;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_DIMMER;

/**
 * DimmerHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 17:39
 */
public class DimmerHandler extends FoxtrotBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private DimmerConfiguration conf;

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        super.initialize();

        logger.debug("Initializing Dimmer handler ...");
        conf = getConfigAs(DimmerConfiguration.class);

        try {
            foxtrotBridgeHandler.register(conf.state, this, conf.delta);

            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Enabling variable '" + conf.state + "' failed due error: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Dimmer handler resources ...");
        foxtrotBridgeHandler.unregister(conf.state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        if (RefreshType.REFRESH.equals(command)) {
            commandExecutor.execGet(conf.state);
        } else if (OnOffType.ON.equals(command) || PercentType.HUNDRED.equals(command)) {
            commandExecutor.execSet(conf.on, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
            commandExecutor.execSet(conf.off, Boolean.TRUE);
        } else if (IncreaseDecreaseType.INCREASE.equals(command)) {
            commandExecutor.execSet(conf.increase, Boolean.TRUE);
        } else if (IncreaseDecreaseType.DECREASE.equals(command)) {
            commandExecutor.execSet(conf.decrease, Boolean.TRUE);
        } else if (command instanceof PercentType) {
            commandExecutor.execSet(conf.state, ((PercentType)command).toBigDecimal());
        }
    }

    @Override
    public void refresh(PlcComSReply reply) {
        if (reply.getNumber() != null) {
            updateState(CHANNEL_DIMMER, new PercentType(reply.getNumber()));
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        return new StringBuilder("DimmerHandler{'").append(conf != null ? conf.state : null).append("'}").toString();
    }
}
