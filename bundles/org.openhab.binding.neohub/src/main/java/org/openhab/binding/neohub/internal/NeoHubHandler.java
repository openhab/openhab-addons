/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;

import org.openhab.binding.neohub.internal.NeoHubInfoResponse.DeviceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeoHubHandler} is the openHAB Handler for NeoHub devices
 *
 * @author Andrew Fiddian-Green - Initial contribution (v2.x binding code)
 * @author Sebastian Prehn - Initial contribution (v1.x hub communication)
 * 
 */
public class NeoHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NeoHubHandler.class);

    private NeoHubConfiguration config;
    private NeoHubSocket socket;

    private NeoHubInfoResponse lastInfoResponse = null;

    private ScheduledFuture<?> lazyPollingScheduler;
    private ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    public NeoHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // future: currently there is nothing to do for a NeoHub
    }

    @Override
    public void initialize() {
        config = getConfigAs(NeoHubConfiguration.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "parameter(s) hostName, portNumber, pollingInterval must be set!");
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("hostname={}", config.hostName);

        if (config.hostName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "parameter hostName must be set!");
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("port={}", config.portNumber);

        if (config.portNumber <= 0 || config.portNumber > 0xFFFF) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "portNumber is invalid!");
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("polling interval={}", config.pollingInterval);

        if (config.pollingInterval < FAST_POLL_INTERVAL || config.pollingInterval > LAZY_POLL_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                    .format("pollingInterval must be in range [%d..%d]!", FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL));
            return;
        }

        socket = new NeoHubSocket(config.hostName, config.portNumber);

        if (logger.isDebugEnabled())
            logger.debug("start background polling..");

        // create a "lazy" polling scheduler
        if (lazyPollingScheduler == null || lazyPollingScheduler.isCancelled()) {
            lazyPollingScheduler = scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute,
                    config.pollingInterval, config.pollingInterval, TimeUnit.SECONDS);
        }

        // create a "fast" polling scheduler
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
        if (fastPollingScheduler == null || fastPollingScheduler.isCancelled()) {
            fastPollingScheduler = scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute,
                    FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
        }

        updateStatus(ThingStatus.UNKNOWN);

        // start a fast polling burst to ensure the NeHub is initialized quickly
        startFastPollingBurst();
    }

    @Override
    public void dispose() {
        if (logger.isDebugEnabled())
            logger.debug("stop background polling..");

        // clean up the lazy polling scheduler
        if (lazyPollingScheduler != null && !lazyPollingScheduler.isCancelled()) {
            lazyPollingScheduler.cancel(true);
            lazyPollingScheduler = null;
        }

        // clean up the fast polling scheduler
        if (fastPollingScheduler != null && !fastPollingScheduler.isCancelled()) {
            fastPollingScheduler.cancel(true);
            fastPollingScheduler = null;
        }
    }

    /*
     * device handlers call this to initiate a burst of fast polling requests (
     * improves response time to users when openHAB changes a channel value )
     */
    public void startFastPollingBurst() {
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
    }

    /*
     * device handlers call this method to issue commands to the NeoHub
     */
    public synchronized NeoHubReturnResult toNeoHubSendChannelValue(String commandStr) {
        if (socket == null || config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
            return NeoHubReturnResult.ERR_INITIALIZATION;
        }

        try {
            socket.sendMessage(commandStr);

            // start a fast polling burst (to confirm the status change)
            startFastPollingBurst();

            return NeoHubReturnResult.SUCCEEDED;
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("set value error \"{}\"", e.getMessage());
            return NeoHubReturnResult.ERR_COMMUNICATION;
        }
    }

    /**
     * sends a JSON "INFO" request to the NeoHub
     * 
     * @return a class that contains the full status of all devices
     * 
     */
    protected NeoHubInfoResponse fromNeoHubFetchPollingResponse() {
        if (socket == null || config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE);
            return null;
        }

        try {
            @Nullable
            String response = socket.sendMessage(CMD_CODE_INFO);

            lastInfoResponse = NeoHubInfoResponse.createInfoResponse(response);

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }

            return lastInfoResponse;
        } catch (Exception e) {
            logger.warn("set value error \"{}\"", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return null;
        }
    }

    /*
     * this is the callback used by the lazy polling scheduler.. fetches the info
     * for all devices from the NeoHub, and passes the results the respective device
     * handlers
     */
    private synchronized void lazyPollingSchedulerExecute() {
        fromNeoHubFetchPollingResponse();

        if (lastInfoResponse != null) {
            List<Thing> children = getThing().getThings();

            // dispatch the infoResponse to each of the hub's owned devices ..
            for (Thing child : children) {
                ThingHandler device = child.getHandler();
                if (device instanceof NeoBaseHandler) {
                    ((NeoBaseHandler) device).toBaseSendPollResponse(this, lastInfoResponse);
                }
            }
        }

        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }

    /*
     * this is the callback used by the fast polling scheduler.. checks if a fast
     * polling burst is scheduled, and if so calls lazyPollingSchedulerExecute
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }

    /**
     * determine if the particular device name is configured in the hub
     * 
     * @return the device is fully configured
     * 
     */
    public boolean isConfigured(String deviceName) {
        if (lastInfoResponse == null) {
            fromNeoHubFetchPollingResponse();
        }
        return lastInfoResponse != null && lastInfoResponse.getDeviceInfo(deviceName) != null;
    }

    /**
     * determine if the particular device name is communicating with the hub
     * 
     * @return the device is fully configured and talking to the hub
     * 
     */
    public boolean isOnline(String deviceName) {
        DeviceInfo deviceInfo;
        return isConfigured(deviceName) && (deviceInfo = lastInfoResponse.getDeviceInfo(deviceName)) != null
                && !deviceInfo.isOffline();
    }
}
