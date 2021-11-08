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

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudConnector;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    private Configuration config;
    private TapoCredentials credentials;
    private String uid;
    protected final TapoCloudConnector cloudConnector;
    protected @Nullable ScheduledFuture<?> pollingJob;

    public TapoBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.credentials = new TapoCredentials();
        this.config = getThing().getConfiguration();
        this.cloudConnector = new TapoCloudConnector(getThing(), httpClient);
        this.uid = getThing().getUID().toString();
    }

    @Override
    /**
     * INIT BRIDGE
     * set credentials and login cloud
     */
    public void initialize() {
        logger.debug("Initializing Bridge");
        String username = "";
        String password = "";

        try {
            this.config = getThing().getConfiguration();
            username = config.get(CONFIG_EMAIL).toString();
            password = config.get(CONFIG_PASS).toString();
        } catch (Exception e) {
            logger.error("{} Bridge configuration error: '{}'", this.uid, e.toString());
        }
        loginCloud(username, password);
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
        logger.debug("{} starting bridge sheduler", this.uid);
        String interval = getThing().getConfiguration().get(CONFIG_CLOUD_UPDATE_INTERVAL).toString();
        Integer pollingInterval = Integer.valueOf(interval);

        this.pollingJob = scheduler.scheduleWithFixedDelay(this::schedulerAction, 0, pollingInterval, TimeUnit.HOURS);
    }

    /**
     * Stop scheduler
     */
    protected void stopScheduler() {
        logger.debug("{} stopping bridge sheduler", this.uid);
        if (this.pollingJob != null) {
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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cloudConnector.errorMessage());
            }
        } else {
            logger.warn("{} credentials not set", this.uid);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "credentials not set");
        }
        return false;
    }

    /**
     * Get DeviceList
     * 
     * @return
     */
    public JsonArray getDeviceList() {
        logger.trace("{} getDeviceList", this.uid);
        JsonArray deviceList = new JsonArray();
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (loginCloud(username, password)) {
            deviceList = this.cloudConnector.getDeviceList();
        }
        return deviceList;
    }
}
