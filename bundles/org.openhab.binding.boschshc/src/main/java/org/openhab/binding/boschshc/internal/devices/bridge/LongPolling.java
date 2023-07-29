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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.eclipse.jetty.http.HttpMethod.POST;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollError;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.LongPollingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the long polling to the Smart Home Controller.
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class LongPolling {

    private final Logger logger = LoggerFactory.getLogger(LongPolling.class);

    /**
     * gson instance to convert a class to json string and back.
     */
    private final Gson gson = new Gson();

    /**
     * Executor to schedule long polls.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Handler for long poll results.
     */
    private final Consumer<LongPollResult> handleResult;

    /**
     * Handler for unrecoverable.
     */
    private final Consumer<Throwable> handleFailure;

    /**
     * Current running long polling request.
     */
    private @Nullable Request request;

    /**
     * Indicates if long polling was aborted.
     */
    private boolean aborted = false;

    public LongPolling(ScheduledExecutorService scheduler, Consumer<LongPollResult> handleResult,
            Consumer<Throwable> handleFailure) {
        this.scheduler = scheduler;
        this.handleResult = handleResult;
        this.handleFailure = handleFailure;
    }

    public void start(BoschHttpClient httpClient) throws LongPollingFailedException {
        // Subscribe to state updates.
        String subscriptionId = this.subscribe(httpClient);
        this.executeLongPoll(httpClient, subscriptionId);
    }

    public void stop() {
        // Abort long polling.
        this.aborted = true;
        Request request = this.request;
        if (request != null) {
            request.abort(new AbortLongPolling());
            this.request = null;
        }
    }

    /**
     * Subscribe to events and store the subscription ID needed for long polling.
     *
     * @param httpClient Http client to use for sending subscription request
     * @return Subscription id
     */
    private String subscribe(BoschHttpClient httpClient) throws LongPollingFailedException {
        try {
            String url = httpClient.getBoschShcUrl("remote/json-rpc");
            JsonRpcRequest request = new JsonRpcRequest("2.0", "RE/subscribe",
                    new String[] { "com/bosch/sh/remote/*", null });
            logger.debug("Subscribe: Sending request: {} - using httpClient {}", request.toString(),
                    httpClient.toString());
            Request httpRequest = httpClient.createRequest(url, POST, request);
            SubscribeResult response = httpClient.sendRequest(httpRequest, SubscribeResult.class,
                    SubscribeResult::isValid, null);

            logger.debug("Subscribe: Got subscription ID: {} {}", response.getResult(), response.getJsonrpc());
            String subscriptionId = response.getResult();
            return subscriptionId;
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            throw new LongPollingFailedException(
                    String.format("Error on subscribe (Http client: %s): %s", httpClient.toString(), e.getMessage()),
                    e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LongPollingFailedException(
                    String.format("Interrupted subscribe (Http client: %s): %s", httpClient.toString(), e.getMessage()),
                    e);
        }
    }

    /**
     * Create a new subscription for long polling.
     *
     * @param httpClient Http client to send requests to
     */
    private void resubscribe(BoschHttpClient httpClient) {
        try {
            String subscriptionId = this.subscribe(httpClient);
            this.executeLongPoll(httpClient, subscriptionId);
        } catch (LongPollingFailedException e) {
            this.handleFailure.accept(e);
            return;
        }
    }

    private void executeLongPoll(BoschHttpClient httpClient, String subscriptionId) {
        scheduler.execute(() -> this.longPoll(httpClient, subscriptionId));
    }

    /**
     * Start long polling the home controller. Once a long poll resolves, a new one is started.
     */
    private void longPoll(BoschHttpClient httpClient, String subscriptionId) {
        logger.debug("Sending long poll request");

        JsonRpcRequest requestContent = new JsonRpcRequest("2.0", "RE/longPoll", new String[] { subscriptionId, "20" });
        String url = httpClient.getBoschShcUrl("remote/json-rpc");
        Request request = httpClient.createRequest(url, POST, requestContent);

        // Long polling responds after 20 seconds with an empty response if no update has happened.
        // 10 second threshold was added to not time out if response from controller takes a bit longer than 20 seconds.
        request.timeout(30, TimeUnit.SECONDS);

        this.request = request;
        LongPolling longPolling = this;
        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                // NOTE: This handler runs inside the HTTP thread, so we schedule the response handling in a new thread
                // because the HTTP thread is terminated after the timeout expires.
                scheduler.execute(() -> longPolling.onLongPollComplete(httpClient, subscriptionId, result,
                        this.getContentAsString()));
            }
        });
    }

    /**
     * This is the handler for responses of long poll requests.
     *
     * @param httpClient HTTP client which received the response
     * @param subscriptionId Id of subscription the response is for
     * @param result Complete result of the response
     * @param content Content of the response
     */
    private void onLongPollComplete(BoschHttpClient httpClient, String subscriptionId, @Nullable Result result,
            String content) {
        // Check if thing is still online
        if (this.aborted) {
            logger.debug("Canceling long polling for subscription id {} because it was aborted", subscriptionId);
            return;
        }

        // Check if response was failure or success
        Throwable failure = result != null ? result.getFailure() : null;
        if (failure != null) {
            if (failure instanceof ExecutionException) {
                if (failure.getCause() instanceof AbortLongPolling) {
                    logger.debug("Canceling long polling for subscription id {} because it was aborted",
                            subscriptionId);
                } else {
                    this.handleFailure.accept(new LongPollingFailedException(
                            "Unexpected exception during long polling request", failure));
                }
            } else {
                this.handleFailure.accept(
                        new LongPollingFailedException("Unexpected exception during long polling request", failure));
            }
        } else {
            logger.debug("Long poll response: {}", content);

            String nextSubscriptionId = subscriptionId;

            LongPollResult longPollResult = gson.fromJson(content, LongPollResult.class);
            if (longPollResult != null && longPollResult.result != null) {
                this.handleResult.accept(longPollResult);
            } else {
                logger.debug("Long poll response contained no result: {}", content);

                // Check if we got a proper result from the SHC
                LongPollError longPollError = gson.fromJson(content, LongPollError.class);

                if (longPollError != null && longPollError.error != null) {
                    logger.debug("Got long poll error: {} (code: {})", longPollError.error.message,
                            longPollError.error.code);

                    if (longPollError.error.code == LongPollError.SUBSCRIPTION_INVALID) {
                        logger.debug("Subscription {} became invalid, subscribing again", subscriptionId);
                        this.resubscribe(httpClient);
                        return;
                    }
                }
            }

            // Execute next run
            this.longPoll(httpClient, nextSubscriptionId);
        }
    }

    @SuppressWarnings("serial")
    private class AbortLongPolling extends BoschSHCException {
    }
}
