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
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.ErrorData;
import org.openhab.binding.nest.internal.data.NestDevices;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.exceptions.FailedResolvingNestUrlException;
import org.openhab.binding.nest.internal.exceptions.FailedSendingNestDataException;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.openhab.binding.nest.internal.listener.NestDeviceDataListener;
import org.openhab.binding.nest.internal.listener.NestStreamingDataListener;
import org.openhab.binding.nest.internal.rest.NestAuthorizer;
import org.openhab.binding.nest.internal.rest.NestStreamingRestClient;
import org.openhab.binding.nest.internal.rest.NestUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This bridge handler connects to Nest and handles all the API requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author David Bennett - initial contribution
 * @author Martin van Wingerden - Use listeners not only for discovery but for all data processing
 * @author Wouter Born - Improve exception and URL redirect handling
 */
public class NestBridgeHandler extends BaseBridgeHandler implements NestStreamingDataListener {
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private final List<NestDeviceDataListener> listeners = new CopyOnWriteArrayList<>();
    private final List<NestUpdateRequest> nestUpdateRequests = new CopyOnWriteArrayList<>();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    private NestAuthorizer authorizer;
    private NestBridgeConfiguration config;
    private ScheduledFuture<?> initializeJob;
    private ScheduledFuture<?> transmitJob;
    private NestRedirectUrlSupplier redirectUrlSupplier;
    private NestStreamingRestClient streamingRestClient;

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
                redirectUrlSupplier = new NestRedirectUrlSupplier(getHttpHeaders());
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
     * Allows the NestRedirectUrlSupplier to be overriden in tests.
     *
     * @return the NestRedirectUrlSupplier
     */
    protected NestRedirectUrlSupplier getRedirectUrlSupplier() {
        return this.redirectUrlSupplier;
    }

    private void startStreamingUpdates() {
        synchronized (this) {
            try {
                streamingRestClient = new NestStreamingRestClient(getExistingOrNewAccessToken(),
                        getRedirectUrlSupplier(), scheduler);
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

    private void restartStreamingUpdates() {
        synchronized (this) {
            stopStreamingUpdates();
            startStreamingUpdates();
        }
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

    /**
     * Handles an incoming command update
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            broadcastLastReceivedTopLevelData();
        }
    }

    public void broadcastLastReceivedTopLevelData() {
        if (streamingRestClient != null && streamingRestClient.getLastReceivedTopLevelData() != null) {
            broadcastTopLevelData(streamingRestClient.getLastReceivedTopLevelData());
        }
    }

    public void broadcastTopLevelData(TopLevelData data) {
        if (data.getDevices() != null) {
            broadcastDevices(data.getDevices());
        }
        if (data.getStructures() != null) {
            broadcastStructures(data.getStructures().values());
        }
    }

    private void broadcastDevices(NestDevices devices) {
        listeners.forEach(listener -> broadcastDevices(listener, devices));
    }

    private void broadcastDevices(NestDeviceDataListener listener, NestDevices devices) {
        if (devices.getThermostats() != null) {
            devices.getThermostats().values().forEach(listener::onNewNestThermostatData);
        }
        if (devices.getCameras() != null) {
            devices.getCameras().values().forEach(listener::onNewNestCameraData);
        }
        if (devices.getSmokeDetectors() != null) {
            devices.getSmokeDetectors().values().forEach(listener::onNewNestSmokeDetectorData);
        }
    }

    private void broadcastStructures(Collection<Structure> structures) {
        structures.forEach(structure -> listeners.forEach(l -> l.onNewNestStructureData(structure)));
    }

    private void broadcastStructures(NestDeviceDataListener listener, Collection<Structure> structures) {
        structures.forEach(listener::onNewNestStructureData);
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

    /**
     * @param nestDeviceDataListener The device added listener to add
     */
    public boolean addDeviceDataListener(NestDeviceDataListener listener) {
        boolean success = listeners.add(listener);
        if (streamingRestClient != null) {
            scheduler.schedule(() -> {
                TopLevelData data = streamingRestClient.getLastReceivedTopLevelData();
                if (data != null) {
                    if (data.getDevices() != null) {
                        broadcastDevices(listener, data.getDevices());
                    }
                    if (data.getStructures() != null) {
                        broadcastStructures(listener, data.getStructures().values());
                    }
                } else {
                    logger.debug("Last received TopLevelData is null");
                }
            }, 1, SECONDS);
        } else {
            logger.debug("streamingRestClient is null");
        }
        return success;
    }

    /**
     * @param nestDeviceDataListener The device added listener to remove
     */
    public boolean removeDeviceDataListener(NestDeviceDataListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Adds the update request into the queue for doing something with, send immediately if the queue is empty.
     */
    void addUpdateRequest(NestUpdateRequest request) {
        nestUpdateRequests.add(request);
        scheduleTransmitJobForPendingRequests();
    }

    private void scheduleTransmitJobForPendingRequests() {
        if (!nestUpdateRequests.isEmpty() && (transmitJob == null || transmitJob.isDone())) {
            transmitJob = scheduler.schedule(this::transmitQueue, 0, SECONDS);
        }
    }

    private void transmitQueue() {
        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Not transmitting events because bridge is OFFLINE");
            return;
        }

        try {
            while (nestUpdateRequests.size() > 0) {
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
            getRedirectUrlSupplier().resetCache();
        }
    }

    private void jsonToPutUrl(NestUpdateRequest request)
            throws FailedSendingNestDataException, InvalidAccessTokenException, FailedResolvingNestUrlException {
        try {
            String url = request.getUpdateUrl().replaceFirst(NestBindingConstants.NEST_URL,
                    getRedirectUrlSupplier().getRedirectUrl());
            logger.debug("Putting data to: {}", url);

            String jsonContent = gson.toJson(request.getValues());
            logger.debug("PUT content: {}", jsonContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            String jsonResponse = HttpUtil.executeUrl("PUT", url, getHttpHeaders(), inputStream, JSON_CONTENT_TYPE,
                    REQUEST_TIMEOUT);
            logger.debug("PUT response: {}", jsonResponse);

            ErrorData error = gson.fromJson(jsonResponse, ErrorData.class);
            if (StringUtils.isNotBlank(error.getError())) {
                logger.debug("Nest API error: {}", error);
                logger.warn("Nest API error: {}", error.getMessage());
            }
        } catch (IOException e) {
            throw new FailedSendingNestDataException("Failed to send data", e);
        }
    }

    private Properties getHttpHeaders() throws InvalidAccessTokenException {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Authorization", "Bearer " + getExistingOrNewAccessToken());
        httpHeaders.put("Content-Type", JSON_CONTENT_TYPE);
        return httpHeaders;
    }

    /**
     * Called to start the discovery scan. Forces a data refresh.
     */
    public void startDiscoveryScan() {
        broadcastLastReceivedTopLevelData();
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
        if (data.getDevices() != null) {
            broadcastDevices(data.getDevices());
        }
        if (data.getStructures() != null) {
            broadcastStructures(data.getStructures().values());
        }
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Receiving streaming data");
    }

}
