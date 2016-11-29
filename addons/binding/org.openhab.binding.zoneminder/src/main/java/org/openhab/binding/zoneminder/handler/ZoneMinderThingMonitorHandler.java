/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.math.BigDecimal;
import java.util.EventObject;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingMonitorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import name.eskildsen.zoneminder.api.daemon.ZoneMinderMonitorAnalysisDaemonStatus;
import name.eskildsen.zoneminder.api.daemon.ZoneMinderMonitorCaptureDaemonStatus;
import name.eskildsen.zoneminder.api.daemon.ZoneMinderMonitorFrameDaemonStatus;
import name.eskildsen.zoneminder.api.monitor.ZoneMinderMonitor;
import name.eskildsen.zoneminder.api.telnet.ZoneMinderTriggerEvent;
import name.eskildsen.zoneminder.trigger.ZoneMinderTriggerSubscriber;

/**
 * The {@link ZoneMinderThingMonitorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderThingMonitorHandler extends ZoneMinderBaseThingHandler
        implements ZoneMinderTriggerSubscriber, ZoneMinderMonitorEventListener {

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

    public ZoneMinderThingMonitorHandler(Thing thing) {
        super(thing);

        logger.info("Starting ZoneMinder Server Thing Handler (Thing='{}')", thing.getUID());
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getZoneMinderId() {
        if (config == null) {
            logger.error("Configuration for Thing '{}' is not loaded correctly.", getThing().getUID());
            return "";
        }
        return config.getZoneMinderId().toString();

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            logger.debug("Channel '{}' in monitor '{}' received command='{}'", channelUID, getZoneMinderId(), command);

            // Allow refresh of channels
            if (command == RefreshType.REFRESH) {
                updateChannel(channelUID);
                return;
            }

            // Communication TO Monitor
            switch (channelUID.getId()) {

                // Done via Telnet connection
                case ZoneMinderConstants.CHANNEL_MONITOR_TRIGGER_EVENT:

                    if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                        String eventText = getConfigValueAsString(ZoneMinderConstants.PARAMETER_MONITOR_EVENTTEXT);

                        BigDecimal eventTimeout = getConfigValueAsBigDecimal(
                                ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT);

                        ZoneMinderServerBridgeHandler bridge = getZoneMinderBridgeHandler();
                        if (bridge == null) {
                            logger.warn("'handleCommand()': Bridge is 'null'!");
                        }

                        if (command == OnOffType.ON) {
                            logger.debug(String.format(
                                    "Activate 'ForceAlarm' for monitor '%s' (Reason='%s', Timeout='%d'), from OpenHAB in ZoneMinder",
                                    getZoneMinderId(), eventText, eventTimeout.intValue()));
                            bridge.activateZoneMinderMonitorTrigger(getZoneMinderId(), eventText,
                                    eventTimeout.intValue());
                        }

                        else if (command == OnOffType.OFF) {
                            logger.debug(
                                    String.format("Cancel 'ForceAlarm' for monitor '%s', from OpenHAB in ZoneMinder",
                                            getZoneMinderId()));
                            bridge.cancelZoneMinderMonitorTrigger(getZoneMinderId());
                        }
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                        logger.debug(
                                "'handleCommand' => CHANNEL_MONITOR_ENABLED: Command '{}' received for monitor enabled: {}",
                                command, channelUID.getId());
                        ZoneMinderServerBridgeHandler bridge = (ZoneMinderServerBridgeHandler) getZoneMinderBridgeHandler();
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    logger.warn(
                            "Missing implementation of set functionality in ZM for Command '{}' received for monitor mode: {}",
                            command, channelUID.getId());
                    break;

                // They are all readonly in the channel config.
                case ZoneMinderConstants.CHANNEL_MONITOR_NAME:
                case ZoneMinderConstants.CHANNEL_MONITOR_SOURCETYPE:
                case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATUSTEXT:
                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATUSTEXT:
                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE:
                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATUSTEXT:
                    // Do nothing, they are all read only
                    break;
                default:
                    logger.warn("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } catch (Exception ex) {
            logger.error("handleCommand: Command='{}' failed for channel='{}' Exception='{}'", command,
                    channelUID.getId(), ex.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZoneMinder handler.");

        super.initialize();
        this.config = getMonitorConfig();

    }

    @Override
    public void onTrippedForceAlarm(ZoneMinderTriggerEvent event) {
        logger.debug(String.format("Tripped forceAlarm for monitor {}", event.getMonitorId()));
        Channel channel = this.getThing().getChannel(ZoneMinderConstants.CHANNEL_MONITOR_TRIGGER_EVENT);
        this.updateState(channel.getUID(), event.getState() ? OnOffType.ON : OnOffType.OFF);
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
                // to Zero
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
            logger.debug("IsAlive(): No match found in status text.");
        }
        return daemonStatus;
    }

    @Override
    public void updateAvaliabilityStatus() {
        ThingStatus newThingStatus = ThingStatus.OFFLINE;

        try {
            String msg;
            // 1. Is there a Bridge assigned?
            if (getBridge() == null) {
                msg = String.format("No Bridge assigned to monitor '{}'", thing.getUID());

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                logger.error(msg);
                return;
            }

            // 2. Is Bridge Online?
            if (getBridge().getStatus() != ThingStatus.ONLINE) {
                msg = String.format("Bridge {} is OFFLINE", getBridge().getBridgeUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, msg);
                logger.error(msg);
                return;
            }

            // 3. Is Configuration OK?
            if (getMonitorConfig() == null) {
                msg = String.format("No valid configuration found for {}", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                logger.error(msg);
                return;

            }

            if (getMonitorConfig().getZoneMinderId().isEmpty()) {
                msg = String.format("No Id is specified for monitor {}", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                logger.error(msg);
                return;

            }

            isAlive = true;
            newThingStatus = isAlive ? ThingStatus.ONLINE : ThingStatus.OFFLINE;

        } catch (

        Exception exception) {

            logger.error("'ThingMonitorHandler.updateAvailabilityStatus()': Exception occurred '{}'",
                    exception.getMessage());
        } finally {
            if (this.thing.getStatus() != newThingStatus) {
                updateStatus(newThingStatus);
            }
        }
    }

    /*
     * From here we update states in OpenHAB
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
                case ZoneMinderConstants.CHANNEL_MONITOR_NAME:
                    state = getNameState();
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    state = getEnabledState();
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_SOURCETYPE:
                    state = getSourceTypeState();
                    break;

                case ZoneMinderConstants.CHANNEL_IS_ALIVE:
                    // Ask super class to handle, because this is shared for all things
                    super.updateChannel(channel);
                    break;

                // Handled from Telnet listener, so just ignore it here.
                // The handler has to be here to avoid a warning in the OpenHAB log
                case ZoneMinderConstants.CHANNEL_MONITOR_TRIGGER_EVENT:
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    state = getFunctionState();
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATE:
                    state = getCaptureDaemonRunningState();
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_CAPTURE_DAEMON_STATUSTEXT:
                    state = getCaptureDaemonStatusTextState();
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE:
                    state = getAnalysisDaemonRunningState();
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_ANALYSIS_DAEMON_STATUSTEXT:
                    state = getAnalysisDaemonStatusTextState();
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATE:
                    state = getFrameDaemonRunningState();
                    break;
                case ZoneMinderConstants.CHANNEL_MONITOR_FRAME_DAEMON_STATUSTEXT:
                    state = getFrameDaemonStatusTextState();
                    break;

                default:
                    logger.warn("updateChannel(): Monitor '{}': No handler defined for channel='{}'", thing.getLabel(),
                            channel.getAsString());

                    // Ask super class to handle
                    super.updateChannel(channel);
            }

            if (state != null) {
                updateState(channel.getId(), state);
            }
        } catch (Exception ex) {
            logger.error("Error occurred when 'updateChannel' was called for channel='{}'", channel.getId());
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
        updateState(ZoneMinderConstants.CHANNEL_IS_ALIVE,
                ((status == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF));

    }

    protected State getNameState() {
        State state = new StringType("");

        try {
            ZoneMinderMonitor data = (ZoneMinderMonitor) getZoneMinderData(ZoneMinderMonitor.class);
            if (data != null) {
                state = new StringType(data.getName());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getFunctionState() {
        State state = new StringType("");

        try {
            ZoneMinderMonitor data = (ZoneMinderMonitor) getZoneMinderData(ZoneMinderMonitor.class);
            if (data != null) {
                state = new StringType(data.getFunction());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getSourceTypeState() {
        State state = new StringType("");

        try {
            ZoneMinderMonitor data = (ZoneMinderMonitor) getZoneMinderData(ZoneMinderMonitor.class);
            if (data != null) {
                state = new StringType(data.getSourceType());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getEnabledState() {
        State state = OnOffType.OFF;

        try {
            ZoneMinderMonitor data = (ZoneMinderMonitor) getZoneMinderData(ZoneMinderMonitor.class);
            if (data != null) {
                state = data.getEnabled() ? OnOffType.ON : OnOffType.OFF;
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;

    }

    protected State getCaptureDaemonRunningState() {
        State state = OnOffType.OFF;

        try {
            ZoneMinderMonitorCaptureDaemonStatus data = (ZoneMinderMonitorCaptureDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorCaptureDaemonStatus.class);
            if (data != null) {
                state = data.getStatus() ? OnOffType.ON : OnOffType.OFF;
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getCaptureDaemonStatusTextState() {

        State state = new StringType("");

        try {
            ZoneMinderMonitorCaptureDaemonStatus data = (ZoneMinderMonitorCaptureDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorCaptureDaemonStatus.class);
            if (data != null) {
                state = new StringType(data.getStatusText());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }
        return state;
    }

    protected State getAnalysisDaemonRunningState() {
        State state = OnOffType.OFF;

        try {
            ZoneMinderMonitorAnalysisDaemonStatus data = (ZoneMinderMonitorAnalysisDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorAnalysisDaemonStatus.class);
            if (data != null) {
                state = data.getStatus() ? OnOffType.ON : OnOffType.OFF;
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getAnalysisDaemonStatusTextState() {
        State state = new StringType("");

        try {

            ZoneMinderMonitorAnalysisDaemonStatus data = (ZoneMinderMonitorAnalysisDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorAnalysisDaemonStatus.class);
            if (data != null) {
                state = new StringType(data.getStatusText());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getFrameDaemonRunningState() {

        State state = OnOffType.OFF;

        try {
            ZoneMinderMonitorFrameDaemonStatus data = (ZoneMinderMonitorFrameDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorFrameDaemonStatus.class);
            if (data != null) {
                state = data.getStatus() ? OnOffType.ON : OnOffType.OFF;
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

    protected State getFrameDaemonStatusTextState() {
        State state = new StringType("");

        try {
            ZoneMinderMonitorFrameDaemonStatus data = (ZoneMinderMonitorFrameDaemonStatus) getZoneMinderData(
                    ZoneMinderMonitorFrameDaemonStatus.class);
            if (data != null) {
                state = new StringType(data.getStatusText());
            }

        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

        return state;
    }

}
