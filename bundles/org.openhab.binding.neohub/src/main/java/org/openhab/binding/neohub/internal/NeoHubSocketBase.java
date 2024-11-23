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
package org.openhab.binding.neohub.internal;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base abstract class for text based communication between openHAB and NeoHub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public abstract class NeoHubSocketBase implements Closeable {

    protected final NeoHubConfiguration config;
    protected final String hubId;

    private static final int REQUEST_INTERVAL_MILLISECS = 1000;
    private final Lock lock = new ReentrantLock(true);
    private Optional<Instant> lastRequestTime = Optional.empty();

    public NeoHubSocketBase(NeoHubConfiguration config, String hubId) {
        this.config = config;
        this.hubId = hubId;
    }

    /**
     * Sends the message over the network to the NeoHub and returns its response
     *
     * @param requestJson the message to be sent to the NeoHub
     * @return responseJson received from NeoHub
     * @throws IOException if there was a communication error or the socket state would not permit communication
     * @throws NeoHubException if the communication returned a response but the response was not valid JSON
     */
    public abstract String sendMessage(final String requestJson) throws IOException, NeoHubException;

    /**
     * Class for throttling requests to prevent overloading the hub.
     * <p>
     * The NeoHub can get confused if, while it is uploading data to the cloud, it also receives too many local
     * requests, so this method throttles the requests to one per REQUEST_INTERVAL_MILLISECS maximum.
     *
     * @throws InterruptedException if the wait is interrupted
     */
    protected class Throttler implements AutoCloseable {

        public Throttler() throws InterruptedException {
            lock.lock();
            long delay;
            synchronized (NeoHubSocketBase.this) {
                Instant now = Instant.now();
                delay = lastRequestTime
                        .map(t -> Math.max(0, Duration.between(now, t).toMillis() + REQUEST_INTERVAL_MILLISECS))
                        .orElse(0L);
                lastRequestTime = Optional.of(now.plusMillis(delay));
            }
            Thread.sleep(delay);
        }

        @Override
        public void close() {
            lock.unlock();
        }
    }
}
