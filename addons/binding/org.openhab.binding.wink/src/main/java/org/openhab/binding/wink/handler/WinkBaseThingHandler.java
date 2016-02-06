/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.AuthenticationException;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.JsonWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

/**
 * This is the base class for devices connected to the wink hub. Implements pubnub registration
 * and initialization for all wink devices.
 *
 * @author Shawn Crosby
 *
 */
public abstract class WinkBaseThingHandler extends BaseThingHandler {
    public WinkBaseThingHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(WinkBaseThingHandler.class);

    protected WinkHub2BridgeHandler bridgeHandler;
    protected PubNub pubnub;
    private ScheduledFuture<?> pollingJob;

    @Override
    public void initialize() {
        logger.debug("Initializing Device {}", getThing());
        bridgeHandler = (WinkHub2BridgeHandler) getBridge().getHandler();
        if (getThing().getConfiguration().get("uuid") == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "UUID must be specified in Config");
        } else {
            try {
                if (getDevice().getCurrentState().get("connection").equals("true")) {
                    updateStatus(ThingStatus.ONLINE);
                    updateDeviceState(getDevice());
                    registerToPubNub();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device Not Connected");
                }
            } catch (AuthenticationException e) {
                logger.error("Unable to initialize device: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Unable to initialize device: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        pollingJob = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateDeviceState(getDevice());
            }
        }, 0, 300, TimeUnit.SECONDS);

        super.initialize();
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down thing {}", getThing());
        if (pubnub != null) {
            this.pubnub.unsubscribeAll();
            this.pubnub.destroy();
        }

        this.pollingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleWinkCommand(channelUID, command);
        } catch (RuntimeException e) {
            logger.error("Unable to process command: {}", e.getMessage());
        }
    }

    /**
     * Sub-implementation of ThingHandler handleCommand to deal with exception handling more cleanly
     *
     * @param channelUID
     * @param command
     */
    protected abstract void handleWinkCommand(ChannelUID channelUID, Command command);

    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            for (Channel channel : getThing().getChannels()) {
                if (channelUID.equals(channel.getUID())) {
                    updateDeviceState(getDevice());
                    logger.debug("Channel {} Linked", channelUID.getId());
                    break;
                }
            }
        } catch (AuthenticationException e) {
            logger.error("Unable to process channel link: {}", e.getMessage());
        }
    }

    /**
     * Subclasses must define the correct wink supported device type
     *
     * @return Enum from WinkSupportedDevice for this device
     */
    protected abstract WinkSupportedDevice getDeviceType();

    /**
     * Retrieves the device configuration and state from the API
     *
     * @return
     */
    protected IWinkDevice getDevice() {
        return bridgeHandler.getDevice(getDeviceType(), getThing().getConfiguration().get("uuid").toString());
    }

    /**
     * Subclasses must implement this method to perform the mapping between the properties and state
     * retrieved from the API and how that state is represented in OpenHab.
     *
     * @param device
     */
    protected abstract void updateDeviceState(IWinkDevice device);

    /**
     * Handles state change events from the api
     */
    protected void registerToPubNub() {
        logger.debug("Doing the PubNub registration for :\n{}", thing.getLabel());

        try {
            IWinkDevice device = getDevice();

            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey(device.getPubNubSubscriberKey());
            pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

            this.pubnub = new PubNub(pnConfiguration);
            this.pubnub.addListener(new SubscribeCallback() {
                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    JsonParser parser = new JsonParser();
                    JsonObject jsonMessage = parser.parse(message.getMessage().getAsString()).getAsJsonObject();
                    IWinkDevice device = new JsonWinkDevice(jsonMessage);
                    logger.debug("Received update from device: {}", device);
                    updateDeviceState(device);
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                }

                @Override
                public void status(PubNub arg0, PNStatus status) {
                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                        // internet got lost, do some magic and call reconnect when ready
                        logger.error("Unexpected Disconnect from PubNub, reconnecting");
                        pubnub.reconnect();
                    } else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                        // do some magic and call reconnect when ready
                        logger.error("PubNub timeout, reconnecting");
                        pubnub.reconnect();
                    } else if (status.isError()) {
                        logger.error("PubNub Error {}", status.getCategory());
                    } else {
                        logger.debug("PubNub Status {}", status.getCategory());
                    }
                }
            });

            this.pubnub.subscribe().channels(Arrays.asList(device.getPubNubChannel())).execute();
        } catch (AuthenticationException e) {
            logger.error("Unable to subscribe to pubnub: {}", e.getMessage());
        }
    }

}
