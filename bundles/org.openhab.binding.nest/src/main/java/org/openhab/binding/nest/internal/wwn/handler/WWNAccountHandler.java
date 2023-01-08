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
package org.openhab.binding.nest.internal.wwn.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.JSON_CONTENT_TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.WWNUtils;
import org.openhab.binding.nest.internal.wwn.config.WWNAccountConfiguration;
import org.openhab.binding.nest.internal.wwn.discovery.WWNDiscoveryService;
import org.openhab.binding.nest.internal.wwn.dto.WWNErrorData;
import org.openhab.binding.nest.internal.wwn.dto.WWNIdentifiable;
import org.openhab.binding.nest.internal.wwn.dto.WWNTopLevelData;
import org.openhab.binding.nest.internal.wwn.dto.WWNUpdateRequest;
import org.openhab.binding.nest.internal.wwn.exceptions.FailedResolvingWWNUrlException;
import org.openhab.binding.nest.internal.wwn.exceptions.FailedSendingWWNDataException;
import org.openhab.binding.nest.internal.wwn.exceptions.InvalidWWNAccessTokenException;
import org.openhab.binding.nest.internal.wwn.listener.WWNStreamingDataListener;
import org.openhab.binding.nest.internal.wwn.listener.WWNThingDataListener;
import org.openhab.binding.nest.internal.wwn.rest.WWNAuthorizer;
import org.openhab.binding.nest.internal.wwn.rest.WWNStreamingRestClient;
import org.openhab.binding.nest.internal.wwn.update.WWNCompositeUpdateHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This account handler connects to Nest and handles all the WWN API requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author David Bennett - Initial contribution
 * @author Martin van Wingerden - Use listeners not only for discovery but for all data processing
 * @author Wouter Born - Improve exception and URL redirect handling
 */
@NonNullByDefault
public class WWNAccountHandler extends BaseBridgeHandler implements WWNStreamingDataListener {

    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(WWNAccountHandler.class);

    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final List<WWNUpdateRequest> nestUpdateRequests = new CopyOnWriteArrayList<>();
    private final WWNCompositeUpdateHandler updateHandler = new WWNCompositeUpdateHandler(
            this::getPresentThingsNestIds);

    private @NonNullByDefault({}) WWNAuthorizer authorizer;
    private @NonNullByDefault({}) WWNAccountConfiguration config;

    private @Nullable ScheduledFuture<?> initializeJob;
    private @Nullable ScheduledFuture<?> transmitJob;
    private @Nullable WWNRedirectUrlSupplier redirectUrlSupplier;
    private @Nullable WWNStreamingRestClient streamingRestClient;

