/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectTimerApi;
import org.openhab.binding.robonect.internal.model.TimerFull;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.Weekdays;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles reads, updates, and dynamic channels for timer API responses.
 *
 * @author Nico Brüttner - Initial timer endpoint handling
 */
@NonNullByDefault
final class RobonectTimerHandler {

    private static final Pattern TIMER_CHANNEL_PATTERN = Pattern.compile("timer-\\d+-.+");
    private static final Set<String> TIMER_SWITCH_SETTINGS = Set.of("enabled", "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday", "sunday");
    private final Logger logger = LoggerFactory.getLogger(RobonectTimerHandler.class);
    private final RobonectHandler handler;
    private final RobonectTimerApi timerApi;

    /**
     * Creates a timer component backed by the Thing handler.
     *
     * @param handler the {@link RobonectHandler} providing state updates
     * @param timerApi the timer API for the current client
     */
    RobonectTimerHandler(RobonectHandler handler, RobonectTimerApi timerApi) {
        this.handler = handler;
        this.timerApi = timerApi;
    }

    /**
     * Writes a supported timer setting and schedules a refresh after success.
     *
     * @param channelId the channel identifier
     * @param command the command received for the channel
     */
    void handleCommand(String channelId, Command command) {
        String[] parts = channelId.split("-");
        if (parts.length != 3) {
            return;
        }
        try {
            int timer = Integer.parseInt(parts[1]);
            String setting = parts[2];
            String value;
            String parameter;
            if (command instanceof OnOffType onOff && switch (setting) {
                case "enabled", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" -> true;
                default -> false;
            }) {
                parameter = switch (setting) {
                    case "enabled" -> "enable";
                    case "monday" -> "mo";
                    case "tuesday" -> "tu";
                    case "wednesday" -> "we";
                    case "thursday" -> "th";
                    case "friday" -> "fr";
                    case "saturday" -> "sa";
                    default -> "su";
                };
                value = onOff == OnOffType.ON ? "1" : "0";
            } else if (command instanceof DateTimeType dateTime && (setting.equals("start") || setting.equals("end"))) {
                LocalTime time = dateTime.getZonedDateTime(handler.getTimeZone()).toLocalTime();
                parameter = setting;
                value = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                if (TIMER_SWITCH_SETTINGS.contains(setting)) {
                    logger.debug("Ignoring timer command of type {}; OnOffType is expected for {}.",
                            command.getClass().getName(), channelId);
                } else if (setting.equals("start") || setting.equals("end")) {
                    logger.debug("Ignoring timer command of type {}; DateTimeType is expected for {}.",
                            command.getClass().getName(), channelId);
                }
                return;
            }
            handler.cancelCommandRefresh("timer");
            TimerInfo result = timerApi.updateTimer(timer, parameter, value);
            if (result.isSuccessful()) {
                TimerFull updatedTimer = result.getTimer();
                if (updatedTimer != null) {
                    updateTimerState(updatedTimer);
                }
                handler.scheduleCommandRefresh("timer", this::refreshTimersInfo);
            }
        } catch (NumberFormatException e) {
            logger.debug("Ignoring malformed timer channel {}", channelId);
        }
    }

    /** Reads timer configurations and reconciles their channels and states. */
    void refreshTimersInfo() {
        TimersInfo info = timerApi.getTimersInfo();
        @Nullable
        List<TimerFull> timers = info.getTimers();
        if (info.isSuccessful() && timers != null) {
            updateTimerChannels(timers);
            timers.forEach(this::updateTimerState);
        }
    }

    /**
     * Publishes the available fields for a timer with a valid identifier.
     *
     * @param timer the timer configuration to publish
     */
    private void updateTimerState(TimerFull timer) {
        Integer id = timer.getId();
        if (id == null || id < 0) {
            return;
        }
        String prefix = "timer-" + id + "-";
        if (timer.getEnabled() != null) {
            handler.setState(prefix + "enabled", OnOffType.from(timer.getEnabled()));
        }
        updateTimerTime(prefix + "start", timer.getStart());
        updateTimerTime(prefix + "end", timer.getEnd());
        Weekdays days = timer.getWeekdays();
        if (days == null) {
            return;
        }
        updateTimerDay(prefix, "monday", days.getMo());
        updateTimerDay(prefix, "tuesday", days.getTu());
        updateTimerDay(prefix, "wednesday", days.getWe());
        updateTimerDay(prefix, "thursday", days.getTh());
        updateTimerDay(prefix, "friday", days.getFr());
        updateTimerDay(prefix, "saturday", days.getSa());
        updateTimerDay(prefix, "sunday", days.getSu());
    }

    /**
     * Publishes a valid timer time using the configured time zone.
     *
     * @param channel the channel identifier
     * @param time the timer time in {@code HH:mm} format, or {@code null} if absent
     */
    private void updateTimerTime(String channel, @Nullable String time) {
        if (time != null && time.matches("\\d{2}:\\d{2}")) {
            int hours = Integer.parseInt(time.substring(0, 2));
            int minutes = Integer.parseInt(time.substring(3, 5));
            if (hours < 24 && minutes < 60) {
                handler.setState(channel, handler.toTimeOnlyDateTime(LocalTime.of(hours, minutes)));
            }
        }
    }

    /**
     * Publishes a reported weekday enablement value for a timer.
     *
     * @param prefix the channel identifier prefix
     * @param day the weekday suffix of the channel identifier
     * @param value whether the timer is enabled for that weekday, or {@code null} if absent
     */
    private void updateTimerDay(String prefix, String day, @Nullable Boolean value) {
        if (value != null) {
            handler.setState(prefix + day, OnOffType.from(value));
        }
    }

    /**
     * Reconciles dynamic timer channels for the reported valid timer identifiers.
     *
     * @param timers the timer configurations used to build channels
     */
    private void updateTimerChannels(List<TimerFull> timers) {
        Set<Integer> timerIds = new TreeSet<>();
        for (TimerFull timer : timers) {
            @Nullable
            Integer id = timer.getId();
            if (id != null && id >= 0) {
                timerIds.add(id);
            }
        }
        List<org.openhab.core.thing.Channel> channels = new ArrayList<>();
        for (Integer id : timerIds.stream().sorted().toList()) {
            String prefix = "timer-" + id + "-";
            String label = "Timer " + id + " ";
            channels.add(handler.createChannel(prefix + "enabled", "Switch", "timerEnabledType", label + "Enabled"));
            channels.add(handler.createChannel(prefix + "start", "DateTime", "timerTimeType", label + "Start Time"));
            channels.add(handler.createChannel(prefix + "end", "DateTime", "timerTimeType", label + "End Time"));
            for (String day : List.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")) {
                channels.add(handler.createChannel(prefix + day, "Switch", "timerDayType",
                        label + Character.toUpperCase(day.charAt(0)) + day.substring(1)));
            }
        }
        handler.updateDynamicChannels(TIMER_CHANNEL_PATTERN, channels);
    }
}
