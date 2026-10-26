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
package org.openhab.binding.awtrixng.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.dto.Settings;
import org.openhab.binding.awtrixng.internal.dto.WeekdayBar;

/**
 * The {@link SettingsChannels} lists the channels of the clock that are plain fields of its settings, and how each one
 * maps to its field.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class SettingsChannels {

    private static final PayloadChannels<Settings> CHANNELS = new PayloadChannels<>();

    static {
        // App rotation
        CHANNELS.switchChannel("auto-transition", s -> s.autoTransition, (s, v) -> s.autoTransition = v);
        CHANNELS.textChannel("transition-effect", s -> s.transitionEffect, (s, v) -> s.transitionEffect = v);
        CHANNELS.textChannel("transition-direction", s -> s.transitionDirection, (s, v) -> s.transitionDirection = v);
        CHANNELS.durationChannel("transition-duration", s -> s.transitionDurationMs,
                (s, v) -> s.transitionDurationMs = v.intValue(), 0, Integer.MAX_VALUE);
        CHANNELS.durationChannel("app-duration", s -> s.appDurationMs, (s, v) -> s.appDurationMs = v.intValue(), 0,
                Integer.MAX_VALUE);
        CHANNELS.switchChannel("block-navigation", s -> s.blockNavigation, (s, v) -> s.blockNavigation = v);

        // Sound
        CHANNELS.percentChannel("volume", s -> s.volume, (s, v) -> s.volume = v, 0, 100);
        CHANNELS.percentChannel("app-volume", s -> s.appVolume, (s, v) -> s.appVolume = v, 0, 100);
        CHANNELS.percentChannel("alert-volume", s -> s.alertVolume, (s, v) -> s.alertVolume = v, 0, 100);

        // Clock and date text
        CHANNELS.numberChannel("time-mode", s -> s.timeMode, (s, v) -> s.timeMode = v, 0, 6);
        CHANNELS.switchChannel("time-24h", s -> s.time24h, (s, v) -> s.time24h = v);
        CHANNELS.switchChannel("time-leading-zero", s -> s.timeLeadingZero, (s, v) -> s.timeLeadingZero = v);
        CHANNELS.switchChannel("time-show-seconds", s -> s.timeShowSeconds, (s, v) -> s.timeShowSeconds = v);
        CHANNELS.switchChannel("time-show-ampm", s -> s.timeShowAmPm, (s, v) -> s.timeShowAmPm = v);
        CHANNELS.textChannel("time-separator", s -> s.timeSeparatorMode, (s, v) -> s.timeSeparatorMode = v);
        CHANNELS.textChannel("date-order", s -> s.dateOrder, (s, v) -> s.dateOrder = v);
        CHANNELS.textChannel("date-separator", s -> s.dateSeparator, (s, v) -> s.dateSeparator = v);
        CHANNELS.textChannel("date-year-mode", s -> s.dateYearMode, (s, v) -> s.dateYearMode = v);
        CHANNELS.switchChannel("date-show-weekday", s -> s.dateShowWeekday, (s, v) -> s.dateShowWeekday = v);
        CHANNELS.switchChannel("date-month-names", s -> s.dateMonthNames, (s, v) -> s.dateMonthNames = v);
        CHANNELS.switchChannel("use-celsius", s -> s.useCelsius, (s, v) -> s.useCelsius = v);
        CHANNELS.switchChannel("uppercase", s -> s.uppercase, (s, v) -> s.uppercase = v);

        // Colors
        CHANNELS.colorChannel("text-color", s -> s.textColor, (s, v) -> s.textColor = v);
        CHANNELS.colorChannel("time-color", s -> s.timeColor, (s, v) -> s.timeColor = v);
        CHANNELS.colorChannel("date-color", s -> s.dateColor, (s, v) -> s.dateColor = v);
        CHANNELS.colorChannel("temperature-color", s -> s.temperatureColor, (s, v) -> s.temperatureColor = v);
        CHANNELS.colorChannel("humidity-color", s -> s.humidityColor, (s, v) -> s.humidityColor = v);
        CHANNELS.colorChannel("battery-color", s -> s.batteryColor, (s, v) -> s.batteryColor = v);
        CHANNELS.colorChannel("calendar-header-color", s -> s.calendarHeaderColor, (s, v) -> s.calendarHeaderColor = v);
        CHANNELS.colorChannel("calendar-text-color", s -> s.calendarTextColor, (s, v) -> s.calendarTextColor = v);
        CHANNELS.colorChannel("calendar-body-color", s -> s.calendarBodyColor, (s, v) -> s.calendarBodyColor = v);

        // Weekday bar of the clock app
        CHANNELS.switchChannel("weekday-bar-show", s -> s.weekdayBar == null ? null : s.weekdayBar.show,
                (s, v) -> weekdayBar(s).show = v);
        CHANNELS.switchChannel("weekday-bar-start-on-monday",
                s -> s.weekdayBar == null ? null : s.weekdayBar.startOnMonday,
                (s, v) -> weekdayBar(s).startOnMonday = v);
        CHANNELS.colorChannel("weekday-bar-active-color", s -> s.weekdayBar == null ? null : s.weekdayBar.activeColor,
                (s, v) -> weekdayBar(s).activeColor = v);
        CHANNELS.colorChannel("weekday-bar-inactive-color",
                s -> s.weekdayBar == null ? null : s.weekdayBar.inactiveColor,
                (s, v) -> weekdayBar(s).inactiveColor = v);
    }

    private SettingsChannels() {
    }

    /**
     * @return the mapping for the channel, or null if the channel is not a plain settings field
     */
    public static @Nullable PayloadChannel<Settings> get(String channelId) {
        return CHANNELS.get(channelId);
    }

    public static Iterable<Map.Entry<String, PayloadChannel<Settings>>> all() {
        return CHANNELS.all();
    }

    private static WeekdayBar weekdayBar(Settings settings) {
        WeekdayBar bar = settings.weekdayBar;
        if (bar == null) {
            bar = new WeekdayBar();
            settings.weekdayBar = bar;
        }
        return bar;
    }
}
