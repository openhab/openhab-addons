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
import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.PlcComSClient;
import org.openhab.binding.foxtrot.internal.RefreshGroup;
import org.openhab.binding.foxtrot.internal.RefreshableHandler;
import org.openhab.binding.foxtrot.internal.config.VariableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FoxtrotNumberHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-02-10 23:56
 */
public class VariableHandler extends BaseThingHandler implements RefreshableHandler {

    private final Logger logger = LoggerFactory.getLogger(VariableHandler.class);

    private String variableName;
    private RefreshGroup group;

    public VariableHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        //logger.debug("Initializing Variable handler ...");
        VariableConfiguration config = getConfigAs(VariableConfiguration.class);

        try {
            variableName = config.variableName;
            group = RefreshGroup.valueOf(config.refreshGroup.toUpperCase());

            logger.debug("Adding Variable handler {} into refresh group {}", this, group.name());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            // todo error description
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Variable handler resources ...");
        if (group != null) {
            logger.debug("Removing Variable handler {} from refresh group {} ...", this, group.name());
            group.removeHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        CommandExecutor ce = CommandExecutor.get();
        if (OnOffType.ON.equals(command)) {
            ce.execCommand(variableName, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command)) {
            ce.execCommand(variableName, Boolean.FALSE);
        } else if (command instanceof DecimalType || command instanceof StringType) {
            ce.execCommand(variableName, command.toFullString());
        }
    }

    @Override
    public void refreshFromPlc(PlcComSClient plcClient) {
        //logger.trace("Requesting value for Plc variable: {} ...", variableName);
        try {
            String newValue = plcClient.get(variableName);
            // fixme handle asserts
            // todo throws PlcXXXXException instead of IOException

            // Updating channels
            updateState("number", isNumber(newValue) ? new DecimalType(newValue) : UnDefType.UNDEF);

            updateState("string", new StringType(newValue));

            if (isBool(newValue)) {
                updateState("bool", "1".equals(newValue) ? OnOffType.ON : OnOffType.OFF);
            }
        } catch (IOException e) {
            logger.warn("Getting new value of variable: {} failed w error: {}", variableName, e.getMessage());
            updateState("number", UnDefType.UNDEF);
            updateState("string", UnDefType.UNDEF);
            updateState("bool", UnDefType.UNDEF);
        }
    }

    private boolean isNumber(String value) {
        if (value == null) {
            return false;
        }

        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBool(String value) {
        return "1".equals(value) || "0".equals(value);
    }
}
