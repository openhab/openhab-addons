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
package org.openhab.binding.gme.internal.handler;

import static org.openhab.binding.gme.internal.GmeBindingConstants.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gme.internal.api.GmeApiClient;
import org.openhab.binding.gme.internal.api.GmeApiException;
import org.openhab.binding.gme.internal.api.GmeAuthManager;
import org.openhab.binding.gme.internal.config.GmeApiConfiguration;
import org.openhab.binding.gme.internal.model.GmeCredentialTracker;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePasswordAge;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * Handler for a GME API account.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeApiBridgeHandler extends BaseBridgeHandler {

    private static final ZoneId GME_ZONE = ZoneId.of("Europe/Rome");
    private static final Set<String> MARKET_ZONES = Set.of("NORD", "CNOR", "CSUD", "SUD", "CALA", "SICI", "SARD");

    private final GmeApiClient apiClient;
    private final long initialAuthRetrySeconds;
    private final GmeCredentialTracker credentialTracker;
    private final GmePasswordAge passwordAge;
    private volatile @Nullable GmeAuthManager authManager;
    private volatile int refreshInterval = 60;
    private volatile String marketZone = "";
    private volatile GmeGranularity granularity = GmeGranularity.PT60;
    private @Nullable ScheduledFuture<?> passwordRefreshJob;
    private @Nullable ScheduledFuture<?> authenticationRetryJob;
    private static final long MAX_AUTH_RETRY_SECONDS = 300;
    private final AtomicLong lifecycleGeneration = new AtomicLong();
    private final Object lifecycleLock = new Object();

    public GmeApiBridgeHandler(Bridge bridge, HttpClient httpClient, Storage<String> storage) {
        this(bridge, httpClient, storage, Clock.system(GME_ZONE));
    }

    GmeApiBridgeHandler(Bridge bridge, HttpClient httpClient, Storage<String> storage, Clock clock) {
        this(bridge, new GmeApiClient(httpClient), storage, clock, 5);
    }

    GmeApiBridgeHandler(Bridge bridge, GmeApiClient apiClient, Storage<String> storage, Clock clock,
            long initialAuthRetrySeconds) {
        super(bridge);
        this.apiClient = apiClient;
        this.initialAuthRetrySeconds = initialAuthRetrySeconds;
        this.credentialTracker = new GmeCredentialTracker(storage, GME_ZONE, clock);
        this.passwordAge = new GmePasswordAge(GME_ZONE, clock);
    }

    @Override
    public void initialize() {
        long generation;
        synchronized (lifecycleLock) {
            generation = lifecycleGeneration.incrementAndGet();
            cancelPasswordRefresh();
            cancelAuthenticationRetry();
            authManager = null;
        }

        GmeApiConfiguration config = getConfigAs(GmeApiConfiguration.class);
        refreshInterval = Math.max(1, config.refreshInterval);
        marketZone = config.marketZone.trim().toUpperCase();

        try {
            granularity = GmeGranularity.fromApiValue(config.granularity);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.gme.invalid-granularity [\"" + e.getMessage() + "\"]");
            return;
        }

        if (!marketZone.isBlank() && !MARKET_ZONES.contains(marketZone)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.gme.invalid-market-zone [\"" + marketZone + "\"]");
            return;
        }

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.gme.credentials-missing");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> authenticate(generation, config, initialAuthRetrySeconds));
    }

    private void authenticate(long generation, GmeApiConfiguration config, long retrySeconds) {
        if (!isCurrentGeneration(generation)) {
            return;
        }
        GmeAuthManager manager = new GmeAuthManager(apiClient, config.username, config.password);
        try {
            manager.getToken();
            synchronized (lifecycleLock) {
                if (!isCurrentGeneration(generation)) {
                    return;
                }
                cancelAuthenticationRetry();
                authManager = manager;
                credentialTracker.updateAfterSuccessfulAuthentication(config.username, config.password,
                        config.initialPasswordChangedAt);
                updatePasswordChannels();
                schedulePasswordRefresh(generation);
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            synchronized (lifecycleLock) {
                if (isCurrentGeneration(generation)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/status.gme.authentication-interrupted");
                }
            }
        } catch (TimeoutException | ExecutionException | IllegalStateException e) {
            synchronized (lifecycleLock) {
                if (!isCurrentGeneration(generation)) {
                    return;
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.gme.authentication-failed [\"" + e.getMessage() + "\"]");
                cancelAuthenticationRetry();
                if (e instanceof GmeApiException apiError && apiError.getStatusCode() >= 400
                        && apiError.getStatusCode() < 500) {
                    return;
                }
                authenticationRetryJob = scheduler.schedule(
                        () -> authenticate(generation, config, Math.min(retrySeconds * 2, MAX_AUTH_RETRY_SECONDS)),
                        retrySeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void cancelAuthenticationRetry() {
        ScheduledFuture<?> job = authenticationRetryJob;
        if (job != null) {
            job.cancel(false);
            authenticationRetryJob = null;
        }
    }

    private void updatePasswordChannels() {
        Instant changedAt = credentialTracker.getChangedAt();
        if (changedAt == null) {
            return;
        }

        updateState(CHANNEL_PASSWORD_LAST_CHANGED, new DateTimeType(changedAt));
        updateState(CHANNEL_PASSWORD_EXPIRY, new DateTimeType(passwordAge.getExpiry(changedAt)));
        updateState(CHANNEL_PASSWORD_DAYS_REMAINING,
                new DecimalType(Long.toString(passwordAge.getDaysRemaining(changedAt))));
        updateState(CHANNEL_PASSWORD_STATUS, new StringType(passwordAge.getStatus(changedAt).name()));
    }

    private void schedulePasswordRefresh(long generation) {
        cancelPasswordRefresh();
        passwordRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
            if (isCurrentGeneration(generation)) {
                updatePasswordChannels();
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    private boolean isCurrentGeneration(long generation) {
        return lifecycleGeneration.get() == generation;
    }

    private void cancelPasswordRefresh() {
        ScheduledFuture<?> job = passwordRefreshJob;
        if (job != null) {
            job.cancel(true);
            passwordRefreshJob = null;
        }
    }

    @Override
    public void dispose() {
        synchronized (lifecycleLock) {
            lifecycleGeneration.incrementAndGet();
            cancelPasswordRefresh();
            cancelAuthenticationRetry();
            authManager = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public GmeApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable GmeAuthManager getAuthManager() {
        return authManager;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public String getMarketZone() {
        return marketZone;
    }

    public GmeGranularity getGranularity() {
        return granularity;
    }
}
