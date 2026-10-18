/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.loqed.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.loqed.internal.api.BoltState;
import org.openhab.binding.loqed.internal.api.LoqedApiClient;
import org.openhab.binding.loqed.internal.api.LoqedApiException;
import org.openhab.binding.loqed.internal.api.LoqedAuthenticationException;
import org.openhab.binding.loqed.internal.api.LoqedCommunicationException;
import org.openhab.binding.loqed.internal.api.LoqedLockData;
import org.openhab.binding.loqed.internal.discovery.LoqedDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles authentication and polling for all locks belonging to one LOQED account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedBridgeHandler extends BaseBridgeHandler implements LoqedBridge {
    private final Logger logger = LoggerFactory.getLogger(LoqedBridgeHandler.class);
    private final HttpClient httpClient;
    private final Object lifecycleLock = new Object();

    private volatile List<LoqedLockData> locks = List.of();
    private volatile @Nullable LoqedApiClient apiClient;
    private @Nullable ScheduledFuture<?> pollingJob;

    public LoqedBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        LoqedConfiguration config = getConfigAs(LoqedConfiguration.class);
        if (config.apiToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status.loqed.account.token-empty");
            return;
        }

        synchronized (lifecycleLock) {
            apiClient = new LoqedApiClient(httpClient, config.apiToken);
        }
        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, Math.max(30, config.refreshInterval),
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        synchronized (lifecycleLock) {
            apiClient = null;
            locks = List.of();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The account bridge has no channels.
    }

    public synchronized List<LoqedLockData> refreshAndGetLocks() {
        refresh();
        return locks;
    }

    public void refresh() {
        LoqedApiClient client = apiClient;
        if (client == null) {
            return;
        }

        try {
            List<LoqedLockData> refreshedLocks = client.getLocks();
            synchronized (lifecycleLock) {
                if (!client.equals(apiClient)) {
                    return;
                }
                locks = refreshedLocks;
            }
            if (!client.equals(apiClient)) {
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            if (!client.equals(apiClient)) {
                return;
            }
            getThing().getThings().stream().map(thing -> thing.getHandler()).filter(LoqedLockHandler.class::isInstance)
                    .map(LoqedLockHandler.class::cast).forEach(handler -> handler.updateFromBridge(refreshedLocks));
        } catch (LoqedAuthenticationException e) {
            if (client.equals(apiClient)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.account.token-rejected");
                logger.debug("LOQED rejected the personal access token", e);
            }
        } catch (LoqedApiException e) {
            if (client.equals(apiClient)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing-status.loqed.account.communication-error");
                logger.debug("Could not refresh LOQED locks", e);
            }
        }
    }

    public void setBoltState(String lockId, String keySecret, int localKeyId, BoltState boltState)
            throws LoqedApiException {
        LoqedApiClient client = apiClient;
        if (client == null) {
            throw new LoqedCommunicationException("The LOQED account bridge is not initialized",
                    new IllegalStateException());
        }
        client.setBoltState(lockId, boltState);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(LoqedDiscoveryService.class);
    }
}
