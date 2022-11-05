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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // Initialize the handler.
        updateStatus(ThingStatus.UNKNOWN);

        // background initialization (delay it a little bit):
        this.startupJob = scheduler.schedule(this::delayedStartUp, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        stopScheduler(this.startupJob);
        stopScheduler(this.pollingJob);
        stopScheduler(this.discoveryJob);
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
        connector.login();
        if (connector.isValidLogin()) {
            connector.querySysInfo(false);
            devicePropertiesChanged(this.deviceInfo);
            updateStatus(ThingStatus.ONLINE);
            queryDeviceData();
            startPollingJob();
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
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
            logger.trace("({}) starScheduler: create job with interval : {}", this.getUID(), pollingInterval);
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
        queryDeviceData();
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
     * QUERY DEVICE DATA
     * 
     * @param asyncRequest
     */
    public void queryDeviceData(Boolean asyncRequest) {
        connector.queryDeviceData(CMD_GET_LANINFO + CMD_GET_WANINFO + CMD_GET_CLIENTLIST, asyncRequest);
    }

    /**
     * QUERY DEVICE DATA
     * do asynchronous request
     */
    public void queryDeviceData() {
        connector.queryDeviceData(CMD_GET_LANINFO + CMD_GET_WANINFO + CMD_GET_CLIENTLIST, true);
    }

    /**
     * UPDATE ALL CLIENT THINGS
     * 
     * @param AsuswrtRouterInfo
     */
    public void updateClients(AsuswrtRouterInfo routerInfo) {
        updateClients(routerInfo.getClients());
    }

    /**
     * UPDATE ALL CLIENT THINGS
     * 
     * @param clientList AsuswrtClientList
     */
    public void updateClients(AsuswrtClientList clientList) {
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
                } else {
                    logger.debug("({}) unable to send RefreshCommand to client : {} - thingHandler is null", thingUID,
                            thingTypeUID.getAsString());
                }

            }
        }
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

    public AsuswrtClientList getClients() {
        return this.deviceInfo.getClients();
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
     * CHANNELS
     *
     ************************************/

    /**
     * Update all Channels
     * 
     * @param deviceInfo
     */
    public void updateChannels(AsuswrtRouterInfo deviceInfo) {
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

        /* clients */
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENT_ONLINE_NAMES),
                getStringType(deviceInfo.getClients().getOnlineClientNames()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENT_ONLINE_MAC),
                getStringType(deviceInfo.getClients().getOnlineClientMACs()));
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
