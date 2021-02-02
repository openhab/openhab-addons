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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * The {@link ChargeProfileWrapper} Wrapper for ChargeProfiles
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - add ChargeProfileActions
 */
@NonNullByDefault
public class ChargeProfileWrapper {

    public static final DateTimeFormatter TIMEFORMATER = DateTimeFormatter.ofPattern("HH:mm");

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

    protected ProfileType type = ProfileType.EMPTY;

    @Nullable
    private ChargingMode mode;
    @Nullable
    private ChargingPreference preference;

    private final Map<ProfileKey, Boolean> enabled = new HashMap<>();
    private final Map<ProfileKey, LocalTime> times = new HashMap<>();
    private final Map<ProfileKey, Set<Day>> daysOfWeek = new HashMap<>();

    public static @Nullable ChargeProfileWrapper fromJson(final String content) {
        final ChargeProfile cp = Converter.getGson().fromJson(content, ChargeProfile.class);
        return cp == null ? null : new ChargeProfileWrapper(cp);
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

        preference = planner.chargingPreferences == null ? null
                : ChargingPreference.valueOf(planner.chargingPreferences);
        mode = planner.chargingMode == null ? null : ChargingMode.valueOf(planner.chargingMode);

        this.setEnabled(ProfileKey.CLIMATE, planner.climatizationEnabled);

        addTimer(ProfileKey.TIMER1, planner.timer1);
        addTimer(ProfileKey.TIMER2, planner.timer2);

        if (planner.preferredChargingWindow != null) {
            addTime(ProfileKey.WINDOWSTART, planner.preferredChargingWindow.startTime);
            addTime(ProfileKey.WINDOWEND, planner.preferredChargingWindow.endTime);
        } else if (ChargingPreference.CHARGING_WINDOW.equals(preference)) {
            addTime(ProfileKey.WINDOWSTART, Constants.NULL_TIME);
            addTime(ProfileKey.WINDOWEND, Constants.NULL_TIME);
        }

        if (ProfileType.WEEKLY.equals(type)) {
            addTimer(ProfileKey.TIMER3, planner.timer3);
            addTimer(ProfileKey.OVERRIDE, planner.overrideTimer);
        }
    }

    public @Nullable Boolean isEnabled(final ProfileKey key) {
        return enabled.get(key);
    }

    public void setEnabled(final ProfileKey key, final boolean enabled) {
        this.enabled.put(key, enabled);
    }

    public @Nullable String getMode() {
        return mode == null ? null : mode.name();
    }

    public void setMode(final String mode) {
        try {
            this.mode = ChargingMode.valueOf(mode);
        } catch (IllegalArgumentException iae) {
            this.mode = null;
        }
    }

    public @Nullable String getPreference() {
        return preference == null ? null : preference.name();
    }

    public void setPreference(final String preference) {
        try {
            this.preference = ChargingPreference.valueOf(preference);
        } catch (IllegalArgumentException iae) {
            this.preference = null;
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

    public void setDays(final ProfileKey key, final List<String> days) {
        final EnumSet<Day> daySet = EnumSet.noneOf(Day.class);
        for (String day : days) {
            daySet.add(Day.valueOf(day));
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
            dateTime = LocalTime.parse(Constants.NULL_TIME, TIMEFORMATER);
        }
        times.put(key, dateTime.withHour(hour));
    }

    public void setMinute(final ProfileKey key, int minute) {
        LocalTime dateTime = times.get(key);
        if (dateTime == null) {
            dateTime = LocalTime.parse(Constants.NULL_TIME, TIMEFORMATER);
        }
        times.put(key, dateTime.withMinute(minute));
    }

    public String getJson() {
        final ChargeProfile profile = new ChargeProfile();
        final WeeklyPlanner planner = new WeeklyPlanner();

        planner.chargingPreferences = preference == null ? null : preference.name();
        planner.climatizationEnabled = isEnabled(ProfileKey.CLIMATE);
        if (ChargingPreference.CHARGING_WINDOW.equals(preference)) {
            planner.chargingMode = getMode();
            final LocalTime start = getTime(ProfileKey.WINDOWSTART);
            final LocalTime end = getTime(ProfileKey.WINDOWEND);
            if (start != null || end != null) {
                planner.preferredChargingWindow = new ChargingWindow();
                planner.preferredChargingWindow.startTime = start == null ? null : start.format(TIMEFORMATER);
                planner.preferredChargingWindow.endTime = end == null ? null : end.format(TIMEFORMATER);
            }
        }
        planner.timer1 = getTimer(ProfileKey.TIMER1);
        planner.timer2 = getTimer(ProfileKey.TIMER2);
        if (ProfileType.WEEKLY.equals(type)) {
            planner.timer3 = getTimer(ProfileKey.TIMER3);
            planner.overrideTimer = getTimer(ProfileKey.OVERRIDE);
            profile.weeklyPlanner = planner;
        } else if (ProfileType.TWO_TIMES.equals(type)) {
            profile.twoTimesTimer = planner;
        }
        return Converter.getGson().toJson(profile);
    }

    private void addTime(final ProfileKey key, final String time) {
        times.put(key, LocalTime.parse(time, TIMEFORMATER));
    }

    private void addTimer(final ProfileKey key, @Nullable final Timer timer) {
        if (timer == null) {
            enabled.put(key, false);
            addTime(key, Constants.NULL_TIME);
            if (ProfileType.WEEKLY.equals(type)) {
                setDays(key, List.of());
            }
        } else {
            enabled.put(key, timer.timerEnabled);
            addTime(key, timer.departureTime == null ? Constants.NULL_TIME : timer.departureTime);
            if (timer.weekdays != null) {
                setDays(key, timer.weekdays);
            } else if (ProfileType.WEEKLY.equals(type)) {
                setDays(key, List.of());
            }
        }
    }

    private @Nullable Timer getTimer(final ProfileKey key) {
        final Timer timer = new Timer();
        timer.timerEnabled = enabled.get(key);
        final LocalTime time = times.get(key);
        timer.departureTime = time == null ? null : time.format(TIMEFORMATER);
        if (ProfileType.WEEKLY.equals(type)) {
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
}
