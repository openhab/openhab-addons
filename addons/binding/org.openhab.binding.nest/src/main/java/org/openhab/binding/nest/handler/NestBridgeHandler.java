/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.openhab.binding.nest.internal.NestAccessToken;
import org.openhab.binding.nest.internal.NestDeviceDataListener;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.NestDevices;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.exceptions.FailedRetrievingNestDataException;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This bridge handler connects to Nest and handles all the API requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author David Bennett - initial contribution
 */
public class NestBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private final List<NestDeviceDataListener> listeners = new ArrayList<>();
    private final List<NestUpdateRequest> nestUpdateRequests = new CopyOnWriteArrayList<>();
    private final Gson gson;

    private ScheduledFuture<?> pollingJob;
    private NestAccessToken accessToken;
    private NestBridgeConfiguration config;
    private ScheduledFuture<?> sender;

    /**
     * Creates the bridge handler to connect to Nest.
     *
     * @param bridge The bridge to connect to Nest with.
     */
    public NestBridgeHandler(Bridge bridge) {
        super(bridge);
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    }

    /**
     * Initialize the connection to Nest.
     */
    @Override
    public void initialize() {
        logger.debug("Initialize the Nest bridge handler");

        config = getConfigAs(NestBridgeConfiguration.class);
        logger.debug("Client Id       {}", config.clientId);
        logger.debug("Client Secret   {}", config.clientSecret);
        logger.debug("Pincode         {}", config.pincode);
        accessToken = new NestAccessToken(config);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Starting poll query");

        startAutomaticRefresh();
    }

    /**
     * Do something useful when the configuration update happens. Triggers changing
     * polling intervals as well as re-doing the access token.
     */
    @Override
    public void updateConfiguration(Configuration configuration) {
        logger.debug("Config update");
        super.updateConfiguration(configuration);
        synchronized (this) {
            stopAutomaticRefresh();
            startAutomaticRefresh();
        }
    }

    /**
     * Clean up the handler.
     */
    @Override
    public void dispose() {
        logger.debug("Nest bridge disposed");
        stopAutomaticRefresh();
        this.accessToken = null;
        this.pollingJob = null;
    }

    /**
     * Handles an incoming command update
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received");
            refreshData();
        }
    }

    /**
     * Read the data from Nest and then forward it to anyone listening.
     */
    public void refreshData() {
        logger.trace("starting refreshData");
        try {
            String uri = buildQueryString();
            String data = jsonFromGetUrl(uri);
            logger.debug("Data from Nest {}", data);

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Successfully requested new data from Nest");

            // Now convert the incoming data into something more useful.
            TopLevelData newData = gson.fromJson(data, TopLevelData.class);
            broadcastDevices(newData.getDevices());
            broadcastStructure(newData.getStructures().values());
        } catch (InvalidAccessTokenException e) {
            logger.warn("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        } catch (FailedRetrievingNestDataException e) {
            logger.warn("Error retrieving data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error retrieving data " + e.getMessage());
        }

    }

    private void broadcastDevices(NestDevices devices) {
        listeners.forEach(listener -> {
            if (devices.getThermostats() != null) {
                devices.getThermostats().values().forEach(listener::onNewNestThermostatData);
            }
            if (devices.getCameras() != null) {
                devices.getCameras().values().forEach(listener::onNewNestCameraData);
            }
            if (devices.getSmokeDetectors() != null) {
                devices.getSmokeDetectors().values().forEach(listener::onNewNestSmokeDetectorData);
            }
        });
    }

    private void broadcastStructure(Collection<Structure> structures) {
        structures.forEach(structure -> listeners.forEach(l -> l.onNewNestStructureData(structure)));
    }

    private String buildQueryString() throws InvalidAccessTokenException {
        String stringAccessToken;

        if (StringUtils.isEmpty(config.accessToken)) {
            stringAccessToken = accessToken.getAccessToken();

            // Update the configuration and persist to the database.
            Configuration bridgeConfiguration = editConfiguration();
            bridgeConfiguration.put(NestBridgeConfiguration.ACCESS_TOKEN, stringAccessToken);
            updateConfiguration(bridgeConfiguration);
            logger.debug("Retrieved fresh access token: {}", stringAccessToken);
        } else {
            stringAccessToken = config.accessToken;
            logger.debug("Re-using access token from configuration: {}", stringAccessToken);
        }

        StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_URL).append("?auth=")
                .append(stringAccessToken);
        logger.debug("Constructed URL: {}", urlBuilder);
        return urlBuilder.toString();
    }

    private String jsonFromGetUrl(String url) throws FailedRetrievingNestDataException {
        try {
            logger.debug("Fetching data from {}", url);
            return HttpUtil.executeUrl("GET", url, 5000);
        } catch (IOException e) {
            throw new FailedRetrievingNestDataException(e);
        }
    }

    private synchronized void startAutomaticRefresh() {
        if (config == null || config.refreshInterval < 60){
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not schedule polling job, refresh interval should be set and be more then 60 seconds");
            return;
        }

        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.refreshInterval, SECONDS);
        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * @param nestDeviceDataListener The device added listener to add
     */
    public boolean addDeviceDataListener(NestDeviceDataListener nestDeviceDataListener) {
        boolean success = listeners.add(nestDeviceDataListener);
        scheduler.schedule(this::refreshData, 1, SECONDS);
        return success;
    }

    /**
     * @param nestDeviceDataListener The device added listener to remove
     */
    public boolean removeDeviceDataListener(NestDeviceDataListener nestDeviceDataListener) {
        return listeners.remove(nestDeviceDataListener);
    }

    /**
     * Adds the update request into the queue for doing something with, send immediately if the queue is empty.
     */
    void addUpdateRequest(NestUpdateRequest request) {
        nestUpdateRequests.add(request);
        if (sender == null || sender.isDone()) {
            sender = scheduler.schedule(this::transmitQueue, 0, SECONDS);
        }
    }

    private void transmitQueue() {
        if (getBridge().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Not transmitting events because bridge is OFFLINE");
            return;
        }

        for (NestUpdateRequest updateRequest : nestUpdateRequests) {
            jsonToPutUrl(updateRequest);
        }
    }


    private void jsonToPutUrl(NestUpdateRequest request) {
        try {
            logger.debug("Putting data to {}", request.getUpdateUrl());
            String content = gson.toJson(request.getValues());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
            HttpUtil.executeUrl("PUT", request.getUpdateUrl(), null, inputStream, null, 5000);
        } catch (IOException e) {
            // TODO we could or maybe should check the cause for authentication failure.
            // FIXME we should handle this here otherwise it will get lost
            logger.error("Failed to send data", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Called to start the discovery scan. Forces a data refresh.
     */
    public void startDiscoveryScan() {
        refreshData();
    }
}
