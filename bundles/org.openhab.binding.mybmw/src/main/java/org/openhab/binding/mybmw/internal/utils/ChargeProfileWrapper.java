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
package org.openhab.binding.mybmw.internal.utils;

import static org.openhab.binding.mybmw.internal.utils.ChargeProfileWrapper.ProfileKey.*;
import static org.openhab.binding.mybmw.internal.utils.Constants.*;

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
import org.openhab.binding.mybmw.internal.MyBMWConstants.ChargingMode;
import org.openhab.binding.mybmw.internal.MyBMWConstants.ChargingPreference;
import org.openhab.binding.mybmw.internal.dto.charge.ChargeProfile;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingSettings;
import org.openhab.binding.mybmw.internal.dto.charge.ChargingWindow;
import org.openhab.binding.mybmw.internal.dto.charge.Time;
import org.openhab.binding.mybmw.internal.dto.charge.Timer;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeProfileWrapper.class);

    private static final String CHARGING_WINDOW = "chargingWindow";
    private static final String WEEKLY_PLANNER = "weeklyPlanner";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";

    public enum ProfileKey {
        CLIMATE,
        TIMER1,
        TIMER2,
        TIMER3,
        TIMER4,
        WINDOWSTART,
        WINDOWEND
    }

    private Optional<ChargingMode> mode = Optional.empty();
    private Optional<ChargingPreference> preference = Optional.empty();
    private Optional<String> controlType = Optional.empty();
    private Optional<ChargingSettings> chargeSettings = Optional.empty();

    private final Map<ProfileKey, Boolean> enabled = new HashMap<>();
    private final Map<ProfileKey, LocalTime> times = new HashMap<>();
    private final Map<ProfileKey, Set<DayOfWeek>> daysOfWeek = new HashMap<>();

    public ChargeProfileWrapper(final ChargeProfile profile) {
        setPreference(profile.chargingPreference);
        setMode(profile.chargingMode);
        controlType = Optional.of(profile.chargingControlType);
        chargeSettings = Optional.of(profile.chargingSettings);
        setEnabled(CLIMATE, profile.climatisationOn);

        addTimer(TIMER1, profile.getTimerId(1));
        addTimer(TIMER2, profile.getTimerId(2));
        if (profile.chargingControlType.equals(WEEKLY_PLANNER)) {
            addTimer(TIMER3, profile.getTimerId(3));
            addTimer(TIMER4, profile.getTimerId(4));
        }

        if (CHARGING_WINDOW.equals(profile.chargingPreference)) {
            addTime(WINDOWSTART, profile.reductionOfChargeCurrent.start);
            addTime(WINDOWEND, profile.reductionOfChargeCurrent.end);
        } else {
            preference.ifPresent(pref -> {
                if (ChargingPreference.chargingWindow.equals(pref)) {
                    addTime(WINDOWSTART, null);
                    addTime(WINDOWEND, null);
                }
            });
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

    public @Nullable String getControlType() {
        return controlType.get();
    }

    public @Nullable ChargingSettings getChargeSettings() {
        return chargeSettings.get();
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

    public LocalTime getTime(final ProfileKey key) {
        LocalTime t = times.get(key);
        if (t != null) {
            return t;
        } else {
            LOGGER.debug("Profile not valid - Key {} doesn't contain boolean value", key);
            return Constants.NULL_LOCAL_TIME;
        }
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

        preference.ifPresent(pref -> profile.chargingPreference = pref.name());
        profile.chargingControlType = controlType.get();
        Boolean enabledBool = isEnabled(CLIMATE);
        profile.climatisationOn = enabledBool == null ? false : enabledBool;
        preference.ifPresent(pref -> {
            if (ChargingPreference.chargingWindow.equals(pref)) {
                profile.chargingMode = getMode();
                final LocalTime start = getTime(WINDOWSTART);
                final LocalTime end = getTime(WINDOWEND);
                if (!start.equals(Constants.NULL_LOCAL_TIME) && !end.equals(Constants.NULL_LOCAL_TIME)) {
                    ChargingWindow cw = new ChargingWindow();
                    profile.reductionOfChargeCurrent = cw;
                    cw.start = new Time();
                    cw.start.hour = start.getHour();
                    cw.start.minute = start.getMinute();
                    cw.end = new Time();
                    cw.end.hour = end.getHour();
                    cw.end.minute = end.getMinute();
                }
            }
        });
        profile.departureTimes = new ArrayList<Timer>();
        profile.departureTimes.add(getTimer(TIMER1));
        profile.departureTimes.add(getTimer(TIMER2));
        if (profile.chargingControlType.equals(WEEKLY_PLANNER)) {
            profile.departureTimes.add(getTimer(TIMER3));
            profile.departureTimes.add(getTimer(TIMER4));
        }

        profile.chargingSettings = chargeSettings.get();
        return Converter.getGson().toJson(profile);
    }

    private void addTime(final ProfileKey key, @Nullable final Time time) {
        try {
            times.put(key, time == null ? NULL_LOCAL_TIME : LocalTime.parse(Converter.getTime(time), TIME_FORMATER));
        } catch (DateTimeParseException dtpe) {
            LOGGER.warn("unexpected value for {} time: {}", key.name(), time);
        }
    }

    private void addTimer(final ProfileKey key, @Nullable final Timer timer) {
        if (timer == null) {
            enabled.put(key, false);
            addTime(key, null);
            daysOfWeek.put(key, EnumSet.noneOf(DayOfWeek.class));
        } else {
            enabled.put(key, ACTIVATE.equals(timer.action));
            addTime(key, timer.timeStamp);
            final EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
            if (timer.timerWeekDays != null) {
                daysOfWeek.put(key, EnumSet.noneOf(DayOfWeek.class));
                for (String day : timer.timerWeekDays) {
                    try {
                        daySet.add(DayOfWeek.valueOf(day.toUpperCase()));
                    } catch (IllegalArgumentException iae) {
                        LOGGER.warn("unexpected value for {} day: {}", key.name(), day);
                    }
                    daysOfWeek.put(key, daySet);
                }
            }
        }
    }

    private Timer getTimer(final ProfileKey key) {
        final Timer timer = new Timer();
        switch (key) {
            case TIMER1:
                timer.id = 1;
                break;
            case TIMER2:
                timer.id = 2;
                break;
            case TIMER3:
                timer.id = 3;
                break;
            case TIMER4:
                timer.id = 4;
                break;
            default:
                // timer id stays -1
                break;
        }
        Boolean enabledBool = isEnabled(key);
        if (enabledBool != null) {
            timer.action = enabledBool ? ACTIVATE : DEACTIVATE;
        } else {
            timer.action = DEACTIVATE;
        }
        final LocalTime time = getTime(key);
        if (!time.equals(Constants.NULL_LOCAL_TIME)) {
            timer.timeStamp = new Time();
            timer.timeStamp.hour = time.getHour();
            timer.timeStamp.minute = time.getMinute();
        }
        final Set<DayOfWeek> days = daysOfWeek.get(key);
        if (days != null) {
            timer.timerWeekDays = new ArrayList<>();
            for (DayOfWeek day : days) {
                timer.timerWeekDays.add(day.name().toLowerCase());
            }
        }
        return timer;
    }
}
