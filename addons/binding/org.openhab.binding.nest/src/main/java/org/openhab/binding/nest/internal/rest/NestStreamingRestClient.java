/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.rest;

import static org.openhab.binding.nest.NestBindingConstants.KEEP_ALIVE_MILLIS;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.openhab.binding.nest.handler.NestRedirectUrlSupplier;
import org.openhab.binding.nest.internal.NestUtils;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.data.TopLevelStreamingData;
import org.openhab.binding.nest.internal.exceptions.FailedResolvingNestUrlException;
import org.openhab.binding.nest.internal.listener.NestStreamingDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that generates events based on Nest streaming REST API Server-Sent Events (SSE).
 *
 * @author Wouter Born - Replace polling with REST streaming
 */
@NonNullByDefault
public class NestStreamingRestClient {

    // Assume connection timeout when 2 keep alive message should have been received
    private static final long CONNECTION_TIMEOUT_MILLIS = 2 * KEEP_ALIVE_MILLIS + KEEP_ALIVE_MILLIS / 2;

    public static final String AUTH_REVOKED = "auth_revoked";
    public static final String ERROR = "error";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String OPEN = "open";
    public static final String PUT = "put";

    private final Logger logger = LoggerFactory.getLogger(NestStreamingRestClient.class);

    private final List<NestStreamingDataListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;

    private String accessToken;
    private @Nullable ScheduledFuture<?> checkConnectionJob;
    private boolean connected;
    private boolean openingEventSource;
    private @Nullable EventSource eventSource;
    private long lastEventTimestamp;
    private @Nullable TopLevelData lastReceivedTopLevelData;
    private NestRedirectUrlSupplier redirectUrlSupplier;

    public NestStreamingRestClient(String accessToken, NestRedirectUrlSupplier redirectUrlSupplier,
            ScheduledExecutorService scheduler) {
        this.accessToken = accessToken;
        this.redirectUrlSupplier = redirectUrlSupplier;
        this.scheduler = scheduler;
    }

    private EventSource createEventSource() throws FailedResolvingNestUrlException {
        SSLContext sslContext = SslConfigurator.newInstance().createSSLContext();
        Client client = ClientBuilder.newBuilder().sslContext(sslContext).register(SseFeature.class)
                .register(new NestStreamingRequestFilter(accessToken)).build();
        EventSource eventSource = new EventSource(client.target(redirectUrlSupplier.getRedirectUrl()), false);
        eventSource.register(this::onEvent);
        return eventSource;
    }

    private void checkConnection() {
        long millisSinceLastEvent = System.currentTimeMillis() - lastEventTimestamp;
        if (millisSinceLastEvent > CONNECTION_TIMEOUT_MILLIS) {
            logger.debug("Check: Disconnected from streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
            if (connected) {
                connected = false;
                listeners.forEach(listener -> listener.onDisconnected());
            }
            redirectUrlSupplier.resetCache();
            reopenEventSource();
        } else {
            logger.debug("Check: Receiving streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
        }
    }

    /**
     * Closes the existing EventSource and opens a new EventSource as workaround when the EventSource fails to reconnect
     * itself.
     */
    private void reopenEventSource() {
        if (openingEventSource) {
            logger.debug("EventSource is currently being opened");
            return;
        }

        synchronized (this) {
            try {
                logger.debug("Reopening EventSource");
                openingEventSource = true;

                if (eventSource != null) {
                    if (!eventSource.isOpen()) {
                        logger.debug("Existing EventSource is already closed");
                    } else if (eventSource.close(10, TimeUnit.SECONDS)) {
                        logger.debug("Succesfully closed existing EventSource");
                    } else {
                        logger.debug("Failed to close existing EventSource");
                    }
                    eventSource = null;
                }

                logger.debug("Opening new EventSource");
                eventSource = createEventSource();
                eventSource.open();
            } catch (FailedResolvingNestUrlException e) {
                logger.debug("Failed to resolve Nest redirect URL while opening new EventSource");
            } finally {
                openingEventSource = false;
            }
        }
    }

    public void start() {
        synchronized (this) {
            logger.debug("Opening EventSource and starting checkConnection job");
            reopenEventSource();

            if (checkConnectionJob == null || checkConnectionJob.isCancelled()) {
                checkConnectionJob = scheduler.scheduleWithFixedDelay(this::checkConnection, KEEP_ALIVE_MILLIS,
                        KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS);
            }
            logger.debug("Started");
        }
    }

    public void stop() {
        synchronized (this) {
            logger.debug("Closing EventSource and stopping checkConnection job");
            if (eventSource != null) {
                eventSource.close(0, TimeUnit.SECONDS);
            }
            if (checkConnectionJob != null && !checkConnectionJob.isCancelled()) {
                checkConnectionJob.cancel(true);
                checkConnectionJob = null;
            }
            logger.debug("Stopped");
        }
    }

    public boolean addStreamingDataListener(NestStreamingDataListener listener) {
        return listeners.add(listener);
    }

    public boolean removeStreamingDataListener(NestStreamingDataListener listener) {
        return listeners.remove(listener);
    }

    public @Nullable TopLevelData getLastReceivedTopLevelData() {
        return lastReceivedTopLevelData;
    }

    private void onEvent(InboundEvent inboundEvent) {
        try {
            lastEventTimestamp = System.currentTimeMillis();

            String name = inboundEvent.getName();
            String data = inboundEvent.readData();

            logger.debug("Received '{}' event, data: {}", name, data);

            if (!connected) {
                logger.debug("Connected to streaming events");
                connected = true;
                listeners.forEach(listener -> listener.onConnected());
            }

            if (AUTH_REVOKED.equals(name)) {
                logger.debug("API authorization has been revoked for access token: {}", data);
                listeners.forEach(listener -> listener.onAuthorizationRevoked(data));
            } else if (ERROR.equals(name)) {
                logger.warn("Error occurred: {}", data);
                listeners.forEach(listener -> listener.onError(data));
            } else if (KEEP_ALIVE.equals(name)) {
                logger.debug("Received message to keep connection alive");
            } else if (OPEN.equals(name)) {
                logger.debug("Event stream opened");
            } else if (PUT.equals(name)) {
                logger.debug("Data has changed (or initial data sent)");
                TopLevelData topLevelData = NestUtils.fromJson(data, TopLevelStreamingData.class).getData();
                lastReceivedTopLevelData = topLevelData;
                listeners.forEach(listener -> listener.onNewTopLevelData(topLevelData));
            } else {
                logger.debug("Received unhandled event with name '{}' and data '{}'", name, data);
            }
        } catch (Exception e) {
            // catch exceptions here otherwise they will be swallowed by the implementation
            logger.warn("An exception occurred while processing the inbound event", e);
        }
    }

}
