/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.ZoneMinderProperties;
import org.openhab.binding.zoneminder.internal.RefreshPriority;
import org.openhab.binding.zoneminder.internal.ZoneMinderConnectionStatus;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderBridgeServerConfig;
import org.openhab.binding.zoneminder.internal.discovery.ZoneMinderDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.IZoneMinderConnectionHandler;
import name.eskildsen.zoneminder.IZoneMinderEventSession;
import name.eskildsen.zoneminder.IZoneMinderServer;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.common.ZoneMinderConfigEnum;
import name.eskildsen.zoneminder.data.IMonitorDataGeneral;
import name.eskildsen.zoneminder.data.IZoneMinderDaemonStatus;
import name.eskildsen.zoneminder.data.IZoneMinderDiskUsage;
import name.eskildsen.zoneminder.data.IZoneMinderHostLoad;
import name.eskildsen.zoneminder.data.IZoneMinderHostVersion;
import name.eskildsen.zoneminder.data.ZoneMinderConfig;
import name.eskildsen.zoneminder.exception.ZoneMinderApiNotEnabledException;
import name.eskildsen.zoneminder.exception.ZoneMinderAuthenticationException;
import name.eskildsen.zoneminder.exception.ZoneMinderException;
import name.eskildsen.zoneminder.exception.ZoneMinderGeneralException;
import name.eskildsen.zoneminder.exception.ZoneMinderInvalidData;
import name.eskildsen.zoneminder.exception.ZoneMinderResponseException;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;

/**
 * Handler for a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen - Initial contribution
 *
 */
public class ZoneMinderServerBridgeHandler extends BaseBridgeHandler implements IZoneMinderHandler {

