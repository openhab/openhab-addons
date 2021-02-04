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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey.*;
import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.NULL_LOCAL_TIME;
import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.TIME_FORMATER;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingMode;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingPreference;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.Day;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargeProfile;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargingWindow;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.Timer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.WeeklyPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChargeProfileWrapper} Wrapper for ChargeProfiles
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - add ChargeProfileActions
 */
@NonNullByDefault
public class ChargeProfileWrapper {

    private final Logger logger = LoggerFactory.getLogger(ChargeProfileWrapper.class);

    public enum ProfileType {
        WEEKLY,
        TWO_TIMES,
        EMPTY
    }

    public enum ProfileKey {
        CLIMATE,
        TIMER1,
        TIMER2,
        TIMER3,
        OVERRIDE,
        WINDOWSTART,
        WINDOWEND
    }

    final protected ProfileType type;

    @Nullable
    private Optional<ChargingMode> mode;
    @Nullable
    private Optional<ChargingPreference> preference;

    private final Map<ProfileKey, Boolean> enabled = new HashMap<>();
    private final Map<ProfileKey, LocalTime> times = new HashMap<>();
    private final Map<ProfileKey, Set<Day>> daysOfWeek = new HashMap<>();

    public static Optional<ChargeProfileWrapper> fromJson(final String content) {
        return Optional.ofNullable(Converter.getGson().fromJson(content, ChargeProfile.class))
                .map(cp -> new ChargeProfileWrapper(cp));
    }

    private ChargeProfileWrapper(final ChargeProfile profile) {

        final WeeklyPlanner planner;

        if (profile.weeklyPlanner != null) {
            type = ProfileType.WEEKLY;
            planner = profile.weeklyPlanner;
        } else if (profile.twoTimesTimer != null) {
            type = ProfileType.TWO_TIMES;
            planner = profile.twoTimesTimer;
            // timer days not supported
        } else {
            type = ProfileType.EMPTY;
            return;
        }

        setPreference(planner.chargingPreferences);
        setMode(planner.chargingMode);

        setEnabled(CLIMATE, planner.climatizationEnabled);

        addTimer(TIMER1, planner.timer1);
        addTimer(TIMER2, planner.timer2);

        if (planner.preferredChargingWindow != null) {
            addTime(WINDOWSTART, planner.preferredChargingWindow.startTime);
            addTime(WINDOWEND, planner.preferredChargingWindow.endTime);
        } else {
            preference.ifPresent(pref -> {
                if (ChargingPreference.CHARGING_WINDOW.equals(pref)) {
                    addTime(WINDOWSTART, null);
                    addTime(WINDOWEND, null);
                }
            });
        }

        if (isWeekly()) {
            addTimer(TIMER3, planner.timer3);
            addTimer(OVERRIDE, planner.overrideTimer);
        }
    }

    public @Nullable Boolean isEnabled(final ProfileKey key) {
        return enabled.get(key);
    }

    public void setEnabled(final ProfileKey key, final boolean enabled) {
        this.enabled.put(key, enabled);
    }

    public @Nullable String getMode() {
        return mode.map(m -> m.name()).get();
    }

    public void setMode(final String mode) {
        try {
            this.mode = Optional.of(ChargingMode.valueOf(mode));
        } catch (IllegalArgumentException iae) {
            logger.warn("unexpected value for chargingMode: {}", mode);
            this.mode = Optional.empty();
        }
    }

    public @Nullable String getPreference() {
        return preference.map(pref -> pref.name()).get();
    }

    public void setPreference(final String preference) {
        try {
            this.preference = Optional.of(ChargingPreference.valueOf(preference));
        } catch (IllegalArgumentException iae) {
            logger.warn("unexpected value for chargingPreference: {}", preference);
            this.preference = Optional.empty();
        }
    }

    public @Nullable List<String> getDays(final ProfileKey key) {
        final Set<Day> daySet = daysOfWeek.get(key);
        if (daySet != null) {
            final ArrayList<String> days = new ArrayList<>();
            for (Day day : daySet) {
                days.add(day.name());
            }
            return days;
        }
        return null;
    }

    public void setDays(final ProfileKey key, @Nullable final List<String> days) {
        final EnumSet<Day> daySet = EnumSet.noneOf(Day.class);
        if (days != null) {
            for (String day : days) {
                try {
                    daySet.add(Day.valueOf(day));
                } catch (IllegalArgumentException iae) {
                    logger.warn("unexpected value for {} day: {}", key.name(), day);
                }
            }
        }
        daysOfWeek.put(key, daySet);
    }

