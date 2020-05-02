/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api;

import static org.eclipse.jetty.http.HttpStatus.*;

import java.lang.invoke.MethodHandles;
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
import org.openhab.binding.smarther.internal.api.exception.SmartherAuthorizationException;
import org.openhab.binding.smarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.smarther.internal.api.exception.SmartherSubscriptionAlreadyExistsException;
import org.openhab.binding.smarther.internal.api.exception.SmartherTokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Class to perform the actual call to the BTicino/Legrand API gateway, interprets the returned Http status codes, and
 * handles the error codes returned by the API gateway.
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

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JsonParser parser = new JsonParser();
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructor.
     *
     * @param scheduler Scheduler to reschedule calls when rate limit exceeded or call not ready
     * @param httpClient http client to use to make http calls
     */
    public SmartherApiConnector(ScheduledExecutorService scheduler, HttpClient httpClient) {
        this.scheduler = scheduler;
        this.httpClient = httpClient;
    }

    /**
     * Performs a call to the BTicino/Legrand API gateway and returns the raw response. In there are problems this
     * method can throw a SmartherGateway exception.
     *
     * @param requester The function to construct the request with http client that is passed as argument to the
     *            function
     * @param subscription The subscription string to use in the Subscription header
     * @param authorization The authorization string to use in the Authorization header
     * @return the raw reponse given
     */
    public ContentResponse request(Function<HttpClient, Request> requester, String subscription, String authorization) {
        final Caller caller = new Caller(requester, subscription, authorization);

        try {
            return caller.call().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SmartherGatewayException("Thread interrupted");
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new SmartherGatewayException(e.getMessage(), e);
            }
        }
    }

    /**
     * Class to handle a call to the BTicino/Legrand API gateway. In case of rate limiting or not finished jobs it will
     * retry in a specified time frame. It retries a number of times and then gives up with an exception.
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
         * Constructor.
         *
         * @param requester The function to construct the request with http client that is passed as argument to the
         *            function
         * @param subscription The subscription string to use in the Subscription header
         * @param authorization The authorization string to use in the Authorization header
         */
        public Caller(Function<HttpClient, Request> requester, String subscription, String authorization) {
            this.requester = requester;
            this.subscription = subscription;
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
                future.completeExceptionally(e.getCause());
            } catch (RuntimeException | TimeoutException e) {
                future.completeExceptionally(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            }
            return future;
        }

        /**
         * Processes the response of the BTicino/Legrand API gateway call and handles the HTTP status codes. The method
         * returns true
         * if the response indicates a successful and false if the call should be retried. If there were other problems
         * a SmartherGateway exception is thrown indicating no retry should be done an the user should be informed.
         *
         * @param response the response given by the API gateway
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
         * Processes the responded content if the status code indicated an error. If the response could be parsed the
         * content error message is returned. If the error indicated a token or authorization error a specific exception
         * is thrown. If an error message is thrown the caller throws the appropriate exception based on the state with
         * which the error was returned by the BTicino/Legrand API gateway.
         *
         * @param response content returned by API gateway
         * @return the error messages
         */
        private String processErrorState(ContentResponse response) {
            try {
                final JsonElement element = parser.parse(response.getContentAsString());

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
                return "Unknown response";
            }
        }
    }

}
