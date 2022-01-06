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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey.*;
import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingMode;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.ChargingPreference;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargeProfile;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.ChargingWindow;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.Timer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.charge.WeeklyPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link ChargeProfileWrapper} Wrapper for ChargeProfiles
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - add ChargeProfileActions
 */
@NonNullByDefault
public class ChargeProfileWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeProfileWrapper.class);

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
        TIMER4,
        OVERRIDE,
        WINDOWSTART,
        WINDOWEND
    }

    protected final ProfileType type;

    private Optional<ChargingMode> mode = Optional.empty();
    private Optional<ChargingPreference> preference = Optional.empty();

    private final Map<ProfileKey, Boolean> enabled = new HashMap<>();
    private final Map<ProfileKey, LocalTime> times = new HashMap<>();
    private final Map<ProfileKey, Set<DayOfWeek>> daysOfWeek = new HashMap<>();

    public static Optional<ChargeProfileWrapper> fromJson(final String content) {
        try {
            final ChargeProfile cp = Converter.getGson().fromJson(content, ChargeProfile.class);
            if (cp != null) {
                return Optional.of(new ChargeProfileWrapper(cp));
            }
        } catch (JsonSyntaxException jse) {
            LOGGER.debug("ChargeProfile unparsable: {}", content);
        }
        return Optional.empty();
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

    public void setEnabled(final ProfileKey key, @Nullable final Boolean enabled) {
        if (enabled == null) {
            this.enabled.remove(key);
        } else {
            this.enabled.put(key, enabled);
        }
    }

    public @Nullable String getMode() {
        return mode.map(m -> m.name()).orElse(null);
    }

    public void setMode(final @Nullable String mode) {
        if (mode != null) {
            try {
                this.mode = Optional.of(ChargingMode.valueOf(mode));
                return;
            } catch (IllegalArgumentException iae) {
                LOGGER.warn("unexpected value for chargingMode: {}", mode);
            }
        }
        this.mode = Optional.empty();
    }

    public @Nullable String getPreference() {
        return preference.map(pref -> pref.name()).orElse(null);
    }

    public void setPreference(final @Nullable String preference) {
        if (preference != null) {
            try {
                this.preference = Optional.of(ChargingPreference.valueOf(preference));
                return;
            } catch (IllegalArgumentException iae) {
                LOGGER.warn("unexpected value for chargingPreference: {}", preference);
            }
        }
        this.preference = Optional.empty();
    }

    public @Nullable Set<DayOfWeek> getDays(final ProfileKey key) {
        return daysOfWeek.get(key);
    }

    public void setDays(final ProfileKey key, final @Nullable Set<DayOfWeek> days) {
        if (days == null) {
            daysOfWeek.remove(key);
        } else {
            daysOfWeek.put(key, days);
        }
    }

    public void setDayEnabled(final ProfileKey key, final DayOfWeek day, final boolean enabled) {
        final Set<DayOfWeek> days = daysOfWeek.get(key);
        if (days == null) {
            daysOfWeek.put(key, enabled ? EnumSet.of(day) : EnumSet.noneOf(DayOfWeek.class));
        } else {
            if (enabled) {
                days.add(day);
            } else {
                days.remove(day);
            }
        }
    }

    public @Nullable LocalTime getTime(final ProfileKey key) {
        return times.get(key);
    }

    public void setTime(final ProfileKey key, @Nullable LocalTime time) {
        if (time == null) {
            times.remove(key);
        } else {
            times.put(key, time);
        }
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
            LOGGER.warn("unexpected value for {} time: {}", key.name(), time);
        }
    }

    private void addTimer(final ProfileKey key, @Nullable final Timer timer) {
        if (timer == null) {
            enabled.put(key, false);
            addTime(key, null);
            if (isWeekly()) {
                daysOfWeek.put(key, EnumSet.noneOf(DayOfWeek.class));
            }
        } else {
            enabled.put(key, timer.timerEnabled);
            addTime(key, timer.departureTime);
            if (isWeekly()) {
                final EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
                if (timer.weekdays != null) {
                    for (String day : timer.weekdays) {
                        try {
                            daySet.add(DayOfWeek.valueOf(day));
                        } catch (IllegalArgumentException iae) {
                            LOGGER.warn("unexpected value for {} day: {}", key.name(), day);
                        }
                    }
                }
                daysOfWeek.put(key, daySet);
            }
        }
    }

    private @Nullable Timer getTimer(final ProfileKey key) {
        final Timer timer = new Timer();
        timer.timerEnabled = enabled.get(key);
        final LocalTime time = times.get(key);
        timer.departureTime = time == null ? null : time.format(TIME_FORMATER);
        if (isWeekly()) {
            final Set<DayOfWeek> days = daysOfWeek.get(key);
            if (days != null) {
                timer.weekdays = new ArrayList<>();
                for (DayOfWeek day : days) {
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
