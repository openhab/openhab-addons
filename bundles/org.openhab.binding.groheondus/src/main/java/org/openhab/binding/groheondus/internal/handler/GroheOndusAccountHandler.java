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
package org.openhab.binding.groheondus.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.groheondus.internal.GroheOndusAccountConfiguration;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.api.GroheOndusAuthenticationException;
import org.openhab.binding.groheondus.internal.api.dto.TokenResponse;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates against the GROHE ONDUS cloud and keeps the access token fresh.
 *
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 * @author Arne Seime - Use own API client, asynchronous login and retry with backoff
 */
@NonNullByDefault
public class GroheOndusAccountHandler extends BaseBridgeHandler {

    private static final String STORAGE_KEY_REFRESH_TOKEN = "refreshToken";
    private static final long TOKEN_REFRESH_MARGIN_SECONDS = 300;
    private static final long MIN_TOKEN_REFRESH_DELAY_SECONDS = 30;
    private static final long[] RETRY_DELAYS_SECONDS = { 60, 120, 300, 600 };

    private final Logger logger = LoggerFactory.getLogger(GroheOndusAccountHandler.class);

    private final Storage<String> storage;
    private final GroheOndusApi api;

    private @Nullable ScheduledFuture<?> authenticationFuture;
    private int failedAttempts;
    // Incremented on initialize/dispose so that an authentication still running for an earlier lifecycle is ignored
    private int generation;

    public GroheOndusAccountHandler(Bridge bridge, Storage<String> storage, HttpClient httpClient,
            TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.storage = storage;
        this.api = new GroheOndusApi(httpClient, timeZoneProvider);
    }

    @Override
    public void initialize() {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);
        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.login.missing.credentials");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        synchronized (this) {
            generation++;
            failedAttempts = 0;
            scheduleAuthentication(0);
        }
    }

    @Override
    public void dispose() {
        synchronized (this) {
            generation++;
            ScheduledFuture<?> future = authenticationFuture;
            if (future != null) {
                future.cancel(true);
                authenticationFuture = null;
            }
        }
        api.clearAuthentication();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for bridge
    }

    /**
     * @return the API client, or null while the account is not authenticated
     */
    public @Nullable GroheOndusApi getApi() {
        return getThing().getStatus() == ThingStatus.ONLINE && api.isAuthenticated() ? api : null;
    }

    /**
     * Called when a request was rejected because the access token is no longer valid.
     */
    public synchronized void reauthenticate() {
        if (api.isAuthenticated()) {
            logger.debug("Access token rejected, re-authenticating");
            api.clearAuthentication();
            scheduleAuthentication(0);
        }
    }

    private synchronized void scheduleAuthentication(long delaySeconds) {
        ScheduledFuture<?> future = authenticationFuture;
        if (future != null) {
            // Do not interrupt: this may be called from the running authentication task itself
            future.cancel(false);
        }
        int scheduledGeneration = generation;
        authenticationFuture = scheduler.schedule(() -> authenticate(scheduledGeneration), delaySeconds,
                TimeUnit.SECONDS);
    }

    private void authenticate(int scheduledGeneration) {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);
        try {
            TokenResponse token = null;
            String refreshToken = storage.get(STORAGE_KEY_REFRESH_TOKEN);
            if (refreshToken != null) {
                try {
                    logger.debug("Authenticating using refresh token");
                    token = api.authenticateWithRefreshToken(refreshToken);
                } catch (GroheOndusAuthenticationException e) {
                    logger.debug("Refresh token rejected, logging in with username and password");
                    storage.remove(STORAGE_KEY_REFRESH_TOKEN);
                }
            }
            if (token == null) {
                logger.debug("Authenticating using username and password");
                token = api.authenticateWithCredentials(config.username, config.password);
            }
            synchronized (this) {
                if (scheduledGeneration != generation) {
                    return;
                }
                storage.put(STORAGE_KEY_REFRESH_TOKEN, token.refreshToken);
                failedAttempts = 0;
                long refreshDelay = Math.max(MIN_TOKEN_REFRESH_DELAY_SECONDS,
                        token.expiresIn - TOKEN_REFRESH_MARGIN_SECONDS);
                logger.debug("Authenticated, refreshing token in {}s", refreshDelay);
                scheduleAuthentication(refreshDelay);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (GroheOndusAuthenticationException e) {
            logger.debug("Login to GROHE ONDUS failed: {}", e.getMessage());
            synchronized (this) {
                if (scheduledGeneration != generation) {
                    return;
                }
                api.clearAuthentication();
            }
            // No automatic retry with rejected credentials; updating the configuration re-initializes the handler
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.login.failed");
        } catch (GroheOndusApiException e) {
            long retryDelay;
            synchronized (this) {
                if (scheduledGeneration != generation) {
                    return;
                }
                retryDelay = RETRY_DELAYS_SECONDS[Math.min(failedAttempts, RETRY_DELAYS_SECONDS.length - 1)];
                failedAttempts++;
                scheduleAuthentication(retryDelay);
            }
            logger.debug("Could not authenticate with GROHE ONDUS, retrying in {}s", retryDelay, e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
