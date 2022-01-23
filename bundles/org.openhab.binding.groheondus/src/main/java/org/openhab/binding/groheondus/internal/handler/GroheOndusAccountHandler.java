/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.groheondus.internal.AccountServlet;
import org.openhab.binding.groheondus.internal.GroheOndusAccountConfiguration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.service.http.HttpService;
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

    private HttpService httpService;
    private Storage<String> storage;
    private @Nullable AccountServlet accountServlet;
    private @Nullable OndusService ondusService;
    private @Nullable ScheduledFuture<?> refreshTokenFuture;

    public GroheOndusAccountHandler(Bridge bridge, HttpService httpService, Storage<String> storage) {
        super(bridge);
        this.httpService = httpService;
        this.storage = storage;
    }

    public OndusService getService() {
        OndusService ret = this.ondusService;
        if (ret == null) {
            throw new IllegalStateException("OndusService requested, which is null (UNINITIALIZED)");
        }
        return ret;
    }

    public void deleteRefreshToken() {
        this.storage.remove(STORAGE_KEY_REFRESH_TOKEN);
        this.initialize();

        if (refreshTokenFuture != null) {
            refreshTokenFuture.cancel(true);
        }
    }

    public void setRefreshToken(String refreshToken) {
        this.storage.put(STORAGE_KEY_REFRESH_TOKEN, refreshToken);
        this.login();
    }

    private void scheduleTokenRefresh() {
        if (ondusService != null) {
            Instant expiresAt = ondusService.authorizationExpiresAt();
            // Refresh 1 hour before expiry
            Duration between = Duration.between(Instant.now(), expiresAt.minus(1, ChronoUnit.HOURS));
            refreshTokenFuture = scheduler.schedule(() -> {
                OndusService ondusService = this.ondusService;
                if (ondusService == null) {
                    logger.warn("Trying to refresh Ondus account without a service being present.");
                    return;
                }
                try {
                    setRefreshToken(ondusService.refreshAuthorization());
                } catch (Exception e) {
                    logger.debug(
                            "Could not refresh authorization for GROHE ONDUS account, scheduling another attempt in 20 minutes. Keeping original token",
                            e);
                    // Initiate a new login
                    storage.remove(STORAGE_KEY_REFRESH_TOKEN);
                    scheduler.schedule(() -> login(), 20, TimeUnit.MINUTES);
                }
            }, between.getSeconds(), TimeUnit.SECONDS);
        }
    }

    public boolean hasRefreshToken() {
        return this.storage.containsKey(STORAGE_KEY_REFRESH_TOKEN);
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
        if (accountServlet != null) {
            accountServlet.dispose();
        }
        if (refreshTokenFuture != null) {
            refreshTokenFuture.cancel(true);
        }
        super.dispose();
    }

    private boolean login() {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);
        if (this.accountServlet == null) {
            this.accountServlet = new AccountServlet(httpService, this.getThing().getUID().getId(), this);
        }

        if ((config.username == null || config.password == null) && !this.hasRefreshToken()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/error.login.missing.credentials");
            return false;
        }

        try {
            if (storage.containsKey(STORAGE_KEY_REFRESH_TOKEN)) {
                ondusService = OndusService.login(storage.get(STORAGE_KEY_REFRESH_TOKEN));
            } else {
                // TODO: That's probably really inefficient, internally the loginWebform method acquires a refresh
                // token, maybe there should be a way to obtain this token here, somehow.
                ondusService = OndusService.loginWebform(config.username, config.password);
            }
            scheduleTokenRefresh();
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (LoginException e) {
            logger.debug("Grohe api login failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.login.failed");
        } catch (IOException e) {
            logger.debug("Communication error while logging into the grohe api", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return false;
    }

    @Override
    public void initialize() {
        if (!login()) {
            logger.debug("Could not log in, messages has been provided");
        }
    }
}
