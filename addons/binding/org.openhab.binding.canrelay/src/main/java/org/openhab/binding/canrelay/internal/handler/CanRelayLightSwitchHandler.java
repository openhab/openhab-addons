/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.handler;

import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CanRelayLightSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanRelayLightSwitchHandler extends BaseThingHandler {

    private static final Logger logger = LoggerFactory.getLogger(CanRelayLightSwitchHandler.class);
    // timeout in ms to switch the lights to proper status after startup
    private static final int STARTUP_TIMEOUT = 100;

    // timeout in ms after which handled commands would be reverted if CanRelay reports error
    private static final int REVERT_FAILED_COMMAND_TIMEOUT = 500;
    private final Integer nodeID;
    private final OnOffType initialState;

    public CanRelayLightSwitchHandler(Thing thing) {
        super(thing);
        Object nodeID = getThing().getConfiguration().get(CONFIG_NODEID);
        this.nodeID = Integer.parseInt(nodeID.toString());
        this.initialState = ((Boolean) getThing().getConfiguration().get(CONFIG_INITIALSTATE)) ? OnOffType.ON
                : OnOffType.OFF;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        // did not find any better way, when trying to post command or update state during initialize, it is ignored, so
        // scheduling a background thread to do it later
        scheduler.schedule(() -> {
            postCommand(CHANNEL_LIGHT_SWITCH, initialState);
        }, STARTUP_TIMEOUT, TimeUnit.MILLISECONDS);
    };

    // getBridge().getHandler() call below, but shall be safe according to API javadoc for this subordinate thing
    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String nodeString = nodeAsString(nodeID);
        logger.debug("Received command {} on channel {} for light switch {}", command, channelUID, nodeString);
        if (CHANNEL_LIGHT_SWITCH.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                if (getBridge() != null) {
                    boolean success = ((CanRelayBridgeHandler) getBridge().getHandler())
                            .handleLightSwitchCommand(nodeID, (OnOffType) command);
                    if (!success) {
                        logger.warn("Command not processed properly. Schedulling a revert command after {} ms",
                                REVERT_FAILED_COMMAND_TIMEOUT);
                        // let the current thread finish its job and schedule a background task to simply post reverting
                        // command after a timeout (the user would then notice the respective switch got to its previous
                        // position)
                        scheduler.schedule(() -> {
                            postCommand(channelUID, command == ON ? OFF : ON);
                        }, REVERT_FAILED_COMMAND_TIMEOUT, TimeUnit.MILLISECONDS);
                    }
                } else {
                    logger.warn(
                            "Probably misconfigured CanRelay Light Switch (node ID {}) without a bridge! Ignoring command {}",
                            nodeString, command);
                }
            }
        }
    }

    /**
     * Declaring public so that the bridge handler can post commands to this
     */
    @Override
    public void postCommand(String channelID, Command command) {
        super.postCommand(channelID, command);
    }
}
