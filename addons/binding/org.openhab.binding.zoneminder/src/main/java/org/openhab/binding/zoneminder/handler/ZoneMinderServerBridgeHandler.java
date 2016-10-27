/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.api.ConfigData;
import org.openhab.binding.zoneminder.internal.api.ConfigEnum;
import org.openhab.binding.zoneminder.internal.api.MonitorDaemonStatus;
import org.openhab.binding.zoneminder.internal.api.MonitorData;
import org.openhab.binding.zoneminder.internal.api.ServerCpuLoad;
import org.openhab.binding.zoneminder.internal.api.ServerData;
import org.openhab.binding.zoneminder.internal.api.ServerDiskUsage;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderOutgoingRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpMonitorRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpRequest;
import org.openhab.binding.zoneminder.internal.command.http.ZoneMinderHttpServerRequest;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.openhab.binding.zoneminder.internal.connection.ZoneMinderHttpProxy;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderData;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderMonitorData;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Handler for a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen
 *
 */
public class ZoneMinderServerBridgeHandler extends ZoneMinderBaseBridgeHandler
        implements ZoneMinderMonitorEventListener {

    public static final int TELNET_TIMEOUT = 5000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER);

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Bridge configuration from OpenHAB
     */
    ZoneMinderBridgeServerConfig config = null;

    Boolean isInitialized = false;
    /**
     * ZoneMinder HTTP connection
     */
    private ZoneMinderHttpProxy zoneMinderServerProxy = null;

    /**
     * ZoneMinder Telnet Connection
     */
    private Socket tcpSocket = null;
    private PrintWriter tcpOutput = null;
    private BufferedReader tcpInput = null;

    private ZoneMinderServerData zoneMinderServerData = null;

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
     * @param bridge
     *            Bridge object representing a ZoneMinder Server
     */
    public ZoneMinderServerBridgeHandler(Bridge bridge) {
        super(bridge, ZoneMinderThingType.ZoneMinderServerBridge);

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
            updateStatus(ThingStatus.INITIALIZING);

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

            taskHighPriorityRefresh = startTask(refreshHighPriorityDataRunnable, config.getRefreshInterval(),
                    TimeUnit.SECONDS);
            taskLowPriorityRefresh = startTask(refreshLowPriorityDataRunnable,
                    config.getRefreshIntervalLowPriorityTask(), TimeUnit.MINUTES);

        } catch (Exception ex) {
            logger.error("'ZoneMinderServerBridgeHandler' failed to initialize. Exception='{}'", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
        } finally {
            startWatchDogTask();
            isInitialized = true;
        }
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

    protected ZoneMinderServerData getServerData() {
        return zoneMinderServerData;
    }

    /**
     * Method for polling the ZoneMinder Server.
     */
    @Override
    protected void onRefreshLowPriorityPriorityData() {
        logger.debug("Refreshing diskusage from ZoneMinder Server Task - '{}'", getThing().getUID());

        if (!isConnected()) {
            logger.error("Not Connected to the ZoneMinder Server!");
            connect();
        }

        if (isConnected()) {
            ZoneMinderHttpRequest request = new ZoneMinderHttpServerRequest(
                    ZoneMinderRequestType.SERVER_LOW_PRIORITY_DATA, getZoneMinderId());
            sendZoneMinderHttpRequest(request);

        }
    }

    @Override
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

    @Override
    protected void refreshThing() {

        logger.debug("refreshThing(): Thing='{}'!", getThing().getUID(), this.getThing().getUID());
        List<Thing> things = getThing().getThings();
        List<Channel> channels = getThing().getChannels();

        if (isConnected()) {
            /*
             * Fetch data for all monitors attached to this bridge
             */

            ZoneMinderHttpRequest request = new ZoneMinderHttpServerRequest(
                    ZoneMinderRequestType.SERVER_HIGH_PRIORITY_DATA, getZoneMinderId());
            sendZoneMinderHttpRequest(request);

            for (Thing thing : things) {
                try {
                    if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                        Thing thingMonitor = thing;
                        ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                        request = new ZoneMinderHttpMonitorRequest(ZoneMinderRequestType.MONITOR_THING,
                                thingHandler.getZoneMinderId());
                        sendZoneMinderHttpRequest(request);
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
    public void notifyZoneMinderApiDataUpdated(ThingTypeUID thingTypeUID, String zoneMinderId, ZoneMinderData data) {

        if (thingTypeUID.equals(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER)) {

            // Check data sets individually and update the local copy if they are there
            ZoneMinderServerData updatedData = (ZoneMinderServerData) data;

            if (zoneMinderServerData == null) {
                zoneMinderServerData = new ZoneMinderServerData(null, null, null);
            }

            if (updatedData.getServerVersionData() != null) {
                zoneMinderServerData.setServerVersionData(updatedData.getServerVersionData());
            }

            if (updatedData.getServerCpuLoadData() != null) {
                zoneMinderServerData.setServerCpuLoadData(updatedData.getServerCpuLoadData());
            }
            if (updatedData.getServerDiskUsageData() != null) {
                zoneMinderServerData.setServerDiskUsageData(updatedData.getServerDiskUsageData());
            }

        } else {
            ZoneMinderBaseThingHandler thing = getZoneMinderThingHandlerFromZoneMinderId(thingTypeUID, zoneMinderId);

            // If thing not found, then it is not to this thing that it belongs :-)
            if (thing != null) {
                thing.notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, data);
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
            return;
        }

        // Check if we have a connection to ZoneMinder Server - else try to establish one
        if (zoneMinderServerProxy == null) {
            if (openConnection() == false) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot access ZoneMinder Server. Check provided usercredentials");
                return;
            }
        }

        if (!zoneMinderServerProxy.getIsConnected()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot access ZoneMinder Server. Check provided usercredentials");
            return;
        }

        if (!zoneMinderServerProxy.getServerDaemonCheckState()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "ZoneMinder Server Daemon isn't running.");
            return;
        }
        // Set Status to OFFLINE if it is OFFLINE

        // Check if server API can be accessed
        if (!isZoneMinderApiEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ZoneMinder ServerAPI isn't enabled. In ZoneMinder enabled ZM_xxx");
            return;
        }

        if (!isZoneMinderExternalTriggerEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "ZoneMinder Server External trigger isn't enabled. In ZoneMinder enabled ZM_xxx");
            return;
        }

        // 4. Check server version
        // 5. Check server API version

        // Check if refresh jobs is running
        if (!isZoneMinderServerDaemonEnabled()) {

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "ZoneMinder Server cannot be reached. Daemon appears to be stopped.");
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

        /*
         * // Ask all things to update their Availability Status
         * for (Thing thing : getThing().getThings()) {
         * ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
         * if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
         * try {
         * thingHandler.updateAvaliabilityStatus();
         * } catch (Exception ex) {
         * logger.debug("Failed to call 'updateAvailabilityStatus()' for '{}'",
         * thingHandler.getThing().getUID());
         * }
         * }
         *
         * }
         */
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

        /*
         * JSON Return upon Error
         * {
         * "success": false,
         * "data": {
         * "name": "API Disabled",
         * "message": "API Disabled",
         * "url": "\/zm\/api\/configs\/view\/ZM_OPT_USE_API.json",
         * "exception": {
         * "class": "UnauthorizedException",
         * "code": 401,
         * "message": "API Disabled",
         * "trace": [
         * "#0 [internal function]: AppController->beforeFilter(Object(CakeEvent))",
         * "#1 \/usr\/share\/zoneminder\/www\/api\/lib\/Cake\/Event\/CakeEventManager.php(243): call_user_func(Array, Object(CakeEvent))"
         * ,
         * "#2 \/usr\/share\/zoneminder\/www\/api\/lib\/Cake\/Controller\/Controller.php(677): CakeEventManager->dispatch(Object(CakeEvent))"
         * ,
         * "#3 \/usr\/share\/zoneminder\/www\/api\/lib\/Cake\/Routing\/Dispatcher.php(189): Controller->startupProcess()"
         * ,
         * "#4 \/usr\/share\/zoneminder\/www\/api\/lib\/Cake\/Routing\/Dispatcher.php(167): Dispatcher->_invoke(Object(ConfigsController), Object(CakeRequest))"
         * ,
         * "#5 \/usr\/share\/zoneminder\/www\/api\/app\/webroot\/index.php(108): Dispatcher->dispatch(Object(CakeRequest), Object(CakeResponse))"
         * ,
         * "#6 {main}"
         * ]
         * },
         * "queryLog": {
         * "default": {
         * "log": [
         * {
         * "query":
         * "SELECT `Config`.`Id`, `Config`.`Name`, `Config`.`Value`, `Config`.`Type`, `Config`.`DefaultValue`, `Config`.`Hint`, `Config`.`Pattern`, `Config`.`Format`, `Config`.`Prompt`, `Config`.`Help`, `Config`.`Category`, `Config`.`Readonly`, `Config`.`Requires` FROM `zm`.`Config` AS `Config`   WHERE `Config`.`Name` = 'ZM_OPT_USE_API'    LIMIT 1"
         * ,
         * "params": [],
         * "affected": 1,
         * "numRows": 1,
         * "took": 0
         * },
         * {
         * "query":
         * "SELECT `Config`.`Id`, `Config`.`Name`, `Config`.`Value`, `Config`.`Type`, `Config`.`DefaultValue`, `Config`.`Hint`, `Config`.`Pattern`, `Config`.`Format`, `Config`.`Prompt`, `Config`.`Help`, `Config`.`Category`, `Config`.`Readonly`, `Config`.`Requires` FROM `zm`.`Config` AS `Config`   WHERE `Config`.`Name` = 'ZM_OPT_USE_API'    LIMIT 1"
         * ,
         * "params": [],
         * "affected": 1,
         * "numRows": 1,
         * "took": 0
         * }
         * ],
         * "count": 2,
         * "time": 0
         * }
         * }
         * }
         * }
         */
        ConfigData cfg = zoneMinderServerProxy.getConfig(ConfigEnum.ZM_OPT_USE_API);
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
        if (getServerData() == null) {
            logger.warn("updateChannel(): ServerData not fetched for Thing='{}', Channel='{}'",
                    getThing().getBridgeUID(), channel);
            return;
        }
        try {
            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                    super.updateChannel(channel);
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
                    // Ask super class to handle
                    super.updateChannel(channel);
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

    @Override
    Boolean openConnection() {
        boolean connected = false;
        if (isConnected() == false) {
            logger.debug("Connecting Bridge to ZoneMinder Server.");

            try {
                closeConnection();

                // Initialize HTTP Server proxy
                zoneMinderServerProxy = new ZoneMinderHttpProxy(getBridgeConfig().getProtocol(),
                        getBridgeConfig().getHostName(), getBridgeConfig().getServerBasePath(),
                        getBridgeConfig().getUserName(), getBridgeConfig().getPassword());

                connected = zoneMinderServerProxy.connect();

                logger.debug("openConnection(): Connecting to ZoneMinder Server (Telnet)");

                tcpSocket = new Socket();
                SocketAddress TPIsocketAddress = new InetSocketAddress(config.getHostName(), config.getTelnetPort());
                tcpSocket.connect(TPIsocketAddress, TELNET_TIMEOUT);
                tcpInput = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                tcpOutput = new PrintWriter(tcpSocket.getOutputStream(), true);

                Thread tcpListener = new Thread(new TCPListener());
                tcpListener.start();

                setBridgeConnection(connected);

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

    @Override
    synchronized void closeConnection() {
        try {
            if (zoneMinderServerProxy != null) {
                zoneMinderServerProxy.close();
                zoneMinderServerProxy = null;
            }
            logger.debug("closeConnection(): Closed HTTP Connection!");

            if (tcpSocket != null) {
                tcpSocket.close();
                tcpSocket = null;
            }
            if (tcpInput != null) {
                tcpInput.close();
                tcpInput = null;
            }
            if (tcpOutput != null) {
                tcpOutput.close();
                tcpOutput = null;
            }
            logger.debug("closeConnection(): Closed TCP Connection!");
            setBridgeConnection(false);

        } catch (IOException ioException) {
            logger.error("closeConnection(): Unable to close connection - " + ioException.getMessage());
        } catch (Exception exception) {
            logger.error("closeConnection(): Error closing connection - " + exception.getMessage());
        }

    }

    /**
     *
     **/
    protected void writeTCP(String writeString) {
        try {
            tcpOutput.write(writeString);
            tcpOutput.flush();
            logger.debug("writeTCP(): Message Sent: {}", writeString);
        } catch (Exception exception) {
            logger.error("writeTCP(): Unable to write to socket: {} ", exception);
            setBridgeConnection(false);
        }
    }

    /**
     *
     **/

    protected String readTCP() {
        String message = "";

        try {

            message = tcpInput.readLine();
            logger.debug("readTCP(): Message Received: {}", message);
        } catch (SocketTimeoutException stException) {
            // Just ignore this

        } catch (IOException ioException) {
            logger.error("readTCP(): IO Exception: ", ioException);
            setBridgeConnection(false);
        } catch (Exception exception) {
            logger.error("readTCP(): Exception: ", exception);
            setBridgeConnection(false);
        }

        return message;
    }

    /**
     * TCPMessageListener: Receives Socket messages from the ZoneMinder API.
     */
    private class TCPListener implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(TCPListener.class);

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            String messageLine;

            try {

                // Allow other things to get started before running
                Thread.sleep(1000);
                while (isConnected()) {
                    if ((messageLine = readTCP()) != null) {
                        handleIncomingTelnetMessage(messageLine);
                    } else {
                        setBridgeConnection(false);
                    }
                }
            } catch (Exception e) {
                logger.error("TCPListener(): Unable to read message: ", e);
                closeConnection();
            }
        }
    }

    @Override
    public boolean sendZoneMinderHttpRequest(ZoneMinderHttpRequest request) {
        ZoneMinderData data = null;
        ThingTypeUID thingTypeUID = request.getThingTypeUID();
        String zoneMinderId = request.getId();
        switch (request.getRequestType()) {

            case MONITOR_THING:
                ZoneMinderHttpMonitorRequest monitorRequest = (ZoneMinderHttpMonitorRequest) request;
                MonitorData monitorData = zoneMinderServerProxy.getMonitor(zoneMinderId);
                MonitorDaemonStatus captureDaemonStatus = zoneMinderServerProxy
                        .getMonitorCaptureDaemonStatus(zoneMinderId);
                MonitorDaemonStatus analysisDaemonStatus = zoneMinderServerProxy
                        .getMonitorAnalysisDaemonStatus(zoneMinderId);
                MonitorDaemonStatus frameDaemonStatus = zoneMinderServerProxy.getMonitorFrameDaemonStatus(zoneMinderId);
                data = new ZoneMinderMonitorData(monitorData, captureDaemonStatus, analysisDaemonStatus,
                        frameDaemonStatus);
                break;
            case SERVER_HIGH_PRIORITY_DATA:
                ZoneMinderHttpServerRequest serverRequest = (ZoneMinderHttpServerRequest) request;
                ServerData serverData = zoneMinderServerProxy.getServerData();
                ServerCpuLoad serverCpuLoad = zoneMinderServerProxy.getServerCpuLoad();
                data = new ZoneMinderServerData(serverData, null, serverCpuLoad);
                break;
            case SERVER_LOW_PRIORITY_DATA:
                ZoneMinderHttpServerRequest serverDiskUsageRequest = (ZoneMinderHttpServerRequest) request;
                ServerDiskUsage serverDiskUsage = zoneMinderServerProxy.getServerDiskUsage();
                data = new ZoneMinderServerData(null, serverDiskUsage, null);
                break;
            default:
                logger.warn("Unhandled HTTP request occurred (request='{}'", request.getRequestType());
        }

        notifyZoneMinderApiDataUpdated(thingTypeUID, zoneMinderId, data);
        return false;
    }

    @Override
    protected boolean onHandleZoneMinderTelnetRequest(ZoneMinderRequestType requestType,
            ZoneMinderOutgoingRequest request) {
        boolean result = false;

        switch (requestType) {
            case MONITOR_TRIGGER:
                logger.debug("[TCP] Writing command '{}' to ZoneMinder Server", request.toCommandString());
                writeTCP(request.toCommandString());
                result = true;
                break;

            default:
                result = false;
        }
        return result;
    }

    public ArrayList<MonitorData> getMonitors() {
        return zoneMinderServerProxy.getMonitors();
    }

    protected State getServerVersionState() {

        return new StringType(getServerData().getServerVersion());
    }

    protected State getServerVersionApiState() {

        return new StringType(getServerData().getServerVersionApi());
    }

    protected State getServerCpuLoadState() {

        try {

            return new DecimalType(getServerData().getServerCpuLoad());
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return null;
    }

    protected State getServerDiskUsageState() {
        try {
            if (getServerData().getServerDiskUsageData() != null) {
                return new StringType(getServerData().getServerDiskUsageData().getDiskUsage());
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage());

        }

        return new StringType("");

    }

    @Override
    public Boolean isOnline() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean isRunning() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onBridgeConnected(ZoneMinderBaseBridgeHandler bridge) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBridgeDisconnected(ZoneMinderBaseBridgeHandler bridge) {
        zoneMinderServerProxy.close();

    }
}
