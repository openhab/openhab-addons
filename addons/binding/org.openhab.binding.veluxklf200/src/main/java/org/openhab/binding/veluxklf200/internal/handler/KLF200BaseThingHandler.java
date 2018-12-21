/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.veluxklf200.internal.engine.KLFCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction from the main handlers to include some helper functionality such as methods for finding the bridge and
 * finding things
 *
 * @author MFK - Initial Contribution
 */
public abstract class KLF200BaseThingHandler extends BaseThingHandler {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(KLF200BaseThingHandler.class);

    /**
     * Constructor
     *
     * @param thing The thing
     */
    public KLF200BaseThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Gets a channel by channel UID.
     *
     * @param channelUID the channel UID
     * @return the channel from the channel UID or null if not found
     */
    protected Channel getChannelByChannelUID(ChannelUID channelUID) {
        if (thing.getChannel(channelUID.getId()) != null) {
            return thing.getChannel(channelUID.getId());
        }
        logger.debug("Cannot find channel for UID: {}", channelUID.getId());
        return null;
    }

    /**
     * Gets the parent bridge handler.
     *
     * @return the parent bridge handler.
     */
    protected KLF200BridgeHandler getBridgeHandler() {
        Bridge b = getBridge();
        if (null != b) {
            return (KLF200BridgeHandler) b.getHandler();
        }
        return null;
    }

    /**
     * Gets the KLFCommandProcessor from the bridge.
     *
     * @return the KLF CommandProcessor
     */
    protected KLFCommandProcessor getKLFCommandProcessor() {
        return getBridgeHandler().getKLFCommandProcessor();
    }
}
