/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2MultiChannelThingHandler} is a hollow shell for holding manually linked Items.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@NonNullByDefault
public class Ihc2MultiChannelThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Ihc2MultiChannelThingHandler.class);

    public Ihc2MultiChannelThingHandler(Thing thing) {
        super(thing);
        logger.debug("Ihc2MultiChannelThingHandler() for: {}", thing.getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

}