    public static final int TELNET_TIMEOUT = 5000;
    static final int HTTP_TIMEOUT = 5000;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER);

    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    private ZoneMinderConnectionStatus zmConnectStatus = ZoneMinderConnectionStatus.UNINITIALIZED;
    private ZoneMinderConnectionStatus lastSucceededStatus = ZoneMinderConnectionStatus.UNINITIALIZED;

    private RefreshPriority forcedPriority = RefreshPriority.UNKNOWN;

    private ZoneMinderDiscoveryService discoveryService;
    private ServiceRegistration discoveryRegistration;

    private ScheduledFuture<?> taskWatchDog;
    private Integer refreshCycleCount = 0;

    /** Connection status for the bridge. */
    private boolean connected = false;

    private Runnable watchDogRunnable = new Runnable() {
        private int watchDogCount = 0;

        @Override
        public void run() {
            try {
                updateAvaliabilityStatus(getZoneMinderConnection());

                // Only Discover if Bridge is online
                if (thing.getStatusInfo().getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                // Check if autodiscovery is enabled
                boolean bAutoDiscover = getBridgeConfig().getAutodiscoverThings();

                if ((discoveryService != null) && bAutoDiscover) {
                    watchDogCount++;
                    // Run every two minutes
                    if ((watchDogCount % 8) == 0) {
                        discoveryService.startBackgroundDiscovery();
                        watchDogCount = 0;
                    }
                }

            } catch (Exception exception) {
                StackTraceElement ste = exception.getStackTrace()[0];
                logger.error("[WATCHDOG]: Server run(): StackTrace: File='{}', Line='{}', Method='{}'",
                        ste.getFileName(), ste.getLineNumber(), ste.getMethodName(), exception);
            }
        }
    };

    /**
     * Local copies of last fetched values from ZM
     */
    private String channelCpuLoad = "";
    private String channelDiskUsage = "";

    private boolean initialized = false;

    private IZoneMinderEventSession zoneMinderEventSession;
    private IZoneMinderConnectionHandler zoneMinderConnection;

    private ScheduledFuture<?> taskRefreshData = null;

    private IZoneMinderConnectionHandler getZoneMinderConnection() {
        return zoneMinderConnection;
    }

    private Runnable refreshDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!isConnected()) {
                    return;
                }
                refreshCycleCount++;
                int intervalBatch = 3600;
                int intervalLow = getBridgeConfig().getRefreshIntervalLow();
                int intervalNormal = getBridgeConfig().getRefreshIntervalNormal();
                int intervalHigh = 5;

                RefreshPriority cyclePriority = RefreshPriority.PRIORITY_ALARM;

                // boolean isBatch = ((refreshCycleCount % intervalBatch) == 0);
                boolean isLow = ((refreshCycleCount % intervalLow) == 0);
                boolean isNormal = ((refreshCycleCount % intervalNormal) == 0);
                boolean isHigh = ((refreshCycleCount % intervalHigh) == 0);

                if (isLow) {
                    cyclePriority = RefreshPriority.PRIORITY_BATCH;
                } else if (isLow) {
                    cyclePriority = RefreshPriority.PRIORITY_LOW;
                } else if (isNormal) {
                    cyclePriority = RefreshPriority.PRIORITY_NORMAL;
                } else if (isHigh) {
                    cyclePriority = RefreshPriority.PRIORITY_HIGH;
                }

                refreshThing(cyclePriority);

                if ((refreshCycleCount >= intervalLow) && (refreshCycleCount >= intervalNormal)
                        && (refreshCycleCount >= intervalHigh) && (refreshCycleCount >= intervalBatch)) {
                    refreshCycleCount = 0;
                }

            } catch (Exception exception) {
                logger.error("{}: monitorRunnable::run()", getLogIdentifier(), exception);
            }
        }
    };

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a ZoneMinder Server
     */
    public ZoneMinderServerBridgeHandler(Bridge bridge) {
        super(bridge);

        logger.info("{}: context='constructor' Starting ZoneMinder Server Bridge Handler (Bridge='{}')",
                getLogIdentifier(), bridge.getBridgeUID());
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        try {
            zoneMinderConnection = null;

            taskRefreshData = null;

            updateStatus(ThingStatus.OFFLINE);

            ZoneMinderBridgeServerConfig config = getBridgeConfig();
            logger.info("{}: ZoneMinder Server Bridge Handler Initialized", getLogIdentifier());
            logger.debug("{}:    HostName:           {}", getLogIdentifier(), config.getHost());
            logger.debug("{}:    Protocol:           {}", getLogIdentifier(), config.getProtocol());
            logger.debug("{}:    Port HTTP(S)        {}", getLogIdentifier(), config.getHttpPort());
            logger.debug("{}:    Port Telnet         {}", getLogIdentifier(), config.getTelnetPort());
            logger.debug("{}:    Portal Path         {}", getLogIdentifier(), config.getServerBasePath());
            logger.debug("{}:    API Path            {}", getLogIdentifier(), config.getServerApiPath());
            logger.debug("{}:    Refresh interval:   {}", getLogIdentifier(), config.getRefreshIntervalNormal());
            logger.debug("{}:    Low  prio. refresh: {}", getLogIdentifier(), config.getRefreshIntervalLow());
            logger.debug("{}:    Autodiscovery:      {}", getLogIdentifier(), config.getAutodiscoverThings());

            startWatchDogTask();
            initialized = true;
            return;
        } catch (Exception ex) {
            if (zoneMinderConnection == null) {
                logger.error(
                        "{}: 'ZoneMinderServerBridgeHandler' general configuration error occurred. Failed to initialize.",
                        getLogIdentifier(), ex);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                logger.error("{}: 'ZoneMinderServerBridgeHandler' failed to initialize", getLogIdentifier(), ex);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
            }

        }

        initialized = false;

    }

    protected IZoneMinderConnectionHandler aquireSession() {
        return zoneMinderConnection;
    }

    protected void releaseSession() {
    }

    protected void startWatchDogTask() {
        taskWatchDog = startTask(watchDogRunnable, 5, 1, TimeUnit.SECONDS);
    }

    protected void stopWatchDogTask() {
        stopTask(taskWatchDog);
        taskWatchDog = null;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        try {
            setConnected(false);
            zoneMinderConnection = null;
            setConnectionStatus(ZoneMinderConnectionStatus.UNINITIALIZED);
        } catch (IllegalArgumentException | GeneralSecurityException | IOException | ZoneMinderUrlNotFoundException e) {
            logger.error("{}: context='handleConfigurationUpdate'", getLogIdentifier(), e.getCause());
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        // Inform thing handlers of connection
    }

    /**
     */
    @Override
    public void dispose() {
        logger.debug("{}:  context='dispose' Stop polling of ZoneMinder Server API", getLogIdentifier());

        logger.info("{}: context='dispose' Stopping Discovery service", getLogIdentifier());
        // Remove the discovery service
        if (discoveryService != null) {
            discoveryService.deactivate();
            discoveryService = null;
        }

        if (discoveryRegistration != null) {
            discoveryRegistration.unregister();
            discoveryRegistration = null;
        }

        logger.info("{}: context='dispose' Stopping WatchDog task", getLogIdentifier());
        stopWatchDogTask();

        logger.info("{}: context='dispose' Stopping refresh data task", getLogIdentifier());
        stopTask(taskRefreshData);
    }

    protected String getThingId() {
        return getThing().getUID().getId();
    }

    @Override
    public String getZoneMinderId() {
        return getThing().getUID().getAsString();
    }

    protected ArrayList<IMonitorDataGeneral> getMonitors(IZoneMinderConnectionHandler session)
            throws ZoneMinderAuthenticationException {
        if (isConnected()) {
            try {
                return ZoneMinderFactory.getServerProxy(session).getMonitors();
            } catch (ZoneMinderGeneralException | ZoneMinderResponseException | ZoneMinderInvalidData ex) {
                logger.error("{}: context='getMonitors' Exception {}", getLogIdentifier(), ex.getMessage(),
                        ex.getCause());

            }
        }
        return new ArrayList<>();

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
        logger.debug("{}: Update '{}' with '{}'", getLogIdentifier(), channelUID.getAsString(), command.toString());
    }

    protected /* synchronized */ void refreshThing(RefreshPriority refresh) {
        IZoneMinderServer zoneMinderServerProxy = null;
        RefreshPriority curPriority = RefreshPriority.DISABLED;

        // logger.debug("{}: 'refreshThing()': Thing='{}'!", getLogIdentifier(), this.getThing().getUID());

        List<Channel> channels = getThing().getChannels();
        List<Thing> things = getThing().getThings();

        try {
            if (forcedPriority == RefreshPriority.UNKNOWN) {
                return;
            } else if (forcedPriority == RefreshPriority.DISABLED) {
                curPriority = refresh;
            } else {
                curPriority = forcedPriority;
                forcedPriority = RefreshPriority.DISABLED;
            }

            zoneMinderServerProxy = ZoneMinderFactory.getServerProxy(aquireSession());
            if (zoneMinderServerProxy == null) {
                logger.warn("{}:  Could not obtain ZonerMinderServerProxy ", getLogIdentifier());

                // Make sure old data is cleared
                channelCpuLoad = "";
                channelDiskUsage = "";

            } else if (isConnected()) {
                /*
                 * Fetch data for Bridge
                 */
                if (curPriority.isPriorityActive(RefreshPriority.PRIORITY_NORMAL)) {
                    IZoneMinderHostLoad hostLoad = null;
                    try {
                        hostLoad = zoneMinderServerProxy.getHostCpuLoad();
                        logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                                hostLoad.getHttpRequestUrl(), hostLoad.getHttpStatus(),
                                hostLoad.getHttpResponseMessage());

                    } catch (ZoneMinderUrlNotFoundException | IOException ex) {
                        logger.error("{}: Exception thrown in call to ZoneMinderHostLoad ('{}')", getLogIdentifier(),
                                ex.getMessage());
                    } catch (ZoneMinderException ex) {
                        logger.error(
                                "{}: context='refreshThing' error in call to 'getHostCpuLoad' ExceptionClass='{}' - Message='{}'",
                                getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(), ex.getCause());
                    }

                    if (hostLoad == null) {
                        logger.warn("{}: ZoneMinderHostLoad dataset could not be obtained (received 'null')",
                                getLogIdentifier());
                    } else if (hostLoad.getHttpStatus() != HttpStatus.OK_200) {
                        logger.warn(
                                "{}: ZoneMinderHostLoad dataset could not be obtained (HTTP Response: Code='{}', Message='{}')",
                                getLogIdentifier(), hostLoad.getHttpStatus(), hostLoad.getHttpResponseMessage());

                    } else {
                        channelCpuLoad = hostLoad.getCpuLoad().toString();
                    }

                    if (curPriority.isPriorityActive(getBridgeConfig().getDiskUsageRefresh())) {
                        IZoneMinderDiskUsage diskUsage = null;
                        try {
                            diskUsage = zoneMinderServerProxy.getHostDiskUsage();
                            logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                                    diskUsage.getHttpRequestUrl(), diskUsage.getHttpStatus(),
                                    diskUsage.getHttpResponseMessage());
                        } catch (Exception ex) {
                            logger.error(
                                    "{}: context='refreshThing' Exception thrown in call to ZoneMinderDiskUsage ('{}')",
                                    getLogIdentifier(), ex.getMessage());
                        } catch (ZoneMinderException ex) {
                            logger.error(
                                    "{}: context='refreshThing' error in call to 'getHostDiskUsage' ExceptionClass='{}' - Message='{}'",
                                    getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(),
                                    ex.getCause());
                        }

                        if (diskUsage == null) {
                            logger.warn(
                                    "{}: context='refreshThing' ZoneMinderDiskUsage dataset could not be obtained (received 'null')",
                                    getLogIdentifier());
                        } else if (diskUsage.getHttpStatus() != HttpStatus.OK_200) {
                            logger.warn(
                                    "{}: context='refreshThing' ZoneMinderDiskUsage dataset could not be obtained (HTTP Response: Code='{}', Message='{}')",
                                    getLogIdentifier(), diskUsage.getHttpStatus(), diskUsage.getHttpResponseMessage());

                        } else {
                            channelDiskUsage = diskUsage.getDiskUsage();
                        }
                    }
                }
            } else {
                // Make sure old data is cleared
                channelCpuLoad = "";
                channelDiskUsage = "";
            }
        } catch (Exception ex) {
            logger.error("{}: context='refreshThing' tag='exception' Exception thrown when refreshing bridge='{}'",
                    getLogIdentifier(), getThing().getBridgeUID(), ex);
        } finally {
            if (zoneMinderServerProxy != null) {
                releaseSession();
            }

        }

        /*
         * Update all channels on Bridge
         */
        for (Channel channel : channels) {

            if (isLinked(channel.getUID().getId())) {
                updateChannel(channel.getUID());
            }
        }

        /*
         * Request Things attached to Bridge to refresh
         */
        for (Thing thing : things) {
            try {
                if (thing.getThingTypeUID().equals(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR)) {
                    ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

                    if (thingHandler != null) {
                        thingHandler.refreshThing(curPriority);
                    }
                }
            } catch (Exception ex) {
                logger.error("{}: context='refreshThing' tag='exception' Exception thrown when refreshing thing='{}'",
                        getLogIdentifier(), thing.getUID(), ex.getCause());
            }

        }

    }

    /**
     * Returns connection status.
     */
    public synchronized Boolean isConnected() {
        return connected;
    }

    public boolean isOnline() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return (statusInfo.getStatus() == ThingStatus.ONLINE) ? true : false;
    }

    /**
     * Set connection status.
     *
     * @param connected
     * @throws ZoneMinderUrlNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    private void setConnected(boolean connected)
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {
        if (this.connected != connected) {
            try {
                if (connected) {
                    try {
                        if (zoneMinderEventSession == null) {
                            zoneMinderEventSession = ZoneMinderFactory.CreateEventSession(zoneMinderConnection);
                        }

                    } catch (Exception ex) {
                        zoneMinderEventSession = null;
                        return;
                    }

                } else {
                    if (zoneMinderEventSession != null) {
                        zoneMinderEventSession.unsubscribeAllMonitorEvents();
                    }
                    zoneMinderConnection = null;
                    zoneMinderEventSession = null;
                }
            } finally {
                this.connected = connected;
                if (connected) {
                    onConnected();
                } else {
                    onDisconnected();
                }
            }
        }
    }

    /**
     * Runs when connection established.
     *
     * @throws ZoneMinderUrlNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    public void onConnected()
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {
        logger.debug("{}: [{}]: onConnected(): Bridge Connected!", getLogIdentifier(), getThingId());
        onBridgeConnected(this, getZoneMinderConnection());

        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                try {
                    thingHandler.onBridgeConnected(this, getZoneMinderConnection());
                } catch (IllegalArgumentException e) {
                    logger.error("{}: context='onConnected' failed - Exceprion: {}", getLogIdentifier(),
                            e.getMessage());
                }
                logger.debug("{}: context='onConnected': Bridge - {}, Thing - {}, Thing Handler - {}",
                        getLogIdentifier(), thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }

    }

    /**
     * Runs when disconnected.
     */
    private void onDisconnected() {
        logger.debug("{}: context='onDisconnected': Bridge Disconnected!", getLogIdentifier());

        onBridgeDisconnected(this);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.debug("{}: context='onDisconnected': Bridge - {}, Thing - {}, Thing Handler - {}",
                        getLogIdentifier(), thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }
    }

    private int initRetriesCount = 0;
    private static int initMaxRevoverableRetries = 5;
    private static int initMaxUnrecoverableRetries = 5;

    private ZoneMinderConnectionStatus verifyBindingConfiguration(ThingStatus currentStatus) {
        String context = "verifyBindingConfiguration";
        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";

        ZoneMinderConnectionStatus status = ZoneMinderConnectionStatus.BINDING_CONFIG_INVALID;
        ZoneMinderBridgeServerConfig config = null;
        try {
            // Is it a retry loop? (Is this step already verified?)
            // Or is there an unrecoverable error?
            if ((getLastSucceededStatus().greatherThanEqual(ZoneMinderConnectionStatus.BINDING_CONFIG_LOAD_PASSED))
                    || (getLastSucceededStatus().hasUnrecoverableError())) {
                return getLastSucceededStatus();
            }

            // get Bridge Config
            config = getBridgeConfig();

            // Check if server Bridge configuration is valid
            if (config == null) {
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "Configuration not found";
                updateStatus(newStatus, statusDetail, statusDescription);

                logger.error("{}: context='{}' state='{}' check='FAILED' - {}", getLogIdentifier(), context,
                        newStatus.toString(), statusDescription);

                return status;

            } else if (config.getHost() == null) {
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "Host not configured";

                updateStatus(newStatus, statusDetail, statusDescription);

                logger.error("{}: context='{}' state='{}' check='FAILED' - {}", getLogIdentifier(), context,
                        newStatus.toString(), statusDescription);

                return status;

            } else if (config.getProtocol() == null) {
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "Unknown protocol in configuration";

                updateStatus(newStatus, statusDetail, statusDescription);

                logger.error("{}: context='{}' state='{}' check='FAILED' - {}", getLogIdentifier(), context,
                        newStatus.toString(), statusDescription);
                return status;
            }

            else if (config.getHttpPort() == null) {
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "HTTP port invalid in configuration";

                updateStatus(newStatus, statusDetail, statusDescription);

                logger.error("{}: context='{}' state='{}' check='FAILED' - {}", getLogIdentifier(), context,
                        newStatus.toString(), statusDescription);
                return status;

            }

            else if (config.getTelnetPort() == null) {
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "Telnet port invalid in configuration";
                updateStatus(newStatus, statusDetail, statusDescription);
                logger.error("{}: context='{}' state='{}' check='FAILED' - {}", getLogIdentifier(), context,
                        newStatus.toString(), statusDescription);
                return status;

            }

            // Configuration verified
            status = ZoneMinderConnectionStatus.BINDING_CONFIG_LOAD_PASSED;

            logger.debug("{}: context='{}' state='{}' check='PASSED'", getLogIdentifier(), context,
                    newStatus.toString());

        } catch (Exception ex) {
            logger.error("{}: context='{}' state='{}' check='FAILED' tag='exception'", getLogIdentifier(), context,
                    newStatus.toString(), ex);

        }
        return status;
    }

    private ZoneMinderConnectionStatus validateConfig(ZoneMinderBridgeServerConfig config) {
        String context = "validateConfig";
        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";

        ZoneMinderConnectionStatus status = ZoneMinderConnectionStatus.GENERAL_ERROR;

        // Is it a retry loop? (Is this step already verified?)
        // Or is there an unrecoverable error?
        if ((getLastSucceededStatus().greatherThanEqual(ZoneMinderConnectionStatus.BINDING_CONFIG_VALIDATE_PASSED))
                || (getLastSucceededStatus().hasUnrecoverableError())) {
            return getLastSucceededStatus();
        }

        // Check if something is responding give host and port HTTP
        try {
            // Check Socket
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(config.getHost(), config.getHttpPort()), 5000);
            if (socket.isConnected()) {
                socket.close();
            }

            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.NONE;
            statusDescription = "Connecting to ZoneMinder Server";
            updateStatus(newStatus, statusDetail, statusDescription);

            status = ZoneMinderConnectionStatus.BINDING_CONFIG_VALIDATE_PASSED;
            logger.debug("{}: context='{}' previousState='OFFLINE' Socket connection to ZM Website (PASSED)",
                    getLogIdentifier(), context);

        } catch (UnknownHostException e) {
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "Unknown host - Check configuration";
            status = ZoneMinderConnectionStatus.BINDING_CONNECTION_INVALID;
            logger.warn(
                    "{}: context='{}' tag='exception' previousState='OFFLINE' UnknowHostException when connecting to ZoneMinder Server.",
                    getLogIdentifier(), context, e);
        } catch (IOException e) {
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "Could not contact host - Check configuration";
            updateStatus(newStatus, statusDetail, statusDescription);

            status = ZoneMinderConnectionStatus.BINDING_CONNECTION_TIMEOUT;
            logger.warn(
                    "{}: context='validateConfig' tag='exception' previousState='OFFLINE' Socket connection Timeout.",
                    getLogIdentifier(), e);
        }
        return status;
    }

    private ZoneMinderConnectionStatus getLastSucceededStatus() {
        return lastSucceededStatus;
    }

    private ZoneMinderConnectionStatus getConnectionStatus() {
        return zmConnectStatus;
    }

    private void setConnectionStatus(ZoneMinderConnectionStatus newStatus) {
        zmConnectStatus = newStatus;
        if (!newStatus.isErrorState()) {
            lastSucceededStatus = newStatus;
        }
    }

    private ZoneMinderConnectionStatus validateConnection(ZoneMinderBridgeServerConfig config) {
        String context = "validateConnection";
        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";

        ZoneMinderConnectionStatus status = ZoneMinderConnectionStatus.BINDING_CONNECTION_INVALID;

        // Is it a retry loop? (Is this step already verified?)
        // Or is there an unrecoverable error?
        if ((getLastSucceededStatus().greatherThanEqual(ZoneMinderConnectionStatus.ZONEMINDER_CONNECTION_CREATED))
                || (getLastSucceededStatus().hasUnrecoverableError())) {
            return getLastSucceededStatus();
        }

        try {
            aquireSession();

            if (getZoneMinderConnection() == null) {
                zoneMinderConnection = ZoneMinderFactory.CreateConnection(config.getProtocol(), config.getHost(),
                        config.getHttpPort(), config.getTelnetPort(), config.getUserName(), config.getPassword(),
                        config.getStreamingUser(), config.getStreamingPassword(), config.getServerBasePath(),
                        config.getServerApiPath(), HTTP_TIMEOUT);

                logger.debug(
                        "{}: context='{}' - ZoneMinderFactory.CreateConnection() called (Protocol='{}', Host='{}', HttpPort='{}', SocketPort='{}', Path='{}', API='{}')",
                        getLogIdentifier(), context, config.getProtocol(), config.getHost(), config.getHttpPort(),
                        config.getTelnetPort(), config.getServerBasePath(), config.getServerApiPath());

            }

        } catch (ZoneMinderAuthenticationException authenticationException) {
            String detailedMessage = "";
            setConnectionStatus(ZoneMinderConnectionStatus.BINDING_CONFIG_INVALID);

            if (authenticationException.getStackTrace() != null) {
                if (authenticationException.getStackTrace().length > 0) {
                    StackTraceElement ste = authenticationException.getStackTrace()[0];
                    detailedMessage = String.format(" StackTrace='%s'", ste.toString());
                }

            }
            logger.error(
                    "{}: context='{}' check='FAILED' - Failed to login to ZoneMinder Server.  Check provided usercredentials (Exception='{}', {})",
                    getLogIdentifier(), context, authenticationException.getMessage(), detailedMessage,
                    authenticationException);
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "Login to ZoneMinder Server failed. Check provided usercredentials";
            updateStatus(newStatus, statusDetail, statusDescription);
            status = ZoneMinderConnectionStatus.SERVER_CREDENTIALS_INVALID;
            return status;

        } catch (ZoneMinderApiNotEnabledException e) {
            setConnectionStatus(ZoneMinderConnectionStatus.SERVER_API_DISABLED);
            logger.error(
                    "{}: context='{}' check='FAILED' - ZoneMinder Server API is not enabled. Enable option in ZoneMinder Server Settings and restart openHAB Binding.",
                    getLogIdentifier(), context, e);
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "ZoneMinder Server 'OPT_USE_API' not enabled";
            updateStatus(newStatus, statusDetail, statusDescription);
            status = ZoneMinderConnectionStatus.SERVER_API_DISABLED;
            return status;

        } catch (ZoneMinderException | Exception e) {
            logger.error(
                    "{}: context='{}' check='FAILED' - General error when creating ConnectionInfo. Retrying next cycle...",
                    getLogIdentifier(), context, e);
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "ZoneMinder Server Connection error";
            updateStatus(newStatus, statusDetail, statusDescription);
            status = ZoneMinderConnectionStatus.GENERAL_ERROR;
            return status;

        } finally {
            releaseSession();
        }

        // Check that we have a connection
        if (getZoneMinderConnection() != null) {
            logger.debug("{}: context='{}' previousState='OFFLINE' ZoneMinder Connection check (PASSED)",
                    getLogIdentifier(), context);
            status = ZoneMinderConnectionStatus.ZONEMINDER_CONNECTION_CREATED;
        } else {
            zoneMinderConnection = null;
            status = ZoneMinderConnectionStatus.BINDING_CONNECTION_INVALID;
            logger.warn("{}: context='{}' check='FAILED' - Failed to obtain ZoneMinder Connection. Retrying next cycle",
                    getLogIdentifier(), context);
        }

        return status;
    }

    public ZoneMinderConnectionStatus validateZoneMinderServerConfig() {
        IZoneMinderConnectionHandler curSession = null;
        String context = "validateZoneMinderServerConfig";
        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";
        ZoneMinderConnectionStatus status = ZoneMinderConnectionStatus.GENERAL_ERROR;

        IZoneMinderServer serverProxy = null;
        try {
            curSession = aquireSession();
            if (curSession == null) {
                logger.error(
                        "{}: context='{}' check='FAILED' - Could not verify ZoneMinder Server Config. Session failed to connect.",
                        getLogIdentifier(), context);

                status = ZoneMinderConnectionStatus.GENERAL_ERROR;
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "Session not available";
                updateStatus(newStatus, statusDetail, statusDescription);
                return status;
            }

            serverProxy = ZoneMinderFactory.getServerProxy(curSession);

            IZoneMinderDaemonStatus daemonStatus = serverProxy.getHostDaemonCheckState();

            // Check if server API can be accessed
            if (!daemonStatus.getStatus()) {
                logger.error("{}: context='{}' check='FAILED' - Bridge OFFLINE because server Daemon is not running",
                        getLogIdentifier(), context);
                status = ZoneMinderConnectionStatus.SERVER_DAEMON_NOT_RUNNING;
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "ZoneMinder Server Daemon not running";
                updateStatus(newStatus, statusDetail, statusDescription);

            }

            // Verify that 'OPT_TRIGGER' is set to true in ZoneMinder
            else if (!serverProxy.isTriggerOptionEnabled()) {
                logger.error(
                        "{}: context='{}' check='FAILED' - Bridge OFFLINE because ZoneMinder Server option 'OPT_TRIGGERS' not enabled",
                        getLogIdentifier(), context);
                status = ZoneMinderConnectionStatus.SERVER_OPT_TRIGGERS_DISABLED;
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                statusDescription = "ZoneMinder Server option 'OPT_TRIGGERS' not enabled";
                updateStatus(newStatus, statusDetail, statusDescription);

            } else {
                // Seems like everything is as we want it :-)
                logger.debug("{}: context='{}' check='PASSED' - ZoneMinder ", getLogIdentifier(), context);
                status = ZoneMinderConnectionStatus.ZONEMINDER_SERVER_CONFIG_PASSED;

            }

        } catch (ZoneMinderException | Exception ex) {
            if (ex.getMessage() != null) {
                logger.error(
                        "{}: context='{}' check='FAILED' - ZoneMinder Server configuration failed to verify. (Exception='{}')",
                        getLogIdentifier(), context, ex.getMessage(), ex.getCause());
            } else {
                logger.error("{}: context='{}' check='FAILED' - ZoneMinder Server configuration failed to verify.",
                        getLogIdentifier(), context, ex.getCause());
            }
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
            statusDescription = "ZoneMinder Server configuration not verified ";
            updateStatus(newStatus, statusDetail, statusDescription);

        } finally {
            releaseSession();
        }
        return status;
    }

    private void initializeAvaliabilityStatus(IZoneMinderConnectionHandler conn) {
        String context = "initializeAvaliabilityStatus";
        ZoneMinderBridgeServerConfig config = null;

        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";
        ThingStatus currentStatus = getThing().getStatus();

        // Only continue if handler is initialized and status is OFFLINE
        if ((currentStatus != ThingStatus.OFFLINE) || (!initialized)) {
            return;
        }

        /********************************
         *
         * Retry handling
         *
         *******************************/
        // An unrecoverable error in the Binding Configuration was found. OR a recoverable has failed and is no
        // unrecoverable (try again later)
        if (getConnectionStatus().hasUnrecoverableError() || (initRetriesCount > initMaxRevoverableRetries)) {
            initRetriesCount++;

            // Reset unrecoverable error and try from beginning
            if (initRetriesCount > initMaxRevoverableRetries + initMaxUnrecoverableRetries) {
                initRetriesCount = 0;
                zoneMinderConnection = null;
                zoneMinderEventSession = null;
                setConnectionStatus(ZoneMinderConnectionStatus.UNINITIALIZED);

                // Clear old error information
                newStatus = ThingStatus.OFFLINE;
                statusDetail = ThingStatusDetail.NONE;
                statusDescription = "Retrying to connect";
                updateStatus(newStatus, statusDetail, statusDescription);

                logger.debug("{}: context='{}' state='{}' - Retrying initialization after unrecoverable error",
                        getLogIdentifier(), context, newStatus.toString());

            }
            return;

        } else if (getConnectionStatus().hasRecoverableError()) {
            initRetriesCount++;
            logger.debug("{}: context='{}' state='{}' - Retrying initialization (Last Error='{}', Retries='{}')",
                    getLogIdentifier(), context, newStatus.toString(), getConnectionStatus().toString(),
                    initRetriesCount);

        }

        /***********************************
         *
         * Verify Binding Configuration
         *
         ***********************************/
        setConnectionStatus(verifyBindingConfiguration(currentStatus));
        if (!getConnectionStatus().hasPassed(ZoneMinderConnectionStatus.BINDING_CONFIG_LOAD_PASSED)) {
            return;
        }

        // Great the Bridge Config is valid get started
        config = getBridgeConfig();

        /***********************************
         *
         * Validate Binding Configuration
         *
         ***********************************/
        setConnectionStatus(validateConfig(config));

        // Check that Status corresponds actual state
        if (!getConnectionStatus().hasPassed(ZoneMinderConnectionStatus.BINDING_CONFIG_VALIDATE_PASSED)) {
            return;
        }

        /***********************************
         *
         * VALIDATE ZONEMINDER CONNECTION (API + AUTHENTICATION)
         *
         ***********************************/
        setConnectionStatus(validateConnection(config));

        // A previous step failed.
        if (!getConnectionStatus().hasPassed(ZoneMinderConnectionStatus.ZONEMINDER_CONNECTION_CREATED)) {
            return;
        }

        /***********************************
         *
         * VALIDATE ZONEMINDER HTTP Session
         *
         ***********************************/
        zmConnectStatus = ZoneMinderConnectionStatus.ZONEMINDER_SESSION_CREATED;
        // A previous step failed.
        if (!getConnectionStatus().hasPassed(ZoneMinderConnectionStatus.ZONEMINDER_SESSION_CREATED)) {
            return;
        }

        /***********************************
         *
         * VALIDATE ZONEMINDER HTTP Session
         *
         ***********************************/
        setConnectionStatus(validateZoneMinderServerConfig());
        if (!getConnectionStatus().hasPassed(ZoneMinderConnectionStatus.ZONEMINDER_SERVER_CONFIG_PASSED)) {
            return;
        }

        /***********************************
         *
         * Everything looks fine -> GO ONLINE
         *
         ***********************************/

        zmConnectStatus = ZoneMinderConnectionStatus.INITIALIZED;
        // _zoneMinderSession = curSession;
        newStatus = ThingStatus.ONLINE;
        statusDetail = ThingStatusDetail.NONE;
        statusDescription = "";

        updateBridgeStatus(newStatus, statusDetail, statusDescription, true);
        logger.debug("{}:  context='{}' Successfully established session to ZoneMinder Server.", getLogIdentifier(),
                context);

    }

    @Override
    public void updateAvaliabilityStatus(IZoneMinderConnectionHandler conn) {
        String context = "updateAvaliabilityStatus";
        IZoneMinderConnectionHandler curSession = null;
        ThingStatus newStatus = ThingStatus.OFFLINE;
        ThingStatusDetail statusDetail = ThingStatusDetail.NONE;
        String statusDescription = "";

        ThingStatus prevStatus = getThing().getStatus();
        try {
            // Just perform a health check to see if we are still connected
            if (prevStatus == ThingStatus.ONLINE) {
                try {
                    curSession = aquireSession();

                    if (curSession == null) {
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
                        statusDescription = "Session lost connection to ZoneMinder Server";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        return;
                    } else if (!curSession.isConnected()) {
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
                        statusDescription = "Session lost connection to ZoneMinder Server";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        return;
                    }

                    else if (!curSession.isAuthenticated()) {
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
                        statusDescription = "Not authenticated";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        return;
                    }

                    IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(curSession);
                    IZoneMinderHostVersion hostVersion = null;
                    try {
                        hostVersion = serverProxy.getHostVersion();
                    } catch (ZoneMinderException ex) {
                        hostVersion = null;
                    }

                    if ((hostVersion == null) || (hostVersion.getHttpStatus() >= 400)) {
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
                        statusDescription = "Connection to ZoneMinder Server was lost";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);

                        logger.error("{}: Lost connection to ZoneMinder server.", getLogIdentifier());

                        setConnected(false);
                    }

                    // Check if ZoneMinder Server Daemon is running
                    if (!serverProxy.isDaemonRunning()) {
                        logger.error("{}: context='{}' Bridge OFFLINE because ZoneMinder Server Daemon stopped",
                                getLogIdentifier(), context);
                        zmConnectStatus = ZoneMinderConnectionStatus.SERVER_DAEMON_NOT_RUNNING;
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                        statusDescription = "ZoneMinder Server Daemon stopped";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        // setConnected(false);
                        return;
                    }
                    // Verify that 'OPT_TRIGGER' is set to true in ZoneMinder
                    else if (!serverProxy.isTriggerOptionEnabled()) {
                        logger.error(
                                "{}: context='{}' Bridge OFFLINE because ZoneMinder Server OPT_TRIGGER was disabled",
                                getLogIdentifier(), context);
                        zmConnectStatus = ZoneMinderConnectionStatus.SERVER_OPT_TRIGGERS_DISABLED;
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                        statusDescription = "Option external triggers 'OPT_TRIGGERS' was disabled";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        return;
                    }
                    // Check if ZoneMinder Server API can be accessed
                    else if (!serverProxy.isApiEnabled()) {
                        logger.error("{}: context='{}' Bridge OFFLINE because ZoneMinder Server API was disabled",
                                getLogIdentifier(), context);
                        zmConnectStatus = ZoneMinderConnectionStatus.SERVER_API_DISABLED;
                        newStatus = ThingStatus.OFFLINE;
                        statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                        statusDescription = "ZoneMinder Server API was disabled";
                        updateBridgeStatus(newStatus, statusDetail, statusDescription, false);
                        return;
                    }

                } finally {
                    releaseSession();
                }

                newStatus = ThingStatus.ONLINE;
                statusDetail = ThingStatusDetail.NONE;
                statusDescription = "";
                updateBridgeStatus(newStatus, statusDetail, statusDescription, true);

                // Ask all child things to update their Availability Status
                for (Thing thing : getThing().getThings()) {
                    ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
                    if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                        try {
                            thingHandler.updateAvaliabilityStatus(getZoneMinderConnection());
                        } catch (Exception ex) {
                            logger.debug("{}: Failed to call 'updateAvailabilityStatus()' for '{}'", getLogIdentifier(),
                                    thingHandler.getThing().getUID());
                        }
                    }
                }

            } else if (prevStatus == ThingStatus.OFFLINE) {
                initializeAvaliabilityStatus(conn);

                if (getConnectionStatus() == ZoneMinderConnectionStatus.INITIALIZED) {
                    newStatus = ThingStatus.ONLINE;
                    statusDetail = ThingStatusDetail.NONE;
                    statusDescription = "";
                    updateBridgeStatus(newStatus, statusDetail, statusDescription, true);
                }
            }
        } catch (Exception ex) {
            // Hmmm We shouldn't really end here.
            logger.error(
                    "{}: context='updateAvailablilityStatus' Exception occurred in updateAvailabilityStatus Exception='{}'",
                    getLogIdentifier(), ex.getMessage(), ex.getCause());
            zmConnectStatus = ZoneMinderConnectionStatus.GENERAL_ERROR;
            newStatus = ThingStatus.OFFLINE;
            statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
            statusDescription = "General error occurred (Check log)";
            updateBridgeStatus(newStatus, statusDetail, statusDescription, true);
        }

    }

    protected void updateBridgeStatus(ThingStatus newStatus, ThingStatusDetail statusDetail, String statusDescription,
            boolean updateConnection) {
        ThingStatusInfo curStatusInfo = thing.getStatusInfo();
        String curDescription = StringUtils.isBlank(curStatusInfo.getDescription()) ? ""
                : curStatusInfo.getDescription();

        // Status changed
        if ((curStatusInfo.getStatus() != newStatus) || (curStatusInfo.getStatusDetail() != statusDetail)
                || (!curDescription.equals(statusDescription))) {
            if (!curStatusInfo.getStatus().equals(newStatus)) {
                logger.info("{}: context='updateBridgeStatus' Bridge status changed from '{}' to '{}'",
                        getLogIdentifier(), thing.getStatus(), newStatus);
            }

            // Update Status correspondingly
            if ((newStatus == ThingStatus.OFFLINE) && (statusDetail != ThingStatusDetail.NONE)) {
                updateStatus(newStatus, statusDetail, statusDescription);

                forcedPriority = RefreshPriority.UNKNOWN;
                if (updateConnection) {
                    try {
                        setConnected(false);
                    } catch (IllegalArgumentException | GeneralSecurityException | IOException
                            | ZoneMinderUrlNotFoundException e) {
                        logger.error(
                                "{}: context='updateBridgeStatus' Exception occurred when changing connected status",
                                getLogIdentifier(), e);
                    }
                }
            } else {
                updateStatus(newStatus);
                forcedPriority = RefreshPriority.PRIORITY_BATCH;
                if (updateConnection) {
                    try {
                        setConnected(true);
                    } catch (IllegalArgumentException | GeneralSecurityException | IOException
                            | ZoneMinderUrlNotFoundException e) {
                        logger.error(
                                "{}: context='updateBridgeStatus' Exception occurred when changing connected status",
                                getLogIdentifier(), e);
                    }
                }
            }

            // Ask all child things to update their Availability Status, since Bridge has changed
            for (Thing thing : getThing().getThings()) {
                ZoneMinderBaseThingHandler thingHandler = (ZoneMinderBaseThingHandler) thing.getHandler();
                if (thingHandler instanceof ZoneMinderThingMonitorHandler) {
                    try {
                        thingHandler.updateAvaliabilityStatus(getZoneMinderConnection());
                    } catch (Exception ex) {
                        logger.debug(
                                "{}:  context='updateBridgeStatus' Failed to call 'updateAvailabilityStatus' for '{}' (Exception='{}')",
                                getLogIdentifier(), thingHandler.getThing().getUID(), ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void updateChannel(ChannelUID channel) {
        State state = null;
        try {
            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_ONLINE:
                    updateState(channel, (isOnline() ? OnOffType.ON : OnOffType.OFF));
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_DISKUSAGE:
                    if (getBridgeConfig().getDiskUsageRefresh() != RefreshPriority.DISABLED) {
                        state = getServerDiskUsageState();
                    } else {
                        state = UnDefType.UNDEF;
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_SERVER_CPULOAD:
                    state = getServerCpuLoadState();
                    break;

                default:
                    logger.warn("{}: updateChannel(): Server '{}': No handler defined for channel='{}'",
                            getLogIdentifier(), thing.getLabel(), channel.getAsString());
                    break;
            }

            if (state != null) {
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            logger.error(
                    "{}: context='updateChannel' Error when 'updateChannel()' was called for thing='{}' (Exception='{}'",
                    getLogIdentifier(), channel.getId(), ex.getMessage());
        }
    }

    public void subscribeMonitorEvents(ZoneMinderThingMonitorHandler monitorHandler) {
        try {
            if (zoneMinderEventSession != null) {
                logger.info("{}: context='SubscribeMonitorEvents' thing='monitor' id='{}'", getLogIdentifier(),
                        monitorHandler.getZoneMinderId());

                zoneMinderEventSession.subscribeMonitorEvents(zoneMinderConnection, monitorHandler.getZoneMinderId(),
                        monitorHandler);
            } else {
                logger.warn(
                        "{}: context='SubscribeMonitorEvents' thing='monitor' id='{}' - Could not subscribe to monitor events, because EventSession not initialisaed",
                        getLogIdentifier(), monitorHandler.getZoneMinderId());

            }
        } catch (IllegalArgumentException | GeneralSecurityException | IOException | ZoneMinderUrlNotFoundException e) {
            logger.error(
                    "{}: context='SubscribeMonitorEvents' - Exception occurred when subscribing for MonitorEvents. Exception='{}'",
                    getLogIdentifier(), e.getMessage());
        }
    }

    public void unsubscribeMonitorEvents(ZoneMinderThingMonitorHandler monitorHandler) {
        try {
            if (zoneMinderEventSession != null) {
                zoneMinderEventSession.unsubscribeMonitorEvents(monitorHandler.getZoneMinderId(), monitorHandler);

                logger.info("{}: context='UnsubscribeMonitorEvents' thing='monitor' id='{}'", getLogIdentifier(),
                        monitorHandler.getZoneMinderId());

            } else {
                logger.warn(
                        "{}: context='UnsubscribeMonitorEvents' thing='monitor' id='{}' - Could not unsubscribe to monitor events, because EventSession not initialisaed",
                        getLogIdentifier(), monitorHandler.getZoneMinderId());
            }
        } catch (Exception ex) {
            logger.error(
                    "{}: context='SubscribeMonitorEvents' - Exception occurred when subscribing for MonitorEvents.",
                    getLogIdentifier(), ex);
        }

    }

    public void activateForceAlarm(String monitorId, Integer priority, String reason, String note, String showText,
            Integer timeoutSeconds) {
        try {
            if (zoneMinderEventSession != null) {
                zoneMinderEventSession.activateForceAlarm(monitorId, priority, reason, note, showText, timeoutSeconds);
            } else {
                logger.error("{}: context='activateForceAlarm' No EventSession active for Monitor with Id='{}'",
                        getLogIdentifier(), monitorId);
            }

        } catch (IOException ex) {
            logger.error("{}: context='activateForceAlarm' tag='exception' - Call to activeForceAlarm failed",
                    getLogIdentifier(), ex);
        }

    }

    public void deactivateForceAlarm(String monitorId) {
        try {
            if (zoneMinderEventSession != null) {
                zoneMinderEventSession.deactivateForceAlarm(monitorId);
            } else {
                logger.error("{}: context='deactivateForceAlarm' No EventSession active for Monitor with Id='{}'",
                        getLogIdentifier(), monitorId);
            }

        } catch (Exception ex) {
            logger.error("{}: context='deactivateForceAlarm' tag='exception' - Call to deactiveForceAlarm failed",
                    getLogIdentifier(), ex);
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
            logger.debug("{}: context='getServerCpuLoadState' Exception='{}'", getLogIdentifier(), ex.getMessage());
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
            logger.debug("{}: context='getServerDiskUsageState' Exception {}", getLogIdentifier(), ex.getMessage());
        }

        return state;
    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionHandler connection) {
        IZoneMinderConnectionHandler session = null;
        try {
            session = aquireSession();

            IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(session);
            ZoneMinderConfig cfgPathZms = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_PATH_ZMS);
            ZoneMinderConfig cfgOptFrameServer = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_FRAME_SERVER);
            logger.debug("{}: context='onBridgeConnected'    Api Enabled :            {}", getLogIdentifier(),
                    zoneMinderConnection.isApiEnabled());
            logger.debug("{}: context='onBridgeConnected'    Authentication Enabled : {}", getLogIdentifier(),
                    zoneMinderConnection.isAuthenticationEnabled());
            logger.debug("{}: context='onBridgeConnected'    AuthHash Allowed :       {}", getLogIdentifier(),
                    zoneMinderConnection.getAuthenticationHashAllowed());
            if (zoneMinderConnection.getAuthenticationHashAllowed()) {
                logger.debug("{}: context='onBridgeConnected'    AuthHash Relay :       {}", getLogIdentifier(),
                        zoneMinderConnection.getAuthenticationHashReleayMethod().toString());
            }
            logger.debug("{}: context='onBridgeConnected'    Portal URI:              {}", getLogIdentifier(),
                    zoneMinderConnection.getPortalUri().toString());
            logger.debug("{}: context='onBridgeConnected'    API URI:                 {}", getLogIdentifier(),
                    zoneMinderConnection.getApiUri().toString());
            logger.debug("{}: context='onBridgeConnected'    ZMS URI:                 {}", getLogIdentifier(),
                    cfgPathZms.getValueAsString());
            logger.debug("{}: context='onBridgeConnected'    FrameServer:             {}", getLogIdentifier(),
                    cfgOptFrameServer.getvalueAsBoolean());
        } catch (ZoneMinderException | Exception ex) {
            logger.error(
                    "{}: context='onBridgeConnected' Exception occurred when calling 'onBridgeConencted()' Message='{}'",
                    getLogIdentifier(), ex.getMessage(), ex.getCause());

        } finally {
            if (session != null) {
                releaseSession();
            }
        }

        try {
            // Start the discovery service
            if (discoveryService == null) {
                discoveryService = new ZoneMinderDiscoveryService(this, 30);
            }

            discoveryService.activate();

            if (discoveryRegistration == null) {
                // And register it as an OSGi service
                discoveryRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                        discoveryService, new Hashtable<String, Object>());
            }
        } catch (Exception e) {
            logger.error("{}: context='onBridgeConnected' Exception occurred when starting discovery service",
                    getLogIdentifier(), e.getCause());

        }

        try {
            // Update properties
            updateServerProperties();
        } catch (Exception e) {
            logger.error(
                    "{}: method='onBridgeConnected' context='updateServerProperties' Exception occurred when starting discovery service",
                    getLogIdentifier(), e.getCause());

        }

        if (taskRefreshData != null) {
            taskRefreshData.cancel(true);
            taskRefreshData = null;
        }

        // Start job to handle next updates
        taskRefreshData = startTask(refreshDataRunnable, 1, 1, TimeUnit.SECONDS);

    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.info("{}: Brigde went OFFLINE", getLogIdentifier());

        // Deactivate discovery service
        discoveryService.deactivate();

        // Stopping refresh thread while OFFLINE
        if (taskRefreshData != null) {
            taskRefreshData.cancel(true);
            taskRefreshData = null;
            logger.debug("{}: Stopping DataRefresh task", getLogIdentifier());
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
                logger.debug("{}: onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", getLogIdentifier(),
                        thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }

    }

    /**
     * Method to start a data refresh task.
     */
    protected ScheduledFuture<?> startTask(Runnable command, long delay, long interval, TimeUnit unit) {
        logger.debug("{}: Starting ZoneMinder Bridge Monitor Task. Command='{}'", getLogIdentifier(),
                command.toString());
        if (interval == 0) {
            return null;
        }

        return scheduler.scheduleWithFixedDelay(command, delay, interval, unit);
    }

    /**
     * Method to stop the datarefresh task.
     */
    protected void stopTask(ScheduledFuture<?> task) {
        try {
            if (task != null && !task.isCancelled()) {
                logger.debug("{}: Stopping ZoneMinder Bridge Monitor Task. Task='{}'", getLogIdentifier(),
                        task.toString());
                task.cancel(true);
            }
        } catch (Exception ex) {
        }
    }

    public ArrayList<IMonitorDataGeneral> getMonitors() {
        if (isConnected()) {
            IZoneMinderServer serverProxy = null;
            try {
                serverProxy = ZoneMinderFactory.getServerProxy(aquireSession());
                ArrayList<IMonitorDataGeneral> result = serverProxy.getMonitors();
                return result;
            } catch (ZoneMinderGeneralException | ZoneMinderResponseException | ZoneMinderInvalidData
                    | ZoneMinderAuthenticationException ex) {
                logger.error("{}: context='getMonitors' Exception occurred", getLogIdentifier(), ex.getCause());

            } finally {
                if (serverProxy != null) {
                    releaseSession();
                }
            }
        }
        return new ArrayList<>();
    }

    private void updateServerProperties() {
        if (!isConnected()) {
            return;
        }

        // Update property information about this device
        Map<String, String> properties = editProperties();

        IZoneMinderConnectionHandler session = null;
        IZoneMinderHostVersion hostVersion = null;
        try {
            session = aquireSession();
            IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(session);

            hostVersion = serverProxy.getHostVersion();
            if (hostVersion.getHttpStatus() != HttpStatus.OK_200) {
                return;
            }

            ZoneMinderConfig configUseApi = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_API);

            ZoneMinderConfig configUseAuth = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_USE_AUTH);
            ZoneMinderConfig configTrigerrs = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_TRIGGERS);
            ZoneMinderConfig configAllowHashLogin = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_AUTH_HASH_LOGINS);
            ZoneMinderConfig configFrameServer = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_FRAME_SERVER);

            properties.put(ZoneMinderProperties.PROPERTY_SERVER_VERSION, hostVersion.getVersion());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_API_VERSION, hostVersion.getApiVersion());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_USE_API, configUseApi.getValueAsString());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_USE_AUTHENTIFICATION, configUseAuth.getValueAsString());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_USE_AUTH_HASH, configAllowHashLogin.getValueAsString());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_TRIGGERS_ENABLED, configTrigerrs.getValueAsString());
            properties.put(ZoneMinderProperties.PROPERTY_SERVER_FRAME_SERVER, configFrameServer.getValueAsString());

        } catch (ZoneMinderUrlNotFoundException | IOException | ZoneMinderGeneralException | ZoneMinderResponseException
                | ZoneMinderInvalidData | ZoneMinderAuthenticationException e) {
            logger.warn("{}: Exception occurred when updating monitor properties", getLogIdentifier(), e);
        } finally {
            if (session != null) {
                releaseSession();
            }
        }

        // Must loop over the new properties since we might have added data
        boolean update = false;
        Map<String, String> originalProperties = editProperties();
        for (String property : properties.keySet()) {
            if ((originalProperties.get(property) == null
                    || !originalProperties.get(property).equals(properties.get(property)))) {
                update = true;
                break;
            }
        }

        if (update) {
            logger.info("{}: Properties synchronised, Thing id: {}", getLogIdentifier(), getThingId());
            updateProperties(properties);
        }
    }

    @Override
    public String getLogIdentifier() {
        String result = "[BRIDGE]";
        try {
            result = String.format("[BRIDGE (%s)]", getThingId());
        } catch (Exception e) {
            result = "[BRIDGE (?)]";
        }
        return result;
    }
}
