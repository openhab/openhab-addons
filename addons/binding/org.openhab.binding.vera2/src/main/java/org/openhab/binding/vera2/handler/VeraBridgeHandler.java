/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.vera2.config.VeraBridgeConfiguration;
import org.openhab.binding.vera2.controller.Controller;
import org.openhab.binding.vera2.controller.Vera.json.Sdata;
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

    private VeraBridgeConfiguration mConfig = null;
    private Controller controller = null;

    private class Initializer implements Runnable {
        @Override
        public void run() {
            logger.debug("Authenticate to the Vera controller ...");

            try {
                if (controller.isConnected()) {
                    logger.info("Vera controller successfully authenticated");
                    updateStatus(ThingStatus.ONLINE);

                    // Initialize bridge polling
                    if (pollingJob == null || pollingJob.isCancelled()) {
                        logger.debug("Starting polling job at intervall {}", mConfig.getPollingInterval());
                        pollingJob = scheduler.scheduleAtFixedRate(bridgePolling, 10, mConfig.getPollingInterval(),
                                TimeUnit.SECONDS);
                    } else {
                        // Called when thing or bridge updated ...
                        logger.debug("Polling is allready active");
                    }

                    // Initializing all containing device things
                    logger.debug("Initializing all configured devices ...");
                    for (Thing thing : getThing().getThings()) {
                        ThingHandler handler = thing.getHandler();
                        if (handler != null) {
                            logger.debug("Initializing device: {}", thing.getLabel());
                            handler.initialize();
                        } else {
                            logger.warn("Initializing device failed (DeviceHandler is null): {}", thing.getLabel());
                        }
                    }
                } else {
                    logger.warn("Cant connect to Vera controller");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when initialize bridge.");
                }
            }
        }
    };

    /**
     * Disposer clean up openHAB Connector configuration
     */
    private class Remover implements Runnable {

        @Override
        public void run() {
            // Removing all containing device things
            logger.debug("Removing all configured devices ...");
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    logger.debug("Removing device: {}", thing.getLabel());
                    handler.handleRemoval();
                } else {
                    logger.warn("Removing device failed (DeviceHandler is null): {}", thing.getLabel());
                }
            }

            // status update will finally remove the thing
            updateStatus(ThingStatus.REMOVED);
        }
    };

    public VeraBridgeHandler(Bridge bridge) {
        super(bridge);
        bridgePolling = new BridgePolling();
    }

    @Override
    public void initialize() {
        logger.info("Initializing Vera controller ...");

        // Set thing status to a valid status
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");

        // Configuration - thing status update with a error message
        mConfig = loadAndCheckConfiguration();

        if (mConfig != null) {
            controller = new Controller(mConfig.getVeraIpAddress(), "" + mConfig.getVeraPort());
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
        super.handleConfigurationUpdate(configurationParameters);
    }

    private class BridgePolling implements Runnable {
        @Override
        public void run() {
            // logger.debug("Starting polling for bridge: {}", getThing().getLabel());
            if (getController().updateSdata() != null) {
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Connection to bridge {} restored.", getThing().getLabel());
                }
            } else if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Error occurred when polling bridge.");
            }
        }
    };

    @Override
    public void handleRemoval() {
        logger.debug("Handle removal Vera controller ...");
        scheduler.execute(new Remover());
    }

    protected VeraBridgeConfiguration getVeraBridgeConfiguration() {
        return mConfig;
    }

    private VeraBridgeConfiguration loadAndCheckConfiguration() {
        VeraBridgeConfiguration config = getConfigAs(VeraBridgeConfiguration.class);
        if (StringUtils.trimToNull(config.getVeraIpAddress()) == null) {
            config.setVeraIpAddress("192.168.1.10"); // default value
        }
        if (config.getVeraPort() == null) {
            config.setVeraPort(3480);
        }
        if (config.getPollingInterval() == null) {
            config.setPollingInterval(60);
        }
        return config;
    }

    public Controller getController() {
        return controller;
    }

    public Sdata getData() {
        return controller.getSdata();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
