/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.handler;

import static org.openhab.binding.zoneminder.internal.ZmBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zoneminder.internal.action.ZmActions;
import org.openhab.binding.zoneminder.internal.config.ZmMonitorConfig;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private @Nullable Integer imageRefreshIntervalSeconds;
    private Integer alarmDuration = DEFAULT_ALARM_DURATION_SECONDS;

    private @Nullable ScheduledFuture<?> imageRefreshJob;
    private @Nullable ScheduledFuture<?> alarmOffJob;

    private final Map<String, State> monitorStatusCache = new ConcurrentHashMap<>();

    public ZmMonitorHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        ZmMonitorConfig config = getConfigAs(ZmMonitorConfig.class);
        monitorId = config.monitorId;
        imageRefreshIntervalSeconds = config.imageRefreshInterval;
        Integer value = config.alarmDuration;
        alarmDuration = value != null ? value : DEFAULT_ALARM_DURATION_SECONDS;
        bridgeHandler = (ZmBridgeHandler) getBridge().getHandler();
        monitorStatusCache.clear();
        updateStatus(ThingStatus.ONLINE);
        startImageRefreshJob();
    }

    @Override
    public void dispose() {
        stopAlarmOffJob();
        turnAlarmOff();
        stopImageRefreshJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = monitorStatusCache.get(channelUID.getId());
            if (state != null) {
                updateState(channelUID, state);
            }
            return;
        }
        logger.debug("Monitor {}: Received command '{}' for channel '{}'", monitorId, command, channelUID.getId());
        ZmBridgeHandler localHandler = bridgeHandler;
        if (localHandler == null) {
            logger.warn("Monitor {}: Can't execute command because bridge handler is null", monitorId);
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
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ZmActions.class);
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
        logger.debug("Monitor {}: Updating Monitor: {}", m.getId(), m);
        updateChannelState(CHANNEL_ID, new StringType(m.getId()));
        updateChannelState(CHANNEL_NAME, new StringType(m.getName()));
        updateChannelState(CHANNEL_FUNCTION, new StringType(m.getFunction()));
        updateChannelState(CHANNEL_ENABLE, m.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_HOUR_EVENTS, new DecimalType(m.getHourEvents()));
        updateChannelState(CHANNEL_DAY_EVENTS, new DecimalType(m.getDayEvents()));
        updateChannelState(CHANNEL_WEEK_EVENTS, new DecimalType(m.getWeekEvents()));
        updateChannelState(CHANNEL_MONTH_EVENTS, new DecimalType(m.getMonthEvents()));
        updateChannelState(CHANNEL_TOTAL_EVENTS, new DecimalType(m.getTotalEvents()));
        updateChannelState(CHANNEL_IMAGE_URL, new StringType(m.getImageUrl()));
        updateChannelState(CHANNEL_VIDEO_URL, new StringType(m.getVideoUrl()));
        updateChannelState(CHANNEL_ALARM, m.isAlarm() ? OnOffType.ON : OnOffType.OFF);
        updateChannelState(CHANNEL_STATE, new StringType(m.getState().toString()));
        if (!m.isAlarm()) {
            updateChannelState(CHANNEL_TRIGGER_ALARM, m.isAlarm() ? OnOffType.ON : OnOffType.OFF);
        }
        Event event = m.getMostRecentCompletedEvent();
        if (event == null) {
            // No most recent event, so clear out the event channels
            clearEventChannels();
            return;
        }
        // Update channels for most recent completed event
        logger.debug("Monitor {}: Updating Event Id:{}, Name:{}, Frames:{}, AlarmFrames:{}, Length:{}", m.getId(),
                event.getId(), event.getName(), event.getFrames(), event.getAlarmFrames(), event.getLength());
        updateChannelState(CHANNEL_EVENT_ID, new StringType(event.getId()));
        updateChannelState(CHANNEL_EVENT_NAME, new StringType(event.getName()));
        updateChannelState(CHANNEL_EVENT_CAUSE, new StringType(event.getCause()));
        updateChannelState(CHANNEL_EVENT_NOTES, new StringType(event.getNotes()));
        updateChannelState(CHANNEL_EVENT_START, new DateTimeType(
                ZonedDateTime.ofInstant(event.getStart().toInstant(), timeZoneProvider.getTimeZone())));
        updateChannelState(CHANNEL_EVENT_END,
                new DateTimeType(ZonedDateTime.ofInstant(event.getEnd().toInstant(), timeZoneProvider.getTimeZone())));
        updateChannelState(CHANNEL_EVENT_FRAMES, new DecimalType(event.getFrames()));
        updateChannelState(CHANNEL_EVENT_ALARM_FRAMES, new DecimalType(event.getAlarmFrames()));
        updateChannelState(CHANNEL_EVENT_LENGTH, new QuantityType<Time>(event.getLength(), Units.SECOND));
    }

    private void clearEventChannels() {
        updateChannelState(CHANNEL_EVENT_ID, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_NAME, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_CAUSE, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_NOTES, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_START, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_END, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_FRAMES, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_ALARM_FRAMES, UnDefType.NULL);
        updateChannelState(CHANNEL_EVENT_LENGTH, UnDefType.NULL);
    }

    private void refreshImage() {
        if (isLinked(CHANNEL_IMAGE)) {
            getImage();
        } else {
            logger.trace("Monitor {}: Can't update image because '{}' channel is not linked", CHANNEL_IMAGE, monitorId);
        }
    }

    private void getImage() {
        ZmBridgeHandler localHandler = bridgeHandler;
        if (localHandler != null) {
            logger.debug("Monitor {}: Updating image channel", monitorId);
            RawType image = localHandler.getImage(monitorId, imageRefreshIntervalSeconds);
            updateChannelState(CHANNEL_IMAGE, image != null ? image : UnDefType.UNDEF);
        }
    }

    private void updateChannelState(String channelId, State state) {
        updateState(channelId, state);
        monitorStatusCache.put(channelId, state);
    }

    private void startImageRefreshJob() {
        stopImageRefreshJob();
        Integer interval = imageRefreshIntervalSeconds;
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
