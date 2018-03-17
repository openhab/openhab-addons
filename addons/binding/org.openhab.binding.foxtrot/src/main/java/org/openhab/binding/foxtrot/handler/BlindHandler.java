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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.*;
import org.openhab.binding.foxtrot.internal.config.BlindConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

import static org.openhab.binding.foxtrot.FoxtrotBindingConstants.CHANNEL_BLIND;

/**
 * BlindHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 16:57
 */
public class BlindHandler extends BaseThingHandler implements RefreshableHandler {

    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    private BlindConfiguration conf;
    private RefreshGroup group;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Blind handler ...");
        conf = getConfigAs(BlindConfiguration.class);

        try {
            group = ((FoxtrotBridgeHandler)getBridge().getHandler()).findByName(conf.refreshGroup);

            logger.debug("Adding Blind handler {} into refresh group {}", this, group.getName());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unknown refresh group: "+conf.refreshGroup.toUpperCase());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Blind handler resources ...");
        if (group != null) {
            logger.debug("Removing Blind handler {} from refresh group {} ...", this, group.getName());
            group.removeHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        CommandExecutor ce = CommandExecutor.get();
        if (UpDownType.UP.equals(command)) {
            ce.execCommand(conf.up, Boolean.TRUE);
        } else if (UpDownType.DOWN.equals(command)) {
            ce.execCommand(conf.down, Boolean.TRUE);
        } else if (StopMoveType.STOP.equals(command)) {
            ce.execCommand(conf.stop, Boolean.TRUE);
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
            logger.error("PLCComS returned {} while getting value for '{}': {}: {}", e.getType(), conf.state, e.getCode(), e.getMessage());
        } catch (IOException e) {
            logger.error("Communication with PLCComS failed while value for '{}': {}", conf.state, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Wrong received new value, error: {}", e.getMessage());
        } finally {
            updateState(CHANNEL_BLIND, newState);
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder("BlindHandler{");
        sb.append("'").append(conf != null ? conf.state : null);
        sb.append("', ").append(group);
        sb.append('}');
        return sb.toString();
    }
}
