/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snapcast.internal.data.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Main snapcast handler
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class SnapcastController {

    private final Logger logger = LoggerFactory.getLogger(SnapcastController.class);

    private final Gson gson = new Gson();
    private final Random requestId = new Random();

    private final HashMap<String, @Nullable HandlerRef<?>> notifyHandlers = new HashMap<>();
    private final HashMap<Integer, @Nullable HandlerRef<?>> responseHandlers = new HashMap<>();

    private final ServerController serverController;
    private final ClientController clientController;
    private final GroupController groupController;
    private final StreamController streamController;

    private @Nullable SocketHandler socketHandler;

    public SnapcastController() {
        serverController = new ServerController(this);
        clientController = new ClientController(this);
        groupController = new GroupController(this);
        streamController = new StreamController(this);
    }

    /**
     * open a connection to the snapcast server
     *
     * @param host host of the snapcast server
     * @param port port of the snapcast server
     */
    public void connect(String host, Integer port) {
        logger.debug("open snapcast connection");
        synchronized (this) {
            if (socketHandler == null) {
                SocketHandler sh = this.socketHandler = new SocketHandler(host, port);
                try {
                    sh.openSocket();
                } catch (IOException e) {
                    serverController.disconnected();
                }
                sh.start();
            }
        }
    }

    /**
     * close the connection to the snapcast server
     */
    public void dispose() {
        logger.debug("dispose snapcast connection");
        synchronized (this) {
            SocketHandler sh = this.socketHandler;
            if (sh != null) {
                this.socketHandler = null;
                sh.interrupt();
            }
        }
    }

    /**
     * Returns the server controller
     *
     * @return server controller
     */
    public ServerController serverController() {
        return serverController;
    }

    /**
     * Returns the client controller
     *
     * @return client controller
     */
    public ClientController clientController() {
        return clientController;
    }

    /**
     * Returns the group controller
     *
     * @return group controller
     */
    public GroupController groupController() {
        return groupController;
    }

    /**
     * Returns the stream controller
     *
     * @return stream controller
     */
    public StreamController streamController() {
        return streamController;
    }

    /**
     * Register a listener for a notification method
     *
     * @param method        Name of the method
     * @param notifyType    Type of the notification parameters
     * @param notifyHandler Handler for processing the notification
     */
    public <T> void registerNotifyListener(String method, Class<T> notifyType, Consumer<T> notifyHandler) {
        final HandlerRef<T> handlerRef = new HandlerRef<>(notifyType, notifyHandler);

        synchronized (notifyHandlers) {
            notifyHandlers.put(method, handlerRef);
        }
    }

    /**
     * Send a request to the snapcast server
     *
     * @param method          Name of the method
     * @param responseType    Type of the response parameters
     * @param responseHandler Handler for processing the response
     */
    public <T> void sendRequest(String method, Class<T> responseType, Consumer<T> responseHandler) {
        sendRequest(method, null, responseType, responseHandler);
    }

    /**
     * Send a request to the snapcast server
     *
     * @param method          Name of the method
     * @param params          Data to send
     * @param responseType    Type of the response parameters
     * @param responseHandler Handler for processing the response
     */
    public <T> void sendRequest(String method, @Nullable Identifiable params, Class<T> responseType,
            Consumer<T> responseHandler) {
        final HandlerRef<T> handlerRef = new HandlerRef<>(responseType, responseHandler, params);

        int id;
        synchronized (responseHandlers) {
            do {
                id = requestId.nextInt(Integer.MAX_VALUE);
            } while (responseHandlers.containsKey(id));
            responseHandlers.put(id, handlerRef);
        }

        OutputMessage<Object> msg = new OutputMessage<>();
        msg.id = id;
        msg.jsonrpc = "2.0";
        msg.method = method;
        msg.params = params;

        SocketHandler sh = this.socketHandler;
        if (sh != null) {
            sh.write(gson.toJson(msg));
        }
    }

    /**
     * Snapcast connection handler
     *
     * @author Steffen Brandemann - Initial contribution
     */
    private class SocketHandler extends Thread {

        private final String host;
        private final Integer port;

        private @Nullable Socket socket;

        public SocketHandler(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            boolean running = true;
            while (running && !isInterrupted()) {
                try {
                    final Socket socket;
                    synchronized (SocketHandler.this) {
                        socket = this.socket;
                    }
                    if (socket != null && socket.isConnected() && !socket.isClosed()) {
                        try {
                            String msg;
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            while ((msg = reader.readLine()) != null) {
                                parseInputMessage(msg);
                            }
                        } catch (SocketException e) {
                            final String message = e.getMessage();
                            if (message == null || !message.equals("Socket closed")) {
                                throw e;
                            }
                        } finally {
                            serverController.disconnected();
                            closeSocket();
                        }
                    } else {
                        try {
                            openSocket();
                        } catch (IOException e) {
                            Thread.sleep(5000);
                        }
                    }
                } catch (InterruptedException e) {
                    running = false;
                } catch (IOException e) {
                    logger.error("snapcast connection error", e);
                }
            }
        }

        @Override
        public void interrupt() {
            try {
                closeSocket();
            } catch (IOException e) {
            }
            super.interrupt();
        }

        /**
         * send the message
         *
         * @param msg the message
         */
        public void write(String msg) {
            logger.trace("send: {}", msg);
            byte[] data = (msg + "\r\n").getBytes();
            try {
                synchronized (SocketHandler.this) {
                    Socket socket = this.socket;
                    if (socket != null && socket.isConnected() && !socket.isClosed()) {
                        socket.getOutputStream().write(data);
                    }
                }
            } catch (IOException e) {
                logger.error("error while send message", e);
            }
        }

        /**
         * open the socket
         *
         * @throws IOException
         */
        public void openSocket() throws IOException {
            logger.debug("try to connect to {}:{}...", host, port);

            final Socket socket;
            synchronized (SocketHandler.this) {
                socket = this.socket = new Socket();
                socket.connect(new InetSocketAddress(host, port));
            }
            if (socket.isConnected()) {
                serverController.connected();
            }
        }

        /**
         * close the socket
         *
         * @throws IOException
         */
        public void closeSocket() throws IOException {
            final Socket socket;
            synchronized (SocketHandler.this) {
                socket = this.socket;
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            }
        }

        private void parseInputMessage(String msg) {
            logger.trace("recv: {}", msg);

            InputMessage json = gson.fromJson(msg, InputMessage.class);

            final String method;
            final Integer id;

            if ((method = json.method) != null && json.params != null) {
                callNotifyHandler(method, (@NonNull JsonElement) json.params);
            } else if ((id = json.id) != null && json.result != null) {
                callResponseHandler(id, (@NonNull JsonElement) json.result);
            }

        }

        private void callNotifyHandler(String method, JsonElement result) {
            HandlerRef<?> ref = null;
            synchronized (notifyHandlers) {
                ref = notifyHandlers.get(method);
            }
            if (ref != null) {
                ref.callHandler(result);
            }
        }

        private void callResponseHandler(Integer id, JsonElement result) {
            HandlerRef<?> ref = null;
            synchronized (responseHandlers) {
                ref = responseHandlers.remove(id);
            }
            if (ref != null) {
                ref.callHandler(result);
            }
        }

    }

    /**
     * @author Steffen Brandemann - Initial contribution
     */
    private class HandlerRef<T> {
        private final Class<T> responseType;
        private final Consumer<T> responseHandler;
        private final @Nullable String id;

        /**
         * @param responseType    Type of the response parameters
         * @param responseHandler Handler for processing the response
         */
        private HandlerRef(Class<T> responseType, Consumer<T> responseHandler) {
            this(responseType, responseHandler, null);
        }

        /**
         * @param responseType    Type of the response parameters
         * @param responseHandler Handler for processing the response
         * @param params
         */
        private HandlerRef(Class<T> responseType, Consumer<T> responseHandler, @Nullable Identifiable params) {
            this.responseType = responseType;
            this.responseHandler = responseHandler;
            this.id = (params != null ? params.getId() : null);
        }

        /**
         * Invoke the handler
         *
         * @param data Data for processing
         */
        private void callHandler(JsonElement data) {
            T response = gson.fromJson(data, responseType);
            if (response instanceof Identifiable) {
                Identifiable im = (Identifiable) response;
                if (im.getId() == null && this.id != null) {
                    im.setId(this.id);
                }
            }
            responseHandler.accept(response);
        }
    }

    /**
     * Data structure for incoming messages
     *
     * @author Steffen Brandemann - Initial contribution
     */
    @SuppressWarnings("unused")
    private static class InputMessage {
        private @Nullable Integer id;
        private @Nullable String jsonrpc;
        private @Nullable String method;
        private @Nullable JsonElement params;
        private @Nullable JsonElement result;
    }

    /**
     * Data structure for outgoing messages
     *
     * @author Steffen Brandemann - Initial contribution
     */
    @SuppressWarnings("unused")
    private static class OutputMessage<T> {
        private @Nullable Integer id;
        private @Nullable String jsonrpc;
        private @Nullable String method;
        private @Nullable T params;
    }
}
