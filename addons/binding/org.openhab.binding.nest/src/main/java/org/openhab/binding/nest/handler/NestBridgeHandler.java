/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.discovery.NestDiscoveryService;
import org.openhab.binding.nest.internal.NestAccessToken;
import org.openhab.binding.nest.internal.NestDeviceAddedListener;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.NestDevices;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.data.TopLevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This bridge handler connects to nest and handles all the api requests. It pulls down the
 * updated data, polls the system and does all the co-ordination with the other handlers
 * to get the data updated to the correct things.
 *
 * @author David Bennett - initial contribution
 */
public class NestBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(NestBridgeHandler.class);

    private List<NestDeviceAddedListener> listeners = new ArrayList<NestDeviceAddedListener>();

    // Will refresh the data each time it runs.
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };

    private ScheduledFuture<?> pollingJob;
    private NestAccessToken accessToken;
    private List<NestUpdateRequest> nestUpdateRequests = new ArrayList<>();
    private TopLevelData lastDataQuery;
    private HttpClient httpClient;

    private final GsonBuilder builder;

    /**
     * Creates the bridge handler to connect to nest.
     *
     * @param bridge The bridge to connect to nest with.
     */
    public NestBridgeHandler(Bridge bridge) {
        super(bridge);
        builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    /**
     * Initialize the connection to nest.
     */
    @Override
    public void initialize() {
        logger.debug("Initialize the Nest bridge handler");

        // Create the jetty client and configure for https.
        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setConnectTimeout(30000);

        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        startAutomaticRefresh(config.refreshInterval);
        updateAccessToken();

        logger.debug("Client Id       {}.", config.clientId);
        logger.debug("Client Secret   {}.", config.clientSecret);
        logger.debug("Pincode         {}.", config.pincode);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Starting poll query");
    }

    /**
     * Do something useful when the configuration update happens. Triggers changing
     * polling intervals as well as re-doing the access token.
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Config update");
        super.handleConfigurationUpdate(configurationParameters);

        updateAccessToken();

        stopAutomaticRefresh();
        startAutomaticRefresh(getConfigAs(NestBridgeConfiguration.class).refreshInterval);
    }

    /**
     * Clean up the handler.
     */
    @Override
    public void dispose() {
        logger.debug("Nest bridge disposed");
        stopAutomaticRefresh();
        this.accessToken = null;
        this.lastDataQuery = null;
        this.pollingJob = null;
        this.pollingRunnable = null;
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
     * Read the data from nest and then parse it into something useful.
     */
    private void refreshData() {
        logger.trace("starting refreshData");
        NestBridgeConfiguration config = getConfigAs(NestBridgeConfiguration.class);
        try {
            String uri = buildQueryString(config);
            String data = jsonFromGetUrl(uri, config);
            logger.error("Data from nest {}", data);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Received update from nest");
            // Now convert the incoming data into something more useful.
            Gson gson = builder.create();
            TopLevelData newData = gson.fromJson(data, TopLevelData.class);
            if (newData != null) {
                lastDataQuery = newData;
            } else {
                newData = lastDataQuery;
            }
            // Turn this new data into things and stuff.
            compareThings(newData.getDevices());
            compareStructure(newData.getStructures().values());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Error parsing data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error parsing data " + e.getMessage());
        }

    }

    private Thing getDevice(String deviceId, List<Thing> things) {
        for (Thing thing : things) {
            String thingDeviceId = thing.getUID().getId();
            if (thingDeviceId.equals(deviceId)) {
                return thing;
            }
        }
        return null;
    }

    private void compareThings(NestDevices devices) {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();

        for (Thermostat thermostat : devices.getThermostats().values()) {
            Thing thingThermostat = getDevice(thermostat.getDeviceId(), things);
            if (thingThermostat != null) {
                NestThermostatHandler handler = (NestThermostatHandler) thingThermostat.getHandler();
                if (handler != null) {
                    handler.updateThermostat(thermostat);
                }
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.debug("Found new thermostat {}", thermostat.getDeviceId());
                    listener.onThermostatAdded(thermostat);
                }
            }
        }
        for (Camera camera : devices.getCameras().values()) {
            Thing thingCamera = getDevice(camera.getDeviceId(), things);
            if (thingCamera != null) {
                NestCameraHandler handler = (NestCameraHandler) thingCamera.getHandler();
                if (handler != null) {
                    handler.updateCamera(camera);
                }
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.debug("Found new camera. {}", camera.getDeviceId());
                    listener.onCameraAdded(camera);
                }
            }
        }
        for (SmokeDetector smokeDetector : devices.getSmokeDetectors().values()) {
            Thing thingSmokeDetector = getDevice(smokeDetector.getDeviceId(), things);
            if (thingSmokeDetector != null) {
                NestSmokeDetectorHandler handler = (NestSmokeDetectorHandler) thingSmokeDetector.getHandler();
                if (handler != null) {
                    handler.updateSmokeDetector(smokeDetector);
                }
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.debug("Found new smoke detector. {}", smokeDetector.getDeviceId());
                    listener.onSmokeDetectorAdded(smokeDetector);
                }
            }
        }
    }

    private void compareStructure(Collection<Structure> structures) {
        Bridge bridge = getThing();
        List<Thing> things = bridge.getThings();

        for (Structure struct : structures) {
            Thing thingStructure = getDevice(struct.getStructureId(), things);
            if (thingStructure != null) {
                NestStructureHandler handler = (NestStructureHandler) thingStructure.getHandler();
                if (handler != null) {
                    handler.updateStructure(struct);
                }
            } else {
                for (NestDeviceAddedListener listener : listeners) {
                    logger.debug("Found new structure {}", struct.getStructureId());
                    listener.onStructureAdded(struct);
                }
            }
        }
    }

    private String buildQueryString(NestBridgeConfiguration config)
            throws InterruptedException, TimeoutException, ExecutionException {
        logger.debug("Making url with access token {}", config.accessToken);
        StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_URL);
        urlBuilder.append("?auth=");
        String stringAccessToken;
        if (config.accessToken == null) {
            stringAccessToken = accessToken.getAccessToken();
            // Update the configuration and persist to the database.
            Configuration bridgeConfiguration = editConfiguration();
            bridgeConfiguration.put("accessToken", stringAccessToken);
            updateConfiguration(bridgeConfiguration);
        } else {
            stringAccessToken = config.accessToken;
        }
        urlBuilder.append(stringAccessToken);
        logger.debug("Made url {}", urlBuilder.toString());
        return urlBuilder.toString();
    }

    private String jsonFromGetUrl(final String url, NestBridgeConfiguration config)
            throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("connecting to {}", url);
        ContentResponse response = this.httpClient.GET(url);
        return response.getContentAsString();
    }

    private synchronized void startAutomaticRefresh(int refreshInterval) {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopAutomaticRefresh() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void updateAccessToken() {
        accessToken = new NestAccessToken(getConfigAs(NestBridgeConfiguration.class), this.httpClient);

        try {
            logger.debug("New Access Token {}.", accessToken.getAccessToken());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error getting Access Token.", e);
        }
    }

    /**
     * @param nestDiscoveryService The device added listener to add
     */
    public void addDeviceAddedListener(NestDeviceAddedListener nestDiscoveryService) {
        this.listeners.add(nestDiscoveryService);
    }

    /**
     * @param nestDiscoveryService The device added listener to remove
     */
    public void removeDeviceAddedListener(NestDiscoveryService nestDiscoveryService) {
        this.listeners.remove(nestDiscoveryService);
    }

    /** Adds the update request into the queue for doing something with, send immediately if the queue is empty. */
    public void addUpdateRequest(NestUpdateRequest request) {
        nestUpdateRequests.add(request);
    }

    /**
     * Called to start the discovery scan. Forces a data refresh.
     */
    public void startDiscoveryScan() {
        refreshData();
    }
}
