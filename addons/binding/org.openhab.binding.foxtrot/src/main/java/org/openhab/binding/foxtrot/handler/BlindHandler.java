/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.config.BlindConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BlindHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-03-04 16:57
 */
public class BlindHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BlindHandler.class);

    private BlindConfiguration conf;

    public BlindHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        logger.debug("Initializing Blind handler ...");
        conf = getConfigAs(BlindConfiguration.class);

        try {
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Blind handler resources ...");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command: {} for channel: {}", command, channelUID);

        CommandExecutor ce = CommandExecutor.get();
        if (UpDownType.UP.equals(command)) {
            ce.execCommand(conf.up, Boolean.TRUE);
        } else if (UpDownType.DOWN.equals(command)) {
            ce.execCommand(conf.down, Boolean.TRUE);
        } else if (StopMoveType.STOP.equals(command)) {
            ce.execCommand(conf.stop, Boolean.TRUE);
        }
    }

}
