/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.handler;

import java.util.EventObject;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.ZoneMinderMonitorEventListener;
import org.openhab.binding.zoneminder.internal.api.MonitorDaemonStatus;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderEvent;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderTelnetEvent;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderTrigger;
import org.openhab.binding.zoneminder.internal.config.ZoneMinderThingMonitorConfig;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderData;
import org.openhab.binding.zoneminder.internal.data.ZoneMinderMonitorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link ZoneMinderThingMonitorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderThingMonitorHandler extends ZoneMinderBaseThingHandler
        implements ZoneMinderMonitorEventListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets
            .newHashSet(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR);

    private static final String MONITOR_STATUS_NOT_INIT = "<Not Initialized>";
    private static final int MAX_MONITOR_STATUS_WATCH_COUNT = 3;

    private ZoneMinderMonitorData zoneMinderMonitorData = null;

    /** Make sure we can log errors, warnings or what ever somewhere */
    private Logger logger = LoggerFactory.getLogger(ZoneMinderThingMonitorHandler.class);

    private String lastMonitorStatus = MONITOR_STATUS_NOT_INIT;
    private Integer monitorStatusMatchCount = 3;
    private Boolean alive = false;

    private ZoneMinderThingMonitorConfig config;

    public ZoneMinderThingMonitorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
    }

    /** isAlive Method */
    @Override
    public Boolean isAlive() {
        return alive;
    }

    @Override
    public String getZoneMinderId() {
        if (config == null) {
            logger.error("Configuration for Thing '{}' is not loaded correctly.", getThing().getUID());
            return "";
        }
        return config.getZoneMinderId().toString();

    }

    protected ZoneMinderMonitorData getMonitorData() {
        return zoneMinderMonitorData;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            // Communication TO Monitor
            switch (channelUID.getId()) {

                // Done via Telnet connection
                case ZoneMinderConstants.CHANNEL_MONITOR_TRIGGER_EVENT:
                    logger.debug("Command '{}' received for an external trigger: {}", command, channelUID.getId());

                    if ((command == OnOffType.OFF) || (command == OnOffType.ON)) {
                        String eventText = getConfigValueAsString(ZoneMinderConstants.PARAMETER_MONITOR_EVENTTEXT);
                        Integer eventTimeout = getConfigValueAsInteger(
                                ZoneMinderConstants.PARAMETER_MONITOR_TRIGGER_TIMEOUT);
                        getZoneMinderBridgeHandler().sendZoneMinderTelnetRequest(ZoneMinderRequestType.MONITOR_TRIGGER,
                                ZoneMinderTrigger.create(getMonitorConfig().getZoneMinderId(), command, eventText,
                                        eventTimeout));
                    }
                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_ENABLED:
                    // getZoneMinderBridgeHandler().sendZoneMinderRequest(ZoneMinderRequestType.MONITOR_TRIGGER,
                    logger.debug(
                            "'handleCommand' => CHANNEL_MONITOR_ENABLED: Command '{}' received for monitor enabled: {}",
                            command, channelUID.getId());

                    break;

                case ZoneMinderConstants.CHANNEL_MONITOR_FUNCTION:
                    logger.debug(
                            "Missing implementation of set functionality in ZM for Command '{}' received for monitor mode: {}",
                            command, channelUID.getId());
                    break;

                // They are all readonly in the channel config.
                case ZoneMinderConstants.CHANNEL_MONITOR_NAME:
                case ZoneMinderConstants.CHANNEL_MONITOR_SOURCETYPE:
                case ZoneMinderConstants.CHANNEL_MONITOR_ONLINE:
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
            logger.error("handleCommand: Command='{}' failed for channel='{}'", command, channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZoneMinder handler.");
        updateStatus(ThingStatus.INITIALIZING);

        super.initialize();
        this.config = getMonitorConfig();
    }

    @Override
    public void notifyZoneMinderApiDataUpdated(ThingTypeUID thingTypeUID, String ZoneMinderId, ZoneMinderData data) {

        zoneMinderMonitorData = (ZoneMinderMonitorData) data;
    }

    @Override
    public void notifyZoneMinderEvent(ZoneMinderEvent event) {

        if (event.getZoneMinderMessage() instanceof ZoneMinderTelnetEvent) {

            ZoneMinderTelnetEvent incommingMsg = (ZoneMinderTelnetEvent) event.getZoneMinderMessage();

            // If event occurred in this ZoneMonitor monitor -> do something
            if (incommingMsg.getMonitorId() == config.getId()) {
                // Channel channel = this.getThing().getChannel(ZoneMinderConstants.CHANNEL_MONITOR_EVENT);
                Channel channel = this.getThing().getChannel(ZoneMinderConstants.CHANNEL_MONITOR_TRIGGER_EVENT);
                this.updateState(channel.getUID(), incommingMsg.getStateAsOnOffType());

                logger.debug("ZoneMinder event '{}' handled in ZoneMinderThingMonitorHandler",
                        incommingMsg.toCommandString());
            }
        }
    }

    protected ZoneMinderThingMonitorConfig getMonitorConfig() {
        return this.getConfigAs(ZoneMinderThingMonitorConfig.class);
    }

    @Override
    protected String getZoneMinderThingType() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

    @Override
    protected void checkIsAlive(MonitorDaemonStatus status) {
        ThingStatus newThingStatus = ThingStatus.OFFLINE;

        try {
            if (status == null) {
                monitorStatusMatchCount = 0;
                lastMonitorStatus = "";
                alive = false;
            } else {

                Pattern pattern = Pattern
                        .compile("[0-9]{2}/[0-9]{2}/[0-9]{2}\\s+([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
                String statusText = status.getStatustext();
                Matcher matcher = pattern.matcher(statusText);

                if (monitorStatusMatchCount > MAX_MONITOR_STATUS_WATCH_COUNT) {
                    monitorStatusMatchCount--;
                }

                if (matcher.find()) {
                    // logger.debug("IsAlive(): Found the text '{}' starting at index {} and ending at index {}.",
                    // matcher.group(), matcher.start(), matcher.end());

                    String currentMonitorStatus = status.getStatustext().substring(matcher.start(), matcher.end());
                    if (lastMonitorStatus.equals(currentMonitorStatus)) {
                        monitorStatusMatchCount++;
                    } else if (lastMonitorStatus.equals(MONITOR_STATUS_NOT_INIT)) {
                        // We have just started, so we will assume that the monitor is running (don't set match count
                        // to Zero
                        monitorStatusMatchCount++;
                        lastMonitorStatus = status.getStatustext().substring(matcher.start(), matcher.end());
                    } else {
                        monitorStatusMatchCount = 0;
                        lastMonitorStatus = status.getStatustext().substring(matcher.start(), matcher.end());
                    }
                }

                else {
                    monitorStatusMatchCount = 0;
                    lastMonitorStatus = "";
                    logger.debug("IsAlive(): No match found in status text.");
                }
                alive = (status.getStatus() && (monitorStatusMatchCount > MAX_MONITOR_STATUS_WATCH_COUNT));
            }
            newThingStatus = alive ? ThingStatus.ONLINE : ThingStatus.OFFLINE;
        } catch (Exception exception) {

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
        if (getMonitorData() == null) {
            return;
        }
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
                case ZoneMinderConstants.CHANNEL_MONITOR_ONLINE:
                    break;

                // Handled from Telnet listener, so just ignor it here.
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
        // TODO Auto-generated method stub

    }

    @Override
    public void ZoneMinderEventReceived(EventObject event, Thing thing) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateState(ZoneMinderConstants.CHANNEL_MONITOR_ONLINE,
                ((status == ThingStatus.ONLINE) ? OnOffType.ON : OnOffType.OFF));

    }

    protected State getNameState() {
        return new StringType(getMonitorData().getName());
    }

    protected State getFunctionState() {
        return new StringType(getMonitorData().getFunction());
    }

    protected State getSourceTypeState() {
        return new StringType(getMonitorData().getSourceType());
    }

    protected State getDiskUsageState() {
        return new DecimalType(0.0); // getMonitorData().getName());
    }

    protected State getEnabledState() {
        return ((getMonitorData().getEnabled().equalsIgnoreCase("1")) ? OnOffType.ON : OnOffType.OFF);
    }

    protected State getCaptureDaemonRunningState() {

        return (getMonitorData().getCaptureDaemonRunningState() ? OnOffType.ON : OnOffType.OFF);
    }

    protected State getCaptureDaemonStatusTextState() {
        return new StringType(getMonitorData().getCaptureDaemonStatusText());
    }

    protected State getAnalysisDaemonRunningState() {

        return (getMonitorData().getAnalysisDaemonRunningState() ? OnOffType.ON : OnOffType.OFF);
    }

    protected State getAnalysisDaemonStatusTextState() {
        return new StringType(getMonitorData().getAnalysisDaemonStatusText());
    }

    protected State getFrameDaemonRunningState() {

        return (getMonitorData().getFrameDaemonRunningState() ? OnOffType.ON : OnOffType.OFF);
    }

    protected State getFrameDaemonStatusTextState() {
        return new StringType(getMonitorData().getFrameDaemonStatusText());
    }

}
