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
package org.openhab.binding.ondilo.internal;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.Pool;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link OndiloBridge} handles OAuth2 authentication for Ondilo API.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloBridge {
    private final Logger logger = LoggerFactory.getLogger(OndiloBridge.class);
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> ondiloBridgePollingJob;
    private @Nullable List<Pool> pools;
    public @Nullable OndiloApiClient apiClient;

    public OndiloBridge(OndiloBridgeHandler bridgeHandler, OAuthClientService oAuthService,
            AccessTokenResponse accessTokenResponse, int refreshInterval, ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;

        this.apiClient = new OndiloApiClient(oAuthService, accessTokenResponse);
        startOndiloBridgePolling(refreshInterval);
    }

    private void startOndiloBridgePolling(Integer refreshInterval) {
        ScheduledFuture<?> currentPollingJob = ondiloBridgePollingJob;
        if (currentPollingJob == null) {
            ondiloBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollOndilos(), 1, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    public void stopOndiloBridgePolling() {
        ScheduledFuture<?> currentPollingJob = ondiloBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            ondiloBridgePollingJob = null;
        }
    }

    public synchronized void pollOndilos() {
        try {
            OndiloApiClient apiClient = this.apiClient;
            if (apiClient != null) {
                String poolsJson = apiClient.get("/pools");
                logger.trace("pools: {}", poolsJson);
                // Parse JSON to DTO
                Gson gson = new Gson();
                List<Pool> pools = gson.fromJson(poolsJson, new TypeToken<List<Pool>>() {
                }.getType());
                if ((pools != null) && !pools.isEmpty()) {
                    logger.trace("Polled {} pools", pools.size());
                } else {
                    logger.warn("No pools found or failed to parse JSON response");
                }
                this.pools = pools;
            } else {
                logger.error("OndiloApiClient is not initialized, cannot poll pools");
            }
        } catch (RuntimeException e) {
            logger.error("Unexpected error in polling job: {}", e.getMessage(), e);
        }
    }

    public @Nullable List<Pool> getPools() {
        return pools;
    }

    public void dispose() {
        stopOndiloBridgePolling();
        this.apiClient = null;
        logger.trace("OndiloBridge disposed and polling job stopped.");
    }
}
