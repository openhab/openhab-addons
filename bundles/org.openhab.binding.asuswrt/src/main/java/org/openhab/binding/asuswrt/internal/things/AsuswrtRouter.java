/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.asuswrt.internal.structures.AsuswrtInterfaceList;
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
 * The {@link AsuswrtRouter} is responsible for handling commands, which are sent to one of the channels.
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
    private @Nullable AsuswrtConnector connector;
    private AsuswrtConfiguration config;
    private AsuswrtRouterInfo deviceInfo;
    private AsuswrtInterfaceList interfaceList = new AsuswrtInterfaceList();
    private AsuswrtClientList clientList = new AsuswrtClientList();
    private final HttpClient httpClient;
    private final String uid;

    private int backoffDuration = RECONNECT_BACKOFF_START_S;

    public AsuswrtErrorHandler errorHandler;

    public AsuswrtRouter(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        Thing thing = getThing();
        uid = thing.getUID().toString();
        errorHandler = new AsuswrtErrorHandler();
        this.httpClient = httpClient;
        deviceInfo = new AsuswrtRouterInfo();
        config = new AsuswrtConfiguration();
    }

    @Override
    public void initialize() {
        config = getConfigAs(AsuswrtConfiguration.class);
        connector = new AsuswrtConnector(this);

        // Initialize the handler.
        setState(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        startupJob = scheduler.schedule(this::delayedStartUp, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        stopScheduler(startupJob);
        stopScheduler(pollingJob);
        stopScheduler(discoveryJob);
        stopScheduler(reconnectJob);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(AsuswrtDiscoveryService.class);
    }

    public void setDiscoveryService(AsuswrtDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /*
     * Scheduler
     */

    /**
     * Delayed one-time startup job.
     */
    private void delayedStartUp() {
        connect();
    }

    public void startPollingJob() {
        int pollingInterval = AsuswrtUtils.getValueOrDefault(config.pollingInterval, POLLING_INTERVAL_DEFAULT_S);
        if (pollingInterval > 0) {
            pollingInterval = Math.max(pollingInterval, POLLING_INTERVAL_MIN_S);
            logger.trace("({}) start polling scheduler with interval {} {}", getUID(), pollingInterval,
                    TimeUnit.SECONDS);
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingJobAction, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
        } else {
            stopScheduler(pollingJob);
        }
    }

    protected void pollingJobAction() {
        if (ThingStatus.ONLINE.equals(getState())) {
            queryDeviceData();
        }
    }

    protected void startReconnectScheduler() {
        backoffDuration = RECONNECT_BACKOFF_START_S;
        logger.debug("({}) will attempt reconnect in {} {}", getUID(), backoffDuration, TimeUnit.SECONDS);
        reconnectJob = scheduler.schedule(this::reconnectJobAction, backoffDuration, TimeUnit.SECONDS);
    }

    protected void reconnectJobAction() {
        if (!connect()) {
            backoffDuration = Math.min(backoffDuration * 2, RECONNECT_BACKOFF_MAX_S);
            logger.debug("({}) will attempt reconnect in {} {}", getUID(), backoffDuration, TimeUnit.SECONDS);
            reconnectJob = scheduler.schedule(this::reconnectJobAction, backoffDuration, TimeUnit.SECONDS);
        }
    }

    protected void startDiscoveryScheduler() {
        int pollingInterval = config.discoveryInterval;
        if (config.autoDiscoveryEnabled && pollingInterval > 0) {
            logger.trace("{} starting bridge discovery sheduler with interval {} {}", getUID(), pollingInterval,
                    TimeUnit.SECONDS);
            discoveryJob = scheduler.scheduleWithFixedDelay(discoveryService::startScan, 0, pollingInterval,
                    TimeUnit.SECONDS);
        } else {
            stopScheduler(discoveryJob);
        }
    }

    /**
     * Stops a scheduler.
     *
     * @param scheduler {@code ScheduledFeature<?>} which should be stopped
     */
    protected void stopScheduler(@Nullable ScheduledFuture<?> scheduler) {
        if (scheduler != null) {
            logger.trace("{} stopping scheduler {}", uid, scheduler);
            scheduler.cancel(true);
        }
    }

    /*
     * Functions
     */

    /**
     * Connects to the router and sets the states.
     */
    @SuppressWarnings("null")
    protected boolean connect() {
        connector.login();
        if (connector.cookieStore.cookieIsSet()) {
            stopScheduler(reconnectJob);
            queryDeviceData(false);
            devicePropertiesChanged(deviceInfo);
            setState(ThingStatus.ONLINE);
            startPollingJob();
            return true;
        } else {
            setState(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorHandler.getErrorMessage());
            return false;
        }
    }

    @SuppressWarnings("null")
    public void queryDeviceData(Boolean asyncRequest) {
        connector.queryDeviceData(CMD_GET_SYSINFO + CMD_GET_USAGE + CMD_GET_LANINFO + CMD_GET_WANINFO
                + CMD_GET_CLIENTLIST + CMD_GET_TRAFFIC, asyncRequest);
    }

    /**
     * Queries device data asynchronously.
     */
    public void queryDeviceData() {
        queryDeviceData(true);
    }

    /**
     * Sets routerInfo data and updates channels on receiving new data with the associated command.
     *
     * @param jsonObject contains the received data
     * @param command the command that was sent
     */
    public void dataReceived(JsonObject jsonObject, String command) {
        if (command.contains(CMD_GET_SYSINFO)) {
            deviceInfo.setSysInfo(jsonObject);
            devicePropertiesChanged(deviceInfo);
        }
        if (command.contains(CMD_GET_CLIENTLIST)) {
            clientList.setData(jsonObject);
            updateClients();
        }
        if (command.contains(CMD_GET_LANINFO)) {
            interfaceList.setData(INTERFACE_LAN, jsonObject);
            updateChild(THING_TYPE_INTERFACE, NETWORK_REPRESENTATION_PROPERTY, INTERFACE_LAN);
        }
        if (command.contains(CMD_GET_WANINFO)) {
            interfaceList.setData(INTERFACE_WAN, jsonObject);
            updateChild(THING_TYPE_INTERFACE, NETWORK_REPRESENTATION_PROPERTY, INTERFACE_WAN);
        }
        if (command.contains(CMD_GET_USAGE) || command.contains(CMD_GET_MEMUSAGE)
                || command.contains(CMD_GET_CPUUSAGE)) {
            deviceInfo.setUsageStats(jsonObject);
        }
        updateChannels(deviceInfo, clientList);
    }

    /**
     * Updates the router status.
     */
    public void setState(ThingStatus thingStatus, ThingStatusDetail statusDetail, String text) {
        if (!thingStatus.equals(getThing().getStatus())) {
            updateStatus(thingStatus, statusDetail, text);
            updateChildStates(thingStatus);
            if (ThingStatus.OFFLINE.equals(thingStatus)) {
                stopScheduler(pollingJob);
                // Set channels to undef
                getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
                startReconnectScheduler();
            }
        }
    }

    /**
     * Upate RouterStatus
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
        return httpClient;
    }

    public AsuswrtConfiguration getConfiguration() {
        return config;
    }

    public AsuswrtErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public AsuswrtRouterInfo getDeviceInfo() {
        return deviceInfo;
    }

    public AsuswrtClientList getClients() {
        return clientList;
    }

    public AsuswrtInterfaceList getInterfaces() {
        return interfaceList;
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
     */
    public void devicePropertiesChanged(AsuswrtRouterInfo deviceInfo) {
        /* device properties */
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_MAC_ADDRESS, interfaceList.getByName(INTERFACE_WAN).getMAC());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceInfo.getProductId());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getFirmwareVersion());
        updateProperties(properties);
    }

    /***********************************
     *
     * CHANNELS
     *
     ************************************/

    /**
     * Update all Channels
     */
    public void updateChannels(AsuswrtRouterInfo deviceInfo, AsuswrtClientList clientList) {
        updateClientChannels(clientList);
        updateUsageChannels(deviceInfo);
    }

    /**
     * Update Channel Usage
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
     * Update Client Channel
     */
    public void updateClientChannels(AsuswrtClientList clientList) {
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_KNOWN),
                getStringType(clientList.getClientList()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_ONLINE),
                getStringType(clientList.getOnlineClients().getClientList()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_COUNT),
                getDecimalType(clientList.getOnlineClients().getCount()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENTS_ONLINE_MAC),
                getStringType(clientList.getOnlineClients().getMacAddresses()));
    }

    /**
     * Fire Event
     *
     * @param channel chanelUID event belongs to
     * @param event event-name is fired
     */
    protected void fireEvent(String channel, String event) {
        triggerChannel(channel, event);
    }

    /***********************************
     *
     * CHILD THINGS
     *
     ************************************/
    /**
     * Update all Child-Things with type Client
     */
    public void updateClients() {
        updateChildThings(THING_TYPE_CLIENT);
    }

    /**
     * Update all Child-Things with type Interface
     */
    public void updateInterfaces() {
        updateChildThings(THING_TYPE_INTERFACE);
    }

    /**
     * Update all Child-Things belonging to ThingTypeUID
     */
    public void updateChildThings(ThingTypeUID thingTypeToUpdate) {
        ThingTypeUID thingTypeUID;
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {
            thingTypeUID = thing.getThingTypeUID();
            if (thingTypeToUpdate.equals(thingTypeUID)) {
                updateChild(thing);
            }
        }
    }

    /**
     * Update Child single child with special representationProperty
     *
     * @param thingTypeToUpdate ThingTypeUID of Thing to update
     * @param representationProperty Name of representationProperty
     * @param propertyValue Value of representationProperty
     */
    public void updateChild(ThingTypeUID thingTypeToUpdate, String representationProperty, String propertyValue) {
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();
            if (thingTypeToUpdate.equals(thingTypeUID)) {
                String thingProperty = thing.getProperties().get(representationProperty);
                if (propertyValue.equals(thingProperty)) {
                    updateChild(thing);
                }
            }
        }
    }

    /**
     * Update Child-Thing (send refreshCommand)
     *
     * @param thing - Thing to update
     */
    public void updateChild(Thing thing) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            ChannelUID cUid = new ChannelUID(thing.getUID(), CHANNELS_ALL);
            handler.handleCommand(cUid, RefreshType.REFRESH);
        }
    }

    /**
     * Set State of all clients
     *
     * @param thingStatus new ThingStatus
     */
    public void updateChildStates(ThingStatus thingStatus) {
        List<Thing> things = getThing().getThings();
        for (Thing thing : things) {
            updateChildState(thing, thingStatus);
        }
    }

    /**
     * Set State of a Thing
     *
     * @param thing Thing to update
     * @param thingStatus new ThingStatus
     */
    public void updateChildState(Thing thing, ThingStatus thingStatus) {
        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            if (ThingStatus.OFFLINE.equals(thingStatus)) {
                handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.BRIDGE_OFFLINE, ""));
            } else {
                handler.bridgeStatusChanged(new ThingStatusInfo(thingStatus, ThingStatusDetail.NONE, ""));
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
        channel = channel.replace(CHANNEL_GROUP_CLIENTS + "#", "");
        return channel;
    }
}
