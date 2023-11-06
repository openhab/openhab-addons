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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.HttpUtil;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceDisconnectSseException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;
import org.openhab.binding.mielecloud.internal.webservice.retry.AuthorizationFailedRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An active or inactive SSE connection emitting a stream of events.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class SseConnection {
    private static final long CONNECTION_TIMEOUT = 30;
    private static final TimeUnit CONNECTION_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Logger logger = LoggerFactory.getLogger(SseConnection.class);

    private final String endpoint;
    private final SseRequestFactory requestFactory;
    private final ScheduledExecutorService scheduler;
    private final BackoffStrategy backoffStrategy;

    private final List<SseListener> listeners = new ArrayList<>();

    private boolean active = false;

    private int failedConnectionAttempts = 0;

    @Nullable
    private Request sseRequest;

    /**
     * Creates a new {@link SseConnection} to the given endpoint.
     *
     * Note: It is required to call {@link #connect()} in order to open the connection and start receiving events.
     *
     * @param endpoint The endpoint to connect to.
     * @param requestFactory Factory for creating requests.
     * @param scheduler Scheduler to run scheduled and concurrent tasks on.
     */
    public SseConnection(String endpoint, SseRequestFactory requestFactory, ScheduledExecutorService scheduler) {
        this(endpoint, requestFactory, scheduler, new ExponentialBackoffWithJitter());
    }

    /**
     * Creates a new {@link SseConnection} to the given endpoint.
     *
     * Note: It is required to call {@link #connect()} in order to open the connection and start receiving events.
     *
     * @param endpoint The endpoint to connect to.
     * @param requestFactory Factory for creating requests.
     * @param scheduler Scheduler to run scheduled and concurrent tasks on.
     * @param backoffStrategy Strategy for deriving the wait time between connection attempts.
     */
    SseConnection(String endpoint, SseRequestFactory requestFactory, ScheduledExecutorService scheduler,
            BackoffStrategy backoffStrategy) {
        this.endpoint = endpoint;
        this.requestFactory = requestFactory;
        this.scheduler = scheduler;
        this.backoffStrategy = backoffStrategy;
    }

    public synchronized void connect() {
        active = true;
        connectInternal();
    }

    private synchronized void connectInternal() {
        if (!active) {
            return;
        }

        Request runningRequest = this.sseRequest;
        if (runningRequest != null) {
            return;
        }

        logger.debug("Opening SSE connection...");
        Request sseRequest = createRequest();
        if (sseRequest == null) {
            logger.warn("Could not create SSE request, not opening SSE connection.");
            return;
        }

        final InputStreamResponseListener stream = new InputStreamResponseListener();
        SseStreamParser eventStreamParser = new SseStreamParser(stream.getInputStream(), this::onServerSentEvent,
                this::onSseStreamClosed);

        sseRequest = sseRequest
                .onResponseHeaders(
                        response -> scheduler.schedule(eventStreamParser::parseAndDispatchEvents, 0, TimeUnit.SECONDS))
                .onComplete(result -> onConnectionComplete(result));
        sseRequest.send(stream);
        this.sseRequest = sseRequest;
    }

    @Nullable
    private Request createRequest() {
        Request sseRequest = requestFactory.createSseRequest(endpoint);
        if (sseRequest == null) {
            return null;
        }

        return sseRequest.timeout(0, TimeUnit.SECONDS).idleTimeout(CONNECTION_TIMEOUT, CONNECTION_TIMEOUT_UNIT);
    }

    private synchronized void onSseStreamClosed(@Nullable Throwable exception) {
        if (exception != null && AuthorizationFailedRetryStrategy.JETTY_401_HEADER_BODY_MISMATCH_EXCEPTION_MESSAGE
                .equals(exception.getMessage())) {
            onConnectionError(ConnectionError.AUTHORIZATION_FAILED);
        } else if (exception instanceof TimeoutException) {
            onConnectionError(ConnectionError.TIMEOUT);
        } else {
            onConnectionError(ConnectionError.SSE_STREAM_ENDED);
        }
    }

    private synchronized void onConnectionComplete(@Nullable Result result) {
        sseRequest = null;

        if (result == null) {
            logger.warn("SSE stream was closed but there was no result delivered.");
            onConnectionError(ConnectionError.SSE_STREAM_ENDED);
            return;
        }

        Response response = result.getResponse();
        if (response == null) {
            logger.warn("SSE stream was closed without response.");
            onConnectionError(ConnectionError.SSE_STREAM_ENDED);
            return;
        }

        onConnectionClosed(response);
    }

    private void onConnectionClosed(Response response) {
        try {
            HttpUtil.checkHttpSuccess(response);
            onConnectionError(ConnectionError.SSE_STREAM_ENDED);
        } catch (AuthorizationFailedException e) {
            onConnectionError(ConnectionError.AUTHORIZATION_FAILED);
        } catch (TooManyRequestsException e) {
            long secondsUntilRetry = e.getSecondsUntilRetry();
            if (secondsUntilRetry < 0) {
                onConnectionError(ConnectionError.TOO_MANY_RERQUESTS);
            } else {
                onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, secondsUntilRetry);
            }
        } catch (MieleWebserviceTransientException e) {
            onConnectionError(e.getConnectionError(), 0);
        } catch (MieleWebserviceException e) {
            onConnectionError(e.getConnectionError());
        }
    }

    private void onConnectionError(ConnectionError connectionError) {
        onConnectionError(connectionError, backoffStrategy.getSecondsUntilRetry(failedConnectionAttempts));
    }

    private synchronized void onConnectionError(ConnectionError connectionError, long secondsUntilRetry) {
        if (!active) {
            return;
        }

        if (connectionError != ConnectionError.AUTHORIZATION_FAILED) {
            scheduleReconnect(secondsUntilRetry);
        }

        fireConnectionError(connectionError);
        failedConnectionAttempts++;
    }

    private void scheduleReconnect(long secondsUntilRetry) {
        long retryInSeconds = Math.max(backoffStrategy.getMinimumSecondsUntilRetry(),
                Math.min(secondsUntilRetry, backoffStrategy.getMaximumSecondsUntilRetry()));
        scheduler.schedule(this::connectInternal, retryInSeconds, TimeUnit.SECONDS);
        logger.debug("Scheduled reconnect attempt for Miele webservice to take place in {} seconds", retryInSeconds);
    }

    public synchronized void disconnect() {
        active = false;

        Request runningRequest = sseRequest;
        if (runningRequest == null) {
            logger.debug("SSE connection is not established, skipping SSE disconnect.");
            return;
        }

        logger.debug("Disconnecting SSE");
        runningRequest.abort(new MieleWebserviceDisconnectSseException());
        sseRequest = null;
        logger.debug("Disconnected");
    }

    private void onServerSentEvent(ServerSentEvent event) {
        failedConnectionAttempts = 0;
        listeners.forEach(l -> l.onServerSentEvent(event));
    }

    private void fireConnectionError(ConnectionError connectionError) {
        listeners.forEach(l -> l.onConnectionError(connectionError, failedConnectionAttempts));
    }

    public void addSseListener(SseListener listener) {
        listeners.add(listener);
    }
}
