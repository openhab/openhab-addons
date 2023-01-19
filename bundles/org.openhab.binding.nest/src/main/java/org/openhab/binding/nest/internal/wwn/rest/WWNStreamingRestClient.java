/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.rest;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.KEEP_ALIVE_MILLIS;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.WWNUtils;
import org.openhab.binding.nest.internal.wwn.dto.WWNTopLevelData;
import org.openhab.binding.nest.internal.wwn.dto.WWNTopLevelStreamingData;
import org.openhab.binding.nest.internal.wwn.exceptions.FailedResolvingWWNUrlException;
import org.openhab.binding.nest.internal.wwn.handler.WWNRedirectUrlSupplier;
import org.openhab.binding.nest.internal.wwn.listener.WWNStreamingDataListener;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client that generates events based on Nest streaming WWN REST API Server-Sent Events (SSE).
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Replace polling with REST streaming
 */
@NonNullByDefault
public class WWNStreamingRestClient {

    // Assume connection timeout when 2 keep alive message should have been received
    private static final long CONNECTION_TIMEOUT_MILLIS = 2 * KEEP_ALIVE_MILLIS + KEEP_ALIVE_MILLIS / 2;

    public static final String AUTH_REVOKED = "auth_revoked";
    public static final String ERROR = "error";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String OPEN = "open";
    public static final String PUT = "put";

    private final Logger logger = LoggerFactory.getLogger(WWNStreamingRestClient.class);

    private final String accessToken;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final WWNRedirectUrlSupplier redirectUrlSupplier;
    private final ScheduledExecutorService scheduler;

    private final Object startStopLock = new Object();
    private final List<WWNStreamingDataListener> listeners = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> checkConnectionJob;
    private boolean connected;
    private @Nullable SseEventSource eventSource;
    private long lastEventTimestamp;
    private @Nullable WWNTopLevelData lastReceivedTopLevelData;

    public WWNStreamingRestClient(String accessToken, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory, WWNRedirectUrlSupplier redirectUrlSupplier,
            ScheduledExecutorService scheduler) {
        this.accessToken = accessToken;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.redirectUrlSupplier = redirectUrlSupplier;
        this.scheduler = scheduler;
    }

    private SseEventSource createEventSource() throws FailedResolvingWWNUrlException {
        Client client = clientBuilder.register(new WWNStreamingRequestFilter(accessToken)).build();
        SseEventSource eventSource = eventSourceFactory.newSource(client.target(redirectUrlSupplier.getRedirectUrl()));
        eventSource.register(this::onEvent, this::onError);
        return eventSource;
    }

    private void checkConnection() {
        long millisSinceLastEvent = System.currentTimeMillis() - lastEventTimestamp;
        if (millisSinceLastEvent > CONNECTION_TIMEOUT_MILLIS) {
            logger.debug("Check: Disconnected from streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
            synchronized (startStopLock) {
                stopCheckConnectionJob(false);
                if (connected) {
                    connected = false;
                    listeners.forEach(listener -> listener.onDisconnected());
                }
                redirectUrlSupplier.resetCache();
                reopenEventSource();
                startCheckConnectionJob();
            }
        } else {
            logger.debug("Check: Receiving streaming events, millisSinceLastEvent={}", millisSinceLastEvent);
        }
    }

    /**
     * Closes the existing EventSource and opens a new EventSource as workaround when the EventSource fails to reconnect
     * itself.
     */
    private void reopenEventSource() {
        try {
            logger.debug("Reopening EventSource");
            closeEventSource(10, TimeUnit.SECONDS);

            logger.debug("Opening new EventSource");
            SseEventSource localEventSource = createEventSource();
            localEventSource.open();

            eventSource = localEventSource;
        } catch (FailedResolvingWWNUrlException e) {
            logger.debug("Failed to resolve Nest redirect URL while opening new EventSource");
        }
    }

    public void start() {
        synchronized (startStopLock) {
            logger.debug("Opening EventSource and starting checkConnection job");
            reopenEventSource();
            startCheckConnectionJob();
            logger.debug("Started");
        }
    }

    public void stop() {
        synchronized (startStopLock) {
            logger.debug("Closing EventSource and stopping checkConnection job");
            stopCheckConnectionJob(true);
            closeEventSource(0, TimeUnit.SECONDS);
            logger.debug("Stopped");
        }
    }

    private void closeEventSource(long timeout, TimeUnit timeoutUnit) {
        SseEventSource localEventSource = eventSource;
        if (localEventSource != null) {
            if (!localEventSource.isOpen()) {
                logger.debug("Existing EventSource is already closed");
            } else if (localEventSource.close(timeout, timeoutUnit)) {
                logger.debug("Succesfully closed existing EventSource");
            } else {
                logger.debug("Failed to close existing EventSource");
            }
            eventSource = null;
        }
    }

    private void startCheckConnectionJob() {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob == null || localCheckConnectionJob.isCancelled()) {
            checkConnectionJob = scheduler.scheduleWithFixedDelay(this::checkConnection, CONNECTION_TIMEOUT_MILLIS,
                    KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopCheckConnectionJob(boolean mayInterruptIfRunning) {
        ScheduledFuture<?> localCheckConnectionJob = checkConnectionJob;
        if (localCheckConnectionJob != null && !localCheckConnectionJob.isCancelled()) {
            localCheckConnectionJob.cancel(mayInterruptIfRunning);
            checkConnectionJob = null;
        }
    }

    public boolean addStreamingDataListener(WWNStreamingDataListener listener) {
        return listeners.add(listener);
    }

    public boolean removeStreamingDataListener(WWNStreamingDataListener listener) {
        return listeners.remove(listener);
    }

    public @Nullable WWNTopLevelData getLastReceivedTopLevelData() {
        return lastReceivedTopLevelData;
    }

    private void onEvent(InboundSseEvent inboundEvent) {
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
                WWNTopLevelData topLevelData = WWNUtils.fromJson(data, WWNTopLevelStreamingData.class).getData();
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

    private void onError(Throwable error) {
        logger.debug("Error occurred while receiving events", error);
    }
}
