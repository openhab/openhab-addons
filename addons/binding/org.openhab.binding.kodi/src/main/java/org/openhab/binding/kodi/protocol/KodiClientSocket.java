/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.protocol;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketTextListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;

public class KodiClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(KodiClientSocket.class);

    private static final ExecutorService threadpool = Executors.newFixedThreadPool(3);
    private static final int REQUEST_TIMEOUT_MS = 60000;

    private CountDownLatch commandLatch = null;
    private JsonObject commandResponse = null;
    private int nextMessageId = 1;

    private WebSocket webSocket;
    private final AsyncHttpClient client;
    private final WebSocketUpgradeHandler handler;
    private boolean connected = false;

    private final JsonParser parser = new JsonParser();
    private final Gson mapper = new Gson();
    private URI uri;
    private String userName;
    private String password;

    private final KodiClientSocketEventListener eventHandler;

    public KodiClientSocket(KodiClientSocketEventListener eventHandler, URI uri, String userName, String password) {
        this.eventHandler = eventHandler;
        this.uri = uri;
        this.userName = userName;
        this.password = password;
        this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(createAsyncHttpClientConfig()));

        this.handler = createWebSocketHandler();
    }

    /**
     * Attempts to create a connection to the kodi host and begin listening
     * for updates over the async http web socket
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public synchronized void open() throws IOException, InterruptedException, ExecutionException {
        if (isConnected()) {
            logger.warn("connect: connection is already open");
        }
        webSocket = client.prepareGet(uri.toString()).execute(handler).get();

    }

    private AsyncHttpClientConfig createAsyncHttpClientConfig() {
        Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setRealm(createRealm());
        builder.setRequestTimeout(REQUEST_TIMEOUT_MS);
        return builder.build();
    }

    private Realm createRealm() {
        Realm.RealmBuilder builder = new Realm.RealmBuilder();
        builder.setPrincipal(userName);
        builder.setPassword(password);
        builder.setUsePreemptiveAuth(true);
        builder.setScheme(AuthScheme.BASIC);
        return builder.build();
    }

    private WebSocketUpgradeHandler createWebSocketHandler() {
        WebSocketUpgradeHandler.Builder builder = new WebSocketUpgradeHandler.Builder();
        builder.addWebSocketListener(new KodiWebSocketListener());
        return builder.build();
    }

    /*
     * private AsyncHttpClientConfig createAsyncHttpClientConfig() {
     * Builder builder = new DefaultAsyncHttpClientConfig.Builder();
     * // builder.setRealm(createRealm());
     * builder.setRequestTimeout(REQUEST_TIMEOUT_MS);
     * return builder.build();
     * }
     */

    /*
     * private WebSocketUpgradeHandler createWebSocketHandler() {
     * WebSocketUpgradeHandler.Builder builder = new WebSocketUpgradeHandler.Builder();
     * builder.addWebSocketListener(new KodiWebSocketListener());
     * return builder.build();
     * }
     */

    /***
     * Close this connection to the kodi instance
     */
    public void close() {
        // if there is an old web socket then clean up and destroy
        if (webSocket != null) {
            try {
                webSocket.close();
            } catch (Exception e) {
                logger.error("Exception during closing the websocket ", e);
            }
            webSocket = null;
        }
    }

    public boolean isConnected() {
        if (webSocket == null || !webSocket.isOpen()) {
            return false;
        }

        return connected;
    }

    class KodiWebSocketListener implements WebSocketTextListener {

        @Override
        public void onOpen(WebSocket ws) {
            logger.debug("Connected to server");
            webSocket = ws;
            connected = true;
            if (eventHandler != null) {
                threadpool.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            eventHandler.onConnectionOpened();
                        } catch (Exception e) {
                            logger.error("Error handling onConnectionOpened()", e);
                        }

                    }
                });

            }
        }

        @Override
        public void onMessage(String message) {
            logger.debug("Message received from server:" + message);
            final JsonObject json = parser.parse(message).getAsJsonObject();
            if (json.has("id")) {
                logger.debug("Response received from server:" + json.toString());
                int messageId = json.get("id").getAsInt();
                if (messageId == nextMessageId - 1) {
                    commandResponse = json;
                    commandLatch.countDown();
                }

            } else {
                logger.debug("Event received from server:" + json.toString());
                try {
                    if (eventHandler != null) {

                        threadpool.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    eventHandler.handleEvent(json);
                                } catch (Exception e) {
                                    logger.error("Error handling event {} player state change message: {}", json, e);
                                }

                            }
                        });

                    }
                } catch (Exception e) {

                    logger.error("Error handling player state change message", e);
                }

            }
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof ConnectException) {
                logger.debug("[{}]: Websocket connection error '{}'", uri.toString(), e.getMessage());
            } else if (e instanceof TimeoutException) {
                logger.debug("[{}]: Websocket timeout error", uri.toString());
            } else {
                logger.error("[{}]: Websocket error: {}", uri.toString(), e.getMessage());
            }
        }

        @Override
        public void onClose(WebSocket webSocket) {
            webSocket = null;
            connected = false;
            logger.debug("Closing a WebSocket ");
            threadpool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        eventHandler.onConnectionClosed();
                    } catch (Exception e) {
                        logger.error("Error handling onConnectionClosed()", e);
                    }

                }
            });

        }

    }

    private void sendMessage(String str) throws Exception {
        if (isConnected()) {
            logger.debug("send message: " + str);
            webSocket.sendMessage(str);
        } else {
            throw new Exception("socket not initialized");
        }
    }

    public JsonElement callMethod(String methodName) {
        return callMethod(methodName, null);
    }

    public synchronized JsonElement callMethod(String methodName, JsonObject params) {
        try {
            JsonObject payloadObject = new JsonObject();
            payloadObject.addProperty("jsonrpc", "2.0");
            payloadObject.addProperty("id", nextMessageId);
            payloadObject.addProperty("method", methodName);

            if (params != null) {
                payloadObject.add("params", params);
            }

            String message = mapper.toJson(payloadObject);

            commandLatch = new CountDownLatch(1);
            commandResponse = null;
            nextMessageId++;

            sendMessage(message);
            if (commandLatch.await(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.debug("callMethod returns " + commandResponse.toString());
                return commandResponse.get("result");
            } else {
                logger.error("Timeout during callMethod({}, {})", methodName, params != null ? params.toString() : "");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error during callMethod", e);
            return null;
        }
    }
}
