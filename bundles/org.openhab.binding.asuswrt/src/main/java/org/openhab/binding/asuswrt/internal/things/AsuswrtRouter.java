/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal.things;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.asuswrt.internal.AsuswrtDiscoveryService;
import org.openhab.binding.asuswrt.internal.api.AsuswrtConnector;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtErrorHandler;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtClientList;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtRouterInfo;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link asuswrtRouter} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtRouter extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtRouter.class);

    private @Nullable ScheduledFuture<?> startupJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> discoveryJob;
    private @NonNullByDefault({}) AsuswrtDiscoveryService discoveryService;
    private Map<String, Object> oldStates = new HashMap<>();
    private final HttpClient httpClient;
    private final String uid;
    private AsuswrtConfiguration config;
    private AsuswrtConnector connector;
    public AsuswrtErrorHandler errorHandler;
    public AsuswrtRouterInfo deviceInfo;

    /**
     * INIT CLASS
     * 
     * @param bridge Supported AssuswrtRouterThing
     */
    public AsuswrtRouter(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        Thing thing = getThing();
        this.uid = thing.getUID().toString();
        this.errorHandler = new AsuswrtErrorHandler();
        this.config = new AsuswrtConfiguration(thing);
        this.httpClient = httpClient;
        this.connector = new AsuswrtConnector(this);
        this.deviceInfo = new AsuswrtRouterInfo();
    }

    /***********************************
     *
     * INITIALIZATION
     *
     ************************************/
    @Override
    public void initialize() {
        this.config.loadSettings();

        // Initialize the handler.
        setState(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        this.startupJob = scheduler.schedule(this::delayedStartUp, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        stopScheduler(this.startupJob);
        stopScheduler(this.pollingJob);
        stopScheduler(this.discoveryJob);
        stopScheduler(this.reconnectJob);
    }

    /**
     * ACTIVATE DISCOVERY SERVICE
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(AsuswrtDiscoveryService.class);
    }

    /**
     * Set DiscoveryService
     * 
     * @param discoveryService
     */
    public void setDiscoveryService(AsuswrtDiscoveryService discoveryService) {
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
        connect();
    }

    /**
     * Start Polling Job Scheduler
     */
    public void startPollingJob() {
        Integer pollingInterval = AsuswrtUtils.getValueOrDefault(config.refreshInterval, POLLING_INTERVAL_S_DEFAULT);
        if (pollingInterval > 0) {
            if (pollingInterval < POLLING_INTERVAL_S_MIN) {
                pollingInterval = POLLING_INTERVAL_S_MIN;
            }
            logger.trace("({}) start polling scheduler with interval : {}", this.getUID(), pollingInterval);
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::pollingJobAction, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
        } else {
            stopScheduler(this.pollingJob);
        }
    }

    /**
     * Polling Job Action
     */
    protected void pollingJobAction() {
        if (ThingStatus.ONLINE.equals(getState())) {
            queryDeviceData();
        }
    }

    /**
     * Start Reconnect Scheduler
     */
    protected void startReconnectScheduler() {
        Integer pollingInterval = config.refreshInterval;
        if (pollingInterval < RECONNECT_INTERVAL_S) {
            pollingInterval = RECONNECT_INTERVAL_S;
        }
        logger.trace("({}) start reconnect scheduler with interval : {}", this.getUID(), pollingInterval);
        this.reconnectJob = scheduler.scheduleWithFixedDelay(this::reconnectJobAction, pollingInterval, pollingInterval,
                TimeUnit.SECONDS);
    }

    /**
     * reconnect job action
     */
    protected void reconnectJobAction() {
        connect();
    }

    /**
     * Start DeviceDiscovery Scheduler
     */
    protected void startDiscoveryScheduler() {
        Integer pollingInterval = config.refreshInterval;
        if (config.autoDiscoveryEnabled && pollingInterval > 0) {
            logger.trace("{} starting bridge discovery sheduler", this.uid);

            this.discoveryJob = scheduler.scheduleWithFixedDelay(discoveryService::startScan, 0, pollingInterval,
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
            logger.trace("{} stopping sheduler {}", this.uid, scheduler.toString());
            scheduler.cancel(true);
            scheduler = null;
        }
    }

    /***********************************
     *
     * FUNCTIONS
     *
     ************************************/

    /**
     * Connect to router and set states
     */
    protected void connect() {
        connector.login();
        if (connector.isValidLogin()) {
            stopScheduler(reconnectJob);
            queryDeviceData(false);
            devicePropertiesChanged(this.deviceInfo);
            setState(ThingStatus.ONLINE);
            startPollingJob();
        } else {
            setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorHandler.getErrorMessage());
        }
    }

    /**
     * QUERY DEVICE DATA
     * 
     * @param asyncRequest
     */
    public void queryDeviceData(Boolean asyncRequest) {
        connector.queryDeviceData(
                CMD_GET_SYSINFO + CMD_GET_USAGE + CMD_GET_LANINFO + CMD_GET_WANINFO + CMD_GET_CLIENTLIST, asyncRequest);
    }

    /**
     * QUERY DEVICE DATA
     * do asynchronous request
     */
    public void queryDeviceData() {
        queryDeviceData(true);
    }

    /**
     * Set routerInfo-Data and update channels on receiving new data with the associated command
     * 
     * @param jsonObject
     * @param command
     */
    public void dataReceived(JsonObject jsonObject, String command) {
        fireEvents(deviceInfo);
        if (command.contains(CMD_GET_SYSINFO)) {
            deviceInfo.setSysInfo(jsonObject);
            devicePropertiesChanged(deviceInfo);
        }
        if (command.contains(CMD_GET_CLIENTLIST)) {
            deviceInfo.setClientData(jsonObject);
            updateClientThings(deviceInfo.getClients());
        }
        if (command.contains(CMD_GET_LANINFO) || command.contains(CMD_GET_WANINFO)) {
            this.deviceInfo.setNetworkData(jsonObject);
        }
        if (command.contains(CMD_GET_USAGE) || command.contains(CMD_GET_MEMUSAGE)
                || command.contains(CMD_GET_CPUUSAGE)) {
            this.deviceInfo.setUsageStats(jsonObject);
        }
        updateChannels(deviceInfo);
    }

    /**
     * Upate RouterStatus
     * 
     * @param thingStatus
     * @param statusDetail
     * @param text
     */
    public void setState(ThingStatus thingStatus, ThingStatusDetail statusDetail, String text) {
        if (!thingStatus.equals(getThing().getStatus())) {
            updateStatus(thingStatus, statusDetail, text);
            updateClientStates(thingStatus);
            if (thingStatus.equals(ThingStatus.OFFLINE)) {
                /* set channels to undef */
                getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
                stopScheduler(this.pollingJob);
                startReconnectScheduler();
            }
        }
    }

    /**
     * Upate RouterStatus
     * 
     * @param thingStatus
     */
    public void setState(ThingStatus thingStatus) {
        setState(thingStatus, ThingStatusDetail.NONE, "");
    }

    /***********************************
     *
     * PUBLIC GETs
     *
     ************************************/

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public AsuswrtConfiguration getConfiguration() {
        return this.config;
    }

    public AsuswrtErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public AsuswrtRouterInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    public ThingStatus getState() {
        return getThing().getStatus();
    }

    /***********************************
     *
     * COMMAND HANDLER
     *
     ************************************/
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            queryDeviceData();
        }
    }

    /***********************************
     *
     * PROPERTIES
     *
     ************************************/

    /**
     * UPDATE PROPERTIES
     * 
     * If only one property must be changed, there is also a convenient method
     * updateProperty(String name, String value).
     * 
     * @param TapoDeviceInfo
     */
    public void devicePropertiesChanged(AsuswrtRouterInfo deviceInfo) {
        /* device properties */
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, deviceInfo.getMAC());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceInfo.getProductId());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getFirmwareVersion());
        updateProperties(properties);
    }

    /***********************************
     *
     * CHANNELS / CLIENTS
     *
     ************************************/

    /**
     * Update all Channels
     * 
     * @param deviceInfo
     */
    public void updateChannels(AsuswrtRouterInfo deviceInfo) {
        updateNetworkChannels(deviceInfo);
        updateClientChannels(deviceInfo);
        updateUsageChannels(deviceInfo);
    }

    /**
     * Update Channel Usage
     * 
     * @param deviceInfo
     */
    public void updateUsageChannels(AsuswrtRouterInfo deviceInfo) {
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_MEM_TOTAL),
                getQuantityType(deviceInfo.getMemUsage().getTotal(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_MEM_FREE),
                getQuantityType(deviceInfo.getMemUsage().getFree(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_MEM_USED),
                getQuantityType(deviceInfo.getMemUsage().getUsed(), Units.MEGABYTE));
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_MEM_FREE_PERCENT),
                getQuantityType(deviceInfo.getMemUsage().getFreePercent(), Units.PERCENT));
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_MEM_USED_PERCENT),
                getQuantityType(deviceInfo.getMemUsage().getUsedPercent(), Units.PERCENT));
        updateState(getChannelID(CHANNEL_GROUP_SYSINFO, CHANNEL_CPU_USED_PERCENT),
                getQuantityType(deviceInfo.getCpuAverage().getUsedPercent(), Units.PERCENT));
    }

    /**
     * Update Network Channels
     * 
     * @param deviceInfo
     */
    public void updateNetworkChannels(AsuswrtRouterInfo deviceInfo) {
        /* lanInfo */
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_IP_ADDRESS),
                getStringType(deviceInfo.getLanInfo().getIpAddress()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_SUBNET),
                getStringType(deviceInfo.getLanInfo().getSubnet()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_GATEWAY),
                getStringType(deviceInfo.getLanInfo().getGateway()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_MAC_ADDRESS),
                getStringType(deviceInfo.getLanInfo().getMAC()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_IP_METHOD),
                getStringType(deviceInfo.getLanInfo().getIpProto()));

        /* wanInfo */
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_IP_ADDRESS),
                getStringType(deviceInfo.getWanInfo().getIpAddress()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_SUBNET),
                getStringType(deviceInfo.getWanInfo().getSubnet()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_GATEWAY),
                getStringType(deviceInfo.getWanInfo().getGateway()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_IP_METHOD),
                getStringType(deviceInfo.getWanInfo().getIpProto()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_DNS_SERVER),
                getStringType(deviceInfo.getWanInfo().getDNSNServer()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_STATUS),
                getOnOffType(deviceInfo.getWanInfo().isConnected()));
    }

    /**
     * Update Client Channel
     * 
     * @param deviceInfo
     */
    public void updateClientChannels(AsuswrtRouterInfo deviceInfo) {
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_KNOWN),
                getStringType(deviceInfo.getClients().getClientList()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_ONLINE),
                getStringType(deviceInfo.getClients().getOnlineClients().getClientList()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_ONLINE_MAC),
                getStringType(deviceInfo.getClients().getOnlineClients().getMacAddresses()));
    }

    /**
     * fire events when new clientInformations changed
     * 
     * @param clientInfo
     */
    public void fireEvents(AsuswrtRouterInfo deviceInfo) {
        Boolean isConnected = deviceInfo.getWanInfo().isConnected();
        if (checkForStateChange(CHANNEL_WAN_STATUS, isConnected)) {
            if (isConnected) {
                triggerChannel(getChannelID(CHANNEL_GROUP_WANINFO, EVENT_CONNECTION), EVENT_STATE_CONNECTED);
            } else {
                triggerChannel(getChannelID(CHANNEL_GROUP_WANINFO, EVENT_CONNECTION), EVENT_STATE_DISCONNECTED);
            }
        }
    }

    /**
     * UPDATE ALL CLIENT THINGS
     * 
     * @param AsuswrtRouterInfo
     */
    public void updateClients(AsuswrtRouterInfo routerInfo) {
        updateClientThings(routerInfo.getClients());
    }

    /**
     * UPDATE ALL CLIENT THINGS
     * 
     * @param clientList AsuswrtClientList
     */
    public void updateClientThings(AsuswrtClientList clientList) {
        ThingTypeUID thingTypeUID;
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {
            thingTypeUID = thing.getThingTypeUID();
            ThingUID thingUID = thing.getUID();
            if (THING_TYPE_CLIENT.equals(thingTypeUID)) {
                ChannelUID cuid = new ChannelUID(thingUID, CHANNEL_GROUP_CLIENT);
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handler.handleCommand(cuid, RefreshType.REFRESH);
                }
            }
        }
    }

    /**
     * Set State of all clients
     * 
     * @param thingStatus new ThingStatus
     */
    public void updateClientStates(ThingStatus thingStatus) {
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                if (ThingStatus.OFFLINE.equals(thingStatus)) {
                    handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.BRIDGE_OFFLINE, ""));
                } else {
                    handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.NONE, ""));
                }
            }
        }
    }

    /***********************************
     *
     * FUNCTIONS
     *
     ************************************/

    /**
     * Get ChannelID including group
     * 
     * @param group String channel-group
     * @param channel String channel-name
     * @return String channelID
     */
    protected String getChannelID(String group, String channel) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CHANNEL_GROUP_THING_SET.contains(thingTypeUID) && group.length() > 0) {
            return group + "#" + channel;
        }
        return channel;
    }

    /**
     * Get Channel from ChannelID
     * 
     * @param channelID String channelID
     * @return String channel-name
     */
    protected String getChannelFromID(ChannelUID channelID) {
        String channel = channelID.getIdWithoutGroup();
        channel = channel.replace(CHANNEL_GROUP_SYSINFO + "#", "");
        channel = channel.replace(CHANNEL_GROUP_LANINFO + "#", "");
        return channel;
    }

    /**
     * Check if state changed since last channel update
     * 
     * @param stateName name of state (channel)
     * @param comparator comparation value
     * @return true if changed, false if not or no old value existds
     */
    private Boolean checkForStateChange(String stateName, Object comparator) {
        if (oldStates.get(stateName) == null) {
            oldStates.put(stateName, comparator);
        } else if (!comparator.equals(oldStates.get(stateName))) {
            oldStates.put(stateName, comparator);
            return true;
        }
        return false;
    }
}
