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
package org.openhab.binding.webthing.internal.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;
import org.openhab.binding.webthing.internal.client.dto.PropertyStatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The WebsocketConnection implementation
 *
 * @author Gregor Roth - Initial contribution
 */
class WebSocketConnectionImpl implements WebSocketConnection {
    private final static PropertyChangedListener EMPTY_PROPERTY_CHANGED_LISTENER = new PropertyChangedListener() {
    };
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnectionImpl.class);
    private final ConsumedThing webThing;
    private final URI webSocketURI;
    private final WebSocket websocket;
    private final AtomicBoolean isOpen = new AtomicBoolean(true);
    private final ConnectionListener connectionListener;
    private final Map<String, PropertyChangedListener> propertyChangedListeners = new HashMap<>();
    private final AtomicReference<Instant> lastTimeReceived = new AtomicReference<>(Instant.now());
    private final AtomicReference<String> receivedTextbufferRef = new AtomicReference<>("");

    /**
     * constructor
     * 
     * @param webThing the associated Webthing
     * @param webSocketURI the webSocket uri
     * @param connectionListener the connection listener
     * @param pingPeriod the ping period to check the healthiness of the connection
     */
    WebSocketConnectionImpl(ConsumedThing webThing, URI webSocketURI, ConnectionListener connectionListener,
            Duration pingPeriod) {
        this.webThing = webThing;
        this.connectionListener = connectionListener;
        this.webSocketURI = webSocketURI;
        this.websocket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(webSocketURI, new WebSocketListener()).join();
        new ConnectionWatchDog(pingPeriod).start();
    }

    @Override
    public void observeProperty(@NotNull String propertyName, @NotNull PropertyChangedListener listener) {
        propertyChangedListeners.put(propertyName, listener);
    }

    @Override
    public void close() {
        close("");
    }

    private void close(String reason) {
        try {
            if (isOpen.getAndSet(false)) {
                logger.debug("websocket connection {} of {} closed. {}", webSocketURI,
                        webThing.getThingDescription().title, reason);
                connectionListener.onDisconnected(reason);
                websocket.abort();
            }
        } catch (Exception e) {
            logger.warn("error occurred by closing the WebSocket", e);
        }
    }

    private final class WebSocketListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            connectionListener.onConnected();
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            lastTimeReceived.set(Instant.now());
            return WebSocket.Listener.super.onPong(webSocket, message);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence event, boolean last) {
            try {
                lastTimeReceived.set(Instant.now());

                var message = receivedTextbufferRef.accumulateAndGet(event.toString(),
                        (received, current) -> received + current);
                if (last) {
                    receivedTextbufferRef.set("");
                    var propertyStatus = new Gson().fromJson(message, PropertyStatusMessage.class);
                    if (isOpen.get()) {
                        if (propertyStatus.messageType.equals("propertyStatus")) {
                            for (var propertyName : propertyStatus.data.keySet()) {
                                propertyChangedListeners.getOrDefault(propertyName, EMPTY_PROPERTY_CHANGED_LISTENER)
                                        .onPropertyValueChanged(webThing, propertyName,
                                                propertyStatus.data.get(propertyName));
                            }
                        } else {
                            logger.debug("Ignoring received message of unknown type: {}", event.toString());
                        }
                    }
                }
            } catch (JsonSyntaxException se) {
                logger.warn("received invalid message: {}", event.toString());
            }

            return WebSocket.Listener.super.onText(webSocket, event, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            close("websocket closed: " + reason);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            close("error notified. " + error.getMessage());
        }
    }

    private final class ConnectionWatchDog extends Thread {
        private final Duration pingPeriod;

        ConnectionWatchDog(Duration pingPeriod) {
            this.pingPeriod = pingPeriod;
            setDaemon(true);
        }

        @Override
        public void run() {
            // initial pause
            pause(pingPeriod);

            while (isOpen.get()) {
                // check if connection is alive (message has been received recently)
                var elapsedSinceLast = Duration.between(lastTimeReceived.get(), Instant.now());
                if (elapsedSinceLast.getSeconds() > pingPeriod.getSeconds()) {
                    close("connection seems to be broken (last message received at " + lastTimeReceived.get() + ", "
                            + elapsedSinceLast.getSeconds() + " sec ago)");
                    return;
                }
                pause(pingPeriod.dividedBy(2));

                // send ping
                if (isOpen.get()) {
                    try {
                        websocket.sendPing(ByteBuffer.wrap(Instant.now().toString().getBytes()));
                    } catch (Exception e) {
                        close("could not send ping. " + e.getMessage());
                        return;
                    }
                }
                pause(pingPeriod.dividedBy(2));
            }

            logger.debug("websocket of {} ({}) has been closed. terminating watchdog",
                    webThing.getThingDescription().title, webSocketURI);
        }

        private void pause(Duration delay) {
            if (isOpen.get()) {
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
}
