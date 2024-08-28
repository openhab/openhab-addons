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
package org.openhab.binding.hydrawise.internal.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hydrawise.internal.HydrawiseControllerListener;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.graphql.HydrawiseGraphQLClient;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Customer;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.QueryResponse;
import org.openhab.binding.hydrawise.internal.config.HydrawiseAccountConfiguration;
import org.openhab.binding.hydrawise.internal.discovery.HydrawiseCloudControllerDiscoveryService;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.config.core.Configuration;
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
 * The {@link HydrawiseAccountHandler} is responsible for handling for connecting to a Hydrawise account and polling for
 * controller data
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseAccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseAccountHandler.class);
    /**
     * Minimum amount of time we can poll for updates
     */
    private static final int MIN_REFRESH_SECONDS = 30;
    private static final int TOKEN_REFRESH_SECONDS = 60;
    private static final int WEATHER_REFRESH_MILLIS = 60 * 60 * 1000; // 1 hour
    private static final String BASE_URL = "https://app.hydrawise.com/api/v2/";
    private static final String AUTH_URL = BASE_URL + "oauth/access-token";
    private static final String CLIENT_SECRET = "zn3CrjglwNV1";
    private static final String CLIENT_ID = "hydrawise_app";
    private static final String SCOPE = "all";
    private final List<HydrawiseControllerListener> controllerListeners = Collections
            .synchronizedList(new ArrayList<>());
    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;
    private @Nullable OAuthClientService oAuthService;
    private @Nullable HydrawiseGraphQLClient apiClient;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> tokenFuture;
    private @Nullable Customer lastData;
    private long lastWeatherUpdate;
    private int refresh;

    public HydrawiseAccountHandler(final Bridge bridge, final HttpClient httpClient, final OAuthFactory oAuthFactory) {
        super(bridge);
        this.httpClient = httpClient;
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(getThing().toString(), AUTH_URL,
                AUTH_URL, CLIENT_ID, CLIENT_SECRET, SCOPE, false);
        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(this);
        this.apiClient = new HydrawiseGraphQLClient(httpClient, oAuthService);
        logger.debug("Handler initialized.");
        scheduler.schedule(() -> configure(oAuthService), 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
        clearTokenRefresh();
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
            oAuthFactory.ungetOAuthService(getThing().toString());
            this.oAuthService = null;
        }
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(getThing().toString());
        super.handleRemoval();
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        logger.debug("Auth Token Refreshed, expires in {}", tokenResponse.getExpiresIn());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HydrawiseCloudControllerDiscoveryService.class);
    }

    public void addControllerListeners(HydrawiseControllerListener listener) {
        this.controllerListeners.add(listener);
        Customer data = lastData;
        if (data != null) {
            listener.onData(data.controllers);
        }
    }

    public void removeControllerListeners(HydrawiseControllerListener listener) {
        synchronized (controllerListeners) {
            this.controllerListeners.remove(listener);
        }
    }

    public @Nullable HydrawiseGraphQLClient graphQLClient() {
        return apiClient;
    }

    public @Nullable Customer lastData() {
        return lastData;
    }

    public void refreshData(int delaySeconds) {
        initPolling(delaySeconds, this.refresh);
    }

    private void configure(OAuthClientService oAuthService) {
        HydrawiseAccountConfiguration config = getConfig().as(HydrawiseAccountConfiguration.class);
        try {
            if (!config.userName.isEmpty() && !config.password.isEmpty()) {
                if (!config.savePassword) {
                    Configuration editedConfig = editConfiguration();
                    editedConfig.remove("password");
                    updateConfiguration(editedConfig);
                }
                oAuthService.getAccessTokenByResourceOwnerPasswordCredentials(config.userName, config.password, SCOPE);
            } else if (oAuthService.getAccessTokenResponse() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Login credentials required.");
                return;
            }
            this.refresh = Math.max(config.refreshInterval, MIN_REFRESH_SECONDS);
            initPolling(0, refresh);
        } catch (OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (OAuthResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Login credentials required.");
        }
    }

    /**
     * Starts/Restarts polling with an initial delay. This allows changes in the poll cycle for when commands are sent
     * and we need to poll sooner then the next refresh cycle.
     */
    private synchronized void initPolling(int initalDelay, int refresh) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, initalDelay, refresh, TimeUnit.SECONDS);
    }

    /**
     * The API will randomly reject a request with a 401 not authorized, waiting a min and refreshing the token usually
     * fixes it
     */
    private synchronized void retryToken() {
        clearTokenRefresh();
        tokenFuture = scheduler.schedule(() -> {
            try {
                OAuthClientService oAuthService = this.oAuthService;
                if (oAuthService != null) {
                    oAuthService.refreshToken();
                    initPolling(0, refresh);
                }
            } catch (OAuthException | IOException | OAuthResponseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }, TOKEN_REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        clearFuture(pollFuture);
    }

    private void clearTokenRefresh() {
        clearFuture(tokenFuture);
    }

    private void clearFuture(@Nullable final ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private void poll() {
        poll(true);
    }

    private void poll(boolean retry) {
        HydrawiseGraphQLClient apiClient = this.apiClient;
        if (apiClient == null) {
            logger.debug("apiclient not initalized");
            return;
        }
        try {
            QueryResponse response = apiClient.queryControllers();
            if (response == null) {
                throw new HydrawiseConnectionException("Malformed response");
            }
            if (response.errors != null && !response.errors.isEmpty()) {
                throw new HydrawiseConnectionException(response.errors.stream().map(error -> error.message).reduce("",
                        (messages, message) -> messages + message + ". "));
            }
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime > lastWeatherUpdate + WEATHER_REFRESH_MILLIS) {
                lastWeatherUpdate = currentTime;
                try {
                    QueryResponse weatherResponse = apiClient.queryWeather();
                    if (weatherResponse != null) {
                        response.data.me.controllers.forEach(controller -> {
                            weatherResponse.data.me.controllers.stream().filter(c -> c.id.equals(controller.id))
                                    .findFirst().ifPresent(c -> controller.location.forecast = c.location.forecast);
                        });
                    }
                } catch (HydrawiseConnectionException e) {
                    logger.debug("Weather data is not supported", e);
                }
            }
            lastData = response.data.me;
            synchronized (controllerListeners) {
                controllerListeners.forEach(listener -> {
                    listener.onData(response.data.me.controllers);
                });
            }
        } catch (HydrawiseConnectionException e) {
            if (retry) {
                logger.debug("Retrying failed poll", e);
                poll(false);
            } else {
                logger.debug("Will try again during next poll period", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Token has been rejected, will try to refresh token in {} secs: {}", TOKEN_REFRESH_SECONDS,
                    e.getLocalizedMessage());
            clearPolling();
            retryToken();
        }
    }
}
