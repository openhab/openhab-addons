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
package org.openhab.binding.tapocontrol.internal.device;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudConnector;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.structures.TapoBridgeConfiguration;
import org.openhab.binding.tapocontrol.internal.test.TapoUDP;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
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
    private final TapoBridgeConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable TapoCloudConnector cloudConnector;
    private final HttpClient httpClient;
    private TapoCredentials credentials;
    private String uid;

    public TapoBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        Thing thing = getThing();
        this.credentials = new TapoCredentials();
        this.cloudConnector = new TapoCloudConnector(this, httpClient);
        this.config = new TapoBridgeConfiguration(thing);
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
        logger.debug("Initializing Bridge");
        this.config.loadSettings();
        loginCloud(config.username, config.password);
        startScheduler();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} Bridge doesn't handle command: {}", this.uid, command);
    }

    @Override
    public void dispose() {
        logger.debug("{} dispose bridge", this.uid);
        stopScheduler();
        updateStatus(ThingStatus.OFFLINE);
        super.dispose();
    }

    /**
     * Start scheduler
     */
    protected void startScheduler() {
        Integer pollingInterval = config.cloudReconnectInterval;
        if (pollingInterval > 0) {
            logger.debug("{} starting bridge sheduler", this.uid);

            this.pollingJob = scheduler.scheduleWithFixedDelay(this::schedulerAction, pollingInterval, pollingInterval,
                    TimeUnit.MINUTES);
        } else {
            stopScheduler();
        }
    }

    /**
     * Stop scheduler
     */
    protected void stopScheduler() {
        if (this.pollingJob != null) {
            logger.debug("{} stopping bridge sheduler", this.uid);
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Scheduler Action
     */
    protected void schedulerAction() {
        loginCloud();
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
     * 
     * @param username
     * @param password
     * @return
     */
    public Boolean loginCloud(String username, String password) {
        credentials.setCredectials(username, password);
        return loginCloud();
    }

    /**
     * Login to Cloud
     * 
     * @return
     */
    public boolean loginCloud() {
        bridgeError.reset(); // reset ErrorHandler
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (username != "" && password != "") {
            logger.debug("{} login with user {}", this.uid, username);
            if (cloudConnector.login(username, password)) {
                logger.debug("login success");
                updateStatus(ThingStatus.ONLINE);
                return true;
            } else {
                logger.debug("login failed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, bridgeError.getMessage());
            }
        } else {
            logger.warn("{} credentials not set", this.uid);
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
     * GET DEVICELIST CONNECTED TO BRIDGE
     * 
     * @return devicelist
     */
    public JsonArray getDeviceList() {
        JsonArray deviceList = new JsonArray();
        if (config.cloudDiscoveryEnabled) {
            logger.trace("{} discover devicelist from cloud", this.uid);
            deviceList = getDeviceListCloud();
        } else if (config.udpDiscoveryEnabled) {
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
        logger.trace("{} getDeviceList", this.uid);
        bridgeError.reset(); // reset ErrorHandler
        JsonArray deviceList = new JsonArray();
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (loginCloud(username, password)) {
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
}
