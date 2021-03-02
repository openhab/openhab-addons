/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.*;

/**
 *
 * @author Patrick Fink
 *
 */
public class FlicButtonEventListener extends ButtonConnectionChannel.Callbacks {
    private final Logger logger = LoggerFactory.getLogger(FlicButtonEventListener.class);

    private final FlicButtonHandler thingHandler;

    FlicButtonEventListener(@NonNull FlicButtonHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public synchronized void onCreateConnectionChannelResponse(ButtonConnectionChannel channel,
            CreateConnectionChannelError createConnectionChannelError, ConnectionStatus connectionStatus) {
        logger.debug("Create response {}: {}, {}", channel.getBdaddr(), createConnectionChannelError, connectionStatus);
        // Handling does not differ from Status change, so redirect
        thingHandler.initializeStatus(connectionStatus);
        notify();
    }

    @Override
    public void onRemoved(ButtonConnectionChannel channel, RemovedReason removedReason) {
        thingHandler.flicButtonRemoved();
        logger.debug("Button {} removed. ThingStatus updated to OFFLINE. Reason: {}", channel.getBdaddr(),
                removedReason);
    }

    @Override
    public void onConnectionStatusChanged(ButtonConnectionChannel channel, ConnectionStatus connectionStatus,
            DisconnectReason disconnectReason) {
        logger.debug("New status for {}: {}", channel.getBdaddr(),
                connectionStatus + (connectionStatus == ConnectionStatus.Disconnected ? ", " + disconnectReason : ""));

        thingHandler.connectionStatusChanged(connectionStatus, disconnectReason);
    }

    @Override
    public void onButtonUpOrDown(ButtonConnectionChannel channel, ClickType clickType, boolean wasQueued, int timeDiff)
            throws IOException {

        logger.debug("{} {}", channel.getBdaddr(), clickType.name());

        String commonTriggerEvent = FlicButtonUtils.flicOpenhabTriggerEventMap.get(clickType.name());

        if (commonTriggerEvent != null) {
            thingHandler.fireTriggerEvent(commonTriggerEvent);
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(ButtonConnectionChannel channel, ClickType clickType,
            boolean wasQueued, int timeDiff) throws IOException {
        // Handling does not differ from up/down events, so redirect
        onButtonUpOrDown(channel, clickType, wasQueued, timeDiff);
    }
}
