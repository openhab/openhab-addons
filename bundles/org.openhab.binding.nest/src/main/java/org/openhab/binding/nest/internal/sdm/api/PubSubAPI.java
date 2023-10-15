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
package org.openhab.binding.nest.internal.sdm.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.openhab.binding.nest.internal.sdm.dto.SDMGson.GSON;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubAcknowledgeRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubCreateRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubPullRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubPullResponse;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingPubSubDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidPubSubAccessTokenException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidPubSubAuthorizationCodeException;
import org.openhab.binding.nest.internal.sdm.listener.PubSubSubscriptionListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PubSubAPI} implements a subset of the Pub/Sub REST API which allows for subscribing to SDM events.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://cloud.google.com/pubsub/docs/reference/rest">
 *      https://cloud.google.com/pubsub/docs/reference/rest</a>
 * @see <a href="https://developers.google.com/nest/device-access/api/events">
 *      https://developers.google.com/nest/device-access/api/events</a>
 */
@NonNullByDefault
public class PubSubAPI {

    private class Subscriber implements Runnable {

        private final String subscriptionId;

        Subscriber(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        @Override
        public void run() {
            if (!subscriptionListeners.containsKey(subscriptionId)) {
                logger.debug("Stop receiving subscription '{}' messages since there are no listeners", subscriptionId);
                return;
            }

            try {
                checkAccessTokenValidity();
                String messages = pullSubscriptionMessages(subscriptionId);

                PubSubPullResponse pullResponse = GSON.fromJson(messages, PubSubPullResponse.class);

                if (pullResponse != null && pullResponse.receivedMessages != null) {
                    logger.debug("Subscription '{}' has {} new message(s)", subscriptionId,
                            pullResponse.receivedMessages.size());
                    forEachListener(listener -> pullResponse.receivedMessages
                            .forEach(message -> listener.onMessage(message.message)));
                    List<String> ackIds = pullResponse.receivedMessages.stream().map(message -> message.ackId)
                            .collect(Collectors.toList());
                    acknowledgeSubscriptionMessages(subscriptionId, ackIds);
                } else {
                    forEachListener(PubSubSubscriptionListener::onNoNewMessages);
                }

                scheduler.submit(this);
            } catch (FailedSendingPubSubDataException e) {
                logger.debug("Expected exception while pulling message for '{}' subscription", subscriptionId, e);
                Throwable cause = e.getCause();
                if (!(cause instanceof InterruptedException)) {
                    forEachListener(listener -> listener.onError(e));
                    scheduler.schedule(this, RETRY_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS);
                }
            } catch (InvalidPubSubAccessTokenException e) {
                logger.warn("Cannot pull messages for '{}' subscription (access or refresh token invalid)",
                        subscriptionId, e);
                forEachListener(listener -> listener.onError(e));
            } catch (Exception e) {
                logger.warn("Unexpected exception while pulling message for '{}' subscription", subscriptionId, e);
                forEachListener(listener -> listener.onError(e));
                scheduler.schedule(this, RETRY_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS);
            }
        }

        private void forEachListener(Consumer<PubSubSubscriptionListener> consumer) {
            Set<PubSubSubscriptionListener> listeners = subscriptionListeners.get(subscriptionId);
            if (listeners != null) {
                listeners.forEach(consumer::accept);
            } else {
                logger.debug("Subscription '{}' has no listeners", subscriptionId);
            }
        }
    }

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static final String REDIRECT_URI = "https://www.google.com";

    private static final String PUBSUB_HANDLE_FORMAT = "%s.pubsub";
    private static final String PUBSUB_SCOPE = "https://www.googleapis.com/auth/pubsub";

    private static final String PUBSUB_URL_PREFIX = "https://pubsub.googleapis.com/v1/";
    private static final int PUBSUB_PULL_MAX_MESSAGES = 10;

    private static final String APPLICATION_JSON = "application/json";
    private static final String BEARER = "Bearer ";

    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration RETRY_TIMEOUT = Duration.ofSeconds(30);

    private final Logger logger = LoggerFactory.getLogger(PubSubAPI.class);

    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;
    private final OAuthClientService oAuthService;
    private final String oAuthServiceHandleId;
    private final String projectId;
    private final ScheduledThreadPoolExecutor scheduler;
    private final Map<String, Set<PubSubSubscriptionListener>> subscriptionListeners = new HashMap<>();

    public PubSubAPI(HttpClientFactory httpClientFactory, OAuthFactory oAuthFactory, String ownerId, String projectId,
            String clientId, String clientSecret) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.projectId = projectId;
        this.oAuthFactory = oAuthFactory;
        this.oAuthServiceHandleId = String.format(PUBSUB_HANDLE_FORMAT, ownerId);
        this.oAuthService = oAuthFactory.createOAuthClientService(oAuthServiceHandleId, TOKEN_URL, AUTH_URL, clientId,
                clientSecret, PUBSUB_SCOPE, false);
        scheduler = new ScheduledThreadPoolExecutor(3, new NamedThreadFactory(ownerId, true));
    }

    public void dispose() {
        subscriptionListeners.clear();
        scheduler.shutdownNow();
        oAuthFactory.ungetOAuthService(oAuthServiceHandleId);
    }

    public void deleteOAuthServiceAndAccessToken() {
        oAuthFactory.deleteServiceAndAccessToken(oAuthServiceHandleId);
    }

