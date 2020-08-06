/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.warmup.internal.api.MyWarmupApi;
import org.openhab.binding.warmup.internal.api.MyWarmupApiException;
import org.openhab.binding.warmup.internal.discovery.WarmupDiscoveryService;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;

/**
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class MyWarmupAccountHandler extends BaseBridgeHandler {

    private final MyWarmupApi api;

    private @Nullable QueryResponseDTO queryResponse = null;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable WarmupDiscoveryService discoveryService;

    public MyWarmupAccountHandler(Bridge thing, final HttpClient httpClient) {
        super(thing);
        api = new MyWarmupApi(httpClient, getConfigAs(MyWarmupConfigurationDTO.class));
    }

    @Override
    public void initialize() {
        MyWarmupConfigurationDTO config = getConfigAs(MyWarmupConfigurationDTO.class);
        if (config.refreshIntervalSec >= 10) {
            api.setConfiguration(config);
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshFromServer, 0, config.refreshIntervalSec,
                    TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Refresh interval misconfigured (minimum 10s)");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        cancelRefresh();
    }

    public void cancelRefresh() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public synchronized void refreshFromServer() {
        try {
            queryResponse = api.getStatus();
        } catch (MyWarmupApiException e) {
            queryResponse = null;
            cancelRefresh();
        } finally {
            if (queryResponse != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to contact MyWarmup");
            }
            refreshFromCache();
        }
    }

    /**
     * Trigger updates to all devices
     */
    public synchronized void refreshFromCache() {
        notifyListeners(queryResponse);
    }

    public synchronized void scanDevices() {
        if (discoveryService != null && queryResponse != null) {
            discoveryService.refresh(queryResponse);
        }
    }

    /**
     * Initiate discovery
     */
    public void setDiscoveryService(final WarmupDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     *
     * @return reference to the bridge's API
     */
    public MyWarmupApi getApi() {
        return api;
    }

    private void notifyListeners(@Nullable QueryResponseDTO domain) {
        if (discoveryService != null && queryResponse != null) {
            discoveryService.refresh(queryResponse);
        }
        getThing().getThings().stream().filter(thing -> thing.getHandler() instanceof WarmupRefreshListener)
                .map(Thing::getHandler).map(WarmupRefreshListener.class::cast).forEach(thing -> thing.refresh(domain));
    }
}
