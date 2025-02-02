/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lirc.internal.handler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.lirc.internal.LIRCMessageListener;
import org.openhab.binding.lirc.internal.config.LIRCBridgeConfiguration;
import org.openhab.binding.lirc.internal.connector.LIRCConnector;
import org.openhab.binding.lirc.internal.connector.LIRCEventListener;
import org.openhab.binding.lirc.internal.messages.LIRCButtonEvent;
import org.openhab.binding.lirc.internal.messages.LIRCResponse;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LIRCBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(LIRCBridgeHandler.class);

    private LIRCBridgeConfiguration configuration;
    private ScheduledFuture<?> connectorTask;
    private LIRCConnector connector;
    private EventListener eventListener = new EventListener();
    private Set<LIRCMessageListener> deviceStatusListeners = new CopyOnWriteArraySet<>();

    public LIRCBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge commands not supported.");
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the LIRC Bridge handler");
        configuration = getConfigAs(LIRCBridgeConfiguration.class);
        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    logger.debug("Checking LIRC connection, thing status = {}", thing.getStatus());
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        connect();
                    }
                }
            }, 0, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge handler.");
        if (connectorTask != null && !connectorTask.isCancelled()) {
            logger.debug("Cancelling task.");
            connectorTask.cancel(true);
            connectorTask = null;
        }
        if (connector != null) {
            logger.debug("Stopping connector");
            connector.removeEventListener(eventListener);
            connector.disconnect();
        }
        for (LIRCMessageListener deviceStatusListener : deviceStatusListeners) {
            unregisterMessageListener(deviceStatusListener);
        }
        super.dispose();
        logger.debug("Bridge handler disposed.");
    }

    private void connect() {
        logger.debug("Connecting to LIRC");

        try {
            if (connector != null) {
                connector.disconnect();
            }
            if (configuration.getHost() != null && connector == null) {
                connector = new LIRCConnector();
            }
            if (connector != null) {
                connector.connect(configuration);
                connector.addEventListener(eventListener);
                updateStatus(ThingStatus.ONLINE);
                startDeviceDiscovery();
            }
        } catch (UnknownHostException e) {
            logger.error("Connection to LIRC failed: unknown host");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown Host");
        } catch (IOException e) {
            logger.error("Connection to LIRC failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Initiates discovery of remotes
     */
    public void startDeviceDiscovery() {
        if (connector != null) {
            connector.startRemoteDiscovery();
        }
    }

    /**
     * Registers a message listener
     *
     * @param listener message listener to add
     * @return true if listener as added successfully; false otherwise
     */
    public boolean registerMessageListener(LIRCMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener parameter may not be null.");
        }
        return deviceStatusListeners.add(listener);
    }

    /**
     * Unregisters a message listener
     *
     * @param listener message listener to remove
     * @return true if listener as removed successfully; false otherwise
     */
    public boolean unregisterMessageListener(LIRCMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener parameter may not be null.");
        }
        return deviceStatusListeners.remove(listener);
    }

    /**
     * Transmits the button press for the specified remote.
     *
     * @param remote Name of the remote
     * @param button Button to press
     */
    public void transmit(String remote, String button) {
        connector.transmit(remote, button);
    }

    private class EventListener implements LIRCEventListener {

        @Override
        public void messageReceived(LIRCResponse response) {
            for (LIRCMessageListener deviceStatusListener : deviceStatusListeners) {
                try {
                    deviceStatusListener.onMessageReceived(getThing().getUID(), response);
                } catch (Exception e) {
                    logger.error("An exception occurred while calling the DeviceStatusListener", e);
                }
            }
        }

        @Override
        public void buttonPressed(LIRCButtonEvent buttonEvent) {
            for (LIRCMessageListener deviceStatusListener : deviceStatusListeners) {
                try {
                    deviceStatusListener.onButtonPressed(getThing().getUID(), buttonEvent);
                } catch (Exception e) {
                    logger.error("An exception occurred while calling the DeviceStatusListener", e);
                }
            }
        }

        @Override
        public void errorOccurred(String error) {
            logger.error("Error occurred: {}", error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
        }
    }
}
