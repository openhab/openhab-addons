/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.groups.GroupContentResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the refreshing of the devices of a OJElectronics session
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public final class RefreshService implements AutoCloseable {

    private final OJElectronicsBridgeConfiguration config;
    private final Logger logger = LoggerFactory.getLogger(RefreshService.class);
    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    private final ScheduledExecutorService schedulerService;

    private @Nullable Runnable connectionLost;
    private @Nullable Consumer<@Nullable GroupContentResponseModel> refreshDone;
    private @Nullable ScheduledFuture<?> scheduler;
    private @Nullable Runnable unauthorized;
    private String sessionId = "";
    private static boolean destroyed = false;

    /**
     * Creates a new instance of {@link RefreshService}
     *
     * @param config Configuration of the bridge
     * @param httpClient HTTP client
     */
    public RefreshService(OJElectronicsBridgeConfiguration config, HttpClient httpClient,
            ScheduledExecutorService schedulerService) {
        this.config = config;
        this.httpClient = httpClient;
        this.schedulerService = schedulerService;
    }

    /**
     * Starts refreshing all thing values
     *
     * @param sessionId Session-Id
     * @param refreshDone This method is called if refreshing is done.
     * @param connectionLosed This method is called if no connection could established.
     * @param unauthorized This method is called if the result is unauthorized.
     */
    public void start(String sessionId, Consumer<@Nullable GroupContentResponseModel> refreshDone,
            Runnable connectionLost, Runnable unauthorized) {
        logger.trace("RefreshService.startService({})", sessionId);
        this.connectionLost = connectionLost;
        this.refreshDone = refreshDone;
        this.unauthorized = unauthorized;
        this.sessionId = sessionId;
        long refreshTime = config.refreshDelayInSeconds;
        scheduler = schedulerService.scheduleWithFixedDelay(this::refresh, refreshTime, refreshTime, TimeUnit.SECONDS);
        destroyed = false;
    }

    /**
     * Stops refreshing.
     */
    public void stop() {
        destroyed = true;
        final ScheduledFuture<?> scheduler = this.scheduler;
        if (scheduler != null) {
            scheduler.cancel(false);
        }
        this.scheduler = null;
    }

    private void refresh() {
        if (sessionId.equals("")) {
            handleConnectionLost();
            return;
        }
        final Runnable unauthorized = this.unauthorized;
        createRequest().send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (!destroyed) {
                    if (result == null || result.isFailed()) {
                        handleConnectionLost();
                    } else if (result.getResponse().getStatus() == HttpStatus.FORBIDDEN_403) {
                        if (unauthorized != null) {
                            unauthorized.run();
                        }
                    } else {
                        handleRefreshDone(getContentAsString());
                    }
                }
            }
        });
    }

    private Request createRequest() {
        Request request = httpClient.newRequest(config.apiUrl + "/Group/GroupContents").param("sessionid", sessionId)
                .param("APIKEY", config.apiKey).method(HttpMethod.GET);
        return request;
    }

    private void handleRefreshDone(String responseBody) {
        final Consumer<@Nullable GroupContentResponseModel> refreshDone = this.refreshDone;
        if (refreshDone != null) {
            logger.trace("refresh {}", responseBody);
            try {
                GroupContentResponseModel content = gson.fromJson(responseBody, GroupContentResponseModel.class);
                refreshDone.accept(content);
            } catch (JsonSyntaxException exception) {
                logger.error("Error mapping Result to model", exception);
                handleConnectionLost();
            }
        }
    }

    private void handleConnectionLost() {
        final Runnable connectionLost = this.connectionLost;
        if (connectionLost != null) {
            connectionLost.run();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
