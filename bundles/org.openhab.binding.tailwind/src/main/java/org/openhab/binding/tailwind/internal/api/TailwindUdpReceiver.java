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
package org.openhab.binding.tailwind.internal.api;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shares the UDP listeners for status reports between the controller Things, so that several controllers can send
 * their reports to the same port. Each Thing receives all reports of its port and ignores those of other controllers.
 *
 * If the requested port is taken by another program, a free port is used instead, so that status reports keep
 * working without any setup.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindUdpReceiver {
    private final Logger logger = LoggerFactory.getLogger(TailwindUdpReceiver.class);
    // Keyed by the requested port, which can differ from the port in use
    private final Map<Integer, Shared> listeners = new HashMap<>();

    private class Shared {
        final int requestedPort;
        final TailwindUdpListener listener;
        final Set<BiConsumer<InetAddress, String>> consumers = new CopyOnWriteArraySet<>();

        Shared(int requestedPort) {
            this.requestedPort = requestedPort;
            this.listener = new TailwindUdpListener("OH-binding-tailwind-udp-" + requestedPort, this::deliver);
        }

        private void deliver(InetAddress sender, String message) {
            for (BiConsumer<InetAddress, String> consumer : consumers) {
                consumer.accept(sender, message);
            }
        }
    }

    public class Registration {
        private final Shared shared;
        private final BiConsumer<InetAddress, String> consumer;

        private Registration(Shared shared, BiConsumer<InetAddress, String> consumer) {
            this.shared = shared;
            this.consumer = consumer;
        }

        // -1 if the listener stopped
        public int getPort() {
            return shared.listener.getPort();
        }

        public void close() {
            unregister(this);
        }
    }

    // Port 0 picks a free port
    public synchronized Registration register(int requestedPort, BiConsumer<InetAddress, String> consumer)
            throws SocketException {
        Shared shared = listeners.get(requestedPort);
        if (shared == null || shared.listener.getPort() == -1 || requestedPort == 0) {
            if (shared != null && requestedPort != 0) {
                // The socket of the old listener closed after an error, so open it again for all its Things
                shared.listener.stop();
            } else {
                shared = new Shared(requestedPort);
            }
            start(shared);
            if (requestedPort != 0) {
                listeners.put(requestedPort, shared);
            }
        }
        shared.consumers.add(consumer);
        return new Registration(shared, consumer);
    }

    private void start(Shared shared) throws SocketException {
        int port = shared.requestedPort;
        if (port == 0) {
            shared.listener.start(0);
            return;
        }
        try {
            shared.listener.start(port);
        } catch (SocketException e) {
            int freePort = shared.listener.start(0);
            logger.warn(
                    "UDP port {} is used by another program, so TailWind status reports are received on port {} instead. "
                            + "If a firewall blocks that port, door changes show up only every 5 minutes.",
                    port, freePort);
        }
    }

    private synchronized void unregister(Registration registration) {
        Shared shared = registration.shared;
        shared.consumers.remove(registration.consumer);
        if (shared.consumers.isEmpty()) {
            shared.listener.stop();
            listeners.remove(shared.requestedPort, shared);
        }
    }

    public synchronized void stop() {
        listeners.values().forEach(shared -> shared.listener.stop());
        listeners.clear();
    }
}
