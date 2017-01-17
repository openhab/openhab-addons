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
import java.util.EventObject;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
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
import org.openhab.binding.zoneminder.ZoneMinderMonitorProperties;
import org.openhab.binding.zoneminder.internal.DataRefreshPriorityEnum;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingMonitorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.IZoneMinderConnectionInfo;
import name.eskildsen.zoneminder.IZoneMinderMonitor;
import name.eskildsen.zoneminder.IZoneMinderMonitorData;
import name.eskildsen.zoneminder.ZoneMinderFactory;
import name.eskildsen.zoneminder.api.event.ZoneMinderEvent;
import name.eskildsen.zoneminder.api.telnet.ZoneMinderTriggerEvent;
import name.eskildsen.zoneminder.common.ZoneMinderMonitorFunctionEnum;
import name.eskildsen.zoneminder.common.ZoneMinderMonitorStatusEnum;
import name.eskildsen.zoneminder.exception.ZoneMinderUrlNotFoundException;
import name.eskildsen.zoneminder.trigger.ZoneMinderEventSubscriber;

/**
 * The {@link ZoneMinderThingMonitorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderThingMonitorHandler extends ZoneMinderBaseThingHandler
        implements ZoneMinderEventSubscriber/* , ZoneMinderMonitorEventListener */ {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR);

    private static final String MONITOR_STATUS_NOT_INIT = "<Not Initialized>";
    private static final int MAX_MONITOR_STATUS_WATCH_COUNT = 3;

    /** Make sure we can log errors, warnings or what ever somewhere */
    private Logger logger = LoggerFactory.getLogger(ZoneMinderThingMonitorHandler.class);

    private String lastMonitorStatus = MONITOR_STATUS_NOT_INIT;
    private Integer monitorStatusMatchCount = 3;

    private ZoneMinderThingMonitorConfig config;

    private Boolean _running = false;

    private ZoneMinderEvent curEvent = null;

    /**
     * Channels
     */
    private ZoneMinderMonitorFunctionEnum channelFunction = ZoneMinderMonitorFunctionEnum.NONE;
    private Boolean channelEnabled = false;
    private boolean channelRecordingState = false;
    private boolean channelAlarmedState = false;
    private String channelEventCause = "";
    private ZoneMinderMonitorStatusEnum channelMonitorStatus = ZoneMinderMonitorStatusEnum.UNKNOWN;
    private boolean channelDaemonCapture = false;
    private boolean channelDaemonAnalysis = false;
    private boolean channelDaemonFrame = false;
    private boolean channelForceAlarm = false;

    private int forceAlarmManualState = -1;

    public ZoneMinderThingMonitorHandler(Thing thing) {
        super(thing);

        logger.info("{}: Starting ZoneMinder Server Thing Handler (Thing='{}')", getLogIdentifier(), thing.getUID());
    }

    @Override
    public void dispose() {
    }

    protected String getLogIdentifier() {
        String result = String.format("MONITOR");
        if (config != null) {
            result = String.format("MONITOR-%s", config.getZoneMinderId());
        }
        return result;
    }

    @Override
    public String getZoneMinderId() {
        if (config == null) {
            logger.error("MONITOR: Configuration for Thing '{}' is not loaded correctly.", getThing().getUID());
            return "";
        }
        return config.getZoneMinderId().toString();

    }

    @Override
    public void onBridgeConnected(ZoneMinderServerBridgeHandler bridge, IZoneMinderConnectionInfo connection)
            throws IllegalArgumentException, GeneralSecurityException, IOException, ZoneMinderUrlNotFoundException {

        logger.debug("{}: Bridge '{}' connected", getLogIdentifier(), bridge.getThing().getUID().getAsString());
        super.onBridgeConnected(bridge, connection);

        ZoneMinderFactory.SubscribeMonitorEvents(config.getZoneMinderId(), this);

        updateMonitorProperties();
    }

    @Override
    public void onBridgeDisconnected(ZoneMinderServerBridgeHandler bridge) {
        logger.debug("{}: Bridge '{}' disconnected", getLogIdentifier(), bridge.getThing().getUID().getAsString());
        ZoneMinderFactory.UnsubscribeMonitorEvents(config.getZoneMinderId(), this);
        super.onBridgeDisconnected(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {

            logger.debug("{}: Channel '{}' in monitor '{}' received command='{}'", getLogIdentifier(), channelUID,
                    getZoneMinderId(), command);

            // Allow refresh of channels
            if (command == RefreshType.REFRESH) {
                updateChannel(channelUID);
                return;
            }
            IZoneMinderMonitor monitorProxy = ZoneMinderFactory.getMonitorProxy(getZoneMinderId());
            // Communication TO Monitor
            switch (channelUID.getId()) {

                // Done via Telnet connection
                case ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM:
                    logger.debug(
                            "'handleCommand' => CHANNEL_MONITOR_FORCE_ALARM: Command '{}' received for monitor '{}'",
                            command, channelUID.getId());

                    if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                        String eventText = getConfigValueAsString(ZoneMinderConstants.PARAMETER_MONITOR_EVENTTEXT);

                        BigDecimal eventTimeout = getConfigValueAsBigDecimal(
                                ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT);

                        ZoneMinderServerBridgeHandler bridge = getZoneMinderBridgeHandler();
                        if (bridge == null) {
                            logger.warn("'handleCommand()': Bridge is 'null'!");
                        }

                        if (command == OnOffType.ON) {
                            forceAlarmManualState = 1;
                            logger.info("{}: Activate 'ForceAlarm' to '{}' (Reason='{}', Timeout='{}')",
                                    getLogIdentifier(), command, eventText, eventTimeout.intValue());

                            monitorProxy.activateForceAlarm(255, ZoneMinderConstants.MONITOR_EVENT_OPENHAB, eventText,
                                    "", eventTimeout.intValue());

                        }

                        else if (command == OnOffType.OFF) {
                            forceAlarmManualState = 0;
                            logger.info("{}: Cancel 'ForceAlarm'", getLogIdentifier());
                            monitorProxy.deactivateForceAlarm();

                        }
                        RecalculateChannelStates();

                        handleCommand(channelUID, RefreshType.REFRESH);
                        handleCommand(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE),
                                RefreshType.REFRESH);
                        handleCommand(getChannelUIDFromChannelId(ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE),
                                RefreshType.REFRESH);

                        // Force a refresh
                        startPriorityRefresh();

                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    logger.debug(
                            "{}: 'handleCommand' => CHANNEL_MONITOR_ENABLED: Command '{}' received for monitor '{}'",
                            getLogIdentifier(), command, channelUID.getId());

                    if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                        boolean newState = ((command == OnOffType.ON) ? true : false);
                        monitorProxy.SetEnabled(newState);
                        channelEnabled = newState;

                        logger.info("{}: Setting enabled to '{}'", getLogIdentifier(), command);
                    }

                    handleCommand(channelUID, RefreshType.REFRESH);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    String commandString = "";
                    if (ZoneMinderMonitorFunctionEnum.isValid(command.toString())) {

                        commandString = ZoneMinderMonitorFunctionEnum.getEnum(command.toString()).toString();
                        ZoneMinderServerBridgeHandler bridge = getZoneMinderBridgeHandler();

                        monitorProxy.SetFunction(commandString);

                        // Make sure local copy is set to new value
                        channelFunction = ZoneMinderMonitorFunctionEnum.getEnum(command.toString());

                        logger.info("{}: Setting function to '{}'", getLogIdentifier(), commandString);

                    } else {
                        logger.error(
                                "{}: Value '{}' for monitor channel is not valid. Accepted values is: 'None', 'Monitor', 'Modect', Record', 'Mocord', 'Nodect'",
                                getLogIdentifier(), commandString);
                    }
                    handleCommand(channelUID, RefreshType.REFRESH);
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
            logger.error("{}: handleCommand: Command='{}' failed for channel='{}' Exception='{}'", getLogIdentifier(),
                    command, channelUID.getId(), ex.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("MONITOR-{}: Initializing ZoneMinder handler.");

        super.initialize();
        this.config = getMonitorConfig();
        logger.debug("MONITOR-{}: ZoneMinder Monitor Handler Initialized", getLogIdentifier());
        logger.debug("MONITOR-{}:    Monitor Id:         {}", getLogIdentifier(), config.getZoneMinderId());

    }

    @Override
    public void onTrippedForceAlarm(ZoneMinderTriggerEvent event) {
        logger.info("{}: Received forceAlarm for monitor {}", getLogIdentifier(), event.getMonitorId());
        Channel channel = this.getThing().getChannel(ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS);
        Channel chEventCause = this.getThing().getChannel(ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE);

        // Set Current Event to actual event
        if (event.getState()) {
            // 2017.01.09 FIXME :: curEvent = monitorProxy.getLastEvent();
            startPriorityRefresh();

        } else {
            curEvent = null;
        }

    }

    protected ZoneMinderThingMonitorConfig getMonitorConfig() {
        return this.getConfigAs(ZoneMinderThingMonitorConfig.class);
    }

    @Override
    protected String getZoneMinderThingType() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

    private Boolean isDaemonRunning(Boolean daemonStatus, String daemonStatusText) {
        Boolean result = false;

        Pattern pattern = Pattern
                .compile("[0-9]{2}/[0-9]{2}/[0-9]{2}\\s+([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");

        Matcher matcher = pattern.matcher(daemonStatusText);

        if (matcher.find()) {

            String currentMonitorStatus = daemonStatusText.substring(matcher.start(), matcher.end());
            if (lastMonitorStatus.equals(currentMonitorStatus)) {
                monitorStatusMatchCount++;
            } else if (lastMonitorStatus.equals(MONITOR_STATUS_NOT_INIT)) {
                // We have just started, so we will assume that the monitor is running (don't set match count
                // to Zero)
                monitorStatusMatchCount++;
                lastMonitorStatus = daemonStatusText.substring(matcher.start(), matcher.end());
            } else {
                monitorStatusMatchCount = 0;
                lastMonitorStatus = daemonStatusText.substring(matcher.start(), matcher.end());
            }
        }

        else {
            monitorStatusMatchCount = 0;
            lastMonitorStatus = "";
            logger.debug("MONITOR-{}: Online(): No match found in status text.", getLogIdentifier());
        }
        return daemonStatus;
    }

    @Override
    public void updateAvaliabilityStatus(IZoneMinderConnectionInfo connection) {
        ThingStatus newThingStatus = ThingStatus.OFFLINE;
        ThingStatus curRhingStatus = this.getThing().getStatus();

        boolean connectionStatus = false;
        try {
            connectionStatus = ZoneMinderFactory.validateConnection(connection);
        } catch (IllegalArgumentException e) {
            logger.error("{}: validateConnection failed with exceptin='{}'", getLogIdentifier(), e.getMessage());
        }

        try {
            String msg;
            Bridge b = getBridge();

            // 1. Is there a Bridge assigned?
            if (getBridge() == null) {
                msg = String.format("No Bridge assigned to monitor '%s'", thing.getUID());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug("{}: ThingAvailability: Thing '{}' has Bridge '{}' defined (Check PASSED)",
                        getLogIdentifier(), thing.getUID(), getBridge().getBridgeUID());
            }

            // 2. Is Bridge Online?
            if (getBridge().getStatus() != ThingStatus.ONLINE) {
                msg = String.format("Bridge '%s' is OFFLINE", getBridge().getBridgeUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug("{}: ThingAvailability: Bridge '{}' is ONLINE (Check PASSED)", getLogIdentifier(),
                        getBridge().getBridgeUID());
            }

            // 3. Is Configuration OK?
            if (getMonitorConfig() == null) {
                msg = String.format("No valid configuration found for '%s'", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug("{}: ThingAvailability: Thing '{}' has valid configuration (Check PASSED)",
                        getLogIdentifier(), thing.getUID());
            }

            // ZoneMinder Id for Monitor not set, we are pretty much lost then
            if (getMonitorConfig().getZoneMinderId().isEmpty()) {
                msg = String.format("No Id is specified for monitor '%s'", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                logger.error("{}: {}", getLogIdentifier(), msg);
                return;
            } else {
                logger.debug("{}: ThingAvailability: ZoneMinder Id for Thing '{}' defined (Check PASSED)",
                        getLogIdentifier(), thing.getUID());
            }

            isAlive = true;
            newThingStatus = ThingStatus.ONLINE;

        } catch (Exception exception) {

            logger.error("{}: 'ThingMonitorHandler.updateAvailabilityStatus()': Exception occurred '{}'",
                    getLogIdentifier(), exception.getMessage());
        } finally {
            if (this.thing.getStatus() != newThingStatus) {
                logger.info("{}: Status changed to '{}'", getLogIdentifier(), newThingStatus.name());

                updateStatus(newThingStatus);
            }
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
        State state = null;

        try {
            switch (channel.getId()) {
                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    state = getChannelBoolAsOnOffState(channelEnabled);
                    break;

                case ZoneMinderConstants.CHANNEL_ONLINE:
                    // Ask super class to handle, because this channel is shared for all things
                    super.updateChannel(channel);
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_FORCE_ALARM:
                    state = getChannelBoolAsOnOffState(channelForceAlarm);
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_STATE:
                    state = getChannelBoolAsOnOffState(channelAlarmedState);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_RECORD_STATE:
                    state = getChannelBoolAsOnOffState(channelRecordingState);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_DETAILED_STATUS:
                    state = getDetailedStatus();
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_EVENT_CAUSE:
                    state = getChannelStringAsStringState(channelEventCause);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    state = getChannelStringAsStringState(channelFunction.toString());
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE:
                    state = getChannelBoolAsOnOffState(channelDaemonCapture);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE:
                    state = getChannelBoolAsOnOffState(channelDaemonAnalysis);
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE:
                    state = getChannelBoolAsOnOffState(channelDaemonFrame);
                    break;

                default:
                    logger.warn("{}: updateChannel(): Monitor '{}': No handler defined for channel='{}'",
                            getLogIdentifier(), thing.getLabel(), channel.getAsString());

                    // Ask super class to handle
                    super.updateChannel(channel);
            }

            if (state != null) {

                logger.debug("{}: Setting channel '{}' to '{}'", getLogIdentifier(), channel.toString(),
                        state.toString());
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            logger.error("{}: Error when 'updateChannel' was called (channelId='{}'state='{}', exception'{}')",
                    getLogIdentifier(), channel.toString(), state.toString(), ex.getMessage());
        }
    }

    @Override
    public void updateProperties(ChannelUID channelUID, int state, String description) {

    }

    @Override
    public void ZoneMinderEventReceived(EventObject event, Thing thing) {

    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateState(ZoneMinderConstants.CHANNEL_ONLINE,
                ((status == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF));

    }

    protected void RecalculateChannelStates() {
        boolean recordingFunction = false;
        boolean recordingDetailedState = false;
        boolean alarmedFunction = false;
        boolean alarmedDetailedState = false;

        // Calculate based on state of Function
        switch (channelFunction) {
            case NONE:
            case MONITOR:
                alarmedFunction = false;
                recordingFunction = false;
                break;

            case MODECT:
                alarmedFunction = true;
                recordingFunction = true;
                break;
            case RECORD:
                alarmedFunction = false;
                recordingFunction = true;
                break;
            case MOCORD:
                alarmedFunction = true;
                recordingFunction = true;
                break;
            case NODECT:
                alarmedFunction = false;
                recordingFunction = true;
                break;
            default:
                recordingFunction = (curEvent != null) ? true : false;
        }
        logger.debug(
                "{}: Recalculate channel states based on Function: Function='{}' -> alarmState='{}', recordingState='{}'",
                getLogIdentifier(), channelFunction.name(), alarmedFunction, recordingFunction);

        // Calculated based on detailed Monitor Status
        switch (channelMonitorStatus) {
            case IDLE:
                alarmedDetailedState = false;
                recordingDetailedState = false;
                channelForceAlarm = false;
                channelEventCause = "";
                break;

            case PRE_ALARM:
                alarmedDetailedState = true;
                recordingDetailedState = true;
                channelForceAlarm = false;
                break;

            case ALARM:
                alarmedDetailedState = true;
                recordingDetailedState = true;
                channelForceAlarm = true;
                break;

            case ALERT:
                alarmedDetailedState = true;
                recordingDetailedState = true;
                channelForceAlarm = false;
                break;

            case RECORDING:
                alarmedDetailedState = false;
                recordingDetailedState = true;
                channelForceAlarm = false;
                break;
        }
        logger.debug(
                "{}: Recalculate channel states based on Detailed State: DetailedState='{}' -> alarmState='{}', recordingState='{}'",
                getLogIdentifier(), channelMonitorStatus.name(), alarmedDetailedState, recordingDetailedState);

        // Check if Force alarm was initialed from openHAB
        if (forceAlarmManualState == 0) {
            if (channelForceAlarm) {
                channelForceAlarm = false;
            } else {
                forceAlarmManualState = -1;
            }
        } else if (forceAlarmManualState == 1) {

            if (channelForceAlarm == false) {
                channelForceAlarm = true;
            } else {
                forceAlarmManualState = -1;
            }

        }

        // Now we can conclude on the Alarmed and Recording channel state
        channelRecordingState = (recordingFunction && recordingDetailedState && channelEnabled);
        channelAlarmedState = (alarmedFunction && alarmedDetailedState && channelEnabled);

    }

    @Override
    protected void onFetchData() {

        IZoneMinderMonitor monitorProxy = ZoneMinderFactory.getMonitorProxy(getZoneMinderId());
        IZoneMinderMonitorData data = monitorProxy.getMonitorData();

        if (isConnected()) {
            channelMonitorStatus = monitorProxy.getMonitorDetailedStatus();
            channelFunction = data.getFunction();
            channelEnabled = data.getEnabled();
            channelEventCause = monitorProxy.getLastEvent().getCause();
            channelDaemonCapture = monitorProxy.getCaptureDaemonStatus().getStatus();
            channelDaemonAnalysis = monitorProxy.getAnalysisDaemonStatus().getStatus();
            channelDaemonFrame = monitorProxy.getFrameDaemonStatus().getStatus();
        } else {
            channelMonitorStatus = ZoneMinderMonitorStatusEnum.UNKNOWN;
            channelFunction = ZoneMinderMonitorFunctionEnum.NONE;
            channelEnabled = false;
            channelEventCause = "";
            channelDaemonCapture = false;
            channelDaemonAnalysis = false;
            channelDaemonFrame = false;
        }

        RecalculateChannelStates();

        if ((channelForceAlarm == false) && (channelAlarmedState == false)
                && (DataRefreshPriorityEnum.HIGH_PRIORITY == getRefreshPriority())) {
            stopPriorityRefresh();
        }

    }

    protected State getDetailedStatus() {
        State state = UnDefType.UNDEF;

        try {
            if (channelMonitorStatus == ZoneMinderMonitorStatusEnum.UNKNOWN) {
                state = getChannelStringAsStringState("");
            } else {
                state = getChannelStringAsStringState(channelMonitorStatus.toString());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;

    }

    /*
     * This is experimental
     * Try to add different properties
     */
    private void updateMonitorProperties() {
        // Update property information about this device
        Map<String, String> properties = editProperties();
        IZoneMinderMonitor monitorProxy = ZoneMinderFactory.getMonitorProxy(getZoneMinderId());
        IZoneMinderMonitorData monitorData = monitorProxy.getMonitorData();

        properties.put(ZoneMinderMonitorProperties.PROPERTY_ID, getLogIdentifier());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_NAME, monitorData.getName());

        properties.put(ZoneMinderMonitorProperties.PROPERTY_SOURCETYPE, monitorData.getSourceType().name());

        properties.put(ZoneMinderMonitorProperties.PROPERTY_ANALYSIS_FPS, monitorData.getAnalysisFPS());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_MAXIMUM_FPS, monitorData.getMaxFPS());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_ALARM_MAXIMUM, monitorData.getAlarmMaxFPS());

        properties.put(ZoneMinderMonitorProperties.PROPERTY_IMAGE_WIDTH, monitorData.getWidth());
        properties.put(ZoneMinderMonitorProperties.PROPERTY_IMAGE_HEIGHT, monitorData.getHeight());

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
            logger.debug("{}: Properties synchronised", getLogIdentifier());
            updateProperties(properties);
        }
    }
}
