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
package org.openhab.binding.elroconnects.internal.handler;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsConnector;
import org.openhab.binding.elroconnects.internal.discovery.ElroConnectsBridgeDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link ElroConnectsAccountHandler} is the bridge handler responsible to for connecting the the ELRO Connects
 * cloud service and retrieving the defined K1 hubs.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsAccountHandler.class);

    private static final String ELRO_CLOUD_LOGIN_URL = "https://uaa-openapi.hekreu.me/login";
    private static final String ELRO_CLOUD_URL = "https://user-openapi.hekreu.me/device";
    private static final String ELRO_PID = "01288154146"; // ELRO Connects Enterprise PID on hekr cloud

    private static final int TIMEOUT_MS = 2500;
    private static final int INITIAL_DELAY_S = 5; // initial delay for polling to allow time for login and access token
                                                  // retrieval to succeed
    private static final int REFRESH_INTERVAL_S = 300;

    private volatile @Nullable ScheduledFuture<?> pollingJob;
    private final HttpClient client;

    private Gson gson = new Gson();
    private Type loginType = new TypeToken<Map<String, String>>() {
    }.getType();
    private Type deviceListType = new TypeToken<List<ElroConnectsConnector>>() {
    }.getType();

    private final Map<String, String> login = new HashMap<>();
    private String loginJson = "";
    private volatile @Nullable String accessToken;
    private volatile Map<String, ElroConnectsConnector> devices = new HashMap<>();

    public ElroConnectsAccountHandler(Bridge bridge, HttpClient client) {
        super(bridge);
        this.client = client;
        login.put("pid", ELRO_PID);
        login.put("clientType", "WEB");
    }

    @Override
    public void initialize() {
        ElroConnectsAccountConfiguration config = getConfigAs(ElroConnectsAccountConfiguration.class);
        String username = config.username;
        String password = config.password;

        if ((username == null) || username.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.no-username");
            return;
        }
        if ((password == null) || password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.no-password");
            return;
        }

        login.put("username", username);
        login.put("password", password);
        loginJson = gson.toJson(login);

        // Do initial login to retrieve access token and start polling with small initial delay, so the access token is
        // available
        scheduler.submit(this::login);
        startPolling();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed");
        stopPolling();
        super.dispose();
    }

    private void startPolling() {
        final ScheduledFuture<?> localRefreshJob = this.pollingJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            logger.debug("Start polling");
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY_S, REFRESH_INTERVAL_S,
                    TimeUnit.SECONDS);
        }
    }

    private void stopPolling() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Stop polling");
            localPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void poll() {
        logger.debug("Polling");

        // when access token not yet received or expired, login and get devices in next refresh cycle
        if (accessToken == null) {
            login();
            return;
        }

        getControllers().handle((devicesList, accountException) -> {
            if (devicesList != null) {
                logger.trace("deviceList response: {}", devicesList);

                List<ElroConnectsConnector> response = null;
                try {
                    response = gson.fromJson(devicesList, deviceListType);
                } catch (JsonParseException parseException) {
                    logger.warn("Parsing failed: {}", parseException.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.request-failed");
                    return null;
                }
                Map<String, ElroConnectsConnector> devices = new HashMap<>();
                if (response != null) {
                    response.forEach(d -> devices.put(d.getDevTid(), d));
                }
                this.devices = devices;
                updateStatus(ThingStatus.ONLINE);
            } else {
                if (accountException == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            accountException.getLocalizedMessage());
                }
            }

            return null;
        });
    }

    private void login() {
        logger.debug("Login");
        getAccessToken().handle((accessToken, accountException) -> {
            this.accessToken = accessToken;
            if (accessToken != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                if (accountException == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            accountException.getLocalizedMessage());
                }
            }

            return null;
        });
    }

    private CompletableFuture<@Nullable String> getAccessToken() {
        CompletableFuture<@Nullable String> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(ELRO_CLOUD_LOGIN_URL));

        request.method(HttpMethod.POST).content(new StringContentProvider(loginJson), "application/json")
                .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
                    @NonNullByDefault({})
                    @Override
                    public void onComplete(Result result) {
                        if (result.isSucceeded()) {
                            final HttpResponse response = (HttpResponse) result.getResponse();
                            if (response.getStatus() == 200) {
                                try {
                                    Map<String, String> content = gson.fromJson(getContentAsString(), loginType);
                                    String accessToken = (content != null) ? content.get("access_token") : null;
                                    f.complete(accessToken);
                                } catch (JsonParseException parseException) {
                                    logger.warn("Access token request response parsing failed: {}",
                                            parseException.getMessage());
                                    f.completeExceptionally(
                                            new ElroConnectsAccountException("@text/offline.request-failed"));
                                }
                            } else {
                                logger.warn("Unexpected response on access token request: {} - {}",
                                        response.getStatus(), getContentAsString());
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-failed"));
                            }
                        } else {
                            Throwable e = result.getFailure();
                            if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-timeout", e));
                            } else {
                                logger.warn("Access token request failed: {}", e);
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-failed", e));
                            }
                        }
                    }
                });

        return f;
    }

    private CompletableFuture<@Nullable String> getControllers() {
        CompletableFuture<@Nullable String> f = new CompletableFuture<>();
        Request request = client.newRequest(URI.create(ELRO_CLOUD_URL));

        request.method(HttpMethod.GET).header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeader.ACCEPT, "application/json").timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .send(new BufferingResponseListener() {
                    @NonNullByDefault({})
                    @Override
                    public void onComplete(Result result) {
                        if (result.isSucceeded()) {
                            final HttpResponse response = (HttpResponse) result.getResponse();
                            if (response.getStatus() == 200) {
                                f.complete(getContentAsString());
                            } else if (response.getStatus() == 401) {
                                // Access token expired, so clear it
                                accessToken = null;
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.token-expired"));
                            } else {
                                logger.warn("Unexpected response on get controllers request: {} - {}",
                                        response.getStatus(), getContentAsString());
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-failed"));
                            }
                        } else {
                            Throwable e = result.getFailure();
                            if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-timeout", e));
                            } else {
                                logger.warn("Get controllers request failed: {}", e);
                                f.completeExceptionally(
                                        new ElroConnectsAccountException("@text/offline.request-failed", e));
                            }
                        }
                    }
                });

        return f;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ElroConnectsBridgeDiscoveryService.class);
    }

    /**
     * @return connectors on the account from the ELRO Connects cloud API
     */
    public @Nullable Map<String, ElroConnectsConnector> getDevices() {
        return devices;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do, there are no channels
    }
}
