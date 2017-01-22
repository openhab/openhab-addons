/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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

import javax.security.auth.login.FailedLoginException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.ZoneMinderMonitorProperties;
import org.openhab.binding.zoneminder.internal.DataRefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.IZoneMinderDaemonStatus;
import name.eskildsen.zoneminder.IZoneMinderDiskUsage;
import name.eskildsen.zoneminder.IZoneMinderHostLoad;
import name.eskildsen.zoneminder.IZoneMinderHostVersion;
import name.eskildsen.zoneminder.IZoneMinderMonitorData;
import name.eskildsen.zoneminder.IZoneMinderServer;
import name.eskildsen.zoneminder.IZoneMinderSession;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.api.config.ZoneMinderConfig;
import name.eskildsen.zoneminder.api.config.ZoneMinderConfigEnum;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

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
                logger.error("[WATCHDOG]: Server run(): Exception: {}", exception);
            }
        }
    };

    /**
     * Local copies of last fetched values from ZM
     */
    private String channelCpuLoad = "";
    private String channelDiskUsage = "";

    Boolean isInitialized = false;

    private IZoneMinderSession zoneMinderSession = null;
    private IZoneMinderConnectionInfo zoneMinderConnection = null;

    private ScheduledFuture<?> taskRefreshData = null;
    private ScheduledFuture<?> taskPriorityRefreshData = null;

    // private DataFetchRunnable refreshDataRunnable = new DataFetchRunnable(zoneMinderConnection) {
    private Runnable refreshDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                boolean fetchDiskUsage = false;
                /*
                 * if (isConnected()) {
                 * connect();
                 * }
                 */
                refreshCycleCount++;

                int iMaxCycles;
                boolean resetCount = false;
                boolean doRefresh = false;

                // Disk Usage is disabled
                if (getBridgeConfig().getRefreshIntervalLowPriorityTask() == 0) {
                    iMaxCycles = getBridgeConfig().getRefreshInterval();
                    resetCount = true;
                    doRefresh = true;

                } else {
                    iMaxCycles = getBridgeConfig().getRefreshIntervalLowPriorityTask() * 60;
                    doRefresh = true;
                    if ((refreshCycleCount * refreshFrequency) >= (getBridgeConfig().getRefreshIntervalLowPriorityTask()
                            * 60)) {
                        fetchDiskUsage = true;
                        resetCount = true;

                    }
                }

                logger.debug(
                        "BRIDGE: Running Refresh data task count='{}', freq='{}', max='{}', interval='{}', intervalLow='{}'",
                        refreshCycleCount, refreshFrequency, iMaxCycles, getBridgeConfig().getRefreshInterval(),
                        getBridgeConfig().getRefreshIntervalLowPriorityTask());

                if (doRefresh) {

                    if (resetCount == true) {
                        refreshCycleCount = 0;
                    }

                    logger.debug("BRIDGE: 'refreshDataRunnable()': (diskUsage='{}')", fetchDiskUsage);

                    refreshThing(zoneMinderSession, fetchDiskUsage);
                }

            } catch (Exception exception) {
                logger.error("BRIDGE: monitorRunnable::run(): Exception: {}", exception);
            }
        }
    };

    private Runnable refreshPriorityDataRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                // Make sure priority updates is done
                for (Thing thing : getThing().getThings()) {
                    try {

                        if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                            Thing thingMonitor = thing;
                            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                            if (thingHandler.getRefreshPriority() == DataRefreshPriorityEnum.HIGH_PRIORITY) {
                                logger.debug(String.format("MONITOR-%s: RefreshPriority is High Priority",
                                        thingHandler.getZoneMinderId()));
                                thingHandler.refreshThing(zoneMinderSession, DataRefreshPriorityEnum.HIGH_PRIORITY);
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
            logger.info("BRIDGE: ZoneMinder Server Bridge Handler Initialized");
            logger.debug("BRIDGE:    HostName:           {}", getBridgeConfig().getHostName());
            logger.debug("BRIDGE:    Protocol:           {}", getBridgeConfig().getProtocol());
            logger.debug("BRIDGE:    Port HTTP(S)        {}", getBridgeConfig().getHttpPort());
            logger.debug("BRIDGE:    Port Telnet         {}", getBridgeConfig().getTelnetPort());
            logger.debug("BRIDGE:    Server Path         {}", getBridgeConfig().getServerBasePath());
            logger.debug("BRIDGE:    User:               {}", getBridgeConfig().getUserName());
            logger.debug("BRIDGE:    Refresh interval:   {}", getBridgeConfig().getRefreshInterval());
            logger.debug("BRIDGE:    Low  prio. refresh: {}", getBridgeConfig().getRefreshIntervalLowPriorityTask());

            closeConnection();

            zoneMinderConnection = ZoneMinderFactory.CreateConnection(getBridgeConfig().getProtocol(),
                    getBridgeConfig().getHostName(), getBridgeConfig().getHttpPort(), getBridgeConfig().getTelnetPort(),
                    getBridgeConfig().getServerBasePath(), getBridgeConfig().getUserName(),
                    getBridgeConfig().getPassword(), 3000);

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
        try {
            logger.debug("BRIDGE: - Stop polling of ZoneMinder Server API");

            logger.info("BRIDGE: - Stopping WatchDog task");
            stopWatchDogTask();

            logger.info("BRIDGE: - Stopping refresh data task");
            stopTask(taskRefreshData);
        } catch (Exception ex) {
        }
    }

    protected String getThingId() {
        return getThing().getUID().getId();
    }

    @Override
    public String getZoneMinderId() {

        return getThing().getUID().getAsString();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        ThingUID s1 = getThing().getUID();
        ThingTypeUID s2 = getThing().getThingTypeUID();
        logger.debug("BRIDGE [{}]: Channel '{}' was linked to '{}'", getThingId(), channelUID.getAsString(),
                this.thing.getThingTypeUID());
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        logger.debug("BRIDGE [{}]: Channel '{}' was unlinked from '{}'", getThingId(), channelUID.getAsString(),
                this.thing.getThingTypeUID());
    }

    protected ArrayList<IZoneMinderMonitorData> getMonitors(IZoneMinderSession session) {

        if (isConnected()) {
            return ZoneMinderFactory.getServerProxy(session).getMonitors();
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
        logger.debug("BRIDGE [{}]: Update '{}' with '{}'", getThingId(), channelUID.getAsString(), command.toString());
    }

    protected synchronized void refreshThing(IZoneMinderSession session, boolean fetchDiskUsage) {

        logger.debug("BRIDGE [{}]: 'refreshThing()': Thing='{}'!", getThingId(), this.getThing().getUID());

        List<Channel> channels = getThing().getChannels();
        List<Thing> things = getThing().getThings();

        IZoneMinderServer zoneMinderServerProxy = ZoneMinderFactory.getServerProxy(session);
        if (zoneMinderServerProxy == null) {
            logger.warn("BRIDGE [{}]:  Could not obtain ZonerMinderServerProxy ", getThingId());

            // Make sure old data is cleared
            channelCpuLoad = "";
            channelDiskUsage = "";

        } else if (isConnected()) {
            /*
             * Fetch data for Bridge
             */
            IZoneMinderHostLoad hostLoad = null;
            try {
                hostLoad = zoneMinderServerProxy.getHostCpuLoad();

            } catch (FailedLoginException | ZoneMinderUrlNotFoundException | IOException ex) {
                logger.error("BRIDGE [{}]: Exceptioon thrown in call to ZoneMinderHostLoad ('{}')", getThingId(), ex);
            }

            if (hostLoad == null) {
                logger.warn("BRIDGE [{}]: ZoneMinderHostLoad dataset could not be obtained (received 'null')",
                        getThingId());
            } else if (hostLoad.getHttpResponseCode() != 200) {
                logger.warn(
                        "BRIDGE [{}]: ZoneMinderHostLoad dataset could not be obtained (HTTP Response: Code='{}', Message='{}')",
                        getThingId(), hostLoad.getHttpResponseCode(), hostLoad.getHttpResponseMessage());

            } else {
                channelCpuLoad = hostLoad.getCpuLoad().toString();
            }

            if (fetchDiskUsage) {
                IZoneMinderDiskUsage diskUsage = null;
                try {
                    diskUsage = zoneMinderServerProxy.getHostDiskUsage();
                } catch (FailedLoginException | ZoneMinderUrlNotFoundException | IOException ex) {
                    logger.error("BRIDGE [{}]: Exceptioon thrown in call to ZoneMinderDiskUsage ('{}')", getThingId(),
                            ex);
                }

                if (diskUsage == null) {
                    logger.warn("BRIDGE [{}]: ZoneMinderDiskUsage dataset could not be obtained (received 'null')",
                            getThingId());
                } else if (hostLoad.getHttpResponseCode() != 200) {
                    logger.warn(
                            "BRIDGE [{}]: ZoneMinderDiskUsage dataset could not be obtained (HTTP Response: Code='{}', Message='{}')",
                            getThingId(), hostLoad.getHttpResponseCode(), hostLoad.getHttpResponseMessage());

                } else {
                    channelDiskUsage = diskUsage.getDiskUsage();
                }
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

                    thingHandler.refreshThing(session, DataRefreshPriorityEnum.SCHEDULED);
                }

            } catch (NullPointerException ex) {
                // TODO:: This isn't critical (unless it comes over and over). There seems to be a bug so that a null
                // pointer exception is coming every now and then.
                // HAve to find the reason for that. Until thenm, don't Spamm
                logger.debug(
                        "BRIDGE [{}]: Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                        getThingId(), this.getZoneMinderId(), thing.getUID(), ex.getMessage());

                // Other exceptions has to be shown as errors
            } catch (Exception ex) {
                logger.error(
                        "BRIDGE [{}]: Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                        getThingId(), this.getZoneMinderId(), thing.getUID(), ex.getMessage());
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
            if (connected) {
                try {
                    zoneMinderSession = ZoneMinderFactory.CreateSession(zoneMinderConnection);
                } catch (FailedLoginException | IllegalArgumentException | IOException
                        | ZoneMinderUrlNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                zoneMinderSession = null;
            }
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
                    logger.error("BRIDGE [{}]: onConnected() failed - Exceprion: {}", getThingId(), e.getMessage());
                }
                logger.debug("BRIDGE [{}]: onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", getThingId(),
                        thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Runs when disconnected.
     */
    private void onDisconnected() {
        logger.debug("BRIDGE [{}]: onDisconnected(): Bridge Disconnected!", getThingId());
        setConnected(false);
        onBridgeDisconnected(this);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.debug("BRIDGE [{}]: onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", getThingId(),
                        thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }
    }

    @Override
    public void updateAvaliabilityStatus(IZoneMinderConnectionInfo connection) {
        ThingStatus newStatus = ThingStatus.OFFLINE;
        boolean _isAlive = false;

        ThingStatus prevStatus = getThing().getStatus();

        try {
            // Just perform a health check to see if we are still conencted
            if (prevStatus == ThingStatus.ONLINE) {
                if (zoneMinderSession == null) {
                    return;
                } else if (!zoneMinderSession.isConnected()) {
                    return;
                }

                IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(zoneMinderSession);
                IZoneMinderDaemonStatus daemonStatus = serverProxy.getHostDaemonCheckState();

                // If service isn't running OR we revceived a http responsecode other than 200, assume we are offline
                if ((!daemonStatus.getStatus()) || (daemonStatus.getHttpResponseCode() != 200)) {
                    newStatus = ThingStatus.OFFLINE;
                    return;
                }

                // TODO:: Tjek other things without being harsh

                newStatus = ThingStatus.ONLINE;
            }
            // If we are OFFLINE, check everything
            else if (prevStatus == ThingStatus.OFFLINE) {

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

                if (!isZoneMinderLoginValid(connection)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot access ZoneMinder Server. Check provided usercredentials");
                    setBridgeConnectionStatus(false);
                    return;
                }
                //
                IZoneMinderSession curSession = null;
                try {
                    curSession = ZoneMinderFactory.CreateSession(connection);
                } catch (FailedLoginException | IllegalArgumentException | IOException
                        | ZoneMinderUrlNotFoundException ex) {
                    logger.error("BRIDGE: Create Session failed with exception {}", ex.getMessage());
                }
                IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(curSession);

                // Check if server API can be accessed
                // if (!isZoneMinderApiEnabled(connection)) {
                if (!serverProxy.isApiEnabled()) {

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "ZoneMinder Server 'OPT_USE_API' not enabled");
                    setBridgeConnectionStatus(false);
                    return;
                    // } else if (!isZoneMinderServerDaemonRunning(connection)) {
                } else if (!serverProxy.getHostDaemonCheckState().getStatus()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "ZoneMinder Server Daemon is not running");
                    setBridgeConnectionStatus(false);
                    return;
                }
                // Verify that 'OPT_TRIGGER' is set to true in ZoneMinder
                // else if (!isZoneMinderExternalTriggerEnabled(connection)) {
                else if (!serverProxy.isTriggerOptionEnabled()) {

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "ZoneMinder Server option 'OPT_TRIGGERS' not enabled");
                    setBridgeConnectionStatus(false);
                    return;
                } else {
                    _isAlive = true;
                }

                if (_isAlive == true) {
                    zoneMinderSession = curSession;
                    _alive = _isAlive;
                    newStatus = ThingStatus.ONLINE;
                } else {
                    zoneMinderSession = null;
                    _alive = _isAlive;
                    newStatus = ThingStatus.OFFLINE;
                }
            }
            // Status changed
            if (thing.getStatus() != newStatus)

            {
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
        for (Thing thing :

        getThing().getThings()) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
            if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                try {
                    thingHandler.updateAvaliabilityStatus(connection);
                } catch (Exception ex) {
                    logger.debug("BRIDGE [{}]: Failed to call 'updateAvailabilityStatus()' for '{}'", getThingId(),
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

    protected boolean isZoneMinderLoginValid(IZoneMinderConnectionInfo connection) {
        try {
            return ZoneMinderFactory.validateLogin(connection);
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public void updateChannel(ChannelUID channel) {
        State state = null;
        try {

            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_ONLINE:
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
                logger.debug("BRIDGE [{}]: BridgeHandler.updateChannel(): Updating channel '{}' to state='{}'",
                        getThingId(), channel.getId(), state.toString());
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {

            logger.error("BRIDGE [{}]: Error when 'updateChannel()' was called for thing='{}' (Exception='{}'",
                    getThingId(), channel.getId(), ex.getMessage());

        }
    }

    protected boolean openConnection() {
        boolean connected = false;
        if (isConnected() == false) {
            logger.debug("BRIDGE [{}]: Connecting Bridge to ZoneMinder Server", getThingId());

            try {
                closeConnection();

                connected = false;

                setConnected(connected);

                logger.info("BRIDGE [{}]: Connecting to ZoneMinder Server (result='{}'", getThingId(), connected);

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

                                logger.info("BRIDGE [{}]: Subscribe to events from ZoneMinder Server for monitor '{}'",
                                        getThingId(), monitorHandler.getZoneMinderId());

                                // TODO FIX ME
                                /*
                                 * ZoneMinderFactory.SubscribeMonitorEvents(monitorHandler.getZoneMinderId(),
                                 * monitorHandler);
                                 */
                            }

                        } catch (Exception ex) {
                            logger.error(
                                    "BRIDGE [{}]: Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                                    getThingId(), this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                        }
                    }
                }

            } catch (Exception exception) {
                logger.error("BRIDGE [{}]: openConnection(): Exception: ", getThingId(), exception);
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
            logger.debug("BRIDGE [{}]: closeConnection(): Closed HTTP Connection!", getThingId());
            setConnected(false);

        } catch (Exception exception) {
            logger.error("BRIDGE [{}]: closeConnection(): Error closing connection - {}", getThingId(),
                    exception.getMessage());
        }

    }

    protected State getServerCpuLoadState() {

        State state = UnDefType.UNDEF;

        try {
            if ((channelCpuLoad != "") && (isConnected())) {
                state = new DecimalType(new BigDecimal(channelCpuLoad));
            }

        } catch (Exception ex) {
            // Deliberately kept as debug info!
            logger.debug("BRIDGE [{}]: Exception='{}'", getThingId(), ex.getMessage());
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
            // Deliberately kept as debug info!
            logger.debug("BRIDGE [{}]: Exception {}", getThingId(), ex.getMessage());
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionInfo connection) {
        logger.info("BRIDGE [{}]: Brigde went ONLINE", getThingId());

        if (taskRefreshData == null) {

            if (getBridgeConfig().getRefreshIntervalLowPriorityTask() != 0) {
                refreshFrequency = calculateCommonRefreshFrequency(getBridgeConfig().getRefreshInterval());
            } else {
                refreshFrequency = getBridgeConfig().getRefreshInterval();
            }
            logger.info("BRIDGE [{}]: Calculated refresh inetrval to '{}'", getThingId(), refreshFrequency);

            if (taskRefreshData != null) {
                taskRefreshData.cancel(true);
                taskRefreshData = null;
            }

            taskRefreshData = startTask(refreshDataRunnable, 0, refreshFrequency, TimeUnit.SECONDS);

            if (taskPriorityRefreshData != null) {
                taskPriorityRefreshData.cancel(true);
                taskPriorityRefreshData = null;
            }

            // Only start if it is not running and Priority Frequency is higher than ordinary
            if ((taskPriorityRefreshData == null) && (refreshFrequency > 1)) {
                taskPriorityRefreshData = startTask(refreshPriorityDataRunnable, 0, 1, TimeUnit.SECONDS);
            }
        }

        // Update properties
        updateMonitorProperties(zoneMinderSession);
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.info("BRIDGE [{}]: Brigde went OFFLINE", getThingId());

        // Stopping refresh thread while OFFLINE
        if (taskRefreshData != null) {
            taskRefreshData.cancel(true);
            taskRefreshData = null;
            logger.debug("BRIDGE [{}]: Stopping DataRefresh task", getThingId());
        }

        // Stopping High priority thread while OFFLINE
        if (taskPriorityRefreshData != null) {
            taskPriorityRefreshData.cancel(true);
            taskPriorityRefreshData = null;
            logger.debug("BRIDGE [{}]: Stopping Priority DataRefresh task", getThingId());
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
                logger.debug("BRIDGE [{}]: onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", getThingId(),
                        thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }

    }

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startTask(Runnable command, long delay, long interval, TimeUnit unit) {
        ScheduledFuture<?> task = null;
        logger.debug("BRIDGE [{}]: Starting ZoneMinder Bridge Monitor Task. Command='{}'", getThingId(),
                command.toString());
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
        try {
            logger.debug("BRIDGE [{}]: Stopping ZoneMinder Bridge Monitor Task. Task='{}'", getThingId(),
                    task.toString());
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
            }
        } catch (Exception ex) {
        }

    }

    /*
     * This is experimental
     * Try to add different properties
     */
    private void updateMonitorProperties(IZoneMinderSession session) {
        // Update property information about this device
        Map<String, String> properties = editProperties();
        IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(session);

        IZoneMinderHostVersion hostVersion = null;
        try {
            hostVersion = serverProxy.getHostVersion();

            ZoneMinderConfig configUseApi = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_API);
            ZoneMinderConfig configUseAuth = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_AUTH);
            ZoneMinderConfig configTrigerrs = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_TRIGGERS);

            properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_VERSION, hostVersion.getVersion());
            properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_API_VERSION, hostVersion.getApiVersion());
            properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_USE_API, configUseApi.getValueAsString());
            properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_USE_AUTHENTIFICATION,
                    configUseAuth.getValueAsString());
            properties.put(ZoneMinderMonitorProperties.PROPERTY_SERVER_TRIGGERS_ENABELD,
                    configTrigerrs.getValueAsString());
        } catch (FailedLoginException | ZoneMinderUrlNotFoundException | IOException e) {
            // Don't care here
        }

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
            logger.info("BRIDGE [{}]: Properties synchronised", getThingId());
            updateProperties(properties);
        }
    }
}
