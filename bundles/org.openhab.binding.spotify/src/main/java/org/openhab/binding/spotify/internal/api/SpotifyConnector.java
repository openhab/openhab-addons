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
package org.openhab.binding.spotify.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.spotify.internal.api.exception.SpotifyAuthorizationException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
import org.openhab.binding.spotify.internal.api.exception.SpotifyTokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Class to perform the actual call to the Spotify Api, interprets the returned Http status codes, and handles the error
 * codes returned by the Spotify Web Api.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class SpotifyConnector {

    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 10;
    private static final int HTTP_CLIENT_RETRY_COUNT = 5;

    private final Logger logger = LoggerFactory.getLogger(SpotifyConnector.class);

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructor.
     *
     * @param scheduler Scheduler to reschedule calls when rate limit exceeded or call not ready
     * @param httpClient http client to use to make http calls
     */
    public SpotifyConnector(ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
    }

    /**
     * Performs a call to the Spotify Web Api and returns the raw response. In there are problems this method can throw
     * a Spotify exception.
     *
     * @param requester The function to construct the request with http client that is passed as argument to the
     *            function
     * @param authorization The authorization string to use in the Authorization header
     * @return the raw reponse given
     */
    public ContentResponse request(Function<HttpClient, Request> requester, String authorization) {
        final Caller caller = new Caller(requester, authorization);

        try {
            return caller.call().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SpotifyException("Thread interrupted");
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else {
                throw new SpotifyException(e.getMessage(), e);
            }
        }
    }

    /**
     * Class to handle a call to the Spotify Web Api. In case of rate limiting or not finished jobs it will retry in a
     * specified time frame. It retries a number of times and then gives up with an exception.
     *
     * @author Hilbrand Bouwkamp - Initial contribution
     */
    private class Caller {
        private final Function<HttpClient, Request> requester;
        private final String authorization;

        private final CompletableFuture<ContentResponse> future = new CompletableFuture<>();
        private int delaySeconds;
        private int attempts;

        /**
         * Constructor.
         *
         * @param requester The function to construct the request with http client that is passed as argument to the
         *            function
         * @param authorization The authorization string to use in the Authorization header
         */
        public Caller(Function<HttpClient, Request> requester, String authorization) {
            this.requester = requester;
            this.authorization = authorization;
        }

        /**
         * Performs the request as a Future. It will set the Future state once it's finished. This method will be
         * scheduled again when the call is to be retried. The original caller should call the get method on the Future
         * to wait for the call to finish. The first try is not scheduled so if it succeeds on the first call the get
         * method directly returns the value.
         *
         * @return the Future holding the call
         */
        public CompletableFuture<ContentResponse> call() {
            attempts++;
            try {
                final boolean success = processResponse(
                        requester.apply(httpClient).header(AUTHORIZATION_HEADER, authorization)
                                .timeout(HTTP_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send());

                if (!success) {
                    if (attempts < HTTP_CLIENT_RETRY_COUNT) {
                        logger.debug("Spotify Web API call attempt: {}", attempts);

                        scheduler.schedule(this::call, delaySeconds, TimeUnit.SECONDS);
                    } else {
                        logger.debug("Giving up on accessing Spotify Web API. Check network connectivity!");
                        future.completeExceptionally(new SpotifyException(
                                "Could not reach the Spotify Web Api after " + attempts + " retries."));
                    }
                }
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    future.completeExceptionally(cause);
                } else {
                    future.completeExceptionally(e);
                }
            } catch (RuntimeException | TimeoutException e) {
                future.completeExceptionally(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            }
            return future;
        }

        /**
         * Processes the response of the Spotify Web Api call and handles the HTTP status codes. The method returns true
         * if the response indicates a successful and false if the call should be retried. If there were other problems
         * a Spotify exception is thrown indicating no retry should be done and the user should be informed.
         *
         * @param response the response given by the Spotify Web Api
         * @return true if the response indicated a successful call, false if the call should be retried
         */
        private boolean processResponse(ContentResponse response) {
            boolean success = false;

            logger.debug("Response Code: {}", response.getStatus());
            if (logger.isTraceEnabled()) {
                logger.trace("Response Data: {}", response.getContentAsString());
            }
            switch (response.getStatus()) {
                case OK_200:
                case CREATED_201:
                case NO_CONTENT_204:
                case NOT_MODIFIED_304:
                    future.complete(response);
                    success = true;
                    break;
                case ACCEPTED_202:
                    logger.debug(
                            "Spotify Web API returned code 202 - The request has been accepted for processing, but the processing has not been completed.");
                    future.complete(response);
                    success = true;
                    break;
                case BAD_REQUEST_400:
                    throw new SpotifyException(processErrorState(response));
                case UNAUTHORIZED_401:
                    throw new SpotifyAuthorizationException(processErrorState(response));
                case TOO_MANY_REQUESTS_429:
                    // Response Code 429 means requests rate limits exceeded.
                    final String retryAfter = response.getHeaders().get(RETRY_AFTER_HEADER);

                    logger.debug(
                            "Spotify Web API returned code 429 (rate limit exceeded). Retry After {} seconds. Decrease polling interval of bridge! Going to sleep...",
                            retryAfter);
                    delaySeconds = Integer.parseInt(retryAfter);
                    break;
                case FORBIDDEN_403:
                    // Process for authorization error, and logging.
                    processErrorState(response);
                    future.complete(response);
                    success = true;
                    break;
                case NOT_FOUND_404:
                    throw new SpotifyException(processErrorState(response));
                case SERVICE_UNAVAILABLE_503:
                case INTERNAL_SERVER_ERROR_500:
                case BAD_GATEWAY_502:
                default:
                    throw new SpotifyException("Spotify returned with error status: " + response.getStatus());
            }
            return success;
        }

        /**
         * Processes the responded content if the status code indicated an error. If the response could be parsed the
         * content error message is returned. If the error indicated a token or authorization error a specific exception
         * is thrown. If an error message is thrown the caller throws the appropriate exception based on the state with
         * which the error was returned by the Spotify Web Api.
         *
         * @param response content returned by Spotify Web Api
         * @return the error messages
         */
        private String processErrorState(ContentResponse response) {
            try {
                final JsonElement element = JsonParser.parseString(response.getContentAsString());

                if (element.isJsonObject()) {
                    final JsonObject object = element.getAsJsonObject();
                    if (object.has("error") && object.get("error").isJsonObject()) {
                        final String message = object.get("error").getAsJsonObject().get("message").getAsString();

                        // Bad request can be anything, from authorization problems to start play problems.
                        // Therefore authorization type errors are filtered and handled differently.
                        logger.debug("Bad request: {}", message);
                        if (message.contains("expired")) {
                            throw new SpotifyTokenExpiredException(message);
                        } else {
                            return message;
                        }
                    } else if (object.has("error_description")) {
                        final String errorDescription = object.get("error_description").getAsString();

                        throw new SpotifyAuthorizationException(errorDescription);
                    }
                }
                logger.debug("Unknown response: {}", response);
                return "Unknown response";
            } catch (JsonSyntaxException e) {
                logger.debug("Response was not json: ", e);
                return "Unknown response";
            }
        }
    }
}
