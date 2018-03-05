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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.PlcComSClient;
import org.openhab.binding.foxtrot.internal.RefreshGroup;
import org.openhab.binding.foxtrot.internal.RefreshableHandler;
import org.openhab.binding.foxtrot.internal.config.DimmerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_DIMMER;

/**
 * DimmerHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 17:39
 */
public class DimmerHandler extends BaseThingHandler implements RefreshableHandler {

    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private DimmerConfiguration conf;
    private RefreshGroup group;

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Switch handler ...");
        conf = getConfigAs(DimmerConfiguration.class);

        try {
            group = RefreshGroup.valueOf(conf.refreshGroup.toUpperCase());

            logger.debug("Adding Dimmer handler {} into refresh group {}", this, group.name());
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
        if (OnOffType.ON.equals(command) || PercentType.HUNDRED.equals(command)) {
            ce.execCommand(conf.on, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command) || PercentType.ZERO.equals(command)) {
            ce.execCommand(conf.off, Boolean.TRUE);
        } else if (IncreaseDecreaseType.INCREASE.equals(command)) {
            ce.execCommand(conf.increase, Boolean.TRUE);
        } else if (IncreaseDecreaseType.DECREASE.equals(command)) {
            ce.execCommand(conf.decrease, Boolean.TRUE);
        } else if (command instanceof PercentType) {
            ce.execCommand(conf.state, ((PercentType)command).toBigDecimal());
        }
    }

    @Override
    public void refreshFromPlc(PlcComSClient plcClient) {
        try {
            String newValue = plcClient.get(conf.state);
            // fixme handle asserts
            // todo throws PlcXXXXException instead of IOException

            if (isNumber(newValue)) {
                updateState(CHANNEL_DIMMER, new PercentType(new BigDecimal(newValue)));
            }
        } catch (IOException e) {
            logger.warn("Getting new value of variable: {} failed w error: {}", conf.state, e.getMessage());
            updateState(CHANNEL_DIMMER, UnDefType.UNDEF);
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
}
