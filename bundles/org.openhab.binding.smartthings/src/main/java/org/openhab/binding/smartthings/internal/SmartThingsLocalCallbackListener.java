/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsLocalCallbackListener} implements a simple HTTP server to handle the SmartThings OAuth
 * callback request.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Laurent Arnal - code adaptation
 */
@NonNullByDefault
public class SmartThingsLocalCallbackListener {

    private static final String CALLBACK_PATH = "/finish";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsLocalCallbackListener.class);

    private @Nullable ServerSocket callbackServerSocket;
    private @Nullable Thread callbackThread;

    @FunctionalInterface
    public interface ResponseHandlerListener {
        String handle(String path, @Nullable String query);
    }

    private @Nullable ResponseHandlerListener listener;

    public void setListener(ResponseHandlerListener listener) {
        this.listener = listener;
    }

    public boolean startCallbackListener() {
        stopCallbackListener();
        final ServerSocket listenerSocket;
        try {
            listenerSocket = new ServerSocket(61973);
            this.callbackServerSocket = listenerSocket;
        } catch (IOException e) {
            logger.warn("Failed to start OAuth callback listener on port 61973", e);
            return false;
        }

        Thread thread = new Thread(() -> {
            try (ServerSocket serverSocket = listenerSocket) {
                logger.debug("Started OAuth callback listener on port 61973");
                while (!serverSocket.isClosed()) {
                    try (Socket socket = serverSocket.accept();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                            OutputStream out = socket.getOutputStream()) {
                        String line = in.readLine();
                        if (line == null) {
                            continue;
                        }

                        // Simple HTTP parsing
                        String[] parts = line.split(" ");
                        if (parts.length < 2) {
                            continue;
                        }
                        String url = parts[1];

                        URI uri = new URI(url);
                        String path = Objects.requireNonNullElse(uri.getPath(), "");
                        String query = uri.getQuery();

                        if (!CALLBACK_PATH.equals(path)) {
                            writeResponse(out, "404 Not Found", "");
                            continue;
                        }

                        String responseBody = "";
                        ResponseHandlerListener responseHandler = listener;
                        if (responseHandler != null) {
                            responseBody = responseHandler.handle(path, query);
                        }
                        writeResponse(out, "200 OK", responseBody);
                    } catch (Exception e) {
                        if (!serverSocket.isClosed()) {
                            logger.error("Error in OAuth callback listener", e);
                        }
                    }
                }
            } catch (IOException e) {
                if (!listenerSocket.isClosed()) {
                    logger.warn("OAuth callback listener stopped unexpectedly", e);
                }
            } finally {
                if (listenerSocket.equals(callbackServerSocket)) {
                    callbackServerSocket = null;
                }
            }
        });
        thread.setName("SmartThings OAuth Callback Listener");
        thread.setDaemon(true);
        thread.start();
        this.callbackThread = thread;
        return true;
    }

    private void write(OutputStream out, String st) throws IOException {
        out.write(st.getBytes(StandardCharsets.UTF_8));
    }

    private void writeResponse(OutputStream out, String status, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

        write(out, "HTTP/1.1 " + status + "\r\n");
        write(out, "Content-Type: text/html; charset=UTF-8\r\n");
        write(out, "Content-Length: " + bytes.length + "\r\n");
        write(out, "\r\n");
        out.write(bytes);
        out.flush();
    }

    public void stopCallbackListener() {
        ServerSocket serverSocket = callbackServerSocket;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing callback server socket", e);
            }
            callbackServerSocket = null;
        }
        Thread thread = callbackThread;
        if (thread != null) {
            if (!thread.equals(Thread.currentThread())) {
                thread.interrupt();
            }
            callbackThread = null;
        }
    }
}
