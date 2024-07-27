/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.devices.bridge;

import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudConnector;
import org.openhab.binding.tapocontrol.internal.discovery.TapoDiscoveryService;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
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
    private TapoBridgeConfiguration config = new TapoBridgeConfiguration();
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> startupJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @NonNullByDefault({}) TapoCloudConnector cloudConnector;
    private @NonNullByDefault({}) TapoDiscoveryService discoveryService;
    private TapoCredentials credentials;

    private String uid;

    public TapoBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        Thing thing = getThing();
        cloudConnector = new TapoCloudConnector(this);
        credentials = new TapoCredentials();
        uid = thing.getUID().toString();
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
        config = getConfigAs(TapoBridgeConfiguration.class);
        credentials = new TapoCredentials(config.username, config.password);
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
        discoveryService.startBackgroundDiscovery();
    }

    /**
     * Start CloudLogin Scheduler
     */
    protected void startCloudScheduler() {
        int pollingInterval = config.reconnectInterval;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        if (pollingInterval > 0) {
            logger.debug("{} starting cloudScheduler with interval {} {}", this.uid, pollingInterval, timeUnit);

            this.pollingJob = scheduler.scheduleWithFixedDelay(this::loginCloud, pollingInterval, pollingInterval,
                    timeUnit);
        } else {
            logger.debug("({}) cloudScheduler disabled with config '0'", uid);
            stopScheduler(this.pollingJob);
        }
    }

    /**
     * Stop scheduler
     * 
     * @param scheduler ScheduledFeature which should be stopped
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
    public TapoErrorHandler getErrorHandler() {
        return bridgeError;
    }

    /**
     * set device error
     * 
     * @param tapoError TapoErrorHandler-Object
     */
    public void setError(TapoErrorHandler tapoError) {
        bridgeError.set(tapoError);
        handleConnectionState();
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
        if (credentials.areSet()) {
            try {
                cloudConnector.login(credentials);
            } catch (Exception e) {
                logger.trace("({}) login to cloud failed", this.uid);
            }
        } else {
            bridgeError.raiseError(ERR_BINDING_CREDENTIALS, "credentials not set");
        }
        handleConnectionState();
        return cloudConnector.isLoggedIn();
    }

    /**
     * Handle Connection state
     */
    private void handleConnectionState() {
        if (cloudConnector.isLoggedIn() && !bridgeError.hasError()) {
            updateStatus(ThingStatus.ONLINE);
        } else if (bridgeError.hasError()) {
            switch (bridgeError.getType()) {
                case COMMUNICATION_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, bridgeError.getMessage());
                    break;
                case CONFIGURATION_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, bridgeError.getMessage());
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, bridgeError.getMessage());
            }
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        }
    }

    /***********************************
     *
     * BRIDGE GETTERS
     *
     ************************************/

    public TapoCredentials getCredentials() {
        return credentials;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public TapoCloudConnector getCloudConnector() {
        return cloudConnector;
    }

    public TapoDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public ThingUID getUID() {
        return getThing().getUID();
    }

    public TapoBridgeConfiguration getBridgeConfig() {
        return config;
    }
}
