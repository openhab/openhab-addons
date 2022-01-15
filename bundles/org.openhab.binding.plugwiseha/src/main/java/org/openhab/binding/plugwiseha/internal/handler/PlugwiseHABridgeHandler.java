/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHACommunicationException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAInvalidHostException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHANotAuthorizedException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHATimeoutException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAUnauthorizedException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAModel;
import org.openhab.binding.plugwiseha.internal.api.model.dto.GatewayInfo;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHABridgeThingConfig;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHAThingConfig;
import org.openhab.binding.plugwiseha.internal.discovery.PlugwiseHADiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHABridgeHandler} class is responsible for handling
 * commands and status updates for the Plugwise Home Automation bridge.
 * Extends @{link BaseBridgeHandler}
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 * 
 */

@NonNullByDefault
public class PlugwiseHABridgeHandler extends BaseBridgeHandler {

    // Private Static error messages

    private static final String STATUS_DESCRIPTION_COMMUNICATION_ERROR = "Error communicating with the Plugwise Home Automation controller";
    private static final String STATUS_DESCRIPTION_TIMEOUT = "Communication timeout while communicating with the Plugwise Home Automation controller";
    private static final String STATUS_DESCRIPTION_CONFIGURATION_ERROR = "Invalid or missing configuration";
    private static final String STATUS_DESCRIPTION_INVALID_CREDENTIALS = "Invalid username and/or password - please double-check your configuration";
    private static final String STATUS_DESCRIPTION_INVALID_HOSTNAME = "Invalid hostname - please double-check your configuration";

    // Private member variables/constants
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable volatile PlugwiseHAController controller;

    private final HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(PlugwiseHABridgeHandler.class);

    // Constructor

    public PlugwiseHABridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    // Public methods

    @Override
    public void initialize() {
        PlugwiseHABridgeThingConfig bridgeConfig = getConfigAs(PlugwiseHABridgeThingConfig.class);

        if (this.checkConfig(bridgeConfig)) {
            logger.debug("Initializing the Plugwise Home Automation bridge handler with config = {}", bridgeConfig);
            try {
                this.controller = new PlugwiseHAController(httpClient, bridgeConfig.getHost(), bridgeConfig.getPort(),
                        bridgeConfig.getUsername(), bridgeConfig.getsmileId());
                scheduleRefreshJob(bridgeConfig);
            } catch (PlugwiseHAException e) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, e.getMessage());
            }
        } else {
            logger.warn("Invalid config for the Plugwise Home Automation bridge handler with config = {}",
                    bridgeConfig);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(PlugwiseHADiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.warn(
                "Ignoring command = {} for channel = {} - this channel for the Plugwise Home Automation binding is read-only!",
                command, channelUID);
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        if (this.controller != null) {
            this.controller = null;
        }
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_BRIDGE_TYPES_UIDS.contains(thingTypeUID);
    }

    // Getters & setters

    public @Nullable PlugwiseHAController getController() {
        return this.controller;
    }

    // Protected and private methods

    /**
     * Checks the configuration for validity, result is reflected in the status of
     * the Thing
     */
    private boolean checkConfig(PlugwiseHABridgeThingConfig bridgeConfig) {
        if (!bridgeConfig.isValid()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_CONFIGURATION_ERROR);
            return false;
        } else {
            return true;
        }
    }

    private void scheduleRefreshJob(PlugwiseHABridgeThingConfig bridgeConfig) {
        synchronized (this) {
            if (this.refreshJob == null) {
                logger.debug("Scheduling refresh job every {}s", bridgeConfig.getRefresh());
                this.refreshJob = scheduler.scheduleWithFixedDelay(this::run, 0, bridgeConfig.getRefresh(),
                        TimeUnit.SECONDS);
            }
        }
    }

    private void run() {
        try {
            logger.trace("Executing refresh job");
            refresh();

            if (super.thing.getStatus() == ThingStatus.INITIALIZING) {
                setBridgeProperties();
            }

        } catch (PlugwiseHAInvalidHostException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_HOSTNAME);
        } catch (PlugwiseHAUnauthorizedException | PlugwiseHANotAuthorizedException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS);
        } catch (PlugwiseHACommunicationException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        } catch (PlugwiseHATimeoutException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_TIMEOUT);
        } catch (PlugwiseHAException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void refresh() throws PlugwiseHAException {
        if (this.getController() != null) {
            logger.debug("Refreshing the Plugwise Home Automation Controller {}", getThing().getUID());

            PlugwiseHAController controller = this.getController();
            if (controller != null) {
                controller.refresh();
                updateStatus(ONLINE);
            }

            getThing().getThings().forEach((thing) -> {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof PlugwiseHABaseHandler) {
                    ((PlugwiseHABaseHandler<PlugwiseHAModel, PlugwiseHAThingConfig>) thingHandler).refresh();
                }
            });
        }
    }

    @SuppressWarnings("null")
    private void cancelRefreshJob() {
        synchronized (this) {
            if (this.refreshJob != null) {
                logger.debug("Cancelling refresh job");
                this.refreshJob.cancel(true);
                this.refreshJob = null;
            }
        }
    }

    protected void setBridgeProperties() {
        logger.debug("Setting bridge properties");
        try {
            PlugwiseHAController controller = this.getController();
            GatewayInfo localGatewayInfo = null;
            if (controller != null) {
                localGatewayInfo = controller.getGatewayInfo();
            }

            if (localGatewayInfo != null) {
                Map<String, String> properties = editProperties();
                if (localGatewayInfo.getFirmwareVersion() != null) {
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, localGatewayInfo.getFirmwareVersion());
                }
                if (localGatewayInfo.getHardwareVersion() != null) {
                    properties.put(Thing.PROPERTY_HARDWARE_VERSION, localGatewayInfo.getHardwareVersion());
                }
                if (localGatewayInfo.getMacAddress() != null) {
                    properties.put(Thing.PROPERTY_MAC_ADDRESS, localGatewayInfo.getMacAddress());
                }
                if (localGatewayInfo.getVendorName() != null) {
                    properties.put(Thing.PROPERTY_VENDOR, localGatewayInfo.getVendorName());
                }
                if (localGatewayInfo.getVendorModel() != null) {
                    properties.put(Thing.PROPERTY_MODEL_ID, localGatewayInfo.getVendorModel());
                }

                updateProperties(properties);
            }
        } catch (PlugwiseHAException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        }
    }
}
