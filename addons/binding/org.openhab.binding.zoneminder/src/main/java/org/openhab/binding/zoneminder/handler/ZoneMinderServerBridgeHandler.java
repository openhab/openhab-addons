/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.openhab.binding.zoneminder.internal.request.ZoneMinderMonitorRequest;
import org.openhab.binding.zoneminder.internal.request.ZoneMinderMonitorTriggerRequest;
import org.openhab.binding.zoneminder.internal.request.ZoneMinderRequestType;
import org.openhab.binding.zoneminder.internal.request.ZoneMinderServerBaseRequest;
import org.openhab.binding.zoneminder.internal.request.ZoneMinderServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.ZoneMinderServerProxy;
import name.eskildsen.zoneminder.api.ZoneMinderData;
import name.eskildsen.zoneminder.api.ZoneMinderDiskUsage;
import name.eskildsen.zoneminder.api.daemon.ZoneMinderHostDaemonStatus;
import name.eskildsen.zoneminder.api.host.ZoneMinderHostLoad;
import name.eskildsen.zoneminder.api.host.ZoneMinderHostVersion;
import name.eskildsen.zoneminder.api.monitor.ZoneMinderMonitor;

/**
 * Handler for a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen
 *
 */
public class ZoneMinderServerBridgeHandler extends BaseBridgeHandler
        implements ZoneMinderHandler, ZoneMinderMonitorEventListener {

    public static final int TELNET_TIMEOUT = 5000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER);

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledFuture<?> taskWatchDog = null;

    /** Connection status for the bridge. */
    private boolean connected = false;

    protected Boolean isAlive = false;

    private Runnable watchDogRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                updateAvaliabilityStatus();

            } catch (Exception exception) {
                logger.error("Server WatchDog::run(): Exception: ", exception);
            }
        }
    };

    /**
     * Bridge configuration from OpenHAB
     */
    ZoneMinderBridgeServerConfig config = null;

    Boolean isInitialized = false;

    Map<String, ZoneMinderData> zmServerData = new HashMap<String, ZoneMinderData>();

    /**
     * ZoneMinder Server connection
     */
    private ZoneMinderServerProxy zoneMinderServerProxy = null;

    private ScheduledFuture<?> taskHighPriorityRefresh = null;
    private ScheduledFuture<?> taskLowPriorityRefresh = null;

    private Runnable refreshHighPriorityDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                refreshHighPriorityPriorityData();
            } catch (Exception exception) {
                logger.error("monitorRunnable::run(): Exception: ", exception);
            }
        }
    };

    private Runnable refreshLowPriorityDataRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (isConnected() == true) {
                    refreshLowPriorityPriorityData();
                }
            } catch (Exception exception) {
                logger.error("monitorRunnable::run(): Exception: ", exception);
            }
        }
    };

    /**
     * Constructor
     *
     *
     * @param bridge
     *            Bridge object representing a ZoneMinder Server
     */
    public ZoneMinderServerBridgeHandler(Bridge bridge) {
        super(bridge);

        logger.info("Starting ZoneMinder Server Bridge Handler (Bridge='{}')", bridge.getBridgeUID());
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize bridge " + ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER);
        super.initialize();
        try {

            this.config = getBridgeConfig();

            logger.debug("ZoneMinder Server Bridge Handler Initialized");
            logger.debug("   HostName:           {}", config.getHostName());
            logger.debug("   Protocol:           {}", config.getProtocol());
            logger.debug("   Port (HTTP)         {}", config.getHttpPort());
            logger.debug("   Port (Telnet)       {}", config.getTelnetPort());
            logger.debug("   Server Path         {}", config.getServerBasePath());
            logger.debug("   User:               {}", config.getUserName());
            logger.debug("   Refresh interval:   {}", config.getRefreshInterval());
            logger.debug("   Low  prio. refresh: {}", config.getRefreshIntervalLowPriorityTask());

            closeConnection();

            taskHighPriorityRefresh = null;
            taskLowPriorityRefresh = null;

        } catch (Exception ex) {
            logger.error("'ZoneMinderServerBridgeHandler' failed to initialize. Exception='{}'", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        } finally {
            startWatchDogTask();
            isInitialized = true;
        }
    }

    protected void startWatchDogTask() {
        taskWatchDog = startTask(watchDogRunnable, 10, TimeUnit.SECONDS);
    }

    protected void stopWatchDogTask() {
        stopTask(taskWatchDog);
        taskWatchDog = null;
    }

    /**
     */
    @Override
    public void dispose() {
        logger.debug("Stop polling of ZoneMinder Server API");
        stopWatchDogTask();
        stopTask(taskHighPriorityRefresh);
        stopTask(taskLowPriorityRefresh);
    }

    @Override
    public String getZoneMinderId() {
        return getThing().getUID().getAsString();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug("Channel '" + channelUID.getAsString() + "' was linked to '" + this.thing.getThingTypeUID() + "'");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug(
                "Channel '" + channelUID.getAsString() + "' was unlinked from '" + this.thing.getThingTypeUID() + "'");
    }

    protected ZoneMinderBridgeServerConfig getBridgeConfig() {
        return this.getConfigAs(ZoneMinderBridgeServerConfig.class);
    }

    /**
    *
    */
    public ZoneMinderBaseThingHandler getZoneMinderThingHandlerFromZoneMinderId(ThingTypeUID thingTypeUID,
            String zoneMinderId) {

        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            logger.debug("getZoneMinderThingHandlerFromZoneMinderId(): '{}' ?= '{}'", thingHandler.getZoneMinderId(),
                    zoneMinderId);
            if ((thingHandler.getZoneMinderId().equals(zoneMinderId))
                    && (thing.getThingTypeUID().equals(thingTypeUID))) {
                return thingHandler;
            }
        }
        return null;
    }

    /**
     * Method for polling the ZoneMinder Server.
     */
    protected void onRefreshLowPriorityPriorityData() {
        logger.debug("Refreshing diskusage from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (!isConnected()) {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }

        /*
         * Fetch data for all monitors attached to this bridge
         */
        if (isConnected()) {
            ZoneMinderServerRequest request = new ZoneMinderServerRequest(
                    ZoneMinderRequestType.SERVER_LOW_PRIORITY_DATA, getZoneMinderId());
            processZoneMinderServerRequest(request);
        }
    }

    protected void onRefreshHighPriorityPriorityData() {
        logger.trace("ZoneMinder Server: Refreshing Bridge...");

        // Refresh channels on the bridge
        this.refreshThing();

        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {

            ZoneMinderBaseThingHandler handler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (handler != null) {
                logger.debug("***Checking '{}' - Status: {}, Refreshed: {}", thing.getUID(), thing.getStatus(),
                        handler.isThingRefreshed());

                handler.refreshThing();

            } else {
                logger.error("refreshBridge(): Thing handler not found!");
            }

        }

    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("update " + channelUID.getAsString() + " with " + command.toString());
    }

    protected void refreshThing() {

        logger.debug("refreshThing(): Thing='{}'!", getThing().getUID(), this.getThing().getUID());
        List<Thing> things = getThing().getThings();
        List<Channel> channels = getThing().getChannels();

        if (isConnected()) {

            /*
             * Fetch data for all monitors attached to this bridge
             */
            logger.debug("Sending request for refresh of Server channels....");
            ZoneMinderServerBaseRequest request = new ZoneMinderServerRequest(
                    ZoneMinderRequestType.SERVER_HIGH_PRIORITY_DATA, getZoneMinderId());
            processZoneMinderServerRequest(request);

            for (Thing thing : things) {
                try {
                    if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                        Thing thingMonitor = thing;
                        ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                        logger.debug(String.format("Sending request for refresh of channels for monitor '%s'....",
                                thingHandler.getZoneMinderId()));

                        request = new ZoneMinderMonitorRequest(ZoneMinderRequestType.MONITOR_THING,
                                thingHandler.getZoneMinderId());
                        processZoneMinderServerRequest(request);

                        logger.debug("Updated ZoneMinderApiData for Thing: {}  {}", thing.getThingTypeUID(),
                                thing.getUID());
                    }

                } catch (Exception ex) {
                    logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                            this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                }
            }

        }

        /*
         * Update all channels
         */
        for (Channel channel : channels) {
            updateChannel(channel.getUID());
        }

        // this.setThingRefreshed(false);
    }

    @Override
    public void notifyZoneMinderApiDataUpdated(ThingTypeUID thingTypeUID, String zoneMinderId,
            List<ZoneMinderData> arrData) {

        if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER)) {

            // Check data sets individually and update the local copy if they are there
            // ZoneMinderServerData updatedData = (ZoneMinderServerData) data;
            synchronized (zmServerData) {
                for (ZoneMinderData data : arrData) {
                    String dataClassKey = data.getKey();
                    if (zmServerData.containsKey(dataClassKey)) {
                        zmServerData.remove(dataClassKey);
                    }
                    zmServerData.put(dataClassKey, data);
                }
            }
        } else {
            ZoneMinderBaseThingHandler thing = getZoneMinderThingHandlerFromZoneMinderId(thingTypeUID, zoneMinderId);

            // If thing not found, then it is not to this thing that it belongs :-)
            if (thing != null) {

                thing.notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, arrData);
            }
        }
    }

    /**
     * Connect The Bridge.
     */
    protected synchronized void connect() {
        onDisconnected();

        openConnection();

        if (isConnected()) {
            onConnected();
        }
    }

    /**
     * Disconnect The Bridge.
     */
    private synchronized void disconnect() {

        closeConnection();

        if (!isConnected()) {
            onDisconnected();
        }
    }

    /**
     * Returns connection status.
     */
    public synchronized Boolean isConnected() {
        return connected;
    }

    public Boolean isAlive() {
        return isAlive;
    }

    /**
     * Set connection status.
     *
     * @param connected
     */
    private synchronized void setConnected(boolean connected) {

        if (this.connected != connected) {
            this.connected = connected;

            if (connected == true) {
                onConnected();
            } else if (connected == false) {
                onDisconnected();
            }
        }

    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    protected void setBridgeConnection(boolean connected) {
        logger.debug("setBridgeConnection(): Set Bridge to {}", connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingStatus status = bridge.getStatus();
            logger.debug("Bridge ThingStatus is: {}", status);
        }

        setConnected(connected);

    }

    /**
     * Runs when connection established.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        onBridgeConnected(this);

        refreshThing();
        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeConnected(this);
                logger.trace("onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Runs when disconnected.
     */
    public void onDisconnected() {
        logger.debug("onDisconnected(): Bridge Disconnected!");

        onBridgeDisconnected(this);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.trace("onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    @Override
    public void updateAvaliabilityStatus() {
        ThingStatus newStatus = ThingStatus.OFFLINE;
        isAlive = false;

        // Just wait until we are finished initializing
        if (isInitialized == false) {
            return;
        }

        // Check if server Bridge configuration is valid
        if (!isConfigValid()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration");
            setBridgeConnection(false);
            return;
        }

        // Check if we have a connection to ZoneMinder Server - else try to establish one
        if (zoneMinderServerProxy == null) {
            if (openConnection() == false) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot access ZoneMinder Server. Check provided usercredentials");
                setBridgeConnection(false);
                return;
            }
        }

        if (!zoneMinderServerProxy.isConnected()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot access ZoneMinder Server. Check provided usercredentials");
            setBridgeConnection(false);
            return;
        }

        if (zoneMinderServerProxy.getHostDaemonCheckState() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "ZoneMinder Server Daemon state is unknown.");
            setBridgeConnection(false);
            return;
        }

        ZoneMinderHostDaemonStatus daemonStatus = zoneMinderServerProxy.getHostDaemonCheckState();
        if (!daemonStatus.isDaemonRunning()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "ZoneMinder Server Daemon isn't running.");
            setBridgeConnection(false);
            return;
        }

        // Set Status to OFFLINE if it is OFFLINE

        // Check if server API can be accessed
        if (!isZoneMinderApiEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ZoneMinder Server API isn't enabled. In ZoneMinder make sure option 'ZM_OPT_USE_API' is enabled");
            setBridgeConnection(false);
            return;
        }

        if (!isZoneMinderExternalTriggerEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ZoneMinder Server External trigger isn't enabled. In ZoneMinder enabled ZM_xxx");
            setBridgeConnection(false);
            return;
        }

        // 4. Check server version
        // 5. Check server API version

        // Check if refresh jobs is running
        if (!isZoneMinderServerDaemonEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "ZoneMinder Server cannot be reached. Daemon appears to be stopped.");
            setBridgeConnection(false);
            return;
        }

        isAlive = true;
        if (isAlive == true) {
            newStatus = ThingStatus.ONLINE;
        } else {
            newStatus = ThingStatus.OFFLINE;
        }

        if (thing.getStatus() != newStatus) {
            updateStatus(newStatus);
        }

        // Ask child things to update their Availability Status
        for (Thing thing : getThing().getThings()) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                try {
                    thingHandler.updateAvaliabilityStatus();
                } catch (Exception ex) {
                    logger.debug("Failed to call 'updateAvailabilityStatus()' for '{}'",
                            thingHandler.getThing().getUID());
                }
            }

        }

    }

    protected Boolean isConfigValid() {
        ZoneMinderBridgeServerConfig config = getBridgeConfig();

        if (config == null) {
            return false;
        }

        if (config.getProtocol() == null) {
            return false;
        }

        if (config.getHostName() == null) {
            return false;
        }

        if (config.getHttpPort() == null) {
            return false;
        }

        if (config.getTelnetPort() == null) {
            return false;
        }

        return true;
    }

    protected Boolean isZoneMinderApiEnabled() {

        return true;
    }

    protected Boolean isZoneMinderExternalTriggerEnabled() {
        return true;
    }

    // 4. Check server version
    // 5. Check server API version

    // Check if refresh jobs is running
    protected Boolean isZoneMinderServerDaemonEnabled() {

        return true;
    }

    @Override
    public void updateChannel(ChannelUID channel) {
        State state = null;
        try {

            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                    updateState(channel, (isAlive() ? OnOffType.ON : OnOffType.OFF));
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_ZM_VERSION:
                    state = getServerVersionState();
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_ZM_API_VERSION:
                    state = getServerVersionApiState();
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_DISKUSAGE:
                    state = getServerDiskUsageState();
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_CPULOAD:
                    state = getServerCpuLoadState();
                    break;

                default:
                    logger.warn("updateChannel(): Server '{}': No handler defined for channel='{}'", thing.getLabel(),
                            channel.getAsString());
                    break;
            }

            if (state != null) {
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            logger.error("Error occurred when 'updateChannel()' was called for thing='{}', channel='{}'",
                    channel.getThingUID(), channel.getId());
        }
    }

    Boolean openConnection() {
        boolean connected = false;
        if (isConnected() == false) {
            logger.debug("Connecting Bridge to ZoneMinder Server.");

            try {
                closeConnection();

                zoneMinderServerProxy = new ZoneMinderServerProxy(getBridgeConfig().getProtocol(),
                        getBridgeConfig().getHostName(), getBridgeConfig().getServerBasePath(),
                        getBridgeConfig().getUserName(), getBridgeConfig().getPassword());

                connected = zoneMinderServerProxy.connect();

                logger.debug("openConnection(): Connecting to ZoneMinder Server (Telnet)");

                setBridgeConnection(connected);

                // Subscribe to events for this monitor
                List<Thing> things = getThing().getThings();
                for (Thing thing : things) {
                    try {
                        if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                            Thing thingMonitor = thing;
                            ZoneMinderThingMonitorHandler monitorHandler = (ZoneMinderThingMonitorHandler) thing
                                    .getHandler();

                            logger.debug(
                                    String.format("Subscribe to events from ZoneMinder Server for monitor '%s'....",
                                            monitorHandler.getZoneMinderId()));

                            zoneMinderServerProxy.SubscribeMonitorEvents(monitorHandler.getZoneMinderId(),
                                    monitorHandler);
                        }

                    } catch (Exception ex) {
                        logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                                this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                    }
                }

            } catch (UnknownHostException unknownException) {
                logger.error("openConnection(): Unknown Host Exception: ", unknownException);
                setBridgeConnection(false);
            } catch (SocketException socketException) {
                logger.error("openConnection(): Socket Exception: ", socketException);
                setBridgeConnection(false);
            } catch (IOException ioException) {
                logger.error("openConnection(): IO Exception: ", ioException);
                setBridgeConnection(false);
            } catch (Exception exception) {
                logger.error("openConnection(): Exception: ", exception);
                setBridgeConnection(false);
            } finally {
                if (isConnected() == false) {
                    closeConnection();
                }
            }

        }
        return isConnected();

    }

    synchronized void closeConnection() {
        try {
            if (zoneMinderServerProxy != null) {
                zoneMinderServerProxy.closeConnection();
                zoneMinderServerProxy = null;
            }
            logger.debug("closeConnection(): Closed HTTP Connection!");

            setBridgeConnection(false);

        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - " + exception.getMessage());
        }

    }

    public void activateZoneMinderMonitorTrigger(String monitorId, String text, Integer timeout) {
        ZoneMinderMonitorTriggerRequest request = new ZoneMinderMonitorTriggerRequest(
                ZoneMinderRequestType.MONITOR_TRIGGER_REQUEST, true, monitorId, 255, text, timeout);

        processZoneMinderServerRequest(request);
    }

    public void cancelZoneMinderMonitorTrigger(String monitorId) {
        ZoneMinderMonitorTriggerRequest request = new ZoneMinderMonitorTriggerRequest(
                ZoneMinderRequestType.MONITOR_TRIGGER_REQUEST, false, monitorId, 255, "", 0);

        processZoneMinderServerRequest(request);
    }

    private boolean processZoneMinderServerRequest(ZoneMinderServerBaseRequest request) {
        List<ZoneMinderData> arrData = new ArrayList<ZoneMinderData>();
        ThingTypeUID thingTypeUID = request.getThingTypeUID();
        String zoneMinderId = request.getId();
        Boolean doNotify = false;
        switch (request.getRequestType()) {

            case MONITOR_THING:
                arrData.add(zoneMinderServerProxy.getMonitor(zoneMinderId));
                arrData.add(zoneMinderServerProxy.getMonitorCaptureDaemonStatus(zoneMinderId));
                arrData.add(zoneMinderServerProxy.getMonitorAnalysisDaemonStatus(zoneMinderId));
                arrData.add(zoneMinderServerProxy.getMonitorFrameDaemonStatus(zoneMinderId));
                doNotify = true;
                break;

            case SERVER_HIGH_PRIORITY_DATA:
                arrData.add((zoneMinderServerProxy.getHostVersion()));
                arrData.add((zoneMinderServerProxy.getHostCpuLoad()));
                arrData.add((zoneMinderServerProxy.getHostDaemonCheckState()));
                doNotify = true;
                break;

            case SERVER_LOW_PRIORITY_DATA:
                arrData.add((zoneMinderServerProxy.getHostDiskUsage()));
                doNotify = true;
                break;

            case MONITOR_TRIGGER_REQUEST:
                ZoneMinderMonitorTriggerRequest triggerRequest = (ZoneMinderMonitorTriggerRequest) request;
                try {
                    if (triggerRequest.getActivatedState()) {
                        zoneMinderServerProxy.activateForceAlarm(triggerRequest.getId(), triggerRequest.getPriority(),
                                triggerRequest.getReason(), "", "", triggerRequest.getTimeout());
                    } else {
                        zoneMinderServerProxy.deactivateForceAlarm(triggerRequest.getId());
                    }
                } catch (Exception e) {

                    logger.error(e.getMessage());
                }
                break;
            default:
                logger.warn("Unhandled ZoneMinder Server request occurred (request='{}'", request.getRequestType());
        }

        if (doNotify == true) {
            notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, arrData);
        }

        return false;

    }

    public ArrayList<ZoneMinderMonitor> getMonitors() {
        return zoneMinderServerProxy.getMonitors();
    }

    protected State getServerVersionState() {

        State state = new StringType("-");
        synchronized (zmServerData) {
            if (zmServerData.containsKey(ZoneMinderHostVersion.class.getSimpleName())) {
                ZoneMinderHostVersion data = (ZoneMinderHostVersion) zmServerData
                        .get(ZoneMinderHostVersion.class.getSimpleName());
                state = new StringType(data.getVersion());
            }
        }

        return state;
    }

    protected State getServerVersionApiState() {

        State state = new StringType("-");
        synchronized (zmServerData) {
            if (zmServerData.containsKey(ZoneMinderHostVersion.class.getSimpleName())) {
                ZoneMinderHostVersion data = (ZoneMinderHostVersion) zmServerData
                        .get(ZoneMinderHostVersion.class.getSimpleName());
                state = new StringType(data.getApiVersion());
            }
        }

        return state;
    }

    protected State getServerCpuLoadState() {

        State state = new StringType("-");

        try {
            synchronized (zmServerData) {
                if (zmServerData.containsKey(ZoneMinderHostLoad.class.getSimpleName())) {
                    ZoneMinderHostLoad data = (ZoneMinderHostLoad) zmServerData
                            .get(ZoneMinderHostLoad.class.getSimpleName());

                    state = new StringType(data.getCpuLoad().toString());
                }
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getServerDiskUsageState() {

        State state = new StringType("-");

        try {
            synchronized (zmServerData) {
                if (zmServerData.containsKey(ZoneMinderDiskUsage.class.getSimpleName())) {
                    ZoneMinderDiskUsage data = (ZoneMinderDiskUsage) zmServerData
                            .get(ZoneMinderDiskUsage.class.getSimpleName());

                    state = new StringType(data.getDiskUsage());
                }
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge) {

        if (taskHighPriorityRefresh == null) {
            taskHighPriorityRefresh = startTask(refreshHighPriorityDataRunnable, config.getRefreshInterval(),
                    TimeUnit.SECONDS);
        }

        if (taskLowPriorityRefresh == null) {
            taskLowPriorityRefresh = startTask(refreshLowPriorityDataRunnable,
                    config.getRefreshIntervalLowPriorityTask(), TimeUnit.MINUTES);
        }

    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {

        if (taskLowPriorityRefresh != null) {
            stopTask(taskLowPriorityRefresh);
            taskLowPriorityRefresh = null;
        }
        synchronized (zmServerData) {
            zmServerData.clear();
        }

    }

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startTask(Runnable command, long refreshInterval, TimeUnit unit) {
        ScheduledFuture<?> task = null;
        logger.debug("Starting ZoneMinder Bridge Monitor Task. Command='{}'", command.toString());
        if (refreshInterval == 0) {
            return task;
        }

        if (task == null || task.isCancelled()) {
            task = scheduler.scheduleWithFixedDelay(command, 0, refreshInterval, unit);
        }
        return task;
    }

    /**
     * Method to stop the datarefresh task.
     */
    protected void stopTask(ScheduledFuture<?> task) {
        logger.debug("Stopping ZoneMinder Bridge Monitor Task. Task='{}'", task.toString());
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    /**
     * Method for updating High priority Data.
     */

    public synchronized void refreshHighPriorityPriorityData() {
        logger.debug("Refreshing high priority data from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (!isConnected()) {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }

        onRefreshHighPriorityPriorityData();
    }

    /**
     * Method for updating Low priority Data.
     */

    public synchronized void refreshLowPriorityPriorityData() {
        logger.debug("Refreshing low priority data from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (isConnected()) {
            onRefreshLowPriorityPriorityData();
        } else {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }
    }

}
