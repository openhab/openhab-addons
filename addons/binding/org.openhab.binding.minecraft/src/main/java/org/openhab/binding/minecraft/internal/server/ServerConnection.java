/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.server;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.minecraft.internal.discovery.MinecraftDiscoveryService;
import org.openhab.binding.minecraft.internal.message.OHMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firebase.tubesock.WebSocket;
import com.firebase.tubesock.WebSocketException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Holds information about connection to Minecraft server.
 *
 * @author Mattias Markehed
 */
public class ServerConnection {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);

    private String host;
    private int port;
    private ThingUID thingUID;
    private Gson gson = new GsonBuilder().create();

    private MinecraftSocketHandler socketHandler;

    private WebSocket webSocket;

    private ServerConnection(ThingUID thingUID, String host, int port) {
        this.host = host;
        this.port = port;
        this.thingUID = thingUID;
    }

    public class ServerData {
        public String host;
        public int port;
        public ServerConnection connection;

        public ServerData(String host, int port, ServerConnection connection) {
            this.host = host;
            this.port = port;
            this.connection = connection;
        }
    }

    /**
     * Get host address of server.
     *
     * @return server host address.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get port used to communicate to Minecraft server.
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Get UID of server
     *
     * @return server uid.
     */
    public ThingUID getThingUID() {
        return thingUID;
    }

    public void sendMessage(OHMessage message) {
        String serializedMessage = gson.toJson(message);
        webSocket.send(serializedMessage);
    }

    /**
     * Add the handler used to handle messages state updates web socket.
     *
     * @param handler the websocket handler to use for new connections.
     */
    private void setSocketHandler(MinecraftSocketHandler handler) {
        socketHandler = handler;
    }

    private void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    /**
     * Get the object used to handle state changes and messages from web socket.
     *
     * @return handler for web socket.
     */
    public MinecraftSocketHandler getSocketHandler() {
        return socketHandler;
    }

    /**
     * Directly connect to server.
     * Reconnects when connection is lost
     */
    public static Observable<ServerConnection> create(final ThingUID thingUID, final String host, final int port) {

        final String serverUrl = String.format("ws://%s:%d/stream", host, port);

        return Observable.<ServerConnection>create(new OnSubscribe<ServerConnection>() {

            @Override
            public void call(final Subscriber<? super ServerConnection> subscriber) {

                logger.info("Start connecting to Minecraft server at: {}", serverUrl);
                if (!subscriber.isUnsubscribed()) {

                    ServerConnection serverConnection = new ServerConnection(thingUID, host, port);
                    MinecraftSocketHandler socketHandler = new MinecraftSocketHandler() {
                        @Override
                        public void onError(WebSocketException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onClose() {
                            logger.info("Connection to Minecraft server stopped");
                            subscriber.onCompleted();
                        };
                    };

                    URI destUri = null;
                    try {
                        destUri = new URI(serverUrl);
                    } catch (URISyntaxException e) {
                        subscriber.onError(e);
                    }
                    final WebSocket websocket = new WebSocket(destUri);
                    websocket.setEventHandler(socketHandler);
                    websocket.connect();

                    serverConnection.setSocketHandler(socketHandler);
                    serverConnection.setWebSocket(websocket);
                    subscriber.onNext(serverConnection);

                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            subscriber.unsubscribe();
                            websocket.close();
                        }
                    }));
                }
            }
        });
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + ((socketHandler == null) ? 0 : socketHandler.hashCode());
        result = prime * result + ((thingUID == null) ? 0 : thingUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServerConnection other = (ServerConnection) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (socketHandler == null) {
            if (other.socketHandler != null) {
                return false;
            }
        } else if (!socketHandler.equals(other.socketHandler)) {
            return false;
        }
        if (thingUID == null) {
            if (other.thingUID != null) {
                return false;
            }
        } else if (!thingUID.equals(other.thingUID)) {
            return false;
        }
        return true;
    }
}
