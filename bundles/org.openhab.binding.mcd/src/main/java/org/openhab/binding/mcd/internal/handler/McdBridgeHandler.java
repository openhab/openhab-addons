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
package org.openhab.binding.mcd.internal.handler;

import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.mcd.internal.util.Listener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link McdBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class McdBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(McdBridgeHandler.class);

    private @Nullable McdBridgeConfiguration config;

    private final HttpClient httpClient;
    private final Gson gson;

    private String accessToken = "";
    private int expiresIn;
    private @Nullable ScheduledFuture<?> future = null;

    private HashSet<Listener> listeners = new HashSet<>();

    public McdBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        gson = new Gson();
    }

    @Override
    public void initialize() {
        config = getConfigAs(McdBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::logMeIn);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localFuture = future;
        if (localFuture != null) {
            localFuture.cancel(true);
        }
    }

    public void register(Listener listener) {
        listeners.add(listener);
    }

    private void triggerEvent() {
        logger.debug("Event triggered");
        for (Listener l : listeners) {
            l.onEvent();
        }
    }

    /**
     * Uses the given credentials to log the user in.
     */
    protected void logMeIn() {
        logger.debug("Logging in...");
        McdBridgeConfiguration localConfig = config;
        if (localConfig != null) {
            try {
                Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/token")
                        .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                        .header(HttpHeader.ACCEPT, "application/json");
                String content = "grant_type=password&username=" + localConfig.getUserEmail() + "&password="
                        + localConfig.getUserPassword();
                request.content(new StringContentProvider(content), "application/x-www-form-urlencoded");
                request.send(new BufferingResponseListener() {
                    @NonNullByDefault({})
                    @Override
                    public void onComplete(Result result) {
                        String contentString = getContentAsString();
                        JsonObject content = gson.fromJson(contentString, JsonObject.class);
                        int responseCode = result.getResponse().getStatus();
                        switch (responseCode) {
                            case 200:
                                if (content != null && content.has("access_token")) {
                                    updateStatus(ThingStatus.ONLINE);
                                    accessToken = content.get("access_token").getAsString();
                                    expiresIn = content.get("expires_in").getAsInt();
                                    long delay = ((long) expiresIn) * 1000L - 60000L;
                                    Runnable task = () -> {
                                        logMeIn();
                                    };
                                    future = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
                                    getAccessToken();
                                    break;
                                } // else go to default
                            case 400:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "wrong credentials");
                                break;
                            case 0:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "please check your internet connection");
                                break;
                            default:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Login was not successful");
                        }
                        triggerEvent();
                    }
                });
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Should be used by C&S binding's things to obtain the bridge's access token
     * 
     * @return returns the access token as String
     */
    protected String getAccessToken() {
        return accessToken;
    }
}
