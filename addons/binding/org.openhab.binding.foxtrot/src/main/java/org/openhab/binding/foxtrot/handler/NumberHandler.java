/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.foxtrot.internal.config.VariableConfiguration;
import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_NUMBER;

/**
 * FoxtrotNumberHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-02-10 23:56
 */
public class NumberHandler extends FoxtrotBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(NumberHandler.class);

    private String variableName;

    public NumberHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        super.initialize();

        VariableConfiguration config = getConfigAs(VariableConfiguration.class);
        variableName = config.var;
        try {
            foxtrotBridgeHandler.register(variableName, this, config.delta);

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Enabling variable '" + variableName + "' failed due error: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Variable handler resources ...");
        foxtrotBridgeHandler.unregister(variableName);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        if (RefreshType.REFRESH.equals(command)) {
            commandExecutor.execGet(variableName);
        } else if (OnOffType.ON.equals(command)) {
            commandExecutor.execSet(variableName, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command)) {
            commandExecutor.execSet(variableName, Boolean.FALSE);
        } else if (command instanceof DecimalType || command instanceof StringType) {
            commandExecutor.execSet(variableName, command.toFullString());
        }
    }

    @Override
    public void refresh(PlcComSReply reply) {
        if (reply.getNumber() != null) {
            updateState(CHANNEL_NUMBER, new DecimalType(reply.getNumber()));
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        return new StringBuilder("NumberHandler{'").append(variableName).append("'}").toString();
    }
}
