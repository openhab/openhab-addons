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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.foxtrot.internal.*;
import org.openhab.binding.foxtrot.internal.config.SwitchConfiguration;
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
            group = ((FoxtrotBridgeHandler)getBridge().getHandler()).findByName(conf.refreshGroup);

            logger.debug("Adding Switch handler {} into refresh group {}", this, group.getName());
            group.addHandler(this);

            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unknown refresh group: "+conf.refreshGroup.toUpperCase());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Switch handler resources ...");
        if (group != null) {
            logger.debug("Removing Switch handler {} from refresh group {} ...", this, group.getName());
            group.removeHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        CommandExecutor ce = CommandExecutor.get();
        if (OnOffType.ON.equals(command)) {
            ce.execCommand(conf.on, Boolean.TRUE);
        } else if (OnOffType.OFF.equals(command)) {
            ce.execCommand(conf.off, Boolean.TRUE);
        }
    }

    @Override
    public void refreshFromPlc(PlcComSClient plcClient) {
        State newState = UnDefType.UNDEF;
        try {
            Boolean newValue = plcClient.getBool(conf.state);

            if (newValue != null) {
                newState = newValue ? OnOffType.ON : OnOffType.OFF;
            }
            logger.trace("Refreshing {} value: {} -> {}", this, newValue, newState);
        } catch (PlcComSEception e) {
            logger.warn("PLCComS returned {} while getting variable '{}' value: {}: {}", e.getType(), conf.state, e.getCode(), e.getMessage());
        } catch (IOException e) {
            logger.warn("Communication with PLCComS failed while getting variable '{}' value: {}", conf.state, e.getMessage());
        } finally {
            updateState(CHANNEL_SWITCH, newState);
        }
    }

    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwitchHandler{");
        sb.append("'").append(conf != null ? conf.state : null).append('\'');
        sb.append(", ").append(group);
        sb.append('}');
        return sb.toString();
    }
}
