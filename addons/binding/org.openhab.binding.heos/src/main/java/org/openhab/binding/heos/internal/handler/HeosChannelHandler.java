/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heos.internal.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.handler.HeosBridgeHandler;
import org.openhab.binding.heos.handler.HeosGroupHandler;
import org.openhab.binding.heos.handler.HeosPlayerHandler;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandler} handles the base class for the different
 * channel handler which handles the command from the channels of the things
 * to the HEOS system
 *
 * @author Johannes Einig - Initial contribution
 */
public abstract class HeosChannelHandler {
    protected final Logger logger = LoggerFactory.getLogger(HeosChannelHandler.class);

    protected Object handler;
    protected HeosBridgeHandler bridge;
    protected HeosFacade api;
    protected String id;
    protected Command command;
    protected ChannelUID channelUID;

    /**
     *
     * @param bridge Requires the HeosBridgeHandler
     * @param api    The HeosFacade class
     */
    public HeosChannelHandler(HeosBridgeHandler bridge, HeosFacade api) {
        this.bridge = bridge;
        this.api = api;
    }

    /**
     * Handle a command received from a channel. Requires the class which
     * wants to handle the command to decide which subclass has to be used
     *
     * @param command    the command to handle
     * @param id         of the group or player
     * @param handler    The class which wants to handle the command
     * @param channelUID the channelUID of the handleCommand function
     */
    public void handleCommand(Command command, String id, Object handler, ChannelUID channelUID) {
        this.command = command;
        this.id = id;
        this.handler = handler;
        this.channelUID = channelUID;

        if (handler instanceof HeosPlayerHandler) {
            handleCommandPlayer();
        } else if (handler instanceof HeosGroupHandler) {
            handleCommandGroup();
        }
    }

    /**
     * Handles a command for classes without an id. Used
     * for BridgeHandler
     *
     * @param command    the command to handle
     * @param handler    The class which wants to handle the command
     * @param channelUID the channelUID of the handleCommand function
     */
    public void handleCommand(Command command, Object handler, ChannelUID channelUID) {
        this.command = command;
        this.handler = handler;
        this.channelUID = channelUID;
        if (handler instanceof HeosBridgeHandler) {
            handleCommandBridge();
        }
    }

    /**
     * Handles the command for HEOS player
     */
    protected abstract void handleCommandPlayer();

    /**
     * Handles the command for HEOS groups
     */
    protected abstract void handleCommandGroup();

    /**
     * Handles the command for the HEOS bridge
     */
    protected abstract void handleCommandBridge();
}
