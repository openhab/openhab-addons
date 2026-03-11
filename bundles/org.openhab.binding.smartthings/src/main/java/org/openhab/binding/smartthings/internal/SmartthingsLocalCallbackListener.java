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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsLocalCallbackListener} : implemts a callback listener simple http server to handle oauth
 * callback request.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Laurent Arnal - code adaptation
 */
@NonNullByDefault
public class SmartthingsLocalCallbackListener {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsLocalCallbackListener.class);

    private @Nullable ServerSocket callbackServerSocket;
    private @Nullable Thread callbackThread;

    @FunctionalInterface
    public interface ResponseHandlerListener {
        String handle(String path, String query);
    }

    private @Nullable ResponseHandlerListener listener;

    public void setListener(ResponseHandlerListener listener) {
        this.listener = listener;
    }

    public void startCallbackListener() {
        stopCallbackListener();
        Thread thread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(61973)) {
                this.callbackServerSocket = serverSocket;
                logger.info("Started OAuth callback listener on port 61973");
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
                        String path = uri.getPath();
                        String query = uri.getQuery();

                        String responseBody = "";
                        if (listener != null) {
                            responseBody = listener.handle(path, query);
                        }
                        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

                        write(out, "HTTP/1.1 200 OK\r\n");
                        write(out, "Content-Type: text/html; charset=UTF-8\r\n");
                        write(out, "Content-Length: " + bytes.length + "\r\n");
                        write(out, "\r\n");
                        out.write(bytes);
                        out.flush();
                    } catch (Exception e) {
                        if (!serverSocket.isClosed()) {
                            logger.error("Error in OAuth callback listener", e);
                        }
                    }
                }
            } catch (IOException e) {
                if (callbackServerSocket != null) {
                    logger.error("Failed to start OAuth callback listener", e);
                }
            }
        });
        thread.setName("SmartThings OAuth Callback Listener");
        thread.setDaemon(true);
        thread.start();
        this.callbackThread = thread;
    }

    private void write(OutputStream out, String st) throws IOException {
        out.write(st.getBytes(StandardCharsets.UTF_8));
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
            thread.interrupt();
            callbackThread = null;
        }
    }
}
