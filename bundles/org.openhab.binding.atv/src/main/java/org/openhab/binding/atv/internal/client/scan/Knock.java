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
package org.openhab.binding.atv.internal.client.scan;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Knocks on TCP ports to wake a sleeping device.
 *
 * <p>
 * Opens a TCP connection to one or more ports on a given host and immediately closes
 * it again. The use case is to wake a device sleeping via a Bonjour sleep proxy: such a
 * device is automatically woken up when any of its services is accessed, which this
 * module emulates. It runs concurrently with a unicast scan.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Knock {

    /**
     * Ports knocked during scanning, chosen because a device normally listens on one
     * of them.
     */
    public static final List<Integer> KNOCK_PORTS = List.of(3689, 7000, 49152, 32498);

    private static final Logger LOGGER = LoggerFactory.getLogger(Knock.class);

    /** Time the connection is kept open after connecting. */
    private static final Duration SLEEP_AFTER_CONNECT = Duration.ofMillis(100);

    /** Safety buffer subtracted from the timeout. */
    private static final Duration KNOCK_TIMEOUT_BUFFER = Duration.ofMillis(200);

    /** Interval between knock rounds when knocking continuously. */
    private static final Duration KNOCK_INTERVAL = Duration.ofSeconds(2);

    private Knock() {
    }

    /**
     * Knocks on a set of ports for a given host once. Each port is knocked
     * concurrently: connect, linger briefly, close. Connection errors are ignored (an
     * unreachable host aborts nothing here; {@link NoRouteToHostException} is simply
     * logged).
     *
     * @param address host to knock on
     * @param ports ports to knock on
     * @param timeout maximum time for the connection attempts
     * @return future completing when all knocks have finished
     */
    public static CompletableFuture<@Nullable Void> knock(InetAddress address, List<Integer> ports, Duration timeout) {
        Duration knockRuntime = timeout.minus(KNOCK_TIMEOUT_BUFFER);
        long connectTimeoutMs = Math.max(1, knockRuntime.toMillis());
        List<CompletableFuture<@Nullable Void>> tasks = new ArrayList<>();
        for (int port : ports) {
            CompletableFuture<@Nullable Void> task = new CompletableFuture<>();
            tasks.add(task);
            Thread.ofVirtual().name("atv-knock-" + address.getHostAddress() + ":" + port).start(() -> {
                LOGGER.debug("Knocking at port {} on {}", port, address);
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(address, port), (int) connectTimeoutMs);
                    Thread.sleep(SLEEP_AFTER_CONNECT.toMillis());
                } catch (IOException e) {
                    LOGGER.debug("Knock at port {} on {} failed: {}", port, address, e.toString());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    task.complete(null);
                }
            });
        }
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    /**
     * Continuously knocks on a set of ports. A new round of knocks is started every
     * two seconds, so a four second timeout results in two rounds. The returned future
     * can be cancelled to stop knocking early (e.g. when the accompanying scan has
     * finished).
     *
     * @param address host to knock on
     * @param ports ports to knock on
     * @param timeout total time to keep knocking
     * @return cancellable future completing when the timeout has expired
     */
    public static CompletableFuture<@Nullable Void> knocker(InetAddress address, List<Integer> ports,
            Duration timeout) {
        CompletableFuture<@Nullable Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("atv-knocker-" + address.getHostAddress()).start(() -> {
            long deadline = System.nanoTime() + timeout.toNanos();
            while (!result.isDone()) {
                long remainingMs = (deadline - System.nanoTime()) / 1_000_000;
                if (remainingMs <= 0) {
                    break;
                }
                knock(address, ports, Duration.ofMillis(Math.min(remainingMs, KNOCK_INTERVAL.toMillis())));
                try {
                    Thread.sleep(Math.min(remainingMs, KNOCK_INTERVAL.toMillis()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            result.complete(null);
        });
        return result;
    }
}
