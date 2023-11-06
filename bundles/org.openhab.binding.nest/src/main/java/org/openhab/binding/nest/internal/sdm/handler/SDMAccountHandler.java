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
package org.openhab.binding.nest.internal.sdm.handler;

import static java.util.function.Predicate.not;
import static org.openhab.binding.nest.internal.sdm.dto.SDMGson.GSON;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.sdm.api.PubSubAPI;
import org.openhab.binding.nest.internal.sdm.api.SDMAPI;
import org.openhab.binding.nest.internal.sdm.config.SDMAccountConfiguration;
import org.openhab.binding.nest.internal.sdm.discovery.SDMDiscoveryService;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubMessage;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent;
import org.openhab.binding.nest.internal.sdm.dto.SDMEvent.SDMResourceUpdate;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingPubSubDataException;
import org.openhab.binding.nest.internal.sdm.exception.FailedSendingSDMDataException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidPubSubAccessTokenException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidPubSubAuthorizationCodeException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAccessTokenException;
import org.openhab.binding.nest.internal.sdm.exception.InvalidSDMAuthorizationCodeException;
import org.openhab.binding.nest.internal.sdm.listener.PubSubSubscriptionListener;
import org.openhab.binding.nest.internal.sdm.listener.SDMAPIRequestListener;
import org.openhab.binding.nest.internal.sdm.listener.SDMEventListener;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SDMAccountHandler} provides the {@link SDMAPI} instance used by the device handlers.
 * The {@link SDMAPI} is used by device handlers for periodically refreshing device data and sending device commands.
 * When Pub/Sub is properly configured, the account handler also sends received {@link SDMEvent}s from the
 * {@link PubSubAPI} to the subscribed {@link SDMEventListener}s.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMAccountHandler extends BaseBridgeHandler {

    private static final String PUBSUB_TOPIC_NAME_PREFIX = "projects/sdm-prod/topics/enterprise-";

    private final Logger logger = LoggerFactory.getLogger(SDMAccountHandler.class);

    private HttpClientFactory httpClientFactory;
    private OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) SDMAccountConfiguration config;
    private @Nullable Future<?> initializeFuture;

    private @Nullable PubSubAPI pubSubAPI;
    private @Nullable Exception pubSubException;

    private @Nullable SDMAPI sdmAPI;
    private @Nullable Exception sdmException;
    private @Nullable Future<?> sdmCheckFuture;
    private final Duration sdmCheckDelay = Duration.ofMinutes(1);

    private final Map<String, SDMEventListener> listeners = new ConcurrentHashMap<>();

    private final SDMAPIRequestListener requestListener = new SDMAPIRequestListener() {
        @Override
        public void onError(Exception exception) {
            sdmException = exception;
            logger.debug("SDM exception occurred");
            updateThingStatus();

            Future<?> future = sdmCheckFuture;
            if (future == null || future.isDone()) {
                sdmCheckFuture = scheduler.scheduleWithFixedDelay(() -> {
                    SDMAPI localSDMAPI = sdmAPI;
                    if (localSDMAPI != null) {
                        try {
                            logger.debug("Checking SDM API");
                            localSDMAPI.listDevices();
                        } catch (FailedSendingSDMDataException | InvalidSDMAccessTokenException e) {
                            logger.debug("SDM API check failed");
                        }
                    }
                }, sdmCheckDelay.toNanos(), sdmCheckDelay.toNanos(), TimeUnit.NANOSECONDS);
                logger.debug("Scheduled SDM API check job");
            }
        }

        @Override
        public void onSuccess() {
            if (sdmException != null) {
                sdmException = null;
                logger.debug("SDM exception cleared");
                updateThingStatus();
            }

            Future<?> future = sdmCheckFuture;
            if (future != null) {
                future.cancel(true);
                sdmCheckFuture = null;
                logger.debug("Cancelled SDM API check job");
            }
        }
    };

    private final PubSubSubscriptionListener subscriptionListener = new PubSubSubscriptionListener() {
        @Override
        public void onError(Exception exception) {
            pubSubException = exception;
            logger.debug("Pub/Sub exception occurred");
            updateThingStatus();
        }

        @Override
        public void onMessage(PubSubMessage message) {
            if (pubSubException != null) {
                pubSubException = null;
                logger.debug("Pub/Sub exception cleared");
                updateThingStatus();
            }
            handlePubSubMessage(message);
        }

        @Override
        public void onNoNewMessages() {
            if (pubSubException != null) {
                pubSubException = null;
                logger.debug("Pub/Sub exception cleared");
                updateThingStatus();
            }
        }
    };

    public SDMAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory, OAuthFactory oAuthFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(SDMAccountConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        initializeFuture = scheduler.submit(() -> {
            sdmAPI = initializeSDMAPI();
            if (config.usePubSub()) {
                pubSubAPI = initializePubSubAPI();
            }
            updateThingStatus();
        });
    }

    private @Nullable SDMAPI initializeSDMAPI() {
        SDMAPI sdmAPI = new SDMAPI(httpClientFactory, oAuthFactory, getThing().getUID().getAsString(),
                config.sdmProjectId, config.sdmClientId, config.sdmClientSecret);
        sdmException = null;

        try {
            if (!config.sdmAuthorizationCode.isBlank()) {
                sdmAPI.authorizeClient(config.sdmAuthorizationCode);

                Configuration configuration = editConfiguration();
                configuration.put(SDMAccountConfiguration.SDM_AUTHORIZATION_CODE, "");
                updateConfiguration(configuration);
            }

            sdmAPI.checkAccessTokenValidity();
            sdmAPI.addRequestListener(requestListener);

            return sdmAPI;
        } catch (InvalidSDMAccessTokenException | InvalidSDMAuthorizationCodeException | IOException e) {
            sdmException = e;
            return null;
        }
    }

    private @Nullable PubSubAPI initializePubSubAPI() {
        PubSubAPI pubSubAPI = new PubSubAPI(httpClientFactory, oAuthFactory, getThing().getUID().getAsString(),
                config.pubsubProjectId, config.pubsubClientId, config.pubsubClientSecret);
        pubSubException = null;

        try {
            if (!config.pubsubAuthorizationCode.isBlank()) {
                pubSubAPI.authorizeClient(config.pubsubAuthorizationCode);

                Configuration configuration = editConfiguration();
                configuration.put(SDMAccountConfiguration.PUBSUB_AUTHORIZATION_CODE, "");
                updateConfiguration(configuration);
            }

            pubSubAPI.checkAccessTokenValidity();
            pubSubAPI.createSubscription(config.pubsubSubscriptionId, PUBSUB_TOPIC_NAME_PREFIX + config.sdmProjectId);
            pubSubAPI.addSubscriptionListener(config.pubsubSubscriptionId, subscriptionListener);

            return pubSubAPI;
        } catch (FailedSendingPubSubDataException | InvalidPubSubAccessTokenException
                | InvalidPubSubAuthorizationCodeException | IOException e) {
            pubSubException = e;
            return null;
        }
    }

    @Override
    public void dispose() {
        Future<?> localFuture = initializeFuture;
        if (localFuture != null) {
            localFuture.cancel(true);
            initializeFuture = null;
        }

        localFuture = sdmCheckFuture;
        if (localFuture != null) {
            localFuture.cancel(true);
            sdmCheckFuture = null;
        }

        PubSubAPI localPubSubAPI = pubSubAPI;
        if (localPubSubAPI != null) {
            localPubSubAPI.dispose();
            pubSubAPI = null;
        }

        SDMAPI localSDMAPI = sdmAPI;
        if (localSDMAPI != null) {
            localSDMAPI.dispose();
            sdmAPI = null;
        }
    }

    @Override
    public void handleRemoval() {
        PubSubAPI localPubSubAPI = pubSubAPI;
        if (localPubSubAPI != null) {
            localPubSubAPI.deleteOAuthServiceAndAccessToken();
        }
        SDMAPI localSDMAPI = sdmAPI;
        if (localSDMAPI != null) {
            localSDMAPI.deleteOAuthServiceAndAccessToken();
        }
        super.handleRemoval();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SDMDiscoveryService.class);
    }

    public void addThingDataListener(String deviceId, SDMEventListener listener) {
        listeners.put(deviceId, listener);
    }

    public void removeThingDataListener(String deviceId, SDMEventListener listener) {
        listeners.remove(deviceId, listener);
    }

    public @Nullable SDMAPI getAPI() {
        return sdmAPI;
    }

    private void handlePubSubMessage(PubSubMessage message) {
        String messageId = message.messageId;
        String json = new String(Base64.getDecoder().decode(message.data), StandardCharsets.UTF_8);

        logger.debug("Handling messageId={} with content:", messageId);
        logger.debug("{}", json);

        SDMEvent event = GSON.fromJson(json, SDMEvent.class);
        if (event == null) {
            logger.debug("Ignoring messageId={} (empty)", messageId);
            return;
        }

        SDMResourceUpdate resourceUpdate = event.resourceUpdate;
        if (resourceUpdate == null) {
            logger.debug("Ignoring messageId={} (no resource update)", messageId);
            return;
        }

        String deviceId = resourceUpdate.name.deviceId;
        SDMEventListener listener = listeners.get(deviceId);
        if (listener != null) {
            logger.debug("Sending messageId={} to listener with deviceId={}", messageId, deviceId);
            listener.onEvent(event);
        } else {
            logger.debug("No listener for messageId={} with deviceId={}", messageId, deviceId);
        }
    }

    private void updateThingStatus() {
        Exception e = sdmException != null ? sdmException : pubSubException;
        if (e != null) {
            if (e instanceof InvalidSDMAccessTokenException || e instanceof InvalidSDMAuthorizationCodeException
                    || e instanceof InvalidPubSubAccessTokenException
                    || e instanceof InvalidPubSubAuthorizationCodeException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } else {
                Throwable cause = e.getCause();
                String description = Stream
                        .of(Objects.requireNonNullElse(e.getMessage(), ""),
                                cause == null ? "" : Objects.requireNonNullElse(cause.getMessage(), ""))
                        .filter(not(String::isBlank)) //
                        .collect(Collectors.joining(": "));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
            }
        } else {
            String description = config.usePubSub() ? "Using periodic refresh and Pub/Sub" : "Using periodic refresh";
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, description);
        }
    }
}
