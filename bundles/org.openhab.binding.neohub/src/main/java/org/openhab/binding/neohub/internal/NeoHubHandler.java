/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData.AbstractRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import tec.uom.se.unit.Units;

/**
 * The {@link NeoHubHandler} is the openHAB Handler for NeoHub devices
 *
 * @author Andrew Fiddian-Green - Initial contribution (v2.x binding code)
 * @author Sebastian Prehn - Initial contribution (v1.x hub communication)
 *
 */
@NonNullByDefault
public class NeoHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NeoHubHandler.class);

    private final Map<String, Boolean> connectionStates = new HashMap<>();

    private @Nullable NeoHubConfiguration config;
    private @Nullable NeoHubSocket socket;
    private @Nullable ScheduledFuture<?> lazyPollingScheduler;
    private @Nullable ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    private @Nullable NeoHubReadDcbResponse systemData = null;

    private boolean isLegacyApiSelected = true;
    private boolean isApiOnline = false;

    public NeoHubHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // future: currently there is nothing to do for a NeoHub
    }

    @Override
    public void initialize() {
        NeoHubConfiguration config = getConfigAs(NeoHubConfiguration.class);

        if (logger.isDebugEnabled()) {
            logger.debug("hostname={}", config.hostName);
        }

        if (!MATCHER_IP_ADDRESS.matcher(config.hostName).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "parameter hostName must be set!");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("port={}", config.portNumber);
        }

        if (config.portNumber <= 0 || config.portNumber > 0xFFFF) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "portNumber is invalid!");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("polling interval={}", config.pollingInterval);
        }

        if (config.pollingInterval < FAST_POLL_INTERVAL || config.pollingInterval > LAZY_POLL_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                    .format("pollingInterval must be in range [%d..%d]!", FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("socketTimeout={}", config.socketTimeout);
        }

        if (config.socketTimeout < 5 || config.socketTimeout > 20) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("socketTimeout must be in range [%d..%d]!", 5, 20));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("preferLegacyApi={}", config.preferLegacyApi);
        }

        socket = new NeoHubSocket(config.hostName, config.portNumber, config.socketTimeout);
        this.config = config;

        if (logger.isDebugEnabled()) {
            logger.debug("start background polling..");
        }

        // create a "lazy" polling scheduler
        ScheduledFuture<?> lazy = this.lazyPollingScheduler;
        if (lazy == null || lazy.isCancelled()) {
            this.lazyPollingScheduler = scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute,
                    config.pollingInterval, config.pollingInterval, TimeUnit.SECONDS);
        }

        // create a "fast" polling scheduler
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
        ScheduledFuture<?> fast = this.fastPollingScheduler;
        if (fast == null || fast.isCancelled()) {
            this.fastPollingScheduler = scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute,
                    FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
        }

        updateStatus(ThingStatus.UNKNOWN);

        // start a fast polling burst to ensure the NeHub is initialized quickly
        startFastPollingBurst();
    }

    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("stop background polling..");
        }

        // clean up the lazy polling scheduler
        ScheduledFuture<?> lazy = this.lazyPollingScheduler;
        if (lazy != null && !lazy.isCancelled()) {
            lazy.cancel(true);
            this.lazyPollingScheduler = null;
        }

        // clean up the fast polling scheduler
        ScheduledFuture<?> fast = this.fastPollingScheduler;
        if (fast != null && !fast.isCancelled()) {
            fast.cancel(true);
            this.fastPollingScheduler = null;
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
        NeoHubSocket socket = this.socket;

        if (socket == null || config == null) {
            return NeoHubReturnResult.ERR_INITIALIZATION;
        }

        try {
            socket.sendMessage(commandStr);

            // start a fast polling burst (to confirm the status change)
            startFastPollingBurst();

            return NeoHubReturnResult.SUCCEEDED;
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn(MSG_FMT_SET_VALUE_ERR, commandStr, e.getMessage());
            return NeoHubReturnResult.ERR_COMMUNICATION;
        }
    }

    /**
     * sends a JSON request to the NeoHub to read the device data
     * 
     * @return a class that contains the full status of all devices
     */
    protected @Nullable NeoHubAbstractDeviceData fromNeoHubGetDeviceData() {
        NeoHubSocket socket = this.socket;

        if (socket == null || config == null) {
            logger.warn(MSG_HUB_CONFIG);
            return null;
        }

        try {
            String responseJson;
            NeoHubAbstractDeviceData deviceData;

            if (isLegacyApiSelected) {
                responseJson = socket.sendMessage(CMD_CODE_INFO);
                deviceData = NeoHubInfoResponse.createDeviceData(responseJson);
            } else {
                responseJson = socket.sendMessage(CMD_CODE_GET_LIVE_DATA);
                deviceData = NeoHubLiveDeviceData.createDeviceData(responseJson);
            }

            if (deviceData == null) {
                logger.warn(MSG_FMT_DEVICE_POLL_ERR, "failed to create device data response");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return null;
            }

            @Nullable
            List<? extends AbstractRecord> devices = deviceData.getDevices();
            if (devices == null || devices.size() == 0) {
                logger.warn(MSG_FMT_DEVICE_POLL_ERR, "no devices found");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return null;
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            }

            // check if we also need to discard and update systemData
            NeoHubReadDcbResponse systemData = this.systemData;
            if (systemData != null) {
                if (deviceData instanceof NeoHubLiveDeviceData) {
                    /*
                     * note: time-stamps are measured in seconds from 1970-01-01T00:00:00Z
                     * 
                     * new API: discard systemData if its time-stamp is older than the system
                     * time-stamp on the hub
                     */
                    if (systemData.timeStamp < ((NeoHubLiveDeviceData) deviceData).getTimestampSystem()) {
                        this.systemData = null;
                    }
                } else {
                    /*
                     * note: time-stamps are measured in seconds from 1970-01-01T00:00:00Z
                     * 
                     * legacy API: discard systemData if its time-stamp is older than one hour
                     */
                    if (systemData.timeStamp < Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond()) {
                        this.systemData = null;
                    }
                }
            }

            return deviceData;
        } catch (Exception e) {
            logger.warn(MSG_FMT_DEVICE_POLL_ERR, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return null;
        }
    }

    /**
     * sends a JSON request to the NeoHub to read the system data
     * 
     * @return a class that contains the status of the system
     */
    protected @Nullable NeoHubReadDcbResponse fromNeoHubReadSystemData() {
        NeoHubSocket socket = this.socket;

        if (socket == null) {
            return null;
        }

        try {
            String responseJson;
            NeoHubReadDcbResponse systemData;

            if (isLegacyApiSelected) {
                responseJson = socket.sendMessage(CMD_CODE_READ_DCB);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
            } else {
                responseJson = socket.sendMessage(CMD_CODE_GET_SYSTEM);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
            }

            if (systemData == null) {
                logger.warn(MSG_FMT_SYSTEM_POLL_ERR, "failed to create system data response");
                return null;
            }

            return systemData;
        } catch (Exception e) {
            logger.warn(MSG_FMT_SYSTEM_POLL_ERR, e.getMessage());
            return null;
        }
    }

    /*
     * this is the callback used by the lazy polling scheduler.. fetches the info
     * for all devices from the NeoHub, and passes the results the respective device
     * handlers
     */
    private synchronized void lazyPollingSchedulerExecute() {
        // check which API is supported
        if (!isApiOnline) {
            selectApi();
        }

        NeoHubAbstractDeviceData deviceData = fromNeoHubGetDeviceData();
        if (deviceData != null) {
            // dispatch deviceData to each of the hub's owned devices ..
            List<Thing> children = getThing().getThings();
            for (Thing child : children) {
                ThingHandler device = child.getHandler();
                if (device instanceof NeoBaseHandler) {
                    ((NeoBaseHandler) device).toBaseSendPollResponse(deviceData);
                }
            }

            // evaluate and update the state of our RF mesh QoS channel
            List<? extends AbstractRecord> devices = deviceData.getDevices();
            State state;

            if (devices == null || devices.isEmpty()) {
                state = UnDefType.UNDEF;
            } else {
                int totalDeviceCount = devices.size();
                int onlineDeviceCount = 0;

                for (AbstractRecord device : devices) {
                    String deviceName = device.getDeviceName();
                    Boolean online = !device.offline();

                    @Nullable
                    Boolean onlineBefore = connectionStates.put(deviceName, online);
                    if (!online.equals(onlineBefore)) {
                        logger.info("device \"{}\" has {} the RF mesh network", deviceName,
                                online.booleanValue() ? "joined" : "left");
                    }

                    if (online.booleanValue()) {
                        onlineDeviceCount++;
                    }
                }
                state = new QuantityType<>((100.0 * onlineDeviceCount) / totalDeviceCount, Units.PERCENT);
            }
            updateState(CHAN_MESH_NETWORK_QOS, state);
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

    /*
     * select whether to use the old "deprecated" API or the new API
     */
    private void selectApi() {
        boolean supportsLegacyApi = false;
        boolean supportsFutureApi = false;

        NeoHubSocket socket = this.socket;
        if (socket != null) {
            String responseJson;
            NeoHubReadDcbResponse systemData;

            try {
                responseJson = socket.sendMessage(CMD_CODE_READ_DCB);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
                supportsLegacyApi = systemData != null;
                if (!supportsLegacyApi) {
                    throw new NeoHubException("legacy API not supported");
                }
            } catch (JsonSyntaxException | NeoHubException | IOException e) {
                // we learned that this API is not currently supported; no big deal
                logger.debug("Legacy API is not supported!");
            }
            try {
                responseJson = socket.sendMessage(CMD_CODE_GET_SYSTEM);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
                supportsFutureApi = systemData != null;
                if (!supportsFutureApi) {
                    throw new NeoHubException("new API not supported");
                }
            } catch (JsonSyntaxException | NeoHubException | IOException e) {
                // we learned that this API is not currently supported; no big deal
                logger.debug("New API is not supported!");
            }
        }

        if (!supportsLegacyApi && !supportsFutureApi) {
            logger.warn("Currently neither legacy nor new API are supported!");
            isApiOnline = false;
            return;
        }

        NeoHubConfiguration config = this.config;
        boolean isLegacyApiSelected = (supportsLegacyApi && config != null && config.preferLegacyApi);
        if (isLegacyApiSelected != this.isLegacyApiSelected) {
            logger.info("Changing API version: {}",
                    isLegacyApiSelected ? "\"new\" => \"legacy\"" : "\"legacy\" => \"new\"");
        }
        this.isLegacyApiSelected = isLegacyApiSelected;
        this.isApiOnline = true;
    }

    /*
     * get the Engineers data
     */
    public @Nullable NeoHubGetEngineersData fromNeoHubGetEngineersData() {
        NeoHubSocket socket = this.socket;
        if (socket != null) {
            String responseJson;
            try {
                responseJson = socket.sendMessage(CMD_CODE_GET_ENGINEERS);
                return NeoHubGetEngineersData.createEngineersData(responseJson);
            } catch (JsonSyntaxException | IOException | NeoHubException e) {
                logger.warn(MSG_FMT_ENGINEERS_POLL_ERR, e.getMessage());
            }
        }
        return null;
    }

    public boolean isLegacyApiSelected() {
        return isLegacyApiSelected;
    }

    public Unit<?> getTemperatureUnit() {
        NeoHubReadDcbResponse systemData = this.systemData;
        if (systemData == null) {
            this.systemData = systemData = fromNeoHubReadSystemData();
        }
        if (systemData != null) {
            return systemData.getTemperatureUnit();
        }
        return SIUnits.CELSIUS;
    }
}