    public void setDayEnabled(final ProfileKey key, final Day day, final boolean enabled) {
        Set<Day> days = daysOfWeek.get(key);
        if (days == null) {
            days = EnumSet.noneOf(Day.class);
            daysOfWeek.put(key, days);
        }
        if (enabled) {
            daysOfWeek.get(key).add(day);
        } else {
            daysOfWeek.get(key).remove(day);
        }
    }

    public @Nullable Boolean isDayEnabled(final ProfileKey key, final Day day) {
        final Set<Day> daySet = daysOfWeek.get(key);
        if (daySet != null) {
            return daySet.contains(day);
        }
        return null;
    }

    public @Nullable LocalTime getTime(final ProfileKey key) {
        return times.get(key);
    }

    public void setTime(final ProfileKey key, LocalTime time) {
        times.put(key, time);
    }

    public void setHour(final ProfileKey key, final int hour) {
        LocalTime dateTime = times.get(key);
        if (dateTime == null) {
            dateTime = NULL_LOCAL_TIME;
        }
        times.put(key, dateTime.withHour(hour));
    }

    public void setMinute(final ProfileKey key, int minute) {
        LocalTime dateTime = times.get(key);
        if (dateTime == null) {
            dateTime = NULL_LOCAL_TIME;
        }
        times.put(key, dateTime.withMinute(minute));
    }

    public String getJson() {
        final ChargeProfile profile = new ChargeProfile();
        final WeeklyPlanner planner = new WeeklyPlanner();

        preference.ifPresent(pref -> planner.chargingPreferences = pref.name());
        planner.climatizationEnabled = isEnabled(CLIMATE);
        preference.ifPresent(pref -> {
            if (ChargingPreference.CHARGING_WINDOW.equals(pref)) {
                planner.chargingMode = getMode();
                final LocalTime start = getTime(WINDOWSTART);
                final LocalTime end = getTime(WINDOWEND);
                if (start != null || end != null) {
                    planner.preferredChargingWindow = new ChargingWindow();
                    planner.preferredChargingWindow.startTime = start == null ? null : start.format(TIME_FORMATER);
                    planner.preferredChargingWindow.endTime = end == null ? null : end.format(TIME_FORMATER);
                }
            }
        });
        planner.timer1 = getTimer(TIMER1);
        planner.timer2 = getTimer(TIMER2);
        if (isWeekly()) {
            planner.timer3 = getTimer(TIMER3);
            planner.overrideTimer = getTimer(OVERRIDE);
            profile.weeklyPlanner = planner;
        } else if (isTwoTimes()) {
            profile.twoTimesTimer = planner;
        }
        return Converter.getGson().toJson(profile);
    }

    private void addTime(final ProfileKey key, @Nullable final String time) {
        try {
            times.put(key, time == null ? NULL_LOCAL_TIME : LocalTime.parse(time, TIME_FORMATER));
        } catch (DateTimeParseException dtpe) {
            logger.warn("unexpected value for {} time: {}", key.name(), time);
        }
    }

    private void addTimer(final ProfileKey key, @Nullable final Timer timer) {
        if (timer == null) {
            enabled.put(key, false);
            addTime(key, null);
            if (isWeekly()) {
                setDays(key, null);
            }
        } else {
            enabled.put(key, timer.timerEnabled);
            addTime(key, timer.departureTime);
            if (isWeekly()) {
                setDays(key, timer.weekdays);
            }
        }
    }

    private @Nullable Timer getTimer(final ProfileKey key) {
        final Timer timer = new Timer();
        timer.timerEnabled = enabled.get(key);
        final LocalTime time = times.get(key);
        timer.departureTime = time == null ? null : time.format(TIME_FORMATER);
        if (isWeekly()) {
            final Set<Day> days = daysOfWeek.get(key);
            if (days != null) {
                timer.weekdays = new ArrayList<>();
                for (Day day : days) {
                    timer.weekdays.add(day.name());
                }
            }
        }
        return timer.timerEnabled == null && timer.departureTime == null && timer.weekdays == null ? null : timer;
    }

    private boolean isWeekly() {
        return ProfileType.WEEKLY.equals(type);
    }

    private boolean isTwoTimes() {
        return ProfileType.TWO_TIMES.equals(type);
    }
}
