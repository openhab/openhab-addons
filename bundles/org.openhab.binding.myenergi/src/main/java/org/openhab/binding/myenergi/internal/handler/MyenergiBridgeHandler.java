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
package org.openhab.binding.myenergi.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.MyenergiApiClient;
import org.openhab.binding.myenergi.internal.MyenergiBridgeConfiguration;
import org.openhab.binding.myenergi.internal.MyenergiDiscoveryService;
import org.openhab.binding.myenergi.internal.dto.EddiSummary;
import org.openhab.binding.myenergi.internal.dto.HarviSummary;
import org.openhab.binding.myenergi.internal.dto.ZappiSummary;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.binding.myenergi.internal.exception.AuthenticationException;
import org.openhab.core.config.core.status.ConfigStatusCallback;
import org.openhab.core.config.core.status.ConfigStatusSource;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class MyenergiBridgeHandler extends BaseBridgeHandler implements ConfigStatusCallback {

    private final Logger logger = LoggerFactory.getLogger(MyenergiBridgeHandler.class);

    private final MyenergiApiClient apiClient;
    private @Nullable ScheduledFuture<?> devicePollingJob = null;

    public MyenergiBridgeHandler(Bridge thing, MyenergiApiClient apiClient) {
        super(thing);
        this.apiClient = apiClient;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MyEnergiBridgeHandler");
        MyenergiBridgeConfiguration config = getConfigAs(MyenergiBridgeConfiguration.class);

        if (config.hubSerialNumber.isEmpty() || config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-hubSerialNumber-or-password");
            return;
        }
        if (config.refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-intervals");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        try {
            logger.debug("Login to MyEnergi API with hubSerialNumber: {}", config.hubSerialNumber);
            apiClient.initialize(config.hubSerialNumber, config.password);
            apiClient.updateTopologyCache();
            logger.debug("Cache update successful, setting bridge status to ONLINE");
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-authentication");
            return;
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-general");
        }

        ScheduledFuture<?> job = devicePollingJob;
        if (job == null || job.isCancelled()) {
            devicePollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refreshDevices();
                    if ((getThing().getStatus() == ThingStatus.OFFLINE) && (getThing().getStatusInfo()
                            .getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                        // if previous status was COMMUNICATION_ERROR, we now reestablished the comms
                        updateStatus(ThingStatus.ONLINE);
                    }
                } catch (ApiException e) {
                    logger.warn("Exception from API - {}", getThing().getUID(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-general");
                }
            }, config.refreshInterval, config.refreshInterval, TimeUnit.HOURS);
            logger.debug("Bridge device topology polling job every {} hours", config.refreshInterval);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MyenergiDiscoveryService.class);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = devicePollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            devicePollingJob = null;
            logger.debug("Stopped MyEnergi device topology job");
        }
        apiClient.stop();
    }

    @Override
    public void configUpdated(@Nullable ConfigStatusSource configStatusSource) {
        logger.debug("Configuration has been updated for bridge");
    }

    public Iterable<EddiSummary> listEddis() {
        return apiClient.getData().getEddis();
    }

    public Iterable<ZappiSummary> listZappis() {
        return apiClient.getData().getZappis();
    }

    public Iterable<HarviSummary> listHarvis() {
        return apiClient.getData().getHarvis();
    }

    private void refreshDevices() throws ApiException {
        apiClient.updateTopologyCache();
    }
}
