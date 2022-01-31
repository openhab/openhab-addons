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
package org.openhab.binding.unifi.internal.handler;

import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiControllerThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiCommunicationException;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidCredentialsException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidHostException;
import org.openhab.binding.unifi.internal.api.UniFiSSLException;
import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiControllerThingHandler} is responsible for handling commands and status
 * updates for the UniFi Controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerThingHandler extends BaseBridgeHandler {

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return UniFiBindingConstants.THING_TYPE_CONTROLLER.equals(thingTypeUID);
    }

    private static final String STATUS_DESCRIPTION_COMMUNICATION_ERROR = "Error communicating with the UniFi controller";

    private static final String STATUS_DESCRIPTION_SSL_ERROR = "Error establishing an SSL connection with the UniFi controller";

    private static final String STATUS_DESCRIPTION_INVALID_CREDENTIALS = "Invalid username and/or password - please double-check your configuration";

    private static final String STATUS_DESCRIPTION_INVALID_HOSTNAME = "Invalid hostname - please double-check your configuration";

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerThingHandler.class);

    private UniFiControllerThingConfig config = new UniFiControllerThingConfig();

    private @Nullable volatile UniFiController controller; /* mgb: volatile because accessed from multiple threads */

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;

    public UniFiControllerThingHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    // Public API

    @Override
    public void initialize() {
        // mgb: called when the config changes
        cancelRefreshJob();
        config = getConfig().as(UniFiControllerThingConfig.class);
        logger.debug("Initializing the UniFi Controller Handler with config = {}", config);
        try {
            controller = new UniFiController(httpClient, config.getHost(), config.getPort(), config.getUsername(),
                    config.getPassword(), config.isUniFiOS());
            controller.start();
            updateStatus(ONLINE);
        } catch (UniFiInvalidHostException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_HOSTNAME);
        } catch (UniFiCommunicationException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        } catch (UniFiSSLException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_SSL_ERROR);
        } catch (UniFiInvalidCredentialsException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS);
        } catch (UniFiException e) {
            logger.error("Unknown error while configuring the UniFi Controller", e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (status == ONLINE || (status == OFFLINE && statusDetail == COMMUNICATION_ERROR)) {
            scheduleRefreshJob();
        } else if (status == OFFLINE && statusDetail == CONFIGURATION_ERROR) {
            cancelRefreshJob();
        }
        // mgb: update the status only if it's changed
        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(status, statusDetail).withDescription(description)
                .build();
        if (!statusInfo.equals(getThing().getStatusInfo())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        if (controller != null) {
            try {
                controller.stop();
            } catch (UniFiException e) {
                // mgb: nop as we're in dispose
            }
            controller = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nop - read-only binding
        logger.warn("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);
    }

    public @Nullable UniFiController getController() {
        return controller;
    }

    public int getRefreshInterval() {
        return config.getRefresh();
    }

    // Private API

    private void scheduleRefreshJob() {
        synchronized (this) {
            if (refreshJob == null) {
                logger.debug("Scheduling refresh job every {}s", config.getRefresh());
                refreshJob = scheduler.scheduleWithFixedDelay(this::run, 0, config.getRefresh(), TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            if (refreshJob != null) {
                logger.debug("Cancelling refresh job");
                refreshJob.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void run() {
        try {
            logger.trace("Executing refresh job");
            refresh();
            updateStatus(ONLINE);
        } catch (UniFiCommunicationException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        } catch (UniFiInvalidCredentialsException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS);
        } catch (Exception e) {
            logger.warn("Unhandled exception while refreshing the UniFi Controller {} - {}", getThing().getUID(),
                    e.getMessage());
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refresh() throws UniFiException {
        if (controller != null) {
            logger.debug("Refreshing the UniFi Controller {}", getThing().getUID());
            controller.refresh();
            // mgb: then refresh all the client things
            getThing().getThings().forEach((thing) -> {
                if (thing.getHandler() instanceof UniFiBaseThingHandler) {
                    ((UniFiBaseThingHandler) thing.getHandler()).refresh();
                }
            });
        }
    }
}
