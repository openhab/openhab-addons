/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client;

import static java.net.HttpURLConnection.*;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;
import static org.openhab.binding.homeconnect.internal.client.OkHttpHelper.formatJsonBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.listener.ServerSentEventListener;
import org.openhab.binding.homeconnect.internal.client.model.Event;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.LogWriter;
import org.openhab.binding.homeconnect.internal.logger.Type;
import org.slf4j.event.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Server-Sent-Events client for Home Connect API.
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class HomeConnectSseClient {

    private static final String TEXT_EVENT_STREAM = "text/event-stream";
    private static final String KEEP_ALIVE = "KEEP-ALIVE";
    private static final String EMPTY_EVENT = "\"\"";
    private static final String DISCONNECTED = "DISCONNECTED";
    private static final String CONNECTED = "CONNECTED";
    private static final int SSE_REQUEST_READ_TIMEOUT = 90;
    private static final String ACCEPT = "Accept";

    private final String apiUrl;
    private final OkSse oksse;
    private final OAuthClientService oAuthClientService;
    private final HashSet<ServerSentEventListener> eventListeners;
    private final HashMap<ServerSentEventListener, ServerSentEvent> serverSentEventConnections;
    private final LogWriter logger;

    public HomeConnectSseClient(OAuthClientService oAuthClientService, boolean simulated,
            EmbeddedLoggingService loggingService) {
        apiUrl = simulated ? API_SIMULATOR_BASE_URL : API_BASE_URL;
        oksse = new OkSse(OkHttpHelper.builder().readTimeout(SSE_REQUEST_READ_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).build());
        this.oAuthClientService = oAuthClientService;

        eventListeners = new HashSet<>();
        serverSentEventConnections = new HashMap<>();

        logger = loggingService.getLogger(HomeConnectSseClient.class);
    }

    /**
     * Register {@link ServerSentEventListener} to receive SSE events by Home Conncet API. This helps to reduce the
     * amount of request you would usually need to update all channels.
     *
     * Checkout rate limits of the API at. https://developer.home-connect.com/docs/general/ratelimiting
     *
     * @param eventListener
     * @throws CommunicationException
     * @throws AuthorizationException
     */
    public synchronized void registerServerSentEventListener(final String haId,
            final ServerSentEventListener eventListener) throws CommunicationException, AuthorizationException {
        logger.debugWithHaId(haId, "Register event listener: {}", eventListener);

        eventListeners.add(eventListener);

        if (!serverSentEventConnections.containsKey(eventListener)) {
            Request request = OkHttpHelper.requestBuilder(oAuthClientService)
                    .url(apiUrl + "/api/homeappliances/" + haId + "/events").header(ACCEPT, TEXT_EVENT_STREAM).build();

            logger.debugWithHaId(haId, "Create new listener");
            ServerSentEvent sse = oksse.newServerSentEvent(request, new ServerSentEvent.Listener() {

                @Override
                public void onOpen(@Nullable ServerSentEvent sse, @Nullable Response response) {
                    logger.debugWithHaId(haId, "Channel opened");
                }

                @Override
                public void onMessage(@Nullable ServerSentEvent sse, @Nullable String id, @Nullable String event,
                        @Nullable String message) {
                    if (KEEP_ALIVE.equals(event)) {
                        logger.debugWithHaId(haId, "KEEP-ALIVE");
                    } else {
                        logger.log(Type.DEFAULT, Level.DEBUG, haId, null, Arrays.asList(formatJsonBody(message)), null,
                                null, "Received id: {}, event: {}", id, event);
                    }

                    if (message != null && !StringUtils.isEmpty(message) && !EMPTY_EVENT.equals(message)) {
                        ArrayList<Event> events = mapToEvents(message, haId);
                        events.forEach(e -> eventListener.onEvent(e));
                    }

                    if (CONNECTED.equals(event) || DISCONNECTED.equals(event)) {
                        eventListener.onEvent(new Event(event, null, null));
                    }
                }

                @Override
                public void onComment(@Nullable ServerSentEvent sse, @Nullable String comment) {
                    logger.debugWithHaId(haId, "Comment received. comment: {}", comment);
                }

                @Override
                public boolean onRetryTime(@Nullable ServerSentEvent sse, long milliseconds) {
                    logger.debugWithHaId(haId, "Retry time {}", milliseconds);
                    return true; // True to use the new retry time received by SSE
                }

                @Override
                public boolean onRetryError(@Nullable ServerSentEvent sse, @Nullable Throwable throwable,
                        @Nullable Response response) {
                    boolean retry = true;

                    logger.warnWithHaId(haId, "Error: {}", throwable != null ? throwable.getMessage() : "");

                    if (response != null) {
                        if (response.code() == HTTP_FORBIDDEN) {
                            logger.warnWithHaId(haId,
                                    "Stopping listener! Got FORBIDDEN response from server. Please check if you allowed to access this device.");
                            retry = false;
                        } else if (response.code() == HTTP_UNAUTHORIZED) {
                            logger.errorWithHaId(haId, "Stopping listener! Access token became invalid.");
                            retry = false;
                        }

                        response.close();
                    }

                    if (!retry) {
                        eventListener.onReconnectFailed();

                        serverSentEventConnections.remove(eventListener);
                        eventListeners.remove(eventListener);
                    }

                    return retry;
                }

                @Override
                public void onClosed(@Nullable ServerSentEvent sse) {
                    logger.debugWithHaId(haId, "Closed");
                }

                @Override
                public @Nullable Request onPreRetry(@Nullable ServerSentEvent sse, @Nullable Request request) {
                    eventListener.onReconnect();

                    try {
                        return OkHttpHelper.requestBuilder(oAuthClientService)
                                .url(apiUrl + "/api/homeappliances/" + haId + "/events")
                                .header(ACCEPT, TEXT_EVENT_STREAM).build();
                    } catch (AuthorizationException | CommunicationException e) {
                        logger.debugWithHaId(haId, "Could not create new request. {}", e.getMessage());
                    }

                    return request;
                }
            });
            serverSentEventConnections.put(eventListener, sse);
        }
    }

    /**
     * Unregister {@link ServerSentEventListener}.
     *
     * @param eventListener
     */
    public synchronized void unregisterServerSentEventListener(ServerSentEventListener eventListener) {
        eventListeners.remove(eventListener);

        if (serverSentEventConnections.containsKey(eventListener)) {
            serverSentEventConnections.get(eventListener).close();
            serverSentEventConnections.remove(eventListener);
        }
    }

    public synchronized void dispose() {
        eventListeners.clear();

        serverSentEventConnections.forEach((key, value) -> value.close());
        serverSentEventConnections.clear();
    }

    private ArrayList<Event> mapToEvents(String json, String haId) {
        ArrayList<Event> events = new ArrayList<>();

        try {
            JsonObject responseObject = new JsonParser().parse(json).getAsJsonObject();
            JsonArray items = responseObject.getAsJsonArray("items");

            items.forEach(item -> {
                JsonObject obj = (JsonObject) item;
                String key = obj.get("key") != null ? obj.get("key").getAsString() : null;
                String value = obj.get("value") != null && !obj.get("value").isJsonNull()
                        ? obj.get("value").getAsString()
                        : null;
                String unit = obj.get("unit") != null ? obj.get("unit").getAsString() : null;

                events.add(new Event(key, value, unit));
            });
        } catch (IllegalStateException e) {
            logger.errorWithHaId(haId, "Could not parse event! {}", e.getMessage());
        }
        return events;
    }
}
