/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.http.internal.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.http.internal.config.HttpThingConfig;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

/**
 * The {@link RateLimitedHttpClient} is a wrapper for a Jetty HTTP client that limits the number of requests by delaying
 * the request creation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RateLimitedHttpClient {
    private final Logger logger = LoggerFactory.getLogger(RateLimitedHttpClient.class);
    private static final int MAX_QUEUE_SIZE = 1000; // maximum queue size
    private Thing thing;
    private final ScheduledExecutorService scheduler;
    private final LinkedBlockingQueue<RequestQueueEntry> requestQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    private @Nullable HttpClient httpClient = null;
    private @Nullable HttpThingConfig config = null;
    private int delay = 0; // in ms

    private @Nullable String authToken = null;
    private @Nullable Instant authTokenIssueDate = null;
    private @Nullable URI authTokenEndpointUri = null;

    private @Nullable ScheduledFuture<?> processJob;

    public RateLimitedHttpClient(Thing thing, ScheduledExecutorService scheduler) {
        this.thing = thing;
        this.scheduler = scheduler;
    }

    /**
     * Stop processing the queue and clear it
     */
    public void shutdown() {
        stopProcessJob();
        requestQueue.forEach(queueEntry -> queueEntry.future.completeExceptionally(new CancellationException()));
    }

    public void initialize(HttpClient httpClient, HttpThingConfig config) throws URISyntaxException {
        stopProcessJob();

        this.httpClient = httpClient;
        this.config = config;
        if (config.delay < 0) {
            throw new IllegalArgumentException("Delay needs to be larger or equal to zero");
        }

        if (config.delay != 0) {
            processJob = scheduler.scheduleWithFixedDelay(this::processQueue, 0, config.delay, TimeUnit.MILLISECONDS);
        }

        // configure authentication
        if (!config.username.isEmpty()) {

            AuthenticationStore authStore = getHttpClient().getAuthenticationStore();
            URI uri = new URI(config.baseURL);
            switch (config.authMode) {
                case BASIC_PREEMPTIVE:
                    logger.debug("Preemptive Basic Authentication configured for thing '{}'", thing.getUID());
                    break;
                case TOKEN_PASSWORD:
                    logger.debug("OAuth2 Token with user/password configured for thing '{}'", thing.getUID());

                    if (config.tokenEndpointURL != null && !config.tokenEndpointURL.isEmpty())
                        this.authTokenEndpointUri = new URI(config.tokenEndpointURL); // throws URISyntaxException if
                                                                                      // format is invalid
                    else
                        throw new IllegalArgumentException(
                                "tokenEndpointURL needs to be configured when authMode is set to TOKEN_PASSWORD");

                    if (config.tokenValidtyPeriod <= 0)
                        throw new IllegalArgumentException(
                                "tokenValidtyPeriod needs to be larger than zero when authMode is set to TOKEN_PASSWORD");
                    break;
                case BASIC:
                    authStore.addAuthentication(
                            new BasicAuthentication(uri, Authentication.ANY_REALM, config.username, config.password));
                    logger.debug("Basic Authentication configured for thing '{}'", thing.getUID());
                    break;
                case DIGEST:
                    authStore.addAuthentication(
                            new DigestAuthentication(uri, Authentication.ANY_REALM, config.username, config.password));
                    logger.debug("Digest Authentication configured for thing '{}'", thing.getUID());
                    break;
                default:
                    logger.warn("Unknown authentication method '{}' for thing '{}'", config.authMode, thing.getUID());
            }

        } else {
            logger.debug("No authentication configured for thing '{}'", thing.getUID());
        }
    }

    /**
     * Create a new request to the given URL respecting rate-limits
     *
     * @param finalUrl the request URL
     * @return a CompletableFuture that completes with the request
     */
    public CompletableFuture<@Nullable Void> newRequest(URI finalUrl, Consumer<AuthenticatedRequestContext> action) {
        CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
        var task = new RequestQueueEntry(finalUrl, action, future);

        // if no delay is set, return a completed CompletableFuture
        if (delay == 0) {
            execute(task);
        } else if (!requestQueue.offer(task)) {
            future.completeExceptionally(new RejectedExecutionException("Maximum queue size exceeded."));
        } else {
            // enqueued
        }
        return future;
    }

    private void execute(RequestQueueEntry task) {
        AuthenticatedRequestContext context;
        try {
            context = new AuthenticatedRequestContext(getHttpClient().newRequest(task.finalUrl));

            context.response.exceptionally(e -> {
                if (e instanceof HttpAuthException) {
                    if (task.isRetry) {
                        logger.warn("Retry after authentication failure failed again for '{}', failing here",
                                task.finalUrl);
                        task.future.completeExceptionally(e);
                    } else {
                        AuthenticationStore authStore = getHttpClient().getAuthenticationStore();
                        Authentication.Result authResult = authStore.findAuthenticationResult(task.finalUrl);
                        if (authResult != null) {
                            authStore.removeAuthenticationResult(authResult);
                            authToken = null;
                            logger.debug("Cleared authentication result for '{}', retrying immediately", task.finalUrl);
                            task.setIsRetry(true);
                            if (!requestQueue.offer(task)) {
                                task.future.completeExceptionally(
                                        new RejectedExecutionException("Maximum queue size exceeded."));
                            }
                        } else {
                            logger.warn("Could not find authentication result for '{}', failing here", task.finalUrl);
                            task.future.completeExceptionally(e);
                        }
                    }
                }
                return null;
            }).thenAccept(t -> task.future.complete(null));

            task.action.accept(context);

        } catch (HttpAuthException e1) {
            task.future.completeExceptionally(e1);
        }
    }

    /**
     * Get the AuthenticationStore from the wrapped client
     *
     * @return
     */
    public AuthenticationStore getAuthenticationStore() {
        return getHttpClient().getAuthenticationStore();
    }

    private void stopProcessJob() {
        ScheduledFuture<?> processJob = this.processJob;
        if (processJob != null) {
            processJob.cancel(false);
            this.processJob = null;
        }
    }

    private void processQueue() {
        RequestQueueEntry queueEntry = requestQueue.poll();
        execute(queueEntry);
    }

    private HttpClient getHttpClient() {
        return Optional.ofNullable(this.httpClient)
                .orElseThrow(() -> new IllegalStateException("HttpClient is only available if thing is initialized!"));
    }

    private HttpThingConfig getConfig() {
        return Optional.ofNullable(this.config)
                .orElseThrow(() -> new IllegalStateException("ThingConfig is only available if thing is initialized!"));
    }

    private String getValidBearerToken() throws HttpAuthException {

        var now = Instant.now();

        var endpointUri = Optional.ofNullable(this.authTokenEndpointUri)
                .orElseThrow(() -> new IllegalArgumentException("this.authTokenEndpointUri must not be null!"));

        var config = getConfig();

        if (authToken == null || authToken.isEmpty() || authTokenIssueDate == null
                || authTokenIssueDate.until(now, ChronoUnit.MINUTES) > config.tokenValidtyPeriod) {
            logger.info("Auth token is invalid, requesting new token for thing '{}'", thing.getUID());

            var request = getHttpClient().POST(endpointUri);

            var encodedUser = URLEncoder.encode(config.username, StandardCharsets.UTF_8);
            var encodedPassword = URLEncoder.encode(config.password, StandardCharsets.UTF_8);

            var authRequestBody = "grant_type=password&username=" + encodedUser + "&password=" + encodedPassword;

            if (!config.tokenEndpointClientId.isEmpty())
                authRequestBody += "&client_id="
                        + URLEncoder.encode(config.tokenEndpointClientId, StandardCharsets.UTF_8);

            request.content(new StringContentProvider("application/x-www-form-urlencoded", authRequestBody,
                    StandardCharsets.UTF_8));

            try {
                var response = request.send();
                if (response != null && response.getStatus() < 300) {
                    var stringResponse = response.getContentAsString();
                    authToken = fromString(stringResponse, config.tokenJsonPath).toString().replaceAll("^\"|\"$", "");
                    authTokenIssueDate = Instant.now();
                } else {
                    throw new HttpAuthException(
                            "Failed to request Auth-Token, response status code: " + response.getStatus());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new HttpAuthException("Failed to request Auth-Token: " + e.getMessage());
            }
        } else {
            // token is still valid...
        }

        return Optional.ofNullable(authToken)
                .orElseThrow(() -> new HttpAuthException("Failed to request a valid authToken"));
    }

    /**
     * Source: https://stackoverflow.com/a/26087867/1790688
     * 
     * @param json
     * @param path
     * @return JsonElement
     * @throws JsonSyntaxException
     */
    private static JsonElement fromString(String json, String path) throws JsonSyntaxException {
        JsonObject obj = new GsonBuilder().create().fromJson(json, JsonObject.class);
        if (obj != null && !path.isEmpty()) {
            String[] seg = path.split("\\.");
            for (String element : seg) {
                if (element.isEmpty() || element.equals("$")) {
                    // skip
                } else if (obj != null) {
                    JsonElement ele = obj.get(element);
                    if (ele == null) { // not found
                        return obj;
                    } else if (!ele.isJsonObject())
                        return ele;
                    else
                        obj = ele.getAsJsonObject();
                } else {
                    throw new JsonSyntaxException("Given json is invalid");
                }
            }
        }
        return Optional.ofNullable(obj).orElseThrow(() -> new JsonSyntaxException("Given json is invalid"));
    }

    public class AuthenticatedRequestContext {

        public final Request request;
        public final CompletableFuture<@Nullable Content> response;

        private AuthenticatedRequestContext(Request request) throws HttpAuthException {
            this.request = request;
            this.response = new CompletableFuture<>();
            var config = getConfig();

            switch (config.authMode) {
                case BASIC_PREEMPTIVE:
                    request.header("Authorization", "Basic "
                            + Base64.getEncoder().encodeToString((config.username + ":" + config.password).getBytes()));
                    break;
                case TOKEN_PASSWORD:
                    request.header("Authorization", "Bearer " + getValidBearerToken());
                    break;

                case BASIC:
                case DIGEST:
                    // auth-store configured in initialize
                    break;
            }
        }
    }

    private static class RequestQueueEntry {
        public URI finalUrl;
        public CompletableFuture<@Nullable Void> future;
        public Consumer<AuthenticatedRequestContext> action;
        public boolean isRetry = false;

        public RequestQueueEntry(URI finalUrl, Consumer<AuthenticatedRequestContext> action,
                CompletableFuture<@Nullable Void> future) {
            this.finalUrl = finalUrl;
            this.future = future;
            this.action = action;
        }

        public void setIsRetry(boolean value) {
            this.isRetry = value;
        }
    }
}
