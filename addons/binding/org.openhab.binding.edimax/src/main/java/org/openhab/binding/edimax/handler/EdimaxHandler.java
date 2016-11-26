/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.handler;

import static org.openhab.binding.edimax.EdimaxBindingConstants.SWITCH_CHANNEL;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.edimax.configuration.EdimaxConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EdimaxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(EdimaxHandler.class);

    public EdimaxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SWITCH_CHANNEL)) {
            logger.debug("Switch channel");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Things");
        this.getConfigAs(EdimaxConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }
}
