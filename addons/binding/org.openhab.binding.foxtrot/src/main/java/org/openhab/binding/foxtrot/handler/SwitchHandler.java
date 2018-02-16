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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.PlcComSClient;
import org.openhab.binding.foxtrot.internal.RefreshGroup;
import org.openhab.binding.foxtrot.internal.RefreshableHandler;
import org.openhab.binding.foxtrot.internal.config.SwitchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * SwitchHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-02-16 23:04
 */
public class SwitchHandler extends BaseThingHandler implements RefreshableHandler {

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);

    private SwitchConfiguration conf;
    private RefreshGroup group;

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Switch handler ...");
        conf = getConfigAs(SwitchConfiguration.class);

        try {
            group = RefreshGroup.valueOf(conf.refreshGroup.toUpperCase());

            logger.debug("Adding Switch handler {} into refresh group {}", this, group.name());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Switch handler resources ...");
        if (group != null) {
            logger.debug("Removing Switch handler {} from refresh group {} ...", this, group.name());
            group.removeHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        CommandExecutor ce = CommandExecutor.get();
        if (OnOffType.ON.equals(command)) {
            ce.execCommand(conf.onVariableName, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command)) {
            ce.execCommand(conf.offVariableName, Boolean.TRUE);
        }
    }

    @Override
    public void refreshFromPlc(PlcComSClient plcClient) {
        logger.trace("Requesting value for Plc variable: {} ...", conf.stateVariableName);
        try {
            String newValue = plcClient.get(conf.stateVariableName);
            // fixme handle asserts
            // todo throws PlcXXXXException instead of IOException

            if (isBool(newValue)) {
                updateState("bool", "1".equals(newValue) ? OnOffType.ON : OnOffType.OFF);
            }
        } catch (IOException e) {
            logger.warn("Getting new value of variable: {} failed w error: {}", conf.stateVariableName, e.getMessage());
            updateState("bool", UnDefType.UNDEF);
        }
    }

    private boolean isBool(String value) {
        return "1".equals(value) || "0".equals(value);
    }
}
