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

import java.util.concurrent.Executors;
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
public final class RefreshService {

    private final OJElectronicsBridgeConfiguration config;
    private final Logger logger = LoggerFactory.getLogger(RefreshService.class);
    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private @Nullable Runnable connectionLosed;
    private @Nullable Consumer<@Nullable GroupContentResponseModel> refreshDone;
    private @Nullable ScheduledFuture<?> scheduler;
    private @Nullable Runnable unauthorized;
    private String sessionId = "";
    private static Boolean destroyed = false;

    /**
     * Creates a new instance of {@link RefreshService}
     *
     * @param config Configuration of the bridge
     * @param httpClient HTTP client
     */
    public RefreshService(OJElectronicsBridgeConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
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
            Runnable connectionLosed, Runnable unauthorized) {
        logger.trace("RefreshService.startService({})", sessionId);
        this.connectionLosed = connectionLosed;
        this.refreshDone = refreshDone;
        this.unauthorized = unauthorized;
        this.sessionId = sessionId;
        long refreshTime = config.refreshDelayInSeconds;
        scheduler = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> refresh(), refreshTime,
                refreshTime, TimeUnit.SECONDS);
        destroyed = false;
    }

    /**
     * Stops refreshing.
     */
    public void stop() {
        logger.trace("RefreshService.stopService({})", this);
        destroyed = true;
        if (scheduler != null) {
            scheduler.cancel(false);
            scheduler = null;
        }
    }

    private void refresh() {
        logger.trace("RefreshService.refresh({})", this);

        if (sessionId.equals("")) {
            handleConnectionLosed();
            return;
        }
        CreateRequest().send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (!destroyed) {
                    if (result == null || result.isFailed()) {
                        handleConnectionLosed();
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

    private Request CreateRequest() {
        Request request = httpClient.newRequest(config.apiUrl + "/Group/GroupContents").param("sessionid", sessionId)
                .param("APIKEY", config.APIKEY).method(HttpMethod.GET);
        return request;
    }

    private void handleRefreshDone(String responseBody) {
        if (refreshDone != null) {
            logger.trace("refresh {}", responseBody);

            try {
                GroupContentResponseModel content = gson.fromJson(responseBody, GroupContentResponseModel.class);
                if (refreshDone != null) {
                    refreshDone.accept(content);
                }
            } catch (JsonSyntaxException exception) {
                handleConnectionLosed();
            }
        }
    }

    private void handleConnectionLosed() {
        if (connectionLosed != null) {
            connectionLosed.run();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroyed = true;
        stop();
    }
}
