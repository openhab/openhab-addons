/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zm.internal.handler;

import static org.openhab.binding.zm.internal.ZmBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zm.action.ZmActions;
import org.openhab.binding.zm.internal.config.ZmMonitorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZmMonitorHandler} represents a Zoneminder monitor. The monitor handler
 * interacts with the server bridge to communicate with the Zoneminder server.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmMonitorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZmMonitorHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    private @Nullable ZmBridgeHandler bridgeHandler;

    private @NonNullByDefault({}) String monitorId;
    private @Nullable Integer imageRefreshInterval;
    private Integer alarmDuration = DEFAULT_ALARM_DURATION_SECONDS;

    private @Nullable ScheduledFuture<?> imageRefreshJob;
    private @Nullable ScheduledFuture<?> alarmOffJob;

    public ZmMonitorHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        ZmMonitorConfig config = getConfigAs(ZmMonitorConfig.class);
        monitorId = config.monitorId;
        logger.debug("Monitor {}: Handler initializing", monitorId);
        imageRefreshInterval = config.imageRefreshInterval;
        Integer value = config.alarmDuration;
        alarmDuration = value != null ? value : DEFAULT_ALARM_DURATION_SECONDS;
        updateMonitorStatus();
        startImageRefreshJob();
    }

    @Override
    public void dispose() {
        logger.debug("Monitor {}: Handler disposing", monitorId);
        stopAlarmOffJob();
        turnAlarmOff();
        stopImageRefreshJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // No need to handle as status will be updated at the next refresh interval
            return;
        }
        logger.debug("Monitor {}: Received command '{}' for channel '{}'", monitorId, command, channelUID.getId());
        ZmBridgeHandler localHandler = bridgeHandler;
        if (localHandler == null) {
            logger.info("Monitor {}: Can't execute command because bridge handler is null", monitorId);
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_FUNCTION:
                if (command instanceof StringType) {
                    try {
                        MonitorFunction function = MonitorFunction.forValue(command.toString());
                        localHandler.setFunction(monitorId, function);
                        logger.debug("Monitor {}: Set monitor state to {}", monitorId, function);
                    } catch (IllegalArgumentException e) {
                        logger.debug("Monitor {}: Invalid function: {}", monitorId, command);
                    }
                }
                break;
            case CHANNEL_ENABLE:
                if (command instanceof OnOffType) {
                    localHandler.setEnabled(monitorId, (OnOffType) command);
                    logger.debug("Monitor {}: Set monitor enable to {}", monitorId, command);
                }
                break;
            case CHANNEL_TRIGGER_ALARM:
                if (command instanceof OnOffType) {
                    logger.debug("Monitor {}: Set monitor alarm to {}", monitorId, command);
                    if (command == OnOffType.ON) {
                        localHandler.setAlarmOn(monitorId);
                        startAlarmOffJob(alarmDuration.intValue());
                    } else {
                        stopAlarmOffJob();
                        localHandler.setAlarmOff(monitorId);
                    }
                }
                break;
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        logger.debug("Monitor {}: Detected bridge status changed to '{}', Update my status", monitorId, bridgeStatus);
        if (bridgeStatus == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge status unknown");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableList(Stream.of(ZmActions.class).collect(Collectors.toList()));
    }

    public String getId() {
        return monitorId;
    }

    public void actionTriggerAlarm(@Nullable Number duration) {
        if (duration == null) {
            return;
        }
        ZmBridgeHandler localHandler = bridgeHandler;
        if (localHandler != null) {
            logger.debug("Monitor {}: Action tell bridge to turn on alarm", monitorId);
            localHandler.setAlarmOn(monitorId);
            startAlarmOffJob(duration.intValue());
        }
    }

    public void actionTriggerAlarm() {
        actionTriggerAlarm(alarmDuration);
    }

    public void actionCancelAlarm() {
        ZmBridgeHandler localHandler = bridgeHandler;
        if (localHandler != null) {
            logger.debug("Monitor {}: Action tell bridge to turn off alarm", monitorId);
            stopAlarmOffJob();
            localHandler.setAlarmOff(monitorId);
        }
    }

    @SuppressWarnings("null")
    public void updateStatus(Monitor m) {
        logger.debug("Monitor {}: Updating: {}", m.getId(), m.toString());
        updateState(CHANNEL_ID, new StringType(m.getId()));
        updateState(CHANNEL_NAME, new StringType(m.getName()));
        updateState(CHANNEL_FUNCTION, new StringType(m.getFunction()));
        updateState(CHANNEL_ENABLE, m.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_HOUR_EVENTS, new DecimalType(m.getHourEvents()));
        updateState(CHANNEL_DAY_EVENTS, new DecimalType(m.getDayEvents()));
        updateState(CHANNEL_WEEK_EVENTS, new DecimalType(m.getWeekEvents()));
        updateState(CHANNEL_MONTH_EVENTS, new DecimalType(m.getMonthEvents()));
        updateState(CHANNEL_TOTAL_EVENTS, new DecimalType(m.getTotalEvents()));
        updateState(CHANNEL_IMAGE_URL, new StringType(m.getImageUrl()));
        updateState(CHANNEL_VIDEO_URL, new StringType(m.getVideoUrl()));
        updateState(CHANNEL_ALARM, m.isAlarm() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_STATE, new StringType(m.getState().toString()));
        if (!m.isAlarm()) {
            updateState(CHANNEL_TRIGGER_ALARM, m.isAlarm() ? OnOffType.ON : OnOffType.OFF);
        }
        Event event = m.getLastEvent();
        if (event == null) {
            clearEventChannels();
        } else if (event.getEnd() != null) {
            // If end is null, assume event hasn't completed yet
            logger.trace("Monitor {}: Id:{}, Frames:{}, AlarmFrames:{}, Length:{}", m.getId(), event.getId(),
                    event.getFrames(), event.getAlarmFrames(), event.getLength());
            updateState(CHANNEL_EVENT_ID, new StringType(event.getId()));
            updateState(CHANNEL_EVENT_NAME, new StringType(event.getName()));
            updateState(CHANNEL_EVENT_CAUSE, new StringType(event.getCause()));
            updateState(CHANNEL_EVENT_NOTES, new StringType(event.getNotes()));
            updateState(CHANNEL_EVENT_START, new DateTimeType(
                    ZonedDateTime.ofInstant(event.getStart().toInstant(), timeZoneProvider.getTimeZone())));
            updateState(CHANNEL_EVENT_END, new DateTimeType(
                    ZonedDateTime.ofInstant(event.getEnd().toInstant(), timeZoneProvider.getTimeZone())));
            updateState(CHANNEL_EVENT_FRAMES, new DecimalType(event.getFrames()));
            updateState(CHANNEL_EVENT_ALARM_FRAMES, new DecimalType(event.getAlarmFrames()));
            updateState(CHANNEL_EVENT_LENGTH, new QuantityType<Time>(event.getLength(), SmartHomeUnits.SECOND));
        }
    }

    private void clearEventChannels() {
        updateState(CHANNEL_EVENT_ID, UnDefType.NULL);
        updateState(CHANNEL_EVENT_NAME, UnDefType.NULL);
        updateState(CHANNEL_EVENT_CAUSE, UnDefType.NULL);
        updateState(CHANNEL_EVENT_NOTES, UnDefType.NULL);
        updateState(CHANNEL_EVENT_START, UnDefType.NULL);
        updateState(CHANNEL_EVENT_END, UnDefType.NULL);
        updateState(CHANNEL_EVENT_FRAMES, UnDefType.NULL);
        updateState(CHANNEL_EVENT_ALARM_FRAMES, UnDefType.NULL);
        updateState(CHANNEL_EVENT_LENGTH, UnDefType.NULL);
    }

    private void refreshImage() {
        if (isLinked(CHANNEL_IMAGE)) {
            scheduler.execute(this::getImage);
        } else {
            logger.trace("Monitor {}: Can't update image because '{}' channel is not linked", CHANNEL_IMAGE, monitorId);
        }
    }

    private void getImage() {
        try {
            ZmBridgeHandler localHandler = bridgeHandler;
            if (localHandler != null) {
                logger.debug("Monitor {}: Updating image channel", monitorId);
                RawType image = localHandler.getImage(monitorId, imageRefreshInterval);
                updateState(CHANNEL_IMAGE, image != null ? image : UnDefType.UNDEF);
            }
        } catch (RuntimeException e) {
            logger.debug("Monitor {}: Refresh image job got exception: {}", monitorId, e.getMessage(), e);
        }
    }

    private void updateMonitorStatus() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            logger.debug("Monitor {}: Set monitor status to match bridge status: {}", monitorId, bridge.getStatus());
            bridgeHandler = (ZmBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                updateStatus(bridge.getStatus());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Bridge handler does not exist");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge does not exist");
        }
    }

    private void startImageRefreshJob() {
        Integer interval = imageRefreshInterval;
        if (interval != null) {
            long delay = getRandomDelay(interval);
            imageRefreshJob = scheduler.scheduleWithFixedDelay(this::refreshImage, delay, interval, TimeUnit.SECONDS);
            logger.debug("Monitor {}: Scheduled image refresh job will run every {} seconds starting in {} seconds",
                    monitorId, interval, delay);
        }
    }

    private void stopImageRefreshJob() {
        ScheduledFuture<?> localImageRefreshJob = imageRefreshJob;
        if (localImageRefreshJob != null) {
            logger.debug("Monitor {}: Canceled image refresh job", monitorId);
            localImageRefreshJob.cancel(true);
            imageRefreshJob = null;
        }
    }

    private void turnAlarmOff() {
        ZmBridgeHandler localHandler = bridgeHandler;
        if (alarmOffJob != null && localHandler != null) {
            logger.debug("Monitor {}: Tell bridge to turn off alarm", monitorId);
            localHandler.setAlarmOff(monitorId);
        }
    }

    private void startAlarmOffJob(int duration) {
        stopAlarmOffJob();
        if (duration != 0) {
            alarmOffJob = scheduler.schedule(this::turnAlarmOff, duration, TimeUnit.SECONDS);
            logger.debug("Monitor {}: Scheduled alarm off job in {} seconds", monitorId, duration);
        }
    }

    private void stopAlarmOffJob() {
        ScheduledFuture<?> localAlarmOffJob = alarmOffJob;
        if (localAlarmOffJob != null) {
            logger.debug("Monitor {}: Canceled alarm off job", monitorId);
            localAlarmOffJob.cancel(true);
            alarmOffJob = null;
        }
    }

    private long getRandomDelay(int interval) {
        return System.currentTimeMillis() % interval;
    }
}
