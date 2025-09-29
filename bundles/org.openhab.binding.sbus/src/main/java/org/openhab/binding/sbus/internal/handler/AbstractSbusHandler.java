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
package org.openhab.binding.sbus.internal.handler;

import static org.openhab.binding.sbus.BindingConstants.BINDING_ID;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sbus.internal.SbusService;
import org.openhab.binding.sbus.internal.config.SbusDeviceConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.SbusResponse;
import ro.ciprianpascu.sbus.net.SbusMessageListener;

/**
 * The {@link AbstractSbusHandler} is the base class for all Sbus device handlers.
 * It provides common functionality for device initialization, channel management, and polling.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSbusHandler extends BaseThingHandler implements SbusMessageListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected @Nullable SbusService sbusAdapter;
    protected @Nullable ScheduledFuture<?> pollingJob;

    public AbstractSbusHandler(Thing thing) {
        super(thing);
    }

    @Override
    public final void initialize() {
        logger.debug("Initializing Sbus handler for thing {}", getThing().getUID());

        initializeChannels();

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.device.no-bridge");
            return;
        }

        SbusBridgeHandler bridgeHandler = (SbusBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null || bridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        sbusAdapter = bridgeHandler.getSbusConnection();
        if (sbusAdapter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/error.device.bridge-not-initialized");
            return;
        }

        startPolling();

        // Register this handler as a listener for async messages
        try {
            sbusAdapter.addMessageListener(this);
            logger.debug("Registered handler {} as message listener", getThing().getUID());
        } catch (IllegalStateException e) {
            logger.warn("Failed to register message listener for {}: {}", getThing().getUID(), e.getMessage());
        }
    }

    /**
     * Initialize channels for this device based on its configuration.
     * This method should be implemented by concrete handlers to set up their specific channels.
     */
    protected abstract void initializeChannels();

    /**
     * Create or update a channel with the specified ID and type.
     *
     * @param channelId The ID of the channel to create/update
     * @param channelTypeId The type ID of the channel
     */
    protected void createChannel(String channelId, String channelTypeId) {
        ThingBuilder thingBuilder = ThingBuilder.create(getThing().getThingTypeUID(), getThing().getUID())
                .withConfiguration(getThing().getConfiguration()).withBridge(getThing().getBridgeUID());

        // Add all existing channels except the one we're creating/updating
        ChannelUID newChannelUID = new ChannelUID(getThing().getUID(), channelId);
        for (Channel existingChannel : getThing().getChannels()) {
            if (!existingChannel.getUID().equals(newChannelUID)) {
                thingBuilder.withChannel(existingChannel);
            }
        }

        // Add the new channel
        Channel channel = ChannelBuilder.create(newChannelUID).withType(new ChannelTypeUID(BINDING_ID, channelTypeId))
                .withConfiguration(new Configuration()).build();
        thingBuilder.withChannel(channel);

        // Update the thing with the new channel configuration
        updateThing(thingBuilder.build());
    }

    /**
     * Start polling the device for updates based on the configured refresh interval.
     */
    protected void startPolling() {
        SbusDeviceConfig config = getConfigAs(SbusDeviceConfig.class);
        if (config.refresh > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    pollDevice();
                } catch (IllegalStateException e) {
                    logger.warn("Error polling Sbus device: {}", e.getMessage());
                }
            }, 0, config.refresh, TimeUnit.SECONDS);
        } else if (config.refresh == 0) {
            // Run polling once to set initial thing state when refresh is disabled
            pollingJob = scheduler.schedule(() -> {
                try {
                    pollDevice();
                } catch (IllegalStateException e) {
                    logger.warn("Error polling Sbus device: {}", e.getMessage());
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Poll the device for updates. This method should be implemented by concrete handlers
     * to update their specific channel states.
     */
    protected abstract void pollDevice();

    /**
     * Process an asynchronous message received from the network.
     * This method should be implemented by concrete handlers to process messages
     * specific to their device type using their existing protocol adaptation methods.
     *
     * @param response the received SBUS response message
     */
    protected abstract void processAsyncMessage(SbusResponse response);

    /**
     * Reset the polling timer by cancelling the current polling job and starting a new one.
     * This should be called when an async message is successfully processed to reduce
     * unnecessary polling when the device is actively sending updates.
     */
    protected void resetPollingTimer() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(false); // Cancel the polling job without interrupting if it is currently running (cancel(false))
        }
        startPolling();
        logger.debug("Reset polling timer for handler {}", getThing().getUID());
    }

    // SbusMessageListener implementation

    @Override
    public void onMessageReceived(SbusResponse response) {
        try {
            // Check if this message is relevant to this handler
            if (isMessageRelevant(response)) {
                logger.debug("Processing async message for handler {}", getThing().getUID());
                processAsyncMessage(response);
                resetPollingTimer();
            }
        } catch (Exception e) {
            logger.warn("Error processing async message for handler {}: {}", getThing().getUID(), e.getMessage(), e);
        }
    }

    @Override
    public void onError(Exception error, byte[] rawMessage) {
        logger.warn("Error in message listener for handler {}: {}", getThing().getUID(), error.getMessage(), error);
    }

    /**
     * Check if the received message is relevant to this handler.
     * This method should be implemented by concrete handlers to filter messages
     * based on subnet ID, unit ID, or other criteria.
     *
     * @param response the received SBUS response message
     * @return true if the message is relevant to this handler
     */
    protected abstract boolean isMessageRelevant(SbusResponse response);

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        if (sbusAdapter != null) {
            // Unregister this handler as a listener
            sbusAdapter.removeMessageListener(this);
            logger.debug("Unregistered handler {} as message listener", getThing().getUID());
        }
        pollingJob = null;
        sbusAdapter = null;
        super.dispose();
    }
}
