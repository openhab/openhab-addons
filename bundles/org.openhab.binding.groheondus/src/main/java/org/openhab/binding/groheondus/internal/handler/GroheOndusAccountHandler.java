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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.GroheOndusAccountConfiguration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.floriansw.ondus.api.OndusService;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusAccountHandler extends BaseBridgeHandler {

    private static final String STORAGE_KEY_REFRESH_TOKEN = "refreshToken";

    private final Logger logger = LoggerFactory.getLogger(GroheOndusAccountHandler.class);

    private Storage<String> storage;
    private @Nullable OndusService ondusService;
    private @Nullable ScheduledFuture<?> reloginFuture;

    public GroheOndusAccountHandler(Bridge bridge, Storage<String> storage) {
        super(bridge);
        this.storage = storage;
    }

    public OndusService getService() {
        OndusService ret = this.ondusService;
        if (ret == null) {
            throw new IllegalStateException("OndusService requested, which is null (UNINITIALIZED)");
        }
        return ret;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for bridge
    }

    @Override
    public void dispose() {
        if (ondusService != null) {
            ondusService = null;
        }
        if (reloginFuture != null) {
            reloginFuture.cancel(true);
        }
        super.dispose();
    }

    private void login() {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);
        if (config.username == null || config.password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/error.login.missing.credentials");
        } else {
            // Config appears to be ok, lets try
            try {
                OndusService ondusService;
                if (storage.containsKey(STORAGE_KEY_REFRESH_TOKEN)) {
                    try {
                        logger.debug("Trying to login using refresh token");
                        ondusService = OndusService.login(storage.get(STORAGE_KEY_REFRESH_TOKEN));
                    } catch (LoginException e) {
                        logger.debug("Refresh token invalid, try again with username and password");
                        ondusService = OndusService.loginWebform(config.username, config.password);
                    }
                } else {
                    logger.debug("No refresh token found, trying to log in using username and password");
                    ondusService = OndusService.loginWebform(config.username, config.password);
                }
                this.ondusService = ondusService;

                // Assuming everything went fine...
                Instant expiresAt = ondusService.authorizationExpiresAt();
                // Refresh 5 minutes before expiry
                Instant refreshTime = expiresAt.minus(5, ChronoUnit.MINUTES);
                final OndusService ondusServiceInner = ondusService;
                if (refreshTime.isAfter(Instant.now())) {
                    Duration durationUntilRefresh = Duration.between(Instant.now(), refreshTime);
                    reloginFuture = scheduler.schedule(() -> {
                        try {
                            logger.debug("Refreshing token");
                            this.storage.put(STORAGE_KEY_REFRESH_TOKEN, ondusServiceInner.refreshAuthorization());
                            logger.debug("Refreshed token, token expires at {}",
                                    ondusServiceInner.authorizationExpiresAt());
                        } catch (Exception e) {
                            logger.debug("Could not refresh token for GROHE ONDUS account, removing refresh token", e);
                            this.storage.remove(STORAGE_KEY_REFRESH_TOKEN);
                        }
                        login();
                    }, durationUntilRefresh.getSeconds(), TimeUnit.SECONDS);
                    logger.debug("Scheduled token refresh at {}", refreshTime);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    // Refresh time in the past (happens)
                    logger.debug("Refresh time for token was in the past, waiting a minute and retrying");
                    this.storage.remove(STORAGE_KEY_REFRESH_TOKEN);
                    reloginFuture = scheduler.schedule(this::login, 1, TimeUnit.MINUTES);
                }

            } catch (LoginException e) {
                logger.debug("Grohe api login failed", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.login.failed");
            } catch (IOException e) {
                logger.debug("Communication error while logging into the grohe api", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());

                // Cleanup and retry
                this.storage.remove(STORAGE_KEY_REFRESH_TOKEN);
                reloginFuture = scheduler.schedule(this::login, 1, TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public void initialize() {
        login();
    }
}
