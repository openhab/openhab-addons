/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.ZoneMinderProperties;
import org.openhab.binding.zoneminder.internal.RefreshPriority;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingMonitorConfig;
import org.openhab.binding.zoneminder.internal.state.ChannelStateChangeSubscriber;
import org.openhab.binding.zoneminder.internal.state.MonitorThingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.IZoneMinderConnectionHandler;
import name.eskildsen.zoneminder.IZoneMinderEventSubscriber;
import name.eskildsen.zoneminder.IZoneMinderMonitor;
import name.eskildsen.zoneminder.IZoneMinderServer;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.api.monitor.ZoneMinderMonitorStatus;
import name.eskildsen.zoneminder.api.telnet.ZoneMinderTriggerEvent;
import name.eskildsen.zoneminder.common.ZoneMinderConfigEnum;
import name.eskildsen.zoneminder.common.ZoneMinderMonitorFunctionEnum;
import name.eskildsen.zoneminder.data.IMonitorDataGeneral;
import name.eskildsen.zoneminder.data.IMonitorDataStillImage;
import name.eskildsen.zoneminder.data.IZoneMinderDaemonStatus;
import name.eskildsen.zoneminder.data.IZoneMinderEventData;
import name.eskildsen.zoneminder.data.ZoneMinderConfig;
import name.eskildsen.zoneminder.exception.ZoneMinderAuthHashNotEnabled;
import name.eskildsen.zoneminder.exception.ZoneMinderAuthenticationException;
import name.eskildsen.zoneminder.exception.ZoneMinderException;
import name.eskildsen.zoneminder.exception.ZoneMinderGeneralException;
import name.eskildsen.zoneminder.exception.ZoneMinderInvalidData;
import name.eskildsen.zoneminder.exception.ZoneMinderResponseException;
import name.eskildsen.zoneminder.internal.ZoneMinderContentResponse;