    public void authorizeClient(String authorizationCode) throws InvalidPubSubAuthorizationCodeException, IOException {
        try {
            oAuthService.getAccessTokenResponseByAuthorizationCode(authorizationCode, REDIRECT_URI);
        } catch (OAuthException | OAuthResponseException e) {
            throw new InvalidPubSubAuthorizationCodeException(
                    "Failed to authorize Pub/Sub client. Check the authorization code or generate a new one.", e);
        }
    }

    public void checkAccessTokenValidity() throws InvalidPubSubAccessTokenException, IOException {
        getAuthorizationHeader();
    }

    private String acknowledgeSubscriptionMessages(String subscriptionId, List<String> ackIds)
            throws FailedSendingPubSubDataException, InvalidPubSubAccessTokenException {
        logger.debug("Acknowleding {} message(s) for '{}' subscription", ackIds.size(), subscriptionId);
        String url = getSubscriptionUrl(subscriptionId) + ":acknowledge";
        String requestContent = GSON.toJson(new PubSubAcknowledgeRequest(ackIds));
        return postJson(url, requestContent);
    }

    public void addSubscriptionListener(String subscriptionId, PubSubSubscriptionListener listener) {
        synchronized (subscriptionListeners) {
            Set<PubSubSubscriptionListener> listeners = subscriptionListeners.get(subscriptionId);
            if (listeners == null) {
                listeners = new HashSet<>();
                subscriptionListeners.put(subscriptionId, listeners);
            }
            listeners.add(listener);
            if (listeners.size() == 1) {
                scheduler.submit(new Subscriber(subscriptionId));
            }
        }
    }

    public void removeSubscriptionListener(String subscriptionId, PubSubSubscriptionListener listener) {
        synchronized (subscriptionListeners) {
            Set<PubSubSubscriptionListener> listeners = subscriptionListeners.get(subscriptionId);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    subscriptionListeners.remove(subscriptionId);
                    scheduler.getQueue().removeIf(
                            runnable -> runnable instanceof Subscriber s && s.subscriptionId.equals(subscriptionId));
                }
            }
        }
    }

    public void createSubscription(String subscriptionId, String topicName)
            throws FailedSendingPubSubDataException, InvalidPubSubAccessTokenException {
        logger.debug("Creating '{}' subscription", subscriptionId);
        String url = getSubscriptionUrl(subscriptionId);
        String requestContent = GSON.toJson(new PubSubCreateRequest(topicName, true));
        putJson(url, requestContent);
    }

    private String getAuthorizationHeader() throws InvalidPubSubAccessTokenException, IOException {
        try {
            AccessTokenResponse response = oAuthService.getAccessTokenResponse();
            if (response == null || response.getAccessToken() == null || response.getAccessToken().isEmpty()) {
                throw new InvalidPubSubAccessTokenException(
                        "No Pub/Sub access token. Client may not have been authorized.");
            }
            if (response.getRefreshToken() == null || response.getRefreshToken().isEmpty()) {
                throw new InvalidPubSubAccessTokenException(
                        "No Pub/Sub refresh token. Delete and readd credentials, then reauthorize.");
            }
            return BEARER + response.getAccessToken();
        } catch (OAuthException | OAuthResponseException e) {
            throw new InvalidPubSubAccessTokenException(
                    "Error fetching Pub/Sub access token. Check the authorization code or generate a new one.", e);
        }
    }

    private String getSubscriptionUrl(String subscriptionId) {
        return PUBSUB_URL_PREFIX + "projects/" + projectId + "/subscriptions/" + subscriptionId;
    }

    private String postJson(String url, String requestContent)
            throws FailedSendingPubSubDataException, InvalidPubSubAccessTokenException {
        try {
            logger.debug("Posting JSON to: {}", url);
            String response = httpClient.newRequest(url) //
                    .method(POST) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .header(AUTHORIZATION, getAuthorizationHeader()) //
                    .content(new StringContentProvider(requestContent), APPLICATION_JSON) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send() //
                    .getContentAsString();
            logger.debug("Response: {}", response);
            return response;
        } catch (ExecutionException | InterruptedException | IOException | TimeoutException e) {
            throw new FailedSendingPubSubDataException("Failed to send JSON POST request", e);
        }
    }

    private String pullSubscriptionMessages(String subscriptionId)
            throws FailedSendingPubSubDataException, InvalidPubSubAccessTokenException {
        logger.debug("Pulling messages for '{}' subscription", subscriptionId);
        String url = getSubscriptionUrl(subscriptionId) + ":pull";
        String requestContent = GSON.toJson(new PubSubPullRequest(PUBSUB_PULL_MAX_MESSAGES));
        return postJson(url, requestContent);
    }

    private String putJson(String url, String requestContent)
            throws FailedSendingPubSubDataException, InvalidPubSubAccessTokenException {
        try {
            logger.debug("Putting JSON to: {}", url);
            String response = httpClient.newRequest(url) //
                    .method(HttpMethod.PUT) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .header(AUTHORIZATION, getAuthorizationHeader()) //
                    .content(new StringContentProvider(requestContent), APPLICATION_JSON) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send() //
                    .getContentAsString();
            logger.debug("Response: {}", response);
            return response;
        } catch (ExecutionException | InterruptedException | IOException | TimeoutException e) {
            throw new FailedSendingPubSubDataException("Failed to send JSON PUT request", e);
        }
    }
}
