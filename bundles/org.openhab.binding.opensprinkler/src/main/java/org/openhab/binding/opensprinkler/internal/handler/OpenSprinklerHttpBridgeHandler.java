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
package org.openhab.binding.opensprinkler.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApi;
import org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiFactory;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public class OpenSprinklerHttpBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerHttpBridgeHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> delayedJob;
    private @Nullable OpenSprinklerApi openSprinklerDevice;
    private OpenSprinklerHttpInterfaceConfig openSprinklerConfig = new OpenSprinklerHttpInterfaceConfig();
    private OpenSprinklerApiFactory apiFactory;

    public OpenSprinklerHttpBridgeHandler(Bridge bridge, OpenSprinklerApiFactory apiFactory) {
        super(bridge);
        this.apiFactory = apiFactory;
    }

    public OpenSprinklerApi getApi() {
        OpenSprinklerApi api = openSprinklerDevice;
        if (api == null) {
            throw new IllegalStateException();
        }
        return api;
    }

    public void communicationError(Exception e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "Communication Error with the OpenSprinkler: " + e.getMessage());
    }

    public void refreshStations() {
        OpenSprinklerApi localApi = openSprinklerDevice;
        if (localApi == null || !localApi.isManualModeEnabled()) {
            setupAPI();
            localApi = openSprinklerDevice;
        }
        if (localApi != null) {
            try {
                localApi.refresh();
                updateStatus(ThingStatus.ONLINE);
                this.getThing().getThings().forEach(thing -> {
                    OpenSprinklerBaseHandler handler = (OpenSprinklerBaseHandler) thing.getHandler();
                    if (handler != null) {
                        handler.updateChannels();
                    }
                });
            } catch (CommunicationApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Could not sync status with the OpenSprinkler. " + e.getMessage());
            } catch (UnauthorizedApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Unauthorized, check your password is correct");
            }
        }
    }

    public void delayedRefresh() {
        ScheduledFuture<?> localFuture = delayedJob;
        if (localFuture == null || localFuture.isDone()) {
            delayedJob = scheduler.schedule(this::refreshStations, 3, TimeUnit.SECONDS);
        } else {// User has sent multiple commands quickly, only need to update the controls once.
            localFuture.cancel(true);
            delayedJob = scheduler.schedule(this::refreshStations, 3, TimeUnit.SECONDS);
        }
    }

    private void setupAPI() {
        logger.debug("Initializing OpenSprinkler with config (Hostname: {}, Port: {}, Refresh: {}).",
                openSprinklerConfig.hostname, openSprinklerConfig.port, openSprinklerConfig.refresh);
        try {
            openSprinklerDevice = apiFactory.getHttpApi(openSprinklerConfig);
            OpenSprinklerApi localApi = openSprinklerDevice;
            localApi.enterManualMode();
            if (!localApi.isManualModeEnabled()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Could not initialize the connection to the OpenSprinkler.");
            }
        } catch (CommunicationApiException | GeneralApiException exp) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Could not create an API connection to the OpenSprinkler. Error received: " + exp);
            return;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for the bridge handler
    }

    @Override
    public void initialize() {
        openSprinklerConfig = getConfig().as(OpenSprinklerHttpInterfaceConfig.class);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshStations, 2, openSprinklerConfig.refresh,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        OpenSprinklerApi localApi = openSprinklerDevice;
        if (localApi != null) {
            try {
                localApi.leaveManualMode();
            } catch (CommunicationApiException | UnauthorizedApiException e) {
                logger.warn("Could not close connection on teardown.");
            }
            openSprinklerDevice = null;
        }
        ScheduledFuture<?> localFuture = pollingJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            pollingJob = null;
        }
        localFuture = delayedJob;
        if (localFuture != null) {
            localFuture.cancel(true);
            pollingJob = null;
        }
    }
}