/**
 * The {@link ZoneMinderThingMonitorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderThingMonitorHandler extends ZoneMinderBaseThingHandler
        implements ChannelStateChangeSubscriber, IZoneMinderEventSubscriber {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR);

    private static final String MONITOR_STATUS_NOT_INIT = "<Not Initialized>";
    private static final int MAX_MONITOR_STATUS_WATCH_COUNT = 3;

    /** Make sure we can log errors, warnings or what ever somewhere */
    private final Logger logger = LoggerFactory.getLogger(ZoneMinderThingMonitorHandler.class);
    private RefreshPriority forcedPriority = RefreshPriority.DISABLED;

    private String lastMonitorStatus = MONITOR_STATUS_NOT_INIT;
    private Integer monitorStatusMatchCount = 3;

    private ZoneMinderThingMonitorConfig config;

    MonitorThingState dataConverter = new MonitorThingState(this);

    private long lastRefreshGeneralData = 0;
    private long lastRefreshStillImage = 0;
    private boolean frameDaemonActive = false;

    public ZoneMinderThingMonitorHandler(Thing thing) {
        super(thing);

        logger.info("{}: Starting ZoneMinder Server Thing Handler (Thing='{}')", getLogIdentifier(), thing.getUID());
    }

    @Override
    public void dispose() {
        try {
            ZoneMinderServerBridgeHandler bridge = getZoneMinderBridgeHandler();
            logger.info("{}: Unsubscribing from Monitor Events: {}", getLogIdentifier(),
                    bridge.getThing().getUID().getAsString());
            bridge.unsubscribeMonitorEvents(this);

        } catch (Exception ex) {
            logger.error("{}: Exception occurred when calling 'onBridgeDisonnected()'.", getLogIdentifier(), ex);
        }
    }

    @Override
    public String getZoneMinderId() {
        if (config == null) {
            logger.error("{}: Configuration for Thing '{}' is not loaded correctly.", getLogIdentifier(),
                    getThing().getUID());
            return "";
        }
        return config.getZoneMinderId().toString();

    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionHandler connection) {
        try {
            logger.debug("{}: Bridge '{}' connected", getLogIdentifier(), bridge.getThing().getUID().getAsString());
            super.onBridgeConnected(bridge, connection);

            logger.info("{}: Add subsription for Monitor Events: {}", getLogIdentifier(),
                    bridge.getThing().getUID().getAsString());
            bridge.subscribeMonitorEvents(this);

            IZoneMinderServer serverProxy = ZoneMinderFactory.getServerProxy(connection);
            ZoneMinderConfig cfg = serverProxy.getConfig(ZoneMinderConfigEnum.ZM_OPT_FRAME_SERVER);
            frameDaemonActive = cfg.getvalueAsBoolean();
        } catch (ZoneMinderGeneralException | ZoneMinderResponseException | ZoneMinderAuthenticationException
                | ZoneMinderInvalidData ex) {
            logger.error("{}: context='onBridgeConnected' error in call to 'getServerProxy' - Message='{}'",
                    getLogIdentifier(), ex.getMessage(), ex.getCause());

        } catch (MalformedURLException e) {
            logger.error("{}: context='onBridgeConnected' error in call to 'getServerProxy' - Message='{}' (Exception)",
                    getLogIdentifier(), e.getMessage(), e.getCause());
        }

    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        try {
            logger.debug("{}: Bridge '{}' disconnected", getLogIdentifier(), bridge.getThing().getUID().getAsString());

            super.onBridgeDisconnected(bridge);

        } catch (Exception ex) {
            logger.error("{}: Exception occurred when calling 'onBridgeDisonencted()'.", getLogIdentifier(), ex);
        }

    }

    @Override
    public void onThingStatusChanged(ThingStatus thingStatus) {
        if (thingStatus == ThingStatus.ONLINE) {
            IZoneMinderConnectionHandler connection = null;
            try {
                connection = aquireSessionWait();
                IZoneMinderMonitor monitor = ZoneMinderFactory.getMonitorProxy(connection, config.getZoneMinderId());
                IMonitorDataGeneral monitorData = monitor.getMonitorData();

                logger.debug("{}:    SourceType:         {}", getLogIdentifier(), monitorData.getSourceType().name());
                logger.debug("{}:    Format:             {}", getLogIdentifier(), monitorData.getFormat());
                logger.debug("{}:    AlarmFrameCount:    {}", getLogIdentifier(), monitorData.getAlarmFrameCount());
                logger.debug("{}:    AlarmMaxFPS:        {}", getLogIdentifier(), monitorData.getAlarmMaxFPS());
                logger.debug("{}:    AnalysisFPS:        {}", getLogIdentifier(), monitorData.getAnalysisFPS());
                logger.debug("{}:    Height x Width:     {} x {}", getLogIdentifier(), monitorData.getHeight(),
                        monitorData.getWidth());
            } catch (ZoneMinderInvalidData | ZoneMinderAuthenticationException | ZoneMinderGeneralException
                    | ZoneMinderResponseException ex) {
                logger.error("{}: context='onThingStatusChanged' error in call to 'getMonitorData' - Message='{}'",
                        getLogIdentifier(), ex.getMessage(), ex.getCause());

            } finally {
                if (connection != null) {
                    releaseSession();
                }
            }

            try {
                updateMonitorProperties();

            } catch (Exception ex) {
                logger.error(
                        "{}: context='onThingStatusChanged' - Exception occurred when calling 'updateMonitorPropoerties()'. Exception='{}'",
                        getLogIdentifier(), ex.getMessage());

            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            if (!channelUID.getId().equals(ZoneMinderConstants.CHANNEL_ONLINE)) {
                dataConverter.subscribe(channelUID);
            }
            super.channelLinked(channelUID);

            logger.info("{}: context='channelLinked' - Unlinking from channel '{}'", getLogIdentifier(),
                    channelUID.getAsString());
        } catch (Exception ex) {
            logger.info("{}: context='channelUnlinked' - Exception when Unlinking from channel '{}' - EXCEPTION)'{}'",
                    getLogIdentifier(), channelUID.getAsString(), ex.getMessage());

        }

    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        try {
            dataConverter.unsubscribe(channelUID);
            super.channelUnlinked(channelUID);
            logger.info("{}: context='channelUnlinked' - Unlinking from channel '{}'", getLogIdentifier(),
                    channelUID.getAsString());
        } catch (Exception ex) {
            logger.info("{}: context='channelUnlinked' - Exception when Unlinking from channel '{}' - EXCEPTION)'{}'",
                    getLogIdentifier(), channelUID.getAsString(), ex.getMessage());

        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        IZoneMinderMonitor monitorProxy = null;

        try {
            logger.debug("{}: Channel '{}' in monitor '{}' received command='{}'", getLogIdentifier(), channelUID,
                    getZoneMinderId(), command);

            // Allow refresh of channels
            if (command == RefreshType.REFRESH) {
                updateChannel(channelUID);
                return;
            }

            // Communication TO Monitor
            switch (channelUID.getId()) {
                // Done via Telnet connection
                case ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM:
                    IZoneMinderConnectionHandler connection = null;
                    try {
                        // Force Alarm can only be activated when Function is either NODECT or MODECT
                        if ((dataConverter.getMonitorFunction() == ZoneMinderMonitorFunctionEnum.MODECT)
                                || (dataConverter.getMonitorFunction() == ZoneMinderMonitorFunctionEnum.NODECT)) {
                            logger.debug(
                                    "{}: 'handleCommand' => CHANNEL_MONITOR_FORCE_ALARM: Command '{}' received for monitor '{}'",
                                    getLogIdentifier(), command, channelUID.getId());

                            if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                                dataConverter.setMonitorForceAlarmInternal((command == OnOffType.ON) ? true : false);
                                String eventText = getConfigValueAsString(
                                        ZoneMinderConstants.PARAMETER_MONITOR_EVENTTEXT);

                                BigDecimal eventTimeout = getConfigValueAsBigDecimal(
                                        ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT);

                                try {
                                    connection = aquireSession();
                                    if (connection == null) {
                                        logger.error(
                                                "{}: context='handleCommand' tags='ForceAlarm' - Command='{}' failed to obtain session",
                                                getLogIdentifier(), command);
                                        return;
                                    }
                                    monitorProxy = ZoneMinderFactory.getMonitorProxy(connection, getZoneMinderId());

                                    if (command == OnOffType.ON) {
                                        logger.info("{}: Activate 'ForceAlarm' to '{}' (Reason='{}', Timeout='{}')",
                                                getLogIdentifier(), command, eventText, eventTimeout.intValue());

                                        getZoneMinderBridgeHandler().activateForceAlarm(getZoneMinderId(), 255,
                                                ZoneMinderConstants.MONITOR_EVENT_OPENHAB, eventText, "",
                                                eventTimeout.intValue());

                                        dataConverter.setMonitorForceAlarmInternal(true);

                                        // Force a refresh
                                        startAlarmRefresh(eventTimeout.intValue());

                                    }

                                    else if (command == OnOffType.OFF) {
                                        logger.debug("{}: Cancel 'ForceAlarm'", getLogIdentifier());

                                        getZoneMinderBridgeHandler().deactivateForceAlarm(getZoneMinderId());
                                        dataConverter.setMonitorForceAlarmInternal(false);
                                        // Stop Alarm Refresh
                                        forceStopAlarmRefresh();

                                    }
                                    fetchMonitorGeneralData(monitorProxy);

                                } catch (Exception ex) {
                                    logger.error(
                                            "{}: Context='handleCommand' Channel='{}' EXCEPTION:  Call to 'ForceAlarm' Command='{}' failed",
                                            getLogIdentifier(), channelUID.getId(), command, ex);
                                }

                            }
                        } else {
                            logger.warn(
                                    "{}: context='handleCommand' tag='CHANNEL_MONITOR_FORCE_ALARM' is inactive when function is not 'MODECT' or 'NODECT'",
                                    getLogIdentifier());

                        }
                    } catch (Exception ex) {
                        logger.error("{}: context='handleCommand' tag='CHANNEL_MONITOR_FORCE_ALARM'",
                                getLogIdentifier());
                    } finally {
                        if (monitorProxy != null) {
                            monitorProxy = null;
                            releaseSession();
                        }

                        requestChannelRefresh();
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    try {
                        logger.debug(
                                "{}:context='handleCommand' tag='CHANNEL_MONITOR_ENABLED' Command '{}' received for monitor '{}'",
                                getLogIdentifier(), command, channelUID.getId());

                        if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                            boolean newState = ((command == OnOffType.ON) ? true : false);

                            ZoneMinderContentResponse zmcr = null;
                            try {
                                monitorProxy = ZoneMinderFactory.getMonitorProxy(aquireSession(), getZoneMinderId());
                                if (monitorProxy == null) {
                                    logger.error(
                                            "{}: Connection to ZoneMinder Server was lost when handling command '{}'. Restart openHAB",
                                            getLogIdentifier(), command);
                                    return;
                                }
                                zmcr = monitorProxy.SetEnabled(newState);
                                logger.debug("{}: ResponseCode='{}' ResponseMessage='{}' URL='{}'", getLogIdentifier(),
                                        zmcr.getHttpStatus(), zmcr.getHttpResponseMessage(), zmcr.getHttpRequestUrl());

                            } catch (ZoneMinderException ex) {
                                logger.error(
                                        "{}: context='handleCommand' error in call to 'SetEnabled' ExceptionClass='{}' - Message='{}'",
                                        getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(),
                                        ex.getCause());
                            } finally {
                                if (monitorProxy != null) {
                                    monitorProxy = null;
                                    releaseSession();
                                }
                            }

                            dataConverter.setMonitorEnabled(newState);

                            logger.info(
                                    "{}: context='handleCommand' tags='enabled' - Successfully changed function setting to '{}'",
                                    getLogIdentifier(), command);
                        }
                    } catch (Exception ex) {
                        logger.error("{}: Exception in 'handleCommand' => 'CHANNEL_MONITOR_ENABLE' Exception='{}'",
                                getLogIdentifier(), ex.getMessage());

                    } finally {
                        requestChannelRefresh();
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    try {
                        logger.debug(
                                "{}: context='handleCommand' tag='CHANNEL_MONITOR_FUNCTION' Command '{}' received for monitor '{}'",
                                getLogIdentifier(), command, channelUID.getId());

                        String commandString = "";
                        if (ZoneMinderMonitorFunctionEnum.isValid(command.toString())) {
                            commandString = ZoneMinderMonitorFunctionEnum.getEnum(command.toString()).toString();
                            ZoneMinderContentResponse zmcr = null;
                            try {
                                // Change Function for camera in ZoneMinder
                                monitorProxy = ZoneMinderFactory.getMonitorProxy(aquireSession(), getZoneMinderId());
                                if (monitorProxy == null) {
                                    logger.error(
                                            "{}: Connection to ZoneMinder Server was lost when handling command '{}'. Restart openHAB",
                                            getLogIdentifier(), command);
                                    return;
                                }

                                zmcr = monitorProxy.SetFunction(commandString);

                                logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                                        zmcr.getHttpRequestUrl(), zmcr.getHttpStatus(), zmcr.getHttpResponseMessage());

                                fetchMonitorGeneralData(monitorProxy);
                                fetchMonitorDaemonStatus(true, true, monitorProxy);

                            } catch (ZoneMinderAuthenticationException | ZoneMinderGeneralException
                                    | ZoneMinderResponseException ex) {
                            } finally {
                                if (monitorProxy != null) {
                                    monitorProxy = null;
                                    releaseSession();
                                }
                            }

                            dataConverter.setMonitorFunction(ZoneMinderMonitorFunctionEnum.getEnum(command.toString()));

                            logger.debug(
                                    "{}: context='handleCommand' tags='function' - Successfully changed function setting to '{}'",
                                    getLogIdentifier(), commandString);

                        } else {
                            logger.error(
                                    "{}: Value '{}' for monitor channel is not valid. Accepted values is: 'None', 'Monitor', 'Modect', Record', 'Mocord', 'Nodect'",
                                    getLogIdentifier(), commandString);
                        }
                    } catch (Exception ex) {
                        logger.error("{}: Exception in 'handleCommand' => 'CHANNEL_MONITOR_FUNCTION'",
                                getLogIdentifier(), ex.getCause());

                    } finally {
                        requestChannelRefresh();

                    }

                    break;

                // They are all readonly in the channel config.
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS:
                case ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE:
                case ZoneMinderConstants.CHANNEL_ONLINE:
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE:
                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE:
                    // Do nothing, they are all read only
                    break;
                default:
                    logger.warn("{}: Command received for an unknown channel: {}", getLogIdentifier(),
                            channelUID.getId());
                    break;
            }
        } catch (Exception ex) {
            logger.error("{}: handleCommand: Command='{}' failed for channel='{}'", getLogIdentifier(), command,
                    channelUID.getId(), ex);
        }
    }

    @Override
    public void initialize() {
        try {
            this.config = getMonitorConfig();

            super.initialize();
            logger.info("{}: context='initialize' Monitor Handler Initialized", getLogIdentifier());

            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_MOTION_EVENT));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_ENABLED));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION));
            dataConverter.addChannel(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE));

        } catch (Exception ex) {
            logger.error("{}: Exception occurred when calling 'initialize()'. Exception='{}'", getLogIdentifier(),
                    ex.getMessage());
        }
    }

    @Override
    public void onTrippedForceAlarm(ZoneMinderTriggerEvent event) {
        try {
            logger.debug("{}: context='onTrippedForceAlarm' Received forceAlarm for monitor {}", getLogIdentifier(),
                    event.getMonitorId());

            if (!isThingOnline()) {
                logger.warn("{}: context='onTrippedForceAlarm' Skipping event '{}', because Thing is 'OFFLINE'",
                        getLogIdentifier(), event.toString());
                return;
            }

            IZoneMinderEventData eventData = null;

            // Set Current Event to actual event
            if (event.getState()) {
                IZoneMinderConnectionHandler connection = null;
                try {
                    connection = aquireSession();
                    IZoneMinderMonitor monitorProxy = ZoneMinderFactory.getMonitorProxy(connection, getZoneMinderId());
                    eventData = monitorProxy.getEventById(event.getEventId());

                    logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                            eventData.getHttpRequestUrl(), eventData.getHttpStatus(),
                            eventData.getHttpResponseMessage());

                } catch (Exception ex) {
                    logger.error(
                            "{}: context='onTrippedForceAlarm' tag='session' Exception occurred when aquiring session - Exception='{}'",
                            getLogIdentifier(), ex.getMessage());

                } catch (ZoneMinderInvalidData | ZoneMinderAuthenticationException | ZoneMinderGeneralException
                        | ZoneMinderResponseException ex) {
                    logger.error(
                            "{}: context='onTrippedForceAlarm' error in call to 'getMonitorProxy' ExceptionClass='{}' - Message='{}'",
                            getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(), ex.getCause());
                } finally {
                    if (connection != null) {
                        releaseSession();
                    }

                }

                dataConverter.disableRefresh();
                dataConverter.setMonitorForceAlarmExternal(event.getState());
                dataConverter.setMonitorEventData(eventData);
                dataConverter.enableRefresh();

                forceStartAlarmRefresh();
            } else {
                dataConverter.disableRefresh();

                dataConverter.setMonitorForceAlarmExternal(event.getState());
                dataConverter.setMonitorEventData(null);
                dataConverter.enableRefresh();
                forceStopAlarmRefresh();
            }

        } catch (Exception ex) {
            logger.error("{}:  context='onTrippedForceAlarm' Exception occurred inTrippedForceAlarm() Exception='{}'",
                    getLogIdentifier(), ex.getMessage());

        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    protected ZoneMinderThingMonitorConfig getMonitorConfig() {
        return this.getConfigAs(ZoneMinderThingMonitorConfig.class);
    }

    @Override
    protected String getZoneMinderThingType() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

    @Override
    public void updateAvaliabilityStatus(IZoneMinderConnectionHandler connection) {
        // Assume success
        ThingStatus newThingStatus = ThingStatus.ONLINE;
        ThingStatusDetail thingStatusDetailed = ThingStatusDetail.NONE;
        String thingStatusDescription = "";

        ThingStatus curThingStatus = this.getThing().getStatus();

        boolean connectionStatus = false;

        // Is connected to ZoneMinder and thing is ONLINE
        if (isConnected() && curThingStatus == ThingStatus.ONLINE) {
            return;
        }

        try {
            String msg;
            Bridge b = getBridge();

            // 1. Is there a Bridge assigned?
            if (getBridge() == null) {
                msg = String.format("No Bridge assigned to monitor '%s'", thing.getUID());
                logger.error("{}: context='updateAvailabilityStatus' {}", getLogIdentifier(), msg);
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.BRIDGE_OFFLINE;
                thingStatusDescription = "No Bridge assigned to monitor";
                return;
            } else {
                logger.debug(
                        "{}: context='updateAvailabilityStatus' ThingAvailability: Thing '{}' has Bridge '{}' defined (Check PASSED)",
                        getLogIdentifier(), thing.getUID(), getBridge().getBridgeUID());
            }

            // 2. Is Bridge Online?
            if (getBridge().getStatus() != ThingStatus.ONLINE) {
                msg = String.format("Bridge '%s' is OFFLINE", getBridge().getBridgeUID());
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.BRIDGE_OFFLINE;
                thingStatusDescription = msg;
                logger.error("{}: context='updateAvailabilityStatus' {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug(
                        "{}: context='updateAvailabilityStatus' ThingAvailability: Bridge '{}' is ONLINE (Check PASSED)",
                        getLogIdentifier(), getBridge().getBridgeUID());
            }

            // 3. Is Configuration OK?
            if (getMonitorConfig() == null) {
                msg = String.format("No valid configuration found for '%s'", thing.getUID());
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.CONFIGURATION_ERROR;
                thingStatusDescription = msg;

                logger.error("{}: context='updateAvailabilityStatus' {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug(
                        "{}: context='updateAvailabilityStatus' ThingAvailability: Thing '{}' has valid configuration (Check PASSED)",
                        getLogIdentifier(), thing.getUID());
            }

            // ZoneMinder Id for Monitor not set, we are pretty much lost then
            if (getMonitorConfig().getZoneMinderId().isEmpty()) {
                msg = String.format("No Id is specified for monitor '%s'", thing.getUID());
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.CONFIGURATION_ERROR;
                thingStatusDescription = msg;

                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug(
                        "{}: context='updateAvailabilityStatus' ThingAvailability: ZoneMinder Id for Thing '{}' defined (Check PASSED)",
                        getLogIdentifier(), thing.getUID());
            }

            IZoneMinderMonitor monitorProxy = null;
            IZoneMinderDaemonStatus captureDaemon = null;
            // Consider also looking at Analysis and Frame Daemons (only if they are supposed to be running)
            IZoneMinderConnectionHandler curSession = connection;
            if (curSession != null) {
                monitorProxy = ZoneMinderFactory.getMonitorProxy(curSession, getZoneMinderId());

                captureDaemon = monitorProxy.getCaptureDaemonStatus();
                logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                        captureDaemon.getHttpRequestUrl(), captureDaemon.getHttpStatus(),
                        captureDaemon.getHttpResponseMessage());

            }

            if (captureDaemon == null) {
                msg = String.format("Capture Daemon not accssible");
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.COMMUNICATION_ERROR;
                thingStatusDescription = msg;
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else if (!captureDaemon.getStatus()) {
                msg = String.format("Capture Daemon is not running");
                newThingStatus = ThingStatus.OFFLINE;
                thingStatusDetailed = ThingStatusDetail.COMMUNICATION_ERROR;
                thingStatusDescription = msg;
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            }
            newThingStatus = ThingStatus.ONLINE;
            forcedPriority = RefreshPriority.PRIORITY_BATCH;
        } catch (ZoneMinderException | Exception exception) {
            newThingStatus = ThingStatus.OFFLINE;
            thingStatusDetailed = ThingStatusDetail.COMMUNICATION_ERROR;
            thingStatusDescription = "Error occurred (Check log)";
            updateThingStatus(newThingStatus, thingStatusDetailed, thingStatusDescription);

            logger.error("{}: context='updateAvailabilityStatus' Exception occurred '{}'", getLogIdentifier(),
                    exception.getMessage());

            return;
        } finally {
            updateThingStatus(newThingStatus, thingStatusDetailed, thingStatusDescription);
        }
    }

    /*
     * From here we update states in openHAB
     *
     * @see
     * org.openhab.binding.zoneminder.handler.ZoneMinderBaseThingHandler#updateChannel(org.eclipse.smarthome.core.thing.
     * ChannelUID)
     */
    @Override
    public void updateChannel(ChannelUID channel) {
        State state = UnDefType.UNDEF;

        try {
            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_ONLINE:
                    super.updateChannel(channel);
                    return;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                case ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM:
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_MOTION_EVENT:
                case ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS:
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE:
                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_STILL_IMAGE:
                    state = null;
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_VIDEOURL:
                    state = dataConverter.getVideoUrl();
                    break;
                default:
                    logger.warn("{}: updateChannel(): Monitor '{}': No handler defined for channel='{}'",
                            getLogIdentifier(), thing.getLabel(), channel.getAsString());

                    // Ask super class to handle
                    super.updateChannel(channel);
            }

            if (state != null) {
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            logger.error("{}: context='updateChannel' Error when updating channelId='{}' state='{}'",
                    getLogIdentifier(), channel.toString(), state.toString(), ex);
        }
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateState(ZoneMinderConstants.CHANNEL_ONLINE,
                ((status == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF));

    }

    private long getLastRefreshGeneralData() {
        return lastRefreshGeneralData;
    }

    private long getLastRefreshStillImage() {
        return lastRefreshStillImage;
    }

    private boolean refreshGeneralData() {
        long now = System.currentTimeMillis();
        long lastUpdate = getLastRefreshGeneralData();

        // Normal refresh interval
        int interval = 10000;

        if (!isInitialized()) {
            return true;
        }
        if (dataConverter.isAlarmed()) {
            // Alarm refresh interval
            interval = 1000;
        }
        return ((now - lastUpdate) > interval) ? true : false;
    }

    private boolean refreshStillImage() {
        RefreshPriority priority;
        long now = System.currentTimeMillis();
        long lastUpdate = getLastRefreshStillImage();

        // Normal refresh interval
        int interval = 10000;

        if (!isInitialized()) {
            return true;
        }
        if (dataConverter.isAlarmed()) {
            priority = getMonitorConfig().getImageRefreshEvent();
        } else {
            priority = getMonitorConfig().getImageRefreshIdle();
        }
        switch (priority) {
            case DISABLED:
                return false;

            case PRIORITY_BATCH:
                interval = 1000 * 60 * 60;
                break;

            case PRIORITY_LOW:
                interval = 1000 * 60;
                break;

            case PRIORITY_NORMAL:
                interval = 1000 * 10;
                break;

            case PRIORITY_HIGH:
                interval = 1000 * 5;
                break;

            case PRIORITY_ALARM:
                interval = 1000;
                break;
            default:
                return false;
        }
        return ((now - lastUpdate) > interval) ? true : false;
    }

    @Override
    protected void onFetchData(RefreshPriority cyclePriority) {
        IZoneMinderConnectionHandler session = null;
        IMonitorDataGeneral data = null;

        boolean refreshChannels = false;

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }

        RefreshPriority curRefreshPriority = RefreshPriority.DISABLED;

        if (forcedPriority == RefreshPriority.UNKNOWN) {
            return;
        }

        if (forcedPriority == RefreshPriority.DISABLED) {
            curRefreshPriority = cyclePriority;
        } else {
            curRefreshPriority = forcedPriority;
            forcedPriority = RefreshPriority.DISABLED;
        }

        session = null;
        try {
            session = aquireSession();
            if (session == null) {
                logger.warn("{}: Failed to aquire session for refresh, refresh loop for monitor will be skipped.",
                        getLogIdentifier());
                return;
            }

        } catch (Exception ex) {
            logger.error("{}: Exception occurred when aquiring exception. Refresh loop for monitor will be skipped.",
                    getLogIdentifier(), ex.getCause());
            return;
        }

        IZoneMinderMonitor monitorProxy = ZoneMinderFactory.getMonitorProxy(session, getZoneMinderId());
        dataConverter.disableRefresh();
        /**************************
         *
         * Perform refresh of monitor data
         **************************/

        if (refreshGeneralData()) {
            refreshChannels = true;
            fetchMonitorGeneralData(monitorProxy);

            fetchMonitorDaemonStatus(true, true, monitorProxy);
        }

        if (isLinked(ZoneMinderConstants.CHANNEL_MONITOR_STILL_IMAGE)) {
            try {
                if (refreshStillImage()) {
                    lastRefreshStillImage = System.currentTimeMillis();
                    IMonitorDataStillImage monitorImage = monitorProxy.getMonitorStillImage(config.getImageScale(),
                            1000, null);
                    logger.debug("{}: context='onFetchData' tag='image' URL='{}' ResponseCode='{}'", getLogIdentifier(),
                            monitorImage.getHttpRequestUrl(), monitorImage.getHttpStatus());

                    dataConverter.setMonitorStillImage(monitorImage.getImage());
                }
            } catch (MalformedURLException mue) {
                logger.error(
                        "{}: context='onFetchData' NalformedURL Exception occurred when calling to 'getMonitorStillImage'",
                        getLogIdentifier(), mue.getCause());
                dataConverter.setMonitorStillImage(null);
            } catch (Exception ex) {
                logger.error("{}: context='onFetchData' error in call to 'getMonitorStillImage'", getLogIdentifier(),
                        ex.getCause());
                dataConverter.setMonitorStillImage(null);
            } catch (ZoneMinderException ex) {
                logger.error(
                        "{}: context='onFetchData' error in call to 'getMonitorStillImage' ExceptionClass='{}' - Message='{}'",
                        getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(), ex.getCause());
            }
        } else {
            dataConverter.setMonitorStillImage(null);

        }

        if (curRefreshPriority.isPriorityActive(RefreshPriority.PRIORITY_LOW))

        {
            try {
                if (dataConverter != null) {
                    String str = monitorProxy.getMonitorStreamingPath(config.getImageScale(), 1000, null);
                    dataConverter.setMonitorVideoUrl(str);
                }
            } catch (MalformedURLException e1) {
                logger.error("{}: MalformedURLException occurred when calling 'getMonitorStreamingPath()'",
                        getLogIdentifier(), e1.getCause());

            } catch (ZoneMinderGeneralException zmge) {
                logger.error(
                        "{}: context='onFetchData' error in call to 'getMonitorStreamingPath' Exception='{}', Message='{}",
                        getLogIdentifier(), zmge.getClass().getCanonicalName(), zmge.getMessage(), zmge.getCause());
            } catch (ZoneMinderResponseException zmre) {
                logger.error(
                        "{}: context='fetchMonitorDaemonStatus' error in call to 'getMonitorStreamingPath' Exception='{}', Message='{} - Http: Status='{}', Mesage='{}'",
                        getLogIdentifier(), zmre.getClass().getCanonicalName(), zmre.getMessage(), zmre.getHttpStatus(),
                        zmre.getHttpMessage(), zmre.getCause());
            } catch (ZoneMinderAuthHashNotEnabled zmahne) {
                logger.error(
                        "{}: context='onFetchData' error in call to 'getMonitorStreamingPath' Exception='{}', Message='{}'",
                        getLogIdentifier(), zmahne.getClass().getCanonicalName(), zmahne.getMessage(),
                        zmahne.getCause());
            }
        }

        if (session != null) {
            releaseSession();
            session = null;
        }
        dataConverter.enableRefresh();
        if (refreshChannels) {
            logger.debug("{}: context='onFetchData' - Data has changed, channels need refreshing", getLogIdentifier());
            requestChannelRefresh();
        }
        tryStopAlarmRefresh();
    }

    void fetchMonitorGeneralData(IZoneMinderMonitor proxy) {
        IZoneMinderMonitor monitorProxy = proxy;
        boolean doRelase = false;
        if (monitorProxy == null) {
            doRelase = true;
            monitorProxy = ZoneMinderFactory.getMonitorProxy(aquireSession(), getZoneMinderId());
        }

        try {
            IMonitorDataGeneral generalData = monitorProxy.getMonitorData();
            logger.debug("{}: context='onFetchData' tag='monitorData' URL='{}' ResponseCode='{}' ResponseMessage='{}'",
                    getLogIdentifier(), generalData.getHttpRequestUrl(), generalData.getHttpStatus(),
                    generalData.getHttpResponseMessage());

            dataConverter.setMonitorGeneralData(generalData);
        } catch (ZoneMinderInvalidData zmid) {
            logger.error(
                    "{}: context='fetchMonitorDaemonStatus' error in call to 'getMonitorData' Exception='{}' Response='{}', Message='{}'",
                    getLogIdentifier(), zmid.getClass().getCanonicalName(), zmid.getResponseString(), zmid.getMessage(),
                    zmid.getCause());
        } catch (ZoneMinderAuthenticationException | ZoneMinderGeneralException | ZoneMinderResponseException zme) {
            logger.error(
                    "{}: context='fetchMonitorDaemonStatus' error in call to 'getMonitorData' Exception='{}' Message='{}'",
                    getLogIdentifier(), zme.getClass().getCanonicalName(), zme.getMessage(), zme.getCause());
        }

        try {
            ZoneMinderMonitorStatus status = monitorProxy.getMonitorDetailedStatus();

            logger.debug(
                    "{}: context='onFetchData' tag='detailedStatus' URL='{}' ResponseCode='{}' ResponseMessage='{}'",
                    getLogIdentifier(), status.getHttpRequestUrl(), status.getHttpStatus(),
                    status.getHttpResponseMessage());

            dataConverter.setMonitorDetailedStatus(status.getStatus());
        } catch (ZoneMinderInvalidData zmid) {
            logger.error(
                    "{}: context='fetchMonitorDaemonStatus' error in call to 'getMonitorDetailedStatus' Exception='{}', Message='{}',  Response='{}'",
                    getLogIdentifier(), zmid.getClass().getCanonicalName(), zmid.getMessage(), zmid.getResponseString(),
                    zmid.getCause());
        } catch (ZoneMinderAuthenticationException | ZoneMinderGeneralException | ZoneMinderResponseException zme) {
            logger.error(
                    "{}: context='fetchMonitorDaemonStatus' error in call to 'getMonitorDetailedStatus' Exception='{}' Message='{}'",
                    getLogIdentifier(), zme.getClass().getCanonicalName(), zme.getMessage(), zme.getCause());
        } finally {
            if (doRelase) {
                releaseSession();
            }
        }
        lastRefreshGeneralData = System.currentTimeMillis();
    }

    void fetchMonitorDaemonStatus(boolean fetchCapture, boolean fetchAnalysisFrame, IZoneMinderMonitor proxy) {
        IZoneMinderMonitor monitorProxy = proxy;
        boolean fetchFrame = false;

        boolean doRelase = false;
        if (monitorProxy == null) {
            doRelase = true;
            monitorProxy = ZoneMinderFactory.getMonitorProxy(aquireSession(), getZoneMinderId());
        }
        try {
            State stateCapture = UnDefType.UNDEF;
            State stateAnalysis = UnDefType.UNDEF;
            State stateFrame = UnDefType.UNDEF;

            IZoneMinderDaemonStatus captureDaemon = null;
            IZoneMinderDaemonStatus analysisDaemon = null;
            IZoneMinderDaemonStatus frameDaemon = null;

            if (isLinked(ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE)) {
                try {
                    if (fetchCapture) {
                        captureDaemon = monitorProxy.getCaptureDaemonStatus();
                        logger.debug(
                                "{}: context='fetchMonitorDaemonStatus' tag='captureDaemon' URL='{}' ResponseCode='{}' ResponseMessage='{}'",
                                getLogIdentifier(), captureDaemon.getHttpRequestUrl(), captureDaemon.getHttpStatus(),
                                captureDaemon.getHttpResponseMessage());
                        stateCapture = (captureDaemon.getStatus() ? OnOffType.ON : OnOffType.OFF);
                    }
                } catch (ZoneMinderResponseException zmre) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getCaptureDaemonStatus' - Http: Status='{}', Message='{}', ExceptionMessage='{}', Exception='{}', Message={}'",
                            getLogIdentifier(), zmre.getHttpStatus(), zmre.getHttpMessage(), zmre.getExceptionMessage(),
                            zmre.getClass().getCanonicalName(), zmre.getMessage(), zmre.getCause());
                } catch (ZoneMinderInvalidData zmid) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getCaptureDaemonStatus' - Response='{}',  Exception='{}', Message={}'",
                            getLogIdentifier(), zmid.getResponseString(), zmid.getClass().getCanonicalName(),
                            zmid.getMessage(), zmid.getCause());

                } catch (ZoneMinderGeneralException | ZoneMinderAuthenticationException zme) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getCaptureDaemonStatus' - Exception='{}', Message={}' ",
                            getLogIdentifier(), zme.getClass().getCanonicalName(), zme.getMessage(), zme.getCause());
                } finally {
                    if (captureDaemon != null) {
                        dataConverter.setMonitorCaptureDaemonStatus(stateCapture);
                    }

                }
            }

            if (isLinked(ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE)) {
                try {
                    stateAnalysis = UnDefType.UNDEF;
                    if (fetchAnalysisFrame) {
                        analysisDaemon = monitorProxy.getAnalysisDaemonStatus();
                        logger.debug(
                                "{}: context='onFetchData' tag='analysisDaemon' URL='{}' ResponseCode='{}' ResponseMessage='{}'",
                                getLogIdentifier(), analysisDaemon.getHttpRequestUrl(), analysisDaemon.getHttpStatus(),
                                analysisDaemon.getHttpResponseMessage());

                        stateAnalysis = (analysisDaemon.getStatus() ? OnOffType.ON : OnOffType.OFF);
                        fetchFrame = true;
                    }

                } catch (ZoneMinderResponseException zmre) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getAnalysisDaemonStatus' - Http: Status='{}', Message='{}', ExceptionMessage='{}', Exception='{}'",
                            getLogIdentifier(), zmre.getHttpStatus(), zmre.getHttpMessage(), zmre.getExceptionMessage(),
                            zmre.getClass().getCanonicalName(), zmre.getCause());
                } catch (ZoneMinderInvalidData zmid) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getAnalysisDaemonStatus' - Response='{}', Exception='{}'",
                            getLogIdentifier(), zmid.getResponseString(), zmid.getClass().getCanonicalName(),
                            zmid.getCause());

                } catch (ZoneMinderGeneralException | ZoneMinderAuthenticationException zme) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getAnalysisDaemonStatus' - Exception='{}' ",
                            getLogIdentifier(), zme.getClass().getCanonicalName(), zme.getCause());
                } catch (Exception ex) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' tag='exception' error in call to 'getAnalysisDaemonStatus' - Exception='{}'",
                            getLogIdentifier(), ex.getClass().getCanonicalName(), ex);
                } finally {
                    dataConverter.setMonitorAnalysisDaemonStatus(stateAnalysis);

                }
            }

            if (isLinked(ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE)) {
                try {
                    stateFrame = UnDefType.UNDEF;
                    if ((fetchFrame) && frameDaemonActive) {
                        frameDaemon = monitorProxy.getFrameDaemonStatus();
                        logger.debug(
                                "{}: context='fetchMonitorDaemonStatus' tag='frameDaemon' URL='{}' ResponseCode='{}' ResponseMessage='{}'",
                                getLogIdentifier(), frameDaemon.getHttpRequestUrl(), frameDaemon.getHttpStatus(),
                                frameDaemon.getHttpResponseMessage());

                        if (frameDaemon != null) {
                            stateFrame = ((frameDaemon.getStatus() && analysisDaemon.getStatus()) ? OnOffType.ON
                                    : OnOffType.OFF);
                        }
                    }
                } catch (ZoneMinderResponseException zmre) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getFrameDaemonStatus' - Http: Status='{}', Message='{}', ExceptionMessage'{}', Exception='{}'",
                            getLogIdentifier(), zmre.getHttpStatus(), zmre.getHttpMessage(), zmre.getExceptionMessage(),
                            zmre.getClass().getCanonicalName(), zmre.getCause());
                } catch (ZoneMinderInvalidData zmid) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getFrameDaemonStatus' - Response='{}', Exception='{}'",
                            getLogIdentifier(), zmid.getResponseString(), zmid.getClass().getCanonicalName(),
                            zmid.getCause());

                } catch (ZoneMinderGeneralException | ZoneMinderAuthenticationException zme) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' error in call to 'getFrameDaemonStatus' - Exception='{}'",
                            getLogIdentifier(), zme.getClass().getCanonicalName(), zme.getCause());
                } catch (Exception ex) {
                    logger.error(
                            "{}: context='fetchMonitorDaemonStatus' tag='exception' error in call to 'getFrameDaemonStatus' - Exception='{}'",
                            getLogIdentifier(), ex.getClass().getCanonicalName(), ex);
                } finally {
                    dataConverter.setMonitorFrameDaemonStatus(stateFrame);
                }
            }

        } finally {
            if (doRelase) {
                releaseSession();
            }
        }
    }

    /*
     * This is experimental
     * Try to add different properties
     */
    private void updateMonitorProperties() {
        logger.debug("{}: Update Monitor Properties", getLogIdentifier());
        // Update property information about this device
        Map<String, String> properties = editProperties();
        IZoneMinderMonitor monitorProxy = null;
        IMonitorDataGeneral monitorData = null;
        IZoneMinderConnectionHandler session = null;
        try {
            session = aquireSession();

            if (session == null) {
                logger.error("{}: context='updateMonitorProperties' Unable to aquire session.", getLogIdentifier());
                return;
            }
            monitorProxy = ZoneMinderFactory.getMonitorProxy(session, getZoneMinderId());
            monitorData = monitorProxy.getMonitorData();
            logger.debug("{}: URL='{}' ResponseCode='{}' ResponseMessage='{}'", getLogIdentifier(),
                    monitorData.getHttpRequestUrl(), monitorData.getHttpStatus(), monitorData.getHttpResponseMessage());

        } catch (Exception e) {
            logger.error("{}: Exception occurred when updating monitor properties - Message:{}", getLogIdentifier(),
                    e.getMessage());

        } catch (ZoneMinderException ex) {
            logger.error(
                    "{}: context='onFetchData' error in call to 'getMonitorData' ExceptionClass='{}' - Message='{}'",
                    getLogIdentifier(), ex.getClass().getCanonicalName(), ex.getMessage(), ex.getCause());
        } finally {
            if (session != null) {
                releaseSession();
            }
        }

        if (monitorData != null) {
            properties.put(ZoneMinderProperties.PROPERTY_ID, getLogIdentifier());
            properties.put(ZoneMinderProperties.PROPERTY_NAME, monitorData.getName());

            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_SOURCETYPE, monitorData.getSourceType().name());

            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_ANALYSIS_FPS, monitorData.getAnalysisFPS());
            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_MAXIMUM_FPS, monitorData.getMaxFPS());
            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_ALARM_MAXIMUM, monitorData.getAlarmMaxFPS());

            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_IMAGE_WIDTH, monitorData.getWidth());
            properties.put(ZoneMinderProperties.PROPERTY_MONITOR_IMAGE_HEIGHT, monitorData.getHeight());
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
            logger.debug("{}: context='updateMonitorProperties' Properties synchronised", getLogIdentifier());
            updateProperties(properties);
        }
    }

    @Override
    public String getLogIdentifier() {
        String result = "[MONITOR]";

        try {
            if (config != null) {
                result = String.format("[MONITOR-%s]", config.getZoneMinderId().toString());
            }

        } catch (Exception ex) {
            result = "[MONITOR]";
        }
        return result;
    }

    @Override
    public void onStateChanged(ChannelUID channelUID, State state) {
        logger.debug("{}: context='onStateChanged' channel='{}' - State changed to '{}'", getLogIdentifier(),
                channelUID.getAsString(), state.toString());
        updateState(channelUID.getId(), state);
    }

    @Override
    public void onRefreshDisabled() {
    }

    @Override
    public void onRefreshEnabled() {
    }

}
