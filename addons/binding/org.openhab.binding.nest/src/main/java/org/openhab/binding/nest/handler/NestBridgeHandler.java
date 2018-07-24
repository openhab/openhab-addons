/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.nest.NestBindingConstants.JSON_CONTENT_TYPE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.internal.NestUtils;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.ErrorData;
import org.openhab.binding.nest.internal.data.NestIdentifiable;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.exceptions.FailedResolvingNestUrlException;
import org.openhab.binding.nest.internal.exceptions.FailedSendingNestDataException;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.openhab.binding.nest.internal.listener.NestStreamingDataListener;
import org.openhab.binding.nest.internal.listener.NestThingDataListener;
import org.openhab.binding.nest.internal.rest.NestAuthorizer;
import org.openhab.binding.nest.internal.rest.NestStreamingRestClient;
import org.openhab.binding.nest.internal.rest.NestUpdateRequest;
import org.openhab.binding.nest.internal.update.NestCompositeUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bridge handler connects to Nest and handles all the API requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author David Bennett - Initial contribution
 * @author Martin van Wingerden - Use listeners not only for discovery but for all data processing
 * @author Wouter Born - Improve exception and URL redirect handling
 */
@NonNullByDefault
public class NestBridgeHandler extends BaseBridgeHandler implements NestStreamingDataListener {

    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private final List<NestUpdateRequest> nestUpdateRequests = new CopyOnWriteArrayList<>();
    private final NestCompositeUpdateHandler updateHandler = new NestCompositeUpdateHandler(
            this::getPresentThingsNestIds);

    private @Nullable NestAuthorizer authorizer;
    private @Nullable NestBridgeConfiguration config;
    private @Nullable ScheduledFuture<?> initializeJob;
    private @Nullable ScheduledFuture<?> transmitJob;
    private @Nullable NestRedirectUrlSupplier redirectUrlSupplier;
    private @Nullable NestStreamingRestClient streamingRestClient;

    /**
     * Creates the bridge handler to connect to Nest.
     *
     * @param bridge The bridge to connect to Nest with.
     */
    public NestBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Initialize the connection to Nest.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing Nest bridge handler");

        config = getConfigAs(NestBridgeConfiguration.class);
        authorizer = new NestAuthorizer(config);
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Starting poll query");

