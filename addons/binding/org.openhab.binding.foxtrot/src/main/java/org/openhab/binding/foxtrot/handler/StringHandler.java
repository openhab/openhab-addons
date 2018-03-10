/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.*;
import org.openhab.binding.foxtrot.internal.config.VariableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_STRING;

/**
 * StringHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-09 23:32
 */
public class StringHandler extends BaseThingHandler implements RefreshableHandler {

    private final Logger logger = LoggerFactory.getLogger(StringHandler.class);

    private String variableName;
    private RefreshGroup group;

    public StringHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        VariableConfiguration config = getConfigAs(VariableConfiguration.class);

        try {
            variableName = config.var;
            group = RefreshGroup.valueOf(config.refreshGroup.toUpperCase());

            logger.debug("Adding String handler {} into refresh group {}", this, group.name());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            // todo error description
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unknown refresh group: "+config.refreshGroup.toUpperCase());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing String handler resources ...");
        if (group != null) {
            logger.debug("Removing String handler {} from refresh group {} ...", this, group.name());
            group.removeHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        CommandExecutor ce = CommandExecutor.get();
        if (command instanceof StringType) {
            ce.execCommand(variableName, command.toFullString());
        }
    }

    @Override
    public void refreshFromPlc(PlcComSClient plcClient) {
        State newState = UnDefType.UNDEF;
        try {
            newState = new StringType(plcClient.get(variableName));
        } catch (PlcComSEception e) {
            logger.warn("PLCComS returned {} while getting variable '{}' value: {}: {}", e.getType(), variableName, e.getCode(), e.getMessage());
        } catch (IOException e) {
            logger.warn("Communication with PLCComS failed while getting variable '{}' value: {}", variableName, e.getMessage());
        } finally {
            updateState(CHANNEL_STRING, newState);
        }
    }
}

