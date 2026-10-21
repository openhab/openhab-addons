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
package org.openhab.binding.volvocars.internal.handler;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.volvocars.internal.api.VolvoCarsApi;
import org.openhab.binding.volvocars.internal.api.VolvoCarsException;
import org.openhab.binding.volvocars.internal.api.VolvoCarsOAuthClient;
import org.openhab.binding.volvocars.internal.config.AccountConfiguration;
import org.openhab.binding.volvocars.internal.discovery.VolvoCarsDiscoveryService;
import org.openhab.binding.volvocars.internal.servlet.VolvoCarsAuthServlet;
import org.openhab.core.storage.Storage;
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
 * The {@link AccountHandler} represents a Volvo ID account and the application registered on the
 * Volvo Cars developer portal. It handles the OAuth authorization and provides the API client to the
 * vehicle handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {
    private static final int RECONNECT_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final HttpClient httpClient;
    private final Storage<String> storage;
    private final VolvoCarsAuthServlet authServlet;

    private @Nullable VolvoCarsOAuthClient oAuthClient;
    private @Nullable VolvoCarsApi api;
    private @Nullable ScheduledFuture<?> reconnectJob;

    public AccountHandler(Bridge bridge, HttpClient httpClient, Storage<String> storage,
            VolvoCarsAuthServlet authServlet) {
        super(bridge);
        this.httpClient = httpClient;
        this.storage = storage;
        this.authServlet = authServlet;
    }

    @Override
    public void initialize() {
        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        if (!config.isComplete()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error.incomplete");
            return;
        }
        VolvoCarsOAuthClient localOAuthClient = new VolvoCarsOAuthClient(httpClient, storage,
                getThing().getUID().getAsString(), config);
        VolvoCarsApi localApi = new VolvoCarsApi(httpClient, localOAuthClient, config.apiKey);
        oAuthClient = localOAuthClient;
        api = localApi;
        authServlet.register(this);

        if (localOAuthClient.isAuthorized()) {
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(() -> verifyConnection(localApi));
        } else {
            setAuthorizationRequired();
        }
    }

    @Override
    public void dispose() {
        authServlet.unregister(this);
        cancelReconnectJob();
        api = null;
        oAuthClient = null;
    }

    @Override
    public void handleRemoval() {
        VolvoCarsOAuthClient localOAuthClient = oAuthClient;
        if (localOAuthClient != null) {
            localOAuthClient.clearTokens();
        }
        super.handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge has no channels
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(VolvoCarsDiscoveryService.class);
    }

    /**
     * @return the API client, or null if the bridge is not configured
     */
    public @Nullable VolvoCarsApi getApi() {
        return api;
    }

    /**
     * Starts a new authorization and returns the Volvo ID URL the user has to be redirected to.
     */
    public String createAuthorizationUrl() throws VolvoCarsException {
        VolvoCarsOAuthClient localOAuthClient = oAuthClient;
        if (localOAuthClient == null) {
            throw new VolvoCarsException("The account is not configured");
        }
        return localOAuthClient.createAuthorizationUrl();
    }

    public boolean isPendingAuthorization(String state) {
        VolvoCarsOAuthClient localOAuthClient = oAuthClient;
        return localOAuthClient != null && localOAuthClient.isPendingState(state);
    }

    public boolean isAuthorized() {
        VolvoCarsOAuthClient localOAuthClient = oAuthClient;
        return localOAuthClient != null && localOAuthClient.isAuthorized();
    }

    /**
     * Completes the authorization with the code Volvo ID passed to the redirect URI.
     */
    public void authorize(String code, String state) throws VolvoCarsException {
        VolvoCarsOAuthClient localOAuthClient = oAuthClient;
        VolvoCarsApi localApi = api;
        if (localOAuthClient == null || localApi == null) {
            throw new VolvoCarsException("The account is not configured");
        }
        localOAuthClient.authorize(code, state);
        logger.debug("Authorization of {} completed", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> verifyConnection(localApi));
    }

    /**
     * Called by vehicle handlers after a successful API call.
     */
    public void reportSuccess(VolvoCarsApi usedApi) {
        if (usedApi == api && getThing().getStatus() != ThingStatus.ONLINE) {
            cancelReconnectJob();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Called by vehicle handlers if an API call failed for a reason affecting the whole account.
     */
    public void reportFailure(VolvoCarsApi usedApi, VolvoCarsException e) {
        if (usedApi != api) {
            return;
        }
        if (e.isAuthorizationRequired()) {
            cancelReconnectJob();
            setAuthorizationRequired();
        } else {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleReconnect(usedApi);
        }
    }

    private void verifyConnection(VolvoCarsApi usedApi) {
        try {
            usedApi.getVehicles();
            reportSuccess(usedApi);
        } catch (VolvoCarsException e) {
            logger.debug("Connection check failed: {}", e.getMessage());
            reportFailure(usedApi, e);
        }
    }

    private void setAuthorizationRequired() {
        updateStatus(ThingStatusDetail.AUTHORIZATION_PENDING,
                "@text/offline.authorization-pending [\"" + VolvoCarsAuthServlet.SERVLET_PATH + "\"]");
    }

    /**
     * Periodically checks the connection until it succeeds, see {@link #reportSuccess(VolvoCarsApi)}.
     */
    private synchronized void scheduleReconnect(VolvoCarsApi usedApi) {
        if (reconnectJob == null) {
            reconnectJob = scheduler.scheduleWithFixedDelay(() -> verifyConnection(usedApi), RECONNECT_INTERVAL_MINUTES,
                    RECONNECT_INTERVAL_MINUTES, TimeUnit.MINUTES);
        }
    }

    private synchronized void cancelReconnectJob() {
        ScheduledFuture<?> job = reconnectJob;
        if (job != null) {
            job.cancel(false);
            reconnectJob = null;
        }
    }
}