        initializeJob = scheduler.schedule(() -> {
            try {
                logger.debug("Product ID      {}", config.productId);
                logger.debug("Product Secret  {}", config.productSecret);
                logger.debug("Pincode         {}", config.pincode);
                logger.debug("Access Token    {}", getExistingOrNewAccessToken());
                redirectUrlSupplier = createRedirectUrlSupplier();
                restartStreamingUpdates();
            } catch (InvalidAccessTokenException e) {
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

        if (initializeJob != null && !initializeJob.isCancelled()) {
            initializeJob.cancel(true);
            initializeJob = null;
        }

        if (transmitJob != null && !transmitJob.isCancelled()) {
            transmitJob.cancel(true);
            transmitJob = null;
        }

        this.authorizer = null;
        this.redirectUrlSupplier = null;
        this.streamingRestClient = null;
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, NestThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, listener);
    }

    public <T> boolean addThingDataListener(Class<T> dataClass, String nestId, NestThingDataListener<T> listener) {
        return updateHandler.addListener(dataClass, nestId, listener);
    }

    /**
     * Adds the update request into the queue for doing something with, send immediately if the queue is empty.
     */
    public void addUpdateRequest(NestUpdateRequest request) {
        nestUpdateRequests.add(request);
        scheduleTransmitJobForPendingRequests();
    }

    protected NestRedirectUrlSupplier createRedirectUrlSupplier() throws InvalidAccessTokenException {
        return new NestRedirectUrlSupplier(getHttpHeaders());
    }

    private String getExistingOrNewAccessToken() throws InvalidAccessTokenException {
        if (StringUtils.isEmpty(config.accessToken)) {
            config.accessToken = authorizer.getNewAccessToken();
            config.pincode = "";
            // Update and save the access token in the bridge configuration
            Configuration configuration = editConfiguration();
            configuration.put(NestBridgeConfiguration.ACCESS_TOKEN, config.accessToken);
            configuration.put(NestBridgeConfiguration.PINCODE, config.pincode);
            updateConfiguration(configuration);
            logger.debug("Retrieved new access token: {}", config.accessToken);
            return config.accessToken;
        } else {
            logger.debug("Re-using access token from configuration: {}", config.accessToken);
            return config.accessToken;
        }
    }

    protected Properties getHttpHeaders() throws InvalidAccessTokenException {
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

    private NestRedirectUrlSupplier getOrCreateRedirectUrlSupplier() throws InvalidAccessTokenException {
        NestRedirectUrlSupplier localRedirectUrlSupplier = redirectUrlSupplier;
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
                nestIds.add(((NestIdentifiable) handler).getId());
            }
        }
        return nestIds;
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

    private void jsonToPutUrl(NestUpdateRequest request)
            throws FailedSendingNestDataException, InvalidAccessTokenException, FailedResolvingNestUrlException {
        try {
            String url = redirectUrlSupplier.getRedirectUrl() + request.getUpdatePath();
            logger.debug("Putting data to: {}", url);

            String jsonContent = NestUtils.toJson(request.getValues());
            logger.debug("PUT content: {}", jsonContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            String jsonResponse = HttpUtil.executeUrl("PUT", url, getHttpHeaders(), inputStream, JSON_CONTENT_TYPE,
                    REQUEST_TIMEOUT);
            logger.debug("PUT response: {}", jsonResponse);

            ErrorData error = NestUtils.fromJson(jsonResponse, ErrorData.class);
            if (StringUtils.isNotBlank(error.getError())) {
                logger.debug("Nest API error: {}", error);
                logger.warn("Nest API error: {}", error.getMessage());
            }
        } catch (IOException e) {
            throw new FailedSendingNestDataException("Failed to send data", e);
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
    public void onNewTopLevelData(TopLevelData data) {
        updateHandler.handleUpdate(data);
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Receiving streaming data");
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, NestThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, listener);
    }

    public <T> boolean removeThingDataListener(Class<T> dataClass, String nestId, NestThingDataListener<T> listener) {
        return updateHandler.removeListener(dataClass, nestId, listener);
    }

    private void restartStreamingUpdates() {
        synchronized (this) {
            stopStreamingUpdates();
            startStreamingUpdates();
        }
    }

    private void scheduleTransmitJobForPendingRequests() {
        if (!nestUpdateRequests.isEmpty() && (transmitJob == null || transmitJob.isDone())) {
            transmitJob = scheduler.schedule(this::transmitQueue, 0, SECONDS);
        }
    }

    private void startStreamingUpdates() {
        synchronized (this) {
            try {
                streamingRestClient = new NestStreamingRestClient(getExistingOrNewAccessToken(),
                        getOrCreateRedirectUrlSupplier(), scheduler);
                streamingRestClient.addStreamingDataListener(this);
                streamingRestClient.start();
            } catch (InvalidAccessTokenException e) {
                logger.debug("Invalid access token", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Token is invalid and could not be refreshed: " + e.getMessage());
            }
        }
    }

    private void stopStreamingUpdates() {
        if (streamingRestClient != null) {
            synchronized (this) {
                streamingRestClient.stop();
                streamingRestClient.removeStreamingDataListener(this);
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
                NestUpdateRequest request = nestUpdateRequests.get(0);
                jsonToPutUrl(request);
                nestUpdateRequests.remove(request);
            }
        } catch (InvalidAccessTokenException e) {
            logger.debug("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        } catch (FailedResolvingNestUrlException e) {
            logger.debug("Unable to resolve redirect URL", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);
        } catch (FailedSendingNestDataException e) {
            logger.debug("Error sending data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(this::restartStreamingUpdates, 5, SECONDS);
            redirectUrlSupplier.resetCache();
        }
    }

}
