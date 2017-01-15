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
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.ZoneMinderMonitorProperties;
import org.openhab.binding.zoneminder.internal.DataFetchRunnable;
import org.openhab.binding.zoneminder.internal.DataRefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.api.ZoneMinderDiskUsage;
import name.eskildsen.zoneminder.api.config.ZoneMinderConfig;
import name.eskildsen.zoneminder.api.config.ZoneMinderConfigEnum;
import name.eskildsen.zoneminder.api.host.ZoneMinderHostLoad;
import name.eskildsen.zoneminder.api.host.ZoneMinderHostVersion;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;
import name.eskildsen.zoneminder.interfaces.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.interfaces.IZoneMinderMonitorData;
import name.eskildsen.zoneminder.interfaces.IZoneMinderServer;

/**
 * Handler for a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen
 *
 */
public class ZoneMinderServerBridgeHandler extends BaseBridgeHandler implements ZoneMinderHandler {

    public static final int TELNET_TIMEOUT = 5000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER);

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledFuture<?> taskWatchDog = null;
    private int refreshFrequency = 0;
    private int refreshCycleCount = 0;

    /** Connection status for the bridge. */
    private boolean connected = false;

    protected boolean _alive = false;

    private Runnable watchDogRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                updateAvaliabilityStatus(zoneMinderConnection);
            } catch (Exception exception) {
                logger.error("Server WatchDog::run(): Exception: ", exception);
            }
        }
    };

    /**
     * Local copies of last fetched values from ZM
     */
    private String channelCpuLoad = "";
    private String channelDiskUsage = "";

    /**
     * Bridge configuration from OpenHAB
     */
    ZoneMinderBridgeServerConfig config = null;

    Boolean isInitialized = false;

    private IZoneMinderConnectionInfo zoneMinderConnection = null;

    private ScheduledFuture<?> taskRefreshData = null;
    private ScheduledFuture<?> taskPriorityRefreshData = null;

    private DataFetchRunnable refreshDataRunnable = new DataFetchRunnable(zoneMinderConnection) {

        @Override
        public void run() {
            try {
                boolean fetchDiskUsage = false;

                if (isConnected()) {
                    connect();
                }

                refreshCycleCount++;

                if ((refreshCycleCount * refreshFrequency >= config.getRefreshInterval())) {

                    // Is it time to query DiskUsage??
                    if ((refreshCycleCount * refreshFrequency) >= (config.getRefreshIntervalLowPriorityTask() * 60)) {
                        refreshCycleCount = 0;
                        logger.debug("'refreshDataRunnable()': (diskUsage='true'");
                        fetchDiskUsage = true;
                    } else {
                        logger.debug("'refreshDataRunnable()': (diskUsage='false'");
                    }

                    refreshThing(fetchDiskUsage);

                }

            } catch (Exception exception) {
                logger.error("monitorRunnable::run(): Exception: ", exception);
            }
        }
    };

    private DataFetchRunnable refreshPriorityDataRunnable = new DataFetchRunnable(zoneMinderConnection) {

        @Override
        public void run() {
            try {
                if (isConnected()) {
                    connect();
                }

                // Make sure priority updates is done
                for (Thing thing : getThing().getThings()) {
                    try {

                        if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                            Thing thingMonitor = thing;
                            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                            if (thingHandler.getRefreshPriority() == DataRefreshPriorityEnum.HIGH_PRIORITY) {
                                logger.debug(String.format("[MONITOR %s] RefreshPriority is High Priority",
                                        thingHandler.getZoneMinderId()));
                                thingHandler.refreshThing(DataRefreshPriorityEnum.HIGH_PRIORITY);
                            }
                        }

                    } catch (NullPointerException ex) {
                        // TODO:: This isn't critical (unless it comes over and over). There seems to be a bug so that a
                        // null
                        // pointer exception is coming every now and then.
                        // HAve to find the reason for that. Until thenm, don't Spamm
                        logger.debug("Method 'refreshThing()' for Bridge failed for thing='{}' - Exception='{}'",
                                thing.getUID(), ex);
                    } catch (Exception ex) {
                        logger.error("Method 'refreshThing()' for Bridge failed for thing='{}' - Exception='{}'",
                                thing.getUID(), ex);
                    }
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
            updateStatus(ThingStatus.OFFLINE);

            this.config = getBridgeConfig();

            logger.info("ZoneMinder Server Bridge Handler Initialized");
            logger.debug("   HostName:           {}", config.getHostName());
            logger.debug("   Protocol:           {}", config.getProtocol());
            logger.debug("   Port HTTP(S)        {}", config.getHttpPort());
            logger.debug("   Port Telnet         {}", config.getTelnetPort());
            logger.debug("   Server Path         {}", config.getServerBasePath());
            logger.debug("   User:               {}", config.getUserName());
            logger.debug("   Refresh interval:   {}", config.getRefreshInterval());
            logger.debug("   Low  prio. refresh: {}", config.getRefreshIntervalLowPriorityTask());

            closeConnection();

            zoneMinderConnection = ZoneMinderFactory.CreateConnection(getBridgeConfig().getProtocol(),
                    getBridgeConfig().getHostName(), getBridgeConfig().getHttpPort(), getBridgeConfig().getTelnetPort(),
                    getBridgeConfig().getServerBasePath(), getBridgeConfig().getUserName(),
                    getBridgeConfig().getPassword());

            taskRefreshData = null;
            taskPriorityRefreshData = null;

        } catch (Exception ex) {
            logger.error("'ZoneMinderServerBridgeHandler' failed to initialize. Exception='{}'", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        } finally {
            startWatchDogTask();
            isInitialized = true;
        }
    }

    /**
     * Method to find the lowest possible refresh rate (based on configuration)
     *
     * @param refreshRate
     * @return
     */
    protected int calculateCommonRefreshFrequency(int refreshRate) {
        // Check if 30, 15, 10 or 5 seconds is possible
        if ((refreshRate % 30) == 0) {
            return 30;
        } else if ((refreshRate % 15) == 0) {
            return 15;
        } else if ((refreshRate % 10) == 0) {
            return 10;
        } else if ((refreshRate % 5) == 0) {
            return 5;
        }

        // Hmm, didn't find a obvious shared value. Run every second...
        return 1;

    }

    protected void startWatchDogTask() {
        taskWatchDog = startTask(watchDogRunnable, 0, 10, TimeUnit.SECONDS);
    }

    protected void stopWatchDogTask() {
        stopTask(taskWatchDog);
        taskWatchDog = null;
    }

    /**
     */
    @Override
    public void dispose() {
        logger.debug("[BRIDGE] - Stop polling of ZoneMinder Server API");

        logger.info("[BRIDGE] - Stopping WatchDog task");
        stopWatchDogTask();

        logger.info("[BRIDGE] - Stopping refresh data task");
        stopTask(taskRefreshData);
    }

    @Override
    public String getZoneMinderId() {

        return getThing().getUID().getAsString();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug("[BRIDGE] - Channel '" + channelUID.getAsString() + "' was linked to '"
                + this.thing.getThingTypeUID() + "'");
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug("[BRIDGE] - Channel '" + channelUID.getAsString() + "' was unlinked from '"
                + this.thing.getThingTypeUID() + "'");
    }

    public ArrayList<IZoneMinderMonitorData> getMonitors() {

        if (isConnected()) {
            return ZoneMinderFactory.getServerProxy().getMonitors();
        }

        return new ArrayList<IZoneMinderMonitorData>();

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
            if ((thingHandler.getZoneMinderId().equals(zoneMinderId))
                    && (thing.getThingTypeUID().equals(thingTypeUID))) {
                return thingHandler;
            }
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("[BRIDGE] - Update " + channelUID.getAsString() + " with " + command.toString());
    }

    protected synchronized void refreshThing(boolean fetchDiskUsage) {

        logger.debug("'[BRIDGE] - refreshThing()': Thing='{}'!", getThing().getUID(), this.getThing().getUID());

        List<Channel> channels = getThing().getChannels();
        List<Thing> things = getThing().getThings();

        IZoneMinderServer zoneMinderServerProxy = ZoneMinderFactory.getServerProxy();
        if (isConnected()) {
            /*
             * Fetch data for Bridge
             */
            ZoneMinderHostLoad hostLoad = zoneMinderServerProxy.getHostCpuLoad();
            channelCpuLoad = hostLoad.getCpuLoad().toString();

            if (config.getRefreshIntervalLowPriorityTask() != 0) {
                ZoneMinderDiskUsage du = zoneMinderServerProxy.getHostDiskUsage();
                channelDiskUsage = du.getDiskUsage();
            }
        } else {
            _alive = false;
            // Make sure old data is cleared
            channelCpuLoad = "";
            channelDiskUsage = "";
        }

        /*
         * Update all channels on Bridge
         */
        for (Channel channel : channels) {
            updateChannel(channel.getUID());
        }

        /*
         * Request Things attached to Bridge to refresh
         */
        for (Thing thing : things) {
            try {

                if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                    Thing thingMonitor = thing;
                    ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                    thingHandler.refreshThing(DataRefreshPriorityEnum.SCHEDULED);
                }

            } catch (NullPointerException ex) {
                // TODO:: This isn't critical (unless it comes over and over). There seems to be a bug so that a null
                // pointer exception is coming every now and then.
                // HAve to find the reason for that. Until thenm, don't Spamm
                logger.debug("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                        this.getZoneMinderId(), thing.getUID(), ex.getMessage());

                // Other exceptions has to be shown as errors
            } catch (Exception ex) {
                logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                        this.getZoneMinderId(), thing.getUID(), ex.getMessage());
            }
        }

    }

    /**
     * Returns connection status.
     */
    public synchronized Boolean isConnected() {
        return connected;
    }

    public boolean isAlive() {
        return _alive;
    }

    private synchronized boolean getConnected() {
        return this.connected;
    }

    /**
     * Set connection status.
     *
     * @param connected
     */
    private synchronized void setConnected(boolean connected) {

        if (this.connected != connected) {
            this.connected = connected;
        }

    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    private void setBridgeConnectionStatus(boolean connected) {
        logger.debug("setBridgeConnection(): Set Bridge to {}", connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingStatus status = bridge.getStatus();
            logger.debug("Bridge ThingStatus is: {}", status);
        }

        setConnected(connected);
    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    private boolean getBridgeConnectionStatus() {
        return getConnected();
    }

    /**
     * Runs when connection established.
     *
     * @throws ZoneMinderUrlNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");
        setConnected(true);
        onBridgeConnected(this, zoneMinderConnection);

        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                try {
                    thingHandler.onBridgeConnected(this, zoneMinderConnection);
                } catch (IllegalArgumentException | GeneralSecurityException | IOException
                        | ZoneMinderUrlNotFoundException e) {
                    logger.error("Bridge.onConnected() failed - Exceprion: {}", e.getMessage());
                }
                logger.debug("onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Runs when disconnected.
     */
    private void onDisconnected() {
        logger.debug("onDisconnected(): Bridge Disconnected!");
        setConnected(false);
        onBridgeDisconnected(this);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.debug("onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
    }

    @Override
    public void updateAvaliabilityStatus(IZoneMinderConnectionInfo connection) {
        ThingStatus newStatus = ThingStatus.OFFLINE;
        boolean hasValidConnection = false;
        boolean _isAlive = false;

        ThingStatus prevStatus = getThing().getStatus();

        // Connection valid since we sare already online
        if (prevStatus == ThingStatus.ONLINE) {
            hasValidConnection = true;
        }

        // If we we are already ONLINE and have a session
        else if ((prevStatus == ThingStatus.OFFLINE) && (connection != null)) {
            hasValidConnection = ZoneMinderFactory.validateConnection(connection);
            try {
                if (hasValidConnection) {
                    ZoneMinderFactory.Initialize(connection, 5);
                }
            } catch (IllegalArgumentException | GeneralSecurityException | IOException
                    | ZoneMinderUrlNotFoundException e) {
            }
        }

        IZoneMinderServer zoneMinderServerProxy = ZoneMinderFactory.getServerProxy();

        try {

            // Just wait until we are finished initializing
            if (isInitialized == false) {
                _alive = _isAlive;
                return;
            }

            // Check if server Bridge configuration is valid
            if (!isConfigValid()) {

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration");
                setBridgeConnectionStatus(false);
                return;
            }

            if (zoneMinderServerProxy == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot access ZoneMinder Server. Check provided usercredentials");
                setBridgeConnectionStatus(false);
                return;
            } else if (zoneMinderServerProxy.getHostDaemonCheckState() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "ZoneMinder Server Daemon state is unknown.");
                setBridgeConnectionStatus(false);
                return;
            }

            else if (!zoneMinderServerProxy.getHostDaemonCheckState().isDaemonRunning()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "ZoneMinder Server Daemon isn't running.");
                setBridgeConnectionStatus(false);
                return;
            }

            // Set Status to OFFLINE if it is OFFLINE

            // Check if server API can be accessed
            if (!isZoneMinderApiEnabled()) {

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "ZoneMinder Server API isn't enabled. In ZoneMinder make sure option 'ZM_OPT_USE_API' is enabled");
                setBridgeConnectionStatus(false);
                return;
            }

            if (!isZoneMinderExternalTriggerEnabled()) {

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "ZoneMinder Server External trigger isn't enabled. In ZoneMinder enabled ZM_xxx");
                setBridgeConnectionStatus(false);
                return;
            }

            // 4. Check server version
            // 5. Check server API version

            // Check if refresh jobs is running
            if (!isZoneMinderServerDaemonEnabled()) {

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "ZoneMinder Server cannot be reached. Daemon appears to be stopped.");
                setBridgeConnectionStatus(false);
                _alive = _isAlive;
                return;
            } else {
                _isAlive = true;
            }

            if (_isAlive == true) {
                _alive = _isAlive;
                newStatus = ThingStatus.ONLINE;
            } else {
                _alive = _isAlive;
                newStatus = ThingStatus.OFFLINE;
            }

            // Status changed
            if (thing.getStatus() != newStatus) {
                updateStatus(newStatus);
            }

        } finally {
            if (prevStatus != newStatus) {

                // Wen't to other state than ONLINE
                if (prevStatus == ThingStatus.ONLINE) {
                    onDisconnected();

                }
                // Wen't To ONLINE status
                else if (newStatus == ThingStatus.ONLINE) {
                    try {
                        onConnected();
                    } catch (IllegalArgumentException e) {
                        // Just ignore that here
                    }
                }
                updateStatus(newStatus);
            }
        }

        // Ask child things to update their Availability Status
        for (Thing thing : getThing().getThings()) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                try {
                    thingHandler.updateAvaliabilityStatus(connection);
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
                logger.debug(String.format("BridgeHandler.updateChannel(): Updating channel '%s' to state='%s'",
                        channel.getId(), state.toString()));
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            String message = String.format(
                    "Error when 'updateChannel()' was called for thing='%s', channel='%s' [Exception='%s']",
                    channel.getThingUID(), channel.getId(), ex.getMessage());
            logger.error(message);

        }
    }

    protected boolean openConnection() {
        boolean connected = false;
        if (isConnected() == false) {
            logger.debug("Connecting Bridge to ZoneMinder Server.");

            try {
                closeConnection();

                if (zoneMinderConnection != null) {
                    connected = ZoneMinderFactory.validateConnection(zoneMinderConnection);
                } else {
                    connected = false;
                }
                setConnected(connected);

                logger.info("Connecting to ZoneMinder Server (result='{}'", connected);

                if (isConnected()) {
                    // Subscribe to events for this monitor
                    List<Thing> things = getThing().getThings();
                    for (Thing thing : things) {
                        try {
                            if (thing.getThingTypeUID()
                                    .equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {

                                Thing thingMonitor = thing;
                                ZoneMinderThingMonitorHandler monitorHandler = (ZoneMinderThingMonitorHandler) thing
                                        .getHandler();

                                logger.debug(
                                        String.format("Subscribe to events from ZoneMinder Server for monitor '%s'....",
                                                monitorHandler.getZoneMinderId()));

                                ZoneMinderFactory.SubscribeMonitorEvents(monitorHandler.getZoneMinderId(),
                                        monitorHandler);

                            }

                        } catch (Exception ex) {
                            logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                                    this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                        }
                    }
                }

            } catch (Exception exception) {
                logger.error("openConnection(): Exception: ", exception);
                setConnected(false);
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
            logger.debug("closeConnection(): Closed HTTP Connection!");
            setConnected(false);

        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - " + exception.getMessage());
        }

    }

    protected State getServerCpuLoadState() {

        State state = UnDefType.UNDEF;

        try {
            if ((channelCpuLoad != "") && (isConnected())) {
                state = new DecimalType(new BigDecimal(channelCpuLoad));
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getServerDiskUsageState() {

        State state = UnDefType.UNDEF;

        try {
            if ((channelDiskUsage != "") && (isConnected())) {
                state = new DecimalType(new BigDecimal(channelDiskUsage));
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionInfo connection) {
        logger.info("Brigde with Id='{}'' went ONLINE", getZoneMinderId());
        if (taskRefreshData == null) {
            if (config.getRefreshIntervalLowPriorityTask() != 0) {
                refreshFrequency = calculateCommonRefreshFrequency(config.getRefreshInterval());
            } else {
                refreshFrequency = config.getRefreshInterval();
            }
            if (taskRefreshData == null) {
                taskRefreshData = startTask(refreshDataRunnable, 0, refreshFrequency, TimeUnit.SECONDS);
            }
            // Only start if it is not running and Priority Frequency is higher than ordinary
            if ((taskPriorityRefreshData == null) && (refreshFrequency > 1)) {
                taskPriorityRefreshData = startTask(refreshPriorityDataRunnable, 0, 1, TimeUnit.SECONDS);
            }
        }

        // Update properties
        updateMonitorProperties();
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.info("Brigde with Id='{}'' went OFFLINE", getZoneMinderId());

        // Stopping High priority thread while OFFLINE
        if (taskPriorityRefreshData != null) {
            taskPriorityRefreshData.cancel(true);
            taskPriorityRefreshData = null;
        }

        // Make sure everything gets refreshed
        for (Channel ch : getThing().getChannels()) {
            handleCommand(ch.getUID(), RefreshType.REFRESH);
        }

        // Inform thing handlers of disconnection
        for (Thing thing : getThing().getThings()) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.trace("onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }

    }

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startTask(Runnable command, long delay, long interval, TimeUnit unit) {
        ScheduledFuture<?> task = null;
        logger.debug("Starting ZoneMinder Bridge Monitor Task. Command='{}'", command.toString());
        if (interval == 0) {
            return task;
        }

        if (task == null || task.isCancelled()) {
            task = scheduler.scheduleWithFixedDelay(command, delay, interval, unit);
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

    /*
     * This is experimental
     * Try to add different properties
     */
    private void updateMonitorProperties() {
        // Update property information about this device
        Map<String, String> properties = editProperties();
        IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy();
        ZoneMinderHostVersion hostVersion = serverProxy.getHostVersion();
        ZoneMinderConfig configUseApi = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_API);
        ZoneMinderConfig configUseAuth = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_AUTH);
        ZoneMinderConfig configTrigerrs = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_TRIGGERS);

        properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_VERSION, hostVersion.getVersion());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_API_VERSION, hostVersion.getApiVersion());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_USE_API, configUseApi.getValueAsString());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_USE_AUTHENTIFICATION,
                configUseAuth.getValueAsString());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_TRIGGERS_ENABELD, configTrigerrs.getValueAsString());
        // Must loop over the new properties since we might have added data
        boolean update = false;
        Map<String, String> originalProperties = editProperties();
        for (String property : properties.keySet()) {
            if ((originalProperties.get(property) == null
                    || originalProperties.get(property).equals(properties.get(property)) == false)) {
                update = true;
                break;
            }
        }

        if (update == true) {
            logger.info("[MONITOR {}] Properties synchronised", getZoneMinderId());
            updateProperties(properties);
        }
    }
}
