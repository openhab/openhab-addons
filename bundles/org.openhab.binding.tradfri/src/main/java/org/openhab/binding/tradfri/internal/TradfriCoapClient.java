/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tradfri.internal;

import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TradfriCoapClient} provides some convenience features over the
 * plain {@link CoapClient} from californium.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TradfriCoapClient extends CoapClient {

    private static final long TIMEOUT = 2000;
    private static final int DEFAULT_DELAY_MILLIS = 600;
    private final Logger logger = LoggerFactory.getLogger(TradfriCoapClient.class);
    private final LinkedList<PayloadCallbackPair> commandsQueue = new LinkedList<>();
    private @Nullable Future<?> job;

    public TradfriCoapClient(URI uri) {
        super(uri);
        setTimeout(TIMEOUT);
    }

    private void executeCommands() {
        while (true) {
            try {
                synchronized (commandsQueue) {
                    if (commandsQueue.isEmpty()) {
                        return;
                    }
                    PayloadCallbackPair payloadCallbackPair = commandsQueue.poll();
                    logger.debug("CoAP PUT request\nuri: {}\npayload: {}", getURI(), payloadCallbackPair.payload);
                    put(new TradfriCoapHandler(payloadCallbackPair.callback), payloadCallbackPair.payload,
                            MediaTypeRegistry.TEXT_PLAIN);
                }
                Thread.sleep(DEFAULT_DELAY_MILLIS);
            } catch (InterruptedException e) {
                logger.debug("commandExecutorThread was interrupted", e);
            }
        }
    }

    /**
     * Starts observation of the resource and uses the given callback to provide updates.
     *
     * @param callback the callback to use for updates
     */
    public CoapObserveRelation startObserve(CoapCallback callback) {
        return observe(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result through a {@link CompletableFuture}.
     *
     * @return the future that will hold the result
     */
    public CompletableFuture<String> asyncGet() {
        logger.debug("CoAP GET request\nuri: {}", getURI());
        CompletableFuture<String> future = new CompletableFuture<>();
        get(new TradfriCoapHandler(future));
        return future;
    }

    /**
     * Asynchronously executes a GET on the resource and provides the result to a given callback.
     *
     * @param callback the callback to use for the response
     */
    public void asyncGet(CoapCallback callback) {
        logger.debug("CoAP GET request\nuri: {}", getURI());
        get(new TradfriCoapHandler(callback));
    }

    /**
     * Asynchronously executes a PUT on the resource with a payload, provides the result to a given callback
     * and blocks sending new requests to the resource for specified amount of milliseconds.
     *
     * @param payload the payload to send with the PUT request
     * @param callback the callback to use for the response
     * @param scheduler scheduler to be used for sending commands
     */
    public void asyncPut(String payload, CoapCallback callback, ScheduledExecutorService scheduler) {
        asyncPut(new PayloadCallbackPair(payload, callback), scheduler);
    }

    /**
     * Asynchronously executes a PUT on the resource with a payload and provides the result to a given callback.
     *
     * @param payloadCallbackPair object which holds the payload and callback process the PUT request
     * @param scheduler scheduler to be used for sending commands
     */
    public void asyncPut(PayloadCallbackPair payloadCallbackPair, ScheduledExecutorService scheduler) {
        synchronized (this.commandsQueue) {
            if (this.commandsQueue.isEmpty()) {
                this.commandsQueue.offer(payloadCallbackPair);
                final Future<?> job = this.job;
                if (job == null || job.isDone()) {
                    this.job = scheduler.submit(() -> executeCommands());
                }
            } else {
                this.commandsQueue.offer(payloadCallbackPair);
            }
        }
    }

    @Override
    public void shutdown() {
        if (job != null) {
            job.cancel(true);
        }

        super.shutdown();
    }

    public final class PayloadCallbackPair {
        public final String payload;
        public final CoapCallback callback;

        public PayloadCallbackPair(String payload, CoapCallback callback) {
            this.payload = payload;
            this.callback = callback;
        }
    }
}
