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

import static org.openhab.binding.mcd.internal.McdBindingConstants.*;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.*;
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
    private final Timer timer = new Timer();

    public McdBridgeHandler(Bridge bridge) {
        super(bridge);
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        config = getConfigAs(McdBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.BRIDGE_UNINITIALIZED, "initializing...");
        scheduler.execute(this::logMeIn);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handling command");
        if (LOGIN_STATUS.equals(channelUID.getId())) {
            try {
                httpClient.start();
                Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/Account")
                        .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                        .header(HttpHeader.ACCEPT, "application/json");
                request.send(new BufferingResponseListener() {
                    @NonNullByDefault({})
                    @Override
                    public void onComplete(Result result) {
                        if (result.getResponse().getStatus() == 200) {
                            updateStatus(ThingStatus.ONLINE);
                            updateState(LOGIN_STATUS, OnOffType.ON);
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                    "you are not logged in");
                            updateState(LOGIN_STATUS, OnOffType.OFF);
                            logMeIn();
                        }
                    }
                });
            } catch (Exception e) {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "an error occurred");
                updateState(LOGIN_STATUS, OnOffType.OFF);
                logger.warn("{}", e.getMessage());
                logMeIn();
            }
        } else {
            logger.warn("handleCommand: received unexpected command");
        }
    }

    @Override
    public void dispose() {
        timer.cancel();
    }

    /**
     * Uses the given credentials to log the user in.
     */
    protected void logMeIn() {
        if (config != null) {
            try {
                httpClient.start();
                Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/token")
                        .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                        .header(HttpHeader.ACCEPT, "application/json");
                String content = "grant_type=password&username=" + config.getUserEmail() + "&password="
                        + config.getUserPassword();
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
                                    updateState(LOGIN_STATUS, OnOffType.ON);

                                    accessToken = content.get("access_token").getAsString();
                                    expiresIn = content.get("expires_in").getAsInt();
                                    long delay = ((long) expiresIn) * 1000L - 60000L;
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            logMeIn();
                                        }
                                    }, delay);
                                    notifyAccessToken();
                                    break;
                                } // else go to default
                            case 400:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                        "wrong credentials");
                                updateState(LOGIN_STATUS, OnOffType.OFF);
                                break;
                            case 0:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                        "please check your internet connection");
                                updateState(LOGIN_STATUS, OnOffType.OFF);
                                break;
                            default:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                                        "Login was not successful");
                                updateState(LOGIN_STATUS, OnOffType.OFF);
                        }
                    }
                });
            } catch (Exception e) {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.COMMUNICATION_ERROR, "an error occurred");
                logger.warn("{}", e.getMessage());
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

    /**
     * This method is called in order to wait until the Bridge is initialized and the access token is obtained.
     */
    protected synchronized void waitForAccessToken() {
        while (accessToken.equals("")) {
            try {
                wait();
            } catch (Exception e) {
                logger.warn("Thread interrupted: {}", e.getMessage());
            }
        }
    }

    /**
     * This method calls {@link #notifyAll()} and is called when the access token is obtained in order to notify
     * {@link #waitForAccessToken()}.
     */
    protected synchronized void notifyAccessToken() {
        notifyAll();
    }
}