    /**
     * Creates the bridge handler to connect to Nest.
     *
     * @param bridge The bridge to connect to Nest with.
     */
    public WWNAccountHandler(Bridge bridge, ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory) {
        super(bridge);
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    /**
     * Initialize the connection to Nest.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing Nest bridge handler");

        config = getConfigAs(WWNAccountConfiguration.class);
        authorizer = new WWNAuthorizer(config);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Starting poll query");

        initializeJob = scheduler.schedule(() -> {
            try {
                logger.debug("Product ID      {}", config.productId);
                logger.debug("Product Secret  {}", config.productSecret);
                logger.debug("Pincode         {}", config.pincode);
                logger.debug("Access Token    {}", getExistingOrNewAccessToken());
                redirectUrlSupplier = createRedirectUrlSupplier();
                restartStreamingUpdates();
            } catch (InvalidWWNAccessTokenException e) {
                logger.debug("Invalid access token", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Token is invalid and could not be refreshed: " + e.getMessage());
            }
        }, 0, TimeUnit.SECONDS);

        logger.debug("Finished initializing Nest bridge handler");
    }

    /**
     * Clean up the handler.
     */
    @Override
    public void dispose() {
        logger.debug("Nest bridge disposed");
        stopStreamingUpdates();

        ScheduledFuture<?> localInitializeJob = initializeJob;
        if (localInitializeJob != null && !localInitializeJob.isCancelled()) {
            localInitializeJob.cancel(true);
            initializeJob = null;
        }

        ScheduledFuture<?> localTransmitJob = transmitJob;
        if (localTransmitJob != null && !localTransmitJob.isCancelled()) {
            localTransmitJob.cancel(true);
            transmitJob = null;
        }

        this.authorizer = null;
        this.redirectUrlSupplier = null;
        this.streamingRestClient = null;
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, WWNThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, listener);
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, String nestId, WWNThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, nestId, listener);
    }

    /**
     * Adds the update request into the queue for doing something with, send immediately if the queue is empty.
     */
    public void addUpdateRequest(WWNUpdateRequest request) {
        nestUpdateRequests.add(request);
        scheduleTransmitJobForPendingRequests();
    }

    protected WWNRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidWWNAccessTokenException {
        return new WWNRedirectUrlSupplier(getHttpHeaders());
    }

    private String getExistingOrNewAccessToken() throws InvalidWWNAccessTokenException {
        String accessToken = config.accessToken;
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = authorizer.getNewAccessToken();
            config.accessToken = accessToken;
            config.pincode = "";
            // Update and save the access token in the bridge configuration
            Configuration configuration = editConfiguration();
            configuration.put(WWNAccountConfiguration.ACCESS_TOKEN, config.accessToken);
            configuration.put(WWNAccountConfiguration.PINCODE, config.pincode);
            updateConfiguration(configuration);
            logger.debug("Retrieved new access token: {}", config.accessToken);
            return accessToken;
        } else {
            logger.debug("Re-using access token from configuration: {}", accessToken);
            return accessToken;
        }
    }

    protected Properties getHttpHeaders() throws InvalidWWNAccessTokenException {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Authorization", "Bearer " + getExistingOrNewAccessToken());
        httpHeaders.put("Content-Type", JSON_CONTENT_TYPE);
        return httpHeaders;
    }

    public @Nullable <T> T getLastUpdate(Class<T> dataClass, String nestId) {
        return updateHandler.getLastUpdate(dataClass, nestId);
    }

    public <T> List<T> getLastUpdates(Class<T> dataClass) {
        return updateHandler.getLastUpdates(dataClass);
    }

    private WWNRedirectUrlSupplier getOrCreateRedirectUrlSupplier() throws InvalidWWNAccessTokenException {
        WWNRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
        if (localRedirectUrlSupplier == null) {
            localRedirectUrlSupplier = createRedirectUrlSupplier();
            redirectUrlSupplier = localRedirectUrlSupplier;
        }
        return localRedirectUrlSupplier;
    }

    private Set<String> getPresentThingsNestIds() {
        Set<String> nestIds = new HashSet<>();
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null && thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.GONE) {
                nestIds.add(((WWNIdentifiable) handler).getId());
            }
        }
        return nestIds;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(WWNDiscoveryService.class);
    }

    /**
     * Handles an incoming command update
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            updateHandler.resendLastUpdates();
        }
    }

    private void jsonToPutUrl(WWNUpdateRequest request)
            throws FailedSendingWWNDataException, InvalidWWNAccessTokenException, FailedResolvingWWNUrlException {
        try {
            WWNRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
            if (localRedirectUrlSupplier == null) {
                throw new FailedResolvingWWNUrlException("redirectUrlSupplier is null");
            }

            String url = localRedirectUrlSupplier.getRedirectUrl() + request.getUpdatePath();
            logger.debug("Putting data to: {}", url);

            String jsonContent = WWNUtils.toJson(request.getValues());
            logger.debug("PUT content: {}", jsonContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            String jsonResponse = HttpUtil.executeUrl("PUT", url, getHttpHeaders(), inputStream, JSON_CONTENT_TYPE,
                    REQUEST_TIMEOUT);
            logger.debug("PUT response: {}", jsonResponse);

            WWNErrorData error = WWNUtils.fromJson(jsonResponse, WWNErrorData.class);
            if (error.getError() != null && !error.getError().isBlank()) {
                logger.debug("Nest API error: {}", error);
                logger.warn("Nest API error: {}", error.getMessage());
            }
        } catch (IOException e) {
            throw new FailedSendingWWNDataException("Failed to send data", e);
        }
    }

    @Override
    public void onAuthorizationRevoked(String token) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "Authorization token revoked: " + token);
    }

    @Override
    public void onConnected() {
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Streaming data connection established");
        scheduleTransmitJobForPendingRequests();
    }

    @Override
    public void onDisconnected() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Streaming data disconnected");
    }

    @Override
    public void onError(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    @Override
    public void onNewTopLevelData(WWNTopLevelData data) {
        updateHandler.handleUpdate(data);
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Receiving streaming data");
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, WWNThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, listener);
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, String nestId, WWNThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, nestId, listener);
    }

    private void restartStreamingUpdates() {
        synchronized (this) {
            stopStreamingUpdates();
            startStreamingUpdates();
        }
    }

    private void scheduleTransmitJobForPendingRequests() {
        ScheduledFuture<?> localTransmitJob = transmitJob;
        if (!nestUpdateRequests.isEmpty() && (localTransmitJob == null || localTransmitJob.isDone())) {
            transmitJob = scheduler.schedule(this::transmitQueue, 0, SECONDS);
        }
    }

    private void startStreamingUpdates() {
        synchronized (this) {
            try {
                WWNStreamingRestClient localStreamingRestClient = new WWNStreamingRestClient(
                        getExistingOrNewAccessToken(), clientBuilder, eventSourceFactory,
                        getOrCreateRedirectUrlSupplier(), scheduler);
                localStreamingRestClient.addStreamingDataListener(this);
                localStreamingRestClient.start();

                streamingRestClient = localStreamingRestClient;
            } catch (InvalidWWNAccessTokenException e) {
                logger.debug("Invalid access token", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Token is invalid and could not be refreshed: " + e.getMessage());
            }
        }
    }

    private void stopStreamingUpdates() {
        WWNStreamingRestClient localStreamingRestClient = streamingRestClient;
        if (localStreamingRestClient != null) {
            synchronized (this) {
                localStreamingRestClient.stop();
                localStreamingRestClient.removeStreamingDataListener(this);
                streamingRestClient = null;
            }
        }
    }

    private void transmitQueue() {
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Not transmitting events because bridge is OFFLINE");
            return;
        }

        try {
            while (!nestUpdateRequests.isEmpty()) {
                // nestUpdateRequests is a CopyOnWriteArrayList so its iterator does not support remove operations
                WWNUpdateRequest request = nestUpdateRequests.get(0);
                jsonToPutUrl(request);
                nestUpdateRequests.remove(request);
            }
        } catch (InvalidWWNAccessTokenException e) {
            logger.debug("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        } catch (FailedResolvingWWNUrlException e) {
            logger.debug("Unable to resolve redirect URL", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);
        } catch (FailedSendingWWNDataException e) {
            logger.debug("Error sending data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);

            WWNRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
            if (localRedirectUrlSupplier != null) {
                localRedirectUrlSupplier.resetCache();
            }
        }
    }
}
