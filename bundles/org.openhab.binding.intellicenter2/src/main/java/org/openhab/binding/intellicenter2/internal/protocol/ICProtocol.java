/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.intellicenter2.internal.protocol;

import static com.google.common.util.concurrent.Futures.getUnchecked;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.openhab.binding.intellicenter2.internal.protocol.Command.NOTIFY_LIST;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.IntelliCenter2Configuration;
import org.openhab.binding.intellicenter2.internal.model.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents how to communicate with the IntelliCenter control.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class ICProtocol implements AutoCloseable {

    public final static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Logger logger = LoggerFactory.getLogger(ICProtocol.class);

    private final IntelliCenter2Configuration config;
    private final SystemInfo systemInfo;

    @Nullable
    private ExecutorService writerService;
    @Nullable
    private ExecutorService readerService;
    @Nullable
    private ICRead readRunnable;

    private final AtomicReference<@Nullable Socket> clientSocket = new AtomicReference<>();
    private final AtomicReference<@Nullable BufferedOutputStream> out = new AtomicReference<>();
    private final AtomicReference<@Nullable BufferedReader> in = new AtomicReference<>();

    private final ConcurrentMap<String, ICRequest> requests = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, @Nullable SettableFuture<ICResponse>> responses = new ConcurrentHashMap<>(2);
    private final ConcurrentHashMap<String, List<NotifyListListener>> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService listenersNotifier;

    public ICProtocol(IntelliCenter2Configuration config) throws IOException {
        this.config = config;
        this.listenersNotifier = newSingleThreadExecutor(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ic-protocol-listeners-%s").build());
        connect();
        this.systemInfo = querySystemInfo();
    }

    private void connect() throws IOException {
        final Socket socket = new Socket(config.hostname, config.port);
        socket.setKeepAlive(true);
        this.clientSocket.set(socket);
        this.out.set(new BufferedOutputStream(new DataOutputStream(socket.getOutputStream())));
        this.in.set(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        this.writerService = newSingleThreadExecutor(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ic-protocol-writer-%s").build());
        var rs = newSingleThreadExecutor(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ic-protocol-reader-%s").build());
        this.readerService = rs;

        this.readRunnable = new ICRead();
        rs.submit(readRunnable);
    }

    private void reconnect() throws IOException {
        close();
        connect();
    }

    @Override
    public void close() throws IOException {
        var rr = readRunnable;
        if (rr != null) {
            rr.stop = true;
        }
        safeClose(out.get());
        safeClose(in.get());
        safeClose(clientSocket.get());
        try {
            final var ws = writerService;
            if (ws != null) {
                ws.shutdownNow();
            }
            final var rs = readerService;
            if (rs != null) {
                rs.shutdownNow();
            }
        } catch (Exception ignored) {
        }
    }

    private static void safeClose(@Nullable Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException ignored) {
        }
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    @SuppressWarnings("null")
    private SystemInfo querySystemInfo() {
        return new SystemInfo(getUnchecked(submit(SystemInfo.REQUEST)).getObjectList().get(0));
    }

    @SuppressWarnings("null")
    public synchronized void subscribe(NotifyListListener listener, RequestObject request) {
        subscriptions.putIfAbsent(request.getObjectName(), new CopyOnWriteArrayList<>());
        subscriptions.get(request.getObjectName()).add(listener);
        String response = getUnchecked(submit(ICRequest.requestParamList(request))).getResponse();
        if (!"200".equals(response)) {
            logger.warn("Error subscribing listener {} for request {}", listener, request);
        }
    }

    public synchronized void unsubscribe(NotifyListListener listener) {
        final List<String> keysToRemove = new ArrayList<>(1);
        subscriptions.forEach((k, v) -> {
            v.remove(listener);
            if (v.isEmpty()) {
                submit(ICRequest.releaseParamList(k));
                keysToRemove.add(k);
            }
        });
        keysToRemove.forEach(subscriptions::remove);
    }

    @SuppressWarnings("null")
    private void notifyListeners(final ICResponse response) {
        final var runnable = new Runnable() {

            @Override
            public void run() {
                response.getObjectList().forEach(r -> {
                    var listeners = subscriptions.get(r.getObjectName());
                    if (listeners != null) {
                        listeners.forEach(l -> l.onNotifyList(r));
                    }
                });
            }
        };
        listenersNotifier.submit(runnable);
    }

    /**
     * Submits a request to IntelliCenter2. Requests are single threaded to IntelliCenter2 to avoid overtaxing it.
     * 
     * @param request
     * 
     * @return a Future that will contain the eventual ICResponse
     */
    public Future<ICResponse> submit(ICRequest request) {
        var messageID = request.getMessageID();
        requests.put(messageID, request);
        final SettableFuture<ICResponse> future = SettableFuture.create();
        responses.put(messageID, future);
        var ws = writerService;
        if (ws == null) {
            throw new IllegalStateException("Null writerService.");
        }
        ws.submit(new ICWrite(request));
        return future;
    }

    private class ICWrite implements Runnable {

        private final ICRequest request;

        private ICWrite(ICRequest request) {
            this.request = request;
        }

        @Override
        public void run() {
            var jsonRequest = GSON.toJson(request);
            try {
                logger.trace("Sending request {}", jsonRequest);
                write(jsonRequest);
            } catch (IOException e) {
                throw new RuntimeException("Error writing request", e);
            }
        }

        /**
         * Write out bytes, handling reconnecting to the server if we get a SocketException
         *
         * @param jsonRequest
         * @throws IOException
         */
        private void write(String jsonRequest) throws IOException {
            var bytes = jsonRequest.getBytes(UTF_8);
            try {
                writeBytes(bytes);
            } catch (SocketException e) {
                reconnect();
                writeBytes(bytes);
            }
        }

        @SuppressWarnings("null")
        private void writeBytes(byte[] bytes) throws IOException {
            out.get().write(bytes);
            out.get().flush();
        }
    }

    private class ICRead implements Runnable {

        public volatile boolean stop = false;

        @Override
        public void run() {
            try {
                while (!stop) {
                    var response = readNext();
                    if (response == null) {
                        continue;
                    }
                    var future = responses.remove(response.getMessageID());
                    // if you use the iphone app to set a temperature, the existing iphone app doesn't appear to read
                    // back the response,
                    // so the response shows up for the next client read, which we want to ignore, unless it's a
                    // "NotifyList" response
                    // which is a notification that something changed based on a subscription.
                    if (future == null) {
                        if (NOTIFY_LIST.toString().equals(response.getCommand())) {
                            notifyListeners(response);
                        } else {
                            logger.debug("Got a non-NotifyList response without a corresponding messageID {}",
                                    response);
                        }
                    } else {
                        future.set(response);
                    }
                }
            } catch (Exception e) {
                if (!stop) {
                    logger.warn("Error reading from socket", e);
                }
            }
        }

        @Nullable
        private ICResponse readNext() throws IOException {
            var reader = in.get();
            if (reader == null) {
                return null;
            }
            var jsonResponse = reader.readLine();
            logger.trace("Received response {}", jsonResponse);
            if (jsonResponse == null) {
                return null;
            }
            return GSON.fromJson(jsonResponse, ICResponse.class);
        }
    }
}
