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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.*;
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
        logger.debug("Initializing Dimmer handler ...");
        conf = getConfigAs(DimmerConfiguration.class);

        try {
            group = ((FoxtrotBridgeHandler)getBridge().getHandler()).findByName(conf.refreshGroup);

            logger.debug("Adding Dimmer handler {} into refresh group {}", this, group.getName());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unknown refresh group: "+conf.refreshGroup.toUpperCase());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Dimmer handler resources ...");
        if (group != null) {
            logger.debug("Removing Dimmer handler {} from refresh group {} ...", this, group.getName());
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
        State newState = UnDefType.UNDEF;
        try {
            BigDecimal newValue = plcClient.getNumber(conf.state);

            if (newValue != null) {
                newState = new PercentType(newValue);
            }
        } catch (PlcComSEception e) {
            logger.warn("PLCComS returned {} while getting variable '{}' value: {}: {}", e.getType(), conf.state, e.getCode(), e.getMessage());
        } catch (IOException e) {
            logger.warn("Communication with PLCComS failed while getting variable '{}' value: {}", conf.state, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Wrong received new value, error: {}", e.getMessage());
        } finally {
            updateState(CHANNEL_DIMMER, newState);
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder("DimmerHandler{");
        sb.append("'").append(conf != null ? conf.state : null).append("'");
        sb.append(", ").append(group);
        sb.append('}');
        return sb.toString();
    }
}
