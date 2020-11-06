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
package org.openhab.binding.panasonictv.internal.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicEventReceiver} is responsible for
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class PanasonicEventReceiver extends Thread {
    private final Logger logger = LoggerFactory.getLogger(PanasonicEventReceiver.class);
    private final ServerSocket socketServer;
    private final Map<String, PanasonicEventListenerServiceImpl.ListenerObject> listeners;
    private @Nullable CompletableFuture<Boolean> stopFuture;

    public PanasonicEventReceiver(String localIp,
            Map<String, PanasonicEventListenerServiceImpl.ListenerObject> listeners) throws IOException {
        this.listeners = listeners;
        InetAddress inet4Address = Inet4Address.getByName(localIp);
        socketServer = new ServerSocket(0, 50, inet4Address);
    }

    public String getServerAddress() {
        return socketServer.getInetAddress().getHostAddress() + ":" + socketServer.getLocalPort();
    }

    @Override
    public void run() {
        while (!socketServer.isClosed() && stopFuture == null) {
            try (Socket socket = socketServer.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // wait for reader ready at max 500ms
                int msCounter = 0;
                while (msCounter < 500 && !reader.ready()) {
                    Thread.sleep(1);
                }
                if (!reader.ready()) {
                    reader.close();
                    throw new IOException("Timeout while trying to read input");
                }

                List<String> lines = reader.lines().collect(Collectors.toList());
                logger.trace("Received {}", lines);

                // TODO: find listener for processing
                reader.close();
            } catch (IOException | InterruptedException e) {
                logger.warn("processing incoming request failed: {}", e.getMessage());
            }
        }

        CompletableFuture<Boolean> stopFuture = this.stopFuture;
        if (socketServer.isClosed()) {
            logger.warn("Listening socket closed unintentionally.");
            if (stopFuture != null) {
                stopFuture.completeExceptionally(new IllegalStateException("Listening socket closed unintentionally."));
            }
        } else {
            try {
                socketServer.close();
            } catch (IOException e) {
                if (stopFuture != null) {
                    stopFuture.completeExceptionally(e);
                }
            }
            if (stopFuture != null) {
                stopFuture.complete(true);
            }
        }
    }

    public CompletableFuture<Boolean> requestStop() {
        CompletableFuture<Boolean> stopFuture = new CompletableFuture<>();
        this.stopFuture = stopFuture;
        return stopFuture;
    }
}
