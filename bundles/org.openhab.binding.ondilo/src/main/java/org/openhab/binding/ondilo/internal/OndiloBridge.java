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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.LastMeasure;
import org.openhab.binding.ondilo.internal.dto.Pool;
import org.openhab.binding.ondilo.internal.dto.PoolConfiguration;
import org.openhab.binding.ondilo.internal.dto.PoolInfo;
import org.openhab.binding.ondilo.internal.dto.Recommendation;
import org.openhab.binding.ondilo.internal.dto.UserInfo;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OndiloBridge} handles OAuth2 authentication for Ondilo API.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloBridge {
    private final Logger logger = LoggerFactory.getLogger(OndiloBridge.class);
    private final ScheduledExecutorService scheduler;
    private final int refreshInterval;
    private final OndiloBridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> ondiloBridgePollingJob;
    private @Nullable List<Pool> pools;
    public @Nullable OndiloApiClient apiClient;
    private Map<Integer, OndiloHandler> ondiloHandlers = new HashMap<>();

    // Measures are taken every 60 minutes, so we should be able to retrieve next data directly 60 minutes after the
    // last measure. For whatever reason it takes a bit longer for the measures to finally reach the cloud and be
    // available via API. Therefore we add a buffer of 4 minutes to the next polling time.
    // If the last measure was taken at 12:00, we will poll again at 13:04.
    private static final Duration TARGET_REFRESH_INTERVAL = Duration.ofMinutes(60 + 4);

    public OndiloBridge(OndiloBridgeHandler bridgeHandler, OAuthClientService oAuthService,
            AccessTokenResponse accessTokenResponse, int refreshInterval, ScheduledExecutorService scheduler) {
        this.bridgeHandler = bridgeHandler;
        this.scheduler = scheduler;
        this.refreshInterval = refreshInterval;

        this.apiClient = new OndiloApiClient(oAuthService, accessTokenResponse);
        startOndiloBridgePolling(refreshInterval);
    }

    public void registerOndiloHandler(int poolId, OndiloHandler handler) {
        ondiloHandlers.put(poolId, handler);
        logger.trace("Registered OndiloHandler for Ondilo ICO with ID: {}", poolId);
    }

    public void unregisterOndiloHandler(int poolId) {
        ondiloHandlers.remove(poolId);
        logger.trace("Unregistered OndiloHandler for Ondilo ICO with ID: {}", poolId);
    }

    private @Nullable OndiloHandler getOndiloHandlerForPool(int poolId) {
        return ondiloHandlers.get(poolId);
    }

    private void startOndiloBridgePolling(Integer refreshInterval) {
        if (ondiloBridgePollingJob == null) {
            ondiloBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollOndiloICOs(), 10, refreshInterval,
                    TimeUnit.SECONDS);
        } else {
            logger.warn("Ondilo bridge polling job is already running, not starting a new one");
        }
    }

    public void stopOndiloBridgePolling() {
        ScheduledFuture<?> ondiloBridgePollingJob = this.ondiloBridgePollingJob;
        if (ondiloBridgePollingJob != null) {
            ondiloBridgePollingJob.cancel(true);
            this.ondiloBridgePollingJob = null;
        }
    }

    public synchronized void pollOndiloICOs() {
        try {
            OndiloApiClient apiClient = this.apiClient;
            if (apiClient != null) {
                UserInfo userInfo = apiClient.request("GET", "/user/info", UserInfo.class);
                if (userInfo != null) {
                    bridgeHandler.updateUserInfo(userInfo);
                }

                Pool pools[] = apiClient.request("GET", "/pools", Pool[].class);
                if (pools != null && !(pools.length == 0)) {
                    logger.trace("Polled {} Ondilo ICOs", pools.length);
                    // Poll last measures and recommendations for each pool
                    Instant lastValueTime = null;
                    for (Pool pool : pools) {
                        Instant valueTime = pollOndiloICO(pool.id);
                        if (lastValueTime == null || (valueTime != null && valueTime.isBefore(lastValueTime))) {
                            lastValueTime = valueTime;
                        }
                    }
                    if (lastValueTime != null) {
                        adaptPollingToValueTime(lastValueTime, refreshInterval);
                    }
                } else {
                    logger.warn("No Ondilo ICO found or failed to parse JSON response");
                }
                // Store the pools for later access
                this.pools = Arrays.asList(pools);
            } else {
                logger.warn("OndiloApiClient is not initialized, cannot poll Ondilo ICOs");
            }
        } catch (RuntimeException e) {
            logger.warn("Unexpected error in polling job: {}", e.getMessage(), e);
        }
    }

    private void adaptPollingToValueTime(Instant lastValueTime, int refreshInterval) {
        // Adjusting the polling reduces the delay when new measures get available, without polling too frequently and
        // hitting API rate limits.
        Instant nextValueTime = lastValueTime.plus(TARGET_REFRESH_INTERVAL);
        Instant now = Instant.now();
        Instant scheduledTime = now.plusSeconds(refreshInterval);
        if (nextValueTime.isBefore(scheduledTime)) {
            long delay = Duration.between(now, nextValueTime).getSeconds();
            if (delay > 0) {
                ScheduledFuture<?> ondiloBridgePollingJob = this.ondiloBridgePollingJob;
                if (ondiloBridgePollingJob != null) {
                    ondiloBridgePollingJob.cancel(true);
                }
                this.ondiloBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollOndiloICOs(), delay,
                        refreshInterval, TimeUnit.SECONDS);
                logger.trace("Rescheduled polling to {} (delay {} seconds)", nextValueTime, delay);
            }
        }
    }

    public @Nullable Instant pollOndiloICO(int id) {
        OndiloHandler ondiloHandler = getOndiloHandlerForPool(id);
        OndiloApiClient apiClient = this.apiClient;
        Instant lastValueTime = null;
        if (ondiloHandler != null && apiClient != null) {
            LastMeasure[] lastMeasures = apiClient.request("GET", "/pools/" + id
                    + "/lastmeasures?types[]=temperature&types[]=ph&types[]=orp&types[]=salt&types[]=tds&types[]=battery&types[]=rssi",
                    LastMeasure[].class);

            if (lastMeasures == null || lastMeasures.length == 0) {
                logger.warn("No lastMeasures available for Ondilo ICO with ID: {}", id);
                ondiloHandler.clearLastMeasuresChannels();
            } else {
                for (LastMeasure lastMeasure : lastMeasures) {
                    logger.trace("LastMeasure: type={}, value={}", lastMeasure.dataType, lastMeasure.value);
                    Instant valueTime = ondiloHandler.updateLastMeasuresChannels(lastMeasure);
                    if (lastValueTime == null || valueTime.isBefore(lastValueTime)) {
                        lastValueTime = valueTime;
                    }
                }
            }

            Recommendation[] recommendations = apiClient.request("GET", "/pools/" + id + "/recommendations",
                    Recommendation[].class);

            if (recommendations == null || recommendations.length == 0) {
                logger.trace("No Recommendations available for Ondilo ICO with ID: {}", id);
                ondiloHandler.clearRecommendationChannels();
            } else {
                Recommendation waitingRecommendation = null;
                for (Recommendation rec : recommendations) {
                    if (rec.status == Recommendation.Status.waiting) {
                        waitingRecommendation = rec;
                        break;
                    }
                }
                Recommendation recommendation;
                if (waitingRecommendation != null) {
                    logger.trace("Waiting Recommendation: id={}, title={}", waitingRecommendation.id,
                            waitingRecommendation.title);
                    recommendation = waitingRecommendation;
                } else {
                    recommendation = recommendations[0];
                    logger.trace("Latest Recommentation: id={}, title={}", recommendation.id, recommendation.title);
                }
                ondiloHandler.updateRecommendationChannels(recommendation);
            }

            PoolInfo poolInfo = apiClient.request("GET", "/pools/" + id + "/device", PoolInfo.class);
            if (poolInfo != null) {
                ondiloHandler.updatePoolInfo(poolInfo);
            }

            PoolConfiguration poolConfiguration = apiClient.request("GET", "/pools/" + id + "/configuration",
                    PoolConfiguration.class);
            if (poolConfiguration != null) {
                ondiloHandler.updatePoolConfiguration(poolConfiguration);
                ondiloHandler.updatePoolInfo(poolConfiguration);
            }
        } else {
            logger.debug("No OndiloHandler found for Ondilo ICO with ID: {}", id);
        }
        return lastValueTime;
    }

    public void validateRecommendation(int poolId, int recommendationId) {
        OndiloApiClient apiClient = this.apiClient;
        if (apiClient != null) {
            String response = apiClient.request("PUT", "/pools/" + poolId + "/recommendations/" + recommendationId,
                    String.class);
            if (response != null && "Done".equals(response.trim())) {
                logger.trace("Validated Recommendation successfully");
            } else {
                logger.warn("Failed to validate Recommendation, API response: {}", response);
            }
        } else {
            logger.warn("OndiloApiClient is not initialized, cannot validate recommendation");
        }
    }

    public @Nullable List<Pool> getPools() {
        return pools;
    }

    public void dispose() {
        stopOndiloBridgePolling();
        this.apiClient = null;
        logger.trace("OndiloBridge disposed and polling job stopped");
    }
}
