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
package org.openhab.binding.asuswrt.internal;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.asuswrt.internal.api.AsuswrtConnector;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtErrorHandler;
import org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtConfiguration;
import org.openhab.binding.asuswrt.internal.structures.AsuswrtRouterInfo;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
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
public class AsuswrtRouter extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AsuswrtRouter.class);

    private @Nullable ScheduledFuture<?> startupJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private final HttpClient httpClient;
    private AsuswrtConfiguration config;
    private AsuswrtConnector connector;
    public AsuswrtErrorHandler errorHandler;
    public AsuswrtRouterInfo deviceInfo;

    /**
     * INIT CLASS
     * 
     * @param thing
     */
    public AsuswrtRouter(Thing thing, HttpClient httpClient) {
        super(thing);
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
            connector.querySysInfo();
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

    public void queryDeviceData() {
        connector.queryDeviceData(CMD_GET_LANINFO + CMD_GET_WANINFO + CMD_GET_CLIENTLIST);
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

    public String getUID() {
        return thing.getUID().getAsString();
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
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_LAN_IP),
                getStringType(deviceInfo.getLanInfo().getIpAddress()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_LAN_SUBNET),
                getStringType(deviceInfo.getLanInfo().getSubnet()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_LAN_GW),
                getStringType(deviceInfo.getLanInfo().getGateway()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_LAN_MAC),
                getStringType(deviceInfo.getLanInfo().getMAC()));
        updateState(getChannelID(CHANNEL_GROUP_LANINFO, CHANNEL_LAN_PROTO),
                getStringType(deviceInfo.getLanInfo().getIpProto()));

        /* wanInfo */
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_IP),
                getStringType(deviceInfo.getWanInfo().getIpAddress()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_SUBNET),
                getStringType(deviceInfo.getWanInfo().getSubnet()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_GW),
                getStringType(deviceInfo.getWanInfo().getGateway()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_PROTO),
                getStringType(deviceInfo.getWanInfo().getIpProto()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_DNS_SERVER),
                getStringType(deviceInfo.getWanInfo().getDNSNServer()));
        updateState(getChannelID(CHANNEL_GROUP_WANINFO, CHANNEL_WAN_STATUS),
                getOnOffType(deviceInfo.getWanInfo().getConnectionState()));

        /* clients */
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENT_ONLINE_NAMES),
                getStringType(deviceInfo.getClients().getOnlineClientNames()));
        updateState(getChannelID(CHANNEL_GROUP_CLIENTS, CHANNEL_CLIENT_ONLINE_MAC),
                getStringType(deviceInfo.getClients().getOnlineClientMACs()));
    }

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
}
