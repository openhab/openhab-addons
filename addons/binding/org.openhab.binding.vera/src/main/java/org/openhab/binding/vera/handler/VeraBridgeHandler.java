/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera.handler;

import static org.openhab.binding.vera.VeraBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.vera.config.VeraBridgeConfiguration;
import org.openhab.binding.vera.controller.Controller;
import org.openhab.binding.vera.controller.json.Sdata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeraBridgeHandler} manages the connection between Vera and binding.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BridgePolling bridgePolling;
    private ScheduledFuture<?> pollingJob;

    private VeraBridgeConfiguration mConfig;
    private Controller controller;

    private class Initializer implements Runnable {
        @Override
        public void run() {
            logger.debug("Authenticate to the Vera controller ...");

            try {
                if (controller.isConnected()) {
                    logger.debug("Vera controller successfully authenticated");
                    updateStatus(ThingStatus.ONLINE);

                    // Initialize bridge polling
                    if (pollingJob == null || pollingJob.isCancelled()) {
                        logger.debug("Starting polling job at intervall {}", mConfig.getPollingInterval());
                        pollingJob = scheduler.scheduleWithFixedDelay(bridgePolling, 10, mConfig.getPollingInterval(),
                                TimeUnit.SECONDS);
                    } else {
                        // Called when thing or bridge updated ...
                        logger.debug("Polling is allready active");
                    }
                } else {
                    logger.warn("Can't connect to Vera controller");
                }
            } catch (Exception e) {
                logger.error("Error occurred when initialize bridge: {}", e);
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when initialize bridge: " + e.getMessage());
                }
            }
        }
    };

    public VeraBridgeHandler(Bridge bridge) {
        super(bridge);
        bridgePolling = new BridgePolling();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vera controller ...");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");
        mConfig = getConfigAs(VeraBridgeConfiguration.class);
        if (mConfig != null) {
            controller = new Controller(mConfig);
            scheduler.execute(new Initializer());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Vera controller ...");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Handle Vera configuration update ...");
        if (pollingJob != null) {
            pollingJob.cancel(false);
        }
        initialize();
        refreshAllThings();
        super.handleConfigurationUpdate(configurationParameters);
    }

    private class BridgePolling implements Runnable {
        @Override
        public void run() {
            getController().updateSdata();
            if (getController().getSdata() != null) {
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Connection to bridge {} restored.", getThing().getLabel());
                }
                refreshAllThings();
            } else if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Error occurred when polling bridge.");
            }
        }
    };

    private void refreshAllThings() {
        logger.debug("Handle bridge refresh command for all configured things ...");
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler instanceof VeraDeviceHandler) {
                logger.debug("Refreshing device: {}", thing.getLabel());
                ((VeraDeviceHandler) handler).refreshAllChannels();
            } else if (handler instanceof VeraSceneHandler) {
                logger.debug("Refreshing scene: {}", thing.getLabel());
                ((VeraSceneHandler) handler).refreshAllChannels();
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command for channel: {} with command: {}", channelUID.getId(), command.toString());
        if (channelUID.getId().equals(ACTIONS_CHANNEL)) {
            if (command.toString().equals(ACTIONS_CHANNEL_OPTION_REFRESH)) {
                refreshAllThings();
            }
        }
    }

    protected VeraBridgeConfiguration getVeraBridgeConfiguration() {
        return mConfig;
    }

    public Controller getController() {
        return controller;
    }

    public Sdata getData() {
        return controller.getSdata();
    }
}
