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
import java.util.List;
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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.DataFetchRunnable;
import org.openhab.binding.zoneminder.internal.RefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.ZoneMinderConnection;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.ZoneMinderServerProxy;
import name.eskildsen.zoneminder.ZoneMinderSession;
import name.eskildsen.zoneminder.api.ZoneMinderDiskUsage;
import name.eskildsen.zoneminder.api.daemon.ZoneMinderHostDaemonStatus;
import name.eskildsen.zoneminder.api.host.ZoneMinderHostLoad;
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

        ZoneMinderSession zoneMinderSession = null;

        @Override
        public void run() {
            try {
                /*
                 * if (zoneMinderSession == null) {
                 * zoneMinderSession = ZoneMinderFactory.CreateSession(zoneMinderConnection);
                 * }
                 */

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

    /**
     * ZoneMinder Server session
     */
    private ZoneMinderSession zoneMinderSession = null;
    private ZoneMinderConnection zoneMinderConnection = null;

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

                    refreshThing(fetchDiskUsage, getSession());

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

                            if (thingHandler.getRefreshPriority() == RefreshPriorityEnum.HIGH_PRIORITY) {
                                logger.debug(String.format("C-> Monitor %s", thingHandler.getZoneMinderId()));
                                thingHandler.refreshThing(RefreshPriorityEnum.HIGH_PRIORITY, getSession());
                            }
                        }

                    } catch (Exception ex) {
                        logger.error("Method 'refreshThing()' for Bridge failed for thing='{}' - Exception='{}'",
                                thing.getUID(), ex.getMessage());
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

            logger.debug("ZoneMinder Server Bridge Handler Initialized");
            logger.debug("   HostName:           {}", config.getHostName());
            logger.debug("   Protocol:           {}", config.getProtocol());
            logger.debug("   Port HTTP(S)        {}", config.getHttpPort());
            logger.debug("   Port Telnet         {}", config.getTelnetPort());
            logger.debug("   Server Path         {}", config.getServerBasePath());
            logger.debug("   User:               {}", config.getUserName());
            logger.debug("   Refresh interval:   {}", config.getRefreshInterval());
            logger.debug("   Low  prio. refresh: {}", config.getRefreshIntervalLowPriorityTask());

            closeConnection();

            ZoneMinderFactory.Initialize(getBridgeConfig().getProtocol(), getBridgeConfig().getHostName(),
                    getBridgeConfig().getHttpPort(), getBridgeConfig().getTelnetPort(),
                    getBridgeConfig().getServerBasePath(), getBridgeConfig().getUserName(),
                    getBridgeConfig().getPassword());

            /*
             * zoneMinderConnection = ZoneMinderFactory.CreateConnection(getBridgeConfig().getProtocol(),
             * getBridgeConfig().getHostName(), getBridgeConfig().getHttpPort(), getBridgeConfig().getTelnetPort(),
             * getBridgeConfig().getServerBasePath(),
             * getBridgeConfig().getUserName(), getBridgeConfig().getPassword());
             */

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
        logger.debug("Stop polling of ZoneMinder Server API");

        logger.info("Stopping WatchDog task");
        stopWatchDogTask();

        logger.info("Stopping refresh data task");
        stopTask(taskRefreshData);
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
    // TODO:: REMOVED
    /*
     * protected void onRefreshLowPriorityPriorityData() {
     * logger.debug("Refreshing diskusage from ZoneMinder Server Task - '{}'", getThing().getUID());
     *
     * if (!isConnected()) {
     * logger.error("'onRefreshLowPriorityPriorityData()': Not Connected to the ZoneMinder Server!");
     * connect();
     * }
     *
     * if (isConnected()) {
     * ZoneMinderServerRequest request = new ZoneMinderServerRequest(
     * ZoneMinderRequestType.SERVER_LOW_PRIORITY_DATA, getZoneMinderId());
     * processZoneMinderServerRequest(request);
     * }
     * }
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("update " + channelUID.getAsString() + " with " + command.toString());
    }

    protected synchronized void refreshThing(boolean fetchDiskUsage, ZoneMinderSession session) {

        logger.debug("'refreshThing()': Thing='{}'!", getThing().getUID(), this.getThing().getUID());

        if (isConnected()) {
            List<Channel> channels = getThing().getChannels();
            // TOPDO:: FIX THIS ZoneMinderServerProxy zoneMinderServerProxy = ZoneMinderFactory.getServerProxy(session);
            ZoneMinderServerProxy zoneMinderServerProxy = ZoneMinderFactory.getServerProxy();
            /*
             * Fetch data for Bridge
             */
            ZoneMinderHostLoad hostLoad = zoneMinderServerProxy.getHostCpuLoad();
            channelCpuLoad = hostLoad.getCpuLoad().toString();

            if (config.getRefreshIntervalLowPriorityTask() != 0) {
                ZoneMinderDiskUsage du = zoneMinderServerProxy.getHostDiskUsage();
                channelDiskUsage = du.getDiskUsage();
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
            for (Thing thing : getThing().getThings()) {
                try {

                    if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                        Thing thingMonitor = thing;
                        ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                        thingHandler.refreshThing(RefreshPriorityEnum.SCHEDULED, session);
                    }

                } catch (Exception ex) {
                    logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                            this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                }
            }
        }
    }

    /*
     * // TODO:: ReWRITE This
     *
     * @Override
     * public void notifyZoneMinderApiDataUpdated(ThingTypeUID thingTypeUID, String zoneMinderId,
     * List<ZoneMinderData> arrData) {
     *
     * logger.error("TODO:: IMPLEMENT ME");
     * // TODO:: REMOVED
     * if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER)) {
     *
     * // Check data sets individually and update the local copy if they are there
     * // ZoneMinderServerData updatedData = (ZoneMinderServerData) data;
     * synchronized (zmServerData) {
     * for (ZoneMinderData data : arrData) {
     * String dataClassKey = data.getKey();
     * if (zmServerData.containsKey(dataClassKey)) {
     * zmServerData.remove(dataClassKey);
     * }
     * zmServerData.put(dataClassKey, data);
     * }
     * }
     * } else {
     * ZoneMinderBaseThingHandler thing = getZoneMinderThingHandlerFromZoneMinderId(thingTypeUID, zoneMinderId);
     *
     * // If thing not found, then it is not to this thing that it belongs :-)
     * if (thing != null) {
     *
     * thing.notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, arrData);
     * }
     * }
     *
     * }
     */

    /**
     * Connect The Bridge.
     *
     * @throws ZoneMinderUrlNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    protected synchronized boolean connect_NOT_USED_()
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {

        if (isConnected()) {
            onDisconnected();
        }

        openConnection();

        if (isConnected()) {
            onConnected();
        }
        return isConnected();
    }

    /**
     * Disconnect The Bridge.
     */
    private synchronized void disconnect() {

        closeConnection();

        if (!isConnected()) {
            onBridgeDisconnected(this);
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logger.debug("onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(),
                        thing.getUID(), thingHandler);
            }
        }
        /*
         * 2017.01.09 - TODO:: Instead make sure job is started
         * // Then fetch some data from ZoneMinder API....
         * boolean fetchDiskUsage = false;
         * if (config.getRefreshIntervalLowPriorityTask() != 0) {
         * fetchDiskUsage = true;
         * }
         * refreshThing(fetchDiskUsage, zoneMinderSession);
         */
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

    // TODO:: IMPLEMENT ERROR HANDLING

    @Override
    public void updateAvaliabilityStatus(ZoneMinderConnection connection) {
        ThingStatus newStatus = ThingStatus.OFFLINE;
        boolean _isAlive = false;
        ZoneMinderSession tmpSession = null;

        ThingStatus prevStatus = getThing().getStatus();

        // Igf we we are already ONLINE and have a session
        if ((prevStatus == ThingStatus.ONLINE) && (zoneMinderSession != null)) {
            tmpSession = zoneMinderSession;
        } else {
            // Temporary solution, should also implement some mechanishm to reconnect
            try {
                tmpSession = ZoneMinderFactory.CreateSession(connection);
            } catch (IllegalArgumentException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (GeneralSecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ZoneMinderUrlNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        // TODO:: Allow usage of non pooled session
        ZoneMinderServerProxy zoneMinderServerProxy = ZoneMinderFactory.getServerProxy();
        // ZoneMinderServerProxy zoneMinderServerProxy = ZoneMinderFactory.getServerProxy(tmpSession);

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

            if (tmpSession == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot access ZoneMinder Server. Check provided usercredentials");
                setBridgeConnectionStatus(false);
                return;
            }
            // TODO:: FIX THIS
            /*
             * if (tmpSession.isConnected() == false) {
             * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
             * "Cannot access ZoneMinder Server. Check provided usercredentials");
             * setBridgeConnectionStatus(false);
             * return;
             * }
             */
            if (zoneMinderServerProxy.getHostDaemonCheckState() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "ZoneMinder Server Daemon state is unknown.");
                setBridgeConnectionStatus(false);
                return;
            }

            ZoneMinderHostDaemonStatus daemonStatus = zoneMinderServerProxy.getHostDaemonCheckState();
            if (!daemonStatus.isDaemonRunning()) {
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
            }

            // TODO:: Should be improved
            _isAlive = true;
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
                        zoneMinderSession = tmpSession;
                        onConnected();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
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
                zoneMinderSession = ZoneMinderFactory.CreateSession(zoneMinderConnection);
                /*
                 * zoneMinderConnection = ZoneMinderFactory.CreateConnection(getBridgeConfig().getProtocol(),
                 * getBridgeConfig().getHostName(), getBridgeConfig().getServerBasePath(),
                 * getBridgeConfig().getUserName(), getBridgeConfig().getPassword());
                 * // HEST CREATE CONNECTION
                 * // connected = zoneMinderSession.connect();
                 * // zoneMinderServerProxy = ZoneMinderFactory.getServerProxy(zoneMinderSession);
                 */
                // TODO:: FIX THIS
                // connected = zoneMinderSession.isConnected();
                connected = true;

                logger.debug("openConnection(): Connecting to ZoneMinder Server (Telnet)");

                setConnected(true);
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
                            /*
                             * 2017.01.09 - TODO:: FIXME
                             * zoneMinderSession.SubscribeMonitorEvents(monitorHandler.getZoneMinderId(),
                             * monitorHandler);
                             */
                        }

                    } catch (Exception ex) {
                        logger.error("Method 'refreshThing()' for Bridge {} failed for thing='{}' - Exception='{}'",
                                this.getZoneMinderId(), thing.getUID(), ex.getMessage());
                    }
                }

            } /*
               * catch (UnknownHostException unknownException) {
               * logger.error("openConnection(): Unknown Host Exception: ", unknownException);
               * setConnected(false);
               * } catch (SocketException socketException) {
               * logger.error("openConnection(): Socket Exception: ", socketException);
               * setConnected(false);
               * } catch (IOException ioException) {
               * logger.error("openConnection(): IO Exception: ", ioException);
               * setConnected(false);
               * }
               */
            catch (Exception exception) {
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
            /*
             * 2017.01.09 - TODO:: FIXME
             * if (zoneMinderSession != null) {
             * zoneMinderSession.closeConnection();
             * zoneMinderSession = null;
             * // zoneMinderServerProxy = null;
             * }
             */ logger.debug("closeConnection(): Closed HTTP Connection!");

            setConnected(false);

        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - " + exception.getMessage());
        }

    }

    /*
     * private boolean processZoneMinderServerRequest(ZoneMinderServerBaseRequest request) {
     * logger.info("TODO:: FIX ME");
     * // TODO:: REMOVED
     *
     * List<ZoneMinderData> arrData = new ArrayList<ZoneMinderData>();
     * ThingTypeUID thingTypeUID = request.getThingTypeUID();
     * String zoneMinderId = request.getId();
     * Boolean doNotify = false;
     *
     * ZoneMinderMonitorProxy monitorProxy = ZoneMinderFactory.getMonitorProxy(zoneMinderSession, zoneMinderId);
     *
     * switch (request.getRequestType()) {
     *
     * case MONITOR_THING:
     * arrData.add(monitorProxy.getMonitor(zoneMinderId));
     * arrData.add(monitorProxy.getCaptureDaemonStatus());
     * arrData.add(monitorProxy.getAnalysisDaemonStatus());
     * arrData.add(monitorProxy.getFrameDaemonStatus());
     * doNotify = true;
     * break;
     *
     * case SERVER_HIGH_PRIORITY_DATA:
     * arrData.add((zoneMinderServerProxy.getHostVersion()));
     * arrData.add((zoneMinderServerProxy.getHostCpuLoad()));
     * arrData.add((zoneMinderServerProxy.getHostDaemonCheckState()));
     * doNotify = true;
     * break;
     *
     * case SERVER_LOW_PRIORITY_DATA:
     * arrData.add((zoneMinderServerProxy.getHostDiskUsage()));
     * doNotify = true;
     * break;
     *
     * case MONITOR_TRIGGER_REQUEST:
     * ZoneMinderMonitorTriggerRequest triggerRequest = (ZoneMinderMonitorTriggerRequest) request;
     * try {
     * if (triggerRequest.getActivatedState()) {
     * monitorProxy.activateForceAlarm(triggerRequest.getId(), triggerRequest.getPriority(),
     * triggerRequest.getReason(), "", "", triggerRequest.getTimeout());
     * } else {
     * monitorProxy.deactivateForceAlarm(triggerRequest.getId());
     * }
     * } catch (Exception e) {
     *
     * logger.error(e.getMessage());
     * }
     * break;
     * default:
     * logger.warn("Unhandled ZoneMinder Server request occurred (request='{}'", request.getRequestType());
     * }
     *
     * if (doNotify == true) {
     * notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, arrData);
     * }
     * return false;
     *
     * }
     */
    /*
     * public ArrayList<ZoneMinderMonitor> getMonitors() {
     * return zoneMinderServerProxy.getMonitors();
     * }
     */
    protected State getServerCpuLoadState() {

        State state = UnDefType.UNDEF;

        try {
            if (channelCpuLoad != "") {
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
            if (channelDiskUsage != "") {
                state = new DecimalType(new BigDecimal(channelDiskUsage));
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, ZoneMinderConnection conenction) {

        if (taskRefreshData == null) {
            if (config.getRefreshIntervalLowPriorityTask() != 0) {
                refreshFrequency = calculateCommonRefreshFrequency(config.getRefreshInterval());
            } else {
                refreshFrequency = config.getRefreshInterval();
            }

            taskRefreshData = startTask(refreshDataRunnable, 0, refreshFrequency, TimeUnit.SECONDS);
            // TODO:: ACTIVATE ME
            taskPriorityRefreshData = startTask(refreshPriorityDataRunnable, 0, 1, TimeUnit.SECONDS);

        }

    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.debug("'onBridgeDisconnected()' called");

        // Clear values
        channelCpuLoad = "";
        channelDiskUsage = "";

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

}
