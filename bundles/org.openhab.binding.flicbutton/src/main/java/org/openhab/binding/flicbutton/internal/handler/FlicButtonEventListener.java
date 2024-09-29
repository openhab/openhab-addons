/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flicbutton.internal.FlicButtonBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.ButtonConnectionChannel;
import io.flic.fliclib.javaclient.enums.ClickType;
import io.flic.fliclib.javaclient.enums.ConnectionStatus;
import io.flic.fliclib.javaclient.enums.CreateConnectionChannelError;
import io.flic.fliclib.javaclient.enums.DisconnectReason;
import io.flic.fliclib.javaclient.enums.RemovedReason;

/**
 * Each {@link FlicButtonEventListener} object listens to events of a specific Flic button and calls the
 * associated {@link FlicButtonHandler} back accordingly.
 *
 * @author Patrick Fink - Initial contribution
 *
 */
@NonNullByDefault
public class FlicButtonEventListener extends ButtonConnectionChannel.Callbacks {
    private final Logger logger = LoggerFactory.getLogger(FlicButtonEventListener.class);

    private final FlicButtonHandler thingHandler;
    private final Semaphore channelResponseSemaphore = new Semaphore(0);

    FlicButtonEventListener(FlicButtonHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    public Semaphore getChannelResponseSemaphore() {
        return channelResponseSemaphore;
    }

    @Override
    public synchronized void onCreateConnectionChannelResponse(@Nullable ButtonConnectionChannel channel,
            @Nullable CreateConnectionChannelError createConnectionChannelError,
            @Nullable ConnectionStatus connectionStatus) {
        logger.debug("Create response {}: {}, {}", channel.getBdaddr(), createConnectionChannelError, connectionStatus);
        // Handling does not differ from Status change, so redirect
        if (connectionStatus != null) {
            thingHandler.initializeStatus(connectionStatus);
            channelResponseSemaphore.release();
        }
    }

    @Override
    public void onRemoved(@Nullable ButtonConnectionChannel channel, @Nullable RemovedReason removedReason) {
        thingHandler.flicButtonRemoved();
        logger.debug("Button {} removed. ThingStatus updated to OFFLINE. Reason: {}", channel.getBdaddr(),
                removedReason);
    }

    @Override
    public void onConnectionStatusChanged(@Nullable ButtonConnectionChannel channel,
            @Nullable ConnectionStatus connectionStatus, @Nullable DisconnectReason disconnectReason) {
        logger.trace("New status for {}: {}", channel.getBdaddr(),
                connectionStatus + (connectionStatus == ConnectionStatus.Disconnected ? ", " + disconnectReason : ""));
        if (connectionStatus != null) {
            thingHandler.connectionStatusChanged(connectionStatus, disconnectReason);
        }
    }

    @Override
    public void onButtonUpOrDown(@Nullable ButtonConnectionChannel channel, @Nullable ClickType clickType,
            boolean wasQueued, int timeDiff) throws IOException {
        if (channel != null && clickType != null) {
            logger.trace("{} {}", channel.getBdaddr(), clickType.name());
            String commonTriggerEvent = FlicButtonBindingConstants.FLIC_OPENHAB_TRIGGER_EVENT_MAP.get(clickType.name());
            if (commonTriggerEvent != null) {
                thingHandler.fireTriggerEvent(commonTriggerEvent);
            }
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(@Nullable ButtonConnectionChannel channel,
            @Nullable ClickType clickType, boolean wasQueued, int timeDiff) throws IOException {
        // Handling does not differ from up/down events, so redirect
        if (channel != null && clickType != null) {
            onButtonUpOrDown(channel, clickType, wasQueued, timeDiff);
        }
    }
}
