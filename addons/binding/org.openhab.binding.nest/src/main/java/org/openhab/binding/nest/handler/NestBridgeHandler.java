/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.nest.NestBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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
import org.openhab.binding.nest.internal.NestAuthorizer;
import org.openhab.binding.nest.internal.NestDeviceDataListener;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.ErrorData;
import org.openhab.binding.nest.internal.data.NestDevices;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.openhab.binding.nest.internal.exceptions.FailedResolvingNestUrlException;
import org.openhab.binding.nest.internal.exceptions.FailedRetrievingNestDataException;
import org.openhab.binding.nest.internal.exceptions.FailedSendingNestDataException;
import org.openhab.binding.nest.internal.exceptions.InvalidAccessTokenException;
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
public class NestBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private final List<NestDeviceDataListener> listeners = new ArrayList<>();
    private final List<NestUpdateRequest> nestUpdateRequests = new CopyOnWriteArrayList<>();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    private ScheduledFuture<?> refreshDataJob;
    private NestAuthorizer authorizer;
    private NestBridgeConfiguration config;
    private ScheduledFuture<?> sender;
    private String redirectUrl;

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

        logger.debug("Product ID      {}", config.productId);
        logger.debug("Product Secret  {}", config.productSecret);
        logger.debug("Pincode         {}", config.pincode);

        try {
            logger.debug("Access Token    {}", getExistingOrNewAccessToken());
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Starting poll query");
        } catch (InvalidAccessTokenException e) {
            logger.debug("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        }

        restartAutomaticRefresh();

        logger.debug("Finished initializing Nest bridge handler");
    }

    /**
     * Do something useful when the configuration update happens. Triggers changing
     * polling intervals as well as re-doing the access token.
     */
    @Override
    public void updateConfiguration(Configuration configuration) {
        logger.debug("Config update");
        super.updateConfiguration(configuration);
        restartAutomaticRefresh();
    }

    private void restartAutomaticRefresh() {
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
        this.authorizer = null;
        this.refreshDataJob = null;
        this.redirectUrl = null;
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
        logger.debug("Starting data refresh");
        try {
            if (redirectUrl == null) {
                redirectUrl = resolveRedirectUrl();
            }

            String jsonResponse = jsonFromGetUrl();
            logger.debug("GET response: {}", jsonResponse);

            ErrorData error = gson.fromJson(jsonResponse, ErrorData.class);
            if (StringUtils.isBlank(error.getError())) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Successfully requested new data from Nest");

                TopLevelData data = gson.fromJson(jsonResponse, TopLevelData.class);
                if (data.getDevices() != null) {
                    broadcastDevices(data.getDevices());
                }
                if (data.getStructures() != null) {
                    broadcastStructure(data.getStructures().values());
                }
            } else {
                logger.debug("Nest API error: {}", error);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Nest API error: " + error.getMessage());
            }
        } catch (InvalidAccessTokenException e) {
            logger.debug("Invalid access token", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Token is invalid and could not be refreshed: " + e.getMessage());
        } catch (FailedResolvingNestUrlException e) {
            logger.debug("Unable to resolve redirect URL", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (FailedRetrievingNestDataException e) {
            logger.debug("Error retrieving data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error retrieving data " + e.getMessage());
        }
        logger.debug("Finished data refresh");
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

    private String jsonFromGetUrl() throws FailedRetrievingNestDataException, InvalidAccessTokenException {
        try {
            logger.debug("Fetching data from {}", redirectUrl);
            return HttpUtil.executeUrl("GET", redirectUrl, getHttpHeaders(), null, null, 5000);
        } catch (IOException e) {
            throw new FailedRetrievingNestDataException(e);
        }
    }

    private synchronized void startAutomaticRefresh() {
        if (config == null || config.refreshInterval < MIN_SECONDS_BETWEEN_API_CALLS) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not schedule polling job, refresh interval should be set and be more then 60 seconds");
            return;
        }

        if (refreshDataJob == null || refreshDataJob.isCancelled()) {
            refreshDataJob = scheduler.scheduleWithFixedDelay(this::refreshData, 0, config.refreshInterval, SECONDS);
            logger.debug("Started refreshData job");
        } else {
            logger.debug("refreshData job is already scheduled");
        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (refreshDataJob != null && !refreshDataJob.isCancelled()) {
            refreshDataJob.cancel(true);
            refreshDataJob = null;
            logger.debug("Stopping refreshDataJob");
        } else {
            logger.debug("refreshDataJob is already stopped");
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
        } catch (FailedSendingNestDataException e) {
            logger.debug("Error sending data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void jsonToPutUrl(NestUpdateRequest request)
            throws FailedSendingNestDataException, InvalidAccessTokenException {
        try {
            String url = request.getUpdateUrl().replaceFirst(NestBindingConstants.NEST_URL, redirectUrl);
            logger.debug("Putting data to: {}", url);

            String jsonContent = gson.toJson(request.getValues());
            logger.debug("PUT content: {}", jsonContent);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
            String jsonResponse = HttpUtil.executeUrl("PUT", url, getHttpHeaders(), inputStream, JSON_CONTENT_TYPE,
                    5000);
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
     * Resolves the redirect URL for calls using the {@link NestBindingConstants#NEST_URL}.
     *
     * The Jetty client used by {@link HttpUtil} will not pass the Authorization header after a redirect resulting in
     * "401 Unauthorized error" issues.
     *
     * Note that this workaround currently does not use any configured proxy like {@link HttpUtil} does.
     *
     * @see https://developers.nest.com/documentation/cloud/how-to-handle-redirects
     */
    private String resolveRedirectUrl() throws FailedResolvingNestUrlException, InvalidAccessTokenException {
        HttpClient httpClient = new HttpClient(new SslContextFactory());
        httpClient.setFollowRedirects(false);

        Request request = httpClient.newRequest(NestBindingConstants.NEST_URL).method(HttpMethod.GET).timeout(5,
                TimeUnit.SECONDS);
        Properties httpHeaders = getHttpHeaders();
        for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
            request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
        }

        ContentResponse response;
        try {
            httpClient.start();
            response = request.send();
            httpClient.stop();
        } catch (Exception e) {
            throw new FailedResolvingNestUrlException("Failed to resolve redirect URL", e);
        }

        int status = response.getStatus();
        String redirectUrl = response.getHeaders().get(HttpHeader.LOCATION);

        if (status != HttpStatus.TEMPORARY_REDIRECT_307) {
            logger.debug("Redirect status: {}", status);
            logger.debug("Redirect response: {}", response.getContentAsString());
            throw new FailedResolvingNestUrlException("Failed to get redirect URL, expected status "
                    + HttpStatus.TEMPORARY_REDIRECT_307 + " but was " + status);
        } else if (StringUtils.isEmpty(redirectUrl)) {
            throw new FailedResolvingNestUrlException("Redirect URL is empty");
        }

        redirectUrl = redirectUrl.endsWith("/") ? redirectUrl.substring(0, redirectUrl.length() - 1) : redirectUrl;
        logger.debug("Redirect URL: {}", redirectUrl);
        return redirectUrl;
    }

    /**
     * Called to start the discovery scan. Forces a data refresh.
     */
    public void startDiscoveryScan() {
        refreshData();
    }
}
