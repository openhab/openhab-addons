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
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData.AbstractRecord;
import org.openhab.binding.neohub.internal.NeoHubBindingConstants.NeoHubReturnResult;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link NeoHubHandler} is the openHAB Handler for NeoHub devices
 *
 * @author Andrew Fiddian-Green - Initial contribution (v2.x binding code)
 * @author Sebastian Prehn - Initial contribution (v1.x hub communication)
 *
 */
@NonNullByDefault
public class NeoHubHandler extends BaseBridgeHandler {

    private static final String SEE_README = "See documentation chapter \"Connection Refused Errors\"";
    private static final int MAX_FAILED_SEND_ATTEMPTS = 2;

    private final Logger logger = LoggerFactory.getLogger(NeoHubHandler.class);

    private final Map<String, Boolean> connectionStates = new HashMap<>();

    private WebSocketFactory webSocketFactory;

    private @Nullable NeoHubConfiguration config;
    private @Nullable NeoHubSocketBase socket;
    private @Nullable ScheduledFuture<?> lazyPollingScheduler;
    private @Nullable ScheduledFuture<?> fastPollingScheduler;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    private @Nullable NeoHubReadDcbResponse systemData = null;

    private enum ApiVersion {
        LEGACY("legacy"),
        NEW("new");

        public final String label;

        private ApiVersion(String label) {
            this.label = label;
        }
    }

    private ApiVersion apiVersion = ApiVersion.LEGACY;
    private boolean isApiOnline = false;
    private int failedSendAttempts = 0;

    public NeoHubHandler(Bridge bridge, WebSocketFactory webSocketFactory) {
        super(bridge);
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // future: currently there is nothing to do for a NeoHub
    }

    @Override
    public void initialize() {
        NeoHubConfiguration config = getConfigAs(NeoHubConfiguration.class);

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' hostname={}", getThing().getUID(), config.hostName);
        }

