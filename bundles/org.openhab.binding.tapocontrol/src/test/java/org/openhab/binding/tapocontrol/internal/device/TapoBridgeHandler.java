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
package org.openhab.binding.tapocontrol.internal.device;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.TapoDiscoveryService;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudConnector;
import org.openhab.binding.tapocontrol.internal.api.TapoUDP;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * The {@link TapoBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels with a bridge.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoBridgeHandler.class);
    private final TapoErrorHandler bridgeError = new TapoErrorHandler();
    private final HttpClient httpClient;
    private TapoBridgeConfiguration config;
    private @Nullable ScheduledFuture<?> startupJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @NonNullByDefault({}) TapoCloudConnector cloudConnector;
    private @NonNullByDefault({}) TapoDiscoveryService discoveryService;
    private TapoCredentials credentials;

    private String uid;

    public TapoBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        Thing thing = getThing();
        this.cloudConnector = new TapoCloudConnector(this, httpClient);
        this.config = new TapoBridgeConfiguration();
        this.credentials = new TapoCredentials();
        this.uid = thing.getUID().toString();
        this.httpClient = httpClient;
    }

    /***********************************
     *
     * BRIDGE INITIALIZATION
     *
     ************************************/
    @Override
    /**
     * INIT BRIDGE
     * set credentials and login cloud
     */
    public void initialize() {
        this.config = getConfigAs(TapoBridgeConfiguration.class);
        this.credentials = new TapoCredentials(config.username, config.password);
        activateBridge();
    }

    /**
     * ACTIVATE BRIDGE
     */
    private void activateBridge() {
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        this.startupJob = scheduler.schedule(this::delayedStartUp, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} Bridge doesn't handle command: {}", this.uid, command);
    }

    @Override
    public void dispose() {
        stopScheduler(this.startupJob);
        stopScheduler(this.pollingJob);
        stopScheduler(this.discoveryJob);
        super.dispose();
    }

    /**
     * ACTIVATE DISCOVERY SERVICE
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TapoDiscoveryService.class);
    }

    /**
     * Set DiscoveryService
     * 
     * @param discoveryService
     */
    public void setDiscoveryService(TapoDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /***********************************
     *
     * SCHEDULER
     *
     ************************************/

    /**
     * delayed OneTime StartupJob
     */
    private void delayedStartUp() {
        loginCloud();
        startCloudScheduler();
        startDiscoveryScheduler();
    }

    /**
     * Start CloudLogin Scheduler
     */
    protected void startCloudScheduler() {
        Integer pollingInterval = config.reconnectInterval;
        if (pollingInterval > 0) {
            logger.trace("{} starting bridge cloud sheduler", this.uid);

            this.pollingJob = scheduler.scheduleWithFixedDelay(this::loginCloud, pollingInterval, pollingInterval,
                    TimeUnit.MINUTES);
        } else {
            stopScheduler(this.pollingJob);
        }
    }

    /**
     * Start DeviceDiscovery Scheduler
     */
    protected void startDiscoveryScheduler() {
        Integer pollingInterval = config.discoveryInterval;
        if (config.cloudDiscovery && pollingInterval > 0) {
            logger.trace("{} starting bridge discovery sheduler", this.uid);

            this.discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverDevices, 0, pollingInterval,
                    TimeUnit.MINUTES);
        } else {
            stopScheduler(this.discoveryJob);
        }
    }

    /**
     * Stop scheduler
     * 
     * @param scheduler ScheduledFeature<?> which schould be stopped
     */
    protected void stopScheduler(@Nullable ScheduledFuture<?> scheduler) {
        if (scheduler != null) {
            scheduler.cancel(true);
            scheduler = null;
        }
    }

    /***********************************
     *
     * ERROR HANDLER
     *
     ************************************/
    /**
     * return device Error
     * 
     * @return
     */
    public TapoErrorHandler getError() {
        return this.bridgeError;
    }

    /**
     * set device error
     * 
     * @param tapoError TapoErrorHandler-Object
     */
    public void setError(TapoErrorHandler tapoError) {
        this.bridgeError.set(tapoError);
    }

    /***********************************
     *
     * BRIDGE COMMUNICATIONS
     *
     ************************************/

    /**
     * Login to Cloud
     * 
     * @return
     */
    public boolean loginCloud() {
        bridgeError.reset(); // reset ErrorHandler
        if (!config.username.isBlank() && !config.password.isBlank()) {
            logger.debug("{} login with user {}", this.uid, config.username);
            if (cloudConnector.login(config.username, config.password)) {
                updateStatus(ThingStatus.ONLINE);
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, bridgeError.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "credentials not set");
        }
        return false;
    }

    /***********************************
     *
     * DEVICE DISCOVERY
     *
     ************************************/

    /**
     * START DEVICE DISCOVERY
     */
    public void discoverDevices() {
        this.discoveryService.startScan();
    }

    /**
     * GET DEVICELIST CONNECTED TO BRIDGE
     * 
     * @return devicelist
     */
    public JsonArray getDeviceList() {
        JsonArray deviceList = new JsonArray();
        if (config.cloudDiscovery) {
            logger.trace("{} discover devicelist from cloud", this.uid);
            deviceList = getDeviceListCloud();
        } else if (config.udpDiscovery) {
            logger.trace("{} discover devicelist from udp", this.uid);
            deviceList = getDeviceListUDP();
        }
        return deviceList;
    }

    /**
     * GET DEVICELIST FROM CLOUD
     * returns all devices stored in cloud
     * 
     * @return deviceList from cloud
     */
    private JsonArray getDeviceListCloud() {
        logger.trace("{} getDeviceList from cloud", this.uid);
        bridgeError.reset(); // reset ErrorHandler
        JsonArray deviceList = new JsonArray();
        if (loginCloud()) {
            deviceList = this.cloudConnector.getDeviceList();
        }
        return deviceList;
    }

    /**
     * GET DEVICELIST UDP
     * return devices discovered by UDP
     * 
     * @return deviceList from udp
     */
    public JsonArray getDeviceListUDP() {
        bridgeError.reset(); // reset ErrorHandler
        TapoUDP udpDiscovery = new TapoUDP(credentials);
        return udpDiscovery.udpScan();
    }

    /***********************************
     *
     * BRIDGE GETTERS
     *
     ************************************/

    public TapoCredentials getCredentials() {
        return this.credentials;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public ThingUID getUID() {
        return getThing().getUID();
    }

    public TapoBridgeConfiguration getBridgeConfig() {
        return this.config;
    }
}
