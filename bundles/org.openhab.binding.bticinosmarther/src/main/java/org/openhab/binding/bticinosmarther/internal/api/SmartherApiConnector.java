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
package org.openhab.binding.bticinosmarther.internal.api;

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
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherInvalidResponseException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherSubscriptionAlreadyExistsException;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherTokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@code SmartherApiConnector} class is used to perform the actual call to the API gateway.
 * It handles the returned http status codes and the error codes eventually returned by the API gateway itself.
 *
 * Response mappings:
 * <ul>
 * <li>Plants : 200, 204, 400, 401, 404, 408, 469, 470, 500</li>
 * <li>Topology : 200, 400, 401, 404, 408, 469, 470, 500</li>
 * <li>Measures : 200, 400, 401, 404, 408, 469, 470, 500</li>
 * <li>ProgramList : 200, 400, 401, 404, 408, 469, 470, 500</li>
 * <li>Get Status : 200, 400, 401, 404, 408, 469, 470, 500</li>
 * <li>Set Status : 200, 400, 401, 404, 408, 430, 469, 470, 486, 500</li>
 * <li>Get Subscriptions : 200, 204, 400, 401, 404, 500</li>
 * <li>Subscribe : 201, 400, 401, 404, 409, 500</li>
 * <li>Delete Subscription : 200, 400, 401, 404, 500</li>
 * </ul>
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherApiConnector {

    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SUBSCRIPTION_HEADER = "Ocp-Apim-Subscription-Key";

    private static final String ERROR_CODE = "statusCode";
    private static final String ERROR_MESSAGE = "message";
    private static final String TOKEN_EXPIRED = "expired";
    private static final String AUTHORIZATION_ERROR = "error_description";

    private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 10;
    private static final int HTTP_CLIENT_RETRY_COUNT = 5;

    // Set Chronothermostat Status > Wrong input parameters
    private static final int WRONG_INPUT_PARAMS_430 = 430;
    // Official application password expired: password used in the Thermostat official app is expired.
    private static final int APP_PASSWORD_EXPIRED_469 = 469;
    // Official application terms and conditions expired: terms and conditions for Thermostat official app are expired.
    private static final int APP_TERMS_EXPIRED_470 = 470;
    // Set Chronothermostat Status > Busy visual user interface
    private static final int BUSY_VISUAL_UI_486 = 486;

    private final Logger logger = LoggerFactory.getLogger(SmartherApiConnector.class);

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructs a {@code SmartherApiConnector} to the API gateway with the specified scheduler and http client.
     *
     * @param scheduler
     *            the scheduler to be used to reschedule calls when rate limit exceeded or call not succeeded
     * @param httpClient
     *            the http client to be used to make http calls to the API gateway
     */
    public SmartherApiConnector(ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
    }

    /**
     * Performs a call to the API gateway and returns the raw response.
     *
     * @param requester
     *            the function to construct the request, using the http client that is passed as argument to the
     *            function itself
     * @param subscription
     *            the subscription string to be used in the call {@code Subscription} header
     * @param authorization
     *            the authorization string to be used in the call {@code Authorization} header
     *
     * @return the raw response returned by the API gateway
     *
     * @throws {@link SmartherGatewayException}
     *             if the call failed due to an issue with the API gateway
     */
    public ContentResponse request(Function<HttpClient, Request> requester, String subscription, String authorization)
            throws SmartherGatewayException {
        final Caller caller = new Caller(requester, subscription, authorization);

        try {
            return caller.call().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SmartherGatewayException("Thread interrupted");
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();

            if (cause instanceof SmartherGatewayException) {
                throw (SmartherGatewayException) cause;
            } else {
                throw new SmartherGatewayException(e.getMessage(), e);
            }
        }
    }

    /**
     * The {@code Caller} class represents the handler to make calls to the API gateway.
     * In case of rate limiting or not finished jobs, it will retry a number of times in a specified timeframe then
     * gives up with an exception.
     *
     * @author Fabio Possieri - Initial contribution
     */
    private class Caller {
        private final Function<HttpClient, Request> requester;
        private final String subscription;
        private final String authorization;

        private final CompletableFuture<ContentResponse> future = new CompletableFuture<>();
        private int delaySeconds;
        private int attempts;

        /**
         * Constructs a {@code Caller} to the API gateway with the specified requester, subscription and authorization.
         *
         * @param requester
         *            the function to construct the request, using the http client that is passed as argument to the
         *            function itself
         * @param subscription
         *            the subscription string to be used in the call {@code Subscription} header
         * @param authorization
         *            the authorization string to be used in the call {@code Authorization} header
         */
        public Caller(Function<HttpClient, Request> requester, String subscription, String authorization) {
            this.requester = requester;
            this.subscription = subscription;
            this.authorization = authorization;
        }

        /**
         * Performs the request as a {@link CompletableFuture}, setting its state once finished.
         * The original caller should call the {@code get} method on the Future to wait for the call to finish.
         * The first attempt is not scheduled so, if the first call succeeds, the {@code get} method directly returns
         * the value. This method is rescheduled in case the call is to be retried.
         *
         * @return the {@link CompletableFuture} holding the call
         */
        public CompletableFuture<ContentResponse> call() {
            attempts++;
            try {
                final boolean success = processResponse(requester.apply(httpClient)
                        .header(SUBSCRIPTION_HEADER, subscription).header(AUTHORIZATION_HEADER, authorization)
                        .timeout(HTTP_CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).send());

                if (!success) {
                    if (attempts < HTTP_CLIENT_RETRY_COUNT) {
                        logger.debug("API Gateway call attempt: {}", attempts);

                        scheduler.schedule(this::call, delaySeconds, TimeUnit.SECONDS);
                    } else {
                        logger.debug("Giving up on accessing API Gateway. Check network connectivity!");
                        future.completeExceptionally(new SmartherGatewayException(
                                String.format("Could not reach the API Gateway after %s retries.", attempts)));
                    }
                }
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                future.completeExceptionally(cause != null ? cause : e);
            } catch (SmartherGatewayException e) {
                future.completeExceptionally(e);
            } catch (RuntimeException | TimeoutException e) {
                future.completeExceptionally(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            }
            return future;
        }

        /**
         * Processes the response from the API gateway call and handles the http status codes.
         *
         * @param response
         *            the response content returned by the API gateway
         *
         * @return {@code true} if the call was successful, {@code false} if the call failed in a way that can be
         *         retried
         *
         * @throws {@link SmartherGatewayException}
         *             if the call failed due to an irrecoverable issue and cannot be retried (user should be informed)
         */
        private boolean processResponse(ContentResponse response) throws SmartherGatewayException {
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
                            "API Gateway returned error status 202 (the request has been accepted for processing, but the processing has not been completed)");
                    future.complete(response);
                    success = true;
                    break;

                case FORBIDDEN_403:
                    // Process for authorization error, and logging.
                    processErrorState(response);
                    future.complete(response);
                    success = true;
                    break;

                case BAD_REQUEST_400:
                case NOT_FOUND_404:
                case REQUEST_TIMEOUT_408:
                case WRONG_INPUT_PARAMS_430:
                case APP_PASSWORD_EXPIRED_469:
                case APP_TERMS_EXPIRED_470:
                case BUSY_VISUAL_UI_486:
                case INTERNAL_SERVER_ERROR_500:
                    throw new SmartherGatewayException(processErrorState(response));

                case UNAUTHORIZED_401:
                    throw new SmartherAuthorizationException(processErrorState(response));

                case CONFLICT_409:
                    // Subscribe to C2C notifications > Subscription already exists.
                    throw new SmartherSubscriptionAlreadyExistsException(processErrorState(response));

                case TOO_MANY_REQUESTS_429:
                    // Response Code 429 means requests rate limits exceeded.
                    final String retryAfter = response.getHeaders().get(RETRY_AFTER_HEADER);
                    logger.debug(
                            "API Gateway returned error status 429 (rate limit exceeded - retry after {} seconds, decrease polling interval of bridge, going to sleep...)",
                            retryAfter);
                    delaySeconds = Integer.parseInt(retryAfter);
                    break;

                case BAD_GATEWAY_502:
                case SERVICE_UNAVAILABLE_503:
                default:
                    throw new SmartherGatewayException(String.format("API Gateway returned error status %s (%s)",
                            response.getStatus(), HttpStatus.getMessage(response.getStatus())));
            }
            return success;
        }

        /**
         * Processes the responded content if the status code indicated an error.
         *
         * @param response
         *            the response content returned by the API gateway
         *
         * @return the error message extracted from the response content
         *
         * @throws {@link SmartherTokenExpiredException}
         *             if the authorization access token used to communicate with the API gateway has expired
         * @throws {@link SmartherAuthorizationException}
         *             if a generic authorization issue with the API gateway has occurred
         * @throws {@link SmartherInvalidResponseException}
         *             if the response received from the API gateway cannot be parsed
         */
        private String processErrorState(ContentResponse response)
                throws SmartherTokenExpiredException, SmartherAuthorizationException, SmartherInvalidResponseException {
            try {
                final JsonElement element = JsonParser.parseString(response.getContentAsString());

                if (element.isJsonObject()) {
                    final JsonObject object = element.getAsJsonObject();
                    if (object.has(ERROR_CODE) && object.has(ERROR_MESSAGE)) {
                        final String message = object.get(ERROR_MESSAGE).getAsString();

                        // Bad request can be anything, from authorization problems to plant or module problems.
                        // Therefore authorization type errors are filtered and handled differently.
                        logger.debug("Bad request: {}", message);
                        if (message.contains(TOKEN_EXPIRED)) {
                            throw new SmartherTokenExpiredException(message);
                        } else {
                            return message;
                        }
                    } else if (object.has(AUTHORIZATION_ERROR)) {
                        final String errorDescription = object.get(AUTHORIZATION_ERROR).getAsString();
                        throw new SmartherAuthorizationException(errorDescription);
                    }
                }
                logger.debug("Unknown response: {}", response);
                return "Unknown response";
            } catch (JsonSyntaxException e) {
                logger.warn("Response was not json: ", e);
                throw new SmartherInvalidResponseException(e.getMessage());
            }
        }
    }
}
