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

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.warmup.internal.api.MyWarmupApi;
import org.openhab.binding.warmup.internal.api.MyWarmupConfigurationDTO;
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
        updateStatus(ThingStatus.UNKNOWN);
        api.setConfiguration(getConfigAs(MyWarmupConfigurationDTO.class));
        discoveryService = new WarmupDiscoveryService(this);
        BigDecimal refreshInterval = (BigDecimal) getConfig().get("refreshInterval");
        refreshJob = scheduler.scheduleWithFixedDelay(this::refreshFromServer, 0, refreshInterval.intValue(),
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshFromCache();
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    private synchronized void refreshFromServer() {
        try {
            queryResponse = api.getStatus();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            queryResponse = null;
            updateStatus(ThingStatus.OFFLINE);
        } finally {
            refreshFromCache();
        }
    }

    /**
     * Trigger updates to all devices
     */
    public synchronized void refreshFromCache() {
        notifyListeners(queryResponse);
    }

    /**
     * Initiate discovery
     */
    public void setDiscoveryService(final WarmupDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
        refreshFromCache();
    }

    /**
     * Remove reference to discovery service on conclusion
     */
    public void unsetDiscoveryService() {
        discoveryService = null;
    }

    /**
     *
     * @return reference to the bridge's API
     */
    public MyWarmupApi getApi() {
        return api;
    }

    private void notifyListeners(@Nullable QueryResponseDTO domain) {

        if (discoveryService != null && domain != null) {
            discoveryService.onRefresh(domain);
        }

        getThing().getThings().stream().filter(thing -> thing.getHandler() instanceof RoomHandler)
                .map(Thing::getHandler).map(RoomHandler.class::cast).forEach(thing -> thing.onRefresh(domain));
    }
}