        if (!MATCHER_IP_ADDRESS.matcher(config.hostName).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "parameter hostName must be set!");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' port={}", getThing().getUID(), config.portNumber);
        }

        if (config.portNumber < 0 || config.portNumber > 0xFFFF) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "portNumber is invalid!");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' polling interval={}", getThing().getUID(), config.pollingInterval);
        }

        if (config.pollingInterval < FAST_POLL_INTERVAL || config.pollingInterval > LAZY_POLL_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String
                    .format("pollingInterval must be in range [%d..%d]!", FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' socketTimeout={}", getThing().getUID(), config.socketTimeout);
        }

        if (config.socketTimeout < 5 || config.socketTimeout > 20) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("socketTimeout must be in range [%d..%d]!", 5, 20));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' preferLegacyApi={}", getThing().getUID(), config.preferLegacyApi);
        }

        // create a web or TCP socket based on the port number in the configuration
        NeoHubSocketBase socket;
        try {
            if (config.useWebSocket) {
                socket = new NeoHubWebSocket(config, webSocketFactory, thing.getUID());
            } else {
                socket = new NeoHubSocket(config, thing.getUID().getAsString());
            }
        } catch (IOException e) {
            logger.debug("\"hub '{}' error creating web/tcp socket: '{}'", getThing().getUID(), e.getMessage());
            return;
        }

        this.socket = socket;
        this.config = config;

        /*
         * Try to 'ping' the hub, and if there is a 'connection refused', it is probably due to the mobile App |
         * Settings | Legacy API Enable switch not being On, so go offline and log a warning message.
         */
        try {
            socket.sendMessage(CMD_CODE_FIRMWARE);
        } catch (IOException e) {
            String error = e.getMessage();
            if (error != null && error.toLowerCase().startsWith("connection refused")) {
                logger.warn("CONNECTION REFUSED!! (hub '{}') => {}", getThing().getUID(), SEE_README);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, SEE_README);
                return;
            }
        } catch (NeoHubException e) {
            // NeoHubException won't actually occur here
        }

        if (logger.isDebugEnabled()) {
            logger.debug("hub '{}' start background polling..", getThing().getUID());
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
            logger.debug("hub '{}' stop background polling..", getThing().getUID());
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

        NeoHubSocketBase socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            this.socket = null;
        }
    }

    /**
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
        NeoHubSocketBase socket = this.socket;

        if (socket == null || config == null) {
            return NeoHubReturnResult.ERR_INITIALIZATION;
        }

        try {
            socket.sendMessage(commandStr);

            // start a fast polling burst (to confirm the status change)
            startFastPollingBurst();

            return NeoHubReturnResult.SUCCEEDED;
        } catch (IOException | NeoHubException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn(MSG_FMT_SET_VALUE_ERR, getThing().getUID(), commandStr, e.getMessage());
            return NeoHubReturnResult.ERR_COMMUNICATION;
        }
    }

    /**
     * sends a JSON request to the NeoHub to read the device data
     *
     * @return a class that contains the full status of all devices
     */
    protected @Nullable NeoHubAbstractDeviceData fromNeoHubGetDeviceData() {
        NeoHubSocketBase socket = this.socket;

        if (socket == null || config == null) {
            logger.warn(MSG_HUB_CONFIG, getThing().getUID());
            return null;
        }

        try {
            String responseJson;
            NeoHubAbstractDeviceData deviceData;

            if (apiVersion == ApiVersion.LEGACY) {
                responseJson = socket.sendMessage(CMD_CODE_INFO);
                deviceData = NeoHubInfoResponse.createDeviceData(responseJson);
            } else {
                responseJson = socket.sendMessage(CMD_CODE_GET_LIVE_DATA);
                deviceData = NeoHubLiveDeviceData.createDeviceData(responseJson);
            }

            if (deviceData == null) {
                logger.warn(MSG_FMT_DEVICE_POLL_ERR, getThing().getUID(), "failed to create device data response");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return null;
            }

            @Nullable
            List<? extends AbstractRecord> devices = deviceData.getDevices();
            if (devices == null || devices.isEmpty()) {
                logger.warn(MSG_FMT_DEVICE_POLL_ERR, getThing().getUID(), "no devices found");
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
        } catch (IOException | NeoHubException e) {
            logger.warn(MSG_FMT_DEVICE_POLL_ERR, getThing().getUID(), e.getMessage());
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
        NeoHubSocketBase socket = this.socket;

        if (socket == null) {
            return null;
        }

        try {
            String responseJson;
            NeoHubReadDcbResponse systemData;

            if (apiVersion == ApiVersion.LEGACY) {
                responseJson = socket.sendMessage(CMD_CODE_READ_DCB);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
            } else {
                responseJson = socket.sendMessage(CMD_CODE_GET_SYSTEM);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
            }

            if (systemData == null) {
                logger.warn(MSG_FMT_SYSTEM_POLL_ERR, getThing().getUID(), "failed to create system data response");
                return null;
            }

            String physicalFirmware = systemData.getFirmwareVersion();
            if (physicalFirmware != null) {
                String thingFirmware = getThing().getProperties().get(PROPERTY_FIRMWARE_VERSION);
                if (!physicalFirmware.equals(thingFirmware)) {
                    getThing().setProperty(PROPERTY_FIRMWARE_VERSION, physicalFirmware);
                }
            }

            return systemData;
        } catch (IOException | NeoHubException e) {
            logger.warn(MSG_FMT_SYSTEM_POLL_ERR, getThing().getUID(), e.getMessage());
            return null;
        }
    }

    /**
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
        if (deviceData == null) {
            if (fastPollingCallsToGo.get() == 0) {
                failedSendAttempts++;
                if (failedSendAttempts < MAX_FAILED_SEND_ATTEMPTS) {
                    logger.debug("lazyPollingSchedulerExecute() deviceData:null, running again");
                    scheduler.submit(() -> lazyPollingSchedulerExecute());
                }
            }
            return;
        } else {
            failedSendAttempts = 0;

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
            String property;

            if (devices == null || devices.isEmpty()) {
                state = UnDefType.UNDEF;
                property = "[?/?]";
            } else {
                int totalDeviceCount = devices.size();
                int onlineDeviceCount = 0;

                for (AbstractRecord device : devices) {
                    String deviceName = device.getDeviceName();
                    Boolean online = !device.offline();

                    @Nullable
                    Boolean onlineBefore = connectionStates.put(deviceName, online);
                    /*
                     * note: we use logger.info() here to log changes; reason is that the average user does really need
                     * to know if a device (very occasionally) drops out of the normally reliable RF mesh; however we
                     * only log it if 1) the state has changed, and 2) either 2a) the device has already been discovered
                     * by the bridge handler, or 2b) logger debug mode is set
                     */
                    if (!online.equals(onlineBefore) && ((onlineBefore != null) || logger.isDebugEnabled())) {
                        logger.info("hub '{}' device \"{}\" has {} the RF mesh network", getThing().getUID(),
                                deviceName, online.booleanValue() ? "joined" : "left");
                    }

                    if (online.booleanValue()) {
                        onlineDeviceCount++;
                    }
                }
                property = String.format("[%d/%d]", onlineDeviceCount, totalDeviceCount);
                state = new QuantityType<>((100.0 * onlineDeviceCount) / totalDeviceCount, Units.PERCENT);
            }
            getThing().setProperty(PROPERTY_API_DEVICEINFO, property);
            updateState(CHAN_MESH_NETWORK_QOS, state);
        }
        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }

    /**
     * this is the callback used by the fast polling scheduler.. checks if a fast
     * polling burst is scheduled, and if so calls lazyPollingSchedulerExecute
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }

    /**
     * select whether to use the old "deprecated" API or the new API
     */
    private void selectApi() {
        boolean supportsLegacyApi = false;
        boolean supportsFutureApi = false;

        NeoHubSocketBase socket = this.socket;
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
                logger.debug("hub '{}' legacy API is not supported!", getThing().getUID());
            }
            try {
                responseJson = socket.sendMessage(CMD_CODE_GET_SYSTEM);
                systemData = NeoHubReadDcbResponse.createSystemData(responseJson);
                supportsFutureApi = systemData != null;
                if (!supportsFutureApi) {
                    throw new NeoHubException(String.format("hub '%s' new API not supported", getThing().getUID()));
                }
            } catch (JsonSyntaxException | NeoHubException | IOException e) {
                // we learned that this API is not currently supported; no big deal
                logger.debug("hub '{}' new API is not supported!", getThing().getUID());
            }
        }

        if (!supportsLegacyApi && !supportsFutureApi) {
            logger.warn("hub '{}' currently neither legacy nor new API are supported!", getThing().getUID());
            isApiOnline = false;
            return;
        }

        NeoHubConfiguration config = this.config;
        ApiVersion apiVersion = (supportsLegacyApi && config != null && config.preferLegacyApi) ? ApiVersion.LEGACY
                : ApiVersion.NEW;
        if (apiVersion != this.apiVersion) {
            logger.debug("hub '{}' changing API version: '{}' => '{}'", getThing().getUID(), this.apiVersion.label,
                    apiVersion.label);
            this.apiVersion = apiVersion;
        }

        if (!apiVersion.label.equals(getThing().getProperties().get(PROPERTY_API_VERSION))) {
            getThing().setProperty(PROPERTY_API_VERSION, apiVersion.label);
        }

        this.isApiOnline = true;
    }

    /**
     * get the Engineers data
     */
    public @Nullable NeoHubGetEngineersData fromNeoHubGetEngineersData() {
        NeoHubSocketBase socket = this.socket;
        if (socket != null) {
            String responseJson;
            try {
                responseJson = socket.sendMessage(CMD_CODE_GET_ENGINEERS);
                return NeoHubGetEngineersData.createEngineersData(responseJson);
            } catch (JsonSyntaxException | IOException | NeoHubException e) {
                logger.warn(MSG_FMT_ENGINEERS_POLL_ERR, getThing().getUID(), e.getMessage());
            }
        }
        return null;
    }

    public boolean isLegacyApiSelected() {
        return apiVersion == ApiVersion.LEGACY;
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
